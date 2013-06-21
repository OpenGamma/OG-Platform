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
import java.util.Set;
import java.util.TreeSet;
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
import com.sleepycat.je.TransactionConfig;

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

  // Runtime state:
  private final AtomicLong _nextIdentifier = new AtomicLong(1L);

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

  @Override
  public long getIdentifier(ValueSpecification spec) {
    ArgumentChecker.notNull(spec, "spec");
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    // Now we open the transaction.
    TransactionConfig txnConfig = new TransactionConfig();
    txnConfig.setSync(false);
    Transaction txn = getDbEnvironment().beginTransaction(null, txnConfig);
    long result;
    boolean rollback = true;
    try {
      result = getIdentifierImpl(txn, spec, new DatabaseEntry());
      txn.commit();
      rollback = false;
    } finally {
      if (rollback) {
        s_logger.error("Rolling back transaction getting identifier for {}", spec);
        txn.abort();
      }
    }
    return result;
  }

  @Override
  public Object2LongMap<ValueSpecification> getIdentifiers(Collection<ValueSpecification> specs) {
    ArgumentChecker.notNull(specs, "specs");
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    // Now we open the transaction.
    TransactionConfig txnConfig = new TransactionConfig();
    txnConfig.setSync(false);
    Transaction txn = getDbEnvironment().beginTransaction(null, txnConfig);
    final Object2LongMap<ValueSpecification> result = new Object2LongOpenHashMap<>();
    boolean rollback = true;
    try {
      final DatabaseEntry identifierEntry = new DatabaseEntry();
      for (ValueSpecification spec : specs) {
        result.put(spec, getIdentifierImpl(txn, spec, identifierEntry));
      }
      txn.commit();
      rollback = false;
    } finally {
      if (rollback) {
        s_logger.error("Rolling back transaction getting identifiers");
        txn.abort();
      }
    }
    return result;
  }

  protected long getIdentifierImpl(final Transaction txn, final ValueSpecification spec, final DatabaseEntry identifierEntry) {
    byte[] specAsBytes = ValueSpecificationStringEncoder.encodeAsString(spec).getBytes(Charset.forName("UTF-8"));
    DatabaseEntry specEntry = new DatabaseEntry(specAsBytes);
    OperationStatus status = _valueSpecificationToIdentifier.getDatabase().get(txn, specEntry, identifierEntry, LockMode.READ_COMMITTED);
    switch (status) {
      case NOTFOUND:
        return allocateNewIdentifier(spec, txn, specEntry);
      case SUCCESS:
        return LongBinding.entryToLong(identifierEntry);
      default:
        s_logger.warn("Unexpected operation status on load {}, assuming we have to insert a new record", status);
        return allocateNewIdentifier(spec, txn, specEntry);
    }
  }

  @Override
  public ValueSpecification getValueSpecification(final long identifier) {
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    final Transaction txn = getDbEnvironment().beginTransaction(null, null);
    try {
      return getValueSpecificationImpl(txn, identifier, new DatabaseEntry(), new DatabaseEntry());
    } finally {
      txn.commitNoSync();
    }
  }

  @Override
  public Long2ObjectMap<ValueSpecification> getValueSpecifications(LongCollection identifiers) {
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    final Transaction txn = getDbEnvironment().beginTransaction(null, null);
    try {
      final Long2ObjectMap<ValueSpecification> result = new Long2ObjectOpenHashMap<ValueSpecification>();
      final DatabaseEntry identifierEntry = new DatabaseEntry();
      final DatabaseEntry valueSpecEntry = new DatabaseEntry();
      for (long identifier : identifiers) {
        result.put(identifier, getValueSpecificationImpl(txn, identifier, identifierEntry, valueSpecEntry));
      }
      return result;
    } finally {
      txn.commitNoSync();
    }
  }

  protected ValueSpecification getValueSpecificationImpl(final Transaction txn, final long identifier, final DatabaseEntry identifierEntry, final DatabaseEntry valueSpecEntry) {
    LongBinding.longToEntry(identifier, identifierEntry);
    OperationStatus status = _identifierToValueSpecification.getDatabase().get(txn, identifierEntry, valueSpecEntry, LockMode.READ_COMMITTED);
    if (status == OperationStatus.SUCCESS) {
      return convertByteArrayToSpecification(valueSpecEntry.getData());
    }
    s_logger.warn("Couldn't resolve identifier {} - {}", identifier, status);
    return null;
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
  public boolean isRunning() {
    return _valueSpecificationToIdentifier.isRunning() && _identifierToValueSpecification.isRunning();
  }

  @Override
  public void start() {
    _valueSpecificationToIdentifier.start();
    _identifierToValueSpecification.start();
  }

  @Override
  public void stop() {
    _valueSpecificationToIdentifier.stop();
    _identifierToValueSpecification.stop();
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
