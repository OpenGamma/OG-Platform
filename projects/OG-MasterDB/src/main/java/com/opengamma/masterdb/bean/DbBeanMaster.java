/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.bean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.support.SqlLobValue;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.master.AbstractSearchResult;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.paging.Paging;

/**
 * A master implementation based on Joda-Beans using a database for persistence.
 * <p>
 * This is a full implementation of a master using an SQL database.
 * Data is stored based on the Joda-Beans API.
 * <p>
 * The SQL is stored externally in {@code DbBeanMaster.elsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbBeanMaster-MySpecialDB.elsql}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 * 
 * @param <D>  the document type
 * @param <V>  the bean type
 */
public class DbBeanMaster<D extends AbstractDocument, V extends Bean>
    extends AbstractDocumentDbMaster<D> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbBeanMaster.class);

  /**
   * The database table prefix.
   */
  private final BeanMasterCallback<D, V> _callback;
  /**
   * The document sequence.
   */
  private String _sequenceDocument;
  /**
   * The idKey sequence.
   */
  private String _sequenceIdKey;
  /**
   * The attribute sequence.
   */
  private String _sequenceAttribute;
  /**
   * The properties sequence.
   */
  private String _sequenceProperties;

  // -----------------------------------------------------------------
  // TIMERS FOR METRICS GATHERING
  // By default these do nothing. Registration will replace them
  // so that they actually do something.
  // -----------------------------------------------------------------
  private Timer _insertTimer = new Timer();
  private Timer _subTypesTimer = new Timer();
  private Timer _schemaVersionTimer = new Timer();

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   * @param idScheme  the identifier scheme, not null
   * @param callback  the callback, not null
   */
  public DbBeanMaster(final DbConnector dbConnector, final String idScheme, final BeanMasterCallback<D, V> callback) {
    super(dbConnector, idScheme);
    _callback = callback;
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbBeanMaster.class));
  }

  @Override
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailedRegistry, String namePrefix) {
    super.registerMetrics(summaryRegistry, detailedRegistry, namePrefix);
    _insertTimer = summaryRegistry.timer(namePrefix + ".insert");
    _subTypesTimer = summaryRegistry.timer(namePrefix + ".subTypes");
    _schemaVersionTimer = summaryRegistry.timer(namePrefix + ".schemaVersion");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the callback object.
   * 
   * @return the callback object, not null
   */
  protected BeanMasterCallback<D, V> getCallback() {
    return _callback;
  }

  //-------------------------------------------------------------------------
  @Override
  public void setElSqlBundle(ElSqlBundle bundle) {
    super.setElSqlBundle(bundle);
    DbMapSqlParameterSource source = createParameterSource();
    _sequenceDocument = getElSqlBundle().getSql("SequenceDocument", source).trim();
    _sequenceIdKey = getElSqlBundle().getSql("SequenceIdKey", source).trim();
    _sequenceAttribute = getElSqlBundle().getSql("SequenceAttr", source).trim();
    _sequenceProperties = getElSqlBundle().getSql("SequenceProp", source).trim();
  }

  /**
   * Creates the parameter source.
   * @return the source, not null
   */
  @Override
  protected DbMapSqlParameterSource createParameterSource() {
    Objects.requireNonNull(getCallback().getSqlTablePrefix(), "Table prefix");
    return new DbMapSqlParameterSource().addValue("table_prefix", getCallback().getSqlTablePrefix());
  }

  //-------------------------------------------------------------------------
  public List<String> getAllSubTypes() {
    try (Timer.Context context = _subTypesTimer.time()) {
      final String sql = getElSqlBundle().getSql("SelectSubTypes", createParameterSource());
      return getJdbcTemplate().getJdbcOperations().queryForList(sql, String.class);
    }
  }

  public List<String> getAllActualTypes() {
    try (Timer.Context context = _subTypesTimer.time()) {
      final String sql = getElSqlBundle().getSql("SelectActualTypes", createParameterSource());
      return getJdbcTemplate().getJdbcOperations().queryForList(sql, String.class);
    }
  }

  public String getSchemaVersionString() {
    try (Timer.Context context = _schemaVersionTimer.time()) {
      return String.valueOf(getSchemaVersion());
    }
  }

  //-------------------------------------------------------------------------
  public <R extends AbstractSearchResult<D>> R search(final BeanMasterSearchRequest request, final R result) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    result.setVersionCorrection(vc);
    
    final ExternalIdSearch externalIdSearch = request.getExternalIdSearch();
    final Map<String, String> attributes = request.getAttributes();
    final Map<String, String> indexedProperties = request.getIndexedProperties();
    final List<ObjectId> objectIds = request.getObjectIds();
    if ((objectIds != null && objectIds.size() == 0) ||
        (ExternalIdSearch.canMatch(request.getExternalIdSearch()) == false)) {
      result.setPaging(Paging.of(request.getPagingRequest(), 0));
      return result;
    }
    
    final DbMapSqlParameterSource args = createParameterSource()
      .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
      .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
      .addValueNullIgnored("name", getDialect().sqlWildcardAdjustValue(request.getName()))
      .addValueNullIgnored("main_type", request.getMainType())
      .addValueNullIgnored("sub_type", request.getSubType())
      .addValueNullIgnored("actual_type", request.getActualType())
      .addValueNullIgnored("external_id_scheme", getDialect().sqlWildcardAdjustValue(request.getExternalIdScheme()))
      .addValueNullIgnored("external_id_value", getDialect().sqlWildcardAdjustValue(request.getExternalIdValue()));
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
    if (attributes.size() > 0) {
      int i = 0;
      for (Entry<String, String> entry : attributes.entrySet()) {
        args.addValue("attr_key" + i, entry.getKey());
        args.addValue("attr_value" + i, getDialect().sqlWildcardAdjustValue(entry.getValue()));
        i++;
      }
      args.addValue("attr_search_size", attributes.size());
    }
    if (indexedProperties.size() > 0) {
      int i = 0;
      for (Entry<String, String> entry : indexedProperties.entrySet()) {
        args.addValue("prop_key" + i, entry.getKey());
        args.addValue("prop_value" + i, getDialect().sqlWildcardAdjustValue(entry.getValue()));
        i++;
      }
      args.addValue("prop_search_size", indexedProperties.size());
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
    args.addValue("sort_order", (request.getSortOrderSql() != null ? request.getSortOrderSql() : "oid ASC"));
    args.addValue("paging_offset", request.getPagingRequest().getFirstItem());
    args.addValue("paging_fetch", request.getPagingRequest().getPagingSize());
    
    String[] sql = {getElSqlBundle().getSql("Search", args), getElSqlBundle().getSql("SearchCount", args)};
    doSearch(request.getPagingRequest(), sql, args, new DocumentExtractor(), result);
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
  public D get(final UniqueId uniqueId) {
    return doGet(uniqueId, new DocumentExtractor(), getCallback().getMasterName());
  }

  //-------------------------------------------------------------------------
  @Override
  public D get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new DocumentExtractor(), getCallback().getMasterName());
  }

  //-------------------------------------------------------------------------
  public <R extends AbstractHistoryResult<D>> R history(final AbstractHistoryRequest request, final R result) {
    return doHistory(request, result, new DocumentExtractor());
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a new document.
   * 
   * @param document  the document, not null
   * @return the new document, not null
   */
  protected D insert(final D document) {
    @SuppressWarnings("unchecked")
    V value = (V) document.getValue();
    ArgumentChecker.notNull(value, "document.value");
    
    final String name = getCallback().getName(value);
    final ExternalIdBundle externalIdBundle = getCallback().getExternalIdBundle(value);
    final Map<String, String> attributes = getCallback().getAttributes(value);
    final Map<String, String> indexedProperties = getCallback().getIndexedProperties(value);
    final Character mainType = getCallback().getMainType(value);
    final String subType = getCallback().getSubType(value);
    final String actualType = getCallback().getActualType(value);
    final byte[] packedData = getCallback().getPackedData(value);
    ArgumentChecker.notNull(name, "document.value.name");
    ArgumentChecker.notNull(externalIdBundle, "document.value.externalIdBundle");
    ArgumentChecker.notNull(attributes, "document.value.attributes");
    ArgumentChecker.notNull(subType, "document.value.subType");
    ArgumentChecker.notNull(actualType, "document.value.actualType");
    
    try (Timer.Context context = _insertTimer.time()) {
      final long docId = nextId(_sequenceDocument);
      final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
      
      // the arguments for inserting into the table
      final DbMapSqlParameterSource docArgs = createParameterSource()
        .addValue("doc_id", docId)
        .addValue("doc_oid", docOid)
        .addTimestamp("ver_from_instant", document.getVersionFromInstant())
        .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
        .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
        .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
        .addValue("name", name)
        .addValue("main_type", mainType.toString())  // need String not Character for SQL server
        .addValue("sub_type", subType)
        .addValue("actual_type", actualType)
        .addValue("packed_data", new SqlLobValue(packedData, getDialect().getLobHandler()), Types.BLOB);
      // store document
      final String sqlDoc = getElSqlBundle().getSql("Insert", docArgs);
      getJdbcTemplate().update(sqlDoc, docArgs);
      
      // store idkey and attributes
      if (externalIdBundle.size() > 0) {
        insertIdKey(docId, externalIdBundle);
      }
      if (attributes.size() > 0) {
        insertMap(docId, attributes, _sequenceAttribute, "Attr");
      }
      if (indexedProperties.size() > 0) {
        insertMap(docId, indexedProperties, _sequenceProperties, "Prop");
      }
      
      // set the uniqueId
      final UniqueId uniqueId = createUniqueId(docOid, docId);
      IdUtils.setInto(value, uniqueId);
      document.setUniqueId(uniqueId);
      return document;
    }
  }

  protected void insertIdKey(final long docId, ExternalIdBundle externalIdBundle) {
    // cannot convert bundle to map as keys are duplicated
    final List<DbMapSqlParameterSource> assocList = new ArrayList<DbMapSqlParameterSource>();
    final List<DbMapSqlParameterSource> dataList = new ArrayList<DbMapSqlParameterSource>();
    final String sqlSelectData = getElSqlBundle().getSql("SelectIdKey", createParameterSource());
    for (ExternalId id : externalIdBundle) {
      final DbMapSqlParameterSource assocArgs = createParameterSource()
        .addValue("doc_id", docId)
        .addValue("key", id.getScheme().getName())
        .addValue("value", id.getValue());
      assocList.add(assocArgs);
      if (getJdbcTemplate().queryForList(sqlSelectData, assocArgs).isEmpty()) {
        // select avoids creating unnecessary id, but id may still not be used
        final long dataId = nextId(_sequenceIdKey);
        final DbMapSqlParameterSource idkeyArgs = createParameterSource()
          .addValue("id", dataId)
          .addValue("key", id.getScheme().getName())
          .addValue("value", id.getValue());
        dataList.add(idkeyArgs);
      }
    }
    final String sqlData = getElSqlBundle().getSql("InsertIdKey", createParameterSource());
    final String sqlAssoc = getElSqlBundle().getSql("InsertDoc2IdKey", createParameterSource());
    getJdbcTemplate().batchUpdate(sqlData, dataList.toArray(new DbMapSqlParameterSource[dataList.size()]));
    getJdbcTemplate().batchUpdate(sqlAssoc, assocList.toArray(new DbMapSqlParameterSource[assocList.size()]));
  }

  protected void insertMap(final long docId, Map<String, String> attributes, String sequence, String type) {
    final List<DbMapSqlParameterSource> assocList = new ArrayList<DbMapSqlParameterSource>();
    final List<DbMapSqlParameterSource> dataList = new ArrayList<DbMapSqlParameterSource>();
    final String sqlSelectData = getElSqlBundle().getSql("Select" + type, createParameterSource());
    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      final DbMapSqlParameterSource assocArgs = createParameterSource()
        .addValue("doc_id", docId)
        .addValue("key", entry.getKey())
        .addValue("value", entry.getValue());
      assocList.add(assocArgs);
      if (getJdbcTemplate().queryForList(sqlSelectData, assocArgs).isEmpty()) {
        // select avoids creating unnecessary id, but id may still not be used
        final long dataId = nextId(sequence);
        final DbMapSqlParameterSource idkeyArgs = createParameterSource()
          .addValue("id", dataId)
          .addValue("key", entry.getKey())
          .addValue("value", entry.getValue());
        dataList.add(idkeyArgs);
      }
    }
    final String sqlData = getElSqlBundle().getSql("Insert" + type, createParameterSource());
    final String sqlAssoc = getElSqlBundle().getSql("InsertDoc2" + type, createParameterSource());
    getJdbcTemplate().batchUpdate(sqlData, dataList.toArray(new DbMapSqlParameterSource[dataList.size()]));
    getJdbcTemplate().batchUpdate(sqlAssoc, assocList.toArray(new DbMapSqlParameterSource[assocList.size()]));
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a document.
   */
  protected final class DocumentExtractor implements ResultSetExtractor<List<D>> {
    private long _lastDocId = -1;
    private V _value;
    private List<D> _documents = new ArrayList<D>();

    @Override
    public List<D> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long docId = rs.getLong("DOC_ID");
        if (_lastDocId != docId) {
          _lastDocId = docId;
          buildValue(rs, docId);
        } else {
          throw new IllegalStateException("Same row returned twice from database");
        }
      }
      return _documents;
    }

    private void buildValue(final ResultSet rs, final long docId) throws SQLException {
      final long docOid = rs.getLong("DOC_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      final byte[] packedData = getDialect().getLobHandler().getBlobAsBytes(rs, "PACKED_DATA");
      _value = getCallback().parsePackedData(packedData);
      UniqueId uniqueId = createUniqueId(docOid, docId);
      IdUtils.setInto(_value, uniqueId);
      D doc = DbBeanMaster.this.getCallback().createDocument(_value);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setUniqueId(uniqueId);
      _documents.add(doc);
    }
  }

  @Override
  protected AbstractHistoryResult<D> historyByVersionsCorrections(AbstractHistoryRequest request) {
    BeanMasterHistoryRequest historyRequest = new BeanMasterHistoryRequest();
    historyRequest.setCorrectionsFromInstant(request.getCorrectionsFromInstant());
    historyRequest.setCorrectionsToInstant(request.getCorrectionsToInstant());
    historyRequest.setVersionsFromInstant(request.getVersionsFromInstant());
    historyRequest.setVersionsToInstant(request.getVersionsToInstant());
    historyRequest.setObjectId(request.getObjectId());
    return history(historyRequest, new BeanMasterHistoryResult<D>());
  }

}
