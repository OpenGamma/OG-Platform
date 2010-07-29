/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.opengamma.DataNotFoundException;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifiables;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.DbMapSqlParameterSource;

/**
 * Position master worker to modify a position.
 */
public class ModifyPositionDbPositionMasterWorker extends DbPositionMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPositionDbPositionMasterWorker.class);

  /**
   * Creates an instance.
   */
  public ModifyPositionDbPositionMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected PositionDocument addPosition(final PositionDocument document) {
    s_logger.debug("addPosition {}", document);
    // simply replace input values for the moment
    // to use them would require much more validation
    final Instant now = Instant.now(getTimeSource());
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    
    document.setParentNodeId(document.getParentNodeId().toLatest());
    document.setPortfolioId(checkNodeGetPortfolioOid(document));
    document.setPositionId(null);
    
    insertPosition(document);
    return document;
  }

  //-------------------------------------------------------------------------
  /**
   * Validates that the node exists, returning the associated portfolio oid.
   * @param document  the document, not null
   * @return the portfolio oid, not null
   */
  protected UniqueIdentifier checkNodeGetPortfolioOid(final PositionDocument document) {
    final UniqueIdentifier nodeOid = document.getParentNodeId().toLatest();
    final MapSqlParameterSource args = new MapSqlParameterSource()
      .addValue("node_oid", nodeOid.getValue());
    final List<Map<String, Object>> data = getTemplate().queryForList(sqlSelectCheckNodeGetPortfolioOid(), args);
    if (data.size() == 0) {
      throw new DataNotFoundException("Parent node not found: " + nodeOid);
    }
    if (data.size() > 1 || data.get(0).containsKey("PORTFOLIO_OID") == false) {
      throw new IncorrectResultSizeDataAccessException("Parent node table invalid: " + nodeOid, 1, data.size());
    }
    final long portfolioOid = (Long) data.get(0).get("PORTFOLIO_OID");
    return createObjectIdentifier(portfolioOid, null);
  }

  /**
   * Gets the SQL for inserting a position.
   * @return the SQL, not null
   */
  protected String sqlSelectCheckNodeGetPortfolioOid() {
    // check portfolio/node instant
    return "SELECT DISTINCT p.oid AS portfolio_oid " +
            "FROM pos_node n, pos_position p " +
            "WHERE n.portfolio_id = p.id " +
              "AND n.oid = :node_oid ";
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a position.
   * @param document  the document, not null
   */
  protected void insertPosition(final PositionDocument document) {
    final long positionId = nextId();
    // the arguments for inserting into the position table
    final DbMapSqlParameterSource positionArgs = new DbMapSqlParameterSource()
      .addValue("position_id", positionId)
      .addValue("position_oid", positionId)
      .addValue("portfolio_oid", document.getPortfolioId().toLatest().getValue())
      .addValue("parent_node_oid", document.getParentNodeId().toLatest().getValue())
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("quantity", document.getPosition().getQuantity());
    // the arguments for inserting into the seckey table
    final List<DbMapSqlParameterSource> secKeyList = new ArrayList<DbMapSqlParameterSource>();
    for (Identifier id : document.getPosition().getSecurityKey()) {
      final long secKeyId = nextId();
      final DbMapSqlParameterSource treeArgs = new DbMapSqlParameterSource()
        .addValue("seckey_id", secKeyId)
        .addValue("position_id", positionId)
        .addValue("id_scheme", id.getScheme().getName())
        .addValue("id_value", id.getValue());
      secKeyList.add(treeArgs);
    }
    getTemplate().update(sqlInsertPosition(), positionArgs);
    getTemplate().batchUpdate(sqlInsertSecurityKey(), (DbMapSqlParameterSource[]) secKeyList.toArray(new DbMapSqlParameterSource[secKeyList.size()]));
    // set the uid
    final UniqueIdentifier uid = createUniqueIdentifier(positionId, positionId, null);
    UniqueIdentifiables.setInto(document.getPosition(), uid);
    document.setPositionId(uid);
  }

  /**
   * Gets the SQL for inserting a position.
   * @return the SQL, not null
   */
  protected String sqlInsertPosition() {
    return "INSERT INTO pos_position " +
              "(id, oid, portfolio_oid, parent_node_oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, quantity) " +
            "VALUES " +
              "(:position_id, :position_oid, :portfolio_oid, :parent_node_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :quantity)";
  }

  /**
   * Gets the SQL for inserting a security key.
   * @return the SQL, not null
   */
  protected String sqlInsertSecurityKey() {
    return "INSERT INTO pos_securitykey " +
              "(id, position_id, id_scheme, id_value) " +
            "VALUES " +
              "(:seckey_id, :position_id, :id_scheme, :id_value)";
  }

}
