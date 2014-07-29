/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.AbstractBerkeleyDBWorker.PoisonRequest;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.metric.OpenGammaMetricRegistry;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * An implementation of {@link IdentifierMap} that backs all lookups in a Berkeley DB table. Internally, it maintains an {@link AtomicLong} to allocate the next identifier to be used.
 */
public class BerkeleyDBIdentifierMap implements IdentifierMap, Lifecycle {

  private static final Logger s_logger = LoggerFactory.getLogger(BerkeleyDBIdentifierMap.class);

  private static final String VALUE_SPECIFICATION_TO_IDENTIFIER_DATABASE = "value_specification_identifier";
  private static final String IDENTIFIER_TO_VALUE_SPECIFICATION_DATABASE = "identifier_value_specification";

  private final FudgeContext _fudgeContext;
  private final AbstractBerkeleyDBComponent _valueSpecificationToIdentifier;
  private final AbstractBerkeleyDBComponent _identifierToValueSpecification;
  private final AtomicLong _nextIdentifier = new AtomicLong(1L);
  private final Meter _newIdentifierMeter;
  private final Timer _getIdentifierTimer;
  private Thread _worker;
  private BlockingQueue<AbstractBerkeleyDBWorker.Request> _requests;

  private final class Worker extends AbstractBerkeleyDBWorker {

    private final DatabaseEntry _identifier = new DatabaseEntry();
    private final DatabaseEntry _valueSpecKey = new DatabaseEntry();
    private final DatabaseEntry _valueSpecValue = new DatabaseEntry();

    public Worker(final BlockingQueue<Request> requests) {
      super(getDbEnvironment(), requests);
    }

    protected long allocateNewIdentifier(final ValueSpecification valueSpec) {
      _newIdentifierMeter.mark();
      final long identifier = _nextIdentifier.getAndIncrement();
      LongBinding.longToEntry(identifier, _identifier);
      // encode spec to binary using fudge so it can be saved as a value and read back out
      _valueSpecValue.setData(convertSpecificationToByteArray(valueSpec));
      OperationStatus status = _identifierToValueSpecification.getDatabase().put(getTransaction(), _identifier, _valueSpecValue);
      if (status != OperationStatus.SUCCESS) {
        s_logger.error("Unable to write identifier {} -> specification {} - {}", identifier, valueSpec, status);
        throw new OpenGammaRuntimeException("Unable to write new identifier");
      }
      status = _valueSpecificationToIdentifier.getDatabase().put(getTransaction(), _valueSpecKey, _identifier);
      if (status != OperationStatus.SUCCESS) {
        s_logger.error("Unable to write new value {} for spec {} - {}", identifier, valueSpec, status);
        throw new OpenGammaRuntimeException("Unable to write new value");
      }
      return identifier;
    }

    public long getIdentifier(final ValueSpecification spec) {
      try (Timer.Context context = _getIdentifierTimer.time()) {
        final byte[] specAsBytes = ValueSpecificationStringEncoder.encodeAsString(spec).getBytes(Charset.forName("UTF-8"));
        _valueSpecKey.setData(specAsBytes);
        OperationStatus status = _valueSpecificationToIdentifier.getDatabase().get(getTransaction(), _valueSpecKey, _identifier, LockMode.READ_COMMITTED);
        switch (status) {
          case NOTFOUND:
            return allocateNewIdentifier(spec);
          case SUCCESS:
            return LongBinding.entryToLong(_identifier);
          default:
            s_logger.warn("Unexpected operation status on load {}, assuming we have to insert a new record", status);
            return allocateNewIdentifier(spec);
        }
      }
    }

    public Object2LongMap<ValueSpecification> getIdentifiers(final Collection<ValueSpecification> specs) {
      final Object2LongMap<ValueSpecification> result = new Object2LongOpenHashMap<ValueSpecification>();
      for (ValueSpecification spec : specs) {
        result.put(spec, getIdentifier(spec));
      }
      return result;
    }

    public ValueSpecification getValueSpecification(final long identifier) {
      LongBinding.longToEntry(identifier, _identifier);
      final OperationStatus status = _identifierToValueSpecification.getDatabase().get(getTransaction(), _identifier, _valueSpecValue, LockMode.READ_COMMITTED);
      if (status == OperationStatus.SUCCESS) {
        return convertByteArrayToSpecification(_valueSpecValue.getData());
      }
      s_logger.warn("Couldn't resolve identifier {} - {}", identifier, status);
      return null;
    }

    public Long2ObjectMap<ValueSpecification> getValueSpecifications(final long[] identifiers) {
      final Long2ObjectMap<ValueSpecification> result = new Long2ObjectOpenHashMap<ValueSpecification>();
      for (long identifier : identifiers) {
        result.put(identifier, getValueSpecification(identifier));
      }
      return result;
    }

  }

  public BerkeleyDBIdentifierMap(final Environment dbEnvironment, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(dbEnvironment, "dbEnvironment");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
    _valueSpecificationToIdentifier = new AbstractBerkeleyDBComponent(dbEnvironment, VALUE_SPECIFICATION_TO_IDENTIFIER_DATABASE) {
      @Override
      protected DatabaseConfig getDatabaseConfig() {
        return BerkeleyDBIdentifierMap.this.getDatabaseConfig();
      }
    };
    _identifierToValueSpecification = new AbstractBerkeleyDBComponent(dbEnvironment, IDENTIFIER_TO_VALUE_SPECIFICATION_DATABASE) {
      @Override
      protected DatabaseConfig getDatabaseConfig() {
        return BerkeleyDBIdentifierMap.this.getDatabaseConfig();
      }

      @Override
      protected void postStartInitialization() {
        _nextIdentifier.set(_identifierToValueSpecification.getDatabase().count() + 1);
      }
    };
    _newIdentifierMeter = OpenGammaMetricRegistry.getDetailedInstance().meter("BerkeleyDBIdentifierMap.newIdentifier");
    _getIdentifierTimer = OpenGammaMetricRegistry.getDetailedInstance().timer("BerkeleyDBIdentifierMap.getIdentifier");
  }

  /**
   * Gets the fudgeContext field.
   * 
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected Environment getDbEnvironment() {
    // The same environment is passed to both databases, so choice here is arbitrary
    return _valueSpecificationToIdentifier.getDbEnvironment();
  }

  private static final class GetIdentifierRequest extends Worker.Request {

    private final ValueSpecification _spec;
    private volatile long _result;

    public GetIdentifierRequest(final ValueSpecification spec) {
      _spec = spec;
    }

    @Override
    protected void runInTransaction(final AbstractBerkeleyDBWorker worker) {
      _result = ((Worker) worker).getIdentifier(_spec);
    }

    public long run(final Queue<Worker.Request> requests) {
      requests.add(this);
      waitFor();
      return _result;
    }

  }

  @Override
  public long getIdentifier(final ValueSpecification spec) {
    ArgumentChecker.notNull(spec, "spec");
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    return new GetIdentifierRequest(spec).run(_requests);
  }

  private static final class GetIdentifiersRequest extends Worker.Request {

    private final Collection<ValueSpecification> _specs;
    private Object2LongMap<ValueSpecification> _result;

    public GetIdentifiersRequest(final Collection<ValueSpecification> specs) {
      _specs = specs;
    }

    @Override
    protected void runInTransaction(final AbstractBerkeleyDBWorker worker) {
      _result = ((Worker) worker).getIdentifiers(_specs);
    }

    public Object2LongMap<ValueSpecification> run(final Queue<Worker.Request> requests) {
      requests.add(this);
      waitFor();
      return _result;
    }

  }

  @Override
  public Object2LongMap<ValueSpecification> getIdentifiers(Collection<ValueSpecification> specs) {
    ArgumentChecker.notNull(specs, "specs");
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    return new GetIdentifiersRequest(specs).run(_requests);
  }

  private static final class GetValueSpecificationRequest extends Worker.Request {

    private final long _identifier;
    private ValueSpecification _result;

    public GetValueSpecificationRequest(final long identifier) {
      _identifier = identifier;
    }

    @Override
    protected void runInTransaction(final AbstractBerkeleyDBWorker worker) {
      _result = ((Worker) worker).getValueSpecification(_identifier);
    }

    public ValueSpecification run(final Queue<Worker.Request> requests) {
      requests.add(this);
      waitFor();
      return _result;
    }

  }

  @Override
  public ValueSpecification getValueSpecification(final long identifier) {
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    return new GetValueSpecificationRequest(identifier).run(_requests);
  }

  private static final class GetValueSpecificationsRequest extends Worker.Request {

    private final long[] _identifiers;
    private Long2ObjectMap<ValueSpecification> _result;

    public GetValueSpecificationsRequest(final long[] identifiers) {
      _identifiers = identifiers;
    }

    @Override
    protected void runInTransaction(final AbstractBerkeleyDBWorker worker) {
      _result = ((Worker) worker).getValueSpecifications(_identifiers);
    }

    public Long2ObjectMap<ValueSpecification> run(final Queue<Worker.Request> requests) {
      requests.add(this);
      waitFor();
      return _result;
    }

  }

  @Override
  public Long2ObjectMap<ValueSpecification> getValueSpecifications(LongCollection identifiers) {
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    return new GetValueSpecificationsRequest(identifiers.toLongArray()).run(_requests);
  }

  protected byte[] convertSpecificationToByteArray(ValueSpecification valueSpec) {
    FudgeMsg msg = getFudgeContext().toFudgeMsg(valueSpec).getMessage();
    return getFudgeContext().toByteArray(msg);
  }

  protected ValueSpecification convertByteArrayToSpecification(final byte[] specAsBytes) {
    final FudgeDeserializer deserializer = new FudgeDeserializer(getFudgeContext());
    return deserializer.fudgeMsgToObject(ValueSpecification.class, getFudgeContext().deserialize(specAsBytes).getMessage());
  }

  protected DatabaseConfig getDatabaseConfig() {
    DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(true);
    dbConfig.setTransactional(true);
    return dbConfig;
  }

  @Override
  public synchronized boolean isRunning() {
    return _requests != null;
  }

  @Override
  public synchronized void start() {
    if (_requests == null) {
      _requests = new LinkedBlockingQueue<Worker.Request>();
      _valueSpecificationToIdentifier.start();
      _identifierToValueSpecification.start();
      // TODO: We can have multiple worker threads -- will that be good or bad?
      _worker = new Thread(new Worker(_requests));
      _worker.setName("BerkeleyDBIdentifierMap-Worker");
      _worker.setDaemon(true);
      _worker.start();
    }
  }

  @Override
  public synchronized void stop() {
    if (_requests != null) {
      _valueSpecificationToIdentifier.stop();
      _identifierToValueSpecification.stop();
      _requests.add(new PoisonRequest());
      _requests = null;
      try {
        _worker.join(5000L);
      } catch (InterruptedException ie) {
        s_logger.warn("Interrupted while waiting for worker to finish.");
      }
      _worker = null;
    }
  }

}

/**
 * Creates a string representation of a {@link ValueSpecification}. The same string will be produced for different {@link ValueSpecification} instances if they are logically equal. This isn't
 * necessarily true of Fudge encoding which can produce a different binary encoding for equal specifications. The format produced by this class isn't intended to be parsed, it's only needed to produce
 * a unique binary key for a specification. The readability is only intended to help debugging.
 */
/* package */class ValueSpecificationStringEncoder {

  private static final ComputationTargetTypeVisitor<StringBuilder, Void> s_typeToString = new ComputationTargetTypeVisitor<StringBuilder, Void>() {

    @Override
    public Void visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final StringBuilder builder) {
      final String[] typeStrings = new String[types.size()];
      final StringBuilder tmp = new StringBuilder();
      int index = 0;
      for (ComputationTargetType type : types) {
        tmp.delete(0, tmp.length());
        type.accept(this, tmp);
        typeStrings[index++] = tmp.toString();
      }
      Arrays.sort(typeStrings);
      for (int i = 0; i < typeStrings.length; i++) {
        if (i == 0) {
          builder.append('{');
        } else {
          builder.append(',');
        }
        builder.append(typeStrings[i]);
      }
      builder.append('}');
      return null;
    }

    @Override
    public Void visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final StringBuilder builder) {
      builder.append('[');
      boolean comma = false;
      for (ComputationTargetType type : types) {
        if (comma) {
          builder.append(',');
        } else {
          comma = true;
        }
        type.accept(this, builder);
      }
      builder.append(']');
      return null;
    }

    @Override
    public Void visitNullComputationTargetType(final StringBuilder builder) {
      builder.append("NULL");
      return null;
    }

    @Override
    public Void visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final StringBuilder builder) {
      builder.append(type.getName());
      return null;
    }

  };

  private static final ComputationTargetReferenceVisitor<String> s_refToString = new ComputationTargetReferenceVisitor<String>() {

    private String createResult(final ComputationTargetReference reference, final String toString) {
      if (reference.getParent() != null) {
        final StringBuilder sb = new StringBuilder(reference.getParent().accept(s_refToString));
        return sb.append(',').append(toString).toString();
      } else {
        return toString;
      }
    }

    @Override
    public String visitComputationTargetRequirement(final ComputationTargetRequirement requirement) {
      return createResult(requirement, requirement.getIdentifiers().toString());
    }

    @Override
    public String visitComputationTargetSpecification(final ComputationTargetSpecification specification) {
      if (specification.getUniqueId() != null) {
        return createResult(specification, specification.getUniqueId().toString());
      } else {
        return "NULL";
      }
    }

  };

  /* package */static String encodeAsString(ValueSpecification valueSpec) {
    final StringBuilder builder = new StringBuilder(valueSpec.getValueName());
    builder.append(',');
    encodeAsString(builder, valueSpec.getProperties());
    builder.append(',');
    encodeAsString(builder, valueSpec.getTargetSpecification());
    return builder.toString();
  }

  private static void encodeAsString(final StringBuilder builder, final ValueProperties properties) {
    if (properties == ValueProperties.all()) {
      builder.append("INF");
      return;
    }
    if (ValueProperties.isNearInfiniteProperties(properties)) {
      builder.append("INF-");
      final List<String> values = new ArrayList<String>(ValueProperties.all().getUnsatisfied(properties));
      Collections.sort(values);
      builder.append(values);
      return;
    }
    Map<String, Set<String>> props = Maps.newTreeMap();
    for (String propName : properties.getProperties()) {
      props.put(propName, Sets.newTreeSet(properties.getValues(propName)));
    }
    builder.append(props);
  }

  private static void encodeAsString(final StringBuilder builder, final ComputationTargetSpecification targetSpec) {
    builder.append('(');
    builder.append(targetSpec.accept(s_refToString));
    builder.append(',');
    targetSpec.getType().accept(s_typeToString, builder);
    builder.append(')');
  }

}
