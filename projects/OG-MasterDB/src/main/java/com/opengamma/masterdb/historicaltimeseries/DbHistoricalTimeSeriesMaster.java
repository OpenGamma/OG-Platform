/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.opengamma.DataDuplicationException;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
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
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.paging.Paging;

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
 * <p>
 * The SQL is stored externally in {@code DbHistoricalTimeSeriesMaster.elsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbHistoricalTimeSeriesMaster-MySpecialDB.elsql}.
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
   * Worker.
   */
  private final DbHistoricalTimeSeriesDataPointsWorker _dataPointsWorker;

  // -----------------------------------------------------------------
  // TIMERS FOR METRICS GATHERING
  // By default these do nothing. Registration will replace them
  // so that they actually do something.
  // -----------------------------------------------------------------
  private Timer _metaDataTimer = new Timer();
  private Timer _insertTimer = new Timer();
  private Timer _htsAddTimer = new Timer();
  private Timer _getTimeSeriesTimer = new Timer();
  private Timer _updatePointsTimer = new Timer();
  private Timer _correctPointsTimer = new Timer();
  private Timer _removePointsTimer = new Timer();
  
  /**
   * Creates an instance.
   *
   * @param dbConnector  the database connector, not null
   */
  public DbHistoricalTimeSeriesMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbHistoricalTimeSeriesMaster.class));
    _nameTable = new NamedDimensionDbTable(dbConnector, "name", "hts_name", "hts_dimension_seq");
    _dataFieldTable = new NamedDimensionDbTable(dbConnector, "data_field", "hts_data_field", "hts_dimension_seq");
    _dataSourceTable = new NamedDimensionDbTable(dbConnector, "data_source", "hts_data_source", "hts_dimension_seq");
    _dataProviderTable = new NamedDimensionDbTable(dbConnector, "data_provider", "hts_data_provider", "hts_dimension_seq");
    _observationTimeTable = new NamedDimensionDbTable(dbConnector, "observation_time", "hts_observation_time", "hts_dimension_seq");
    _dataPointsWorker = new DbHistoricalTimeSeriesDataPointsWorker(this);
  }

  @Override
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailedRegistry, String namePrefix) {
    super.registerMetrics(summaryRegistry, detailedRegistry, namePrefix);
    _metaDataTimer = summaryRegistry.timer(namePrefix + ".metaData");
    _insertTimer = summaryRegistry.timer(namePrefix + ".insert");
    _htsAddTimer = summaryRegistry.timer(namePrefix + ".htsAdd");
    _getTimeSeriesTimer = summaryRegistry.timer(namePrefix + ".getTimeSeries");
    _updatePointsTimer = summaryRegistry.timer(namePrefix + ".updatePoints");
    _correctPointsTimer = summaryRegistry.timer(namePrefix + ".correctPoints");
    _removePointsTimer = summaryRegistry.timer(namePrefix + ".removePoints");
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

  /**
   * Gets the data points worker.
   *
   * @return the worker, not null
   */
  protected DbHistoricalTimeSeriesDataPointsWorker getDataPointsWorker() {
    return _dataPointsWorker;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoMetaDataResult metaData(HistoricalTimeSeriesInfoMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    try (Timer.Context context = _metaDataTimer.time()) {
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
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoSearchResult search(final HistoricalTimeSeriesInfoSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    final HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult(vc);
    
    final List<ObjectId> objectIds = request.getObjectIds();
    final ExternalIdSearch externalIdSearch = request.getExternalIdSearch();
    if ((objectIds != null && objectIds.size() == 0) ||
      (ExternalIdSearch.canMatch(externalIdSearch) == false)) {
      result.setPaging(Paging.of(request.getPagingRequest(), 0));
      return result;
    }
    
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
    String[] sql = {getElSqlBundle().getSql("Search", args), getElSqlBundle().getSql("SearchCount", args)};
    doSearch(request.getPagingRequest(), sql, args, new HistoricalTimeSeriesDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL to find all the ids for a single bundle.
   * <p>
   * This is too complex for the elsql mechanism.
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
    checkScheme(objectId.getObjectId());
    return doGetByOidInstants(objectId, versionCorrection, new HistoricalTimeSeriesDocumentExtractor(), "HistoricalTimeSeries");
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoHistoryResult history(final HistoricalTimeSeriesInfoHistoryRequest request) {
    return doHistory(request, new HistoricalTimeSeriesInfoHistoryResult(), new HistoricalTimeSeriesDocumentExtractor());
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument add(HistoricalTimeSeriesInfoDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getInfo(), "document.info");
    ArgumentChecker.notNull(document.getInfo().getName(), "document.info.name");
    ArgumentChecker.notNull(document.getInfo().getDataField(), "document.info.dataField");
    ArgumentChecker.notNull(document.getInfo().getDataSource(), "document.info.dataSource");
    ArgumentChecker.notNull(document.getInfo().getDataProvider(), "document.info.dataProvider");
    ArgumentChecker.notNull(document.getInfo().getObservationTime(), "document.info.observationTime");
    ArgumentChecker.notNull(document.getInfo().getExternalIdBundle(), "document.info.externalIdBundle");
    
    try (Timer.Context context = _htsAddTimer.time()) {
      HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
      request.setDataField(document.getInfo().getDataField());
      request.setDataSource(document.getInfo().getDataSource());
      request.setDataProvider(document.getInfo().getDataProvider());
      request.setObservationTime(document.getInfo().getObservationTime());
      request.setExternalIdSearch(new ExternalIdSearch(document.getInfo().getExternalIdBundle().toBundle(), ExternalIdSearchType.EXACT));
      HistoricalTimeSeriesInfoSearchResult result = search(request);
      if (result.getDocuments().size() > 0) {
        throw new DataDuplicationException("Unable to add as similar row exists already: " + result.getDocuments().get(0).getObjectId() + " matched " + request);
      }
      return super.add(document);
    }
  }

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
    ArgumentChecker.notNull(document.getInfo().getExternalIdBundle(), "document.info.externalIdBundle");
    
    try (Timer.Context context = _insertTimer.time()) {
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
      final String sqlSelectIdKey = getElSqlBundle().getSql("SelectIdKey");
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
      final String sqlDoc = getElSqlBundle().getSql("Insert", docArgs);
      final String sqlIdKey = getElSqlBundle().getSql("InsertIdKey");
      final String sqlDoc2IdKey = getElSqlBundle().getSql("InsertDoc2IdKey");
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
  }

  //-------------------------------------------------------------------------

  public ManageableHistoricalTimeSeries getTimeSeries(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    checkScheme(uniqueId);

    final VersionCorrection vc;
    if (uniqueId.isVersioned() && uniqueId.getValue().startsWith(DATA_POINT_PREFIX)) {
      vc = extractTimeSeriesInstants(uniqueId);
    } else {
      vc = VersionCorrection.LATEST;
    }
    return getTimeSeries(uniqueId.getObjectId(), vc);
  }

  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    HistoricalTimeSeriesGetFilter filter = HistoricalTimeSeriesGetFilter.ofRange(null, null);
    return getTimeSeries(objectId, versionCorrection, filter);
  }

  public ManageableHistoricalTimeSeries getTimeSeries(UniqueId uniqueId, HistoricalTimeSeriesGetFilter filter) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    checkScheme(uniqueId);

    final VersionCorrection vc;
    if (uniqueId.isVersioned() && uniqueId.getValue().startsWith(DATA_POINT_PREFIX)) {
      vc = extractTimeSeriesInstants(uniqueId);
    } else {
      vc = VersionCorrection.LATEST;
    }
    return getTimeSeries(uniqueId.getObjectId(), vc, filter);
  }

  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId, VersionCorrection versionCorrection, HistoricalTimeSeriesGetFilter filter) {
    try (Timer.Context context = _getTimeSeriesTimer.time()) {
      return getDataPointsWorker().getTimeSeries(objectId, versionCorrection, filter);
    }
  }

  //-------------------------------------------------------------------------

  @Override
  public UniqueId updateTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDateDoubleTimeSeries series) {
    try (Timer.Context context = _updatePointsTimer.time()) {
      return getDataPointsWorker().updateTimeSeriesDataPoints(objectId, series);
    }
  }

  @Override
  public UniqueId correctTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDateDoubleTimeSeries series) {
    try (Timer.Context context = _correctPointsTimer.time()) {
      return getDataPointsWorker().correctTimeSeriesDataPoints(objectId, series);
    }
  }

  @Override
  public UniqueId removeTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDate fromDateInclusive, final LocalDate toDateInclusive) {
    try (Timer.Context context = _removePointsTimer.time()) {
      return getDataPointsWorker().removeTimeSeriesDataPoints(objectId, fromDateInclusive, toDateInclusive);
    }
  }

  //-------------------------------------------------------------------------

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
      Instant ver = Instant.parse(verStr);
      Instant corr = ver.plus(Duration.parse(corrStr));
      return VersionCorrection.of(ver, corr);
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("UniqueId is not from this master (invalid version): " + uniqueId, ex);
    }
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
        if (idScheme != null && idValue != null) {
          final LocalDate validFrom = DbDateUtils.fromSqlDateNullFarPast(rs.getDate("KEY_VALID_FROM"));
          final LocalDate validTo = DbDateUtils.fromSqlDateNullFarFuture(rs.getDate("KEY_VALID_TO"));
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

  @Override
  protected AbstractHistoryResult<HistoricalTimeSeriesInfoDocument> historyByVersionsCorrections(AbstractHistoryRequest request) {
    HistoricalTimeSeriesInfoHistoryRequest historyRequest = new HistoricalTimeSeriesInfoHistoryRequest();
    historyRequest.setCorrectionsFromInstant(request.getCorrectionsFromInstant());
    historyRequest.setCorrectionsToInstant(request.getCorrectionsToInstant());
    historyRequest.setVersionsFromInstant(request.getVersionsFromInstant());
    historyRequest.setVersionsToInstant(request.getVersionsToInstant());
    historyRequest.setObjectId(request.getObjectId());
    return history(historyRequest);
  }

}
