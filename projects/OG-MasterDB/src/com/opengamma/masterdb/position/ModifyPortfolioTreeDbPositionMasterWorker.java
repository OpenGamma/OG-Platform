/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

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

import com.opengamma.id.UniqueIdentifiables;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePortfolioNode;
import com.opengamma.master.position.PortfolioTreeDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;

/**
 * Position master worker to modify a portfolio tree.
 */
public class ModifyPortfolioTreeDbPositionMasterWorker extends DbPositionMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPortfolioTreeDbPositionMasterWorker.class);

  /**
   * Creates an instance.
   */
  public ModifyPortfolioTreeDbPositionMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected PortfolioTreeDocument addPortfolioTree(final PortfolioTreeDocument document) {
    s_logger.debug("addPortfolioTree{}", document);
    
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
        insertPortfolioTree(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected PortfolioTreeDocument updatePortfolioTree(final PortfolioTreeDocument document) {
    final UniqueIdentifier uid = document.getUniqueId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("updatePortfolioTree {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final PortfolioTreeDocument oldDoc = getPortfolioTreeCheckLatestVersion(uid);
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
        insertPortfolioTree(document);
        // update to remove all positions
        updateRemoveOrphanedPositions(uid, now);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void removePortfolioTree(final UniqueIdentifier uid) {
    s_logger.debug("removePortfolioTree {}", uid);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final PortfolioTreeDocument oldDoc = getPortfolioTreeCheckLatestVersion(uid);
        // update old row
        final Instant now = Instant.now(getTimeSource());
        oldDoc.setVersionToInstant(now);
        updateVersionToInstant(oldDoc);
        // update to remove all positions
        updateRemoveAllPositions(uid, now);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  protected PortfolioTreeDocument correctPortfolioTree(final PortfolioTreeDocument document) {
    final UniqueIdentifier uid = document.getUniqueId();
    ArgumentChecker.isTrue(uid.isVersioned(), "UniqueIdentifier must be versioned");
    s_logger.debug("correctPortfolioTree {}", document);
    
    getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(final TransactionStatus status) {
        // load old row
        final PortfolioTreeDocument oldDoc = getPortfolioTreeCheckLatestCorrection(uid);
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
        insertPortfolioTree(document);
      }
    });
    return document;
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a portfolio tree.
   * @param document  the document, not null
   */
  protected void insertPortfolioTree(final PortfolioTreeDocument document) {
    final Long portfolioId = nextId();
    final Long portfolioOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : portfolioId);
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
    final List<DbMapSqlParameterSource> nodeList = new ArrayList<DbMapSqlParameterSource>();
    insertBuildArgs(document.getPortfolio().getRootNode(), document.getUniqueId() != null, portfolioId, portfolioOid, null, new AtomicInteger(1), 0, nodeList);
    getJdbcTemplate().update(sqlInsertPortfolio(), portfolioArgs);
    getJdbcTemplate().batchUpdate(sqlInsertNode(), nodeList.toArray(new DbMapSqlParameterSource[nodeList.size()]));
    // set the uid
    final UniqueIdentifier uid = createUniqueIdentifier(portfolioOid, portfolioId, null);
    UniqueIdentifiables.setInto(document.getPortfolio(), uid);
    document.setUniqueId(uid);
  }

  /**
   * Recursively create the arguments to insert into the tree existing nodes.
   * @param node  the root node, not null
   * @param update  true if updating portfolio, false if adding new portfolio
   * @param portfolioId  the portfolio id, not null
   * @param portfolioOid  the portfolio oid, not null
   * @param parentNodeId  the parent node id, null if root node
   * @param counter  the counter to create the node id, use {@code getAndIncrement}, not null
   * @param depth  the depth of the node in the portfolio
   * @param argsList  the list of arguments to build, not null
   */
  protected void insertBuildArgs(
      final ManageablePortfolioNode node, final boolean update, final Long portfolioId, final Long portfolioOid, final Long parentNodeId,
      final AtomicInteger counter, final int depth, final List<DbMapSqlParameterSource> argsList) {
    // need to insert parent before children for referential integrity
    final Long nodeId = nextId();
    final Long nodeOid = (update && node.getUniqueIdentifier() != null ? extractOid(node.getUniqueIdentifier()) : nodeId);
    final DbMapSqlParameterSource treeArgs = new DbMapSqlParameterSource()
      .addValue("node_id", nodeId)
      .addValue("node_oid", nodeOid)
      .addValue("portfolio_id", portfolioId)
      .addValue("portfolio_oid", portfolioOid)
      .addValue("parent_node_id", parentNodeId)
      .addValue("depth", depth)
      .addValue("name", StringUtils.defaultString(node.getName()));
    argsList.add(treeArgs);
    // store the left/right before/after the child loop and back fill into stored args row
    treeArgs.addValue("tree_left", counter.getAndIncrement());
    for (ManageablePortfolioNode childNode : node.getChildNodes()) {
      insertBuildArgs(childNode, update, portfolioId, portfolioOid, nodeId, counter, depth + 1, argsList);
    }
    treeArgs.addValue("tree_right", counter.getAndIncrement());
    // set the uid
    final UniqueIdentifier uid = createUniqueIdentifier(nodeOid, nodeId, null);
    UniqueIdentifiables.setInto(node, uid);
  }

  /**
   * Gets the SQL for inserting a portfolio tree.
   * @return the SQL, not null
   */
  protected String sqlInsertPortfolio() {
    return "INSERT INTO pos_portfolio " +
              "(id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, name) " +
            "VALUES " +
              "(:portfolio_id, :portfolio_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :name)";
  }

  /**
   * Gets the SQL for inserting a node.
   * @return the SQL, not null
   */
  protected String sqlInsertNode() {
    return "INSERT INTO pos_node " +
              "(id, oid, portfolio_id, portfolio_oid, parent_node_id, depth, tree_left, tree_right, name) " +
            "VALUES " +
              "(:node_id, :node_oid, :portfolio_id, :portfolio_oid, :parent_node_id, :depth, :tree_left, :tree_right, :name) ";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio tree document ensuring that it is the latest version.
   * @param uid  the unique identifier to load, not null
   * @return the loaded document, not null
   */
  protected PortfolioTreeDocument getPortfolioTreeCheckLatestVersion(final UniqueIdentifier uid) {
    final PortfolioTreeDocument oldDoc = getMaster().getPortfolioTree(uid);  // checks uid exists
    if (oldDoc.getVersionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the version as ended.
   * @param document  the document to update, not null
   */
  protected void updateVersionToInstant(final PortfolioTreeDocument document) {
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
    return "UPDATE pos_portfolio " +
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
  protected PortfolioTreeDocument getPortfolioTreeCheckLatestCorrection(final UniqueIdentifier uid) {
    final PortfolioTreeDocument oldDoc = getMaster().getPortfolioTree(uid);  // checks uid exists
    if (oldDoc.getCorrectionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest correction: " + uid);
    }
    return oldDoc;
  }

  /**
   * Updates the document row to mark the correction as ended.
   * @param document  the document to update, not null
   */
  protected void updateCorrectionToInstant(final PortfolioTreeDocument document) {
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
    return "UPDATE pos_portfolio " +
              "SET corr_to_instant = :corr_to_instant " +
            "WHERE id = :portfolio_id " +
              "AND corr_to_instant = :max_instant ";
  }

  //-------------------------------------------------------------------------
  /**
   * Updates each position as ended where it no longer has an active parent.
   * @param oid  the portfolio object identifier, not null
   * @param endInstant  the end instant, not null
   */
  protected void updateRemoveOrphanedPositions(final UniqueIdentifier oid, final Instant endInstant) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("portfolio_oid", extractOid(oid))
      .addTimestamp("now", endInstant)
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    getJdbcTemplate().update(sqlUpdateRemoveOrphanedPositions(), args);
  }

  /**
   * Gets the SQL for updating the end version of all positions on a portfolio tree.
   * @return the SQL, not null
   */
  protected String sqlUpdateRemoveOrphanedPositions() {
    return "UPDATE pos_position " +
              "SET ver_to_instant = :now " +
            "WHERE portfolio_oid = :portfolio_oid " +
              "AND ver_to_instant = :max_instant " +
              "AND id NOT IN (" +
                "SELECT p.id FROM pos_position p, pos_node n, pos_portfolio f " +
                "WHERE p.portfolio_oid = :portfolio_oid " +
                  "AND p.ver_from_instant <= :now AND p.ver_to_instant > :now " +
                  "AND p.corr_from_instant <= :now AND p.corr_to_instant > :now " +
                  "AND p.parent_node_oid = n.oid " +
                  "AND n.portfolio_id = f.id " +
                  "AND f.ver_from_instant <= :now AND f.ver_to_instant > :now " +
                  "AND f.corr_from_instant <= :now AND f.corr_to_instant > :now " +
              ") ";
  }

  //-------------------------------------------------------------------------
  /**
   * Updates each position as ended where it belongs to the portfolio.
   * This is an efficient version of {@link #updateRemoveOrphanedPositions} for a whole portfolio.
   * @param oid  the portfolio object identifier, not null
   * @param endInstant  the end instant, not null
   */
  protected void updateRemoveAllPositions(final UniqueIdentifier oid, final Instant endInstant) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("portfolio_oid", extractOid(oid))
      .addTimestamp("now", endInstant)
      .addValue("max_instant", DbDateUtils.MAX_SQL_TIMESTAMP);
    getJdbcTemplate().update(sqlUpdateRemoveAllPositions(), args);
  }

  /**
   * Gets the SQL for updating the end version of all positions on a portfolio tree.
   * @return the SQL, not null
   */
  protected String sqlUpdateRemoveAllPositions() {
    return "UPDATE pos_position " +
              "SET ver_to_instant = :now " +
            "WHERE portfolio_oid = :portfolio_oid " +
              "AND ver_to_instant = :max_instant ";
  }

}
