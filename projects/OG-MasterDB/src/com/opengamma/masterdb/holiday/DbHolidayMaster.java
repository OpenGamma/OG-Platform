/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidayMetaDataRequest;
import com.opengamma.master.holiday.HolidayMetaDataResult;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.db.Paging;
import com.opengamma.util.money.Currency;

/**
 * A holiday master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the holiday master using an SQL database.
 * Full details of the API are in {@link HolidayMaster}.
 * <p>
 * This class uses SQL via JDBC. The SQL may be changed by subclassing the relevant methods.
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
   * SQL select.
   */
  protected static final String SELECT =
      "SELECT " +
        "main.id AS doc_id, " +
        "main.oid AS doc_oid, " +
        "main.ver_from_instant AS ver_from_instant, " +
        "main.ver_to_instant AS ver_to_instant, " +
        "main.corr_from_instant AS corr_from_instant, " +
        "main.corr_to_instant AS corr_to_instant, " +
        "main.name AS name, " +
        "main.hol_type AS hol_type, " +
        "main.provider_scheme AS provider_scheme, " +
        "main.provider_value AS provider_value, " +
        "main.region_scheme AS region_scheme, " +
        "main.region_value AS region_value, " +
        "main.exchange_scheme AS exchange_scheme, " +
        "main.exchange_value AS exchange_value, " +
        "main.currency_iso AS currency_iso, " +
        "d.hol_date AS hol_date ";
  /**
   * SQL from.
   */
  protected static final String FROM =
      "FROM hol_holiday main LEFT JOIN hol_date d ON (d.holiday_id = main.id) ";

  /**
   * Creates an instance.
   * 
   * @param dbSource  the database source combining all configuration, not null
   */
  public DbHolidayMaster(final DbSource dbSource) {
    super(dbSource, IDENTIFIER_SCHEME_DEFAULT);
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
    
    final HolidaySearchResult result = new HolidaySearchResult();
    ExternalIdSearch regionSearch = request.getRegionExternalIdSearch();
    ExternalIdSearch exchangeSearch = request.getExchangeExternalIdSearch();
    String currencyISO = (request.getCurrency() != null ? request.getCurrency().getCode() : null);
    if ((request.getHolidayObjectIds() != null && request.getHolidayObjectIds().size() == 0) ||
        ExternalIdSearch.canMatch(regionSearch) == false ||
        ExternalIdSearch.canMatch(exchangeSearch) == false) {
      result.setPaging(Paging.of(request.getPagingRequest(), 0));
      return result;
    }
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
      .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
      .addValueNullIgnored("name", getDbHelper().sqlWildcardAdjustValue(request.getName()))
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
    }
    searchWithPaging(request.getPagingRequest(), sqlSearchHolidays(request), args, new HolidayDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL to search for documents.
   * 
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchHolidays(final HolidaySearchRequest request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
                "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
    if (request.getName() != null) {
      where += getDbHelper().sqlWildcardQuery("AND UPPER(name) ", "UPPER(:name)", request.getName());
    }
    if (request.getType() != null) {
      where += "AND hol_type = :hol_type ";
    }
    if (request.getProviderId() != null) {
      where += "AND provider_scheme = :provider_scheme AND provider_value = :provider_value ";
    }
    if (request.getRegionExternalIdSearch() != null) {
      where += "AND (" + sqlSelectIdKeys(request.getRegionExternalIdSearch(), "region") + ") ";
    }
    if (request.getExchangeExternalIdSearch() != null) {
      where += "AND (" + sqlSelectIdKeys(request.getExchangeExternalIdSearch(), "exchange") + ") ";
    }
    if (request.getCurrency() != null) {
      where += "AND currency_iso = :currency_iso ";
    }
    if (request.getHolidayObjectIds() != null) {
      StringBuilder buf = new StringBuilder(request.getHolidayObjectIds().size() * 10);
      for (ObjectId objectId : request.getHolidayObjectIds()) {
        checkScheme(objectId);
        buf.append(extractOid(objectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      where += "AND oid IN (" + buf + ") ";
    }
    where += sqlAdditionalWhere();
    
    String selectFromWhereInner = "SELECT id FROM hol_holiday " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY id ", request.getPagingRequest());
    String search = sqlSelectFrom() + "WHERE main.id IN (" + inner + ") ORDER BY main.id" + sqlAdditionalOrderBy(false);
    String count = "SELECT COUNT(*) FROM hol_holiday " + where;
    return new String[] {search, count};
  }

  /**
   * Gets the SQL to search for ids.
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
    ArgumentChecker.notNull(document.getName(), "document.name");
    
    final long docId = nextId("hol_holiday_seq");
    final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
    // the arguments for inserting into the holiday table
    final ManageableHoliday holiday = document.getHoliday();
    final DbMapSqlParameterSource holidayArgs = new DbMapSqlParameterSource()
      .addValue("doc_id", docId)
      .addValue("doc_oid", docOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("name", document.getName())
      .addValue("provider_scheme", (document.getProviderId() != null ? document.getProviderId().getScheme().getName() : null))
      .addValue("provider_value", (document.getProviderId() != null ? document.getProviderId().getValue() : null))
      .addValue("hol_type", holiday.getType() != null ? holiday.getType().name() : null)
      .addValue("region_scheme", (holiday.getRegionExternalId() != null ? holiday.getRegionExternalId().getScheme().getName() : null))
      .addValue("region_value", (holiday.getRegionExternalId() != null ? holiday.getRegionExternalId().getValue() : null))
      .addValue("exchange_scheme", (holiday.getExchangeExternalId() != null ? holiday.getExchangeExternalId().getScheme().getName() : null))
      .addValue("exchange_value", (holiday.getExchangeExternalId() != null ? holiday.getExchangeExternalId().getValue() : null))
      .addValue("currency_iso", (holiday.getCurrency() != null ? holiday.getCurrency().getCode() : null));
    // the arguments for inserting into the date table
    final List<DbMapSqlParameterSource> dateList = new ArrayList<DbMapSqlParameterSource>();
    for (LocalDate date : holiday.getHolidayDates()) {
      final DbMapSqlParameterSource dateArgs = new DbMapSqlParameterSource()
        .addValue("doc_id", docId)
        .addDate("hol_date", date);
      dateList.add(dateArgs);
    }
    getJdbcTemplate().update(sqlInsertHoliday(), holidayArgs);
    getJdbcTemplate().batchUpdate(sqlInsertDate(), dateList.toArray(new DbMapSqlParameterSource[dateList.size()]));
    // set the uniqueId
    final UniqueId uniqueId = createUniqueId(docOid, docId);
    holiday.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    return document;
  }

  /**
   * Gets the SQL for inserting a document.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertHoliday() {
    return "INSERT INTO hol_holiday " +
              "(id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, name, hol_type," +
              "provider_scheme, provider_value, region_scheme, region_value, exchange_scheme, exchange_value, currency_iso) " +
            "VALUES " +
              "(:doc_id, :doc_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :name, :hol_type," +
              ":provider_scheme, :provider_value, :region_scheme, :region_value, :exchange_scheme, :exchange_value, :currency_iso)";
  }

  /**
   * Gets the SQL for inserting a date.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertDate() {
    return "INSERT INTO hol_date (holiday_id, hol_date) " +
            "VALUES (:doc_id, :hol_date)";
  }

  //-------------------------------------------------------------------------
  @Override
  protected String sqlSelectFrom() {
    return SELECT + FROM;
  }

  @Override
  protected String sqlAdditionalOrderBy(final boolean orderByPrefix) {
    return (orderByPrefix ? "ORDER BY " : ", ") + "d.hol_date ";
  }

  @Override
  protected String mainTableName() {
    return "hol_holiday";
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

}
