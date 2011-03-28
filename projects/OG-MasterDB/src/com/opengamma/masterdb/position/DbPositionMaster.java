/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import java.util.Set;

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
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifiables;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.tuple.Pair;

/**
 * A position master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the position master using an SQL database.
 * Full details of the API are in {@link PositionMaster}.
 * <p>
 * This class uses SQL via JDBC. The SQL may be changed by subclassing the relevant methods.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbPositionMaster extends AbstractDocumentDbMaster<PositionDocument> implements PositionMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbPositionMaster.class);

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbPos";
  /**
   * SQL select.
   */
  protected static final String SELECT =
      "SELECT " +
        "main.id AS position_id, " +
        "main.oid AS position_oid, " +
        "main.ver_from_instant AS ver_from_instant, " +
        "main.ver_to_instant AS ver_to_instant, " +
        "main.corr_from_instant AS corr_from_instant, " +
        "main.corr_to_instant AS corr_to_instant, " +
        "main.provider_scheme AS pos_provider_scheme, " +
        "main.provider_value AS pos_provider_value, " +
        "main.quantity AS pos_quantity, " +
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
        "t.provider_scheme AS trade_provider_scheme, " +
        "t.provider_value AS trade_provider_value, " +
        "ts.key_scheme AS trade_key_scheme, " +
        "ts.key_value AS trade_key_value ";
  /**
   * SQL from.
   */
  protected static final String FROM =
      "FROM pos_position main " +
      "LEFT JOIN pos_position2idkey pi ON (pi.position_id = main.id) " +
      "LEFT JOIN pos_idkey ps ON (ps.id = pi.idkey_id) " +
      "LEFT JOIN pos_trade t ON (t.position_id = main.id) " +
      "LEFT JOIN pos_trade2idkey ti ON (ti.trade_id = t.id) " +
      "LEFT JOIN pos_idkey ts ON (ts.id = ti.idkey_id) ";

  /**
   * Creates an instance.
   * 
   * @param dbSource  the database source combining all configuration, not null
   */
  public DbPositionMaster(final DbSource dbSource) {
    super(dbSource, IDENTIFIER_SCHEME_DEFAULT);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchResult search(final PositionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final PositionSearchResult result = new PositionSearchResult();
    if ((request.getPositionIds() != null && request.getPositionIds().size() == 0) ||
        (request.getTradeIds() != null && request.getTradeIds().size() == 0) ||
        (IdentifierSearch.canMatch(request.getSecurityKeys()) == false)) {
      return result;
    }
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(Instant.now(getTimeSource()));
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
      .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
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
    if (request.getPositionProviderKey() != null) {
      args.addValue("pos_provider_scheme", request.getPositionProviderKey().getScheme().getName());
      args.addValue("pos_provider_value", request.getPositionProviderKey().getValue());
    }
    if (request.getTradeProviderKey() != null) {
      args.addValue("trade_provider_scheme", request.getTradeProviderKey().getScheme().getName());
      args.addValue("trade_provider_value", request.getTradeProviderKey().getValue());
    }
    searchWithPaging(request.getPagingRequest(), sqlSearchPositions(request), args, new PositionDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL to search for positions.
   * 
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchPositions(final PositionSearchRequest request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
                "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
    if (request.getPositionProviderKey() != null) {
      where += "AND provider_scheme = :pos_provider_scheme AND provider_value = :pos_provider_value ";
    }
    if (request.getMinQuantity() != null) {
      where += "AND quantity >= :min_quantity ";
    }
    if (request.getMaxQuantity() != null) {
      where += "AND quantity <= :max_quantity ";
    }
    if (request.getPositionIds() != null) {
      StringBuilder buf = new StringBuilder(request.getPositionIds().size() * 10);
      for (ObjectIdentifier obectId : request.getPositionIds()) {
        checkScheme(obectId);
        buf.append(extractOid(obectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      where += "AND oid IN (" + buf + ") ";
    }
    if (request.getTradeIds() != null) {
      StringBuilder buf = new StringBuilder(request.getTradeIds().size() * 10);
      for (ObjectIdentifier obejctId : request.getTradeIds()) {
        checkScheme(obejctId);
        buf.append(extractOid(obejctId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      where += "AND oid IN (SELECT DISTINCT position_oid FROM pos_trade WHERE oid IN (" + buf + ")) ";
    }
    if (request.getTradeProviderKey() != null) {
      where += "AND oid IN (SELECT DISTINCT position_oid FROM pos_trade " +
          "WHERE provider_scheme = :trade_provider_scheme AND provider_value = :trade_provider_value) ";
    }
    if (request.getSecurityKeys() != null && request.getSecurityKeys().size() > 0) {
      where += sqlSelectMatchingSecurityKeys(request.getSecurityKeys());
    }
    where += sqlAdditionalWhere();
    
    String selectFromWhereInner = "SELECT id FROM pos_position " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY oid ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE main.id IN (" + inner + ") ORDER BY main.oid " + sqlAdditionalOrderBy(false);
    String count = "SELECT COUNT(*) FROM pos_position " + where;
    return new String[] {search, count};
  }

  /**
   * Gets the SQL to match the {@code IdentifierSearch}.
   * 
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
   * 
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
   * 
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
   * 
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
   * Gets the SQL to find all the ids for a single set of idenifiers.
   * 
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
  public PositionDocument get(final UniqueIdentifier uniqueId) {
    return doGet(uniqueId, new PositionDocumentExtractor(), "Position");
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new PositionDocumentExtractor(), "Position");
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionHistoryResult history(final PositionHistoryRequest request) {
    return doHistory(request, new PositionHistoryResult(), new PositionDocumentExtractor());
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a new document.
   * 
   * @param document  the document, not null
   * @return the new document, not null
   */
  @Override
  protected PositionDocument insert(final PositionDocument document) {
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    
    final long positionId = nextId("pos_master_seq");
    final long positionOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : positionId);
    final UniqueIdentifier positionUid = createUniqueIdentifier(positionOid, positionId);
    final ManageablePosition position = document.getPosition();
    
    // the arguments for inserting into the position table
    final DbMapSqlParameterSource positionArgs = new DbMapSqlParameterSource()
      .addValue("position_id", positionId)
      .addValue("position_oid", positionOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("quantity", position.getQuantity())
      .addValue("provider_scheme", (position.getProviderKey() != null ? position.getProviderKey().getScheme().getName() : null))
      .addValue("provider_value", (position.getProviderKey() != null ? position.getProviderKey().getValue() : null));
    
    // the arguments for inserting into the idkey tables
    final List<DbMapSqlParameterSource> posAssocList = new ArrayList<DbMapSqlParameterSource>();
    final Set<Pair<String, String>> schemeValueSet = Sets.newHashSet();
    for (Identifier id : position.getSecurityKey()) {
      final DbMapSqlParameterSource assocArgs = new DbMapSqlParameterSource()
        .addValue("position_id", positionId)
        .addValue("key_scheme", id.getScheme().getName())
        .addValue("key_value", id.getValue());
      posAssocList.add(assocArgs);
      schemeValueSet.add(Pair.of(id.getScheme().getName(), id.getValue()));
    }
    
    // the arguments for inserting into the trade table
    final List<DbMapSqlParameterSource> tradeList = new ArrayList<DbMapSqlParameterSource>();
    final List<DbMapSqlParameterSource> tradeAssocList = new ArrayList<DbMapSqlParameterSource>();
    for (ManageableTrade trade : position.getTrades()) {
      final long tradeId = nextId("pos_master_seq");
      final long tradeOid = (trade.getUniqueId() != null ? extractOid(trade.getUniqueId()) : tradeId);
      final Identifier counterpartyId = trade.getCounterpartyKey();
      final DbMapSqlParameterSource tradeArgs = new DbMapSqlParameterSource()
        .addValue("trade_id", tradeId)
        .addValue("trade_oid", tradeOid)
        .addValue("position_id", positionId)
        .addValue("position_oid", positionOid)
        .addValue("quantity", trade.getQuantity())
        .addDate("trade_date", trade.getTradeDate())
        .addTimeNullIgnored("trade_time", trade.getTradeTime() != null ? trade.getTradeTime().toLocalTime() : null)
        .addValue("zone_offset", trade.getTradeTime() != null ? trade.getTradeTime().getOffset().getAmountSeconds() : null)
        .addValue("cparty_scheme", counterpartyId.getScheme().getName())
        .addValue("cparty_value", counterpartyId.getValue())
        .addValue("provider_scheme", (position.getProviderKey() != null ? position.getProviderKey().getScheme().getName() : null))
        .addValue("provider_value", (position.getProviderKey() != null ? position.getProviderKey().getValue() : null));
      tradeList.add(tradeArgs);
      
      // set the trade uniqueId
      final UniqueIdentifier tradeUid = createUniqueIdentifier(tradeOid, tradeId);
      UniqueIdentifiables.setInto(trade, tradeUid);
      trade.setPositionId(positionUid);
      for (Identifier id : trade.getSecurityKey()) {
        final DbMapSqlParameterSource assocArgs = new DbMapSqlParameterSource()
          .addValue("trade_id", tradeId)
          .addValue("key_scheme", id.getScheme().getName())
          .addValue("key_value", id.getValue());
        tradeAssocList.add(assocArgs);
        schemeValueSet.add(Pair.of(id.getScheme().getName(), id.getValue()));
      }
    }
    
    final List<DbMapSqlParameterSource> idKeyList = new ArrayList<DbMapSqlParameterSource>();
    for (Pair<String, String> pair : schemeValueSet) {
      final DbMapSqlParameterSource idkeyArgs = new DbMapSqlParameterSource()
        .addValue("key_scheme", pair.getFirst())
        .addValue("key_value", pair.getSecond());
      if (getJdbcTemplate().queryForList(sqlSelectIdKey(), idkeyArgs).isEmpty()) {
        // select avoids creating unecessary id, but id may still not be used
        final long idKeyId = nextId("pos_idkey_seq");
        idkeyArgs.addValue("idkey_id", idKeyId);
        idKeyList.add(idkeyArgs);
      }
    }
    
    getJdbcTemplate().update(sqlInsertPosition(), positionArgs);
    getJdbcTemplate().batchUpdate(sqlInsertIdKey(), idKeyList.toArray(new DbMapSqlParameterSource[idKeyList.size()]));
    getJdbcTemplate().batchUpdate(sqlInsertPositionIdKey(), posAssocList.toArray(new DbMapSqlParameterSource[posAssocList.size()]));
    getJdbcTemplate().batchUpdate(sqlInsertTrades(), tradeList.toArray(new DbMapSqlParameterSource[tradeList.size()]));
    getJdbcTemplate().batchUpdate(sqlInsertTradeIdKey(), tradeAssocList.toArray(new DbMapSqlParameterSource[tradeAssocList.size()]));
    
    // set the uniqueId
    position.setUniqueId(positionUid);
    document.setUniqueId(positionUid);
    return document;
  }

  /**
   * Gets the SQL for inserting a position.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertPosition() {
    return "INSERT INTO pos_position " +
              "(id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, provider_scheme, provider_value, quantity) " +
            "VALUES " +
              "(:position_id, :position_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :provider_scheme, :provider_value, :quantity)";
  }

  /**
   * Gets the SQL for inserting a trade.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertTrades() {
    return "INSERT INTO pos_trade " +
              "(id, oid, position_id, position_oid, quantity, trade_date, trade_time, zone_offset, cparty_scheme, cparty_value) " +
            "VALUES " +
              "(:trade_id, :trade_oid, :position_id, :position_oid, :quantity, :trade_date, :trade_time, :zone_offset, :cparty_scheme, :cparty_value)";
  }

  /**
   * Gets the SQL for inserting an idkey.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertIdKey() {
    return "INSERT INTO pos_idkey (id, key_scheme, key_value) " +
            "VALUES (:idkey_id, :key_scheme, :key_value)";
  }

  /**
   * Gets the SQL for inserting a position-idkey association.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertPositionIdKey() {
    return "INSERT INTO pos_position2idkey " +
              "(position_id, idkey_id) " +
            "VALUES " +
              "(:position_id, (" + sqlSelectIdKey() + "))";
  }

  /**
   * Gets the SQL for inserting a position-idkey association.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertTradeIdKey() {
    return "INSERT INTO pos_trade2idkey " +
              "(trade_id, idkey_id) " +
            "VALUES " +
              "(:trade_id, (" + sqlSelectIdKey() + "))";
  }

  /**
   * Gets the SQL for selecting an idkey.
   * 
   * @return the SQL, not null
   */
  protected String sqlSelectIdKey() {
    return "SELECT id FROM pos_idkey WHERE key_scheme = :key_scheme AND key_value = :key_value";
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableTrade getTrade(final UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    checkScheme(uniqueId);
    
    if (uniqueId.isVersioned()) {
      return getTradeById(uniqueId);
    } else {
      return getTradeByInstants(uniqueId, null, null);
    }
  }

  /**
   * Gets a trade by searching for the latest version of an object identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param versionAsOf  the instant to fetch, not null
   * @param correctedTo  the instant to fetch, not null
   * @return the trade, null if not found
   */
  protected ManageableTrade getTradeByInstants(final UniqueIdentifier uniqueId, final Instant versionAsOf, final Instant correctedTo) {
    s_logger.debug("getTradeByLatest {}", uniqueId);
    final Instant now = Instant.now(getTimeSource());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("trade_oid", extractOid(uniqueId))
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(versionAsOf, now))
      .addTimestamp("corrected_to_instant", Objects.firstNonNull(correctedTo, now));
    final PositionDocumentExtractor extractor = new PositionDocumentExtractor();
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<PositionDocument> docs = namedJdbc.query(sqlGetTradeByInstants(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Trade not found: " + uniqueId);
    }
    return docs.get(0).getPosition().getTrades().get(0);  // SQL loads desired trade as only trade
  }

  /**
   * Gets the SQL for getting a trade by unique row identifier.
   * 
   * @return the SQL, not null
   */
  protected String sqlGetTradeByInstants() {
    return SELECT + FROM + "WHERE t.oid = :trade_oid " +
        "AND ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
        "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant " +
      sqlAdditionalWhere() +
      sqlAdditionalOrderBy(true);
  }

  /**
   * Gets a trade by identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the trade, null if not found
   */
  protected ManageableTrade getTradeById(final UniqueIdentifier uniqueId) {
    s_logger.debug("getTradeById {}", uniqueId);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("trade_id", extractRowId(uniqueId));
    final PositionDocumentExtractor extractor = new PositionDocumentExtractor();
    NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<PositionDocument> docs = namedJdbc.query(sqlGetTradeById(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Trade not found: " + uniqueId);
    }
    return docs.get(0).getPosition().getTrades().get(0);  // SQL loads desired trade as only trade
  }

  /**
   * Gets the SQL for getting a trade by unique row identifier.
   * 
   * @return the SQL, not null
   */
  protected String sqlGetTradeById() {
    return SELECT + FROM + "WHERE t.id = :trade_id " + sqlAdditionalWhere() + sqlAdditionalOrderBy(true);
  }

  //-------------------------------------------------------------------------
  @Override
  protected String sqlSelectFrom() {
    return SELECT + FROM;
  }

  @Override
  protected String sqlAdditionalOrderBy(final boolean orderByPrefix) {
    return (orderByPrefix ? "ORDER BY " : ", ") + "t.trade_date, t.id ";
  }

  @Override
  protected String mainTableName() {
    return "pos_position";
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a PositionDocument.
   */
  protected final class PositionDocumentExtractor implements ResultSetExtractor<List<PositionDocument>> {
    private long _lastPositionId = -1;
    private long _lastTradeId = -1;
    private ManageablePosition _position;
    private ManageableTrade _trade;
    private List<PositionDocument> _documents = new ArrayList<PositionDocument>();

    @Override
    public List<PositionDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
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
          buildTrade(rs, tradeId);
        }
        
        final String tradeIdScheme = rs.getString("TRADE_KEY_SCHEME");
        final String tradeIdValue = rs.getString("TRADE_KEY_VALUE");
        if (tradeIdScheme != null && tradeIdValue != null) {
          Identifier id = Identifier.of(tradeIdScheme, tradeIdValue);
          _trade.setSecurityKey(_trade.getSecurityKey().withIdentifier(id));
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
      final String providerScheme = rs.getString("POS_PROVIDER_SCHEME");
      final String providerValue = rs.getString("POS_PROVIDER_VALUE");
      _position = new ManageablePosition(quantity, IdentifierBundle.EMPTY);
      _position.setUniqueId(createUniqueIdentifier(positionOid, positionId));
      if (providerScheme != null && providerValue != null) {
        _position.setProviderKey(Identifier.of(providerScheme, providerValue));
      }
      PositionDocument doc = new PositionDocument(_position);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setUniqueId(createUniqueIdentifier(positionOid, positionId));
      _documents.add(doc);
    }
    
    private void buildTrade(final ResultSet rs, final long tradeId) throws SQLException {
      _lastTradeId = tradeId;
      final long tradeOid = rs.getLong("TRADE_OID");
      final BigDecimal tradeQuantity = extractBigDecimal(rs, "TRADE_QUANTITY");
      LocalDate tradeDate = DbDateUtils.fromSqlDate(rs.getDate("TRADE_DATE"));
      LocalTime tradeTime = rs.getTimestamp("TRADE_TIME") != null ? DbDateUtils.fromSqlTime(rs.getTimestamp("TRADE_TIME")) : null;
      int zoneOffset = rs.getInt("ZONE_OFFSET");
      final String cpartyScheme = rs.getString("CPARTY_SCHEME");
      final String cpartyValue = rs.getString("CPARTY_VALUE");
      final String providerScheme = rs.getString("TRADE_PROVIDER_SCHEME");
      final String providerValue = rs.getString("TRADE_PROVIDER_VALUE");
      OffsetTime tradeOffsetTime = null;
      if (tradeTime != null) {
        tradeOffsetTime = OffsetTime.of(tradeTime, ZoneOffset.ofTotalSeconds(zoneOffset));
      }
      Identifier counterpartyId = null;
      if (cpartyScheme != null && cpartyValue != null) {
        counterpartyId = Identifier.of(cpartyScheme, cpartyValue);
      }
      _trade = new ManageableTrade(tradeQuantity, IdentifierBundle.EMPTY, tradeDate, tradeOffsetTime, counterpartyId);
      _trade.setUniqueId(createUniqueIdentifier(tradeOid, tradeId));
      if (providerScheme != null && providerValue != null) {
        _trade.setProviderKey(Identifier.of(providerScheme, providerValue));
      }
      _trade.setPositionId(_position.getUniqueId());
      _position.getTrades().add(_trade);
    }
  }

}
