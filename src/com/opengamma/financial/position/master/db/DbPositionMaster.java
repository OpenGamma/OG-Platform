/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import javax.sql.DataSource;
import javax.time.TimeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.master.FullPortfolioGetRequest;
import com.opengamma.financial.position.master.FullPortfolioNodeGetRequest;
import com.opengamma.financial.position.master.FullPositionGetRequest;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricResult;
import com.opengamma.financial.position.master.PortfolioTreeSearchRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchResult;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.position.master.PositionSearchHistoricRequest;
import com.opengamma.financial.position.master.PositionSearchHistoricResult;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbHelper;

/**
 * Low level SQL focused part of the database backed position master.
 */
public class DbPositionMaster implements PositionMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbPositionMaster.class);

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbPos";

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
  private DbPositionMasterWorkers _workers;

  /**
   * Creates an instance.
   * @param transManager  the transaction manager, not null
   * @param transDefinition  the transaction definition, not null
   * @param dbHelper  the database specific helper, not null
   */
  public DbPositionMaster(final DataSourceTransactionManager transManager, final TransactionDefinition transDefinition, final DbHelper dbHelper) {
    ArgumentChecker.notNull(transManager, "transManager");
    ArgumentChecker.notNull(dbHelper, "dbHelper");
    s_logger.debug("installed DataSourceTransactionManager: {}", transManager);
    s_logger.debug("installed DbHelper: {}", dbHelper);
    DataSource dataSource = transManager.getDataSource();
    _jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    _transactionTemplate = new TransactionTemplate(transManager, transDefinition);
    _dbHelper = dbHelper;
    setWorkers(new DbPositionMasterWorkers());
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

  //-------------------------------------------------------------------------
  /**
   * Gets the configured set of workers.
   * @return the configured workers, not null
   */
  public DbPositionMasterWorkers getWorkers() {
    return _workers;
  }

  /**
   * Sets the configured workers to use.
   * The workers will be {@link DbPositionMasterWorkers#init initialized} as part of this method call.
   * @param workers  the configured workers, not null
   */
  public void setWorkers(final DbPositionMasterWorkers workers) {
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
      throw new IllegalArgumentException("UniqueIdentifier is not from this position master: " + uid);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeSearchResult searchPortfolioTrees(final PortfolioTreeSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    return getWorkers().getSearchPortfolioTreesWorker().searchPortfolioTrees(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeDocument getPortfolioTree(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    return getWorkers().getGetPortfolioTreeWorker().getPortfolioTree(uid);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeDocument addPortfolioTree(final PortfolioTreeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolio().getRootNode(), "document.portfolio.rootNode");
    
    return getWorkers().getAddPortfolioTreeWorker().addPortfolioTree(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeDocument updatePortfolioTree(final PortfolioTreeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolioId(), "document.portfolioId");
    checkScheme(document.getPortfolioId());
    
    return getWorkers().getUpdatePortfolioTreeWorker().updatePortfolioTree(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void removePortfolioTree(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    getWorkers().getRemovePortfolioTreeWorker().removePortfolioTree(uid);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeSearchHistoricResult searchPortfolioTreeHistoric(final PortfolioTreeSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPortfolioId(), "document.portfolioId");
    checkScheme(request.getPortfolioId());
    
    return getWorkers().getSearchHistoricPortfolioTreesWorker().searchPortfolioTreeHistoric(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeDocument correctPortfolioTree(final PortfolioTreeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolioId(), "document.portfolioId");
    checkScheme(document.getPortfolioId());
    
    return getWorkers().getCorrectPortfolioTreeWorker().correctPortfolioTree(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchResult searchPositions(final PositionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    return getWorkers().getSearchPositionsWorker().searchPositions(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument getPosition(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    return getWorkers().getGetPositionWorker().getPosition(uid);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument addPosition(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getParentNodeId(), "document.parentNodeId");
    
    return getWorkers().getAddPositionWorker().addPosition(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument updatePosition(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getPositionId(), "document.positionId");
    checkScheme(document.getPositionId());
    
    return getWorkers().getUpdatePositionWorker().updatePosition(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void removePosition(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    getWorkers().getRemovePositionWorker().removePosition(uid);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchHistoricResult searchPositionHistoric(final PositionSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPositionId(), "request.positionId");
    checkScheme(request.getPositionId());
    
    return getWorkers().getSearchHistoricPositionsWorker().searchPositionHistoric(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument correctPosition(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getPositionId(), "document.positionId");
    checkScheme(document.getPositionId());
    
    return getWorkers().getCorrectPositionWorker().correctPosition(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getFullPortfolio(final FullPortfolioGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPortfolioId(), "document.portfolioId");
    checkScheme(request.getPortfolioId());
    
    return getWorkers().getGetFullPortfolioWorker().getFullPortfolio(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioNode getFullPortfolioNode(final FullPortfolioNodeGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPortfolioNodeId(), "document.portfolioNodeId");
    checkScheme(request.getPortfolioNodeId());
    
    return getWorkers().getGetFullPortfolioNodeWorker().getFullPortfolioNode(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public Position getFullPosition(final FullPositionGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPositionId(), "document.positionId");
    checkScheme(request.getPositionId());
    
    return getWorkers().getGetFullPositionWorker().getFullPosition(request);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this position master.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getIdentifierScheme() + "]";
  }

}
