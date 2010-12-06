/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.Paging;

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
    final ConfigHistoryRequest request = new ConfigHistoryRequest(uid, now);
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
    final ConfigSearchResult<T> result = new ConfigSearchResult<T>();
    final Instant now = Instant.now(getTimeSource());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(request.getVersionAsOfInstant(), now))
      .addValueNullIgnored("name", getDbHelper().sqlWildcardAdjustValue(request.getName()))
      .addValue("config_type", getMaster().getReifiedType().getName());
    final String[] sql = sqlSearchConfigs(request);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final int count = namedJdbc.queryForInt(sql[1], args);
    result.setPaging(new Paging(request.getPagingRequest(), count));
    if (count > 0) {
      final ConfigDocumentExtractor extractor = new ConfigDocumentExtractor();
      result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
    }
    return result;
  }

  /**
   * Gets the SQL to search for configuration documents.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchConfigs(final ConfigSearchRequest request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
      "AND config_type = :config_type ";
    if (request.getName() != null) {
      where += getDbHelper().sqlWildcardQuery("AND UPPER(name) ", "UPPER(:name)", request.getName());
    }
    String selectFromWhereInner = "SELECT id FROM cfg_config " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY ver_from_instant DESC ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE c.id IN (" + inner + ") ORDER BY c.ver_from_instant DESC";
    String count = "SELECT COUNT(*) FROM cfg_config " + where;
    return new String[] {search, count};
  }

  //-------------------------------------------------------------------------
  @Override
  protected ConfigHistoryResult<T> history(final ConfigHistoryRequest request) {
    s_logger.debug("historyConfig: {}", request);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("config_oid", extractOid(request.getConfigId()))
      .addTimestampNullIgnored("versions_from_instant", request.getVersionsFromInstant())
      .addTimestampNullIgnored("versions_to_instant", request.getVersionsToInstant())
      .addValue("config_type", getMaster().getReifiedType().getName());
    final String[] sql = sqlSearchConfigHistoric(request);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final int count = namedJdbc.queryForInt(sql[1], args);
    final ConfigHistoryResult<T> result = new ConfigHistoryResult<T>();
    result.setPaging(new Paging(request.getPagingRequest(), count));
    if (count > 0) {
      final ConfigDocumentExtractor extractor = new ConfigDocumentExtractor();
      result.getDocuments().addAll(namedJdbc.query(sql[0], args, extractor));
    }
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
    String selectFromWhereInner = "SELECT id FROM cfg_config " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY ver_from_instant DESC ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE c.id IN (" + inner + ") ORDER BY c.ver_from_instant DESC";
    String count = "SELECT COUNT(*) FROM cfg_config " + where;
    return new String[] {search, count};
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
      final String name = rs.getString("NAME");
      LobHandler lob = getDbHelper().getLobHandler();
      byte[] bytes = lob.getBlobAsBytes(rs, "CONFIG");
      T value = FUDGE_CONTEXT.readObject(getMaster().getReifiedType(), new ByteArrayInputStream(bytes));
      
      ConfigDocument<T> doc = new ConfigDocument<T>();
      doc.setUniqueId(createUniqueIdentifier(configOid, configId));
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setName(name);
      doc.setValue(value);
      _documents.add(doc);
    }
  }

}
