/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.JdkUtils;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDialect;
import com.opengamma.util.db.DbMapSqlParameterSource;

/**
 * An abstract master for rapid implementation of a database backed master.
 * <p>
 * This combines the various configuration elements and convenience methods
 * needed for most database masters.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public abstract class AbstractDbMaster implements ConfigurableDbMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbMaster.class);

  /**
   * The database connector.
   */
  private final DbConnector _dbConnector;
  /**
   * The clock to use.
   */
  private Clock _clock;
  /**
   * The scheme in use for the unique identifier.
   */
  private String _uniqueIdScheme;

  /**
   * The maximum number of retries.
   */
  private int _maxRetries = 10;

  /**
   * External SQL bundle.
   */
  private ElSqlBundle _externalSqlBundle;
  
  /**
   * The Hibernate template.
   */
  private HibernateTemplate _hibernateTemplate;

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   * @param defaultScheme  the default scheme for unique identifier, not null
   */
  public AbstractDbMaster(final DbConnector dbConnector, final String defaultScheme) {
    ArgumentChecker.notNull(dbConnector, "dbConnector");
    s_logger.debug("installed DbConnector: {}", dbConnector);
    _dbConnector = dbConnector;
    _clock = dbConnector.timeSource();
    _uniqueIdScheme = defaultScheme;
    _hibernateTemplate = dbConnector.getHibernateTemplate();
  }

  /**
   * Gets the maximum number of retries.
   * The default is ten.
   *
   * @return the maximum number of retries, not null
   */
  @Override
  public int getMaxRetries() {
    return _maxRetries;
  }

  /**
   * Sets the maximum number of retries.
   * The default is ten.
   *
   * @param maxRetries  the maximum number of retries, not negative
   */
  @Override
  public void setMaxRetries(final int maxRetries) {
    ArgumentChecker.notNegative(maxRetries, "maxRetries");
    _maxRetries = maxRetries;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database connector.
   * 
   * @return the database connector, not null
   */
  @Override
  public DbConnector getDbConnector() {
    return _dbConnector;
  }
  
  /**
   * Gets the Hibernate Session factory.
   *
   * @return the session factory, not null
   */
  public SessionFactory getSessionFactory() {
    return getDbConnector().getHibernateSessionFactory();
  }
  
  /**
   * Gets the local Hibernate template.
   *
   * @return the template, not null
   */
  public HibernateTemplate getHibernateTemplate() {
    return _hibernateTemplate;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the external SQL bundle.
   * 
   * @return the external SQL bundle, not null
   */
  @Override
  public ElSqlBundle getElSqlBundle() {
    return _externalSqlBundle;
  }

  /**
   * Sets the external SQL bundle.
   * 
   * @param bundle  the external SQL bundle, not null
   */
  @Override
  public void setElSqlBundle(ElSqlBundle bundle) {
    _externalSqlBundle = bundle;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the clock that determines the current time.
   * 
   * @return the clock, not null
   */
  @Override
  public Clock getClock() {
    return _clock;
  }

  /**
   * Sets the clock that determines the current time.
   * 
   * @param clock  the clock, not null
   */
  @Override
  public void setClock(final Clock clock) {
    ArgumentChecker.notNull(clock, "clock");
    s_logger.debug("installed Clock: {}", clock);
    _clock = clock;
  }
  
  /**
   * Resets the clock that determines the current time to the default.
   */
  public void resetClock() {
    setClock(getDbConnector().timeSource());
  }

  /**
   * Gets the current instant using the clock.
   * 
   * @return the current instant, not null
   */
  protected Instant now() {
    return Instant.now(getClock());
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the parameter source.
   * @return the source, not null
   */
  protected DbMapSqlParameterSource createParameterSource() {
    return new DbMapSqlParameterSource();
  }

  /**
   * Gets the next database id.
   * 
   * @param sequenceName  the name of the sequence to query, not null
   * @return the next database id
   */
  protected long nextId(String sequenceName) {
    return getJdbcTemplate().getJdbcOperations().queryForObject(getDialect().sqlNextSequenceValueSelect(sequenceName), Long.class);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database template.
   * 
   * @return the database template, not null if correctly initialized
   */
  protected NamedParameterJdbcTemplate getJdbcTemplate() {
    return getDbConnector().getJdbcTemplate();
  }

  /**
   * Gets the transaction template.
   * 
   * @return the transaction template, not null if correctly initialized
   */
  protected TransactionTemplate getTransactionTemplate() {
    return getDbConnector().getTransactionTemplate();
  }

  /**
   * Gets the retrying transaction template.
   *
   * @param retries number of retries of execution before considering the execution failed
   * @return the transaction template, not null if correctly initialized
   */
  protected DbConnector.TransactionTemplateRetrying getTransactionTemplateRetrying(int retries) {
    return getDbConnector().getTransactionTemplateRetrying(retries);
  }
  
  /**
   * Gets the hibernate template wrapped in new transaction.
   *
   * @return the hibernate template wrapped in new transaction, not null if correctly initialized
   */
  protected DbConnector.HibernateTransactionTemplate getHibernateTransactionTemplate() {
    return getDbConnector().getHibernateTransactionTemplate();
  }

  /**
   * Gets the retrying hibernate transaction template.
   *
   * @param retries number of retries of execution before considering the execution failed
   * @return the hibernate transaction template, not null if correctly initialized
   */
  protected DbConnector.HibernateTransactionTemplateRetrying getHibernateTransactionTemplateRetrying(int retries) {
    return getDbConnector().getHibernateTransactionTemplateRetrying(retries);
  }
  
  /**
   * Gets the database dialect.
   * 
   * @return the dialect, not null if correctly initialized
   */
  protected DbDialect getDialect() {
    return getDbConnector().getDialect();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme in use for unique identifier.
   * 
   * @return the scheme, not null
   */
  @Override
  public String getUniqueIdScheme() {
    return _uniqueIdScheme;
  }

  /**
   * Sets the scheme in use for unique identifier.
   * 
   * @param scheme  the scheme for unique identifier, not null
   */
  @Override
  public void setUniqueIdScheme(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    s_logger.debug("installed scheme: {}", scheme);
    _uniqueIdScheme = scheme;
  }

  /**
   * Checks the unique identifier scheme is valid.
   * 
   * @param objectId  the object identifier, not null
   */
  protected void checkScheme(final ObjectIdentifiable objectId) {
    if (getUniqueIdScheme().equals(objectId.getObjectId().getScheme()) == false) {
      throw new IllegalArgumentException("UniqueId is not from this master (" + toString() + "): " + objectId);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts the row identifier.
   * 
   * @param id  the identifier to extract from, not null
   * @return the extracted row id
   */
  protected long extractRowId(final UniqueId id) {
    try {
      return Long.parseLong(id.getValue()) + Long.parseLong(id.getVersion());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("UniqueId is not from this master (non-numeric row id): " + id, ex);
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
      throw new IllegalArgumentException("UniqueId is not from this master (non-numeric object id): " + objectId, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an object identifier.
   * 
   * @param oid  the object identifier
   * @return the unique identifier, not null
   */
  public UniqueId createObjectId(final long oid) {
    return UniqueId.of(getUniqueIdScheme(), Long.toString(oid));
  }

  /**
   * Creates a unique identifier.
   * 
   * @param oid  the object identifier
   * @param rowId  the node unique row identifier, null if object identifier
   * @return the unique identifier, not null
   */
  public UniqueId createUniqueId(final long oid, final long rowId) {
    return UniqueId.of(getUniqueIdScheme(), Long.toString(oid), Long.toString(rowId - oid));
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
    BigDecimal stripped = JdkUtils.stripTrailingZeros(value);  // Derby, and maybe others, add trailing zeroes
    return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Retrieves the version of the master schema from the database.
   *  
   * @return the schema version, or null if not found
   */
  @Override
  public Integer getSchemaVersion() {
    try {
      final DbMapSqlParameterSource args = createParameterSource().addValue("version_key", "schema_patch");
      final String sql = getElSqlBundle().getSql("GetSchemaVersion", args);
      String version = getJdbcTemplate().queryForObject(sql, args, String.class);
      return Integer.parseInt(version);
    } catch (Exception e) {
      s_logger.debug("Error reading schema version from database", e);
      return null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this master.
   * 
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUniqueIdScheme() + "]";
  }

}
