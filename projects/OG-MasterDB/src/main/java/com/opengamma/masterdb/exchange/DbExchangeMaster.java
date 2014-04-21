/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.LobHandler;

import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ExchangeSearchSortOrder;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.paging.Paging;

/**
 * An exchange master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the exchange master using an SQL database.
 * Full details of the API are in {@link ExchangeMaster}.
 * <p>
 * The SQL is stored externally in {@code DbExchangeMaster.elsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbExchangeMaster-MySpecialDB.elsql}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbExchangeMaster extends AbstractDocumentDbMaster<ExchangeDocument> implements ExchangeMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbExchangeMaster.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbExg";
  /**
   * The Fudge context.
   */
  protected static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  /**
   * SQL order by.
   */
  protected static final EnumMap<ExchangeSearchSortOrder, String> ORDER_BY_MAP = new EnumMap<ExchangeSearchSortOrder, String>(ExchangeSearchSortOrder.class);
  static {
    ORDER_BY_MAP.put(ExchangeSearchSortOrder.OBJECT_ID_ASC, "oid ASC");
    ORDER_BY_MAP.put(ExchangeSearchSortOrder.OBJECT_ID_DESC, "oid DESC");
    ORDER_BY_MAP.put(ExchangeSearchSortOrder.VERSION_FROM_INSTANT_ASC, "ver_from_instant ASC");
    ORDER_BY_MAP.put(ExchangeSearchSortOrder.VERSION_FROM_INSTANT_DESC, "ver_from_instant DESC");
    ORDER_BY_MAP.put(ExchangeSearchSortOrder.NAME_ASC, "name ASC");
    ORDER_BY_MAP.put(ExchangeSearchSortOrder.NAME_DESC, "name DESC");
  }

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbExchangeMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbExchangeMaster.class));
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeSearchResult search(final ExchangeSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    final ExchangeSearchResult result = new ExchangeSearchResult(vc);
    
    final ExternalIdSearch externalIdSearch = request.getExternalIdSearch();
    final List<ObjectId> objectIds = request.getObjectIds();
    if ((objectIds != null && objectIds.size() == 0) ||
        (ExternalIdSearch.canMatch(externalIdSearch) == false)) {
      result.setPaging(Paging.of(request.getPagingRequest(), 0));
      return result;
    }
    
    final DbMapSqlParameterSource args = createParameterSource()
      .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
      .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
      .addValueNullIgnored("name", getDialect().sqlWildcardAdjustValue(request.getName()));
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
    args.addValue("sort_order", ORDER_BY_MAP.get(request.getSortOrder()));
    args.addValue("paging_offset", request.getPagingRequest().getFirstItem());
    args.addValue("paging_fetch", request.getPagingRequest().getPagingSize());
    
    String[] sql = {getElSqlBundle().getSql("Search", args), getElSqlBundle().getSql("SearchCount", args)};
    doSearch(request.getPagingRequest(), sql, args, new ExchangeDocumentExtractor(), result);
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
  public ExchangeDocument get(final UniqueId uniqueId) {
    return doGet(uniqueId, new ExchangeDocumentExtractor(), "Exchange");
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new ExchangeDocumentExtractor(), "Exchange");
  }

  //-------------------------------------------------------------------------
  @Override
  public ExchangeHistoryResult history(final ExchangeHistoryRequest request) {
    return doHistory(request, new ExchangeHistoryResult(), new ExchangeDocumentExtractor());
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a new document.
   * 
   * @param document  the document, not null
   * @return the new document, not null
   */
  @Override
  protected ExchangeDocument insert(final ExchangeDocument document) {
    ArgumentChecker.notNull(document.getExchange(), "document.exchange");
    ArgumentChecker.notNull(document.getName(), "document.name");
    
    final ManageableExchange exchange = document.getExchange();
    final long docId = nextId("exg_exchange_seq");
    final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
    // set the uniqueId (needs to go in Fudge message)
    final UniqueId uniqueId = createUniqueId(docOid, docId);
    exchange.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    // the arguments for inserting into the exchange table
    FudgeMsgEnvelope env = FUDGE_CONTEXT.toFudgeMsg(exchange);
    byte[] bytes = FUDGE_CONTEXT.toByteArray(env.getMessage());
    final DbMapSqlParameterSource docArgs = createParameterSource()
      .addValue("doc_id", docId)
      .addValue("doc_oid", docOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("name", document.getName())
      .addValue("time_zone", exchange.getTimeZone() != null ? exchange.getTimeZone().getId() : null, Types.VARCHAR)
      .addValue("detail", new SqlLobValue(bytes, getDialect().getLobHandler()), Types.BLOB);
    // the arguments for inserting into the idkey tables
    final List<DbMapSqlParameterSource> assocList = new ArrayList<DbMapSqlParameterSource>();
    final List<DbMapSqlParameterSource> idKeyList = new ArrayList<DbMapSqlParameterSource>();
    final String sqlSelectIdKey = getElSqlBundle().getSql("SelectIdKey");
    for (ExternalId id : exchange.getExternalIdBundle()) {
      final DbMapSqlParameterSource assocArgs = createParameterSource()
        .addValue("doc_id", docId)
        .addValue("key_scheme", id.getScheme().getName())
        .addValue("key_value", id.getValue());
      assocList.add(assocArgs);
      if (getJdbcTemplate().queryForList(sqlSelectIdKey, assocArgs).isEmpty()) {
        // select avoids creating unecessary id, but id may still not be used
        final long idKeyId = nextId("exg_idkey_seq");
        final DbMapSqlParameterSource idkeyArgs = createParameterSource()
          .addValue("idkey_id", idKeyId)
          .addValue("key_scheme", id.getScheme().getName())
          .addValue("key_value", id.getValue());
        idKeyList.add(idkeyArgs);
      }
    }
    final String sqlDoc = getElSqlBundle().getSql("Insert", docArgs);
    final String sqlIdKey = getElSqlBundle().getSql("InsertIdKey");
    final String sqlDoc2IdKey = getElSqlBundle().getSql("InsertDoc2IdKey");
    getJdbcTemplate().update(sqlDoc, docArgs);
    getJdbcTemplate().batchUpdate(sqlIdKey, idKeyList.toArray(new DbMapSqlParameterSource[idKeyList.size()]));
    getJdbcTemplate().batchUpdate(sqlDoc2IdKey, assocList.toArray(new DbMapSqlParameterSource[assocList.size()]));
    return document;
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a ExchangeDocument.
   */
  protected final class ExchangeDocumentExtractor implements ResultSetExtractor<List<ExchangeDocument>> {
    private List<ExchangeDocument> _documents = new ArrayList<ExchangeDocument>();

    @Override
    public List<ExchangeDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long docId = rs.getLong("DOC_ID");
        buildConfig(rs, docId);
      }
      return _documents;
    }

    private void buildConfig(final ResultSet rs, final long docId) throws SQLException {
      final long docOid = rs.getLong("DOC_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      LobHandler lob = getDialect().getLobHandler();
      byte[] bytes = lob.getBlobAsBytes(rs, "DETAIL");
      ManageableExchange exchange = FUDGE_CONTEXT.readObject(ManageableExchange.class, new ByteArrayInputStream(bytes));
      
      ExchangeDocument doc = new ExchangeDocument();
      doc.setUniqueId(createUniqueId(docOid, docId));
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setExchange(exchange);
      _documents.add(doc);
    }
  }
  
  public ExchangeHistoryResult historyByVersionsCorrections(AbstractHistoryRequest request) {
    ExchangeHistoryRequest exchangeHistoryRequest = new ExchangeHistoryRequest();
    exchangeHistoryRequest.setCorrectionsFromInstant(request.getCorrectionsFromInstant());
    exchangeHistoryRequest.setCorrectionsToInstant(request.getCorrectionsToInstant());
    exchangeHistoryRequest.setVersionsFromInstant(request.getVersionsFromInstant());
    exchangeHistoryRequest.setVersionsToInstant(request.getVersionsToInstant());
    exchangeHistoryRequest.setObjectId(request.getObjectId());
    return history(exchangeHistoryRequest);
  }

}
