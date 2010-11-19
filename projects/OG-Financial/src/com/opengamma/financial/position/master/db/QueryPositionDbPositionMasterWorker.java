/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.financial.position.master.ManageablePosition;
import com.opengamma.financial.position.master.ManageableTrade;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionHistoryRequest;
import com.opengamma.financial.position.master.PositionHistoryResult;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.Paging;

/**
 * Position master worker to get the position.
 */
public class QueryPositionDbPositionMasterWorker extends DbPositionMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorker.class);
  /**
   * SQL select.
   */
  protected static final String SELECT =
      "SELECT " +
        "p.id AS position_id, " +
        "p.oid AS position_oid, " +
        "p.portfolio_oid AS portfolio_oid, " +
        "p.parent_node_oid AS parent_node_oid, " +
        "p.ver_from_instant AS ver_from_instant, " +
        "p.ver_to_instant AS ver_to_instant, " +
        "p.corr_from_instant AS corr_from_instant, " +
        "p.corr_to_instant AS corr_to_instant, " +
        "p.quantity AS quantity, " +
        "s.id_scheme AS seckey_scheme, " +
        "s.id_value AS seckey_value, " +
        "t.quantity AS trade_quantity, " +
        "t.trade_instant AS trade_instant, " +
        "t.cparty_scheme AS cparty_scheme, " +
        "t.cparty_value AS cparty_value ";
  /**
   * SQL from.
   */
  protected static final String FROM =
      "FROM pos_position p LEFT JOIN pos_securitykey s ON (s.position_id = p.id) LEFT JOIN pos_trade t ON (t.position_id = p.id) ";

  /**
   * Creates an instance.
   */
  public QueryPositionDbPositionMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected PositionDocument getPosition(final UniqueIdentifier uid) {
    if (uid.isVersioned()) {
      return getPositionById(uid);
    } else {
      return getPositionByLatest(uid);
    }
  }

  /**
   * Gets a position by searching for the latest version of an object identifier.
   * @param uid  the unique identifier
   * @return the position document, null if not found
   */
  protected PositionDocument getPositionByLatest(final UniqueIdentifier uid) {
    s_logger.debug("getPositionByLatest: {}", uid);
    final Instant now = Instant.now(getTimeSource());
    final PositionHistoryRequest request = new PositionHistoryRequest(uid, now, now);
    final PositionHistoryResult result = getMaster().historyPosition(request);
    if (result.getDocuments().size() != 1) {
      throw new DataNotFoundException("Position not found: " + uid);
    }
    return result.getFirstDocument();
  }

  /**
   * Gets a position by identifier.
   * @param uid  the unique identifier
   * @return the position document, null if not found
   */
  protected PositionDocument getPositionById(final UniqueIdentifier uid) {
    s_logger.debug("getPositionById {}", uid);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("position_id", extractRowId(uid));
    final PositionDocumentExtractor extractor = new PositionDocumentExtractor();
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<PositionDocument> docs = namedJdbc.query(sqlGetPositionById(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Position not found: " + uid);
    }
    return docs.get(0);
  }

  /**
   * Gets the SQL for getting a position by unique row identifier.
   * @return the SQL, not null
   */
  protected String sqlGetPositionById() {
    return SELECT + FROM + "WHERE p.id = :position_id ";
  }

  //-------------------------------------------------------------------------
  @Override
  protected PositionSearchResult searchPositions(PositionSearchRequest request) {
    s_logger.debug("searchPositions: {}", request);
    final Instant now = Instant.now(getTimeSource());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(request.getVersionAsOfInstant(), now))
      .addTimestamp("corrected_to_instant", Objects.firstNonNull(request.getCorrectedToInstant(), now))
      .addValueNullIgnored("min_quantity", request.getMinQuantity())
      .addValueNullIgnored("max_quantity", request.getMaxQuantity());
    if (request.getPortfolioId() != null) {
      args.addValue("portfolio_oid", extractOid(request.getPortfolioId()));
    }
    if (request.getParentNodeId() != null) {
      args.addValue("parent_node_oid", extractOid(request.getParentNodeId()));
    }
    s_logger.debug("args: {}", args);
    // TODO: security key
    final String[] sql = sqlSearchPositions(request);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final int count = namedJdbc.queryForInt(sql[1], args);
    final PositionSearchResult result = new PositionSearchResult();
    result.setPaging(new Paging(request.getPagingRequest(), count));
    if (count > 0) {
      final PositionDocumentExtractor extractor = new PositionDocumentExtractor();
      result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
    }
    return result;
  }

  /**
   * Gets the SQL to search for positions.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchPositions(final PositionSearchRequest request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
                "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
    if (request.getPortfolioId() != null) {
      where += "AND portfolio_oid = :portfolio_oid ";
    }
    if (request.getParentNodeId() != null) {
      where += "AND parent_node_oid = :parent_node_oid ";
    }
    if (request.getMinQuantity() != null) {
      where += "AND quantity >= :min_quantity ";
    }
    if (request.getMaxQuantity() != null) {
      where += "AND quantity < :max_quantity ";
    }
    String selectFromWhereInner = "SELECT id FROM pos_position " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY id ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE p.id IN (" + inner + ") ORDER BY p.id ";
    String count = "SELECT COUNT(*) FROM pos_position " + where;
    return new String[] {search, count};
  }

  //-------------------------------------------------------------------------
  @Override
  protected PositionHistoryResult historyPosition(final PositionHistoryRequest request) {
    s_logger.debug("searchPositionHistoric: {}", request);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("position_oid", extractOid(request.getPositionId()))
      .addTimestampNullIgnored("versions_from_instant", request.getVersionsFromInstant())
      .addTimestampNullIgnored("versions_to_instant", request.getVersionsToInstant())
      .addTimestampNullIgnored("corrections_from_instant", request.getCorrectionsFromInstant())
      .addTimestampNullIgnored("corrections_to_instant", request.getCorrectionsToInstant());
    final String[] sql = sqlSearchPositionHistoric(request);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final int count = namedJdbc.queryForInt(sql[1], args);
    final PositionHistoryResult result = new PositionHistoryResult();
    result.setPaging(new Paging(request.getPagingRequest(), count));
    if (count > 0) {
      final PositionDocumentExtractor extractor = new PositionDocumentExtractor();
      result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
    }
    return result;
  }

  /**
   * Gets the SQL for searching the history of a position.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchPositionHistoric(final PositionHistoryRequest request) {
    String where = "WHERE oid = :position_oid ";
    if (request.getVersionsFromInstant() != null && request.getVersionsFromInstant().equals(request.getVersionsToInstant())) {
      where += "AND ver_from_instant <= :versions_from_instant AND ver_to_instant > :versions_from_instant ";
    } else {
      if (request.getVersionsFromInstant() != null) {
        where += "AND ((ver_from_instant <= :versions_from_instant AND ver_to_instant > :versions_from_instant) " +
                            "OR ver_from_instant >= :versions_from_instant) ";
      }
      if (request.getVersionsToInstant() != null) {
        where += "AND ((ver_from_instant <= :versions_to_instant AND ver_to_instant > :versions_to_instant) " +
                            "OR ver_to_instant < :versions_to_instant) ";
      }
    }
    if (request.getCorrectionsFromInstant() != null && request.getCorrectionsFromInstant().equals(request.getCorrectionsToInstant())) {
      where += "AND corr_from_instant <= :corrections_from_instant AND corr_to_instant > :corrections_from_instant ";
    } else {
      if (request.getCorrectionsFromInstant() != null) {
        where += "AND ((corr_from_instant <= :corrections_from_instant AND corr_to_instant > :corrections_from_instant) " +
                            "OR corr_from_instant >= :corrections_from_instant) ";
      }
      if (request.getCorrectionsToInstant() != null) {
        where += "AND ((corr_from_instant <= :corrections_to_instant AND ver_to_instant > :corrections_to_instant) " +
                            "OR corr_to_instant < :corrections_to_instant) ";
      }
    }
    String selectFromWhereInner = "SELECT id FROM pos_position " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY ver_from_instant DESC, corr_from_instant DESC ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE p.id IN (" + inner + ") ORDER BY p.ver_from_instant DESC, p.corr_from_instant DESC";
    String count = "SELECT COUNT(*) FROM pos_position " + where;
    return new String[] {search, count};
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a PositionDocument.
   */
  protected final class PositionDocumentExtractor implements ResultSetExtractor<List<PositionDocument>> {
    private long _lastPositionId = -1;
    private ManageablePosition _position;
    private List<PositionDocument> _documents = new ArrayList<PositionDocument>();
    private Map<UniqueIdentifier, UniqueIdentifier> _deduplicate = Maps.newHashMap();

    @Override
    public List<PositionDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long positionId = rs.getLong("POSITION_ID");
        if (_lastPositionId != positionId) {
          _lastPositionId = positionId;
          buildPosition(rs, positionId);
        }
        final String idScheme = rs.getString("SECKEY_SCHEME");
        final String idValue = rs.getString("SECKEY_VALUE");
        if (idScheme != null && idValue != null) {
          Identifier id = Identifier.of(idScheme, idValue);
          _position.setSecurityKey(_position.getSecurityKey().withIdentifier(id));
        }
        
        final Timestamp tradeInstant = rs.getTimestamp("TRADE_INSTANT");
        if (tradeInstant != null) {
          ManageableTrade trade = new ManageableTrade();
          final BigDecimal tradeQuantity = extractBigDecimal(rs, "TRADE_QUANTITY");
          trade.setQuantity(tradeQuantity);
          trade.setTradeInstant(DbDateUtils.fromSqlTimestamp(tradeInstant));
          final String cpartyScheme = rs.getString("CPARTY_SCHEME");
          final String cpartyValue = rs.getString("CPARTY_VALUE");
          if (cpartyScheme != null && cpartyValue != null) {
            Identifier id = Identifier.of(cpartyScheme, cpartyValue);
            trade.setCounterpartyId(id);
          }
          _position.getTrades().add(trade);
        }
      }
      return _documents;
    }

    private void buildPosition(final ResultSet rs, final long positionId) throws SQLException {
      final long positionOid = rs.getLong("POSITION_OID");
      final long portfolioOid = rs.getLong("PORTFOLIO_OID");
      final long parentNodeOid = rs.getLong("PARENT_NODE_OID");
      final BigDecimal quantity = extractBigDecimal(rs, "QUANTITY");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      _position = new ManageablePosition(quantity, IdentifierBundle.EMPTY);
      _position.setUniqueIdentifier(createUniqueIdentifier(positionOid, positionId, _deduplicate));
      PositionDocument doc = new PositionDocument(_position);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setPortfolioId(createObjectIdentifier(portfolioOid, _deduplicate));
      doc.setParentNodeId(createObjectIdentifier(parentNodeOid, _deduplicate));
      doc.setPositionId(createUniqueIdentifier(positionOid, positionId, _deduplicate));
      _documents.add(doc);
    }
  }

}
