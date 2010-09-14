/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db;

import javax.time.TimeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.opengamma.financial.security.master.SecurityDocument;
import com.opengamma.financial.security.master.SecurityMaster;
import com.opengamma.financial.security.master.SecuritySearchHistoricRequest;
import com.opengamma.financial.security.master.SecuritySearchHistoricResult;
import com.opengamma.financial.security.master.SecuritySearchRequest;
import com.opengamma.financial.security.master.SecuritySearchResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbHelper;
import com.opengamma.util.db.DbSource;

/**
 * Low level SQL focused part of the database backed security master.
 */
public class DbSecurityMaster implements SecurityMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbSecurityMaster.class);

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbSec";

  /**
   * The template for database operations.
   */
  private final SimpleJdbcTemplate _jdbcTemplate;
  /**
   * The template for transactions.
   */
  private final TransactionTemplate _transactionTemplate;
  /**
   * The database specific helper.
   */
  private final DbHelper _dbHelper;
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
  private DbSecurityMasterWorkers _workers;
  /**
   * The detail provider, null if detail loaded/stored directly.
   */
  private SecurityMasterDetailProvider _detailProvider;

  /**
   * Creates an instance.
   * @param transTemplate  the transaction template, not null
   * @param jdbcTemplate  the JDBC template, not null
   * @param dbHelper  the database specific helper, not null
   * @param detailProvider  the detail provider for loading/storing the security itself, may be null
   */
  public DbSecurityMaster(
      final TransactionTemplate transTemplate, final SimpleJdbcTemplate jdbcTemplate,
      final DbHelper dbHelper, final SecurityMasterDetailProvider detailProvider) {
    ArgumentChecker.notNull(transTemplate, "transTemplate");
    ArgumentChecker.notNull(dbHelper, "dbHelper");
    s_logger.debug("installed TransactionTemplate: {}", transTemplate);
    s_logger.debug("installed SimpleJdbcTemplate: {}", jdbcTemplate);
    s_logger.debug("installed DbHelper: {}", dbHelper);
    _jdbcTemplate = jdbcTemplate;
    _transactionTemplate = transTemplate;
    _dbHelper = dbHelper;
    setWorkers(new DbSecurityMasterWorkers());
    _detailProvider = detailProvider;
  }

  /**
   * Creates an instance.
   * @param dbSource  the database source combining all configuration, not null
   * @param detailProvider  the detail provider for loading/storing the security itself, may be null
   */
  public DbSecurityMaster(DbSource dbSource, final SecurityMasterDetailProvider detailProvider) {
    ArgumentChecker.notNull(dbSource, "dbSource");
    s_logger.debug("installed DbSource: {}", dbSource);
    _jdbcTemplate = dbSource.getJdbcTemplate();
    _transactionTemplate = dbSource.getTransactionTemplate();
    _dbHelper = dbSource.getDialect();
    setWorkers(new DbSecurityMasterWorkers());
    _detailProvider = detailProvider;
    if (detailProvider != null) {
      detailProvider.init(dbSource);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database template.
   * @return the database template, non-null
   */
  public SimpleJdbcTemplate getJdbcTemplate() {
    return _jdbcTemplate;
  }

  /**
   * Gets the transaction template.
   * @return the transaction template, non-null
   */
  public TransactionTemplate getTransactionTemplate() {
    return _transactionTemplate;
  }

  /**
   * Gets the database helper.
   * @return the database helper, non-null
   */
  public DbHelper getDbHelper() {
    return _dbHelper;
  }

  /**
   * Gets the detail provider.
   * @return the detail provider, non-null
   */
  public SecurityMasterDetailProvider getDetailProvider() {
    return _detailProvider;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the configured set of workers.
   * @return the configured workers, not null
   */
  public DbSecurityMasterWorkers getWorkers() {
    return _workers;
  }

  /**
   * Sets the configured workers to use.
   * The workers will be {@link DbSecurityMasterWorkers#init initialized} as part of this method call.
   * @param workers  the configured workers, not null
   */
  public void setWorkers(final DbSecurityMasterWorkers workers) {
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
      throw new IllegalArgumentException("UniqueIdentifier is not from this security master: " + uid);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    return getWorkers().getSearchWorker().search(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument get(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    return getWorkers().getGetWorker().get(uid);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument add(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    
    return getWorkers().getAddWorker().add(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument update(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    ArgumentChecker.notNull(document.getSecurityId(), "document.securityId");
    checkScheme(document.getSecurityId());
    
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
  public SecuritySearchHistoricResult searchHistoric(final SecuritySearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getSecurityId(), "request.securityId");
    checkScheme(request.getSecurityId());
    
    return getWorkers().getSearchHistoricWorker().searchHistoric(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityDocument correct(final SecurityDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSecurity(), "document.security");
    ArgumentChecker.notNull(document.getSecurityId(), "document.securityId");
    checkScheme(document.getSecurityId());
    
    return getWorkers().getCorrectWorker().correct(document);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this security master.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getIdentifierScheme() + "]";
  }

}
