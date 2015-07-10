/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.AbstractHistoryResult;
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
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.money.Currency;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A position master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the position master using an SQL database. Full details of the API are in {@link PositionMaster}.
 * <p>
 * The SQL is stored externally in {@code DbPositionMaster.elsql}. Alternate databases or specific SQL requirements can be handled using database specific overrides, such as
 * {@code DbPositionMaster-MySpecialDB.elsql}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbPositionMaster extends AbstractDocumentDbMaster<PositionDocument> implements PositionMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbPositionMaster.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbPos";

  // -----------------------------------------------------------------
  // TIMERS FOR METRICS GATHERING
  // By default these do nothing. Registration will replace them
  // so that they actually do something.
  // -----------------------------------------------------------------
  private Timer _insertTimer = new Timer();

  /**
   * Creates an instance.
   * 
   * @param dbConnector the database connector, not null
   */
  public DbPositionMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbPositionMaster.class));
  }

  @Override
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailedRegistry, String namePrefix) {
    super.registerMetrics(summaryRegistry, detailedRegistry, namePrefix);
    _insertTimer = summaryRegistry.timer(namePrefix + ".insert");
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchResult search(final PositionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);

    VersionCorrection vc = request.getVersionCorrection();
    if (vc.containsLatest()) {
      vc = vc.withLatestFixed(now());
    }
    final PositionSearchResult result = new PositionSearchResult(vc);

    final ExternalIdSearch securityIdSearch = request.getSecurityIdSearch();
    final Collection<ObjectId> positionObjectIds = request.getPositionObjectIds();
    final Collection<ObjectId> tradeObjectIds = request.getTradeObjectIds();
    if ((positionObjectIds != null && positionObjectIds.size() == 0) ||
        (tradeObjectIds != null && tradeObjectIds.size() == 0) ||
        (ExternalIdSearch.canMatch(securityIdSearch) == false)) {
      result.setPaging(Paging.of(request.getPagingRequest(), 0));
      return result;
    }

    final DbMapSqlParameterSource args = createParameterSource().addTimestamp("version_as_of_instant", vc.getVersionAsOf()).addTimestamp("corrected_to_instant", vc.getCorrectedTo())
        .addValueNullIgnored("min_quantity", request.getMinQuantity()).addValueNullIgnored("max_quantity", request.getMaxQuantity())
        .addValueNullIgnored("security_id_value", getDialect().sqlWildcardAdjustValue(request.getSecurityIdValue()));
    if (request.getPositionProviderId() != null) {
      args.addValue("pos_provider_scheme", request.getPositionProviderId().getScheme().getName());
      args.addValue("pos_provider_value", request.getPositionProviderId().getValue());
    }
    if (request.getTradeProviderId() != null) {
      args.addValue("trade_provider_scheme", request.getTradeProviderId().getScheme().getName());
      args.addValue("trade_provider_value", request.getTradeProviderId().getValue());
    }
    if (securityIdSearch != null && securityIdSearch.alwaysMatches() == false) {
      int i = 0;
      for (final ExternalId id : securityIdSearch) {
        args.addValue("key_scheme" + i, id.getScheme().getName());
        args.addValue("key_value" + i, id.getValue());
        i++;
      }
      args.addValue("sql_search_security_ids_type", securityIdSearch.getSearchType());
      args.addValue("sql_search_security_ids", sqlSelectIdKeys(securityIdSearch));
      args.addValue("security_id_search_size", securityIdSearch.getExternalIds().size());
    }
    if (positionObjectIds != null) {
      final StringBuilder buf = new StringBuilder(positionObjectIds.size() * 10);
      for (final ObjectId objectId : positionObjectIds) {
        checkScheme(objectId);
        buf.append(extractOid(objectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      args.addValue("sql_search_position_ids", buf.toString());
    }
    if (tradeObjectIds != null) {
      final StringBuilder buf = new StringBuilder(tradeObjectIds.size() * 10);
      for (final ObjectId objectId : tradeObjectIds) {
        checkScheme(objectId);
        buf.append(extractOid(objectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      args.addValue("sql_search_trade_ids", buf.toString());
    }
    args.addValue("paging_offset", request.getPagingRequest().getFirstItem());
    args.addValue("paging_fetch", request.getPagingRequest().getPagingSize());

    final String[] sql = {getElSqlBundle().getSql("Search", args), getElSqlBundle().getSql("SearchCount", args) };
    doSearch(request.getPagingRequest(), sql, args, new PositionDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL to find all the ids for a single bundle.
   * <p>
   * This is too complex for the elsql mechanism.
   * 
   * @param idSearch the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectIdKeys(final ExternalIdSearch idSearch) {
    final List<String> list = new ArrayList<String>();
    for (int i = 0; i < idSearch.size(); i++) {
      list.add("(key_scheme = :key_scheme" + i + " AND key_value = :key_value" + i + ") ");
    }
    return StringUtils.join(list, "OR ");
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionDocument get(final UniqueId uniqueId) {
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
   * @param document the document, not null
   * @return the new document, not null
   */
  @Override
  protected PositionDocument insert(final PositionDocument document) {
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getPosition().getQuantity(), "document.position.quantity");
    for (final ManageableTrade trade : document.getPosition().getTrades()) {
      ArgumentChecker.notNull(trade.getQuantity(), "position.trade.quantity");
      ArgumentChecker.notNull(trade.getCounterpartyExternalId(), "position.trade.counterpartyexternalid");
      ArgumentChecker.notNull(trade.getTradeDate(), "position.trade.tradedate");
    }

    try (Timer.Context context = _insertTimer.time()) {
      final long positionId = nextId("pos_master_seq");
      final long positionOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : positionId);
      final UniqueId positionUid = createUniqueId(positionOid, positionId);
      final ManageablePosition position = document.getPosition();

      // the arguments for inserting into the position table
      final DbMapSqlParameterSource docArgs = createParameterSource().addValue("position_id", positionId).addValue("position_oid", positionOid)
          .addTimestamp("ver_from_instant", document.getVersionFromInstant()).addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
          .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
          .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
          .addValue("quantity", position.getQuantity(), Types.DECIMAL)
          .addValue("provider_scheme",
              position.getProviderId() != null ? position.getProviderId().getScheme().getName() : null, Types.VARCHAR)
          .addValue("provider_value",
              position.getProviderId() != null ? position.getProviderId().getValue() : null, Types.VARCHAR);

      // the arguments for inserting into the pos_attribute table
      final List<DbMapSqlParameterSource> posAttrList = Lists.newArrayList();
      for (final Entry<String, String> entry : position.getAttributes().entrySet()) {
        final long posAttrId = nextId("pos_trade_attr_seq");
        final DbMapSqlParameterSource posAttrArgs = createParameterSource().addValue("attr_id", posAttrId)
            .addValue("pos_id", positionId)
            .addValue("pos_oid", positionOid)
            .addValue("key", entry.getKey())
            .addValue("value", entry.getValue());
        posAttrList.add(posAttrArgs);
      }

      // the arguments for inserting into the idkey tables
      final List<DbMapSqlParameterSource> posAssocList = new ArrayList<DbMapSqlParameterSource>();
      final Set<Pair<String, String>> schemeValueSet = Sets.newHashSet();
      for (final ExternalId id : position.getSecurityLink().getAllExternalIds()) {
        final DbMapSqlParameterSource assocArgs = createParameterSource().addValue("position_id", positionId)
            .addValue("key_scheme", id.getScheme().getName())
            .addValue("key_value", id.getValue());
        posAssocList.add(assocArgs);
        schemeValueSet.add(Pairs.of(id.getScheme().getName(), id.getValue()));
      }

      // the arguments for inserting into the trade table
      final List<DbMapSqlParameterSource> tradeList = Lists.newArrayList();
      final List<DbMapSqlParameterSource> tradeAssocList = Lists.newArrayList();
      final List<DbMapSqlParameterSource> tradeAttributeList = Lists.newArrayList();
      for (final ManageableTrade trade : position.getTrades()) {
        final long tradeId = nextId("pos_master_seq");
        final long tradeOid = (trade.getUniqueId() != null ? extractOid(trade.getUniqueId()) : tradeId);
        final ExternalId counterpartyId = trade.getCounterpartyExternalId();

        final DbMapSqlParameterSource tradeArgs = createParameterSource().addValue("trade_id", tradeId)
            .addValue("trade_oid", tradeOid)
            .addValue("position_id", positionId)
            .addValue("position_oid", positionOid)
            .addValue("quantity", trade.getQuantity())
            .addDate("trade_date", trade.getTradeDate())
            .addTimeAllowNull("trade_time", trade.getTradeTime() != null ? trade.getTradeTime().toLocalTime() : null)
            .addValue("zone_offset",
                trade.getTradeTime() != null ? trade.getTradeTime().getOffset().getTotalSeconds() : null, Types.INTEGER)
            .addValue("cparty_scheme", counterpartyId.getScheme().getName())
            .addValue("cparty_value", counterpartyId.getValue())
            .addValue("provider_scheme",
                trade.getProviderId() != null ? trade.getProviderId().getScheme().getName() : null, Types.VARCHAR)
            .addValue("provider_value",
                trade.getProviderId() != null ? trade.getProviderId().getValue() : null, Types.VARCHAR)
            .addValue("premium_value", trade.getPremium(), Types.DOUBLE)
            .addValue("premium_currency",
                trade.getPremiumCurrency() != null ? trade.getPremiumCurrency().getCode() : null, Types.VARCHAR)
            .addDateAllowNull("premium_date", trade.getPremiumDate())
            .addTimeAllowNull("premium_time", (trade.getPremiumTime() != null ? trade.getPremiumTime().toLocalTime() : null))
            .addValue("premium_zone_offset",
                trade.getPremiumTime() != null ? trade.getPremiumTime().getOffset().getTotalSeconds() : null, Types.INTEGER);
        tradeList.add(tradeArgs);

        // trade attributes
        final Map<String, String> attributes = new HashMap<String, String>(trade.getAttributes());
        for (final Entry<String, String> entry : attributes.entrySet()) {
          final long tradeAttrId = nextId("pos_trade_attr_seq");
          final DbMapSqlParameterSource tradeAttributeArgs = createParameterSource().addValue("attr_id", tradeAttrId)
              .addValue("trade_id", tradeId)
              .addValue("trade_oid", tradeOid)
              .addValue("key", entry.getKey())
              .addValue("value", entry.getValue());
          tradeAttributeList.add(tradeAttributeArgs);
        }

        // set the trade uniqueId
        final UniqueId tradeUid = createUniqueId(tradeOid, tradeId);
        IdUtils.setInto(trade, tradeUid);
        trade.setParentPositionId(positionUid);
        for (final ExternalId id : trade.getSecurityLink().getAllExternalIds()) {
          final DbMapSqlParameterSource assocArgs = createParameterSource().addValue("trade_id", tradeId)
              .addValue("key_scheme", id.getScheme().getName())
              .addValue("key_value", id.getValue());
          tradeAssocList.add(assocArgs);
          schemeValueSet.add(Pairs.of(id.getScheme().getName(), id.getValue()));
        }
      }

      final List<DbMapSqlParameterSource> idKeyList = new ArrayList<DbMapSqlParameterSource>();
      final String sqlSelectIdKey = getElSqlBundle().getSql("SelectIdKey");
      for (final Pair<String, String> pair : schemeValueSet) {
        final DbMapSqlParameterSource idkeyArgs = createParameterSource().addValue("key_scheme", pair.getFirst())
            .addValue("key_value", pair.getSecond());
        if (getJdbcTemplate().queryForList(sqlSelectIdKey, idkeyArgs).isEmpty()) {
          // select avoids creating unecessary id, but id may still not be used
          final long idKeyId = nextId("pos_idkey_seq");
          idkeyArgs.addValue("idkey_id", idKeyId);
          idKeyList.add(idkeyArgs);
        }
      }

      final String sqlDoc = getElSqlBundle().getSql("Insert", docArgs);
      final String sqlIdKey = getElSqlBundle().getSql("InsertIdKey");
      final String sqlPosition2IdKey = getElSqlBundle().getSql("InsertPosition2IdKey");
      final String sqlTrade = getElSqlBundle().getSql("InsertTrade");
      final String sqlTrade2IdKey = getElSqlBundle().getSql("InsertTrade2IdKey");
      final String sqlPositionAttributes = getElSqlBundle().getSql("InsertPositionAttributes");
      final String sqlTradeAttributes = getElSqlBundle().getSql("InsertTradeAttributes");
      getJdbcTemplate().update(sqlDoc, docArgs);
      getJdbcTemplate().batchUpdate(sqlIdKey, idKeyList.toArray(new DbMapSqlParameterSource[idKeyList.size()]));
      getJdbcTemplate().batchUpdate(sqlPosition2IdKey, posAssocList.toArray(new DbMapSqlParameterSource[posAssocList.size()]));
      getJdbcTemplate().batchUpdate(sqlTrade, tradeList.toArray(new DbMapSqlParameterSource[tradeList.size()]));
      getJdbcTemplate().batchUpdate(sqlTrade2IdKey, tradeAssocList.toArray(new DbMapSqlParameterSource[tradeAssocList.size()]));
      getJdbcTemplate().batchUpdate(sqlPositionAttributes, posAttrList.toArray(new DbMapSqlParameterSource[posAttrList.size()]));
      getJdbcTemplate().batchUpdate(sqlTradeAttributes, tradeAttributeList.toArray(new DbMapSqlParameterSource[tradeAttributeList.size()]));

      // set the uniqueId
      position.setUniqueId(positionUid);
      document.setUniqueId(positionUid);
      return document;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableTrade getTrade(final UniqueId uniqueId) {
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
   * @param uniqueId the unique identifier, not null
   * @param versionAsOf the instant to fetch, not null
   * @param correctedTo the instant to fetch, not null
   * @return the trade, null if not found
   */
  protected ManageableTrade getTradeByInstants(final UniqueId uniqueId, final Instant versionAsOf, final Instant correctedTo) {
    s_logger.debug("getTradeByLatest {}", uniqueId);
    final Instant now = now();
    final DbMapSqlParameterSource args = createParameterSource().addValue("trade_oid", extractOid(uniqueId))
        .addTimestamp("version_as_of_instant", Objects.firstNonNull(versionAsOf, now))
        .addTimestamp("corrected_to_instant", Objects.firstNonNull(correctedTo, now));
    final PositionDocumentExtractor extractor = new PositionDocumentExtractor();
    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate();
    final String sql = getElSqlBundle().getSql("GetTradeByOidInstants", args);
    final List<PositionDocument> docs = namedJdbc.query(sql, args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Trade not found: " + uniqueId);
    }
    return docs.get(0).getPosition().getTrades().get(0); // SQL loads desired trade as only trade
  }

  /**
   * Gets a trade by identifier.
   * 
   * @param uniqueId the unique identifier, not null
   * @return the trade, null if not found
   */
  protected ManageableTrade getTradeById(final UniqueId uniqueId) {
    s_logger.debug("getTradeById {}", uniqueId);
    final DbMapSqlParameterSource args = createParameterSource().addValue("trade_id", extractRowId(uniqueId));
    final PositionDocumentExtractor extractor = new PositionDocumentExtractor();
    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate();
    final String sql = getElSqlBundle().getSql("GetTradeById", args);
    final List<PositionDocument> docs = namedJdbc.query(sql, args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Trade not found: " + uniqueId);
    }
    return docs.get(0).getPosition().getTrades().get(0); // SQL loads desired trade as only trade
  }

  //-------------------------------------------------------------------------
  @Override
  protected AbstractHistoryResult<PositionDocument> historyByVersionsCorrections(final AbstractHistoryRequest request) {
    final PositionHistoryRequest historyRequest = new PositionHistoryRequest();
    historyRequest.setCorrectionsFromInstant(request.getCorrectionsFromInstant());
    historyRequest.setCorrectionsToInstant(request.getCorrectionsToInstant());
    historyRequest.setVersionsFromInstant(request.getVersionsFromInstant());
    historyRequest.setVersionsToInstant(request.getVersionsToInstant());
    historyRequest.setObjectId(request.getObjectId());
    return history(historyRequest);
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
    private final List<PositionDocument> _documents = new ArrayList<PositionDocument>();

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
          if (posIdScheme.equals(ObjectId.EXTERNAL_SCHEME.getName())) {
            final ObjectId oid = ObjectId.parse(posIdValue);
            _position.getSecurityLink().setObjectId(oid);
          } else {
            final ExternalId id = ExternalId.of(posIdScheme, posIdValue);
            _position.getSecurityLink().addExternalId(id);
          }
        }

        final String posAttrKey = rs.getString("POS_ATTR_KEY");
        final String posAttrValue = rs.getString("POS_ATTR_VALUE");
        if (posAttrKey != null && posAttrValue != null) {
          _position.addAttribute(posAttrKey, posAttrValue);
        }

        final long tradeId = rs.getLong("TRADE_ID");
        if (_lastTradeId != tradeId && tradeId != 0) {
          buildTrade(rs, tradeId);
        }

        final String tradeIdScheme = rs.getString("TRADE_KEY_SCHEME");
        final String tradeIdValue = rs.getString("TRADE_KEY_VALUE");
        if (tradeIdScheme != null && tradeIdValue != null) {
          if (tradeIdScheme.equals(ObjectId.EXTERNAL_SCHEME.getName())) {
            final ObjectId oid = ObjectId.parse(tradeIdValue);
            _trade.getSecurityLink().setObjectId(oid);
          } else {
            final ExternalId id = ExternalId.of(tradeIdScheme, tradeIdValue);
            _trade.getSecurityLink().addExternalId(id);
          }
        }

        final String tradeAttrKey = rs.getString("TRADE_ATTR_KEY");
        final String tradeAttrValue = rs.getString("TRADE_ATTR_VALUE");
        if (tradeAttrKey != null && tradeAttrValue != null) {
          _trade.addAttribute(tradeAttrKey, tradeAttrValue);
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
      _position = new ManageablePosition(quantity, ExternalIdBundle.EMPTY);
      _position.setUniqueId(createUniqueId(positionOid, positionId));
      if (providerScheme != null && providerValue != null) {
        _position.setProviderId(ExternalId.of(providerScheme, providerValue));
      }
      final PositionDocument doc = new PositionDocument(_position);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setUniqueId(createUniqueId(positionOid, positionId));
      _documents.add(doc);
    }

    private void buildTrade(final ResultSet rs, final long tradeId) throws SQLException {
      _lastTradeId = tradeId;
      final long tradeOid = rs.getLong("TRADE_OID");
      final BigDecimal tradeQuantity = extractBigDecimal(rs, "TRADE_QUANTITY");
      final LocalDate tradeDate = DbDateUtils.fromSqlDate(rs.getDate("TRADE_DATE"));
      final LocalTime tradeTime = rs.getTimestamp("TRADE_TIME") != null ? DbDateUtils.fromSqlTime(rs.getTimestamp("TRADE_TIME")) : null;
      final int zoneOffset = rs.getInt("ZONE_OFFSET");
      final String cpartyScheme = rs.getString("CPARTY_SCHEME");
      final String cpartyValue = rs.getString("CPARTY_VALUE");
      final String providerScheme = rs.getString("TRADE_PROVIDER_SCHEME");
      final String providerValue = rs.getString("TRADE_PROVIDER_VALUE");
      OffsetTime tradeOffsetTime = null;
      if (tradeTime != null) {
        tradeOffsetTime = OffsetTime.of(tradeTime, ZoneOffset.ofTotalSeconds(zoneOffset));
      }
      ExternalId counterpartyId = null;
      if (cpartyScheme != null && cpartyValue != null) {
        counterpartyId = ExternalId.of(cpartyScheme, cpartyValue);
      }
      _trade = new ManageableTrade(tradeQuantity, ExternalIdBundle.EMPTY, tradeDate, tradeOffsetTime, counterpartyId);
      _trade.setUniqueId(createUniqueId(tradeOid, tradeId));
      if (providerScheme != null && providerValue != null) {
        _trade.setProviderId(ExternalId.of(providerScheme, providerValue));
      }
      // different databases return different types, notably BigDecimal and Double
      // note that getObject(key, Double.class) does not seem to work
      final Object premiumValue = rs.getObject("PREMIUM_VALUE");
      if (premiumValue != null) {
        _trade.setPremium(rs.getDouble("PREMIUM_VALUE"));
      }
      final String currencyCode = rs.getString("PREMIUM_CURRENCY");
      if (currencyCode != null) {
        _trade.setPremiumCurrency(Currency.of(currencyCode));
      }
      final Date premiumDate = rs.getDate("PREMIUM_DATE");
      if (premiumDate != null) {
        _trade.setPremiumDate(DbDateUtils.fromSqlDate(premiumDate));
      }
      _trade.setParentPositionId(_position.getUniqueId());
      final LocalTime premiumTime = rs.getTimestamp("PREMIUM_TIME") != null ? DbDateUtils.fromSqlTime(rs.getTimestamp("PREMIUM_TIME")) : null;
      final int premiumZoneOffset = rs.getInt("PREMIUM_ZONE_OFFSET");
      if (premiumTime != null) {
        _trade.setPremiumTime(OffsetTime.of(premiumTime, ZoneOffset.ofTotalSeconds(premiumZoneOffset)));
      }
      _position.getTrades().add(_trade);
    }
  }

}
