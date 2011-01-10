/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.lob.LobHandler;

import com.google.common.base.Objects;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.Paging;
import com.opengamma.util.db.PagingRequest;

/**
 * Config master worker to query a configuration document.
 * 
 * @param <T>  the configuration element type
 */
public class QueryConfigDbConfigTypeMasterWorker<T> extends DbConfigTypeMasterWorker<T> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(QueryConfigDbConfigTypeMasterWorker.class);
  /**
   * SQL select.
   */
  protected static final String SELECT =
      "SELECT " +
        "c.id AS config_id, " +
        "c.oid AS config_oid, " +
        "c.ver_from_instant AS ver_from_instant, " +
        "c.ver_to_instant AS ver_to_instant, " +
        "c.corr_from_instant AS corr_from_instant, " +
        "c.corr_to_instant AS corr_to_instant, " +
        "c.name AS name, " +
        "c.config_type AS config_type, " +
        "c.config AS config ";
  /**
   * SQL from.
   */
  protected static final String FROM =
      "FROM cfg_config c ";

  /**
   * Creates an instance.
   */
  public QueryConfigDbConfigTypeMasterWorker() {
  }

  //-------------------------------------------------------------------------
  @Override
  protected ConfigDocument<T> get(final UniqueIdentifier uid) {
    if (uid.isVersioned()) {
      return getById(uid);
    } else {
      return getByLatest(uid);
    }
  }

  /**
   * Gets a config by searching for the latest version of an object identifier.
   * @param uid  the unique identifier
   * @return the config document, null if not found
   */
  protected ConfigDocument<T> getByLatest(final UniqueIdentifier uid) {
    s_logger.debug("getConfigByLatest: {}", uid);
    final Instant now = Instant.now(getTimeSource());
    final ConfigHistoryRequest request = new ConfigHistoryRequest(uid, now, now);
    final ConfigHistoryResult<T> result = getMaster().history(request);
    if (result.getDocuments().size() != 1) {
      throw new DataNotFoundException("Config not found: " + uid);
    }
    return result.getFirstDocument();
  }

  /**
   * Gets a config by identifier.
   * @param uid  the unique identifier
   * @return the config document, null if not found
   */
  protected ConfigDocument<T> getById(final UniqueIdentifier uid) {
    s_logger.debug("getConfigById {}", uid);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("config_id", extractRowId(uid))
      .addValue("config_type", getMaster().getReifiedType().getName());
    
    final ConfigDocumentExtractor extractor = new ConfigDocumentExtractor();
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<ConfigDocument<T>> docs = namedJdbc.query(sqlGetConfigById(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Config not found: " + uid);
    }
    return docs.get(0);
  }

  /**
   * Gets the SQL for getting a config by unique row identifier.
   * @return the SQL, not null
   */
  protected String sqlGetConfigById() {
    return SELECT + FROM + "WHERE c.id = :config_id AND config_type = :config_type ";
  }

  //-------------------------------------------------------------------------
  @Override
  protected ConfigSearchResult<T> search(ConfigSearchRequest request) {
    s_logger.debug("searchConfig: {}", request);
    final Instant now = Instant.now(getTimeSource());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(request.getVersionAsOfInstant(), now))
      .addTimestamp("corrected_to_instant", Objects.firstNonNull(request.getCorrectedToInstant(), now))
      .addValueNullIgnored("name", getDbHelper().sqlWildcardAdjustValue(request.getName()))
      .addValue("config_type", getMaster().getReifiedType().getName());
    final ConfigSearchResult<T> result = new ConfigSearchResult<T>();
    searchWithPaging(request.getPagingRequest(), sqlSearchConfigs(request), args, new ConfigDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL to search for configuration documents.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchConfigs(final ConfigSearchRequest request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
      "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant " +
      "AND config_type = :config_type ";
    if (request.getName() != null) {
      where += getDbHelper().sqlWildcardQuery("AND UPPER(name) ", "UPPER(:name)", request.getName());
    }
    String selectFromWhereInner = "SELECT id FROM cfg_config " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY ver_from_instant DESC, corr_from_instant DESC ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE c.id IN (" + inner + ") ORDER BY c.ver_from_instant DESC, c.corr_from_instant DESC";
    String count = "SELECT COUNT(*) FROM cfg_config " + where;
    return new String[] {search, count};
  }

  //-------------------------------------------------------------------------
  @Override
  protected ConfigHistoryResult<T> history(final ConfigHistoryRequest request) {
    s_logger.debug("historyConfig: {}", request);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("config_oid", extractOid(request.getObjectId()))
      .addTimestampNullIgnored("versions_from_instant", request.getVersionsFromInstant())
      .addTimestampNullIgnored("versions_to_instant", request.getVersionsToInstant())
      .addTimestampNullIgnored("corrections_from_instant", request.getCorrectionsFromInstant())
      .addTimestampNullIgnored("corrections_to_instant", request.getCorrectionsToInstant())
      .addValue("config_type", getMaster().getReifiedType().getName());
    final ConfigHistoryResult<T> result = new ConfigHistoryResult<T>();
    searchWithPaging(request.getPagingRequest(), sqlSearchConfigHistoric(request), args, new ConfigDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL for searching the history of a config.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchConfigHistoric(final ConfigHistoryRequest request) {
    String where = "WHERE oid = :config_oid " +
      "AND config_type = :config_type ";
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
    String selectFromWhereInner = "SELECT id FROM cfg_config " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY ver_from_instant DESC, corr_from_instant DESC ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE c.id IN (" + inner + ") ORDER BY c.ver_from_instant DESC, c.corr_from_instant DESC ";
    String count = "SELECT COUNT(*) FROM cfg_config " + where;
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
      final ResultSetExtractor<List<ConfigDocument<T>>> extractor, final AbstractDocumentsResult<ConfigDocument<T>> result) {
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
   * Mapper from SQL rows to a ConfigDocument<T>.
   */
  protected final class ConfigDocumentExtractor implements ResultSetExtractor<List<ConfigDocument<T>>> {
    private long _lastConfigId = -1;
    private List<ConfigDocument<T>> _documents = new ArrayList<ConfigDocument<T>>();

    @Override
    public List<ConfigDocument<T>> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long configId = rs.getLong("CONFIG_ID");
        if (_lastConfigId != configId) {
          _lastConfigId = configId;
          buildConfig(rs, configId);
        }
      }
      return _documents;
    }

    private void buildConfig(final ResultSet rs, final long configId) throws SQLException {
      final long configOid = rs.getLong("CONFIG_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      final String name = rs.getString("NAME");
      LobHandler lob = getDbHelper().getLobHandler();
      byte[] bytes = lob.getBlobAsBytes(rs, "CONFIG");
      T value = FUDGE_CONTEXT.readObject(getMaster().getReifiedType(), new ByteArrayInputStream(bytes));
      
      ConfigDocument<T> doc = new ConfigDocument<T>();
      doc.setUniqueId(createUniqueIdentifier(configOid, configId));
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setName(name);
      doc.setValue(value);
      _documents.add(doc);
    }
  }

}
