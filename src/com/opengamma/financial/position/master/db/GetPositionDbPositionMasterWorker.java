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

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionSearchHistoricRequest;
import com.opengamma.financial.position.master.PositionSearchHistoricResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.Paging;
import com.opengamma.util.time.DateUtil;

/**
 * Position master worker to get the position.
 */
public class GetPositionDbPositionMasterWorker extends DbPositionMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(GetPositionDbPositionMasterWorker.class);

  /**
   * Creates an instance.
   */
  public GetPositionDbPositionMasterWorker() {
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
    final PositionSearchHistoricRequest request = new PositionSearchHistoricRequest(uid, now, now);
    final PositionSearchHistoricResult result = getMaster().searchPositionHistoric(request);
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
  @SuppressWarnings("unchecked")
  protected PositionDocument getPositionById(final UniqueIdentifier uid) {
    s_logger.debug("getPositionById {}", uid);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("position_id", uid.getVersion());
    final PositionDocumentExtractor extractor = new PositionDocumentExtractor();
    final NamedParameterJdbcOperations namedJdbc = getTemplate().getNamedParameterJdbcOperations();
    final List<PositionDocument> docs = (List<PositionDocument>) namedJdbc.query(sqlGetPositionById(), args, extractor);
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
    // TODO: validate portfolio/node still valid
    return "SELECT " +
              "p.id AS position_id, " +
              "p.oid AS position_oid, " +
              "p.portfolio_oid AS portfolio_oid, " +
              "p.parent_node_oid AS parent_node_oid, " +
              "p.ver_from_instant AS ver_from_instant, " +
              "p.ver_to_instant AS ver_to_instant, " +
              "p.corr_from_instant AS corr_from_instant, " +
              "p.corr_to_instant AS corr_to_instant, " +
              "p.quantity AS quantity, " +
              "s.id_scheme AS seckey_scheme," +
              "s.id_value AS seckey_value " +
            "FROM pos_position p " +
              "LEFT JOIN pos_securitykey s ON (s.position_id = p.id) " +
            "WHERE p.id = :position_id ";
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  protected PositionSearchHistoricResult searchPositionHistoric(final PositionSearchHistoricRequest request) {
    final UniqueIdentifier oid = request.getPositionId().toLatest();
    final Instant now = Instant.now(getTimeSource());
    s_logger.debug("searchPositionHistoric: {}", request);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("position_oid", oid.getValue())
      .addTimestampNullIgnored("versions_from_instant", request.getVersionsFromInstant())
      .addTimestampNullIgnored("versions_to_instant", request.getVersionsToInstant())
      .addTimestampNullIgnored("corrections_from_instant", request.getCorrectionsFromInstant())
      .addTimestampNullIgnored("corrections_to_instant", request.getCorrectionsToInstant());
    final String[] sql = sqlSearchPositions(request, now);
    final NamedParameterJdbcOperations namedJdbc = getTemplate().getNamedParameterJdbcOperations();
    final int count = namedJdbc.queryForInt(sql[1], args);
    final PositionSearchHistoricResult result = new PositionSearchHistoricResult();
    result.setPaging(new Paging(request.getPagingRequest(), count));
    if (count > 0) {
      final PositionDocumentExtractor extractor = new PositionDocumentExtractor();
      result.getDocuments().addAll((List<PositionDocument>) namedJdbc.query(sql[0], args, extractor));
    }
    return result;
  }

  /**
   * Gets the SQL for searching the history of a position.
   * @param request  the request, not null
   * @param now  the current instant, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchPositions(final PositionSearchHistoricRequest request, final Instant now) {
    // TODO: validate portfolio/node still valid
    String select = "SELECT " +
              "p.id AS position_id, " +
              "p.oid AS position_oid, " +
              "p.portfolio_oid AS portfolio_oid, " +
              "p.parent_node_oid AS parent_node_oid, " +
              "p.ver_from_instant AS ver_from_instant, " +
              "p.ver_to_instant AS ver_to_instant, " +
              "p.corr_from_instant AS corr_from_instant, " +
              "p.corr_to_instant AS corr_to_instant, " +
              "p.quantity AS quantity, " +
              "s.id_scheme AS seckey_scheme," +
              "s.id_value AS seckey_value ";
    String fromWhere =
            "FROM pos_position p " +
              "LEFT JOIN pos_securitykey s ON (s.position_id = p.id) " +
            "WHERE p.oid = :position_oid ";
    if (request.getVersionsFromInstant() != null && request.getVersionsFromInstant().equals(request.getVersionsToInstant())) {
      fromWhere += "AND (p.ver_from_instant <= :versions_from_instant AND p.ver_to_instant > :versions_from_instant) ";
    } else {
      if (request.getVersionsFromInstant() != null) {
        fromWhere += "AND ((p.ver_from_instant <= :versions_from_instant AND p.ver_to_instant > :versions_from_instant) " +
                            "OR p.ver_from_instant >= :versions_from_instant) ";
      }
      if (request.getVersionsToInstant() != null) {
        fromWhere += "AND ((p.ver_from_instant <= :versions_to_instant AND p.ver_to_instant > :versions_to_instant) " +
                            "OR p.ver_to_instant < :versions_to_instant) ";
      }
    }
    if (request.getCorrectionsFromInstant() != null && request.getCorrectionsFromInstant().equals(request.getCorrectionsToInstant())) {
      fromWhere += "AND (p.corr_from_instant <= :corrections_from_instant AND p.corr_to_instant > :corrections_from_instant) ";
    } else {
      if (request.getCorrectionsFromInstant() != null) {
        fromWhere += "AND ((p.corr_from_instant <= :corrections_from_instant AND p.corr_to_instant > :corrections_from_instant) " +
                            "OR p.corr_from_instant >= :corrections_from_instant) ";
      }
      if (request.getCorrectionsToInstant() != null) {
        fromWhere += "AND ((p.corr_from_instant <= :corrections_to_instant AND p.ver_to_instant > :corrections_to_instant) " +
                            "OR p.corr_to_instant < :corrections_to_instant) ";
      }
    }
    String search = getDbHelper().sqlApplyPaging(select + fromWhere, "ORDER BY p.ver_from_instant DESC, p.corr_from_instant DESC ", request.getPagingRequest());
    String count = "SELECT COUNT(*) " + fromWhere;
    return new String[] {search, count};
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a PositionDocument.
   */
  protected final class PositionDocumentExtractor implements ResultSetExtractor {
    private long _lastPositionId = -1;
    private PositionImpl _position;
    private List<PositionDocument> _documents = new ArrayList<PositionDocument>();
    private Map<UniqueIdentifier, UniqueIdentifier> _deduplicate = Maps.newHashMap();

    @Override
    public Object extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long positionId = rs.getLong("POSITION_ID");
        if (_lastPositionId != positionId) {
          _lastPositionId = positionId;
          buildPosition(rs, positionId);
        }
        final String idScheme = rs.getString("SECKEY_SCHEME");
        final String idValue = rs.getString("SECKEY_VALUE");
        Identifier id = Identifier.of(idScheme, idValue);
        _position.setSecurityKey(_position.getSecurityKey().withIdentifier(id));
      }
      return _documents;
    }

    private void buildPosition(final ResultSet rs, final long positionId) throws SQLException {
      final long positionOid = rs.getLong("POSITION_OID");
      final long portfolioOid = rs.getLong("PORTFOLIO_OID");
      final long parentNodeOid = rs.getLong("PARENT_NODE_OID");
      final BigDecimal quantity = rs.getBigDecimal("QUANTITY").stripTrailingZeros();  // strip zeroes as DB adds them
      final Timestamp validFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp validTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      final UniqueIdentifier uid = createUniqueIdentifier(positionOid, positionId, _deduplicate);
      _position = new PositionImpl(uid, quantity, IdentifierBundle.EMPTY);
      PositionDocument doc = new PositionDocument(_position);
      doc.setVersionFromInstant(DateUtil.fromSqlTimestamp(validFrom));
      doc.setVersionToInstant(DateUtil.fromSqlTimestamp(validTo));
      doc.setCorrectionFromInstant(DateUtil.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DateUtil.fromSqlTimestamp(correctionTo));
      doc.setPortfolioId(createObjectIdentifier(portfolioOid, _deduplicate));
      doc.setParentNodeId(createObjectIdentifier(parentNodeOid, _deduplicate));
      doc.setPositionId(createUniqueIdentifier(positionOid, positionId, _deduplicate));
      _documents.add(doc);
    }
  }

}
