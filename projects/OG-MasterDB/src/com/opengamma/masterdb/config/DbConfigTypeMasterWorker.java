/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import javax.time.TimeSource;

import org.fudgemsg.FudgeContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.util.db.DbHelper;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * Base worker class for the config master.
 * <p>
 * This is designed to allow each config master method to be implemented by a
 * different class and easily replaced by an alternative.
 * Implementations are registered using {@link DbConfigTypeMasterWorkers}.
 * <p>
 * The API of this class follows {@link ConfigTypeMaster}.
 * Each of the methods should be implemented as per the documentation on the master.
 * The parameters to the methods will be pre-checked for nulls before the worker is called,
 * including any internal required values in request or document objects.
 * <p>
 * This base implementation throws {@code UnsupportedOperationException} from each method.
 * As a result, subclasses only need to implement those methods they want to.
 * 
 * @param <T>  the configuration element type
 */
public class DbConfigTypeMasterWorker<T> {

  /**
   * The Fudge context.
   */
  protected static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  /**
   * The main master.
   */
  private DbConfigTypeMaster<T> _master;

  /**
   * Creates an instance.
   */
  protected DbConfigTypeMasterWorker() {
  }

  /**
   * Initializes the instance.
   * @param master  the security master, not null
   */
  protected void init(final DbConfigTypeMaster<T> master) {
    _master = master;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent master.
   * @return the parent, not null
   */
  protected DbConfigTypeMaster<T> getMaster() {
    return _master;
  }

  /**
   * Gets the database template.
   * @return the database template, non-null if correctly initialized
   */
  protected SimpleJdbcTemplate getJdbcTemplate() {
    return _master.getDbSource().getJdbcTemplate();
  }

  /**
   * Gets the transaction template.
   * @return the transaction template, non-null if correctly initialized
   */
  protected TransactionTemplate getTransactionTemplate() {
    return _master.getDbSource().getTransactionTemplate();
  }

  /**
   * Gets the database helper.
   * @return the helper, non-null if correctly initialized
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
      throw new IllegalArgumentException("UniqueIdentifier is not from this config master: " + id, ex);
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
      throw new IllegalArgumentException("UniqueIdentifier is not from this config master: " + id, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an object identifier.
   * @param oid  the config object identifier
   * @return the unique identifier, not null
   */
  protected UniqueIdentifier createObjectIdentifier(final long oid) {
    return UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid));
  }

  /**
   * Creates a unique identifier.
   * @param oid  the config object identifier
   * @param rowId  the node unique row identifier, null if object identifier
   * @return the unique identifier, not null
   */
  protected UniqueIdentifier createUniqueIdentifier(final long oid, final long rowId) {
    return UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid), Long.toString(rowId - oid));
  }

  //-------------------------------------------------------------------------
  protected ConfigSearchResult<T> search(ConfigSearchRequest request) {
    throw new UnsupportedOperationException();
  }

  protected ConfigDocument<T> get(final UniqueIdentifier uid) {
    throw new UnsupportedOperationException();
  }

  protected ConfigDocument<T> add(ConfigDocument<T> document) {
    throw new UnsupportedOperationException();
  }

  protected ConfigDocument<T> update(ConfigDocument<T> document) {
    throw new UnsupportedOperationException();
  }

  protected void remove(UniqueIdentifier uid) {
    throw new UnsupportedOperationException();
  }

  protected ConfigHistoryResult<T> history(ConfigHistoryRequest request) {
    throw new UnsupportedOperationException();
  }

  protected ConfigDocument<T> correct(ConfigDocument<T> document) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this config master.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getIdentifierScheme() + "]";
  }

}
