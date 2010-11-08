/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday.master.db;

import javax.time.TimeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.world.holiday.master.HolidayDocument;
import com.opengamma.financial.world.holiday.master.HolidayMaster;
import com.opengamma.financial.world.holiday.master.HolidayHistoryRequest;
import com.opengamma.financial.world.holiday.master.HolidayHistoryResult;
import com.opengamma.financial.world.holiday.master.HolidaySearchRequest;
import com.opengamma.financial.world.holiday.master.HolidaySearchResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbSource;

/**
 * A holiday master implementation using a database for persistence.
 * <p>
 * Full details of the API are in {@link HolidayMaster}.
 * This class uses JDBC to store the data via a set of workers.
 * The workers may be replaced by configuration to allow different SQL on different databases.
 */
public class DbHolidayMaster implements HolidayMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbHolidayMaster.class);

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbHol";

  /**
   * The database source.
   */
  private final DbSource _dbSource;
  /**
   * The time-source to use.
   */
  private TimeSource _timeSource = TimeSource.system();
  /**
   * The scheme in use for UniqueIdentifier.
   */
  private String _identifierScheme = IDENTIFIER_SCHEME_DEFAULT;
  /**
   * The workers.
   */
  private DbHolidayMasterWorkers _workers;

  /**
   * Creates an instance.
   * @param dbSource  the database source combining all configuration, not null
   */
  public DbHolidayMaster(DbSource dbSource) {
    ArgumentChecker.notNull(dbSource, "dbSource");
    s_logger.debug("installed DbSource: {}", dbSource);
    _dbSource = dbSource;
    setWorkers(new DbHolidayMasterWorkers());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database source.
   * @return the database source, non-null
   */
  public DbSource getDbSource() {
    return _dbSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the configured set of workers.
   * @return the configured workers, not null
   */
  public DbHolidayMasterWorkers getWorkers() {
    return _workers;
  }

  /**
   * Sets the configured workers to use.
   * The workers will be {@link DbHolidayMasterWorkers#init initialized} as part of this method call.
   * @param workers  the configured workers, not null
   */
  public void setWorkers(final DbHolidayMasterWorkers workers) {
    ArgumentChecker.notNull(workers, "workers");
    workers.init(this);
    s_logger.debug("installed DbPositionMasterWorkers: {}", workers);
    _workers = workers;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-source that determines the current time.
   * @return the time-source, not null
   */
  public TimeSource getTimeSource() {
    return _timeSource;
  }

  /**
   * Sets the time-source.
   * @param timeSource  the time-source, not null
   */
  public void setTimeSource(final TimeSource timeSource) {
    ArgumentChecker.notNull(timeSource, "timeSource");
    s_logger.debug("installed TimeSource: {}", timeSource);
    _timeSource = timeSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme in use for UniqueIdentifier.
   * @return the scheme, not null
   */
  public String getIdentifierScheme() {
    return _identifierScheme;
  }

  /**
   * Sets the scheme in use for UniqueIdentifier.
   * @param scheme  the scheme, not null
   */
  public void setIdentifierScheme(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    s_logger.debug("installed IdentifierScheme: {}", scheme);
    _identifierScheme = scheme;
  }

  /**
   * Checks the scheme is valid.
   * @param uid  the unique identifier
   */
  protected void checkScheme(final UniqueIdentifier uid) {
    if (getIdentifierScheme().equals(uid.getScheme()) == false) {
      throw new IllegalArgumentException("UniqueIdentifier is not from this holiday master: " + uid);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidaySearchResult search(final HolidaySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    return getWorkers().getSearchWorker().search(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument get(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    return getWorkers().getGetWorker().get(uid);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument add(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    
    return getWorkers().getAddWorker().add(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument update(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    ArgumentChecker.notNull(document.getHolidayId(), "document.holidayId");
    checkScheme(document.getHolidayId());
    
    return getWorkers().getUpdateWorker().update(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    getWorkers().getRemoveWorker().remove(uid);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayHistoryResult history(final HolidayHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getHolidayId(), "request.holidayId");
    checkScheme(request.getHolidayId());
    
    return getWorkers().getHistoryWorker().history(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument correct(final HolidayDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    ArgumentChecker.notNull(document.getHolidayId(), "document.holidayId");
    checkScheme(document.getHolidayId());
    
    return getWorkers().getCorrectWorker().correct(document);
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
