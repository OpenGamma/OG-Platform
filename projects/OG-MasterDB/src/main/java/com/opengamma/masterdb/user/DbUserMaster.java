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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableList;
import com.opengamma.DataDuplicationException;
import com.opengamma.DataVersionException;
import com.opengamma.core.user.DateStyle;
import com.opengamma.core.user.TimeStyle;
import com.opengamma.core.user.UserAccount;
import com.opengamma.core.user.UserAccountStatus;
import com.opengamma.core.user.impl.SimpleUserAccount;
import com.opengamma.core.user.impl.SimpleUserProfile;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.HistoryEvent;
import com.opengamma.master.user.HistoryEventType;
import com.opengamma.master.user.ManageableRole;
import com.opengamma.master.user.ManageableUser;
import com.opengamma.master.user.UserEventHistoryRequest;
import com.opengamma.master.user.UserEventHistoryResult;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.master.user.UserSearchSortOrder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

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
    extends AbstractDbUserMaster<ManageableUser>
    implements UserMaster {

  /** Event sequence name. */
  private static final String USR_USER_EVENT_SEQ = "usr_user_event_seq";

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
    ORDER_BY_MAP.put(UserSearchSortOrder.NAME_ASC, "user_name ASC");
    ORDER_BY_MAP.put(UserSearchSortOrder.NAME_DESC, "user_name DESC");
  }

  private final DbRoleMaster _roleMaster;
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
  public DbUserMaster(final DbConnector dbConnector) {
    this(dbConnector, new DbRoleMaster(dbConnector));
  }

  /**
   * Creates an instance controlling the role master.
   * 
   * @param dbConnector  the database connector, not null
   * @param roleMaster  the role master, not null
   */
  public DbUserMaster(final DbConnector dbConnector, DbRoleMaster roleMaster) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    _roleMaster = ArgumentChecker.notNull(roleMaster, "roleMaster");
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbUserMaster.class));
  }

  @Override
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailedRegistry, String namePrefix) {
    super.registerMetrics(summaryRegistry, detailedRegistry, namePrefix);
    _searchTimer = summaryRegistry.timer(namePrefix + ".search");
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean nameExists(String userName) {
    ArgumentChecker.notNull(userName, "userName");
    return doNameExists(userName);
  }

  @Override
  public ManageableUser getByName(String userName) {
    ArgumentChecker.notNull(userName, "userName");
    s_logger.debug("getByName {}", userName);
    
    ObjectId oid = lookupName(userName, OnDeleted.EXCEPTION);
    return doGetById(oid, new UserExtractor());
  }

  @Override
  public ManageableUser getById(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    s_logger.debug("getById {}", objectId);
    checkScheme(objectId);
    
    return doGetById(objectId, new UserExtractor());
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId add(final ManageableUser user) {
    UniqueId added = doAdd(user);
    setupRole(user);
    return added;
  }

  private void setupRole(final ManageableUser user) {
    for (int retry = 0; retry < 5; retry++) {
      try {
        ManageableRole role;
        if (user.getUserName().equals("admin")) {
          if (roleMaster().nameExists("admin")) {
            role = roleMaster().getByName("admin");
            if (role.getAssociatedUsers().contains("admin") == false) {
              role.getAssociatedUsers().add("admin");
            }
          } else {
            role = new ManageableRole("admin");
            role.setDescription("Administrators");
            role.getAssociatedUsers().add("admin");
            role.getAssociatedPermissions().add("*");
          }
        } else {
          if (roleMaster().nameExists("registered")) {
            role = roleMaster().getByName("registered");
            if (role.getAssociatedUsers().contains(caseInsensitive(user.getUserName())) == false) {
              role.getAssociatedUsers().add(caseInsensitive(user.getUserName()));
            }
          } else {
            role = new ManageableRole("registered");
            role.setDescription("Registered users");
            role.getAssociatedUsers().add(caseInsensitive(user.getUserName()));
          }
        }
        roleMaster().save(role);
        return;
        
      } catch (DataVersionException | DataDuplicationException ex) {
        // retry, handling contended user setup senarios
      } catch (RuntimeException ex) {
        // ignore and do not assign a role
        return;
      }
    }
  }

  /**
   * Processes the user add, within a retrying transaction.
   *
   * @param user  the user to add, not null
   * @return the information, not null
   */
  @Override
  Pair<UniqueId, Instant> doAddInTransaction(ManageableUser user) {
    // check if user exists
    if (doNameExists(user.getUserName())) {
      throw new DataDuplicationException("User already exists: " + user.getUserName());
    }
    // insert new row
    final Instant now = now();
    final long docOid = nextId("usr_user_seq");
    final UniqueId uniqueId = createUniqueId(docOid, docOid);
    insertMain(docOid, user);
    insertNameLookup(user.getUserName(), uniqueId.getObjectId());
    insertAlternateIds(docOid, user);
    insertPermissions(docOid, user);
    insertExtensions(docOid, user);
    HistoryEvent event = HistoryEvent.of(HistoryEventType.ADDED, uniqueId, "system", now, ImmutableList.<String>of());
    insertEvent(event, USR_USER_EVENT_SEQ);
    return Pairs.of(uniqueId, now);
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId update(final ManageableUser user) {
    return doUpdate(user);
  }

  /**
   * Processes the update, within a retrying transaction.
   *
   * @param user  the updated user, not null
   * @return the updated document, not null
   */
  @Override
  Pair<UniqueId, Instant> doUpdateInTransaction(ManageableUser user) {
    ObjectId objectId = user.getObjectId();
    String oldVersion = user.getUniqueId().getVersion();
    ManageableUser current = getById(objectId);
    int newVersion = Integer.parseInt(oldVersion) + 1;
    UniqueId newUniqueId = objectId.atVersion(Integer.toString(newVersion));
    // validate
    if (current.equals(user)) {
      return Pairs.of(newUniqueId, null);  // no change
    }
    if (current.getUniqueId().getVersion().equals(oldVersion) == false) {
      throw new DataVersionException("Invalid version, User has already been updated: " + objectId);
    }
    if (caseInsensitive(user.getUserName()).equals(caseInsensitive(current.getUserName())) == false) {
      // check if user exists
      if (doNameExists(user.getUserName())) {
        throw new DataDuplicationException("User cannot be renamed, new name already exists: " + user.getUserName());
      }
      insertNameLookup(user.getUserName(), current.getObjectId());
    }
    // update
    long docOid = extractOid(objectId);
    updateMain(docOid, newVersion, user);
    if (current.getAlternateIds().equals(user.getAlternateIds()) == false) {
      deleteAlternateIds(docOid);
      insertAlternateIds(docOid, user);
    }
    if (current.getAssociatedPermissions().equals(user.getAssociatedPermissions()) == false) {
      deletePermissions(docOid);
      insertPermissions(docOid, user);
    }
    if (current.getProfile().getExtensions().equals(user.getProfile().getExtensions()) == false) {
      deleteExtensions(docOid);
      insertExtensions(docOid, user);
    }
    final Instant now = now();
    List<String> changes = calculateChanges(current, user);
    HistoryEvent event = HistoryEvent.of(HistoryEventType.CHANGED, newUniqueId, "system", now, changes);
    insertEvent(event, USR_USER_EVENT_SEQ);
    return Pairs.of(newUniqueId, now);
  }

  private List<String> calculateChanges(ManageableUser current, ManageableUser updated) {
    List<String> changes = new ArrayList<>();
    // changes
    createChange(changes, current, updated, ManageableUser.meta().userName());
    if (Objects.equals(current.getPasswordHash(), updated.getPasswordHash()) == false) {
      changes.add("Changed password");
    }
    createChange(changes, current, updated, ManageableUser.meta().status());
    createChange(changes, current, updated, ManageableUser.meta().emailAddress());
    SimpleUserProfile currentProfile = SimpleUserProfile.from(current.getProfile());
    SimpleUserProfile updatedProfile = SimpleUserProfile.from(updated.getProfile());
    createChange(changes, currentProfile, updatedProfile, SimpleUserProfile.meta().displayName());
    createChange(changes, currentProfile, updatedProfile, SimpleUserProfile.meta().locale());
    createChange(changes, currentProfile, updatedProfile, SimpleUserProfile.meta().zone());
    createChange(changes, currentProfile, updatedProfile, SimpleUserProfile.meta().dateStyle());
    createChange(changes, currentProfile, updatedProfile, SimpleUserProfile.meta().timeStyle());
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
    // added alternate id
    Set<ExternalId> addedIds = new TreeSet<>(updated.getAlternateIds().getExternalIds());
    addedIds.removeAll(current.getAlternateIds().getExternalIds());
    for (ExternalId alternateId : addedIds) {
      changes.add(StringUtils.left("Added alternateId: " + alternateId, 255));
    }
    // removed alternate id
    Set<ExternalId> removedIds = new TreeSet<>(current.getAlternateIds().getExternalIds());
    removedIds.removeAll(updated.getAlternateIds().getExternalIds());
    for (ExternalId alternateId : removedIds) {
      changes.add(StringUtils.left("Removed alternateId: " + alternateId, 255));
    }
    // added extension
    Set<Entry<String, String>> addedExtensions = new HashSet<>(updated.getProfile().getExtensions().entrySet());
    addedExtensions.removeAll(current.getProfile().getExtensions().entrySet());
    for (Entry<String, String> extension : addedExtensions) {
      changes.add(StringUtils.left("Added extension: " + extension, 255));
    }
    // removed extension
    Set<Entry<String, String>> removedExtensions = new HashSet<>(current.getProfile().getExtensions().entrySet());
    removedExtensions.removeAll(updated.getProfile().getExtensions().entrySet());
    for (Entry<String, String> extension : removedExtensions) {
      changes.add(StringUtils.left("Removed extension: " + extension, 255));
    }
    return changes;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId save(ManageableUser user) {
    ArgumentChecker.notNull(user, "user");
    s_logger.debug("save {}", user.getUserName());
    if (user.getUniqueId() != null) {
      return update(user);
    } else {
      return add(user);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void removeByName(String userName) {
    doRemoveByName(userName);
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
    ManageableUser current = getById(objectId);
    int newVersion = Integer.parseInt(current.getUniqueId().getVersion()) + 1;
    UniqueId newUniqueId = objectId.atVersion(Integer.toString(newVersion));
    
    long docOid = extractOid(objectId);
    deleteAlternateIds(docOid);
    deletePermissions(docOid);
    deleteExtensions(docOid);
    deleteMain(docOid);
    updateNameLookupToDeleted(docOid);
    Instant now = now();
    HistoryEvent event = HistoryEvent.of(HistoryEventType.REMOVED, newUniqueId, "system", now, ImmutableList.<String>of());
    insertEvent(event, USR_USER_EVENT_SEQ);
    return now;
  }

  //-------------------------------------------------------------------------
  @Override
  public UserSearchResult search(UserSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    s_logger.debug("search {}", request);
    if ((request.getObjectIds() != null && request.getObjectIds().isEmpty())) {
      Paging paging = Paging.of(request.getPagingRequest(), 0);
      return new UserSearchResult(paging, new ArrayList<ManageableUser>());
    }
    
    try (Timer.Context context = _searchTimer.time()) {
      return doSearch(request);
    }
  }

  private UserSearchResult doSearch(UserSearchRequest request) {
    PagingRequest pagingRequest = request.getPagingRequest();
    // setup args
    final DbMapSqlParameterSource args = createParameterSource()
      .addValueNullIgnored("user_name_ci", caseInsensitive(getDialect().sqlWildcardAdjustValue(request.getUserName())))
      .addValueNullIgnored("email_address_ci", caseInsensitive(getDialect().sqlWildcardAdjustValue(request.getEmailAddress())))
      .addValueNullIgnored("display_name_ci", caseInsensitive(getDialect().sqlWildcardAdjustValue(request.getDisplayName())))
      .addValueNullIgnored("alternate_id_scheme", getDialect().sqlWildcardAdjustValue(request.getAlternateIdScheme()))
      .addValueNullIgnored("alternate_id_value", getDialect().sqlWildcardAdjustValue(request.getAlternateIdValue()))
      .addValueNullIgnored("permission_str", request.getAssociatedPermission());
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
    List<ManageableUser> results = new ArrayList<>();
    if (pagingRequest.equals(PagingRequest.ALL)) {
      paging = Paging.of(pagingRequest, results);
      results.addAll(namedJdbc.query(sql[0], args, new UserExtractor()));
    } else {
      s_logger.debug("executing sql {}", sql[1]);
      final int count = namedJdbc.queryForObject(sql[1], args, Integer.class);
      paging = Paging.of(pagingRequest, count);
      if (count > 0 && pagingRequest.equals(PagingRequest.NONE) == false) {
        s_logger.debug("executing sql {}", sql[0]);
        results.addAll(namedJdbc.query(sql[0], args, new UserExtractor()));
      }
    }
    return new UserSearchResult(paging, results);
  }

  //-------------------------------------------------------------------------
  @Override
  public UserEventHistoryResult eventHistory(UserEventHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    s_logger.debug("eventHistory {}", request);
    ObjectId objectId = request.getObjectId();
    if (objectId == null) {
      objectId = lookupName(request.getUserName(), OnDeleted.RETURN_ID);
    }
    checkScheme(objectId);
    
    return new UserEventHistoryResult(doEventHistory(objectId));
  }

  //-------------------------------------------------------------------------
  @Override
  public UserAccount getAccount(String userName) {
    ArgumentChecker.notNull(userName, "userName");
    ManageableUser user = getByName(userName);
    SimpleUserAccount account = new SimpleUserAccount(user.getUserName());
    account.setPasswordHash(user.getPasswordHash());
    account.setStatus(user.getStatus());
    account.setAlternateIds(user.getAlternateIds());
    account.setEmailAddress(user.getEmailAddress());
    account.setProfile(user.getProfile());
    return roleMaster().resolveAccount(account);
  }

  @Override
  public DbRoleMaster roleMaster() {
    return _roleMaster;
  }

  //-------------------------------------------------------------------------
  private void insertMain(long docOid, ManageableUser user) {
    final DbMapSqlParameterSource docArgs = mainArgs(docOid, 0, user);
    final String sqlDoc = getElSqlBundle().getSql("InsertMain", docArgs);
    getJdbcTemplate().update(sqlDoc, docArgs);
  }

  private void insertAlternateIds(long docOid, ManageableUser user) {
    final List<DbMapSqlParameterSource> assocList = new ArrayList<DbMapSqlParameterSource>();
    final List<DbMapSqlParameterSource> idKeyList = new ArrayList<DbMapSqlParameterSource>();
    final String sqlSelectIdKey = getElSqlBundle().getSql("SelectIdKey");
    for (ExternalId id : user.getAlternateIds()) {
      final DbMapSqlParameterSource assocArgs = createParameterSource()
        .addValue("doc_id", docOid)
        .addValue("key_scheme", id.getScheme().getName())
        .addValue("key_value", id.getValue());
      assocList.add(assocArgs);
      if (getJdbcTemplate().queryForList(sqlSelectIdKey, assocArgs).isEmpty()) {
        // select avoids creating unecessary id, but id may still not be used
        final long idKeyId = nextId("usr_user_idkey_seq");
        final DbMapSqlParameterSource idkeyArgs = createParameterSource()
          .addValue("idkey_id", idKeyId)
          .addValue("key_scheme", id.getScheme().getName())
          .addValue("key_value", id.getValue());
        idKeyList.add(idkeyArgs);
      }
    }
    final String sqlIdKey = getElSqlBundle().getSql("InsertIdKey");
    final String sqlDoc2IdKey = getElSqlBundle().getSql("InsertDoc2IdKey");
    getJdbcTemplate().batchUpdate(sqlIdKey, idKeyList.toArray(new DbMapSqlParameterSource[idKeyList.size()]));
    getJdbcTemplate().batchUpdate(sqlDoc2IdKey, assocList.toArray(new DbMapSqlParameterSource[assocList.size()]));
  }

  private void insertPermissions(long docOid, ManageableUser user) {
    final List<DbMapSqlParameterSource> argsList = new ArrayList<DbMapSqlParameterSource>();
    for (String permission : user.getAssociatedPermissions()) {
      argsList.add(createParameterSource()
        .addValue("id", nextId("usr_user_perm_seq"))
        .addValue("doc_id", docOid)
        .addValue("permission_str", permission));
    }
    final String sql = getElSqlBundle().getSql("InsertAssocPermission");
    getJdbcTemplate().batchUpdate(sql, argsList.toArray(new DbMapSqlParameterSource[argsList.size()]));
  }

  private void insertExtensions(long docOid, ManageableUser user) {
    final List<DbMapSqlParameterSource> argsList = new ArrayList<DbMapSqlParameterSource>();
    for (Entry<String, String> entry : user.getProfile().getExtensions().entrySet()) {
      argsList.add(createParameterSource()
        .addValue("id", nextId("usr_user_extn_seq"))
        .addValue("doc_id", docOid)
        .addValue("extn_key", entry.getKey())
        .addValue("extn_value", entry.getValue()));
    }
    final String sql = getElSqlBundle().getSql("InsertExtension");
    getJdbcTemplate().batchUpdate(sql, argsList.toArray(new DbMapSqlParameterSource[argsList.size()]));
  }

  //-------------------------------------------------------------------------
  private void updateMain(long docOid, int version, ManageableUser user) {
    final DbMapSqlParameterSource docArgs = mainArgs(docOid, version, user);
    final String sqlDoc = getElSqlBundle().getSql("UpdateMain", docArgs);
    getJdbcTemplate().update(sqlDoc, docArgs);
  }

  private DbMapSqlParameterSource mainArgs(long docOid, int version, ManageableUser user) {
    final DbMapSqlParameterSource docArgs = createParameterSource()
      .addValue("doc_id", docOid)
      .addValue("version", version)
      .addValue("user_name", user.getUserName())
      .addValue("user_name_ci", caseInsensitive(user.getUserName()))
      .addValue("password_hash", user.getPasswordHash())
      .addValue("status", user.getStatus().name().substring(0, 1))
      .addValue("email_address", user.getEmailAddress())
      .addValue("email_address_ci", caseInsensitive(user.getEmailAddress()))
      .addValue("display_name", user.getProfile().getDisplayName())
      .addValue("display_name_ci", caseInsensitive(user.getProfile().getDisplayName()))
      .addValue("locale_tag", user.getProfile().getLocale().toLanguageTag())
      .addValue("time_zone", user.getProfile().getZone().getId())
      .addValue("date_fmt_style", user.getProfile().getDateStyle().name())
      .addValue("time_fmt_style", user.getProfile().getTimeStyle().name());
    return docArgs;
  }

  //-------------------------------------------------------------------------
  private void deleteMain(long docOid) {
    final DbMapSqlParameterSource args = createParameterSource()
        .addValue("doc_id", docOid);
    final String sql = getElSqlBundle().getSql("DeleteMain", args);
    getJdbcTemplate().update(sql, args);
  }

  private void deleteAlternateIds(long docOid) {
    final DbMapSqlParameterSource args = createParameterSource()
        .addValue("doc_id", docOid);
    final String sql = getElSqlBundle().getSql("DeleteAlternateIds", args);
    getJdbcTemplate().update(sql, args);
  }

  private void deletePermissions(long docOid) {
    final DbMapSqlParameterSource args = createParameterSource()
        .addValue("doc_id", docOid);
    final String sql = getElSqlBundle().getSql("DeleteAssocPermissions", args);
    getJdbcTemplate().update(sql, args);
  }

  private void deleteExtensions(long docOid) {
    final DbMapSqlParameterSource args = createParameterSource()
        .addValue("doc_id", docOid);
    final String sql = getElSqlBundle().getSql("DeleteExtensions", args);
    getJdbcTemplate().update(sql, args);
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a ManageableUser.
   */
  final class UserExtractor implements ResultSetExtractor<List<ManageableUser>> {
    private long _previousDocId = -1L;
    private ManageableUser _currUser;
    private Set<ExternalId> _currExternalIds = new HashSet<>();
    private Set<String> _currPermissions = new LinkedHashSet<>();
    private Map<String, String> _currExtensions = new LinkedHashMap<>();
    private List<ManageableUser> _users = new ArrayList<>();

    @Override
    public List<ManageableUser> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long docId = rs.getLong("DOC_ID");
        // DOC_ID tells us when we're on a new document.
        if (docId != _previousDocId) {
          if (_previousDocId >= 0) {
            _currUser.setAlternateIds(ExternalIdBundle.of(_currExternalIds));
            _currUser.setAssociatedPermissions(_currPermissions);
            _currUser.getProfile().setExtensions(_currExtensions);
          }
          _previousDocId = docId;
          buildUser(rs, docId);
          _currExternalIds.clear();
          _currPermissions.clear();
          _currExtensions.clear();
        }
        String idKey = rs.getString("KEY_SCHEME");
        String idValue = rs.getString("KEY_VALUE");
        if (idKey != null && idValue != null) {
          _currExternalIds.add(ExternalId.of(idKey, idValue));
        }
        String permissionPattern = rs.getString("PERMISSION_STR");
        if (permissionPattern != null) {
          _currPermissions.add(permissionPattern);
        }
        String extKey = rs.getString("EXTN_KEY");
        String extValue = rs.getString("EXTN_VALUE");
        if (extKey != null && extValue != null) {
          _currExtensions.put(extKey, extValue);
        }
      }
      // patch up last document read
      if (_previousDocId >= 0) {
        _currUser.setAlternateIds(ExternalIdBundle.of(_currExternalIds));
        _currUser.setAssociatedPermissions(_currPermissions);
        _currUser.getProfile().setExtensions(_currExtensions);
      }
      return _users;
    }

    private void buildUser(final ResultSet rs, final long docId) throws SQLException {
      int version = rs.getInt("VERSION");
      UniqueId uniqueId = UniqueId.of(getUniqueIdScheme(), Long.toString(docId), Integer.toString(version));
      ManageableUser user = new ManageableUser(rs.getString("USER_NAME"));
      user.setUniqueId(uniqueId);
      user.setPasswordHash(rs.getString("PASSWORD_HASH"));
      user.setStatus(extractEnum(rs.getString("STATUS"), UserAccountStatus.values()));
      user.setEmailAddress(rs.getString("EMAIL_ADDRESS"));
      user.getProfile().setDisplayName(rs.getString("DISPLAY_NAME"));
      user.getProfile().setLocale(Locale.forLanguageTag(rs.getString("LOCALE_TAG")));
      user.getProfile().setZone(ZoneId.of(rs.getString("TIME_ZONE")));
      user.getProfile().setDateStyle(DateStyle.valueOf(rs.getString("DATE_FMT_STYLE")));
      user.getProfile().setTimeStyle(TimeStyle.valueOf(rs.getString("TIME_FMT_STYLE")));
      _currUser = user;
      _users.add(user);
    }
  }

}
