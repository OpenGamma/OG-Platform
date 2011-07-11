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

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierWithDates;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.db.Paging;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A time-series master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the time-series master using an SQL database.
 * Full details of the API are in {@link HistoricalTimeSeriesMaster}.
 * <p>
 * This class uses SQL via JDBC. The SQL may be changed by subclassing the relevant methods.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbHistoricalTimeSeriesMaster extends AbstractDocumentDbMaster<HistoricalTimeSeriesInfoDocument> implements HistoricalTimeSeriesMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMaster.class);

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbHts";

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
        "nm.name AS name, " +
        "df.name AS data_field, " +
        "ds.name AS data_source, " +
        "dp.name AS data_provider, " +
        "ot.name AS observation_time, " +
        "i.key_scheme AS key_scheme, " +
        "i.key_value AS key_value, " +
        "di.valid_from AS key_valid_from, " +
        "di.valid_to AS key_valid_to ";
  /**
   * SQL from.
   */
  protected static final String FROM =
      "FROM hts_document main " +
        "INNER JOIN hts_name nm ON main.name_id = nm.id  " +
        "INNER JOIN hts_data_field df ON main.data_field_id = df.id  " +
        "INNER JOIN hts_data_source ds ON main.data_source_id = ds.id  " +
        "INNER JOIN hts_data_provider dp ON main.data_provider_id = dp.id  " +
        "INNER JOIN hts_observation_time ot ON main.observation_time_id = ot.id  " +
        "LEFT JOIN hts_doc2idkey di ON (di.doc_id = main.id) " +
        "LEFT JOIN hts_idkey i ON (di.idkey_id = i.id) ";

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
   * @param dbSource  the database source combining all configuration, not null
   */
  public DbHistoricalTimeSeriesMaster(final DbSource dbSource) {
    super(dbSource, IDENTIFIER_SCHEME_DEFAULT);
    _nameTable = new NamedDimensionDbTable(dbSource, "name", "hts_name", "hts_dimension_seq");
    _dataFieldTable = new NamedDimensionDbTable(dbSource, "data_field", "hts_data_field", "hts_dimension_seq");
    _dataSourceTable = new NamedDimensionDbTable(dbSource, "data_source", "hts_data_source", "hts_dimension_seq");
    _dataProviderTable = new NamedDimensionDbTable(dbSource, "data_provider", "hts_data_provider", "hts_dimension_seq");
    _observationTimeTable = new NamedDimensionDbTable(dbSource, "observation_time", "hts_observation_time", "hts_dimension_seq");
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
  public HistoricalTimeSeriesInfoSearchResult search(final HistoricalTimeSeriesInfoSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult();
    if ((request.getInfoIds() != null && request.getInfoIds().size() == 0) ||
        (IdentifierSearch.canMatch(request.getIdentifierKeys()) == false)) {
      result.setPaging(Paging.of(request.getPagingRequest(), 0));
      return result;
    }
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
      .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
      .addValueNullIgnored("name", getDbHelper().sqlWildcardAdjustValue(request.getName()))
      .addValueNullIgnored("data_field", getDbHelper().sqlWildcardAdjustValue(request.getDataField()))
      .addValueNullIgnored("data_source", getDbHelper().sqlWildcardAdjustValue(request.getDataSource()))
      .addValueNullIgnored("data_provider", getDbHelper().sqlWildcardAdjustValue(request.getDataProvider()))
      .addValueNullIgnored("observation_time", getDbHelper().sqlWildcardAdjustValue(request.getObservationTime()))
      .addDateNullIgnored("id_validity_date", request.getIdentifierValidityDate())  // TODO
      .addValueNullIgnored("key_value", getDbHelper().sqlWildcardAdjustValue(request.getIdentifierValue()));
    if (request.getIdentifierKeys() != null) {
      int i = 0;
      for (Identifier id : request.getIdentifierKeys()) {
        args.addValue("key_scheme" + i, id.getScheme().getName());
        args.addValue("key_value" + i, id.getValue());
        i++;
      }
    }
    searchWithPaging(request.getPagingRequest(), sqlSearchHistoricalTimeSeries(request), args, new HistoricalTimeSeriesDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL to search for documents.
   * 
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchHistoricalTimeSeries(final HistoricalTimeSeriesInfoSearchRequest request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
                "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
    if (request.getName() != null) {
      where += "AND name_id IN (" + getNameTable().sqlSelectSearch(request.getName()) + ") ";
    }
    if (request.getDataField() != null) {
      where += "AND data_field_id IN (" + getDataFieldTable().sqlSelectSearch(request.getDataField()) + ") ";
    }
    if (request.getDataSource() != null) {
      where += "AND data_source_id IN (" + getDataSourceTable().sqlSelectSearch(request.getDataSource()) + ") ";
    }
    if (request.getDataProvider() != null) {
      where += "AND data_provider_id IN (" + getDataProviderTable().sqlSelectSearch(request.getDataProvider()) + ") ";
    }
    if (request.getObservationTime() != null) {
      where += "AND observation_time_id IN (" + getObservationTimeTable().sqlSelectSearch(request.getObservationTime()) + ") ";
    }
    if (request.getInfoIds() != null) {
      StringBuilder buf = new StringBuilder(request.getInfoIds().size() * 10);
      for (ObjectIdentifier objectId : request.getInfoIds()) {
        checkScheme(objectId);
        buf.append(extractOid(objectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      where += "AND oid IN (" + buf + ") ";
    }
    if (request.getIdentifierKeys() != null && request.getIdentifierKeys().size() > 0) {
      where += sqlSelectMatchingHistoricalTimeSeriesKeys(request.getIdentifierKeys());
    }
    if (request.getIdentifierValue() != null) {
      where += sqlSelectIdentifierValue(request.getIdentifierValue());
    }
    where += sqlAdditionalWhere();
    
    String selectFromWhereInner = "SELECT id FROM hts_document " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY id ", request.getPagingRequest());
    String search = sqlSelectFrom() + "WHERE main.id IN (" + inner + ") ORDER BY main.id" + sqlAdditionalOrderBy(false);
    String count = "SELECT COUNT(*) FROM hts_document " + where;
    return new String[] {search, count};
  }

  /**
   * Gets the SQL to match identifier value.
   * 
   * @param identifierValue the identifier value, not null
   * @return the SQL, not null
   */
  protected String sqlSelectIdentifierValue(String identifierValue) {
    String select = "SELECT DISTINCT doc_id " +
        "FROM hts_doc2idkey, hts_document main " +
        "WHERE doc_id = main.id " +
        "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
        "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
        "AND idkey_id IN ( SELECT id FROM hts_idkey WHERE " + getDbHelper().sqlWildcardQuery("UPPER(key_value) ", "UPPER(:key_value)", identifierValue) + ") ";
    return "AND id IN (" + select + ") ";
  }

  /**
   * Gets the SQL to match the {@code IdentifierSearch}.
   * 
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingHistoricalTimeSeriesKeys(final IdentifierSearch idSearch) {
    switch (idSearch.getSearchType()) {
      case EXACT:
        return "AND id IN (" + sqlSelectMatchingHistoricalTimeSeriesKeysExact(idSearch) + ") ";
      case ALL:
        return "AND id IN (" + sqlSelectMatchingHistoricalTimeSeriesKeysAll(idSearch) + ") ";
      case ANY:
        return "AND id IN (" + sqlSelectMatchingHistoricalTimeSeriesKeysAny(idSearch) + ") ";
      case NONE:
        return "AND id NOT IN (" + sqlSelectMatchingHistoricalTimeSeriesKeysAny(idSearch) + ") ";
    }
    throw new UnsupportedOperationException("Search type is not supported: " + idSearch.getSearchType());
  }

  /**
   * Gets the SQL to find all the series matching.
   * 
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingHistoricalTimeSeriesKeysExact(final IdentifierSearch idSearch) {
    // compare size of all matched to size in total
    // filter by dates to reduce search set
    String a = "SELECT doc_id AS matched_doc_id, COUNT(doc_id) AS matched_count " +
      "FROM hts_doc2idkey, hts_document main " +
      "WHERE doc_id = main.id " +
      "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
      "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
      "AND idkey_id IN (" + sqlSelectMatchingHistoricalTimeSeriesKeysOr(idSearch) + ") " +
      "GROUP BY doc_id " +
      "HAVING COUNT(doc_id) >= " + idSearch.size() + " ";
    String b = "SELECT doc_id AS total_doc_id, COUNT(doc_id) AS total_count " +
      "FROM hts_doc2idkey, hts_document main " +
      "WHERE doc_id = main.id " +
      "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
      "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
      "GROUP BY doc_id ";
    String select = "SELECT matched_doc_id AS doc_id " +
      "FROM (" + a + ") AS a, (" + b + ") AS b " +
      "WHERE matched_doc_id = total_doc_id " +
        "AND matched_count = total_count ";
    return select;
  }

  /**
   * Gets the SQL to find all the series matching.
   * 
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingHistoricalTimeSeriesKeysAll(final IdentifierSearch idSearch) {
    // only return doc_id when all requested ids match (having count >= size)
    // filter by dates to reduce search set
    String select = "SELECT doc_id " +
      "FROM hts_doc2idkey, hts_document main " +
      "WHERE doc_id = main.id " +
      "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
      "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
      "AND idkey_id IN (" + sqlSelectMatchingHistoricalTimeSeriesKeysOr(idSearch) + ") " +
      "GROUP BY doc_id " +
      "HAVING COUNT(doc_id) >= " + idSearch.size() + " ";
    return select;
  }

  /**
   * Gets the SQL to find all the series matching any identifier.
   * 
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingHistoricalTimeSeriesKeysAny(final IdentifierSearch idSearch) {
    // optimized search for commons case of individual ORs
    // filter by dates to reduce search set
    String select = "SELECT DISTINCT doc_id " +
      "FROM hts_doc2idkey, hts_document main " +
      "WHERE doc_id = main.id " +
      "AND main.ver_from_instant <= :version_as_of_instant AND main.ver_to_instant > :version_as_of_instant " +
      "AND main.corr_from_instant <= :corrected_to_instant AND main.corr_to_instant > :corrected_to_instant " +
      "AND idkey_id IN (" + sqlSelectMatchingHistoricalTimeSeriesKeysOr(idSearch) + ") ";
    return select;
  }

  /**
   * Gets the SQL to find all the ids for a single bundle.
   * 
   * @param idSearch  the identifier search, not null
   * @return the SQL, not null
   */
  protected String sqlSelectMatchingHistoricalTimeSeriesKeysOr(final IdentifierSearch idSearch) {
    String select = "SELECT id FROM hts_idkey ";
    for (int i = 0; i < idSearch.size(); i++) {
      select += (i == 0 ? "WHERE " : "OR ");
      select += "(key_scheme = :key_scheme" + i + " AND key_value = :key_value" + i + ") ";
    }
    return select;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument get(UniqueIdentifier uniqueId) {
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
    final DbMapSqlParameterSource seriesArgs = new DbMapSqlParameterSource()
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
    for (IdentifierWithDates id : info.getIdentifiers()) {
      final DbMapSqlParameterSource assocArgs = new DbMapSqlParameterSource()
        .addValue("doc_id", docId)
        .addValue("key_scheme", id.getIdentityKey().getScheme().getName())
        .addValue("key_value", id.getIdentityKey().getValue())
        .addValue("valid_from", DbDateUtils.toSqlDateNullFarPast(id.getValidFrom()))
        .addValue("valid_to", DbDateUtils.toSqlDateNullFarFuture(id.getValidTo()));
      assocList.add(assocArgs);
      if (getJdbcTemplate().queryForList(sqlSelectIdKey(), assocArgs).isEmpty()) {
        // select avoids creating unecessary id, but id may still not be used
        final long idKeyId = nextId("hts_idkey_seq");
        final DbMapSqlParameterSource idkeyArgs = new DbMapSqlParameterSource()
          .addValue("idkey_id", idKeyId)
          .addValue("key_scheme", id.getIdentityKey().getScheme().getName())
          .addValue("key_value", id.getIdentityKey().getValue());
        idKeyList.add(idkeyArgs);
      }
    }
    getJdbcTemplate().update(sqlInsertHistoricalTimeSeries(), seriesArgs);
    getJdbcTemplate().batchUpdate(sqlInsertIdKey(), idKeyList.toArray(new DbMapSqlParameterSource[idKeyList.size()]));
    getJdbcTemplate().batchUpdate(sqlInsertHtsIdKey(), assocList.toArray(new DbMapSqlParameterSource[assocList.size()]));
    // set the uniqueId
    final UniqueIdentifier uniqueId = createUniqueIdentifier(docOid, docId);
    info.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    return document;
  }

  /**
   * Gets the SQL for inserting a document.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertHistoricalTimeSeries() {
    return "INSERT INTO hts_document " +
              "(id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, name_id, " +
              "data_field_id, data_source_id, data_provider_id, observation_time_id) " +
            "VALUES " +
              "(:doc_id, :doc_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :name_id, " +
              ":data_field_id, :data_source_id, :data_provider_id, :observation_time_id)";
  }

  /**
   * Gets the SQL for inserting an hts-idkey association.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertHtsIdKey() {
    return "INSERT INTO hts_doc2idkey " +
              "(doc_id, idkey_id, valid_from, valid_to) " +
            "VALUES " +
              "(:doc_id, (" + sqlSelectIdKey() + "), :valid_from, :valid_to)";
  }

  /**
   * Gets the SQL for selecting an idkey.
   * 
   * @return the SQL, not null
   */
  protected String sqlSelectIdKey() {
    return "SELECT id FROM hts_idkey WHERE key_scheme = :key_scheme AND key_value = :key_value";
  }

  /**
   * Gets the SQL for inserting an idkey.
   * 
   * @return the SQL, not null
   */
  protected String sqlInsertIdKey() {
    return "INSERT INTO hts_idkey (id, key_scheme, key_value) " +
            "VALUES (:idkey_id, :key_scheme, :key_value)";
  }

//  //-------------------------------------------------------------------------
//  /**
//   * Gets the data points.
//   * 
//   * @param uniqueId  the data points unique identifier, not null
//   * @param fromDateInclusive  the inclusive start date of the points to remove, null for far past
//   * @param toDateInclusive  the inclusive end date of the points to remove, null for far future
//   * @return the time-series, not null
//   */
//  protected HistoricalTimeSeries getDataPoints(
//      UniqueIdentifier uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
//    ArgumentChecker.notNull(uniqueId, "uniqueId");
//    uniqueId = resolveUniqueId(uniqueId);
//    final long oid = extractOid(uniqueId);
//    final long rowId = extractRowId(uniqueId);
//    final LocalDate pointDate = extractPointDate(uniqueId);
//    if (pointDate == null) {
//      return new HistoricalTimeSeriesImpl(uniqueId, new ArrayLocalDateDoubleTimeSeries());
//    }
//    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
//      .addValue("doc_oid", oid)
//      .addValue("doc_id", rowId)
//      .addDate("point_date", pointDate)
//      .addValue("start_date", DbDateUtils.toSqlDateNullFarPast(fromDateInclusive))
//      .addValue("end_date", DbDateUtils.toSqlDateNullFarFuture(toDateInclusive));
//    final NamedParameterJdbcOperations namedJdbc = getDbSource().getJdbcTemplate().getNamedParameterJdbcOperations();
//    LocalDateDoubleTimeSeries series = namedJdbc.query(sqlSelectDataPointsByUniqueId(), args, new DataPointsExtractor());
//    return new HistoricalTimeSeriesImpl(uniqueId, series);
//  }
//
//  /**
//   * Gets the SQL to load the data points.
//   * 
//   * @return the SQL, not null
//   */
//  protected String sqlSelectDataPointsByUniqueId() {
//    // select the original data points up to the specified date
//    // filtered by the start/end date required
//    String selectPoint = "SELECT point_date, instant, point_value " +
//        "FROM hts_point " +
//        "WHERE doc_oid = :doc_oid " +
//        "AND point_date <= :point_date " +
//        "AND point_date >= :start_date " +
//        "AND point_date <= :end_date ";
//    // select the corrections up to the specified date and before the correction instant
//    // filtered by the start/end date required
//    String selectCorrect = "SELECT point_date, instant, point_value " +
//        "FROM hts_correct " +
//        "WHERE doc_oid = :doc_oid " +
//        "AND point_date <= :point_date " +
//        "AND instant <= (SELECT corr_from_instant FROM hts_document WHERE id = :doc_id) " +
//        "AND point_date >= :start_date " +
//        "AND point_date <= :end_date ";
//    // result is the union, sorted to ensure the latest correction for each point comes first
//    return selectPoint + "UNION ALL " + selectCorrect + "ORDER BY point_date, instant DESC ";
//  }
//
//  //-------------------------------------------------------------------------
//  /**
//   * Resolves an object identifier to a unique identifier.
//   * 
//   * @param uniqueId  the time-series unique identifier, not null
//   * @return the time-series, not null
//   */
//  protected UniqueIdentifier resolveUniqueId(UniqueIdentifier uniqueId) {
//    if (uniqueId.isVersioned() && uniqueId.getVersion().contains("D")) {
//      return uniqueId;
//    }
//    return resolveObjectId(uniqueId, VersionCorrection.LATEST);
//  }
//
//  /**
//   * Resolves an object identifier to a unique identifier.
//   * 
//   * @param objectId  the time-series object identifier, not null
//   * @param versionCorrection  the version-correction locator to search at, not null
//   * @return the time-series, not null
//   */
//  protected UniqueIdentifier resolveObjectId(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
//    long oid = extractOid(objectId);
//    versionCorrection = versionCorrection.withLatestFixed(now());
//    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
//      .addValue("doc_oid", oid)
//      .addTimestamp("version_as_of_instant", versionCorrection.getVersionAsOf())
//      .addTimestamp("corrected_to_instant", versionCorrection.getCorrectedTo());
//    final NamedParameterJdbcOperations namedJdbc = getDbSource().getJdbcTemplate().getNamedParameterJdbcOperations();
//    return namedJdbc.query(sqlSelectUniqueIdentifierByVersionCorrection(), args, new UniqueIdentifierExtractor());
//  }
//
//  /**
//   * Gets the SQL to load the data points.
//   * 
//   * @return the SQL, not null
//   */
//  protected String sqlSelectUniqueIdentifierByVersionCorrection() {
//    // select the row id of the document
//    String selectId =
//      "SELECT oid, id " +
//      "FROM hts_document " +
//      "WHERE oid = :doc_oid " +
//      "AND ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
//      "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
//    // select the latest point date before the version instant
//    String selectPoint =
//      "SELECT MAX(point_date) AS max_date " +
//      "FROM hts_point " +
//      "WHERE doc_oid = :doc_oid " +
//      "AND instant <= :version_as_of_instant ";
//    // return a single row with both pieces of information
//    return "SELECT main.oid AS doc_oid, main.id AS doc_id, point.max_date AS max_point_date " +
//      "FROM (" + selectId + ") main, " +
//      "(" + selectPoint + ") point ";
//  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(UniqueIdentifier uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    return null;
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId, VersionCorrection versionCorrection, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    return null;
  }

  @Override
  public UniqueIdentifier addTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series) {
    return null;
  }

  @Override
  public UniqueIdentifier correctTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series) {
    return null;
  }

  @Override
  public UniqueIdentifier removeTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    return null;
  }

//  /**
//   * Gets the data points.
//   * 
//   * @param objectId  the time-series object identifier, not null
//   * @param versionCorrection  the version-correction locator to search at, not null
//   * @param fromDateInclusive  the inclusive start date of the points to remove, null for far past
//   * @param toDateInclusive  the inclusive end date of the points to remove, null for far future
//   * @return the time-series, not null
//   */
//  protected HistoricalTimeSeries getDataPoints(
//      ObjectIdentifiable objectId, VersionCorrection versionCorrection, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
//    ArgumentChecker.notNull(objectId, "objectId");
//    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
//    long oid = extractOid(objectId);
//    versionCorrection = versionCorrection.withLatestFixed(now());
//    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
//      .addValue("doc_oid", oid)
//      .addTimestamp("version_as_of_instant", versionCorrection.getVersionAsOf())
//      .addTimestamp("corrected_to_instant", versionCorrection.getCorrectedTo())
//      .addValue("start_date", DbDateUtils.toSqlDateNullFarPast(fromDateInclusive))
//      .addValue("end_date", DbDateUtils.toSqlDateNullFarPast(toDateInclusive));
//    final NamedParameterJdbcOperations namedJdbc = getDbSource().getJdbcTemplate().getNamedParameterJdbcOperations();
//    LocalDateDoubleTimeSeries series = namedJdbc.query(sqlSelectDataPointsByVersionCorrection(), args, new DataPointsExtractor());
//    UniqueIdentifier uid = namedJdbc.query(sqlSelectDataPointUniqueIdentifierByVersionCorrection(), args, new UniqueIdentifierExtractor());
//    return new HistoricalTimeSeriesImpl(uid, series);
//  }
//
//  /**
//   * Gets the SQL to load the data points.
//   * 
//   * @return the SQL, not null
//   */
//  protected String sqlSelectDataPointsByVersionCorrection() {
//    String select = "SELECT doc_oid, point_date, point_value " +
//        "FROM hts_point " +
//        "WHERE doc_oid = :doc_oid " +
//        "AND ver_instant <= :version_as_of_instant " +
//        "AND corr_instant <= :corrected_to_instant " +
//        "AND point_date >= :start_date " +
//        "AND point_date <= :end_date " +
//        "ORDER BY point_date, corr_instant DESC ";
//    return select;
//  }
//
//  //-------------------------------------------------------------------------
//  @Override
//  public UniqueIdentifier updateDataPoints(final UniqueIdentifier uniqueId, final LocalDateDoubleTimeSeries series) {
//    ArgumentChecker.notNull(uniqueId, "uniqueId");
//    ArgumentChecker.notNull(series, "series");
//    checkScheme(uniqueId);
//    s_logger.debug("update {}", uniqueId);
//    
//    // retry to handle concurrent conflicting inserts
//    for (int retry = 0; true; retry++) {
//      try {
//        ArgumentChecker.isTrue(uniqueId.isVersioned(), "UniqueIdentifier must be versioned");
//        final Instant now = now();
//        UniqueIdentifier resultId = getTransactionTemplate().execute(new TransactionCallback<UniqueIdentifier>() {
//          @Override
//          public UniqueIdentifier doInTransaction(final TransactionStatus status) {
//            // load old row
//            final HistoricalTimeSeriesInfoDocument oldDoc = getCheckLatestVersion(uniqueId);
//            if (series.isEmpty()) {
//              return oldDoc.getUniqueId();
//            }
//            return insertDataPoints(oldDoc.getUniqueId(), series, now);
//          }
//        });
////        changeManager().masterChanged(MasterChangedType.UPDATED, ***uniqueId, resultId, now);
//        return resultId;
//      } catch (DataIntegrityViolationException ex) {
//        if (retry == getMaxRetries()) {
//          throw ex;
//        }
//      }
//    }
//  }
//
//  /**
//   * Inserts the data points.
//   * 
//   * @param objectId  the object identifier, not null
//   * @param series  the time-series data points, not empty, not null
//   * @param now  the current instant, not null
//   * @return the unique identifier, not null
//   */
//  protected UniqueIdentifier insertDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series, Instant now) {
//    final Long docOid = extractOid(objectId);
//    final Timestamp nowTS = DbDateUtils.toSqlTimestamp(now);
//    // the arguments for inserting into the table
//    final List<DbMapSqlParameterSource> argsList = new ArrayList<DbMapSqlParameterSource>();
//    LocalDate date = null;
//    for (Entry<LocalDate, Double> entry : series) {
//      date = entry.getKey();
//      final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
//        .addValue("doc_oid", docOid)
//        .addDate("point_date", date)
//        .addValue("ver_instant", nowTS)
//        .addValue("corr_instant", nowTS)
//        .addValue("point_value", entry.getValue());
//      argsList.add(args);
//    }
//    getJdbcTemplate().batchUpdate(sqlInsertDataPoint(), argsList.toArray(new DbMapSqlParameterSource[argsList.size()]));
//    return null; //createUniqueIdentifier(docOid, date, now);
//  }
//
//  /**
//   * Gets the SQL for inserting a document.
//   * 
//   * @return the SQL, not null
//   */
//  protected String sqlInsertDataPoint() {
//    return "INSERT INTO hts_point " +
//              "(doc_oid, point_date, ver_instant, corr_instant, point_value) " +
//            "VALUES " +
//              "(:doc_oid, :point_date, :ver_instant, :corr_instant, :point_value)";
//  }
//
//  //-------------------------------------------------------------------------
//  @Override
//  public UniqueIdentifier correctDataPoints(final UniqueIdentifier uniqueId, final LocalDateDoubleTimeSeries series) {
//    ArgumentChecker.notNull(uniqueId, "uniqueId");
//    ArgumentChecker.notNull(series, "series");
//    checkScheme(uniqueId);
//    s_logger.debug("update {}", uniqueId);
//    
//    // retry to handle concurrent conflicting inserts
//    for (int retry = 0; true; retry++) {
//      try {
//        ArgumentChecker.isTrue(uniqueId.isVersioned(), "UniqueIdentifier must be versioned");
//        final Instant now = now();
//        UniqueIdentifier resultId = getTransactionTemplate().execute(new TransactionCallback<UniqueIdentifier>() {
//          @Override
//          public UniqueIdentifier doInTransaction(final TransactionStatus status) {
//            // load old row
//            final HistoricalTimeSeriesInfoDocument oldDoc = getCheckLatestCorrection(uniqueId);
//            if (series.isEmpty()) {
//              return oldDoc.getUniqueId();
//            }
//            return correctDataPoints(oldDoc.getUniqueId(), series, now);
//          }
//        });
////        changeManager().masterChanged(MasterChangedType.CORRECTED, ***uniqueId, resultId, now);
//        return resultId;
//      } catch (DataIntegrityViolationException ex) {
//        if (retry == getMaxRetries()) {
//          throw ex;
//        }
//      }
//    }
//  }
//
//  /**
//   * Corrects the data points.
//   * 
//   * @param uniqueId  the unique identifier, not null
//   * @param series  the time-series data points, not empty, not null
//   * @param now  the current instant, not null
//   * @return the unique identifier, not null
//   */
//  protected UniqueIdentifier correctDataPoints(UniqueIdentifier uniqueId, LocalDateDoubleTimeSeries series, Instant now) {
//    final Long docOid = extractOid(uniqueId);
//    final Timestamp nowTS = DbDateUtils.toSqlTimestamp(now);
//    // the arguments for inserting into the table
//    final List<DbMapSqlParameterSource> argsList = new ArrayList<DbMapSqlParameterSource>();
//    LocalDate date = null;
//    for (Entry<LocalDate, Double> entry : series) {
//      date = entry.getKey();
//      final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
//        .addValue("doc_oid", docOid)
//        .addDate("point_date", date)
//        .addValue("corr_instant", nowTS);
//      args.addValue("point_value", entry.getValue(), Types.DOUBLE);
//      argsList.add(args);
//    }
//    getJdbcTemplate().batchUpdate(sqlInsertCorrectDataPoint(), argsList.toArray(new DbMapSqlParameterSource[argsList.size()]));
//    return null; //createUniqueIdentifier(docOid, date, now);
//  }
//
//  /**
//   * Gets the SQL for inserting a document.
//   * 
//   * @return the SQL, not null
//   */
//  protected String sqlInsertCorrectDataPoint() {
//    return "INSERT INTO hts_point " +
//              "(doc_oid, point_date, ver_instant, corr_instant, point_value) " +
//            "VALUES " +
//              "(:doc_oid, :point_date," +
//                "(SELECT ver_instant FROM hts_point WHERE point_date = :point_date AND ver_instant = corr_instant), " +
//              ":corr_instant, :point_value)";
//  }
//
//  @Override
//  public UniqueIdentifier correctRemoveDataPoints(UniqueIdentifier uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
//    return null;
//  }
//
//  //-------------------------------------------------------------------------
//  /**
//   * Creates a unique identifier.
//   * 
//   * @param oid  the object identifier
//   * @param rowId  the row id
//   * @param latestPointDate  the latest date, may be null
//   * @return the unique identifier
//   */
//  protected UniqueIdentifier createUniqueIdentifier(long oid, long rowId, LocalDate latestPointDate) {
//    if (latestPointDate == null) {
//      return UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid), Long.toString(rowId - oid) + "D");
//    }
//    return UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid), Long.toString(rowId - oid) + "D" + latestPointDate.toString());
//  }
//
//  @Override
//  protected long extractRowId(UniqueIdentifier uniqueId) {
//    String rowId = StringUtils.substringBefore(uniqueId.getVersion(), "D");
//    try {
//      return Long.parseLong(uniqueId.getValue()) + Long.parseLong(rowId);
//    } catch (NumberFormatException ex) {
//      throw new IllegalArgumentException("UniqueIdentifier is not from this master (non-numeric row id): " + uniqueId, ex);
//    }
//  }
//
//  /**
//   * Extracts the point date from the unique identifier.
//   * 
//   * @param uniqueId  the unique identifier, not null
//   * @return the date, null if no point date
//   */
//  protected LocalDate extractPointDate(UniqueIdentifier uniqueId) {
//    String pointDate = StringUtils.substringAfter(uniqueId.getVersion(), "D");
//    if (pointDate.length() == 0) {
//      return null;
//    }
//    try {
//      return LocalDate.parse(pointDate);
//    } catch (RuntimeException ex) {
//      throw new IllegalArgumentException("UniqueIdentifier is not from this master (non-numeric row id): " + uniqueId, ex);
//    }
//  }
//
//  protected HistoricalTimeSeriesInfoDocument getCheckLatestVersion(final UniqueIdentifier uniqueId) {
//    HistoricalTimeSeriesGetRequest request = new HistoricalTimeSeriesGetRequest(uniqueId);
//    request.setLoadTimeSeries(false);
//    final HistoricalTimeSeriesInfoDocument oldDoc = get(request);  // checks uniqueId exists
//    if (oldDoc.getVersionToInstant() != null) {
//      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uniqueId);
//    }
//    return oldDoc;
//  }

  //-------------------------------------------------------------------------
  @Override
  protected String sqlSelectFrom() {
    return SELECT + FROM;
  }

  @Override
  protected String mainTableName() {
    return "hts_document";
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a HistoricalTimeSeriesDocument.
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
          IdentifierWithDates id = IdentifierWithDates.of(Identifier.of(idScheme, idValue), validFrom, validTo);
          _info.setIdentifiers(_info.getIdentifiers().withIdentifier(id));
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
//      final LocalDate maxPointDate = DbDateUtils.fromSqlDateAllowNull(rs.getDate("MAX_POINT_DATE"));
      
      UniqueIdentifier uniqueId = createUniqueIdentifier(docOid, docId);
      _info = new ManageableHistoricalTimeSeriesInfo();
      _info.setUniqueId(uniqueId);
      _info.setName(name);
      _info.setDataField(dataField);
      _info.setDataSource(dataSource);
      _info.setDataProvider(dataProvider);
      _info.setObservationTime(observationTime);
      _info.setIdentifiers(IdentifierBundleWithDates.EMPTY);
      
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
        LocalDate date = DbDateUtils.fromSqlDate(rs.getDate("POINT_DATE"));
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

//  //-------------------------------------------------------------------------
//  /**
//   * Mapper from SQL rows to a UniqueIdentifier.
//   */
//  protected final class UniqueIdentifierExtractor implements ResultSetExtractor<UniqueIdentifier> {
//    @Override
//    public UniqueIdentifier extractData(final ResultSet rs) throws SQLException, DataAccessException {
//      while (rs.next()) {
//        long oid = rs.getLong("DOC_OID");
//        long rowId = rs.getLong("DOC_ID");
//        LocalDate date = DbDateUtils.fromSqlDateAllowNull(rs.getDate("MAX_POINT_DATE"));
//        return createUniqueIdentifier(oid, rowId, date);
//      }
//      throw new DataNotFoundException("Unable to find time-series");
//    }
//  }

}
