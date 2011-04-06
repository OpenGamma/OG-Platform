/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.time.Instant;
import javax.time.TimeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbHelper;
import com.opengamma.util.db.DbSource;

/**
 * An abstract master for rapid implementation of a database backed master.
 * <p>
 * This combines the various configuration elements and convenience methods
 * needed for most database masters.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
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
   * 
   * @param dbSource  the database source combining all configuration, not null
   * @param defaultScheme  the default scheme for unique identifier, not null
   */
  public AbstractDbMaster(final DbSource dbSource, final String defaultScheme) {
    ArgumentChecker.notNull(dbSource, "dbSource");
    s_logger.debug("installed DbSource: {}", dbSource);
    _dbSource = dbSource;
    _timeSource = dbSource.timeSource();
    _identifierScheme = defaultScheme;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database source.
   * 
   * @return the database source, not null
   */
  public DbSource getDbSource() {
    return _dbSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-source that determines the current time.
   * 
   * @return the time-source, not null
   */
  public TimeSource getTimeSource() {
    return _timeSource;
  }

  /**
   * Sets the time-source that determines the current time.
   * 
   * @param timeSource  the time-source, not null
   */
  public void setTimeSource(final TimeSource timeSource) {
    ArgumentChecker.notNull(timeSource, "timeSource");
    s_logger.debug("installed TimeSource: {}", timeSource);
    _timeSource = timeSource;
  }

  /**
   * Gets the current instant using the time-source.
   * 
   * @return the current instant, not null
   */
  protected Instant now() {
    return Instant.now(getTimeSource());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database template.
   * 
   * @return the database template, not null if correctly initialized
   */
  protected SimpleJdbcTemplate getJdbcTemplate() {
    return getDbSource().getJdbcTemplate();
  }

  /**
   * Gets the transaction template.
   * 
   * @return the transaction template, not null if correctly initialized
   */
  protected TransactionTemplate getTransactionTemplate() {
    return getDbSource().getTransactionTemplate();
  }

  /**
   * Gets the database helper.
   * 
   * @return the helper, not null if correctly initialized
   */
  protected DbHelper getDbHelper() {
    return getDbSource().getDialect();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme in use for unique identifier.
   * 
   * @return the scheme, not null
   */
  public String getIdentifierScheme() {
    return _identifierScheme;
  }

  /**
   * Sets the scheme in use for unique identifier.
   * 
   * @param scheme  the scheme for unique identifier, not null
   */
  public void setIdentifierScheme(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    s_logger.debug("installed IdentifierScheme: {}", scheme);
    _identifierScheme = scheme;
  }

  /**
   * Checks the unique identifier scheme is valid.
   * 
   * @param objectId  the object identifier, not null
   */
  protected void checkScheme(final ObjectIdentifiable objectId) {
    if (getIdentifierScheme().equals(objectId.getObjectId().getScheme()) == false) {
      throw new IllegalArgumentException("UniqueIdentifier is not from this master (" + getIdentifierScheme() + "): " + objectId);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts the row identifier.
   * 
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
   * Extracts the object identifier.
   * 
   * @param objectId  the identifier to extract from, not null
   * @return the extracted oid
   */
  protected long extractOid(final ObjectIdentifiable objectId) {
    try {
      return Long.parseLong(objectId.getObjectId().getValue());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("UniqueIdentifier is not from this master (non-numeric object id): " + objectId, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an object identifier.
   * 
   * @param oid  the object identifier
   * @return the unique identifier, not null
   */
  public UniqueIdentifier createObjectIdentifier(final long oid) {
    return UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid));
  }

  /**
   * Creates a unique identifier.
   * 
   * @param oid  the object identifier
   * @param rowId  the node unique row identifier, null if object identifier
   * @return the unique identifier, not null
   */
  public UniqueIdentifier createUniqueIdentifier(final long oid, final long rowId) {
    return UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid), Long.toString(rowId - oid));
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts a BigDecimal handling DB annoyances.
   * 
   * @param rs  the result set, not null
   * @param columnName  the column name, not null
   * @return the extracted value, may be null
   * @throws SQLException 
   */
  protected BigDecimal extractBigDecimal(final ResultSet rs, final String columnName) throws SQLException {
    BigDecimal value = rs.getBigDecimal(columnName);
    if (value == null) {
      return null;
    }
    BigDecimal stripped = value.stripTrailingZeros();  // Derby, and maybe others, add trailing zeroes
    if (stripped.scale() < 0) {
      return stripped.setScale(0);
    }
    return stripped;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this master.
   * 
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getIdentifierScheme() + "]";
  }

}
