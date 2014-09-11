/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.threeten.bp.LocalDate;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidayMetaDataRequest;
import com.opengamma.master.holiday.HolidayMetaDataResult;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.HolidaySearchSortOrder;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.money.Currency;
import com.opengamma.util.paging.Paging;

/**
 * A holiday master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the holiday master using an SQL database.
 * Full details of the API are in {@link HolidayMaster}.
 * <p>
 * The SQL is stored externally in {@code DbHolidayMaster.elsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbHolidayMaster-MySpecialDB.elsql}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbHolidayMaster extends AbstractDocumentDbMaster<HolidayDocument> implements HolidayMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbHolidayMaster.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbHol";

  /**
   * SQL order by.
   */
  protected static final EnumMap<HolidaySearchSortOrder, String> ORDER_BY_MAP = new EnumMap<HolidaySearchSortOrder, String>(HolidaySearchSortOrder.class);
  static {
    ORDER_BY_MAP.put(HolidaySearchSortOrder.OBJECT_ID_ASC, "oid ASC");
    ORDER_BY_MAP.put(HolidaySearchSortOrder.OBJECT_ID_DESC, "oid DESC");
    ORDER_BY_MAP.put(HolidaySearchSortOrder.VERSION_FROM_INSTANT_ASC, "ver_from_instant ASC");
    ORDER_BY_MAP.put(HolidaySearchSortOrder.VERSION_FROM_INSTANT_DESC, "ver_from_instant DESC");
    ORDER_BY_MAP.put(HolidaySearchSortOrder.NAME_ASC, "name ASC");
    ORDER_BY_MAP.put(HolidaySearchSortOrder.NAME_DESC, "name DESC");
  }

  // -----------------------------------------------------------------
  // TIMERS FOR METRICS GATHERING
  // By default these do nothing. Registration will replace them
  // so that they actually do something.
  // -----------------------------------------------------------------
  private Timer _insertTimer = new Timer();
  
  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbHolidayMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbHolidayMaster.class));
  }

  @Override
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailedRegistry, String namePrefix) {
    super.registerMetrics(summaryRegistry, detailedRegistry, namePrefix);
    _insertTimer = summaryRegistry.timer(namePrefix + ".insert");
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayMetaDataResult metaData(HolidayMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    HolidayMetaDataResult result = new HolidayMetaDataResult();
    if (request.isHolidayTypes()) {
      result.getHolidayTypes().addAll(Arrays.asList(HolidayType.values()));
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidaySearchResult search(final HolidaySearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    final HolidaySearchResult result = new HolidaySearchResult(vc);
    
    ExternalIdSearch regionSearch = request.getRegionExternalIdSearch();
    ExternalIdSearch exchangeSearch = request.getExchangeExternalIdSearch();
    ExternalIdSearch customSearch = request.getCustomExternalIdSearch();
    String currencyISO = (request.getCurrency() != null ? request.getCurrency().getCode() : null);
    if ((request.getHolidayObjectIds() != null && request.getHolidayObjectIds().size() == 0) ||
        ExternalIdSearch.canMatch(regionSearch) == false ||
        ExternalIdSearch.canMatch(exchangeSearch) == false) {
      result.setPaging(Paging.of(request.getPagingRequest(), 0));
      return result;
    }
    
    final DbMapSqlParameterSource args = createParameterSource()
      .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
      .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
      .addValueNullIgnored("name", getDialect().sqlWildcardAdjustValue(request.getName()))
      .addValueNullIgnored("hol_type", request.getType() != null ? request.getType().name() : null)
      .addValueNullIgnored("currency_iso", currencyISO);
    if (request.getProviderId() != null) {
      args.addValue("provider_scheme", request.getProviderId().getScheme().getName());
      args.addValue("provider_value", request.getProviderId().getValue());
    }
    if (regionSearch != null) {
      if (regionSearch.getSearchType() != ExternalIdSearchType.ANY) {
        throw new IllegalArgumentException("Unsupported search type: " + regionSearch.getSearchType());
      }
      int i = 0;
      for (ExternalId idKey : regionSearch.getExternalIds()) {
        args.addValue("region_scheme" + i, idKey.getScheme().getName());
        args.addValue("region_value" + i, idKey.getValue());
        i++;
      }
      args.addValue("sql_search_region_ids", sqlSelectIdKeys(request.getRegionExternalIdSearch(), "region"));
    }
    if (exchangeSearch != null) {
      if (exchangeSearch.getSearchType() != ExternalIdSearchType.ANY) {
        throw new IllegalArgumentException("Unsupported search type: " + exchangeSearch.getSearchType());
      }
      int i = 0;
      for (ExternalId idKey : exchangeSearch.getExternalIds()) {
        args.addValue("exchange_scheme" + i, idKey.getScheme().getName());
        args.addValue("exchange_value" + i, idKey.getValue());
        i++;
      }
      args.addValue("sql_search_exchange_ids", sqlSelectIdKeys(request.getExchangeExternalIdSearch(), "exchange"));
    }
    if (customSearch != null) {
      if (customSearch.getSearchType() != ExternalIdSearchType.ANY) {
        throw new IllegalArgumentException("Unsupported search type: " + customSearch.getSearchType());
      }
      int i = 0;
      for (ExternalId idKey : customSearch.getExternalIds()) {
        args.addValue("custom_scheme" + i, idKey.getScheme().getName());
        args.addValue("custom_value" + i, idKey.getValue());
        i++;
      }
      args.addValue("sql_search_custom_ids", sqlSelectIdKeys(request.getCustomExternalIdSearch(), "custom"));
    }
    if (request.getHolidayObjectIds() != null) {
      StringBuilder buf = new StringBuilder(request.getHolidayObjectIds().size() * 10);
      for (ObjectId objectId : request.getHolidayObjectIds()) {
        checkScheme(objectId);
        buf.append(extractOid(objectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      args.addValue("sql_search_object_ids", buf.toString());
    }
    args.addValue("sort_order", ORDER_BY_MAP.get(request.getSortOrder()));
    args.addValue("paging_offset", request.getPagingRequest().getFirstItem());
    args.addValue("paging_fetch", request.getPagingRequest().getPagingSize());
    
    final String[] sql = {getElSqlBundle().getSql("Search", args), getElSqlBundle().getSql("SearchCount", args)};
    doSearch(request.getPagingRequest(), sql, args, new HolidayDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL to search for ids.
   * <p>
   * This is too complex for the elsql mechanism.
   * 
   * @param bundle  the bundle, not null
   * @param type  the type to search for, not null
   * @return the SQL search and count, not null
   */
  protected String sqlSelectIdKeys(final ExternalIdSearch bundle, final String type) {
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < bundle.size(); i++) {
      list.add("(" + type + "_scheme = :" + type + "_scheme" + i + " AND " + type + "_value = :" + type + "_value" + i + ") ");
    }
    return StringUtils.join(list, "OR ");
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument get(final UniqueId uniqueId) {
    return doGet(uniqueId, new HolidayDocumentExtractor(), "Holiday");
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new HolidayDocumentExtractor(), "Holiday");
  }

  //-------------------------------------------------------------------------
  @Override
  public HolidayHistoryResult history(final HolidayHistoryRequest request) {
    return doHistory(request, new HolidayHistoryResult(), new HolidayDocumentExtractor());
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a new document.
   * 
   * @param document  the document, not null
   * @return the new document, not null
   */
  @Override
  protected HolidayDocument insert(final HolidayDocument document) {
    ArgumentChecker.notNull(document.getHoliday(), "document.holiday");
    ArgumentChecker.notNull(document.getHoliday().getType(), "document.holiday.type");
    ArgumentChecker.notNull(document.getName(), "document.name");
    switch (document.getHoliday().getType()) {
      case BANK:
        ArgumentChecker.notNull(document.getHoliday().getRegionExternalId(), "document.holiday.region");
        break;
      case CURRENCY:
        ArgumentChecker.notNull(document.getHoliday().getCurrency(), "document.holiday.currency");
        break;
      case TRADING:
      case SETTLEMENT:
        ArgumentChecker.notNull(document.getHoliday().getExchangeExternalId(), "document.holiday.exchange");
        break;
      case CUSTOM:
        ArgumentChecker.notNull(document.getHoliday().getCustomExternalId(), "document.holiday.custom");
        break;
      default:
        throw new IllegalArgumentException("Holiday type not set");
    }
    
    try (Timer.Context context = _insertTimer.time()) {
      final long docId = nextId("hol_holiday_seq");
      final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
      // the arguments for inserting into the holiday table
      final ManageableHoliday holiday = document.getHoliday();
      final DbMapSqlParameterSource docArgs = createParameterSource()
        .addValue("doc_id", docId)
        .addValue("doc_oid", docOid)
        .addTimestamp("ver_from_instant", document.getVersionFromInstant())
        .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
        .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
        .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
        .addValue("name", document.getName())
        .addValue("provider_scheme",
            document.getProviderId() != null ? document.getProviderId().getScheme().getName() : null,
            Types.VARCHAR)
        .addValue("provider_value",
            document.getProviderId() != null ? document.getProviderId().getValue() : null,
            Types.VARCHAR)
        .addValue("hol_type", holiday.getType().name())
        .addValue("region_scheme",
            holiday.getRegionExternalId() != null ? holiday.getRegionExternalId().getScheme().getName() : null,
            Types.VARCHAR)
        .addValue("region_value",
            holiday.getRegionExternalId() != null ? holiday.getRegionExternalId().getValue() : null,
            Types.VARCHAR)
        .addValue("exchange_scheme",
            holiday.getExchangeExternalId() != null ? holiday.getExchangeExternalId().getScheme().getName() : null,
            Types.VARCHAR)
        .addValue("exchange_value",
            holiday.getExchangeExternalId() != null ? holiday.getExchangeExternalId().getValue() : null,
            Types.VARCHAR)
        .addValue("custom_scheme",
            holiday.getCustomExternalId() != null ? holiday.getCustomExternalId().getScheme().getName() : null,
            Types.VARCHAR)
        .addValue("custom_value",
            holiday.getCustomExternalId() != null ? holiday.getCustomExternalId().getValue() : null,
            Types.VARCHAR)
        .addValue("currency_iso",
            holiday.getCurrency() != null ? holiday.getCurrency().getCode() : null,
            Types.VARCHAR);
      // the arguments for inserting into the date table
      final List<DbMapSqlParameterSource> dateList = new ArrayList<DbMapSqlParameterSource>();
      for (LocalDate date : holiday.getHolidayDates()) {
        final DbMapSqlParameterSource dateArgs = createParameterSource()
          .addValue("doc_id", docId)
          .addDate("hol_date", date);
        dateList.add(dateArgs);
      }
      final String sqlDoc = getElSqlBundle().getSql("Insert", docArgs);
      final String sqlDate = getElSqlBundle().getSql("InsertDate");
      getJdbcTemplate().update(sqlDoc, docArgs);
      getJdbcTemplate().batchUpdate(sqlDate, dateList.toArray(new DbMapSqlParameterSource[dateList.size()]));
      // set the uniqueId
      final UniqueId uniqueId = createUniqueId(docOid, docId);
      holiday.setUniqueId(uniqueId);
      document.setUniqueId(uniqueId);
      return document;
    }
    
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a HolidayDocument.
   */
  protected final class HolidayDocumentExtractor implements ResultSetExtractor<List<HolidayDocument>> {
    private long _lastDocId = -1;
    private ManageableHoliday _holiday;
    private List<HolidayDocument> _documents = new ArrayList<HolidayDocument>();

    @Override
    public List<HolidayDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long docId = rs.getLong("DOC_ID");
        if (_lastDocId != docId) {
          _lastDocId = docId;
          buildHoliday(rs, docId);
        }
        final LocalDate holDate = DbDateUtils.fromSqlDate(rs.getDate("HOL_DATE"));
        _holiday.getHolidayDates().add(holDate);
      }
      return _documents;
    }

    private void buildHoliday(final ResultSet rs, final long docId) throws SQLException {
      final long docOid = rs.getLong("DOC_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      final String name = rs.getString("NAME");
      final String type = rs.getString("HOL_TYPE");
      final String providerScheme = rs.getString("PROVIDER_SCHEME");
      final String providerValue = rs.getString("PROVIDER_VALUE");
      final String regionScheme = rs.getString("REGION_SCHEME");
      final String regionValue = rs.getString("REGION_VALUE");
      final String exchangeScheme = rs.getString("EXCHANGE_SCHEME");
      final String exchangeValue = rs.getString("EXCHANGE_VALUE");
      final String customScheme = rs.getString("CUSTOM_SCHEME");
      final String customValue = rs.getString("CUSTOM_VALUE");
      final String currencyISO = rs.getString("CURRENCY_ISO");

      UniqueId uniqueId = createUniqueId(docOid, docId);
      ManageableHoliday holiday = new ManageableHoliday();
      holiday.setUniqueId(uniqueId);
      holiday.setType(HolidayType.valueOf(type));
      if (regionScheme != null && regionValue != null) {
        holiday.setRegionExternalId(ExternalId.of(regionScheme, regionValue));
      }
      if (exchangeScheme != null && exchangeValue != null) {
        holiday.setExchangeExternalId(ExternalId.of(exchangeScheme, exchangeValue));
      }
      if (customScheme != null && customValue != null) {
        holiday.setCustomExternalId(ExternalId.of(customScheme, customValue));
      }
      if (currencyISO != null) {
        holiday.setCurrency(Currency.of(currencyISO));
      }
      HolidayDocument doc = new HolidayDocument(holiday);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setUniqueId(uniqueId);
      doc.setName(name);
      if (providerScheme != null && providerValue != null) {
        doc.setProviderId(ExternalId.of(providerScheme, providerValue));
      }
      _holiday = doc.getHoliday();
      _documents.add(doc);
    }
  }

  @Override
  protected AbstractHistoryResult<HolidayDocument> historyByVersionsCorrections(AbstractHistoryRequest request) {
    HolidayHistoryRequest historyRequest = new HolidayHistoryRequest();
    historyRequest.setCorrectionsFromInstant(request.getCorrectionsFromInstant());
    historyRequest.setCorrectionsToInstant(request.getCorrectionsToInstant());
    historyRequest.setVersionsFromInstant(request.getVersionsFromInstant());
    historyRequest.setVersionsToInstant(request.getVersionsToInstant());
    historyRequest.setObjectId(request.getObjectId());
    return history(historyRequest);
  }

}
