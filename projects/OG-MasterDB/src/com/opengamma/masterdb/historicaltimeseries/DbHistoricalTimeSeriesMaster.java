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
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSearchResult;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.db.Paging;

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
public class DbHistoricalTimeSeriesMaster extends AbstractDocumentDbMaster<HistoricalTimeSeriesDocument> implements HistoricalTimeSeriesMaster {

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
        "main.name AS name, " +
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
        "INNER JOIN hts_data_field df ON main.data_field_id = df.id  " +
        "INNER JOIN hts_data_source ds ON main.data_source_id = ds.id  " +
        "INNER JOIN hts_data_provider dp ON main.data_provider_id = dp.id  " +
        "INNER JOIN hts_observation_time ot ON main.observation_time_id = ot.id  " +
        "LEFT JOIN hts_doc2idkey di ON (di.doc_id = main.id) " +
        "LEFT JOIN hts_idkey i ON (di.idkey_id = i.id) ";

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
  public HistoricalTimeSeriesSearchResult search(final HistoricalTimeSeriesSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final HistoricalTimeSeriesSearchResult result = new HistoricalTimeSeriesSearchResult();
    if ((request.getHistoricalTimeSeriesIds() != null && request.getHistoricalTimeSeriesIds().size() == 0) ||
        (IdentifierSearch.canMatch(request.getHistoricalTimeSeriesKeys()) == false)) {
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
    if (request.getHistoricalTimeSeriesKeys() != null) {
      int i = 0;
      for (Identifier id : request.getHistoricalTimeSeriesKeys()) {
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
  protected String[] sqlSearchHistoricalTimeSeries(final HistoricalTimeSeriesSearchRequest request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
                "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
    if (request.getName() != null) {
      where += getDbHelper().sqlWildcardQuery("AND UPPER(name) ", "UPPER(:name)", request.getName());
    }
    if (request.getDataField() != null) {
      where += "AND data_field_id = " + getDataFieldTable().sqlSelectSearch(request.getDataField()) + ") ";
    }
    if (request.getDataField() != null) {
      where += "AND data_source_id = " + getDataSourceTable().sqlSelectSearch(request.getDataSource()) + ") ";
    }
    if (request.getDataField() != null) {
      where += "AND data_provider_id = " + getDataProviderTable().sqlSelectSearch(request.getDataProvider()) + ") ";
    }
    if (request.getDataField() != null) {
      where += "AND observation_time_id = " + getObservationTimeTable().sqlSelectSearch(request.getObservationTime()) + ") ";
    }
    if (request.getHistoricalTimeSeriesIds() != null) {
      StringBuilder buf = new StringBuilder(request.getHistoricalTimeSeriesIds().size() * 10);
      for (ObjectIdentifier objectId : request.getHistoricalTimeSeriesIds()) {
        checkScheme(objectId);
        buf.append(extractOid(objectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      where += "AND oid IN (" + buf + ") ";
    }
    if (request.getHistoricalTimeSeriesKeys() != null && request.getHistoricalTimeSeriesKeys().size() > 0) {
      where += sqlSelectMatchingHistoricalTimeSeriesKeys(request.getHistoricalTimeSeriesKeys());
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
   * Gets the SQL to match identifier value
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
  public HistoricalTimeSeriesDocument get(final UniqueIdentifier uniqueId) {
    return doGet(uniqueId, new HistoricalTimeSeriesDocumentExtractor(), "HistoricalTimeSeries");
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new HistoricalTimeSeriesDocumentExtractor(), "HistoricalTimeSeries");
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesDocument get(HistoricalTimeSeriesGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    UniqueIdentifier uniqueId = request.getUniqueId();
    checkScheme(uniqueId);
    
    if (uniqueId.isVersioned()) {
      return get(uniqueId);
    } else {
      return get(uniqueId.getObjectId(), request.getVersionCorrection());
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesHistoryResult history(final HistoricalTimeSeriesHistoryRequest request) {
    return doHistory(request, new HistoricalTimeSeriesHistoryResult(), new HistoricalTimeSeriesDocumentExtractor());
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a new document.
   * 
   * @param document  the document, not null
   * @return the new document, not null
   */
  @Override
  protected HistoricalTimeSeriesDocument insert(final HistoricalTimeSeriesDocument document) {
    ArgumentChecker.notNull(document.getSeries(), "document.series");
    ArgumentChecker.notNull(document.getSeries().getName(), "document.name");
    ArgumentChecker.notNull(document.getSeries().getDataField(), "document.dataField");
    ArgumentChecker.notNull(document.getSeries().getDataSource(), "document.dataSource");
    ArgumentChecker.notNull(document.getSeries().getDataProvider(), "document.dataProvider");
    ArgumentChecker.notNull(document.getSeries().getObservationTime(), "document.observationTime");
    
    final long docId = nextId("hts_master_seq");
    final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
    // the arguments for inserting into the table
    final ManageableHistoricalTimeSeries series = document.getSeries();
    final DbMapSqlParameterSource seriesArgs = new DbMapSqlParameterSource()
      .addValue("doc_id", docId)
      .addValue("doc_oid", docOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("name", document.getSeries().getName())
      .addValue("data_field_id", getDataFieldTable().ensure(series.getDataField()))
      .addValue("data_source_id", getDataSourceTable().ensure(series.getDataSource()))
      .addValue("data_provider_id", getDataProviderTable().ensure(series.getDataProvider()))
      .addValue("observation_time_id", getObservationTimeTable().ensure(series.getObservationTime()));
    // the arguments for inserting into the idkey tables
    final List<DbMapSqlParameterSource> assocList = new ArrayList<DbMapSqlParameterSource>();
    final List<DbMapSqlParameterSource> idKeyList = new ArrayList<DbMapSqlParameterSource>();
    for (IdentifierWithDates id : series.getIdentifiers()) {
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
    series.setUniqueId(uniqueId);
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
              "(id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, name, " +
              "data_field_id, data_source_id, data_provider_id, observation_time_id) " +
            "VALUES " +
              "(:doc_id, :doc_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :name, " +
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
  protected final class HistoricalTimeSeriesDocumentExtractor implements ResultSetExtractor<List<HistoricalTimeSeriesDocument>> {
    private long _lastDocId = -1;
    private ManageableHistoricalTimeSeries _series;
    private List<HistoricalTimeSeriesDocument> _documents = new ArrayList<HistoricalTimeSeriesDocument>();

    @Override
    public List<HistoricalTimeSeriesDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
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
          _series.setIdentifiers(_series.getIdentifiers().withIdentifier(id));
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
      
      UniqueIdentifier uniqueId = createUniqueIdentifier(docOid, docId);
      _series = new ManageableHistoricalTimeSeries();
      _series.setUniqueId(uniqueId);
      _series.setName(name);
      _series.setDataField(dataField);
      _series.setDataSource(dataSource);
      _series.setDataProvider(dataProvider);
      _series.setObservationTime(observationTime);
      _series.setIdentifiers(IdentifierBundleWithDates.EMPTY);
      
      HistoricalTimeSeriesDocument doc = new HistoricalTimeSeriesDocument(_series);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      _documents.add(doc);
    }
  }

}
