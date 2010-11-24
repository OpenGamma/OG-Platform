/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import javax.time.TimeSource;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.DbHelper;

/**
 * Base worker class for rapid implementation of a master.
 * <p>
 * This is designed to provide a convenient base foe the SQL implementation.
 * <p>
 * This class is immutable. Implementations must be able to be shared between threads.
 * 
 * @param <T>  the type of the master
 */
public class AbstractDbMasterWorker<T extends AbstractDbMaster> {

  /**
   * The main master.
   */
  private T _master;

  /**
   * Creates an instance.
   */
  protected AbstractDbMasterWorker() {
  }

  /**
   * Initializes the instance.
   * @param master  the exchange master, not null
   */
  protected void init(final T master) {
    _master = master;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent master.
   * @return the parent, not null
   */
  protected T getMaster() {
    return _master;
  }

  /**
   * Gets the database template.
   * @return the database template, not null if correctly initialized
   */
  protected SimpleJdbcTemplate getJdbcTemplate() {
    return _master.getDbSource().getJdbcTemplate();
  }

  /**
   * Gets the transaction template.
   * @return the transaction template, not null if correctly initialized
   */
  protected TransactionTemplate getTransactionTemplate() {
    return _master.getDbSource().getTransactionTemplate();
  }

  /**
   * Gets the database helper.
   * @return the helper, not null if correctly initialized
   */
  protected DbHelper getDbHelper() {
    return _master.getDbSource().getDialect();
  }

  /**
   * Gets the time-source that determines the current time.
   * @return the time-source, not null
   */
  protected TimeSource getTimeSource() {
    return _master.getTimeSource();
  }

  /**
   * Gets the scheme in use for UniqueIdentifier.
   * @return the scheme, not null
   */
  protected String getIdentifierScheme() {
    return _master.getIdentifierScheme();
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts the row id.
   * @param id  the identifier to extract from, not null
   * @return the extracted row id
   */
  protected long extractRowId(final UniqueIdentifier id) {
    try {
      return Long.parseLong(id.getValue()) + Long.parseLong(id.getVersion());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("UniqueIdentifier is not from this master (non-numeric row id): " + id, ex);
    }
  }

  /**
   * Extracts the oid.
   * @param id  the identifier to extract from, not null
   * @return the extracted oid
   */
  protected long extractOid(final UniqueIdentifier id) {
    try {
      return Long.parseLong(id.getValue());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("UniqueIdentifier is not from this master (non-numeric object id): " + id, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an object identifier.
   * @param oid  the exchange object identifier
   * @return the unique identifier, not null
   */
  protected UniqueIdentifier createObjectIdentifier(final long oid) {
    return UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid));
  }

  /**
   * Creates a unique identifier.
   * @param oid  the exchange object identifier
   * @param rowId  the node unique row identifier, null if object identifier
   * @return the unique identifier, not null
   */
  protected UniqueIdentifier createUniqueIdentifier(final long oid, final long rowId) {
    return UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid), Long.toString(rowId - oid));
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of the worker.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getIdentifierScheme() + "]";
  }

}
