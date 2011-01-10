/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.google.common.base.Objects;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.Paging;
import com.opengamma.util.db.PagingRequest;

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
        "p.ver_from_instant AS ver_from_instant, " +
        "p.ver_to_instant AS ver_to_instant, " +
        "p.corr_from_instant AS corr_from_instant, " +
        "p.corr_to_instant AS corr_to_instant, " +
        "p.provider_scheme AS provider_scheme, " +
        "p.provider_value AS provider_value, " +
        "p.quantity AS pos_quantity, " +
        "ps.key_scheme AS pos_key_scheme, " +
        "ps.key_value AS pos_key_value, " +
        "t.id AS trade_id, " +
        "t.oid AS trade_oid, " +
        "t.quantity AS trade_quantity, " +
        "t.trade_date AS trade_date, " +
        "t.trade_time AS trade_time, " +
        "t.zone_offset AS zone_offset, " +
        "t.cparty_scheme AS cparty_scheme, " +
        "t.cparty_value AS cparty_value, " +
        "ts.key_scheme AS trade_key_scheme, " +
        "ts.key_value AS trade_key_value ";
    
  /**
   * SQL from.
   */
  protected static final String FROM =
      "FROM pos_position p " +
      "LEFT JOIN pos_position2idkey pi ON (pi.position_id = p.id) " +
      "LEFT JOIN pos_idkey ps ON (pi.idkey_id = ps.id) " +
      "LEFT JOIN pos_trade t ON (p.id = t.position_id) " +
      "LEFT JOIN pos_trade2idkey ti ON (t.id = ti.trade_id) " +
      "LEFT JOIN pos_idkey ts ON (ti.idkey_id = ts.id) ";
  /**
   * SQL order by.
   */
  protected static final String ORDER_BY = "ORDER BY p.oid, t.trade_date, t.id ";
  
  /**
   * Creates an instance.
   */
  public QueryPositionDbPositionMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected PositionDocument get(final UniqueIdentifier uid) {
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
    return getPositionByOidInstants(uid, now, now);
  }

  /**
   * Gets a position by object identifier at instants.
   * @param oid  the position oid, not null
   * @param versionAsOf  the version instant, not null
   * @param correctedTo  the corrected to instant, not null
   * @return the position document, null if not found
   */
  protected PositionDocument getPositionByOidInstants(final UniqueIdentifier oid, final Instant versionAsOf, final Instant correctedTo) {
    s_logger.debug("getPositionByOidInstants {}", oid);
    final long portfolioOid = extractOid(oid);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("position_oid", portfolioOid)
      .addTimestamp("version_as_of", versionAsOf)
      .addTimestamp("corrected_to", correctedTo);
    final PositionDocumentExtractor extractor = new PositionDocumentExtractor();
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<PositionDocument> docs = namedJdbc.query(sqlSelectPositionByOidInstants(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Position not found: " + oid);
    }
    return docs.get(0);
  }

  /**
   * Gets the SQL for getting a position by object identifier and instants.
   * @return the SQL, not null
   */
  protected String sqlSelectPositionByOidInstants() {
    return SELECT + FROM +
      "WHERE p.oid = :position_oid " +
        "AND p.ver_from_instant <= :version_as_of AND p.ver_to_instant > :version_as_of " +
        "AND p.corr_from_instant <= :corrected_to AND p.corr_to_instant > :corrected_to " +
      ORDER_BY;
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
    NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
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
    return SELECT + FROM +
      "WHERE p.id = :position_id " +
      ORDER_BY;
  }

  //-------------------------------------------------------------------------
  @Override
  protected PositionSearchResult search(PositionSearchRequest request) {
    s_logger.debug("searchPositions: {}", request);
    final PositionSearchResult result = new PositionSearchResult();
    if ((request.getPositionIds() != null && request.getPositionIds().size() == 0) ||
        (request.getTradeIds() != null && request.getTradeIds().size() == 0) ||
        (IdentifierSearch.canMatch(request.getSecurityKeys()) == false)) {
      return result;
    }
    final Instant now = Instant.now(getTimeSource());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(request.getVersionAsOfInstant(), now))
      .addTimestamp("corrected_to_instant", Objects.firstNonNull(request.getCorrectedToInstant(), now))
      .addValueNullIgnored("min_quantity", request.getMinQuantity())
      .addValueNullIgnored("max_quantity", request.getMaxQuantity());
    if (request.getSecurityKeys() != null) {
      int i = 0;
      for (Identifier id : request.getSecurityKeys()) {
        args.addValue("key_scheme" + i, id.getScheme().getName());
        args.addValue("key_value" + i, id.getValue());
        i++;
      }
    }
    if (request.getProviderKey() != null) {
      args.addValue("provider_scheme", request.getProviderKey().getScheme().getName());
      args.addValue("provider_value", request.getProviderKey().getValue());
    }
    searchWithPaging(request.getPagingRequest(), sqlSearchPositions(request), args, new PositionDocumentExtractor(), result);
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
    if (request.getProviderKey() != null) {
      where += "AND provider_scheme = :provider_scheme AND provider_value = :provider_value ";
    }
    if (request.getMinQuantity() != null) {
      where += "AND quantity >= :min_quantity ";
    }
    if (request.getMaxQuantity() != null) {
      where += "AND quantity < :max_quantity ";
    }
    if (request.getPositionIds() != null) {
      StringBuilder buf = new StringBuilder(request.getPositionIds().size() * 10);
      for (UniqueIdentifier uid : request.getPositionIds()) {
        getMaster().checkScheme(uid);
        buf.append(extractOid(uid)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      where += "AND oid IN (" + buf + ") ";
    }
    if (request.getTradeIds() != null) {
      StringBuilder buf = new StringBuilder(request.getTradeIds().size() * 10);
      for (UniqueIdentifier uid : request.getTradeIds()) {
        getMaster().checkScheme(uid);
        buf.append(extractOid(uid)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      where += "AND oid IN (SELECT DISTINCT position_oid FROM pos_trade WHERE oid IN (" + buf + ")) ";
    }
    if (request.getSecurityKeys() != null && request.getSecurityKeys().size() > 0) {
      where += sqlSelectMatchingSecurityKeys(request.getSecurityKeys());
    }
    
    String selectFromWhereInner = "SELECT id FROM pos_position " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY oid ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE p.id IN (" + inner + ") " + ORDER_BY;
    String count = "SELECT COUNT(*) FROM pos_position " + where;
    return new String[] {search, count};
  }

  /**
   * Gets the SQL to find all the ids for all bundles in the set.
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingSecurityKeys(final IdentifierSearch idSearch) {
    switch (idSearch.getSearchType()) {
      case EXACT:
        return "AND id IN (" + sqlSelectMatchingSecurityKeysExact(idSearch) + ") ";
      case ALL:
        return "AND id IN (" + sqlSelectMatchingSecurityKeysAll(idSearch) + ") ";
      case ANY:
        return "AND id IN (" + sqlSelectMatchingSecurityKeysAny(idSearch) + ") ";
      case NONE:
        return "AND id NOT IN (" + sqlSelectMatchingSecurityKeysAny(idSearch) + ") ";
    }
    throw new UnsupportedOperationException("Search type is not supported: " + idSearch.getSearchType());
  }

  /**
   * Gets the SQL to find all the securities matching.
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingSecurityKeysExact(final IdentifierSearch idSearch) {
    // compare size of all matched to size in total
    // filter by dates to reduce search set
    String a = "SELECT position_id AS matched_position_id, COUNT(position_id) AS matched_count " +
      "FROM pos_position2idkey, pos_position main " +
      "WHERE position_id = main.id " +
      "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
      "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
      "AND idkey_id IN (" + sqlSelectMatchingSecurityKeysOr(idSearch) + ") " +
      "GROUP BY position_id " +
      "HAVING COUNT(position_id) >= " + idSearch.size() + " ";
    String b = "SELECT position_id AS total_position_id, COUNT(position_id) AS total_count " +
      "FROM pos_position2idkey, pos_position main " +
      "WHERE position_id = main.id " +
      "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
      "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
      "GROUP BY position_id ";
    String select = "SELECT matched_position_id AS position_id " +
      "FROM (" + a + ") AS a, (" + b + ") AS b " +
      "WHERE matched_position_id = total_position_id " +
        "AND matched_count = total_count ";
    return select;
  }

  /**
   * Gets the SQL to find all the securities matching.
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingSecurityKeysAll(final IdentifierSearch idSearch) {
    // only return position_id when all requested ids match (having count >= size)
    // filter by dates to reduce search set
    String select = "SELECT position_id " +
      "FROM pos_position2idkey, pos_position main " +
      "WHERE position_id = main.id " +
      "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
      "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
      "AND idkey_id IN (" + sqlSelectMatchingSecurityKeysOr(idSearch) + ") " +
      "GROUP BY position_id " +
      "HAVING COUNT(position_id) >= " + idSearch.size() + " ";
    return select;
  }

  /**
   * Gets the SQL to find all the securities matching any identifier.
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingSecurityKeysAny(final IdentifierSearch idSearch) {
    // optimized search for commons case of individual ORs
    // filter by dates to reduce search set
    String select = "SELECT DISTINCT position_id " +
      "FROM pos_position2idkey, pos_position main " +
      "WHERE position_id = main.id " +
      "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
      "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
      "AND idkey_id IN (" + sqlSelectMatchingSecurityKeysOr(idSearch) + ") ";
    return select;
  }

  /**
   * Gets the SQL to find all the ids for a single bundle.
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingSecurityKeysOr(final IdentifierSearch idSearch) {
    String select = "SELECT id FROM pos_idkey ";
    for (int i = 0; i < idSearch.size(); i++) {
      select += (i == 0 ? "WHERE " : "OR ");
      select += "(key_scheme = :key_scheme" + i + " AND key_value = :key_value" + i + ") ";
    }
    return select;
  }

  //-------------------------------------------------------------------------
  @Override
  protected PositionHistoryResult history(final PositionHistoryRequest request) {
    s_logger.debug("searchPositionHistoric: {}", request);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("position_oid", extractOid(request.getObjectId()))
      .addTimestampNullIgnored("versions_from_instant", request.getVersionsFromInstant())
      .addTimestampNullIgnored("versions_to_instant", request.getVersionsToInstant())
      .addTimestampNullIgnored("corrections_from_instant", request.getCorrectionsFromInstant())
      .addTimestampNullIgnored("corrections_to_instant", request.getCorrectionsToInstant());
    final PositionHistoryResult result = new PositionHistoryResult();
    searchWithPaging(request.getPagingRequest(), sqlSearchPositionHistoric(request), args, new PositionDocumentExtractor(), result);
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
    String search = SELECT + FROM + "WHERE p.id IN (" + inner + ") ORDER BY p.ver_from_instant DESC, p.corr_from_instant DESC, t.trade_date, t.id";
    String count = "SELECT COUNT(*) FROM pos_position " + where;
    return new String[] {search, count};
  }

  /**
   * Searches for documents with paging.
   * 
   * @param pagingRequest  the paging request, not null
   * @param sql  the array of SQL, query and count, not null
   * @param args  the query arguments, not null
   * @param extractor  the extractor of results, not null
   * @param result  the object to populate, not null
   */
  protected void searchWithPaging(
      final PagingRequest pagingRequest, final String[] sql, final DbMapSqlParameterSource args,
      final ResultSetExtractor<List<PositionDocument>> extractor, final AbstractDocumentsResult<PositionDocument> result) {
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    if (pagingRequest.equals(PagingRequest.ALL)) {
      result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
      result.setPaging(Paging.of(result.getDocuments(), pagingRequest));
    } else {
      final int count = namedJdbc.queryForInt(sql[1], args);
      result.setPaging(new Paging(pagingRequest, count));
      if (count > 0) {
        result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a PositionDocument.
   */
  protected final class PositionDocumentExtractor implements ResultSetExtractor<List<PositionDocument>> {
    private long _lastPositionId = -1;
    private long _lastTradeId = -1;
    private ManageablePosition _position;
    private List<PositionDocument> _documents = new ArrayList<PositionDocument>();

    @Override
    public List<PositionDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      ManageableTrade currentTrade = null;
      while (rs.next()) {
        
        final long positionId = rs.getLong("POSITION_ID");
        if (_lastPositionId != positionId) {
          _lastPositionId = positionId;
          buildPosition(rs, positionId);
        }
        
        final String posIdScheme = rs.getString("POS_KEY_SCHEME");
        final String posIdValue = rs.getString("POS_KEY_VALUE");
        if (posIdScheme != null && posIdValue != null) {
          Identifier id = Identifier.of(posIdScheme, posIdValue);
          _position.setSecurityKey(_position.getSecurityKey().withIdentifier(id));
        }
        
        final long tradeId = rs.getLong("TRADE_ID");
        if (_lastTradeId != tradeId && tradeId != 0) {
          _lastTradeId = tradeId;
          final BigDecimal tradeQuantity = extractBigDecimal(rs, "TRADE_QUANTITY");
          LocalDate tradeDate = DbDateUtils.fromSqlDate(rs.getDate("TRADE_DATE"));
          LocalTime tradeTime = rs.getTimestamp("TRADE_TIME") != null ? DbDateUtils.fromSqlTime(rs.getTimestamp("TRADE_TIME")) : null;
          int zoneOffset = rs.getInt("ZONE_OFFSET");
          OffsetTime tradeOffsetTime = null;
          if (tradeTime != null) {
            tradeOffsetTime = OffsetTime.of(tradeTime, ZoneOffset.ofTotalSeconds(zoneOffset));
          }
          final String cpartyScheme = rs.getString("CPARTY_SCHEME");
          final String cpartyValue = rs.getString("CPARTY_VALUE");
          Identifier counterpartyId = null;
          if (cpartyScheme != null && cpartyValue != null) {
            counterpartyId = Identifier.of(cpartyScheme, cpartyValue);
          }
          currentTrade = new ManageableTrade(tradeQuantity, IdentifierBundle.EMPTY, tradeDate, tradeOffsetTime, counterpartyId);
          long tradeOid = rs.getLong("TRADE_OID");
          currentTrade.setUniqueId(createUniqueIdentifier(tradeOid, tradeId));
          currentTrade.setPositionId(_position.getUniqueId());
          _position.getTrades().add(currentTrade);
        }
        
        final String tradeIdScheme = rs.getString("TRADE_KEY_SCHEME");
        final String tradeIdValue = rs.getString("TRADE_KEY_VALUE");
        if (tradeIdScheme != null && tradeIdValue != null) {
          Identifier id = Identifier.of(tradeIdScheme, tradeIdValue);
          currentTrade.setSecurityKey(currentTrade.getSecurityKey().withIdentifier(id));
        }

      }
      return _documents;
    }

    private void buildPosition(final ResultSet rs, final long positionId) throws SQLException {
      final long positionOid = rs.getLong("POSITION_OID");
      final BigDecimal quantity = extractBigDecimal(rs, "POS_QUANTITY");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      final String providerScheme = rs.getString("PROVIDER_SCHEME");
      final String providerValue = rs.getString("PROVIDER_VALUE");
      _position = new ManageablePosition(quantity, IdentifierBundle.EMPTY);
      _position.setUniqueId(createUniqueIdentifier(positionOid, positionId));
      PositionDocument doc = new PositionDocument(_position);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setUniqueId(createUniqueIdentifier(positionOid, positionId));
      if (providerScheme != null && providerValue != null) {
        doc.setProviderKey(Identifier.of(providerScheme, providerValue));
      }
      _documents.add(doc);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableTrade getTrade(final UniqueIdentifier uid) {
    if (uid.isVersioned()) {
      return getTradeById(uid);
    } else {
      return getTradeByInstants(uid, null, null);
    }
  }

  /**
   * Gets a trade by searching for the latest version of an object identifier.
   * @param uid  the unique identifier
   * @param versionAsOf  the instant to fetch, not null
   * @param correctedTo  the instant to fetch, not null
   * @return the trade, null if not found
   */
  protected ManageableTrade getTradeByInstants(final UniqueIdentifier uid, final Instant versionAsOf, final Instant correctedTo) {
    s_logger.debug("getPositionByLatest: {}", uid);
    final Instant now = Instant.now(getTimeSource());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("trade_oid", extractOid(uid))
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(versionAsOf, now))
      .addTimestamp("corrected_to_instant", Objects.firstNonNull(correctedTo, now));
    final PositionDocumentExtractor extractor = new PositionDocumentExtractor();
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<PositionDocument> docs = namedJdbc.query(sqlGetTradeByInstants(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Trade not found: " + uid);
    }
    return docs.get(0).getPosition().getTrades().get(0);  // SQL loads desired trade as only trade
  }

  /**
   * Gets the SQL for getting a trade by unique row identifier.
   * @return the SQL, not null
   */
  protected String sqlGetTradeByInstants() {
    return SELECT + FROM + "WHERE t.oid = :trade_oid " +
        "AND (ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant) " +
        "AND (corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant) ";
  }

  /**
   * Gets a trade by identifier.
   * @param uid  the unique identifier
   * @return the trade, null if not found
   */
  protected ManageableTrade getTradeById(final UniqueIdentifier uid) {
    s_logger.debug("getTradeById {}", uid);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("trade_id", extractRowId(uid));
    final PositionDocumentExtractor extractor = new PositionDocumentExtractor();
    NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<PositionDocument> docs = namedJdbc.query(sqlGetTradeById(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Trade not found: " + uid);
    }
    return docs.get(0).getPosition().getTrades().get(0);  // SQL loads desired trade as only trade
  }

  /**
   * Gets the SQL for getting a trade by unique row identifier.
   * @return the SQL, not null
   */
  protected String sqlGetTradeById() {
    return SELECT + FROM + "WHERE t.id = :trade_id ";
  }

}
