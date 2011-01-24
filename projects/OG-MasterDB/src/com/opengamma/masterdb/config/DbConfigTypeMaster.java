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
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.time.Instant;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.hsqldb.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.LobHandler;

import com.google.common.base.Objects;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.master.listener.MasterChangeListener;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * A config master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the config master using an SQL database.
 * Full details of the API are in {@link ConfigTypeMaster}.
 * <p>
 * This class uses SQL via JDBC. The SQL may be changed by subclassing the relevant methods.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 * 
 * @param <T>  the configuration element type
 */
public class DbConfigTypeMaster<T> extends AbstractDocumentDbMaster<ConfigDocument<T>> implements ConfigTypeMaster<T> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbConfigTypeMaster.class);

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbCfg";
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
   * The class of the configuration.
   */
  private final Class<T> _clazz;
  /**
   * The set of listeners.
   */
  private final CopyOnWriteArraySet<MasterChangeListener> _listeners = new CopyOnWriteArraySet<MasterChangeListener>();

  /**
   * Creates an instance.
   * 
   * @param clazz  the class of the configuration, not null
   * @param dbSource  the database source combining all configuration, not null
   */
  public DbConfigTypeMaster(final Class<T> clazz, final DbSource dbSource) {
    super(dbSource, IDENTIFIER_SCHEME_DEFAULT);
    ArgumentChecker.notNull(clazz, "clazz");
    _clazz = clazz;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the reified generic type.
   * 
   * @return the type, non-null
   */
  public Class<T> getReifiedType() {
    return _clazz;
  }

  //-------------------------------------------------------------------------
  @Override
  public void addChangeListener(final MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  @Override
  public void removeChangeListener(final MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.remove(listener);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigSearchResult<T> search(final ConfigSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    s_logger.debug("search {}", request);
    
    final Instant now = Instant.now(getTimeSource());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(request.getVersionAsOfInstant(), now))
      .addTimestamp("corrected_to_instant", Objects.firstNonNull(request.getCorrectedToInstant(), now))
      .addValueNullIgnored("name", getDbHelper().sqlWildcardAdjustValue(request.getName()))
      .addValue("config_type", getReifiedType().getName());
    final ConfigSearchResult<T> result = new ConfigSearchResult<T>();
    searchWithPaging(request.getPagingRequest(), sqlSearchConfigs(request), args, new ConfigDocumentExtractor(), result);
    return result;
  }

  /**
   * Gets the SQL to search for documents.
   * 
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchConfigs(final ConfigSearchRequest request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
      "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
    if (request.getName() != null) {
      where += getDbHelper().sqlWildcardQuery("AND UPPER(name) ", "UPPER(:name)", request.getName());
    }
    where += sqlAdditionalWhere();
    
    String selectFromWhereInner = "SELECT id FROM cfg_config " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY ver_from_instant DESC, corr_from_instant DESC ", request.getPagingRequest());
    String search = sqlSelectFrom() + "WHERE main.id IN (" + inner + ") ORDER BY main.ver_from_instant DESC, main.corr_from_instant DESC" + sqlAdditionalOrderBy(false);
    String count = "SELECT COUNT(*) FROM cfg_config " + where;
    return new String[] {search, count};
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> get(final UniqueIdentifier uniqueId) {
    return doGet(uniqueId, new ConfigDocumentExtractor(), "Config");
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigHistoryResult<T> history(final ConfigHistoryRequest request) {
    return doHistory(request, new ConfigHistoryResult<T>(), new ConfigDocumentExtractor());
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> add(final ConfigDocument<T> document) {
    ConfigDocument<T> result = super.add(document);
    notifyDocumentAdded(result.getUniqueId());
    return result;
  }

  protected void notifyDocumentAdded(final UniqueIdentifier addedItem) {
    for (MasterChangeListener listener : _listeners) {
      listener.added(addedItem);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> update(final ConfigDocument<T> document) {
    ConfigDocument<T> result = super.update(document);
    notifyDocumentUpdated(document.getUniqueId(), result.getUniqueId());
    return result;
  }

  protected void notifyDocumentUpdated(final UniqueIdentifier oldItem, final UniqueIdentifier newItem) {
    for (MasterChangeListener listener : _listeners) {
      listener.updated(oldItem, newItem);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uniqueId) {
    super.remove(uniqueId);
    notifyDocumentRemoved(uniqueId);
  }

  protected void notifyDocumentRemoved(final UniqueIdentifier removedItem) {
    for (MasterChangeListener listener : _listeners) {
      listener.removed(removedItem);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> correct(final ConfigDocument<T> document) {
    ConfigDocument<T> result = super.correct(document);
    notifyDocumentCorrected(document.getUniqueId(), result.getUniqueId());
    return result;
  }

  protected void notifyDocumentCorrected(final UniqueIdentifier oldItem, UniqueIdentifier newItem) {
    for (MasterChangeListener listener : _listeners) {
      listener.corrected(oldItem, newItem);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a new document.
   * 
   * @param document  the document, not null
   * @return the new document, not null
   */
  @Override
  protected ConfigDocument<T> insert(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document.getValue(), "document.value");
    ArgumentChecker.notNull(document.getName(), "document.name");
    
    final T value = document.getValue();
    final long docId = nextId("cfg_config_seq");
    final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
    // set the uniqueId
    final UniqueIdentifier uniqueId = createUniqueIdentifier(docOid, docId);
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
    final MapSqlParameterSource configArgs = new DbMapSqlParameterSource()
      .addValue("doc_id", docId)
      .addValue("doc_oid", docOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("name", document.getName())
      .addValue("config_type", getReifiedType().getName())
      .addValue("config", new SqlLobValue(bytes, getDbHelper().getLobHandler()), Types.BLOB);
    getJdbcTemplate().update(sqlInsertConfig(), configArgs);
    return document;
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
  @Override
  protected String sqlSelectFrom() {
    return SELECT + FROM;
  }

  @Override
  protected String sqlAdditionalWhere() {
    return "AND config_type = '" + getReifiedType().getName() + "' ";
  }

  @Override
  protected String mainTableName() {
    return "cfg_config";
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a ConfigDocument<T>.
   */
  protected final class ConfigDocumentExtractor implements ResultSetExtractor<List<ConfigDocument<T>>> {
    private long _lastDocId = -1;
    private List<ConfigDocument<T>> _documents = new ArrayList<ConfigDocument<T>>();

    @Override
    public List<ConfigDocument<T>> extractData(final ResultSet rs) throws SQLException, DataAccessException {
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
      LobHandler lob = getDbHelper().getLobHandler();
      byte[] bytes = lob.getBlobAsBytes(rs, "CONFIG");
      T value = FUDGE_CONTEXT.readObject(getReifiedType(), new ByteArrayInputStream(bytes));
      
      ConfigDocument<T> doc = new ConfigDocument<T>();
      doc.setUniqueId(createUniqueIdentifier(docOid, docId));
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
