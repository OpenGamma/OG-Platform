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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.opengamma.core.user.OGEntitlement;
import com.opengamma.core.user.ResourceAccess;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.master.user.ManageableOGRole;
import com.opengamma.master.user.RoleDocument;
import com.opengamma.master.user.RoleHistoryRequest;
import com.opengamma.master.user.RoleHistoryResult;
import com.opengamma.master.user.RoleMaster;
import com.opengamma.master.user.RoleSearchRequest;
import com.opengamma.master.user.RoleSearchResult;
import com.opengamma.master.user.RoleSearchSortOrder;
import com.opengamma.masterdb.AbstractDocumentDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;

/**
 * A role master implementation using a database for persistence.
 * <p/>
 * This is a full implementation of the role master using an SQL database.
 * Full details of the API are in {@link com.opengamma.master.user.RoleMaster}.
 * <p/>
 * The SQL is stored externally in {@code DbRoleMaster.elsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbRoleMaster-MySpecialDB.elsql}.
 * <p/>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbRoleMaster
    extends AbstractDocumentDbMaster<RoleDocument>
    implements RoleMaster {

  /**
   * Logger.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(DbRoleMaster.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbUsr";

  /**
   * SQL order by.
   */
  protected static final EnumMap<RoleSearchSortOrder, String> ORDER_BY_MAP = new EnumMap<RoleSearchSortOrder, String>(RoleSearchSortOrder.class);

  static {
    ORDER_BY_MAP.put(RoleSearchSortOrder.OBJECT_ID_ASC, "oid ASC");
    ORDER_BY_MAP.put(RoleSearchSortOrder.OBJECT_ID_DESC, "oid DESC");
    ORDER_BY_MAP.put(RoleSearchSortOrder.VERSION_FROM_INSTANT_ASC, "ver_from_instant ASC");
    ORDER_BY_MAP.put(RoleSearchSortOrder.VERSION_FROM_INSTANT_DESC, "ver_from_instant DESC");
    ORDER_BY_MAP.put(RoleSearchSortOrder.NAME_ASC, "name ASC");
    ORDER_BY_MAP.put(RoleSearchSortOrder.NAME_DESC, "name DESC");
  }

  /**
   * Creates an instance.
   *
   * @param dbConnector the database connector, not null
   */
  public DbRoleMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbRoleMaster.class));
  }

  //-------------------------------------------------------------------------
  @Override
  public RoleSearchResult search(RoleSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    ArgumentChecker.notNull(request.getVersionCorrection(), "request.versionCorrection");
    s_logger.debug("search {}", request);

    final VersionCorrection vc = request.getVersionCorrection().withLatestFixed(now());
    final RoleSearchResult result = new RoleSearchResult(vc);

    final String resourceId = request.getResourceId();
    final List<ObjectId> objectIds = request.getObjectIds();

    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
        .addTimestamp("version_as_of_instant", vc.getVersionAsOf())
        .addTimestamp("corrected_to_instant", vc.getCorrectedTo())
        .addValueNullIgnored("name", getDialect().sqlWildcardAdjustValue(request.getName()));

    if (resourceId != null) {
      args.addValue("resource_oid", resourceId.toString());
    }
    if (request.getResourceType() != null) {
      args.addValue("resource_type", request.getResourceType());
    }
    if (request.getResourceAccess() != null) {
      args.addValue("resource_access", request.getResourceAccess().name());
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

    args.addValue("search_in_entitlements", request.getResourceId() != null || request.getResourceAccess() != null || request.getResourceType() != null);
    String[] sql = {getElSqlBundle().getSql("Search", args), getElSqlBundle().getSql("SearchCount", args)};

    args.addValueNullIgnored("user_id", request.getUserUid() == null ? null : extractRowId(request.getUserUid()));

    doSearch(request.getPagingRequest(), sql, args, new RoleDocumentExtractor(), result);
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public RoleDocument get(UniqueId uniqueId) {
    return doGet(uniqueId, new RoleDocumentExtractor(), "Role");
  }

  //-------------------------------------------------------------------------
  @Override
  public RoleDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return doGetByOidInstants(objectId, versionCorrection, new RoleDocumentExtractor(), "Role");
  }

  //-------------------------------------------------------------------------
  @Override
  public RoleHistoryResult history(RoleHistoryRequest request) {
    return doHistory(request, new RoleHistoryResult(), new RoleDocumentExtractor());
  }

  //-------------------------------------------------------------------------

  /**
   * Inserts a new document.
   *
   * @param document the document, not null
   * @return the new document, not null
   */
  @Override
  protected RoleDocument insert(RoleDocument document) {
    ArgumentChecker.notNull(document.getRole(), "document.role");
    ArgumentChecker.notNull(document.getRole().getName(), "document.role.name");
    ArgumentChecker.notNull(document.getRole().getKey(), "document.role.cryptokey");

    final long docId = nextId("usr_oguser_seq");
    final long docOid = (document.getUniqueId() != null ? extractOid(document.getUniqueId()) : docId);
    final UniqueId uniqueId = createUniqueId(docOid, docId);
    final ManageableOGRole role = document.getRole();
    role.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);

    // the arguments for inserting into the role table
    final DbMapSqlParameterSource docArgs = new DbMapSqlParameterSource()
        .addValue("doc_id", docId)
        .addValue("doc_oid", docOid)
        .addTimestamp("ver_from_instant", document.getVersionFromInstant())
        .addTimestampNullFuture("ver_to_instant", document.getVersionToInstant())
        .addTimestamp("corr_from_instant", document.getCorrectionFromInstant())
        .addTimestampNullFuture("corr_to_instant", document.getCorrectionToInstant())
        .addValue("name", role.getName())
        .addValue("cryptokey", role.getKey());

    // the arguments for inserting into the entitlement table
    final List<DbMapSqlParameterSource> assocList = new ArrayList<>();
    final List<DbMapSqlParameterSource> entitlementList = new ArrayList<>();
    final String sqlSelectIdKey = getElSqlBundle().getSql("SelectEntitlementsForRole");

    Set<OGEntitlement> insertedEntitlements = new HashSet<>();
    for (OGEntitlement entitlement : role.getEntitlements()) {
      final DbMapSqlParameterSource assocArgs = new DbMapSqlParameterSource()
          .addValue("ogrole_id", docId)
          .addValue("resource_oid", entitlement.getResourceId())
          .addValue("resource_access", entitlement.getAccess().name())
          .addValue("resource_type", entitlement.getType());
      assocList.add(assocArgs);

      if (getJdbcTemplate().queryForList(sqlSelectIdKey, assocArgs).isEmpty()) {
        // select avoids creating unecessary id, but id may still not be used
        final long entitlementId = nextId("usr_oguser_seq");
        OGEntitlement insertedEntitlement = entitlement.setObjectId(ObjectId.of(getUniqueIdScheme(), "" + entitlementId));
        insertedEntitlements.add(insertedEntitlement);
        final DbMapSqlParameterSource entitlementArgs = new DbMapSqlParameterSource()
            .addValue("id", entitlementId)
            .addValue("ogrole_id", docId)
            .addValue("resource_oid", entitlement.getResourceId())
            .addValue("resource_access", entitlement.getAccess().name())
            .addValue("resource_type", entitlement.getType());
        entitlementList.add(entitlementArgs);
      }
    }
    role.setEntitlements(insertedEntitlements);

    final String sqlDoc = getElSqlBundle().getSql("Insert", docArgs);
    final String sqlEntitlement = getElSqlBundle().getSql("InsertEntitlement");
    getJdbcTemplate().update(sqlDoc, docArgs);
    getJdbcTemplate().batchUpdate(sqlEntitlement, entitlementList.toArray(new DbMapSqlParameterSource[entitlementList.size()]));
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  protected AbstractHistoryResult<RoleDocument> historyByVersionsCorrections(AbstractHistoryRequest request) {
    RoleHistoryRequest historyRequest = new RoleHistoryRequest();
    historyRequest.setCorrectionsFromInstant(request.getCorrectionsFromInstant());
    historyRequest.setCorrectionsToInstant(request.getCorrectionsToInstant());
    historyRequest.setVersionsFromInstant(request.getVersionsFromInstant());
    historyRequest.setVersionsToInstant(request.getVersionsToInstant());
    historyRequest.setObjectId(request.getObjectId());
    return doHistory(request, new RoleHistoryResult(), new RoleDocumentExtractor());
  }



  /**
   * Mapper from SQL rows to a UserDocument.
   */
  protected final class RoleDocumentExtractor implements ResultSetExtractor<List<RoleDocument>> {
    private long _previousDocId = -1L;
    private ManageableOGRole _currRole;
    private Set<OGEntitlement> _currEntitlements = new HashSet<>();
    private List<RoleDocument> _documents = new ArrayList<>();

    @Override
    public List<RoleDocument> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        System.out.println("===================");
        System.out.println(rs.getObject("DOC_ID"));

        final long docId = rs.getLong("DOC_ID");
        // DOC_ID tells us when we're on a new document.
        if (docId != _previousDocId) {
          if (_previousDocId >= 0) {
            _currRole.setEntitlements(_currEntitlements);
          }
          _previousDocId = docId;
          buildRole(rs, docId);
          _currEntitlements.clear();
        }

        // always read sub-tables and use set to deduplicate
        // note that there is an effective CROSS JOIN between entitlements
        // and externalIds, which may be a problem
        String entId = rs.getString("ENT_ID");
        String resourceOid = rs.getString("RESOURCE_OID");
        String resourceType = rs.getString("RESOURCE_TYPE");
        String resourceAccessString = rs.getString("RESOURCE_ACCESS");
        ResourceAccess resourceAccess = ResourceAccess.valueOf(resourceAccessString);
        if (entId != null && resourceOid != null && resourceType != null && resourceAccess != null) {
          OGEntitlement entitlement = new OGEntitlement(ObjectId.of(getUniqueIdScheme(), entId), resourceOid, resourceType, resourceAccess);
          _currEntitlements.add(entitlement);
        }
      }
      // patch up last document read
      if (_previousDocId >= 0) {
        _currRole.setEntitlements(_currEntitlements);
      }
      return _documents;
    }

    private void buildRole(final ResultSet rs, final long docId) throws SQLException {
      final long docOid = rs.getLong("DOC_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");

      UniqueId uniqueId = createUniqueId(docOid, docId);

      ManageableOGRole role = new ManageableOGRole(rs.getString("NAME"));
      role.setKey(rs.getString("cryptokey"));
      //role.setName(rs.getString("name"));

      role.setUniqueId(uniqueId);
      _currRole = role;

      RoleDocument doc = new RoleDocument();
      doc.setUniqueId(uniqueId);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setRole(role);
      _documents.add(doc);
    }
  }

}
