/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.threeten.bp.Instant;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableList;
import com.opengamma.DataDuplicationException;
import com.opengamma.DataVersionException;
import com.opengamma.core.user.UserAccount;
import com.opengamma.core.user.impl.SimpleUserAccount;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.HistoryEvent;
import com.opengamma.master.user.HistoryEventType;
import com.opengamma.master.user.ManageableRole;
import com.opengamma.master.user.RoleEventHistoryRequest;
import com.opengamma.master.user.RoleEventHistoryResult;
import com.opengamma.master.user.RoleMaster;
import com.opengamma.master.user.RoleSearchRequest;
import com.opengamma.master.user.RoleSearchResult;
import com.opengamma.master.user.RoleSearchSortOrder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A role master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the role master using an SQL database.
 * Full details of the API are in {@link RoleMaster}.
 * <p>
 * The SQL is stored externally in {@code DbRoleMaster.elsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbRoleMaster-MySpecialDB.elsql}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbRoleMaster
    extends AbstractDbUserMaster<ManageableRole>
    implements RoleMaster {

  /** Event sequence name. */
  private static final String USR_ROLE_EVENT_SEQ = "usr_role_event_seq";

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbRoleMaster.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbUsrRole";
  /**
   * SQL order by.
   */
  protected static final EnumMap<RoleSearchSortOrder, String> ORDER_BY_MAP = new EnumMap<RoleSearchSortOrder, String>(RoleSearchSortOrder.class);
  static {
    ORDER_BY_MAP.put(RoleSearchSortOrder.OBJECT_ID_ASC, "oid ASC");
    ORDER_BY_MAP.put(RoleSearchSortOrder.OBJECT_ID_DESC, "oid DESC");
    ORDER_BY_MAP.put(RoleSearchSortOrder.NAME_ASC, "role_name ASC");
    ORDER_BY_MAP.put(RoleSearchSortOrder.NAME_DESC, "role_name DESC");
  }

  // -----------------------------------------------------------------
  // TIMERS FOR METRICS GATHERING
  // By default these do nothing. Registration will replace them
  // so that they actually do something.
  // -----------------------------------------------------------------
  private Timer _searchTimer = new Timer();

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbRoleMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbRoleMaster.class));
  }

  @Override
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailedRegistry, String namePrefix) {
    super.registerMetrics(summaryRegistry, detailedRegistry, namePrefix);
    _searchTimer = summaryRegistry.timer(namePrefix + ".search");
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean nameExists(String roleName) {
    ArgumentChecker.notNull(roleName, "roleName");
    return doNameExists(roleName);
  }

  @Override
  public ManageableRole getByName(String roleName) {
    ArgumentChecker.notNull(roleName, "roleName");
    s_logger.debug("getByName {}", roleName);
    
    ObjectId oid = lookupName(roleName, OnDeleted.EXCEPTION);
    return doGetById(oid, new RoleExtractor());
  }

  @Override
  public ManageableRole getById(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    s_logger.debug("getById {}", objectId);
    checkScheme(objectId);
    
    return doGetById(objectId, new RoleExtractor());
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId add(final ManageableRole role) {
    return doAdd(role);
  }

  /**
   * Processes the role add, within a retrying transaction.
   *
   * @param role  the role to add, not null
   * @return the information, not null
   */
  @Override
  Pair<UniqueId, Instant> doAddInTransaction(ManageableRole role) {
    // check if role exists
    if (doNameExists(role.getRoleName())) {
      throw new DataDuplicationException("Role already exists: " + role.getRoleName());
    }
    // insert new row
    final Instant now = now();
    final long docOid = nextId("usr_role_seq");
    final UniqueId uniqueId = createUniqueId(docOid, docOid);
    insertMain(docOid, role);
    insertNameLookup(role.getRoleName(), uniqueId.getObjectId());
    insertAssociatedUsers(docOid, role);
    insertAssociatedPermissions(docOid, role);
    insertAssociatedRoles(docOid, role);
    HistoryEvent event = HistoryEvent.of(HistoryEventType.ADDED, uniqueId, "system", now, ImmutableList.<String>of());
    insertEvent(event, USR_ROLE_EVENT_SEQ);
    return Pairs.of(uniqueId, now);
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId update(final ManageableRole role) {
    return doUpdate(role);
  }

  /**
   * Processes the update, within a retrying transaction.
   *
   * @param role  the updated role, not null
   * @return the updated document, not null
   */
  @Override
  Pair<UniqueId, Instant> doUpdateInTransaction(ManageableRole role) {
    ObjectId objectId = role.getObjectId();
    String oldVersion = role.getUniqueId().getVersion();
    ManageableRole current = getById(objectId);
    int newVersion = Integer.parseInt(oldVersion) + 1;
    UniqueId newUniqueId = objectId.atVersion(Integer.toString(newVersion));
    // validate
    if (current.equals(role)) {
      return Pairs.of(newUniqueId, null);  // no change
    }
    if (current.getUniqueId().getVersion().equals(oldVersion) == false) {
      throw new DataVersionException("Invalid version, Role has already been updated: " + objectId);
    }
    if (caseInsensitive(role.getRoleName()).equals(caseInsensitive(current.getRoleName())) == false) {
      // check if role exists
      if (doNameExists(role.getRoleName())) {
        throw new DataDuplicationException("Role cannot be renamed, new name already exists: " + role.getRoleName());
      }
      insertNameLookup(role.getRoleName(), current.getObjectId());
    }
    // update
    long docOid = extractOid(objectId);
    updateMain(docOid, newVersion, role);
    if (current.getAssociatedUsers().equals(role.getAssociatedUsers()) == false) {
      deleteAssociatedUsers(docOid);
      insertAssociatedUsers(docOid, role);
    }
    if (current.getAssociatedPermissions().equals(role.getAssociatedPermissions()) == false) {
      deleteAssociatedPermissions(docOid);
      insertAssociatedPermissions(docOid, role);
    }
    if (current.getAssociatedRoles().equals(role.getAssociatedRoles()) == false) {
      deleteAssociatedRoles(docOid);
      insertAssociatedRoles(docOid, role);
    }
    final Instant now = now();
    List<String> changes = calculateChanges(current, role);
    HistoryEvent event = HistoryEvent.of(HistoryEventType.CHANGED, newUniqueId, "system", now, changes);
    insertEvent(event, USR_ROLE_EVENT_SEQ);
    return Pairs.of(newUniqueId, now);
  }

  private List<String> calculateChanges(ManageableRole current, ManageableRole updated) {
    List<String> changes = new ArrayList<>();
    // changes
    createChange(changes, current, updated, ManageableRole.meta().roleName());
    createChange(changes, current, updated, ManageableRole.meta().description());
    // added permission
    Set<String> addedUsers = new TreeSet<>(updated.getAssociatedUsers());
    addedUsers.removeAll(current.getAssociatedUsers());
    for (String permission : addedUsers) {
      changes.add(StringUtils.left("Added user: " + permission, 255));
    }
    // removed permission
    Set<String> removedUsers = new TreeSet<>(current.getAssociatedUsers());
    removedUsers.removeAll(updated.getAssociatedUsers());
    for (String permission : removedUsers) {
      changes.add(StringUtils.left("Removed user: " + permission, 255));
    }
    // added permission
    Set<String> addedPermissions = new TreeSet<>(updated.getAssociatedPermissions());
    addedPermissions.removeAll(current.getAssociatedPermissions());
    for (String permission : addedPermissions) {
      changes.add(StringUtils.left("Added permission: " + permission, 255));
    }
    // removed permission
    Set<String> removedPermissions = new TreeSet<>(current.getAssociatedPermissions());
    removedPermissions.removeAll(updated.getAssociatedPermissions());
    for (String permission : removedPermissions) {
      changes.add(StringUtils.left("Removed permission: " + permission, 255));
    }
    // added permission
    Set<String> addedRoles = new TreeSet<>(updated.getAssociatedRoles());
    addedRoles.removeAll(current.getAssociatedRoles());
    for (String permission : addedRoles) {
      changes.add(StringUtils.left("Added role: " + permission, 255));
    }
    // removed permission
    Set<String> removedRoles = new TreeSet<>(current.getAssociatedRoles());
    removedRoles.removeAll(updated.getAssociatedRoles());
    for (String permission : removedRoles) {
      changes.add(StringUtils.left("Removed role: " + permission, 255));
    }
    return changes;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId save(ManageableRole role) {
    ArgumentChecker.notNull(role, "role");
    s_logger.debug("save {}", role.getRoleName());
    if (role.getUniqueId() != null) {
      return update(role);
    } else {
      return add(role);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void removeByName(String roleName) {
    doRemoveByName(roleName);
  }

  @Override
  public void removeById(final ObjectId objectId) {
    doRemoveById(objectId);
  }

  /**
   * Processes the document update, within a retrying transaction.
   *
   * @param objectId  the object identifier to remove, not null
   * @return the updated document, not null
   */
  @Override
  Instant doRemoveInTransaction(final ObjectId objectId) {
    ManageableRole current = getById(objectId);
    int newVersion = Integer.parseInt(current.getUniqueId().getVersion()) + 1;
    UniqueId newUniqueId = objectId.atVersion(Integer.toString(newVersion));
    
    long docOid = extractOid(objectId);
    deleteAssociatedUsers(docOid);
    deleteAssociatedPermissions(docOid);
    deleteAssociatedRoles(docOid);
    deleteMain(docOid);
    updateNameLookupToDeleted(docOid);
    Instant now = now();
    HistoryEvent event = HistoryEvent.of(HistoryEventType.REMOVED, newUniqueId, "system", now, ImmutableList.<String>of());
    insertEvent(event, USR_ROLE_EVENT_SEQ);
    return now;
  }

  //-------------------------------------------------------------------------
  @Override
  public RoleSearchResult search(RoleSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    s_logger.debug("search {}", request);
    if ((request.getObjectIds() != null && request.getObjectIds().isEmpty())) {
      Paging paging = Paging.of(request.getPagingRequest(), 0);
      return new RoleSearchResult(paging, new ArrayList<ManageableRole>());
    }
    
    try (Timer.Context context = _searchTimer.time()) {
      return doSearch(request);
    }
  }

  private RoleSearchResult doSearch(RoleSearchRequest request) {
    PagingRequest pagingRequest = request.getPagingRequest();
    // setup args
    final DbMapSqlParameterSource args = createParameterSource()
      .addValueNullIgnored("role_name_ci", caseInsensitive(getDialect().sqlWildcardAdjustValue(request.getRoleName())))
      .addValueNullIgnored("assoc_user", request.getAssociatedUser())
      .addValueNullIgnored("assoc_perm", request.getAssociatedPermission())
      .addValueNullIgnored("assoc_role", request.getAssociatedRole());
    if (request.getObjectIds() != null) {
      StringBuilder buf = new StringBuilder(request.getObjectIds().size() * 10);
      for (ObjectId objectId : request.getObjectIds()) {
        checkScheme(objectId);
        buf.append(extractOid(objectId)).append(", ");
      }
      buf.setLength(buf.length() - 2);
      args.addValue("sql_search_object_ids", buf.toString());
    }
    args.addValue("sort_order", ORDER_BY_MAP.get(request.getSortOrder()));
    args.addValue("paging_offset", pagingRequest.getFirstItem());
    args.addValue("paging_fetch", pagingRequest.getPagingSize());
    // search
    String[] sql = {getElSqlBundle().getSql("Search", args), getElSqlBundle().getSql("SearchCount", args)};
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate();
    Paging paging;
    List<ManageableRole> results = new ArrayList<>();
    if (pagingRequest.equals(PagingRequest.ALL)) {
      paging = Paging.of(pagingRequest, results);
      results.addAll(namedJdbc.query(sql[0], args, new RoleExtractor()));
    } else {
      s_logger.debug("executing sql {}", sql[1]);
      final int count = namedJdbc.queryForObject(sql[1], args, Integer.class);
      paging = Paging.of(pagingRequest, count);
      if (count > 0 && pagingRequest.equals(PagingRequest.NONE) == false) {
        s_logger.debug("executing sql {}", sql[0]);
        results.addAll(namedJdbc.query(sql[0], args, new RoleExtractor()));
      }
    }
    return new RoleSearchResult(paging, results);
  }

  //-------------------------------------------------------------------------
  @Override
  public RoleEventHistoryResult eventHistory(RoleEventHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    s_logger.debug("eventHistory {}", request);
    ObjectId objectId = request.getObjectId();
    if (objectId == null) {
      objectId = lookupName(request.getRoleName(), OnDeleted.RETURN_ID);
    }
    checkScheme(objectId);
    
    return new RoleEventHistoryResult(doEventHistory(objectId));
  }

  //-------------------------------------------------------------------------
  @Override
  public UserAccount resolveAccount(UserAccount account) {
    ArgumentChecker.notNull(account, "account");
    SimpleUserAccount resolved = SimpleUserAccount.from(account);
    
    final DbMapSqlParameterSource args = createParameterSource()
        .addValue("user_name_ci", caseInsensitive(account.getUserName()));
    final String sql = getElSqlBundle().getSql("GetResolvedRoles", args);
    List<Map<String, Object>> result = getJdbcTemplate().queryForList(sql, args);
    for (Map<String, Object> row : result) {
      Object role = row.get("ROLE_NAME");
      if (role != null) {
        resolved.getRoles().add(role.toString());
        Object perm = row.get("ASSOC_PERM");
        if (perm != null) {
          resolved.getPermissions().add(perm.toString());
        }
      }
    }
    return resolved;
  }

  //-------------------------------------------------------------------------
  private void insertMain(long docOid, ManageableRole role) {
    final DbMapSqlParameterSource docArgs = mainArgs(docOid, 0, role);
    final String sqlDoc = getElSqlBundle().getSql("InsertMain", docArgs);
    getJdbcTemplate().update(sqlDoc, docArgs);
  }

  private void insertAssociatedUsers(long docOid, ManageableRole role) {
    final List<DbMapSqlParameterSource> argsList = new ArrayList<DbMapSqlParameterSource>();
    for (String assoc : role.getAssociatedUsers()) {
      argsList.add(createParameterSource()
        .addValue("id", nextId("usr_role_assocuser_seq"))
        .addValue("doc_id", docOid)
        .addValue("assoc_user", caseInsensitive(assoc)));
    }
    final String sql = getElSqlBundle().getSql("InsertAssocUser");
    getJdbcTemplate().batchUpdate(sql, argsList.toArray(new DbMapSqlParameterSource[argsList.size()]));
  }

  private void insertAssociatedPermissions(long docOid, ManageableRole role) {
    final List<DbMapSqlParameterSource> argsList = new ArrayList<DbMapSqlParameterSource>();
    for (String assoc : role.getAssociatedPermissions()) {
      argsList.add(createParameterSource()
        .addValue("id", nextId("usr_role_assocperm_seq"))
        .addValue("doc_id", docOid)
        .addValue("assoc_perm", assoc));
    }
    final String sql = getElSqlBundle().getSql("InsertAssocPerm");
    getJdbcTemplate().batchUpdate(sql, argsList.toArray(new DbMapSqlParameterSource[argsList.size()]));
  }

  private void insertAssociatedRoles(long docOid, ManageableRole role) {
    final List<DbMapSqlParameterSource> argsList = new ArrayList<DbMapSqlParameterSource>();
    for (String assoc : role.getAssociatedRoles()) {
      argsList.add(createParameterSource()
        .addValue("id", nextId("usr_role_assocrole_seq"))
        .addValue("doc_id", docOid)
        .addValue("assoc_role", caseInsensitive(assoc)));
    }
    final String sql = getElSqlBundle().getSql("InsertAssocRole");
    getJdbcTemplate().batchUpdate(sql, argsList.toArray(new DbMapSqlParameterSource[argsList.size()]));
  }

  //-------------------------------------------------------------------------
  private void updateMain(long docOid, int version, ManageableRole role) {
    final DbMapSqlParameterSource docArgs = mainArgs(docOid, version, role);
    final String sqlDoc = getElSqlBundle().getSql("UpdateMain", docArgs);
    getJdbcTemplate().update(sqlDoc, docArgs);
  }

  private DbMapSqlParameterSource mainArgs(long docOid, int version, ManageableRole role) {
    final DbMapSqlParameterSource docArgs = createParameterSource()
      .addValue("doc_id", docOid)
      .addValue("version", version)
      .addValue("role_name", role.getRoleName())
      .addValue("role_name_ci", caseInsensitive(role.getRoleName()))
      .addValue("description", role.getDescription());
    return docArgs;
  }

  //-------------------------------------------------------------------------
  private void deleteMain(long docOid) {
    final DbMapSqlParameterSource args = createParameterSource()
        .addValue("doc_id", docOid);
    final String sql = getElSqlBundle().getSql("DeleteMain", args);
    getJdbcTemplate().update(sql, args);
  }

  private void deleteAssociatedUsers(long docOid) {
    final DbMapSqlParameterSource args = createParameterSource()
        .addValue("doc_id", docOid);
    final String sql = getElSqlBundle().getSql("DeleteAssocUsers", args);
    getJdbcTemplate().update(sql, args);
  }

  private void deleteAssociatedPermissions(long docOid) {
    final DbMapSqlParameterSource args = createParameterSource()
        .addValue("doc_id", docOid);
    final String sql = getElSqlBundle().getSql("DeleteAssocPerms", args);
    getJdbcTemplate().update(sql, args);
  }

  private void deleteAssociatedRoles(long docOid) {
    final DbMapSqlParameterSource args = createParameterSource()
        .addValue("doc_id", docOid);
    final String sql = getElSqlBundle().getSql("DeleteAssocRoles", args);
    getJdbcTemplate().update(sql, args);
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a ManageableRole.
   */
  final class RoleExtractor implements ResultSetExtractor<List<ManageableRole>> {
    private long _previousDocId = -1L;
    private ManageableRole _currRole;
    private Set<String> _currUsers = new HashSet<>();
    private Set<String> _currPermissions = new HashSet<>();
    private Set<String> _currRoles = new HashSet<>();
    private List<ManageableRole> _roles = new ArrayList<>();

    @Override
    public List<ManageableRole> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long docId = rs.getLong("DOC_ID");
        // DOC_ID tells us when we're on a new document.
        if (docId != _previousDocId) {
          if (_previousDocId >= 0) {
            _currRole.setAssociatedUsers(_currUsers);
            _currRole.setAssociatedPermissions(_currPermissions);
            _currRole.setAssociatedRoles(_currRoles);
          }
          _previousDocId = docId;
          buildRole(rs, docId);
          _currUsers.clear();
          _currPermissions.clear();
          _currRoles.clear();
        }
        String assocUser = rs.getString("ASSOC_USER");
        if (assocUser != null) {
          _currUsers.add(assocUser);
        }
        String assocPerm = rs.getString("ASSOC_PERM");
        if (assocPerm != null) {
          _currPermissions.add(assocPerm);
        }
        String assocRole = rs.getString("ASSOC_ROLE");
        if (assocRole != null) {
          _currRoles.add(assocRole);
        }
      }
      // patch up last document read
      if (_previousDocId >= 0) {
        _currRole.setAssociatedUsers(_currUsers);
        _currRole.setAssociatedPermissions(_currPermissions);
        _currRole.setAssociatedRoles(_currRoles);
      }
      return _roles;
    }

    private void buildRole(final ResultSet rs, final long docId) throws SQLException {
      int version = rs.getInt("VERSION");
      UniqueId uniqueId = UniqueId.of(getUniqueIdScheme(), Long.toString(docId), Integer.toString(version));
      ManageableRole role = new ManageableRole(rs.getString("ROLE_NAME"));
      role.setUniqueId(uniqueId);
      role.setDescription(rs.getString("DESCRIPTION"));
      _currRole = role;
      _roles.add(role);
    }
  }

}
