/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.threeten.bp.ZoneId;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.UserDocument;
import com.opengamma.master.user.UserHistoryRequest;
import com.opengamma.master.user.UserHistoryResult;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.master.user.UserSearchSortOrder;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.paging.Paging;

/**
 * A user master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the user master using an SQL database.
 * Full details of the API are in {@link UserMaster}.
 * <p>
 * The SQL is stored externally in {@code DbUserMaster.elsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbUserMaster-MySpecialDB.elsql}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbUserMaster
    extends AbstractDocumentDbMaster<UserDocument>
    implements UserMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbUserMaster.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbUsr";

  /**
   * SQL order by.
   */
  protected static final EnumMap<UserSearchSortOrder, String> ORDER_BY_MAP = new EnumMap<UserSearchSortOrder, String>(UserSearchSortOrder.class);
  static {
    ORDER_BY_MAP.put(UserSearchSortOrder.OBJECT_ID_ASC, "oid ASC");
    ORDER_BY_MAP.put(UserSearchSortOrder.OBJECT_ID_DESC, "oid DESC");
    ORDER_BY_MAP.put(UserSearchSortOrder.VERSION_FROM_INSTANT_ASC, "ver_from_instant ASC");
    ORDER_BY_MAP.put(UserSearchSortOrder.VERSION_FROM_INSTANT_DESC, "ver_from_instant DESC");
    ORDER_BY_MAP.put(UserSearchSortOrder.NAME_ASC, "name ASC");
    ORDER_BY_MAP.put(UserSearchSortOrder.NAME_DESC, "name DESC");
    ORDER_BY_MAP.put(UserSearchSortOrder.EMAIL_ASC, "email_address ASC");
    ORDER_BY_MAP.put(UserSearchSortOrder.EMAIL_DESC, "email_address DESC");
  }

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbUserMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbUserMaster.class));
  }

  //-------------------------------------------------------------------------
  @Override
  public UserSearchResult search(UserSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);
    
    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    final UserSearchResult result = new UserSearchResult(vc);
    
    final ExternalIdSearch externalIdSearch = request.getExternalIdSearch();
    final List<ObjectId> objectIds = request.getObjectIds();
    if ((objectIds != null && objectIds.size() == 0) ||
        (ExternalIdSearch.canMatch(externalIdSearch) == false)) {
      result.setPaging(Paging.of(request.getPagingRequest(), 0));
      return result;
    }
    
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
      .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
      .addValueNullIgnored("userid", getDialect().sqlWildcardAdjustValue(request.getUserId()))
      .addValueNullIgnored("name", getDialect().sqlWildcardAdjustValue(request.getName()))
      .addValueNullIgnored("time_zone", getDialect().sqlWildcardAdjustValue(request.getTimeZone()))
      .addValueNullIgnored("email_address", getDialect().sqlWildcardAdjustValue(request.getEmailAddress()))
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
    doSearch(request.getPagingRequest(), sql, args, new UserDocumentExtractor(), result);
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
  public UserDocument get(UniqueId uniqueId) {
    return doGet(uniqueId, new UserDocumentExtractor(), "User");
  }

  //-------------------------------------------------------------------------
  @Override
  public UserDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new UserDocumentExtractor(), "User");
  }

  //-------------------------------------------------------------------------
  @Override
  public UserHistoryResult history(UserHistoryRequest request) {
    return doHistory(request, new UserHistoryResult(), new UserDocumentExtractor());
  }

  //-------------------------------------------------------------------------
  /**
   * Inserts a new document.
   * 
   * @param document  the document, not null
   * @return the new document, not null
   */
  @Override
  protected UserDocument insert(UserDocument document) {
    ArgumentChecker.notNull(document.getUser(), "document.user");
    ArgumentChecker.notNull(document.getUser().getUserId(), "document.user.userid");
    ArgumentChecker.notNull(document.getUser().getTimeZone(), "document.user.timezone");

    final long docId = nextId("usr_oguser_seq");
    final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
    final UniqueId uniqueId = createUniqueId(docOid, docId);
    final ManageableOGUser user = document.getUser();
    user.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    
    // the arguments for inserting into the user table
    final DbMapSqlParameterSource docArgs = new DbMapSqlParameterSource()
      .addValue("doc_id", docId)
      .addValue("doc_oid", docOid)
      .addTimestamp("ver_from_instant", document.getVersionFromInstant())
      .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
      .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
      .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
      .addValue("userid", user.getUserId())
      .addValue("password", user.getPasswordHash())
      .addValue("name", user.getName())
      .addValue("time_zone", user.getTimeZone().getId())
      .addValue("email_address", user.getEmailAddress());
    
    // the arguments for inserting into the idkey tables
    final List<DbMapSqlParameterSource> assocList = new ArrayList<DbMapSqlParameterSource>();
    final List<DbMapSqlParameterSource> idKeyList = new ArrayList<DbMapSqlParameterSource>();
    final String sqlSelectIdKey = getElSqlBundle().getSql("SelectIdKey");
    for (ExternalId id : user.getExternalIdBundle()) {
      final DbMapSqlParameterSource assocArgs = new DbMapSqlParameterSource()
        .addValue("doc_id", docId)
        .addValue("key_scheme", id.getScheme().getName())
        .addValue("key_value", id.getValue());
      assocList.add(assocArgs);
      if (getJdbcTemplate().queryForList(sqlSelectIdKey, assocArgs).isEmpty()) {
        // select avoids creating unecessary id, but id may still not be used
        final long idKeyId = nextId("usr_idkey_seq");
        final DbMapSqlParameterSource idkeyArgs = new DbMapSqlParameterSource()
          .addValue("idkey_id", idKeyId)
          .addValue("key_scheme", id.getScheme().getName())
          .addValue("key_value", id.getValue());
        idKeyList.add(idkeyArgs);
      }
    }
    
    final List<DbMapSqlParameterSource> entitlementList = new ArrayList<DbMapSqlParameterSource>();
    int iEntitlement = 0;
    for (String entitlement : user.getEntitlements()) {
      entitlementList.add(new DbMapSqlParameterSource()
        .addValue("oguser_id", docId)
        .addValue("entitlement_index", iEntitlement)
        .addValue("entitlement_pattern", entitlement));
      iEntitlement++;
    }
    
    final String sqlDoc = getElSqlBundle().getSql("Insert", docArgs);
    final String sqlIdKey = getElSqlBundle().getSql("InsertIdKey");
    final String sqlDoc2IdKey = getElSqlBundle().getSql("InsertDoc2IdKey");
    final String sqlEntitlement = getElSqlBundle().getSql("InsertEntitlement");
    getJdbcTemplate().update(sqlDoc, docArgs);
    getJdbcTemplate().batchUpdate(sqlIdKey, idKeyList.toArray(new DbMapSqlParameterSource[idKeyList.size()]));
    getJdbcTemplate().batchUpdate(sqlDoc2IdKey, assocList.toArray(new DbMapSqlParameterSource[assocList.size()]));
    getJdbcTemplate().batchUpdate(sqlEntitlement, entitlementList.toArray(new DbMapSqlParameterSource[entitlementList.size()]));
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected AbstractHistoryResult<UserDocument> historyByVersionsCorrections(AbstractHistoryRequest request) {
    UserHistoryRequest historyRequest = new UserHistoryRequest();
    historyRequest.setCorrectionsFromInstant(request.getCorrectionsFromInstant());
    historyRequest.setCorrectionsToInstant(request.getCorrectionsToInstant());
    historyRequest.setVersionsFromInstant(request.getVersionsFromInstant());
    historyRequest.setVersionsToInstant(request.getVersionsToInstant());
    historyRequest.setObjectId(request.getObjectId());
    return doHistory(request, new UserHistoryResult(), new UserDocumentExtractor());
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a UserDocument.
   */
  protected final class UserDocumentExtractor implements ResultSetExtractor<List<UserDocument>> {
    private long _previousDocId = -1L;
    private ManageableOGUser _currUser;
    private Set<ExternalId> _currExternalIds = new HashSet<ExternalId>();
    private Set<String> _currEntitlements = new HashSet<String>();
    private List<UserDocument> _documents = new ArrayList<UserDocument>();

    @Override
    public List<UserDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
//        System.out.println("===================");
//        System.out.println(rs.getObject("DOC_ID"));
//        System.out.println(rs.getObject("IDKEY_ID"));
//        System.out.println(rs.getObject("KEY_SCHEME"));
//        System.out.println(rs.getObject("KEY_VALUE"));
//        System.out.println(rs.getObject("ENTITLEMENT_PATTERN"));
        
        final long docId = rs.getLong("DOC_ID");
        // DOC_ID tells us when we're on a new document.
        if (docId != _previousDocId) {
          if (_previousDocId >= 0) {
            _currUser.setExternalIdBundle(ExternalIdBundle.of(_currExternalIds));
            _currUser.setEntitlements(_currEntitlements);
          }
          _previousDocId = docId;
          buildUser(rs, docId);
          _currExternalIds.clear();
          _currEntitlements.clear();
        }
        
        // always read sub-tables and use set to deduplicate
        // note that there is an effective CROSS JOIN between entitlements
        // and externalIds, which may be a problem
        String idKey = rs.getString("KEY_SCHEME");
        String idValue = rs.getString("KEY_VALUE");
        if (idKey != null && idValue != null) {
          _currExternalIds.add(ExternalId.of(idKey, idValue));
        }
        String entitlementPattern = rs.getString("ENTITLEMENT_PATTERN");
        if (entitlementPattern != null) {
          _currEntitlements.add(entitlementPattern);
        }
      }
      // patch up last document read
      if (_previousDocId >= 0) {
        _currUser.setExternalIdBundle(ExternalIdBundle.of(_currExternalIds));
        _currUser.setEntitlements(_currEntitlements);
      }
      return _documents;
    }

    private void buildUser(final ResultSet rs, final long docId) throws SQLException {
      final long docOid = rs.getLong("DOC_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      
      UniqueId uniqueId = createUniqueId(docOid, docId);
      
      ManageableOGUser user = new ManageableOGUser(rs.getString("USERID"));
      user.setPasswordHash(rs.getString("PASSWORD"));
      user.setName(rs.getString("NAME"));
      user.setTimeZone(ZoneId.of(rs.getString("TIME_ZONE")));
      user.setEmailAddress(rs.getString("EMAIL_ADDRESS"));
      user.setUniqueId(uniqueId);
      _currUser = user;
      
      UserDocument doc = new UserDocument();
      doc.setUniqueId(uniqueId);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setUser(user);
      _documents.add(doc);
    }
  }

}
