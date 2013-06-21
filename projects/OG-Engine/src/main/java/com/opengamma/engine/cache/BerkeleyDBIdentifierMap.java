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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetReferenceVisitor;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

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
  private BlockingQueue<Request> _requests;

  private abstract static class Request {

    private boolean _done;

    protected final synchronized void waitFor() {
      try {
        while (!_done) {
          wait();
        }
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }
    }

    protected final synchronized void signal() {
      _done = true;
      notify();
    }

    protected abstract void runInTransaction(Worker worker);

  }

  private static final class PoisonRequest extends Request {

    @Override
    protected void runInTransaction(final Worker worker) {
      worker.poison(this);
    }

  }

  private final class Worker implements Runnable {

    private final DatabaseEntry _identifier = new DatabaseEntry();
    private final DatabaseEntry _valueSpec = new DatabaseEntry();
    private final BlockingQueue<Request> _requests;
    private Transaction _transaction;
    private PoisonRequest _poisoned;

    public Worker(final BlockingQueue<Request> requests) {
      _requests = requests;
    }

    public long getIdentifier(final ValueSpecification spec) {
      final byte[] specAsBytes = ValueSpecificationStringEncoder.encodeAsString(spec).getBytes(Charset.forName("UTF-8"));
      _valueSpec.setData(specAsBytes);
      OperationStatus status = _valueSpecificationToIdentifier.getDatabase().get(_transaction, _valueSpec, _identifier, LockMode.READ_COMMITTED);
      switch (status) {
        case NOTFOUND:
          return allocateNewIdentifier(spec, _transaction, _valueSpec);
        case SUCCESS:
          return LongBinding.entryToLong(_identifier);
        default:
          s_logger.warn("Unexpected operation status on load {}, assuming we have to insert a new record", status);
          return allocateNewIdentifier(spec, _transaction, _valueSpec);
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
      final OperationStatus status = _identifierToValueSpecification.getDatabase().get(_transaction, _identifier, _valueSpec, LockMode.READ_COMMITTED);
      if (status == OperationStatus.SUCCESS) {
        return convertByteArrayToSpecification(_valueSpec.getData());
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

    public void poison(final PoisonRequest request) {
      _poisoned = request;
    }

    @Override
    public void run() {
      s_logger.info("Worker started");
      _poisoned = null;
      do {
        Request req = null;
        try {
          req = _requests.take();
          if (req == null) {
            s_logger.info("Worker poisoned");
            return;
          }
          boolean rollback = true;
          _transaction = getDbEnvironment().beginTransaction(null, null);
          try {
            do {
              req.runInTransaction(this);
              req.signal();
              req = _requests.poll();
            } while (req != null);
            rollback = false;
          } finally {
            if (rollback) {
              s_logger.error("Rolling back transaction");
              _transaction.abort();
            } else {
              _transaction.commit();
            }
            _transaction = null;
          }
        } catch (Throwable t) {
          s_logger.error("Caught exception", t);
        } finally {
          if (req != null) {
            req.signal();
          }
        }
      } while (_poisoned == null);
      // If there are other workers, put the poison back in the queue for them
      _requests.add(_poisoned);
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

  private static final class GetIdentifierRequest extends Request {

    private final ValueSpecification _spec;
    private volatile long _result;

    public GetIdentifierRequest(final ValueSpecification spec) {
      _spec = spec;
    }

    @Override
    protected void runInTransaction(final Worker worker) {
      _result = worker.getIdentifier(_spec);
    }

    public long run(final Queue<Request> requests) {
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

  private static final class GetIdentifiersRequest extends Request {

    private final Collection<ValueSpecification> _specs;
    private Object2LongMap<ValueSpecification> _result;

    public GetIdentifiersRequest(final Collection<ValueSpecification> specs) {
      _specs = specs;
    }

    @Override
    protected void runInTransaction(final Worker worker) {
      _result = worker.getIdentifiers(_specs);
    }

    public Object2LongMap<ValueSpecification> run(final Queue<Request> requests) {
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

  private static final class GetValueSpecificationRequest extends Request {

    private final long _identifier;
    private ValueSpecification _result;

    public GetValueSpecificationRequest(final long identifier) {
      _identifier = identifier;
    }

    @Override
    protected void runInTransaction(final Worker worker) {
      _result = worker.getValueSpecification(_identifier);
    }

    public ValueSpecification run(final Queue<Request> requests) {
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

  private static final class GetValueSpecificationsRequest extends Request {

    private final long[] _identifiers;
    private Long2ObjectMap<ValueSpecification> _result;

    public GetValueSpecificationsRequest(final long[] identifiers) {
      _identifiers = identifiers;
    }

    @Override
    protected void runInTransaction(final Worker worker) {
      _result = worker.getValueSpecifications(_identifiers);
    }

    public Long2ObjectMap<ValueSpecification> run(final Queue<Request> requests) {
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

  /**
   * Creates a new ID for a specification and saves the ID and the spec in both databases.
   * 
   * @param valueSpec The specification
   * @param txn A running transaction
   * @param specKeyEntry A DB entry containing the encoded specification suitable for use as a key, not as a value
   * @return The new identifier
   */
  protected long allocateNewIdentifier(ValueSpecification valueSpec, Transaction txn, DatabaseEntry specKeyEntry) {
    final DatabaseEntry identifierEntry = new DatabaseEntry();
    final long identifier = _nextIdentifier.getAndIncrement();
    LongBinding.longToEntry(identifier, identifierEntry);
    // encode spec to binary using fudge so it can be saved as a value and read back out
    DatabaseEntry specValueEntry = new DatabaseEntry(convertSpecificationToByteArray(valueSpec));
    OperationStatus status = _identifierToValueSpecification.getDatabase().put(txn, identifierEntry, specValueEntry);
    if (status != OperationStatus.SUCCESS) {
      s_logger.error("Unable to write identifier {} -> specification {} - {}", identifier, valueSpec, status);
      throw new OpenGammaRuntimeException("Unable to write new identifier");
    }
    status = _valueSpecificationToIdentifier.getDatabase().put(txn, specKeyEntry, identifierEntry);
    if (status != OperationStatus.SUCCESS) {
      s_logger.error("Unable to write new value {} for spec {} - {}", identifier, valueSpec, status);
      throw new OpenGammaRuntimeException("Unable to write new value");
    }
    return identifier;
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
      _requests = new LinkedBlockingQueue<Request>();
      _valueSpecificationToIdentifier.start();
      _identifierToValueSpecification.start();
      // TODO: We can have multiple worker threads -- will that be good or bad?
      final Thread worker = new Thread(new Worker(_requests));
      worker.setName("BerkeleyDBIdentifierMap-Worker");
      worker.setDaemon(true);
      worker.start();
    }
  }

  @Override
  public synchronized void stop() {
    if (_requests != null) {
      _valueSpecificationToIdentifier.stop();
      _identifierToValueSpecification.stop();
      _requests.add(new PoisonRequest());
      _requests = null;
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
    if (properties instanceof ValueProperties.InfinitePropertiesImpl) {
      builder.append("INF");
    }
    if (properties instanceof ValueProperties.NearlyInfinitePropertiesImpl) {
      builder.append("INF-{");
      builder.append(new TreeSet<>(((ValueProperties.NearlyInfinitePropertiesImpl) properties).getWithout()));
      builder.append('}');
    } else {
      Map<String, Set<String>> props = Maps.newTreeMap();
      for (String propName : properties.getProperties()) {
        props.put(propName, Sets.newTreeSet(properties.getValues(propName)));
      }
      builder.append(props);
    }
  }

  private static void encodeAsString(final StringBuilder builder, final ComputationTargetSpecification targetSpec) {
    builder.append('(');
    builder.append(targetSpec.accept(s_refToString));
    builder.append(',');
    targetSpec.getType().accept(s_typeToString, builder);
    builder.append(')');
  }

}
