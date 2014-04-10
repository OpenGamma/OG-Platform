/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.threeten.bp.Instant;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableList;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.user.HistoryEvent;
import com.opengamma.master.user.HistoryEventType;
import com.opengamma.masterdb.AbstractDbMaster;
import com.opengamma.masterdb.ConfigurableDbChangeProvidingMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.metric.MetricProducer;
import com.opengamma.util.tuple.Pair;

/**
 * Abstract master implementation using a database for persistence.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 * 
 * @param <T>  the object type
 */
public abstract class AbstractDbUserMaster<T extends UniqueIdentifiable>
    extends AbstractDbMaster
    implements MetricProducer, ConfigurableDbChangeProvidingMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbUserMaster.class);

  /**
   * The change manager.
   */
  private ChangeManager _changeManager = new BasicChangeManager();
  // -----------------------------------------------------------------
  // TIMERS FOR METRICS GATHERING
  // By default these do nothing. Registration will replace them
  // so that they actually do something.
  // -----------------------------------------------------------------
  private Timer _getByIdTimer = new Timer();
  private Timer _lookupNameTimer = new Timer();
  private Timer _addTimer = new Timer();
  private Timer _updateTimer = new Timer();
  private Timer _removeByIdTimer = new Timer();
  private Timer _eventHistoryTimer = new Timer();

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   * @param defaultScheme  the default unique identifier scheme, not null
   */
  public AbstractDbUserMaster(final DbConnector dbConnector, final String defaultScheme) {
    super(dbConnector, defaultScheme);
  }

  @Override
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailedRegistry, String namePrefix) {
    _getByIdTimer = summaryRegistry.timer(namePrefix + ".get");
    _lookupNameTimer = summaryRegistry.timer(namePrefix + ".lookupname");
    _addTimer = summaryRegistry.timer(namePrefix + ".add");
    _updateTimer = summaryRegistry.timer(namePrefix + ".update");
    _removeByIdTimer = summaryRegistry.timer(namePrefix + ".remove");
    _eventHistoryTimer = summaryRegistry.timer(namePrefix + ".history");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the change manager.
   *
   * @return the change manager, not null
   */
  @Override
  public ChangeManager getChangeManager() {
    return _changeManager;
  }

  /**
   * Sets the change manager.
   *
   * @param changeManager  the change manager, not null
   */
  @Override
  public void setChangeManager(final ChangeManager changeManager) {
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  public ChangeManager changeManager() {
    return _changeManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Convert a name to an object identifier.
   * <p>
   * If an object is renamed, the old name remains as an alias.
   * The separate name resolution handles that case.
   * 
   * @param name  the name, not null
   * @param onDeleted  how to handle deletion
   * @return the object identifier, null if not found
   */
  ObjectId lookupName(String name, OnDeleted onDeleted) {
    ArgumentChecker.notNull(name, "name");
    try (Timer.Context context = _lookupNameTimer.time()) {
      final DbMapSqlParameterSource args = createParameterSource()
        .addValue("name_ci", caseInsensitive(name));
      final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate();
      final String sql = getElSqlBundle().getSql("GetIdByName", args);
      SqlRowSet rowSet = namedJdbc.queryForRowSet(sql, args);
      if (rowSet.next() == false) {
        throw new DataNotFoundException("Name not found: " + name);
      }
      String deleted = rowSet.getString("DELETED");
      if (deleted.equals("Y")) {
        if (onDeleted == OnDeleted.RETURN_NULL) {
          return null;
        } else if (onDeleted == OnDeleted.EXCEPTION) {
          throw new DataNotFoundException("Name not found: " + name);
        }
      }
      return createObjectId(rowSet.getLong("DOC_ID")).getObjectId();
    }
  }

  /**
   * Checks if a name exists already.
   * 
   * @param name  the name, not null
   * @return true if exists
   */
  boolean doNameExists(String name) {
    ArgumentChecker.notNull(name, "name");
    try (Timer.Context context = _lookupNameTimer.time()) {
      final DbMapSqlParameterSource args = createParameterSource()
        .addValue("name_ci", caseInsensitive(name));
      final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate();
      final String sql = getElSqlBundle().getSql("GetIdByName", args);
      SqlRowSet rowSet = namedJdbc.queryForRowSet(sql, args);
      return rowSet.next();
    }
  }

  T doGetById(ObjectId objectId, ResultSetExtractor<List<T>> extractor) {
    try (Timer.Context context = _getByIdTimer.time()) {
      final long oid = extractOid(objectId);
      final DbMapSqlParameterSource args = createParameterSource()
        .addValue("doc_id", oid);
      final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate();
      final String sql = getElSqlBundle().getSql("GetById", args);
      final List<T> users = namedJdbc.query(sql, args, extractor);
      if (users.isEmpty()) {
        throw new DataNotFoundException("Identifier not found: " + objectId);
      }
      return users.get(0);
    }
  }

  /**
   * Checks if a user exists already.
   * 
   * @param objectId  the user identifier, not null
   * @return true if exists
   */
  boolean idExists(ObjectId objectId) {
    final long oid = extractOid(objectId);
    final DbMapSqlParameterSource args = createParameterSource()
      .addValue("doc_id", oid);
    final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate();
    final String sql = getElSqlBundle().getSql("GetById", args);
    SqlRowSet rowSet = namedJdbc.queryForRowSet(sql, args);
    return rowSet.next();
  }

  //-------------------------------------------------------------------------
  UniqueId doAdd(final T user) {
    ArgumentChecker.notNull(user, "user");
    s_logger.debug("add {}", user);
    
    try (Timer.Context context = _addTimer.time()) {
      final Pair<UniqueId, Instant> added = getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Pair<UniqueId, Instant>>() {
        @Override
        public Pair<UniqueId, Instant> doInTransaction(final TransactionStatus status) {
          return doAddInTransaction(user);
        }
      });
      changeManager().entityChanged(ChangeType.ADDED, added.getFirst().getObjectId(), added.getSecond(), null, added.getSecond());
      return added.getFirst();
    }
  }

  /**
   * Processes the user add, within a retrying transaction.
   *
   * @param user  the user to add, not null
   * @return the information, not null
   */
  abstract Pair<UniqueId, Instant> doAddInTransaction(T user);

  //-------------------------------------------------------------------------
  UniqueId doUpdate(final T user) {
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(user.getUniqueId(), "user.uniqueId");
    ArgumentChecker.isTrue(user.getUniqueId().isVersioned(), "UniqueId must be versioned");
    checkScheme(user.getUniqueId());
    s_logger.debug("update {}", user);
    
    try (Timer.Context context = _updateTimer.time()) {
      final Pair<UniqueId, Instant> updated = getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Pair<UniqueId, Instant>>() {
        @Override
        public Pair<UniqueId, Instant> doInTransaction(final TransactionStatus status) {
          return doUpdateInTransaction(user);
        }
      });
      if (updated.getSecond() != null) {
        changeManager().entityChanged(ChangeType.CHANGED, updated.getFirst().getObjectId(), updated.getSecond(), null, updated.getSecond());
      }
      return updated.getFirst();
    }
  }

  /**
   * Processes the update, within a retrying transaction.
   *
   * @param user  the updated user, not null
   * @return the updated document, not null
   */
  abstract Pair<UniqueId, Instant> doUpdateInTransaction(T user);

  void createChange(List<String> changes, Bean current, Bean updated, MetaProperty<?> metaProperty) {
    Object currentValue = metaProperty.get(current);
    Object updatedValue = metaProperty.get(updated);
    if (Objects.equals(currentValue, updatedValue) == false) {
      String text = "Changed " + metaProperty.name() + ": " + currentValue + " -> " + updatedValue;
      changes.add(StringUtils.left(text, 255));
    }
  }

  //-------------------------------------------------------------------------
  void doRemoveByName(String name) {
    ArgumentChecker.notNull(name, "name");
    s_logger.debug("removeByName {}", name);
    ObjectId oid = lookupName(name, OnDeleted.RETURN_NULL);
    if (oid == null) {
      return;  // already deleted
    }
    doRemoveById(oid);
  }

  void doRemoveById(final ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    checkScheme(objectId);
    s_logger.debug("removeById {}", objectId);
    
    try (Timer.Context context = _removeByIdTimer.time()) {
      if (idExists(objectId)) {
        final Instant removedInstant = getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<Instant>() {
          @Override
          public Instant doInTransaction(final TransactionStatus status) {
            return doRemoveInTransaction(objectId);
          }
        });
        changeManager().entityChanged(ChangeType.REMOVED, objectId, removedInstant, null, removedInstant);
      }
    }
  }

  /**
   * Processes the document update, within a retrying transaction.
   *
   * @param objectId  the object identifier to remove, not null
   * @return the updated document, not null
   */
  abstract Instant doRemoveInTransaction(final ObjectId objectId);

  List<HistoryEvent> doEventHistory(ObjectId objectId) {
    try (Timer.Context context = _eventHistoryTimer.time()) {
      final long oid = extractOid(objectId);
      final DbMapSqlParameterSource args = createParameterSource()
        .addValue("doc_id", oid);
      final NamedParameterJdbcOperations namedJdbc = getDbConnector().getJdbcTemplate();
      final String sql = getElSqlBundle().getSql("GetEventHistory", args);
      return namedJdbc.query(sql, args, new EventExtractor());
    }
  }

  //-------------------------------------------------------------------------
  String caseInsensitive(String name) {
    return name != null ? name.toLowerCase(Locale.ROOT) : name;
  }

  void insertNameLookup(String name, ObjectId objectId) {
    final DbMapSqlParameterSource eventArgs = createParameterSource()
        .addValue("name_ci", caseInsensitive(name))
        .addValue("doc_id", extractOid(objectId));
    final String sqlEvent = getElSqlBundle().getSql("InsertNameLookup");
    getJdbcTemplate().update(sqlEvent, eventArgs);
  }

  void insertEvent(HistoryEvent event, String eventIdSequence) {
    Long eventId = nextId(eventIdSequence);
    final DbMapSqlParameterSource eventArgs = createParameterSource()
        .addValue("id", eventId)
        .addValue("doc_id", extractOid(event.getUniqueId()))
        .addValue("version", Integer.parseInt(event.getUniqueId().getVersion()))
        .addValue("event_type", event.getType().name().substring(0, 1))
        .addValue("active_user", "system")
        .addValue("event_instant", DbDateUtils.toSqlTimestamp(event.getInstant()));
    final String sqlEvent = getElSqlBundle().getSql("InsertEvent");
    getJdbcTemplate().update(sqlEvent, eventArgs);
    
    final List<DbMapSqlParameterSource> itemList = new ArrayList<DbMapSqlParameterSource>();
    for (String description : event.getChanges()) {
      final DbMapSqlParameterSource itemArgs = createParameterSource()
          .addValue("id", nextId(eventIdSequence))
          .addValue("event_id", eventId)
          .addValue("description", description);
      itemList.add(itemArgs);
    }
    final String sqlEventItem = getElSqlBundle().getSql("InsertEventItem");
    getJdbcTemplate().batchUpdate(sqlEventItem, itemList.toArray(new DbMapSqlParameterSource[itemList.size()]));
  }

  void updateNameLookupToDeleted(long docOid) {
    final DbMapSqlParameterSource docArgs = createParameterSource()
        .addValue("doc_id", docOid);
    final String sqlDoc = getElSqlBundle().getSql("UpdateNameLookupToDeleted", docArgs);
    getJdbcTemplate().update(sqlDoc, docArgs);
  }

  /**
   * Converts a single character to an enum value.
   * 
   * @param typeStr  the type character, not null
   * @param values  the enum values, not null
   * @return the enum, not null
   */
  <E extends Enum<E>> E extractEnum(String typeStr, E[] values) {
    for (E t : values) {
      if (typeStr.equals(t.name().substring(0, 1))) {
        return t;
      }
    }
    throw new IllegalStateException("Invalid enum value: " + typeStr);
  }

  //-------------------------------------------------------------------------
  /**
   * How to handle deletion.
   */
  enum OnDeleted {
    RETURN_NULL,
    RETURN_ID,
    EXCEPTION,
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a HistoryEvent.
   */
  final class EventExtractor implements ResultSetExtractor<List<HistoryEvent>> {

    @Override
    public List<HistoryEvent> extractData(final ResultSet rs) throws SQLException, DataAccessException {
      List<HistoryEvent> events = new ArrayList<>();
      long lastId = -1;
      HistoryEvent current = null;
      List<String> currentDescriptions = new ArrayList<>();
      while (rs.next()) {
        final long docId = rs.getLong("ID");
        if (lastId != docId) {
          if (current != null) {
            events.add(current.toBuilder().changes(currentDescriptions).build());
            currentDescriptions.clear();
          }
          lastId = docId;
          current = buildEvent(rs);
        }
        final String description = rs.getString("DESCRIPTION");
        if (description != null) {
          currentDescriptions.add(description);
        }
      }
      if (current != null) {
        events.add(current.toBuilder().changes(currentDescriptions).build());
      }
      return events;
    }

    private HistoryEvent buildEvent(final ResultSet rs) throws SQLException {
      long userId = rs.getInt("DOC_ID");
      int version = rs.getInt("VERSION");
      UniqueId uniqueId = UniqueId.of(getUniqueIdScheme(), Long.toString(userId), Integer.toString(version));
      String typeStr = rs.getString("EVENT_TYPE");
      HistoryEventType type = extractEnum(typeStr, HistoryEventType.values());
      String activeUser = rs.getString("ACTIVE_USER");
      Instant instant = DbDateUtils.fromSqlTimestamp(rs.getTimestamp("EVENT_INSTANT"));
      return HistoryEvent.of(type, uniqueId, activeUser, instant, ImmutableList.<String>of());
    }
  }

}
