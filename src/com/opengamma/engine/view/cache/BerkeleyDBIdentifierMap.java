/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
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
public class BerkeleyDBIdentifierMap extends AbstractBerkeleyDBComponent implements IdentifierMap, Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(BerkeleyDBIdentifierMap.class);
  /**
   * The default name for the database in the provided environment.
   */
  protected static final String DEFAULT_DATABASE_NAME = "value_specification_identifier";
  private static final byte[] HIGHEST_VALUE_KEY = new byte[] {0};
  private static final DatabaseEntry HIGHEST_VALUE_DB_ENTRY = new DatabaseEntry(HIGHEST_VALUE_KEY);

  private final FudgeContext _fudgeContext;

  // Runtime state:
  private final AtomicLong _nextIdentifier = new AtomicLong(1L);

  public BerkeleyDBIdentifierMap(Environment dbEnvironment, String databaseName, FudgeContext fudgeContext) {
    super(dbEnvironment, databaseName);
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    _fudgeContext = fudgeContext;
  }

  /**
   * Gets the fudgeContext field.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public long getIdentifier(ValueSpecification spec) {
    ArgumentChecker.notNull(spec, "spec");
    if (!isRunning()) {
      s_logger.info("Starting on first call as wasn't called as part of lifecycle interface");
      start();
    }

    long result = -1L;

    byte[] specAsBytes = convertSpecificationToByteArray(spec);
    DatabaseEntry specEntry = new DatabaseEntry(specAsBytes);
    DatabaseEntry valueEntry = new DatabaseEntry();
    // Now we open the transaction.
    TransactionConfig txnConfig = new TransactionConfig();
    txnConfig.setSync(false);
    Transaction txn = getDbEnvironment().beginTransaction(null, txnConfig);
    boolean rollback = true;
    try {
      OperationStatus status = getDatabase().get(txn, specEntry, valueEntry, LockMode.READ_COMMITTED);
      switch (status) {
        case NOTFOUND:
          result = allocateNewIdentifier(spec, txn, specEntry);
          break;
        case SUCCESS:
          result = LongBinding.entryToLong(valueEntry);
          break;
        default:
          s_logger.warn("Unexpected operation status on load {}, assuming we have to insert a new record", status);
          result = allocateNewIdentifier(spec, txn, specEntry);
          break;
      }
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

  protected long allocateNewIdentifier(ValueSpecification valueSpec, Transaction txn, DatabaseEntry specEntry) {
    DatabaseEntry valueEntry = new DatabaseEntry();
    long freshIdentifier = _nextIdentifier.getAndIncrement();
    // s_logger.debug("Allocating identifier {} to {}", freshIdentifier, valueSpec);
    LongBinding.longToEntry(freshIdentifier, valueEntry);
    OperationStatus putStatus = getDatabase().put(txn, specEntry, valueEntry);
    if (putStatus != OperationStatus.SUCCESS) {
      s_logger.error("Unable to write new value {} for spec {} - {}", new Object[] {freshIdentifier, valueSpec, putStatus});
      throw new OpenGammaRuntimeException("Unable to write new value");
    }

    putStatus = getDatabase().put(txn, HIGHEST_VALUE_DB_ENTRY, valueEntry);
    if (putStatus != OperationStatus.SUCCESS) {
      s_logger.error("Unable to write new value {} as highest spec", freshIdentifier);
      throw new OpenGammaRuntimeException("Unable to update highest value");
    }

    return freshIdentifier;
  }

  protected byte[] convertSpecificationToByteArray(ValueSpecification valueSpec) {
    // REVIEW kirk 2010-07-31 -- Cache the serialization context?
    FudgeSerializationContext serContext = new FudgeSerializationContext(getFudgeContext());
    FudgeFieldContainer msg = valueSpec.toFudgeMsg(serContext);
    byte[] specAsBytes = getFudgeContext().toByteArray(msg);
    return specAsBytes;
  }

  @Override
  protected DatabaseConfig getDatabaseConfig() {
    DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(true);
    dbConfig.setTransactional(true);
    return dbConfig;
  }

  @Override
  protected void postStartInitialization() {
    initializeNextIdentifier();
  }

  /**
   * 
   */
  protected void initializeNextIdentifier() {
    TransactionConfig txnConfig = new TransactionConfig();
    txnConfig.setSync(false);
    Transaction txn = getDbEnvironment().beginTransaction(null, txnConfig);
    long nextIdentifier = 1L;
    try {
      DatabaseEntry valueEntry = new DatabaseEntry();
      OperationStatus status = getDatabase().get(txn, HIGHEST_VALUE_DB_ENTRY, valueEntry, LockMode.READ_COMMITTED);
      if (status == OperationStatus.SUCCESS) {
        long previousHighest = LongBinding.entryToLong(valueEntry);
        nextIdentifier = previousHighest + 1;
        s_logger.info("Loaded previous highest value of {}, setting next identifier to {}", previousHighest, nextIdentifier);
      }
    } finally {
      txn.commit();
    }
    _nextIdentifier.set(nextIdentifier);
  }

  @Override
  public Map<ValueSpecification, Long> getIdentifiers(Collection<ValueSpecification> specs) {
    return AbstractIdentifierMap.getIdentifiers(this, specs);
  }

  @Override
  public ValueSpecification getValueSpecification(long identifier) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<Long, ValueSpecification> getValueSpecifications(Collection<Long> identifiers) {
    return AbstractIdentifierMap.getValueSpecifications(this, identifiers);
  }

}
