/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import javax.time.TimeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbSource;

/**
 * A portfolio master implementation using a database for persistence.
 * <p>
 * Full details of the API are in {@link PortfolioMaster}.
 * This class uses JDBC to store the data via a set of workers.
 * The workers may be replaced by configuration to allow different SQL on different databases.
 */
public class DbPortfolioMaster implements PortfolioMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbPortfolioMaster.class);

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbPrt";

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
  private DbPortfolioMasterWorkers _workers;

  /**
   * Creates an instance.
   * @param dbSource  the database source combining all configuration, not null
   */
  public DbPortfolioMaster(final DbSource dbSource) {
    ArgumentChecker.notNull(dbSource, "dbSource");
    s_logger.debug("installed DbSource: {}", dbSource);
    _dbSource = dbSource;
    setWorkers(new DbPortfolioMasterWorkers());
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
   * Gets the configured set of workers.
   * @return the configured workers, not null
   */
  public DbPortfolioMasterWorkers getWorkers() {
    return _workers;
  }

  /**
   * Sets the configured workers to use.
   * The workers will be {@link DbPortfolioMasterWorkers#init initialized} as part of this method call.
   * @param workers  the configured workers, not null
   */
  public void setWorkers(final DbPortfolioMasterWorkers workers) {
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
      throw new IllegalArgumentException("UniqueIdentifier is not from this portfolio master: " + uid);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioSearchResult search(final PortfolioSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    return getWorkers().getSearchWorker().search(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument get(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    return getWorkers().getGetWorker().get(uid);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument add(final PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolio().getRootNode(), "document.portfolio.rootNode");
    
    return getWorkers().getAddWorker().add(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument update(final PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    checkScheme(document.getUniqueId());
    
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
  public PortfolioHistoryResult history(final PortfolioHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "document.objectId");
    checkScheme(request.getObjectId());
    
    return getWorkers().getHistorysWorker().history(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument correct(final PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    checkScheme(document.getUniqueId());
    
    return getWorkers().getCorrectWorker().correct(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageablePortfolioNode getNode(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    return getWorkers().getGetNodeWorker().getNode(uid);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this portfolio master.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getIdentifierScheme() + "]";
  }

}
