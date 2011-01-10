/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueSpecification;
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
 * An implementation of {@link IdentifierMap} that backs all lookups in a
 * Berkeley DB table.
 * Internally, it maintains an {@link AtomicLong} to allocate the next identifier to be used.
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
  public Map<ValueSpecification, Long> getIdentifiers(Collection<ValueSpecification> specs) {
    ArgumentChecker.notNull(specs, "specs");
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    // Now we open the transaction.
    TransactionConfig txnConfig = new TransactionConfig();
    txnConfig.setSync(false);
    Transaction txn = getDbEnvironment().beginTransaction(null, txnConfig);
    final Map<ValueSpecification, Long> result = new HashMap<ValueSpecification, Long>();
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
    byte[] specAsBytes = convertSpecificationToByteArray(spec);
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
  public Map<Long, ValueSpecification> getValueSpecifications(Collection<Long> identifiers) {
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }
    final Transaction txn = getDbEnvironment().beginTransaction(null, null);
    try {
      final Map<Long, ValueSpecification> result = new HashMap<Long, ValueSpecification>();
      final DatabaseEntry identifierEntry = new DatabaseEntry();
      final DatabaseEntry valueSpecEntry = new DatabaseEntry();
      for (Long identifier : identifiers) {
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

  protected long allocateNewIdentifier(ValueSpecification valueSpec, Transaction txn, DatabaseEntry specEntry) {
    final DatabaseEntry identifierEntry = new DatabaseEntry();
    final long identifier = _nextIdentifier.getAndIncrement();
    LongBinding.longToEntry(identifier, identifierEntry);
    OperationStatus status = _identifierToValueSpecification.getDatabase().put(txn, identifierEntry, specEntry);
    if (status != OperationStatus.SUCCESS) {
      s_logger.error("Unable to write identifier {} -> specification {} - {}", new Object[] {identifier, valueSpec, status});
      throw new OpenGammaRuntimeException("Unable to write new identifier");
    }
    status = _valueSpecificationToIdentifier.getDatabase().put(txn, specEntry, identifierEntry);
    if (status != OperationStatus.SUCCESS) {
      s_logger.error("Unable to write new value {} for spec {} - {}", new Object[] {identifier, valueSpec, status});
      throw new OpenGammaRuntimeException("Unable to write new value");
    }
    return identifier;
  }

  protected byte[] convertSpecificationToByteArray(ValueSpecification valueSpec) {
    FudgeFieldContainer msg = getFudgeContext().toFudgeMsg(valueSpec).getMessage();
    byte[] specAsBytes = getFudgeContext().toByteArray(msg);
    return specAsBytes;
  }

  protected ValueSpecification convertByteArrayToSpecification(final byte[] specAsBytes) {
    final FudgeDeserializationContext context = new FudgeDeserializationContext(getFudgeContext());
    return context.fudgeMsgToObject(ValueSpecification.class, getFudgeContext().deserialize(specAsBytes).getMessage());
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
