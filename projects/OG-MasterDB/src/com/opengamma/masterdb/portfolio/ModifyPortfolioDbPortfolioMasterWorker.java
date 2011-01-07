/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.portfolio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.time.Instant;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;

/**
 * Portfolio master worker to modify a portfolio.
 */
public class ModifyPortfolioDbPortfolioMasterWorker extends DbPortfolioMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPortfolioDbPortfolioMasterWorker.class);

  /**
   * Creates an instance.
   */
  public ModifyPortfolioDbPortfolioMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected PortfolioDocument add(final PortfolioDocument document) {
    s_logger.debug("addPortfolio {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // insert new row
        final Instant now = Instant.now(getTimeSource());
        document.setVersionFromInstant(now);
        document.setVersionToInstant(null);
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setUniqueId(null);
        insert(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected PortfolioDocument update(final PortfolioDocument document) {
    final UniqueIdentifier uid = document.getUniqueId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("updatePortfolio {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final PortfolioDocument oldDoc = getCheckLatestVersion(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setVersionToInstant(now);
        updateVersionToInstant(oldDoc);
        // insert new row
        document.setVersionFromInstant(now);
        document.setVersionToInstant(null);
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setUniqueId(oldDoc.getUniqueId().toLatest());
        insert(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void remove(final UniqueIdentifier uid) {
    s_logger.debug("removePortfolio {}", uid);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final PortfolioDocument oldDoc = getCheckLatestVersion(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setVersionToInstant(now);
        updateVersionToInstant(oldDoc);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  protected PortfolioDocument correct(final PortfolioDocument document) {
    final UniqueIdentifier uid = document.getUniqueId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("correctPortfolio {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final PortfolioDocument oldDoc = getCheckLatestCorrection(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setCorrectionToInstant(now);
        updateCorrectionToInstant(oldDoc);
        // insert new row
        document.setVersionFromInstant(oldDoc.getVersionFromInstant());
        document.setVersionToInstant(oldDoc.getVersionToInstant());
        document.setCorrectionFromInstant(now);
        document.setCorrectionToInstant(null);
        document.setUniqueId(oldDoc.getUniqueId().toLatest());
        insert(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a portfolio tree.
   * @param document  the document, not null
   */
  protected void insert(final PortfolioDocument document) {
    final Long portfolioId = nextId();
    final Long portfolioOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : portfolioId);
    final UniqueIdentifier portfolioUid = createUniqueIdentifier(portfolioOid, portfolioId);
    // the arguments for inserting into the portfolio table
    final DbMapSqlParameterSource portfolioArgs = new DbMapSqlParameterSource()
      .addValue("portfolio_id", portfolioId)
      .addValue("portfolio_oid", portfolioOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("name", StringUtils.defaultString(document.getPortfolio().getName()));
    // the arguments for inserting into the node table
    final List<DbMapSqlParameterSource> nodeList = new ArrayList<DbMapSqlParameterSource>(256);
    final List<DbMapSqlParameterSource> posList = new ArrayList<DbMapSqlParameterSource>(256);
    insertBuildArgs(portfolioUid, null, document.getPortfolio().getRootNode(), document.getUniqueId() != null,
        portfolioId, portfolioOid, null, null,
        new AtomicInteger(1), 0, nodeList, posList);
    getJdbcTemplate().update(sqlInsertPortfolio(), portfolioArgs);
    getJdbcTemplate().batchUpdate(sqlInsertNode(), nodeList.toArray(new DbMapSqlParameterSource[nodeList.size()]));
    getJdbcTemplate().batchUpdate(sqlInsertPosition(), posList.toArray(new DbMapSqlParameterSource[posList.size()]));
    // set the uid
    document.getPortfolio().setUniqueId(portfolioUid);
    document.setUniqueId(portfolioUid);
  }

  /**
   * Recursively create the arguments to insert into the tree existing nodes.
   * @param portfolioUid  the portfolio unique identifier, not null
   * @param parentNodeUid  the parent node unique identifier, not null
   * @param node  the root node, not null
   * @param update  true if updating portfolio, false if adding new portfolio
   * @param portfolioId  the portfolio id, not null
   * @param portfolioOid  the portfolio oid, not null
   * @param parentNodeId  the parent node id, null if root node
   * @param parentNodeOid  the parent node oid, null if root node
   * @param counter  the counter to create the node id, use {@code getAndIncrement}, not null
   * @param depth  the depth of the node in the portfolio
   * @param argsList  the list of arguments to build, not null
   * @param posList  the list of arguments to for inserting positions, not null
   */
  protected void insertBuildArgs(
      final UniqueIdentifier portfolioUid, final UniqueIdentifier parentNodeUid,
      final ManageablePortfolioNode node, final boolean update,
      final Long portfolioId, final Long portfolioOid, final Long parentNodeId, final Long parentNodeOid,
      final AtomicInteger counter, final int depth, final List<DbMapSqlParameterSource> argsList, final List<DbMapSqlParameterSource> posList) {
    // need to insert parent before children for referential integrity
    final Long nodeId = nextId();
    final Long nodeOid = (update && node.getUniqueId() != null ? extractOid(node.getUniqueId()) : nodeId);
    UniqueIdentifier nodeUid = createUniqueIdentifier(nodeOid, nodeId);
    node.setUniqueId(nodeUid);
    node.setParentNodeId(parentNodeUid);
    node.setPortfolioId(portfolioUid);
    final DbMapSqlParameterSource treeArgs = new DbMapSqlParameterSource()
      .addValue("node_id", nodeId)
      .addValue("node_oid", nodeOid)
      .addValue("portfolio_id", portfolioId)
      .addValue("portfolio_oid", portfolioOid)
      .addValue("parent_node_id", parentNodeId)
      .addValue("parent_node_oid", parentNodeOid)
      .addValue("depth", depth)
      .addValue("name", StringUtils.defaultString(node.getName()));
    argsList.add(treeArgs);
    
    // store position links
    for (UniqueIdentifier uid : node.getPositionIds()) {
      final DbMapSqlParameterSource posArgs = new DbMapSqlParameterSource()
        .addValue("node_id", nodeId)
        .addValue("key_scheme", uid.getScheme())
        .addValue("key_value", uid.getValue());
      posList.add(posArgs);
    }
    
    // store the left/right before/after the child loop and back fill into stored args row
    treeArgs.addValue("tree_left", counter.getAndIncrement());
    for (ManageablePortfolioNode childNode : node.getChildNodes()) {
      insertBuildArgs(portfolioUid, nodeUid, childNode, update, portfolioId, portfolioOid, nodeId, nodeOid, counter, depth + 1, argsList, posList);
    }
    treeArgs.addValue("tree_right", counter.getAndIncrement());
  }

  /**
   * Gets the SQL for inserting a portfolio.
   * @return the SQL, not null
   */
  protected String sqlInsertPortfolio() {
    return "INSERT INTO prt_portfolio " +
              "(id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, name) " +
            "VALUES " +
              "(:portfolio_id, :portfolio_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :name)";
  }

  /**
   * Gets the SQL for inserting a node.
   * @return the SQL, not null
   */
  protected String sqlInsertNode() {
    return "INSERT INTO prt_node " +
              "(id, oid, portfolio_id, portfolio_oid, parent_node_id, parent_node_oid, depth, tree_left, tree_right, name) " +
            "VALUES " +
              "(:node_id, :node_oid, :portfolio_id, :portfolio_oid, :parent_node_id, :parent_node_oid, :depth, :tree_left, :tree_right, :name) ";
  }

  /**
   * Gets the SQL for inserting a position.
   * @return the SQL, not null
   */
  protected String sqlInsertPosition() {
    return "INSERT INTO prt_position (node_id, key_scheme, key_value) " +
            "VALUES " +
            "(:node_id, :key_scheme, :key_value)";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio tree document ensuring that it is the latest version.
   * @param uid  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected PortfolioDocument getCheckLatestVersion(final UniqueIdentifier uid) {
    final PortfolioDocument oldDoc = getMaster().get(uid);  // checks uid exists
    if (oldDoc.getVersionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the version as ended.
   * @param document  the document to update, not null
   */
  protected void updateVersionToInstant(final PortfolioDocument document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("portfolio_id", extractRowId(document.getUniqueId()))
      .addTimestamp("ver_to_instant", document.getVersionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateVersionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end version instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end version of a portfolio tree.
   * @return the SQL, not null
   */
  protected String sqlUpdateVersionToInstant() {
    return "UPDATE prt_portfolio " +
              "SET ver_to_instant = :ver_to_instant " +
            "WHERE id = :portfolio_id " +
              "AND ver_to_instant = :max_instant ";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio tree document ensuring that it is the latest correction.
   * @param uid  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected PortfolioDocument getCheckLatestCorrection(final UniqueIdentifier uid) {
    final PortfolioDocument oldDoc = getMaster().get(uid);  // checks uid exists
    if (oldDoc.getCorrectionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest correction: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the correction as ended.
   * @param document  the document to update, not null
   */
  protected void updateCorrectionToInstant(final PortfolioDocument document) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("portfolio_id", extractRowId(document.getUniqueId()))
      .addTimestamp("corr_to_instant", document.getCorrectionToInstant())
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    int rowsUpdated = getJdbcTemplate().update(sqlUpdateCorrectionToInstant(), args);
    if (rowsUpdated != 1) {
      throw new IncorrectUpdateSemanticsDataAccessException("Update end correction instant failed, rows updated: " + rowsUpdated);
    }
  }

  /**
   * Gets the SQL for updating the end correction of a portfolio tree.
   * @return the SQL, not null
   */
  protected String sqlUpdateCorrectionToInstant() {
    return "UPDATE prt_portfolio " +
              "SET corr_to_instant = :corr_to_instant " +
            "WHERE id = :portfolio_id " +
              "AND corr_to_instant = :max_instant ";
  }

}
