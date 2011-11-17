/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.hsqldb.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.LobHandler;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.IdUtils;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.ConfigSearchSortOrder;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;

/**
 * 
 */
/*package*/class DbConfigWorker extends AbstractDocumentDbMaster<ConfigDocument<?>> {
  
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbConfigWorker.class);
  
  /**
   * The Fudge context.
   */
  protected static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();
  
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
        "main.config_type AS config_type, " +
        "main.config AS config ";
  /**
   * SQL from.
   */
  protected static final String FROM =
      "FROM cfg_config main ";
  /**
   * SQL select types
   */
  protected static final String SELECT_TYPES = "SELECT DISTINCT main.config_type AS config_type ";
  /**
   * SQL order by.
   */
  protected static final EnumMap<ConfigSearchSortOrder, String> ORDER_BY_MAP = new EnumMap<ConfigSearchSortOrder, String>(ConfigSearchSortOrder.class);
  static {
    ORDER_BY_MAP.put(ConfigSearchSortOrder.OBJECT_ID_ASC, "oid ASC");
    ORDER_BY_MAP.put(ConfigSearchSortOrder.OBJECT_ID_DESC, "oid DESC");
    ORDER_BY_MAP.put(ConfigSearchSortOrder.VERSION_FROM_INSTANT_ASC, "ver_from_instant ASC");
    ORDER_BY_MAP.put(ConfigSearchSortOrder.VERSION_FROM_INSTANT_DESC, "ver_from_instant DESC");
    ORDER_BY_MAP.put(ConfigSearchSortOrder.NAME_ASC, "name ASC");
    ORDER_BY_MAP.put(ConfigSearchSortOrder.NAME_DESC, "name DESC");
  }

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   * @param defaultScheme  the default scheme, not null
   */
  public DbConfigWorker(DbConnector dbConnector, String defaultScheme) {
    super(dbConnector, defaultScheme);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<?> get(UniqueId uniqueId) {
    return doGet(uniqueId, new ConfigDocumentExtractor(), "Config");
  }

  @Override
  public ConfigDocument<?> get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new ConfigDocumentExtractor(), "Config");
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  @Override
  protected void mergeNonUpdatedFields(ConfigDocument<?> newDocument, ConfigDocument<?> oldDocument) {
    if (newDocument.getValue() == null) {
      ConfigDocument hackGenerics = newDocument;
      hackGenerics.setValue(oldDocument.getValue());
    }
  }

  @Override
  protected ConfigDocument<?> insert(ConfigDocument<?> document) {
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getValue(), "document.value");
    
    final Object value = document.getValue();
    final long docId = nextId("cfg_config_seq");
    final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
    // set the uniqueId
    final UniqueId uniqueId = createUniqueId(docOid, docId);
    document.setUniqueId(uniqueId);
    if (value instanceof MutableUniqueIdentifiable) {
      ((MutableUniqueIdentifiable) value).setUniqueId(uniqueId);
    }
    // serialize the configuration value
    FudgeMsgEnvelope env = FUDGE_CONTEXT.toFudgeMsg(value);
    // REVIEW 2011-01-06 Andrew -- the serialization should only add headers for anything subclass
    // to the reified type to match the deserialization call, reduce payload size and allow easier
    // refactoring of stored objects following an upgrade through database operations.
    byte[] bytes = FUDGE_CONTEXT.toByteArray(env.getMessage());
    // the arguments for inserting into the config table
    final DbMapSqlParameterSource configArgs = new DbMapSqlParameterSource()
      .addValue("doc_id", docId)
      .addValue("doc_oid", docOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("name", document.getName())
      .addValue("config_type", document.getType().getName())
      .addValue("config", new SqlLobValue(bytes, getDialect().getLobHandler()), Types.BLOB);
    getJdbcTemplate().update(sqlInsertConfig(), configArgs);
    return document;
  }

  @Override
  protected String sqlSelectFrom() {
    return SELECT + FROM;
  }

  @Override
  protected String mainTableName() {
    return "cfg_config";
  }

  /**
  * Gets the SQL for inserting a document.
  * 
  * @return the SQL, not null
  */
  protected String sqlInsertConfig() {
    return "INSERT INTO cfg_config " +
           "(id, oid, ver_from_instant, ver_to_instant, corr_from_instant, corr_to_instant, name, config_type, config) " +
         "VALUES " +
           "(:doc_id, :doc_oid, :ver_from_instant, :ver_to_instant, :corr_from_instant, :corr_to_instant, :name, :config_type, :config)";
  }

  //-------------------------------------------------------------------------
  protected ConfigMetaDataResult metaData(ConfigMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    ConfigMetaDataResult result = new ConfigMetaDataResult();
    if (request.isConfigTypes()) {
      List<String> configTypes = getJdbcTemplate().getJdbcOperations().queryForList(SELECT_TYPES + FROM, String.class);
      for (String configType : configTypes) {
        try {
          result.getConfigTypes().add(loadClass(configType));
        } catch (ClassNotFoundException ex) {
          s_logger.warn("Unable to load class", ex);
        }
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  protected <T> ConfigSearchResult<T> search(ConfigSearchRequest<T> request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getType(), "request.type");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final ConfigSearchResult<T> result = new ConfigSearchResult<T>();
    if (request.getConfigIds() != null && request.getConfigIds().size() == 0) {
      result.setPaging(Paging.of(request.getPagingRequest(), 0));
      return result;
    }
    
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
        .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
        .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
        .addValueNullIgnored("name", getDialect().sqlWildcardAdjustValue(request.getName()));

    if (!request.getType().isInstance(Object.class)) {
      args.addValue("config_type", request.getType().getName());
    }

    String[] sql = sqlSearchConfigs(request);

    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate().getNamedParameterJdbcOperations();
    ConfigDocumentExtractor configDocumentExtractor = new ConfigDocumentExtractor();
    if (request.equals(PagingRequest.ALL)) {
      List<ConfigDocument<?>> queryResult = namedJdbc.query(sql[0], args, configDocumentExtractor);
      for (ConfigDocument<?> configDocument : queryResult) {
        if (request.getType().isInstance(configDocument.getValue())) {
          result.getDocuments().add((ConfigDocument<T>) configDocument);
        }
      }
      result.setPaging(Paging.of(request.getPagingRequest(), result.getDocuments()));
    } else {
      final int count = namedJdbc.queryForInt(sql[1], args);
      result.setPaging(Paging.of(request.getPagingRequest(), count));
      if (count > 0 && request.getPagingRequest().equals(PagingRequest.NONE) == false) {
        List<ConfigDocument<?>> queryResult = namedJdbc.query(sql[0], args, configDocumentExtractor);
        for (ConfigDocument<?> configDocument : queryResult) {
          if (request.getType().isInstance(configDocument.getValue())) {
            result.getDocuments().add((ConfigDocument<T>) configDocument);
          }
        }
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  protected <T> ConfigHistoryResult<T> history(ConfigHistoryRequest<T> request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getType(), "request.type");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    checkScheme(request.getObjectId());
    s_logger.debug("history {}", request);

    ConfigHistoryResult<T> result = new ConfigHistoryResult<T>();
    ConfigDocumentExtractor extractor = new ConfigDocumentExtractor();
    final DbMapSqlParameterSource args = argsHistory(request);
    String[] sqlHistory = sqlHistory(request);

    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate().getNamedParameterJdbcOperations();
    if (request.getPagingRequest().equals(PagingRequest.ALL)) {
      List<ConfigDocument<?>> queryResult = namedJdbc.query(sqlHistory[0], args, extractor);
      for (ConfigDocument<?> configDocument : queryResult) {
        if (request.getType().isInstance(configDocument.getValue())) {
          result.getDocuments().add((ConfigDocument<T>) configDocument);
        }
      }
      result.setPaging(Paging.of(request.getPagingRequest(), result.getDocuments()));
    } else {
      final int count = namedJdbc.queryForInt(sqlHistory[1], args);
      result.setPaging(Paging.of(request.getPagingRequest(), count));
      if (count > 0 && request.getPagingRequest().equals(PagingRequest.NONE) == false) {
        List<ConfigDocument<?>> queryResult = namedJdbc.query(sqlHistory[0], args, extractor);
        for (ConfigDocument<?> configDocument : queryResult) {
          if (request.getType().isInstance(configDocument.getValue())) {
            result.getDocuments().add((ConfigDocument<T>) configDocument);
          }
        }
      }
    }
    return result;
  }

  /**
  * Gets the SQL to search for documents.
  * 
  * @param <T> the config type
  * @param request  the request, not null
  * @return the SQL search and count, not null
  */
  protected <T> String[] sqlSearchConfigs(final ConfigSearchRequest<T> request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
        "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
    if (request.getName() != null) {
      where += getDialect().sqlWildcardQuery("AND UPPER(name) ", "UPPER(:name)", request.getName());
    }
    if (!request.getType().isInstance(Object.class)) {
      where += "AND config_type = :config_type ";
    }
    if (request.getConfigIds() != null) {
      StringBuilder buf = new StringBuilder(request.getConfigIds().size() * 10);
      for (ObjectId objectId : request.getConfigIds()) {
        checkScheme(objectId);
        buf.append(extractOid(objectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      where += "AND oid IN (" + buf + ") ";
    }
    
    String selectFromWhereInner = "SELECT id FROM cfg_config " + where;
    String orderBy = ORDER_BY_MAP.get(request.getSortOrder());
    String inner = getDialect().sqlApplyPaging(selectFromWhereInner, "ORDER BY " + orderBy + " ", request.getPagingRequest());
    String search = sqlSelectFrom() + "WHERE main.id IN (" + inner + ") ORDER BY main." + orderBy + sqlAdditionalOrderBy(false);
    String count = "SELECT COUNT(*) FROM cfg_config " + where;
    return new String[] {search, count };
  }

  /**
   * Loads a class from a class name.
   * 
   * @param className  the class name, not null
   * @return the class object, not null
   * @throws ClassNotFoundException 
   * 
   */
  protected Class<?> loadClass(String className) throws ClassNotFoundException {
    return Thread.currentThread().getContextClassLoader().loadClass(className);
//    Class<?> reifiedType = null;
//    try {
//      reifiedType = Thread.currentThread().getContextClassLoader().loadClass(className);
//    } catch (ClassNotFoundException ex) {
//      throw new OpenGammaRuntimeException("Unable to load class", ex);
//    }
//    return reifiedType;
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a ConfigDocument<?>.
   */
  private final class ConfigDocumentExtractor implements ResultSetExtractor<List<ConfigDocument<?>>> {

    private long _lastDocId = -1;
    private List<ConfigDocument<?>> _documents = new ArrayList<ConfigDocument<?>>();
    
    @Override
    public List<ConfigDocument<?>> extractData(ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long docId = rs.getLong("DOC_ID");
        if (_lastDocId != docId) {
          _lastDocId = docId;
          buildConfig(rs, docId);
        }
      }
      return _documents;
    }
    
    private void buildConfig(final ResultSet rs, final long docId) throws SQLException {
      final long docOid = rs.getLong("DOC_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      final String name = rs.getString("NAME");
      final String configType = rs.getString("CONFIG_TYPE");
      LobHandler lob = getDialect().getLobHandler();
      byte[] bytes = lob.getBlobAsBytes(rs, "CONFIG");
      Class<?> reifiedType = null;
      try {
        reifiedType = loadClass(configType);
      } catch (ClassNotFoundException ex) {
        throw new OpenGammaRuntimeException("Unable to load class", ex);
      }
      Object value = FUDGE_CONTEXT.readObject(reifiedType, new ByteArrayInputStream(bytes));
      
      ConfigDocument<Object> doc = new ConfigDocument<Object>(reifiedType);
      UniqueId uniqueId = createUniqueId(docOid, docId);
      doc.setUniqueId(uniqueId);
      IdUtils.setInto(value, uniqueId);
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
