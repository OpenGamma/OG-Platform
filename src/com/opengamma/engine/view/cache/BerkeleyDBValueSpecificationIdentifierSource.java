/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueSpecification;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

/**
 * An implementation of {@link ValueSpecificationIdentifierSource} that backs all lookups in a
 * Berkeley DB table.
 * Internally, it maintains an {@link AtomicLong} to allocate the next identifier to be used.
 */
public class BerkeleyDBValueSpecificationIdentifierSource implements ValueSpecificationIdentifierSource, Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(BerkeleyDBValueSpecificationIdentifierSource.class);
  /**
   * The default name for the database in the provided environment.
   */
  protected static final String DEFAULT_DATABASE_NAME = "value_specification_identifier";
  
  // Injected inputs:
  private final Environment _dbEnvironment;
  private final String _databaseName;
  private final FudgeContext _fudgeContext;

  // Runtime state:
  private final AtomicBoolean _started = new AtomicBoolean(false);
  private final AtomicLong _nextIdentifier = new AtomicLong(1L);
  private Database _database; 
  
  public BerkeleyDBValueSpecificationIdentifierSource(Environment dbEnvironment, String databaseName, FudgeContext fudgeContext) {
    Validate.notNull(dbEnvironment, "Database Environment must be specified");
    Validate.notNull(databaseName, "Database name must be provided");
    Validate.notNull(fudgeContext, "Fudge context must be provided");
    _dbEnvironment = dbEnvironment;
    _databaseName = databaseName;
    _fudgeContext = fudgeContext;
  }

  /**
   * Gets the dbEnvironment field.
   * @return the dbEnvironment
   */
  public Environment getDbEnvironment() {
    return _dbEnvironment;
  }

  /**
   * Gets the databaseName field.
   * @return the databaseName
   */
  public String getDatabaseName() {
    return _databaseName;
  }

  /**
   * Gets the fudgeContext field.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the database field.
   * @return the database
   */
  protected Database getDatabase() {
    return _database;
  }

  /**
   * Sets the database field.
   * @param database  the database
   */
  private void setDatabase(Database database) {
    _database = database;
  }

  @Override
  public long getIdentifier(ValueSpecification spec) {
    Validate.notNull(spec, "Specification must not be null");
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
          long freshIdentifier = _nextIdentifier.getAndIncrement();
          LongBinding.longToEntry(freshIdentifier, valueEntry);
          OperationStatus putStatus = getDatabase().put(txn, specEntry, valueEntry);
          if (putStatus != OperationStatus.SUCCESS) {
            s_logger.error("Unable to write new value {} for spec {} - {}", new Object[]{freshIdentifier, spec, putStatus});
            throw new OpenGammaRuntimeException("Unable to write new value");
          }
          result = freshIdentifier;
          break;
        case SUCCESS:
          result = LongBinding.entryToLong(valueEntry);
          break;
        default:
          s_logger.warn("Unexpected operation status on load {}, assuming we have to insert a new record", status);
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
  
  protected byte[] convertSpecificationToByteArray(ValueSpecification valueSpec) {
    // REVIEW kirk 2010-07-31 -- Cache the serialization context?
    FudgeSerializationContext serContext = new FudgeSerializationContext(getFudgeContext());
    FudgeFieldContainer msg = valueSpec.toFudgeMsg(serContext);
    byte[] specAsBytes = getFudgeContext().toByteArray(msg);
    return specAsBytes;
  }

  @Override
  public boolean isRunning() {
    return _started.get();
  }

  @Override
  public void start() {
    s_logger.info("Starting, and opening Database.");
    // TODO kirk 2010-07-31 -- Get next identifier from storage on restart.
    DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setAllowCreate(true);
    dbConfig.setTransactional(true);
    Database database = getDbEnvironment().openDatabase(null, getDatabaseName(), dbConfig);
    setDatabase(database);
    _started.set(true);
  }

  @Override
  public void stop() {
    s_logger.info("Shutting down Database.");
    if (getDatabase() != null) {
      getDatabase().close();
    }
    _started.set(false);
  }

}
