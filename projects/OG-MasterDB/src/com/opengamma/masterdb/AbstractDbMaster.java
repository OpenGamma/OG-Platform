/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import javax.time.TimeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbSource;

/**
 * An abstract master for rapid implementation of a database backed master.
 * <p>
 * This class is immutable. Implementations must be able to be shared between threads.
 */
public abstract class AbstractDbMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbMaster.class);

  /**
   * The database source.
   */
  private final DbSource _dbSource;
  /**
   * The time-source to use.
   */
  private TimeSource _timeSource;
  /**
   * The scheme in use for the unique identifier.
   */
  private String _identifierScheme;

  /**
   * Creates an instance.
   * @param dbSource  the database source combining all configuration, not null
   * @param defaultScheme  the default scheme for unique identifier, not null
   */
  public AbstractDbMaster(final DbSource dbSource, final String defaultScheme) {
    ArgumentChecker.notNull(dbSource, "dbSource");
    s_logger.debug("installed DbSource: {}", dbSource);
    _dbSource = dbSource;
    _timeSource = TimeSource.system();
    _identifierScheme = defaultScheme;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database source.
   * @return the database source, not null
   */
  public DbSource getDbSource() {
    return _dbSource;
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
   * Sets the time-source that determines the current time.
   * @param timeSource  the time-source, not null
   */
  public void setTimeSource(final TimeSource timeSource) {
    ArgumentChecker.notNull(timeSource, "timeSource");
    s_logger.debug("installed TimeSource: {}", timeSource);
    _timeSource = timeSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme in use for unique identifier.
   * @return the scheme, not null
   */
  public String getIdentifierScheme() {
    return _identifierScheme;
  }

  /**
   * Sets the scheme in use for unique identifier.
   * @param scheme  the scheme for unique identifier, not null
   */
  public void setIdentifierScheme(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    s_logger.debug("installed IdentifierScheme: {}", scheme);
    _identifierScheme = scheme;
  }

  /**
   * Checks the unique identifier scheme is valid.
   * @param uid  the unique identifier, not null
   */
  public void checkScheme(final UniqueIdentifier uid) {
    if (getIdentifierScheme().equals(uid.getScheme()) == false) {
      throw new IllegalArgumentException("UniqueIdentifier is not from this master (" + getIdentifierScheme() + "): " + uid);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this master.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getIdentifierScheme() + "]";
  }

}
