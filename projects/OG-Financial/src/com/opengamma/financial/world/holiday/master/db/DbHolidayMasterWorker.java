/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday.master.db;

import javax.time.TimeSource;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.opengamma.financial.world.holiday.master.HolidayDocument;
import com.opengamma.financial.world.holiday.master.HolidayMaster;
import com.opengamma.financial.world.holiday.master.HolidayHistoryRequest;
import com.opengamma.financial.world.holiday.master.HolidayHistoryResult;
import com.opengamma.financial.world.holiday.master.HolidaySearchRequest;
import com.opengamma.financial.world.holiday.master.HolidaySearchResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.DbHelper;

/**
 * Base worker class for the holiday master.
 * <p>
 * This is designed to allow each holiday master method to be implemented by a
 * different class and easily replaced by an alternative.
 * Implementations are registered using {@link DbHolidayMasterWorkers}.
 * <p>
 * The API of this class follows {@link HolidayMaster}.
 * Each of the methods should be implemented as per the documentation on the master.
 * The parameters to the methods will be pre-checked for nulls before the worker is called,
 * including any internal required values in request or document objects.
 * <p>
 * This base implementation throws {@code UnsupportedOperationException} from each method.
 * As a result, subclasses only need to implement those methods they want to.
 */
public class DbHolidayMasterWorker {

  /**
   * The main master.
   */
  private DbHolidayMaster _master;

  /**
   * Creates an instance.
   */
  protected DbHolidayMasterWorker() {
  }

  /**
   * Initializes the instance.
   * @param master  the holiday master, not null
   */
  protected void init(final DbHolidayMaster master) {
    _master = master;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent master.
   * @return the parent, not null
   */
  protected DbHolidayMaster getMaster() {
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
      throw new IllegalArgumentException("UniqueIdentifier is not from this holiday master: " + id, ex);
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
      throw new IllegalArgumentException("UniqueIdentifier is not from this holiday master: " + id, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an object identifier.
   * @param oid  the holiday object identifier
   * @return the unique identifier, not null
   */
  protected UniqueIdentifier createObjectIdentifier(final long oid) {
    return UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid));
  }

  /**
   * Creates a unique identifier.
   * @param oid  the holiday object identifier
   * @param rowId  the node unique row identifier, null if object identifier
   * @return the unique identifier, not null
   */
  protected UniqueIdentifier createUniqueIdentifier(final long oid, final long rowId) {
    return UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid), Long.toString(rowId - oid));
  }

  //-------------------------------------------------------------------------
  protected HolidaySearchResult search(HolidaySearchRequest request) {
    throw new UnsupportedOperationException();
  }

  protected HolidayDocument get(final UniqueIdentifier uid) {
    throw new UnsupportedOperationException();
  }

  protected HolidayDocument add(HolidayDocument document) {
    throw new UnsupportedOperationException();
  }

  protected HolidayDocument update(HolidayDocument document) {
    throw new UnsupportedOperationException();
  }

  protected void remove(UniqueIdentifier uid) {
    throw new UnsupportedOperationException();
  }

  protected HolidayHistoryResult history(HolidayHistoryRequest request) {
    throw new UnsupportedOperationException();
  }

  protected HolidayDocument correct(HolidayDocument document) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this holiday master.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getIdentifierScheme() + "]";
  }

}
