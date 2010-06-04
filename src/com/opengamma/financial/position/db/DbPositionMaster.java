/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.db;

import java.util.Set;

import javax.sql.DataSource;
import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.TimeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.Assert;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.financial.position.SearchPortfoliosRequest;
import com.opengamma.financial.position.SearchPortfoliosResult;
import com.opengamma.financial.position.SearchPositionsRequest;
import com.opengamma.financial.position.SearchPositionsResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbHelper;

/**
 * A database backed position master.
 * <p>
 * The position master provides a uniform structural view over a set of positions
 * holding them in a tree structure portfolio.
 * This class provides database storage for the entire tree.
 */
public class DbPositionMaster implements ManagablePositionMaster {

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbPos";
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbPositionMaster.class);

  /**
   * The underlying worker.
   */
  private final DbPositionMasterWorker _worker;
  /**
   * The template for database operations.
   */
  private final SimpleJdbcTemplate _jdbcTemplate;
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
   * Creates an instance.
   * @param transactionManager  the transaction manager, not null
   * @param dbHelper  the database specific helper, not null
   */
  public DbPositionMaster(DataSourceTransactionManager transactionManager, DbHelper dbHelper) {
    ArgumentChecker.notNull(transactionManager, "transactionManager");
    ArgumentChecker.notNull(dbHelper, "dbHelper");
    DataSource dataSource = transactionManager.getDataSource();
    _jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    _worker = new DbPositionMasterWorker(this);
    _dbHelper = dbHelper;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database worker.
   * @return the worker, non-null
   */
  protected DbPositionMasterWorker getWorker() {
    return _worker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database template.
   * @return the template, non-null
   */
  protected SimpleJdbcTemplate getTemplate() {
    return _jdbcTemplate;
  }

  /**
   * Gets the database helper.
   * @return the database helper, non-null
   */
  protected DbHelper getDbHelper() {
    return _dbHelper;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-source that determines the current time.
   * @return the time-source, not null
   */
  protected TimeSource getTimeSource() {
    return _timeSource;
  }

  /**
   * Sets the time-source.
   * @param timeSource  the time-source, not null
   */
  public void setTimeSource(final TimeSource timeSource) {
    ArgumentChecker.notNull(timeSource, "timeSource");
    _timeSource = timeSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme in use for UniqueIdentifier.
   * @return the scheme, not null
   */
  protected String getIdentifierScheme() {
    return _identifierScheme;
  }

  /**
   * Sets the scheme in use for UniqueIdentifier.
   * @param scheme  the scheme, not null
   */
  public void setIdentifierScheme(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    _identifierScheme = scheme;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks whether the unique identifier has the right scheme.
   * @param uid  the unique identifier, not null
   */
  protected void checkIdentifierScheme(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "UniqueIdentifier");
    if (isManagerFor(uid) == false) {
      s_logger.debug("invalid UniqueIdentifier scheme: {}", uid.getScheme());
      throw new IllegalArgumentException("Invalid identifier for DbPositionMaster: " + uid);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts a portfolio object identifier from a unique identifier.
   * @param uid  the unique identifier, not null
   * @return the portfolio object identifier
   */
  protected long extractPortfolioOid(final UniqueIdentifier uid) {
    int pos = uid.getValue().indexOf('-');
    if (pos < 0) {
      return Long.parseLong(uid.getValue());
    }
    return Long.parseLong(uid.getValue().substring(0, pos));
  }

  /**
   * Extracts the non-portfolio object identifier from a unique identifier.
   * @param uid  the unique identifier, not null
   * @return the non-portfolio object identifier
   */
  protected long extractOtherOid(final UniqueIdentifier uid) {
    int pos = uid.getValue().indexOf('-');
    if (pos < 0) {
      throw new IllegalArgumentException("Unique identifier is invalid: " + uid);
    }
    return Long.parseLong(uid.getValue().substring(pos + 1));
  }

  /**
   * Extracts the version from a unique identifier.
   * @param uid  the unique identifier, not null
   * @return the version
   */
  protected long extractVersion(final UniqueIdentifier uid) {
    return Long.parseLong(uid.getVersion());
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isManagerFor(UniqueIdentifier uid) {
    return uid.getScheme().equals(getIdentifierScheme());
  }

  @Override
  public boolean isModificationSupported() {
    return true;
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getPortfolio(final UniqueIdentifier uid) {
    checkIdentifierScheme(uid);
    if (uid.getValue().contains("-")) {
      return null;  // TODO: better solution/exception
    }
    if (uid.isVersioned()) {
      return getWorker().selectPortfolioByOidVersion(extractPortfolioOid(uid), extractVersion(uid), true, true);
    }
    return getPortfolio(uid, Instant.now(getTimeSource()));
  }

  @Override
  public Portfolio getPortfolio(final UniqueIdentifier uid, final InstantProvider instantProvider) {
    checkIdentifierScheme(uid);
    if (uid.getValue().contains("-")) {
      return null;  // TODO: better solution/exception
    }
    Instant instant = Instant.of(instantProvider);
    long portfolioOid = extractPortfolioOid(uid);
    return getWorker().selectPortfolioByOidInstant(portfolioOid, instant);
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioNode getPortfolioNode(final UniqueIdentifier uid) {
    checkIdentifierScheme(uid);
    if (uid.isVersioned()) {
      return getWorker().selectPortfolioNodeTree(extractPortfolioOid(uid), extractOtherOid(uid), extractVersion(uid));
    }
    return getPortfolioNode(uid, Instant.now(getTimeSource()));
  }

  @Override
  public PortfolioNode getPortfolioNode(final UniqueIdentifier uid, final InstantProvider instantProvider) {
    checkIdentifierScheme(uid);
    Instant instant = Instant.of(instantProvider);
    long portfolioOid = extractPortfolioOid(uid);
    try {
      long version = getWorker().selectVersionByPortfolioOidInstant(portfolioOid, instant, true);
      return getWorker().selectPortfolioNodeTree(portfolioOid, extractOtherOid(uid), version);
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public Position getPosition(final UniqueIdentifier uid) {
    checkIdentifierScheme(uid);
    if (uid.isVersioned()) {
      return getWorker().selectPosition(extractPortfolioOid(uid), extractOtherOid(uid), extractVersion(uid));
    }
    return getPosition(uid, Instant.now(getTimeSource()));
  }

  @Override
  public Position getPosition(final UniqueIdentifier uid, final InstantProvider instantProvider) {
    checkIdentifierScheme(uid);
    Instant instant = Instant.of(instantProvider);
    long portfolioOid = extractPortfolioOid(uid);
    try {
      long version = getWorker().selectVersionByPortfolioOidInstant(portfolioOid, instant, true);
      return getWorker().selectPosition(portfolioOid, extractOtherOid(uid), version);
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the complete set of portfolio unique identifiers.
   * @return the set of unique identifiers, not null
   */
  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    return getWorker().selectPortfolioIds(Instant.now(getTimeSource()));
  }

  /**
   * Gets the complete set of portfolio unique identifiers.
   * @param instantProvider  the instant to query at, not null
   * @return the set of unique identifiers, not null
   */
  public Set<UniqueIdentifier> getPortfolioIds(final InstantProvider instantProvider) {
    Instant instant = Instant.of(instantProvider);
    return getWorker().selectPortfolioIds(instant);
  }

  //-------------------------------------------------------------------------
  @Override
  public SearchPortfoliosResult searchPortfolios(final SearchPortfoliosRequest request) {
    Instant now = Instant.now(getTimeSource());
    return getWorker().selectPortfolioSummaries(request, now);
//    ArrayList<PortfolioSummary> summaries = new ArrayList<PortfolioSummary>();
//    Set<UniqueIdentifier> uids = getPortfolioIds();
//    for (UniqueIdentifier uid : uids) {
//      PortfolioSummary summary = new PortfolioSummary(uid);
//      summary.setName(uid.toString());
//      summaries.add(summary);
//    }
//    return new SearchPortfoliosResult(new Paging(request.getPagingRequest(), summaries.size()), summaries);
  }

  @Override
  public UniqueIdentifier addPortfolio(final Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    Instant instant = Instant.now(getTimeSource());
    UniqueIdentifier inputUid = portfolio.getUniqueIdentifier();
    if (inputUid != null && inputUid.getScheme().equals(getIdentifierScheme())) {
      throw new IllegalArgumentException("Portfolio already exists in this position master");
    }
    long portfolioOid = getWorker().selectNextPortfolioOid();
    UniqueIdentifier uid = getWorker().insertPortfolio(portfolio, portfolioOid, 1, instant, true);
    getWorker().insertTreeNodes(portfolio.getRootNode(), portfolioOid, 1);
    getWorker().insertTreePositions(portfolio.getRootNode(), portfolioOid, 1);
    return uid;
  }

  @Override
  public UniqueIdentifier updatePortfolioOnly(final Portfolio portfolio) {
    Assert.notNull(portfolio, "portfolio");
    Instant instant = Instant.now(getTimeSource());
    UniqueIdentifier oldUid = portfolio.getUniqueIdentifier();
    checkIdentifierScheme(oldUid);
    long portfolioOid = extractPortfolioOid(oldUid);
    long oldVersion = extractVersion(oldUid);
    long latestVersion = getWorker().selectVersionByPortfolioOidInstant(portfolioOid, instant, true);  // find latest version
    if (oldVersion != latestVersion) {
      throw new DataIntegrityViolationException("Unable to update Portfolio as version is not the latest version");
    }
    getWorker().updatePortfolioSetEndInstant(portfolioOid, latestVersion, instant);  // end-date old version
    return getWorker().insertPortfolio(portfolio, portfolioOid, latestVersion + 1, instant, true);  // insert new version
  }

  @Override
  public UniqueIdentifier removePortfolio(final UniqueIdentifier portfolioUid) {
    ArgumentChecker.notNull(portfolioUid, "portfolio unique identifier");
    Instant instant = Instant.now(getTimeSource());
    checkIdentifierScheme(portfolioUid);
    long portfolioOid = extractPortfolioOid(portfolioUid);
    long oldVersion = extractVersion(portfolioUid);
    long latestVersion = getWorker().selectVersionByPortfolioOidInstant(portfolioOid, instant, true);  // find latest version
    if (oldVersion != latestVersion) {
      throw new DataIntegrityViolationException("Unable to update Portfolio as version is not the latest version");
    }
    Portfolio portfolio = getWorker().selectPortfolioByOidVersion(portfolioOid, latestVersion, true, false);
    getWorker().updatePortfolioSetEndInstant(portfolioOid, latestVersion, instant);  // end-date old version
    return getWorker().insertPortfolio(portfolio, portfolioOid, latestVersion + 1, instant, false);  // insert new version
  }

  @Override
  public UniqueIdentifier reinstatePortfolio(final UniqueIdentifier portfolioUid) {
    ArgumentChecker.notNull(portfolioUid, "portfolio unique identifier");
    Instant instant = Instant.now(getTimeSource());
    checkIdentifierScheme(portfolioUid);
    long portfolioOid = extractPortfolioOid(portfolioUid);
    long latestVersion = getWorker().selectVersionByPortfolioOidInstant(portfolioOid, instant, false);  // find latest version
    Portfolio portfolio = getWorker().selectPortfolioByOidVersion(portfolioOid, latestVersion, false, false);
    getWorker().updatePortfolioSetEndInstant(portfolioOid, latestVersion, instant);  // end-date old version
    return getWorker().insertPortfolio(portfolio, portfolioOid, latestVersion + 1, instant, true);  // insert new version
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a portfolio node to the specified node.
   * <p>
   * If the position is mutable, the unique identifier will be altered.
   * 
   * @param nodeUid  the node to add to, not null
   * @param node  the node to add, not null
   * @return the new unique identifier of the node, not null
   * @throws IllegalArgumentException if the node is not from this position master
   */
  public UniqueIdentifier addPortfolioNode(final UniqueIdentifier nodeUid, final PortfolioNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Updates a portfolio node, without updating child nodes or positions.
   * <p>
   * The node specified must be the latest version.
   * If the node is mutable, the unique identifier will be altered.
   * 
   * @param node  the node to update, not null
   * @return the new unique identifier of the node, not null
   * @throws IllegalArgumentException if the node is not from this position master
   * @throws DataNotFoundException if the node is not found
   */
  public UniqueIdentifier updatePortfolioNodeOnly(final PortfolioNode node) {
    throw new UnsupportedOperationException();
  }

  /**
   * Removes a portfolio node.
   * <p>
   * If the unique identifier contains a version it must be the latest version.
   * <p>
   * Where possible, implementations should retain the data in such a way that the
   * positions can be reinstated.
   * 
   * @param nodeUid  the node unique identifier to remove, not null
   * @return the new unique identifier of the node, not null
   * @throws IllegalArgumentException if the node is not from this position master
   * @throws DataNotFoundException if the node is not found
   */
  public UniqueIdentifier removePortfolioNode(final UniqueIdentifier nodeUid) {
    throw new UnsupportedOperationException();
  }

  /**
   * Reinstates a previously removed portfolio node.
   * <p>
   * Any version in the unique identifier will be ignored.
   * 
   * @param nodeUid  the node unique identifier to reinstate, not null
   * @return the new unique identifier of the node, null if unable to reinstate
   * @throws IllegalArgumentException if the node is not from this position master
   */
  public UniqueIdentifier reinstatePortfolioNode(final UniqueIdentifier nodeUid) {
    throw new UnsupportedOperationException();
  }

  //-------------------------------------------------------------------------
  /**
   * Searches for positions matching the request.
   * 
   * @param request  the request to add, not null
   * @return the matched positions, not null
   */
  public SearchPositionsResult searchPositions(final SearchPositionsRequest request) {
    throw new UnsupportedOperationException();
  }

  /**
   * Adds a position to the specified node.
   * <p>
   * If the position is mutable, the unique identifier will be altered.
   * 
   * @param nodeUid  the node to add to, not null
   * @param position  the position to add, not null
   * @return the new unique identifier of the position, not null
   * @throws IllegalArgumentException if the node is not from this position master
   */
  public UniqueIdentifier addPosition(final UniqueIdentifier nodeUid, final Position position) {
    // TODO
    // check node version is non-null, ours and latest for that node
    // check position uid is null or not ours
    // add latest row to portfolio
    // insert positions
    // update node uid and position uid (in memory)
    throw new UnsupportedOperationException();
  }

  /**
   * Updates a position, including the security key.
   * <p>
   * The position specified must be the latest version.
   * If the position is mutable, the unique identifier will be altered.
   * 
   * @param position  the position to update, not null
   * @return the new unique identifier of the position, not null
   * @throws IllegalArgumentException if the node is not from this position master
   * @throws DataNotFoundException if the position is not found
   */
  public UniqueIdentifier updatePosition(final Position position) {
    // TODO
    // check position uid is non-null, ours and latest for that node
    // add latest row to portfolio
    // update position
    // update position uid (in memory)
    throw new UnsupportedOperationException();
  }

  /**
   * Removes a position.
   * <p>
   * If the unique identifier contains a version it must be the latest version.
   * <p>
   * Where possible, implementations should retain the data in such a way that the
   * positions can be reinstated.
   * 
   * @param positionUid  the position unique identifier to remove, not null
   * @return the new unique identifier of the position, not null
   * @throws IllegalArgumentException if the position is not from this position master
   * @throws DataNotFoundException if the position is not found
   */
  public UniqueIdentifier removePosition(final UniqueIdentifier positionUid) {
    // TODO
    // check node version is non-null, ours and latest for that node
    // check position uid is non-null, ours and latest for that node
    // add latest row to portfolio
    // remove position
    // update node uid and position uid (in memory)
    throw new UnsupportedOperationException();
  }

  /**
   * Reinstates a previously removed position.
   * <p>
   * Any version in the unique identifier will be ignored.
   * 
   * @param positionUid  the position unique identifier to reinstate, not null
   * @return the new unique identifier of the position, null if unable to reinstate
   * @throws IllegalArgumentException if the position is not from this position master
   */
  public UniqueIdentifier reinstatePosition(final UniqueIdentifier positionUid) {
    throw new UnsupportedOperationException();
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
