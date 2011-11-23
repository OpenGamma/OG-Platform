/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.time.Duration;
import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetDateTime;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSummary;
import com.opengamma.extsql.ExtSqlBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * A time-series master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the time-series master using an SQL database.
 * Full details of the API are in {@link HistoricalTimeSeriesMaster}.
 * <p>
 * This implementation uses two linked unique identifiers, one for the document
 * and one for the time-series. They share the same scheme, but have different values
 * and versions. All the methods accept both formats although where possible they
 * should be treated separately. 
 * .
 * <p>
 * This class uses SQL via JDBC. The SQL may be changed by subclassing the relevant methods.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbHistoricalTimeSeriesMaster extends AbstractDocumentDbMaster<HistoricalTimeSeriesInfoDocument> implements HistoricalTimeSeriesMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMaster.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbHts";
  /**
   * The prefix used for data point unique identifiers.
   */
  protected static final String DATA_POINT_PREFIX = "DP";

  /**
   * Dimension table.
   */
  private final NamedDimensionDbTable _nameTable;
  /**
   * Dimension table.
   */
  private final NamedDimensionDbTable _dataFieldTable;
  /**
   * Dimension table.
   */
  private final NamedDimensionDbTable _dataSourceTable;
  /**
   * Dimension table.
   */
  private final NamedDimensionDbTable _dataProviderTable;
  /**
   * Dimension table.
   */
  private final NamedDimensionDbTable _observationTimeTable;

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbHistoricalTimeSeriesMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    setExtSqlBundle(ExtSqlBundle.of(dbConnector.getDialect().getExtSqlConfig(), DbHistoricalTimeSeriesMaster.class));
    _nameTable = new NamedDimensionDbTable(dbConnector, "name", "hts_name", "hts_dimension_seq");
    _dataFieldTable = new NamedDimensionDbTable(dbConnector, "data_field", "hts_data_field", "hts_dimension_seq");
    _dataSourceTable = new NamedDimensionDbTable(dbConnector, "data_source", "hts_data_source", "hts_dimension_seq");
    _dataProviderTable = new NamedDimensionDbTable(dbConnector, "data_provider", "hts_data_provider", "hts_dimension_seq");
    _observationTimeTable = new NamedDimensionDbTable(dbConnector, "observation_time", "hts_observation_time", "hts_dimension_seq");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the dimension table helper.
   * 
   * @return the table, not null
   */
  protected NamedDimensionDbTable getNameTable() {
    return _nameTable;
  }

  /**
   * Gets the dimension table helper.
   * 
   * @return the table, not null
   */
  protected NamedDimensionDbTable getDataFieldTable() {
    return _dataFieldTable;
  }

  /**
   * Gets the dimension table helper.
   * 
   * @return the table, not null
   */
  protected NamedDimensionDbTable getDataSourceTable() {
    return _dataSourceTable;
  }

  /**
   * Gets the dimension table helper.
   * 
   * @return the table, not null
   */
  protected NamedDimensionDbTable getDataProviderTable() {
    return _dataProviderTable;
  }

  /**
   * Gets the dimension table helper.
   * 
   * @return the table, not null
   */
  protected NamedDimensionDbTable getObservationTimeTable() {
    return _observationTimeTable;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoMetaDataResult metaData(HistoricalTimeSeriesInfoMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    HistoricalTimeSeriesInfoMetaDataResult result = new HistoricalTimeSeriesInfoMetaDataResult();
    if (request.isDataFields()) {
      result.setDataFields(getDataFieldTable().names());
    }
    if (request.isDataSources()) {
      result.setDataSources(getDataSourceTable().names());
    }
    if (request.isDataProviders()) {
      result.setDataProviders(getDataProviderTable().names());
    }
    if (request.isObservationTimes()) {
      result.setObservationTimes(getObservationTimeTable().names());
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoSearchResult search(final HistoricalTimeSeriesInfoSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult();
    final List<ObjectId> objectIds = request.getObjectIds();
    final ExternalIdSearch externalIdSearch = request.getExternalIdSearch();
    if ((objectIds != null && objectIds.size() == 0) ||
        (ExternalIdSearch.canMatch(externalIdSearch) == false)) {
      result.setPaging(Paging.of(request.getPagingRequest(), 0));
      return result;
    }
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource();
    args.addTimestamp("version_as_of_instant", vc.getVersionAsOf());
    args.addTimestamp("corrected_to_instant", vc.getCorrectedTo());
    args.addValueNullIgnored("name", getDialect().sqlWildcardAdjustValue(request.getName()));
    args.addValueNullIgnored("data_field", getDialect().sqlWildcardAdjustValue(request.getDataField()));
    args.addValueNullIgnored("data_source", getDialect().sqlWildcardAdjustValue(request.getDataSource()));
    args.addValueNullIgnored("data_provider", getDialect().sqlWildcardAdjustValue(request.getDataProvider()));
    args.addValueNullIgnored("observation_time", getDialect().sqlWildcardAdjustValue(request.getObservationTime()));
    args.addDateNullIgnored("id_validity_date", request.getValidityDate());
    args.addValueNullIgnored("external_id_value", getDialect().sqlWildcardAdjustValue(request.getExternalIdValue()));
    if (externalIdSearch != null) {
      int i = 0;
      for (ExternalId id : externalIdSearch) {
        args.addValue("key_scheme" + i, id.getScheme().getName());
        args.addValue("key_value" + i, id.getValue());
        i++;
      }
    }
    if (externalIdSearch != null && externalIdSearch.alwaysMatches() == false) {
      int i = 0;
      for (ExternalId id : externalIdSearch) {
        args.addValue("key_scheme" + i, id.getScheme().getName());
        args.addValue("key_value" + i, id.getValue());
        i++;
      }
      args.addValue("sql_search_external_ids_type", externalIdSearch.getSearchType());
      args.addValue("sql_search_external_ids", sqlSelectIdKeys(externalIdSearch));
      args.addValue("id_search_size", externalIdSearch.getExternalIds().size());
    }
    if (objectIds != null) {
      StringBuilder buf = new StringBuilder(objectIds.size() * 10);
      for (ObjectId objectId : objectIds) {
        checkScheme(objectId);
        buf.append(extractOid(objectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      args.addValue("sql_search_object_ids", buf.toString());
    }
    args.addValue("paging_offset", request.getPagingRequest().getFirstItem());
    args.addValue("paging_fetch", request.getPagingRequest().getPagingSize());
    
    String[] sql = {getExtSqlBundle().getSql("Search", args), getExtSqlBundle().getSql("SearchCount", args)};
    searchWithPaging(request.getPagingRequest(), sql, args, new HistoricalTimeSeriesDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL to find all the ids for a single bundle.
   * <p>
   * This is too complex for the extsql mechanism.
   * 
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectIdKeys(final ExternalIdSearch idSearch) {
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < idSearch.size(); i++) {
      list.add("(key_scheme = :key_scheme" + i + " AND key_value = :key_value" + i + ") ");
    }
    return StringUtils.join(list, "OR ");
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    if (uniqueId.getVersion() != null && uniqueId.getVersion().contains("P")) {
      VersionCorrection vc = extractTimeSeriesInstants(uniqueId);
      return get(uniqueId.getObjectId(), vc);
    }
    return doGet(uniqueId, new HistoricalTimeSeriesDocumentExtractor(), "HistoricalTimeSeries");
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new HistoricalTimeSeriesDocumentExtractor(), "HistoricalTimeSeries");
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoHistoryResult history(final HistoricalTimeSeriesInfoHistoryRequest request) {
    return doHistory(request, new HistoricalTimeSeriesInfoHistoryResult(), new HistoricalTimeSeriesDocumentExtractor());
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a new document.
   * 
   * @param document  the document, not null
   * @return the new document, not null
   */
  @Override
  protected HistoricalTimeSeriesInfoDocument insert(final HistoricalTimeSeriesInfoDocument document) {
    ArgumentChecker.notNull(document.getInfo(), "document.info");
    ArgumentChecker.notNull(document.getInfo().getName(), "document.info.name");
    ArgumentChecker.notNull(document.getInfo().getDataField(), "document.info.dataField");
    ArgumentChecker.notNull(document.getInfo().getDataSource(), "document.info.dataSource");
    ArgumentChecker.notNull(document.getInfo().getDataProvider(), "document.info.dataProvider");
    ArgumentChecker.notNull(document.getInfo().getObservationTime(), "document.info.observationTime");
    
    final long docId = nextId("hts_master_seq");
    final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
    // the arguments for inserting into the table
    final ManageableHistoricalTimeSeriesInfo info = document.getInfo();
    final DbMapSqlParameterSource docArgs = new DbMapSqlParameterSource()
      .addValue("doc_id", docId)
      .addValue("doc_oid", docOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("name_id", getNameTable().ensure(info.getName()))
      .addValue("data_field_id", getDataFieldTable().ensure(info.getDataField()))
      .addValue("data_source_id", getDataSourceTable().ensure(info.getDataSource()))
      .addValue("data_provider_id", getDataProviderTable().ensure(info.getDataProvider()))
      .addValue("observation_time_id", getObservationTimeTable().ensure(info.getObservationTime()));
    // the arguments for inserting into the idkey tables
    final List<DbMapSqlParameterSource> assocList = new ArrayList<DbMapSqlParameterSource>();
    final List<DbMapSqlParameterSource> idKeyList = new ArrayList<DbMapSqlParameterSource>();
    final String sqlSelectIdKey = getExtSqlBundle().getSql("SelectIdKey");
    for (ExternalIdWithDates id : info.getExternalIdBundle()) {
      final DbMapSqlParameterSource assocArgs = new DbMapSqlParameterSource()
        .addValue("doc_id", docId)
        .addValue("key_scheme", id.getExternalId().getScheme().getName())
        .addValue("key_value", id.getExternalId().getValue())
        .addValue("valid_from", DbDateUtils.toSqlDateNullFarPast(id.getValidFrom()))
        .addValue("valid_to", DbDateUtils.toSqlDateNullFarFuture(id.getValidTo()));
      assocList.add(assocArgs);
      if (getJdbcTemplate().queryForList(sqlSelectIdKey, assocArgs).isEmpty()) {
        // select avoids creating unecessary id, but id may still not be used
        final long idKeyId = nextId("hts_idkey_seq");
        final DbMapSqlParameterSource idkeyArgs = new DbMapSqlParameterSource()
          .addValue("idkey_id", idKeyId)
          .addValue("key_scheme", id.getExternalId().getScheme().getName())
          .addValue("key_value", id.getExternalId().getValue());
        idKeyList.add(idkeyArgs);
      }
    }
    
    // insert
    final String sqlDoc = getExtSqlBundle().getSql("Insert", docArgs);
    final String sqlIdKey = getExtSqlBundle().getSql("InsertIdKey");
    final String sqlDoc2IdKey = getExtSqlBundle().getSql("InsertDoc2IdKey");
    getJdbcTemplate().update(sqlDoc, docArgs);
    getJdbcTemplate().batchUpdate(sqlIdKey, idKeyList.toArray(new DbMapSqlParameterSource[idKeyList.size()]));
    getJdbcTemplate().batchUpdate(sqlDoc2IdKey, assocList.toArray(new DbMapSqlParameterSource[assocList.size()]));
    
    // set the uniqueId
    final UniqueId uniqueId = createUniqueId(docOid, docId);
    info.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    document.getInfo().setTimeSeriesObjectId(uniqueId.getObjectId().withValue(DATA_POINT_PREFIX + uniqueId.getValue()));
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(
      UniqueId uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    checkScheme(uniqueId);
    final VersionCorrection vc;
    if (uniqueId.isVersioned() && uniqueId.getValue().startsWith(DATA_POINT_PREFIX)) {
      vc = extractTimeSeriesInstants(uniqueId);
    } else {
      vc = VersionCorrection.LATEST;
    }
    return getTimeSeries(uniqueId, vc, fromDateInclusive, toDateInclusive);
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(
      ObjectIdentifiable objectId, VersionCorrection versionCorrection, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    
    final long oid = extractOid(objectId); 
    final VersionCorrection vc = versionCorrection.withLatestFixed(now());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("doc_oid", oid)
      .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
      .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
      .addValue("start_date", DbDateUtils.toSqlDateNullFarPast(fromDateInclusive))
      .addValue("end_date", DbDateUtils.toSqlDateNullFarFuture(toDateInclusive));
    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate().getNamedParameterJdbcOperations();
    
    // get metadata
    final String sqlCommon = getExtSqlBundle().getSql("SelectDataPointsCommon", args);
    ManageableHistoricalTimeSeries result = namedJdbc.query(sqlCommon, args, new ManageableHTSExtractor(oid));
    if (result == null) {
      throw new DataNotFoundException("Unable to find time-series: " + objectId);
    }
    
    // get data points
    if (toDateInclusive == null || fromDateInclusive == null || !toDateInclusive.isBefore(fromDateInclusive)) {
      final String sqlPoints = getExtSqlBundle().getSql("SelectDataPoints", args);
      LocalDateDoubleTimeSeries series = namedJdbc.query(sqlPoints, args, new DataPointsExtractor());
      result.setTimeSeries(series);
    } else {
      //TODO: this is a hack, most of the places that call with this condition want some kind of metadata, which it would be cheaper for us to expose specifically
      result.setTimeSeries(new ArrayLocalDateDoubleTimeSeries());
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Get a single data point from a hts as defined by the query argument.
   * 
   * @param objectId  the time-series object identifier, not null
   * @param versionCorrection  the version-correction locator to search at, not null
   * @param query  the SQL query that returns one row with two columns: LocalDate and Double, not null
   * @return a pair containing the LocalDate and the Double value of the data point, not null
   */
  protected Pair<LocalDate, Double> getHTSValue(ObjectIdentifiable objectId, VersionCorrection versionCorrection, String query) {
    final long oid = extractOid(objectId);
    versionCorrection = versionCorrection.withLatestFixed(now());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("doc_oid", oid)
      .addTimestamp("version_as_of_instant", versionCorrection.getVersionAsOf())
      .addTimestamp("corrected_to_instant", versionCorrection.getCorrectedTo());
    
    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate().getNamedParameterJdbcOperations();
    List<Map<String, Object>> result;    
    try {
      result = namedJdbc.queryForList(query, args);
    } catch (Exception e) {
      throw new DataNotFoundException("Unable to fetch earliest/latest date/value from time series " + objectId.getObjectId());
    }
    return new ObjectsPair<LocalDate, Double>((LocalDate) DbDateUtils.fromSqlDateAllowNull((Date) (result.get(0).get("point_date"))), (Double) (result.get(0).get("point_value")));
  }

  @Override
  public HistoricalTimeSeriesSummary getSummary(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    checkScheme(uniqueId);
    
    final VersionCorrection vc;
    if (uniqueId.isVersioned() && uniqueId.getValue().startsWith(DATA_POINT_PREFIX)) {
      vc = extractTimeSeriesInstants(uniqueId);
    } else {
      vc = VersionCorrection.LATEST;
    }
    return getSummary(uniqueId.getObjectId(), vc);
  }

  public HistoricalTimeSeriesSummary getSummary(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    final String sqlEarliest = getExtSqlBundle().getSql("SelectEarliestDataPoint");
    Pair<LocalDate, Double> earliest = getHTSValue(objectId, versionCorrection, sqlEarliest);
    final String sqlLatest = getExtSqlBundle().getSql("SelectLatestDataPoint");
    Pair<LocalDate, Double> latest = getHTSValue(objectId, versionCorrection, sqlLatest);
    
    HistoricalTimeSeriesSummary result = new HistoricalTimeSeriesSummary();    
    result.setEarliestDate(earliest.getFirst());
    result.setEarliestValue(earliest.getSecond());
    result.setLatestDate(latest.getFirst());
    result.setLatestValue(latest.getSecond());   
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId updateTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDateDoubleTimeSeries series) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(series, "series");
    s_logger.debug("add time-series data points to {}", objectId);
    
    // retry to handle concurrent conflicts
    for (int retry = 0; true; retry++) {
      final UniqueId uniqueId = resolveObjectId(objectId, VersionCorrection.LATEST);
      if (series.isEmpty()) {
        return uniqueId;
      }
      try {
        final Instant now = now();
        UniqueId resultId = getTransactionTemplate().execute(new TransactionCallback<UniqueId>() {
          @Override
          public UniqueId doInTransaction(final TransactionStatus status) {
            insertDataPointsCheckMaxDate(uniqueId, series);
            return insertDataPoints(uniqueId, series, now);
          }
        });
        changeManager().entityChanged(ChangeType.UPDATED, uniqueId, resultId, now);
        return resultId;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaxRetries()) {
          throw ex;
        }
      } catch (DataAccessException ex) {
        throw fixSQLExceptionCause(ex);
      }
    }
  }

  /**
   * Checks the data points can be inserted.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param series  the time-series data points, not empty, not null
   */
  protected void insertDataPointsCheckMaxDate(final UniqueId uniqueId, final LocalDateDoubleTimeSeries series) {
    final Long docOid = extractOid(uniqueId);
    final VersionCorrection vc = extractTimeSeriesInstants(uniqueId);
    final DbMapSqlParameterSource queryArgs = new DbMapSqlParameterSource()
      .addValue("doc_oid", docOid)
      .addTimestamp("ver_instant", vc.getVersionAsOf())
      .addTimestamp("corr_instant", vc.getCorrectedTo());
    final String sql = getExtSqlBundle().getSql("SelectMaxPointDate", queryArgs);
    Date result = getDbConnector().getJdbcTemplate().queryForObject(sql, Date.class, queryArgs);
    if (result != null) {
      LocalDate maxDate = DbDateUtils.fromSqlDateAllowNull(result);
      if (series.getTimeAt(0).isBefore(maxDate)) {
        throw new IllegalArgumentException("Unable to update data points of time-series " + uniqueId +
            " as the update starts at " + series.getTimeAt(0) +
            " which is before the latest data point in the database at " + maxDate);
      }
    }
  }

  /**
   * Inserts the data points.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param series  the time-series data points, not empty, not null
   * @param now  the current instant, not null
   * @return the unique identifier, not null
   */
  protected UniqueId insertDataPoints(final UniqueId uniqueId, final LocalDateDoubleTimeSeries series, final Instant now) {
    final Long docOid = extractOid(uniqueId);
    final Timestamp nowTS = DbDateUtils.toSqlTimestamp(now);
    final List<DbMapSqlParameterSource> argsList = new ArrayList<DbMapSqlParameterSource>();
    for (Entry<LocalDate, Double> entry : series) {
      LocalDate date = entry.getKey();
      Double value = entry.getValue();
      if (date == null || value == null) {
        throw new IllegalArgumentException("Time-series must not contain a null value");
      }
      final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
        .addValue("doc_oid", docOid)
        .addDate("point_date", date)
        .addValue("ver_instant", nowTS)
        .addValue("corr_instant", nowTS)
        .addValue("point_value", value);
      argsList.add(args);
    }
    final String sqlInsert = getExtSqlBundle().getSql("InsertDataPoint");
    getJdbcTemplate().batchUpdate(sqlInsert, argsList.toArray(new DbMapSqlParameterSource[argsList.size()]));
    return createTimeSeriesUniqueId(docOid, now, now);
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId correctTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDateDoubleTimeSeries series) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(series, "series");
    s_logger.debug("add time-series data points to {}", objectId);
    
    // retry to handle concurrent conflicts
    for (int retry = 0; true; retry++) {
      final UniqueId uniqueId = resolveObjectId(objectId, VersionCorrection.LATEST);
      if (series.isEmpty()) {
        return uniqueId;
      }
      try {
        final Instant now = now();
        UniqueId resultId = getTransactionTemplate().execute(new TransactionCallback<UniqueId>() {
          @Override
          public UniqueId doInTransaction(final TransactionStatus status) {
            return correctDataPoints(uniqueId, series, now);
          }
        });
        changeManager().entityChanged(ChangeType.CORRECTED, uniqueId, resultId, now);
        return resultId;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaxRetries()) {
          throw ex;
        }
      } catch (DataAccessException ex) {
        throw fixSQLExceptionCause(ex);
      }
    }
  }

  /**
   * Corrects the data points.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param series  the time-series data points, not empty, not null
   * @param now  the current instant, not null
   * @return the unique identifier, not null
   */
  protected UniqueId correctDataPoints(UniqueId uniqueId, LocalDateDoubleTimeSeries series, Instant now) {
    final Long docOid = extractOid(uniqueId);
    final Timestamp nowTS = DbDateUtils.toSqlTimestamp(now);
    final List<DbMapSqlParameterSource> argsList = new ArrayList<DbMapSqlParameterSource>();
    for (Entry<LocalDate, Double> entry : series) {
      LocalDate date = entry.getKey();
      Double value = entry.getValue();
      if (date == null || value == null) {
        throw new IllegalArgumentException("Time-series must not contain a null value");
      }
      final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
        .addValue("doc_oid", docOid)
        .addDate("point_date", date)
        .addValue("corr_instant", nowTS)
        .addValue("point_value", value);
      argsList.add(args);
    }
    final String sqlInsert = getExtSqlBundle().getSql("InsertCorrectDataPoint");
    getJdbcTemplate().batchUpdate(sqlInsert, argsList.toArray(new DbMapSqlParameterSource[argsList.size()]));
    return resolveObjectId(uniqueId, VersionCorrection.of(now, now));
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId removeTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDate fromDateInclusive, final LocalDate toDateInclusive) {
    ArgumentChecker.notNull(objectId, "objectId");
    if (fromDateInclusive != null && toDateInclusive != null) {
      ArgumentChecker.inOrderOrEqual(fromDateInclusive, toDateInclusive, "fromDateInclusive", "toDateInclusive");
    }
    s_logger.debug("removing time-series data points from {}", objectId);
    
    // retry to handle concurrent conflicts
    for (int retry = 0; true; retry++) {
      final UniqueId uniqueId = resolveObjectId(objectId, VersionCorrection.LATEST);
      try {
        final Instant now = now();
        UniqueId resultId = getTransactionTemplate().execute(new TransactionCallback<UniqueId>() {
          @Override
          public UniqueId doInTransaction(final TransactionStatus status) {
            return removeDataPoints(uniqueId, fromDateInclusive, toDateInclusive, now);
          }
        });
        changeManager().entityChanged(ChangeType.UPDATED, uniqueId, resultId, now);
        return resultId;
      } catch (DataIntegrityViolationException ex) {
        if (retry == getMaxRetries()) {
          throw ex;
        }
      } catch (DataAccessException ex) {
        throw fixSQLExceptionCause(ex);
      }
    }
  }

  /**
   * Removes data points.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param fromDateInclusive  the start date to remove from, not null
   * @param toDateInclusive  the end date to remove to, not null
   * @param now  the current instant, not null
   * @return the unique identifier, not null
   */
  protected UniqueId removeDataPoints(UniqueId uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive, Instant now) {
    final Long docOid = extractOid(uniqueId);
    // query dates to remove
    final DbMapSqlParameterSource queryArgs = new DbMapSqlParameterSource()
      .addValue("doc_oid", docOid)
      .addValue("start_date", DbDateUtils.toSqlDateNullFarPast(fromDateInclusive))
      .addValue("end_date", DbDateUtils.toSqlDateNullFarFuture(toDateInclusive));
    final String sqlRemove = getExtSqlBundle().getSql("SelectRemoveDataPoints");
    final List<Map<String, Object>> dates = getJdbcTemplate().queryForList(sqlRemove, queryArgs);
    // insert new rows to remove them
    final Timestamp nowTS = DbDateUtils.toSqlTimestamp(now);
    final List<DbMapSqlParameterSource> argsList = new ArrayList<DbMapSqlParameterSource>();
    for (Map<String, Object> date : dates) {
      final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
        .addValue("doc_oid", docOid)
        .addValue("point_date", date.get("POINT_DATE"))
        .addValue("corr_instant", nowTS)
        .addValue("point_value", null, Types.DOUBLE);
      argsList.add(args);
    }
    final String sqlInsert = getExtSqlBundle().getSql("InsertCorrectDataPoint");
    getJdbcTemplate().batchUpdate(sqlInsert, argsList.toArray(new DbMapSqlParameterSource[argsList.size()]));
    return resolveObjectId(uniqueId, VersionCorrection.of(now, now));
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a unique identifier.
   * 
   * @param oid  the object identifier
   * @param verInstant  the version instant, not null
   * @param corrInstant  the correction instant, not null
   * @return the unique identifier
   */
  protected UniqueId createTimeSeriesUniqueId(long oid, Instant verInstant, Instant corrInstant) {
    String oidStr = DATA_POINT_PREFIX + oid;
    Duration dur = Duration.between(verInstant, corrInstant);
    String verStr = verInstant.toString() + dur.toString();
    return UniqueId.of(getUniqueIdScheme(), oidStr, verStr);
  }

  /**
   * Extracts the object row id from the object identifier.
   * 
   * @param objectId  the object identifier, not null
   * @return the date, null if no point date
   */
  @Override
  protected long extractOid(ObjectIdentifiable objectId) {
    String value = objectId.getObjectId().getValue();
    if (value.startsWith(DATA_POINT_PREFIX)) {
      value = value.substring(DATA_POINT_PREFIX.length());
    }
    try {
      return Long.parseLong(value);
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("UniqueId is not from this master (non-numeric object id): " + objectId, ex);
    }
  }

  /**
   * Extracts the instants from the unique identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the instants, version, correction, not null
   */
  protected VersionCorrection extractTimeSeriesInstants(UniqueId uniqueId) {
    try {
      int pos = uniqueId.getVersion().indexOf('P');
      String verStr = uniqueId.getVersion().substring(0, pos);
      String corrStr = uniqueId.getVersion().substring(pos);
      Instant ver = OffsetDateTime.parse(verStr).toInstant();
      Instant corr = ver.plus(Duration.parse(corrStr));
      return VersionCorrection.of(ver, corr);
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("UniqueId is not from this master (invalid version): " + uniqueId, ex);
    }
  }

  @Override
  protected long extractRowId(UniqueId uniqueId) {
    int pos = uniqueId.getVersion().indexOf('P');
    if (pos < 0) {
      return super.extractRowId(uniqueId);
    }
    VersionCorrection vc = extractTimeSeriesInstants(uniqueId);
    HistoricalTimeSeriesInfoDocument doc = get(uniqueId.getObjectId(), vc);  // not very efficient, but works
    return super.extractRowId(doc.getUniqueId());
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves an object identifier to a unique identifier.
   * 
   * @param objectId  the time-series object identifier, not null
   * @param versionCorrection  the version-correction locator to search at, not null
   * @return the time-series, not null
   */
  protected UniqueId resolveObjectId(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    checkScheme(objectId);
    final long oid = extractOid(objectId);
    versionCorrection = versionCorrection.withLatestFixed(now());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("doc_oid", oid)
      .addTimestamp("version_as_of_instant", versionCorrection.getVersionAsOf())
      .addTimestamp("corrected_to_instant", versionCorrection.getCorrectedTo());
    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate().getNamedParameterJdbcOperations();
    final UniqueIdExtractor extractor = new UniqueIdExtractor(oid);
    final String sql = getExtSqlBundle().getSql("SelectUniqueIdByVersionCorrection", args);
    final UniqueId uniqueId = namedJdbc.query(sql, args, extractor);
    if (uniqueId == null) {
      throw new DataNotFoundException("Unable to find time-series: " + objectId.getObjectId());
    }
    return uniqueId;
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a HistoricalTimeSeriesInfoDocument.
   */
  protected final class HistoricalTimeSeriesDocumentExtractor implements ResultSetExtractor<List<HistoricalTimeSeriesInfoDocument>> {
    private long _lastDocId = -1;
    private ManageableHistoricalTimeSeriesInfo _info;
    private List<HistoricalTimeSeriesInfoDocument> _documents = new ArrayList<HistoricalTimeSeriesInfoDocument>();

    @Override
    public List<HistoricalTimeSeriesInfoDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long docId = rs.getLong("DOC_ID");
        if (_lastDocId != docId) {
          _lastDocId = docId;
          buildHistoricalTimeSeries(rs, docId);
        }
        final String idScheme = rs.getString("KEY_SCHEME");
        final String idValue = rs.getString("KEY_VALUE");
        final LocalDate validFrom = DbDateUtils.fromSqlDateNullFarPast(rs.getDate("KEY_VALID_FROM"));
        final LocalDate validTo = DbDateUtils.fromSqlDateNullFarFuture(rs.getDate("KEY_VALID_TO"));
        if (idScheme != null && idValue != null) {
          ExternalIdWithDates id = ExternalIdWithDates.of(ExternalId.of(idScheme, idValue), validFrom, validTo);
          _info.setExternalIdBundle(_info.getExternalIdBundle().withExternalId(id));
        }
      }
      return _documents;
    }

    private void buildHistoricalTimeSeries(final ResultSet rs, final long docId) throws SQLException {
      final long docOid = rs.getLong("DOC_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      final String name = rs.getString("NAME");
      final String dataField = rs.getString("DATA_FIELD");
      final String dataSource = rs.getString("DATA_SOURCE");
      final String dataProvider = rs.getString("DATA_PROVIDER");
      final String observationTime = rs.getString("OBSERVATION_TIME");
      
      UniqueId uniqueId = createUniqueId(docOid, docId);
      _info = new ManageableHistoricalTimeSeriesInfo();
      _info.setUniqueId(uniqueId);
      _info.setName(name);
      _info.setDataField(dataField);
      _info.setDataSource(dataSource);
      _info.setDataProvider(dataProvider);
      _info.setObservationTime(observationTime);
      _info.setExternalIdBundle(ExternalIdBundleWithDates.EMPTY);
      _info.setTimeSeriesObjectId(uniqueId.getObjectId().withValue(DATA_POINT_PREFIX + uniqueId.getValue()));
      
      HistoricalTimeSeriesInfoDocument doc = new HistoricalTimeSeriesInfoDocument(_info);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      _documents.add(doc);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a LocalDateDoubleTimeSeries.
   */
  protected final class DataPointsExtractor implements ResultSetExtractor<LocalDateDoubleTimeSeries> {
    @Override
    public LocalDateDoubleTimeSeries extractData(final ResultSet rs) throws SQLException, DataAccessException {
      final List<LocalDate> dates = new ArrayList<LocalDate>(256);
      final List<Double> values = new ArrayList<Double>(256);
      LocalDate last = null;
      while (rs.next()) {
        LocalDate date = DbDateUtils.fromSqlDateAllowNull(rs.getDate("POINT_DATE"));
        if (date.equals(last) == false) {
          last = date;
          Double value = (Double) rs.getObject("POINT_VALUE");
          if (value != null) {
            dates.add(date);
            values.add(value);
          }
        }
      }
      return new ArrayLocalDateDoubleTimeSeries(dates, values);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a UniqueId.
   */
  protected final class UniqueIdExtractor implements ResultSetExtractor<UniqueId> {
    private final long _objectId;
    public UniqueIdExtractor(final long objectId) {
      _objectId = objectId;
    }
    @Override
    public UniqueId extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        Timestamp ver = rs.getTimestamp("max_ver_instant");
        Timestamp corr = rs.getTimestamp("max_corr_instant");
        if (ver == null) {
          ver = rs.getTimestamp("ver_from_instant");
          corr = rs.getTimestamp("corr_from_instant");
        }
        Instant verInstant = DbDateUtils.fromSqlTimestamp(ver);
        Instant corrInstant = (corr != null ? DbDateUtils.fromSqlTimestamp(corr) : verInstant);
        return createTimeSeriesUniqueId(_objectId, verInstant, corrInstant);
      }
      return null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a ManageableHistoricalTimeSeries.
   */
  protected final class ManageableHTSExtractor implements ResultSetExtractor<ManageableHistoricalTimeSeries> {
    private final long _objectId;
    public ManageableHTSExtractor(final long objectId) {
      _objectId = objectId;
    }
    @Override
    public ManageableHistoricalTimeSeries extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        Timestamp ver = rs.getTimestamp("max_ver_instant");
        Timestamp corr = rs.getTimestamp("max_corr_instant");
        if (ver == null) {
          ver = rs.getTimestamp("ver_from_instant");
          corr = rs.getTimestamp("corr_from_instant");
        }
        Instant verInstant = DbDateUtils.fromSqlTimestamp(ver);
        Instant corrInstant = (corr != null ? DbDateUtils.fromSqlTimestamp(corr) : verInstant);
        ManageableHistoricalTimeSeries hts = new ManageableHistoricalTimeSeries();
        hts.setUniqueId(createTimeSeriesUniqueId(_objectId, verInstant, corrInstant));
        hts.setVersionInstant(verInstant);
        hts.setCorrectionInstant(corrInstant);
        
//        hts.setEarliestDate(DbDateUtils.fromSqlDateAllowNull(rs.getDate("min_point_date")));
//        hts.setLatestDate(DbDateUtils.fromSqlDateAllowNull(rs.getDate("max_point_date")));        
//        hts.setEarliestValue(rs.getDouble("earliest_point_value"));
//        hts.setLatestValue(rs.getDouble("latest_point_value"));
        
        return hts;
      }
      return null;
    }
  }


}
