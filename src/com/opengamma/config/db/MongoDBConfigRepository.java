/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config.db;

import static com.opengamma.config.ConfigDocument.CREATION_INSTANT_FUDGE_FIELD_NAME;
import static com.opengamma.config.ConfigDocument.LAST_READ_INSTANT_FUDGE_FIELD_NAME;
import static com.opengamma.config.ConfigDocument.NAME_FUDGE_FIELD_NAME;
import static com.opengamma.config.ConfigDocument.OID_FUDGE_FIELD_NAME;
import static com.opengamma.config.ConfigDocument.VALUE_FUDGE_FIELD_NAME;
import static com.opengamma.config.ConfigDocument.VERSION_FUDGE_FIELD_NAME;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.time.Instant;
import javax.time.TimeSource;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.ObjectId;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigDocumentRepository;
import com.opengamma.config.DefaultConfigDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.MongoDBConnectionSettings;

/**
 * General purpose configuration data loader backed by MongoDB
 * MongoDBConnectionSettings specifies mongo host, port, database and collectionName to use
 * If collectionName is null, the document classname will be used
 * 
 * @param <T> Configuration Document EntityType
 */
public class MongoDBConfigRepository<T> implements ConfigDocumentRepository<T> {

  private static final Logger s_logger = LoggerFactory.getLogger(MongoDBConfigRepository.class);

  private static final String[] INDICES = {OID_FUDGE_FIELD_NAME, NAME_FUDGE_FIELD_NAME, CREATION_INSTANT_FUDGE_FIELD_NAME, LAST_READ_INSTANT_FUDGE_FIELD_NAME};
  
  private final FudgeContext _fudgeContext;

  private Class<T> _entityClazz;
  private final String _mongoHost;
  private final int _mongoPort;

  private final Mongo _mongo;
  private final DB _mongoDB;
  private final String _collectionName;
  private final boolean _updateLastReadTime;
  /**
   * The time-source to use.
   */
  private TimeSource _timeSource = TimeSource.system();

  public MongoDBConfigRepository(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings,
      final FudgeContext fudgeContext, boolean updateLastRead, final FudgeBuilder<T> messageBuilder) {
    ArgumentChecker.notNull(documentClazz, "document class");
    ArgumentChecker.notNull(mongoSettings, "MongoDB settings");
    ArgumentChecker.notNull(fudgeContext, "FudgeContext");

    _entityClazz = documentClazz;
    _fudgeContext = fudgeContext;

    _mongoHost = mongoSettings.getHost();
    _mongoPort = mongoSettings.getPort();
    if (mongoSettings.getCollectionName() != null) {
      _collectionName = mongoSettings.getCollectionName();
    } else {
      _collectionName = _entityClazz.getSimpleName();
    }
    //set connection for printing
    mongoSettings.setCollectionName(_collectionName);
    s_logger.info("Connecting to {}", mongoSettings);
    try {
      _mongo = new Mongo(_mongoHost, _mongoPort);
      _mongoDB = _mongo.getDB(mongoSettings.getDatabase());
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Unable to connect to MongoDB at " + mongoSettings, e);
    }

    if (messageBuilder != null) {
      fudgeContext.getObjectDictionary().addBuilder(_entityClazz, messageBuilder);
    }

    ensureIndices();
    _updateLastReadTime = updateLastRead;
    String status = _updateLastReadTime ? "updateLastReadTime" : "readWithoutUpdate";
    s_logger.info("creating MongoDBConfigurationRepo for {}", status);
  }

  /**
   * 
   */
  private void ensureIndices() {
    //create necessary indices
    DBCollection dbCollection = _mongoDB.getCollection(_collectionName);
    for (String field : INDICES) {
      s_logger.info("creating index for {} {}:{}", new Object[] {field, getMongoDB().getName(), getCollectionName()});
      //create ascending and descending index
      dbCollection.ensureIndex(new BasicDBObject(field, 1), "ix_" + getCollectionName() + "_" + field + "_asc");
      dbCollection.ensureIndex(new BasicDBObject(field, -1), "ix_" + getCollectionName() + "_" + field + "_desc");
    }
  }

  public MongoDBConfigRepository(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings, boolean updateLastRead, final FudgeBuilder<T> messageBuilder) {
    this(documentClazz, mongoSettings, new FudgeContext(), updateLastRead, messageBuilder);
  }

  public MongoDBConfigRepository(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings, boolean updateLastRead) {
    this(documentClazz, mongoSettings, updateLastRead, null);
  }
  
  public MongoDBConfigRepository(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings) {
    this(documentClazz, mongoSettings, true);
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * @return the mongoHost
   */
  public String getMongoHost() {
    return _mongoHost;
  }

  /**
   * @return the mongoPort
   */
  public int getMongoPort() {
    return _mongoPort;
  }

  /**
   * @return the mongo
   */
  public Mongo getMongo() {
    return _mongo;
  }

  /**
   * @return the mongoDB
   */
  public DB getMongoDB() {
    return _mongoDB;
  }

  /**
   * @return the collectionName
   */
  public String getCollectionName() {
    return _collectionName;
  }

  /**
   * Gets the time-source that determines the current time.
   * @return the time-source, not null
   */
  public TimeSource getTimeSource() {
    return _timeSource;
  }

  /**
   * @param timeSource the timeSource to set
   */
  public void setTimeSource(TimeSource timeSource) {
    Validate.notNull(timeSource, "TimeSource must not be null");
    _timeSource = timeSource;
  }

  @Override
  public ConfigDocument<T> getByName(String name) {
    ArgumentChecker.notNull(name, "name");
    //get latest version by name
    DBObject queryObj = new BasicDBObject(NAME_FUDGE_FIELD_NAME, name);

    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    if (s_logger.isDebugEnabled()) {
      DBCursor find = dbCollection.find(queryObj);
      while (find.hasNext()) {
        DBObject next = find.next();
        s_logger.debug("found doc = {}", next);
      }
    }
    BasicDBObject sortObj = new BasicDBObject(CREATION_INSTANT_FUDGE_FIELD_NAME, -1);
    if (_updateLastReadTime) {
      return findAndUpdateLastRead(queryObj, sortObj);
    } else {
      return findWithoutUpdate(queryObj, sortObj);
    }
  }

  private ConfigDocument<T> findWithoutUpdate(DBObject queryObj, BasicDBObject sortObj) {
    s_logger.debug("findWithoutUpdate  queryObj={} sortObj={}", queryObj, sortObj);
    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    DBCursor cursor = null;
    if (sortObj != null) {
      cursor = dbCollection.find(queryObj).sort(sortObj).limit(1);
    } else {
      cursor = dbCollection.find(queryObj).limit(1);
    }
    //should return the latest version
    if (cursor.hasNext()) {
      DBObject findByName = cursor.next();
      return dBObjectToConficDoc(findByName);
    }
    return null;
  }

  private ConfigDocument<T> dBObjectToConficDoc(DBObject dbObject) {
    s_logger.debug("converting dbOject = {} to config doc", dbObject);
    DBObject valueData = (DBObject) dbObject.get(VALUE_FUDGE_FIELD_NAME);
    FudgeSerializationContext fsc = new FudgeSerializationContext(getFudgeContext());
    MutableFudgeFieldContainer dbObjecToFudge = fsc.objectToFudgeMsg(valueData);
    FudgeDeserializationContext fdc = new FudgeDeserializationContext(getFudgeContext());
    T value = fdc.fudgeMsgToObject(_entityClazz, dbObjecToFudge);
    String objectId = (String) dbObject.get("_id");
    String oid = (String) dbObject.get(OID_FUDGE_FIELD_NAME);
    String name = (String) dbObject.get(NAME_FUDGE_FIELD_NAME);
    int version = (Integer) dbObject.get(VERSION_FUDGE_FIELD_NAME);
    Date creationTime = (Date) dbObject.get(CREATION_INSTANT_FUDGE_FIELD_NAME);
    Date lastRead = (Date) dbObject.get(LAST_READ_INSTANT_FUDGE_FIELD_NAME);
    return new DefaultConfigDocument<T>(objectId, oid, version, name, Instant.ofEpochMillis(creationTime
        .getTime()), Instant.ofEpochMillis(lastRead.getTime()), value);
  }

  private ConfigDocument<T> findAndUpdateLastRead(DBObject queryObj, BasicDBObject sortObj) {
    ConfigDocument<T> result = null;
    Date now = new Date(_timeSource.instant().toEpochMillisLong());
    DBObject updateObj = new BasicDBObject("$set", new BasicDBObject(LAST_READ_INSTANT_FUDGE_FIELD_NAME, now));

    BasicDBObjectBuilder objectBuilder = BasicDBObjectBuilder.start().append("findandmodify", getCollectionName())
        .append("query", queryObj);

    if (sortObj != null) {
      objectBuilder.append("sort", sortObj);
    }
    objectBuilder.append("update", updateObj);
    DBObject cmd = objectBuilder.get();
    s_logger.debug("cmd = {}", cmd);
    // run 
    DBObject commandResult = getMongoDB().command(cmd);
    s_logger.debug("cmdResult = {}", commandResult);
    Object commandResultValue = commandResult.get("value");
    String errMsg = (String) commandResult.get("errmsg");
    double ok = (Double) commandResult.get("ok");
    if (commandResultValue != null && ok == 1.0 && errMsg == null) {
      DBObject findByName = (DBObject) commandResultValue;
      findByName.put(LAST_READ_INSTANT_FUDGE_FIELD_NAME, now);
      result = dBObjectToConficDoc(findByName);
    } else {
      s_logger.warn("error finding the requested doc Mongo err = {}", errMsg);
    }
    return result;
  }

  @Override
  public ConfigDocument<T> getByName(String currentName, Instant effectiveInstant) {
    ArgumentChecker.notNull(currentName, "currentName");
    ArgumentChecker.notNull(effectiveInstant, "effectiveInstant");

    DBObject queryObj = new BasicDBObject();
    DBObject filter = new BasicDBObject("$lte", new Date(effectiveInstant.toEpochMillisLong()));
    queryObj.put(CREATION_INSTANT_FUDGE_FIELD_NAME, filter);
    queryObj.put(NAME_FUDGE_FIELD_NAME, currentName);

    s_logger.debug("query = {}", queryObj);
    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    if (s_logger.isDebugEnabled()) {
      DBCursor find = dbCollection.find(queryObj);
      while (find.hasNext()) {
        DBObject next = find.next();
        s_logger.debug("found doc = {}", next);
      }
    }

    BasicDBObject sortObj = new BasicDBObject(CREATION_INSTANT_FUDGE_FIELD_NAME, -1);
    if (_updateLastReadTime) {
      return findAndUpdateLastRead(queryObj, sortObj);
    } else {
      return findWithoutUpdate(queryObj, sortObj);
    }
  }

  @Override
  public List<ConfigDocument<T>> getSequence(String oid, Instant startDate, Instant endDate) {
    ArgumentChecker.notNull(oid, "oid");
    ArgumentChecker.notNull(startDate, "startDate");
    Date end = null;
    if (endDate == null) {
      end = new Date(_timeSource.instant().toEpochMillisLong());
    } else {
      end = new Date(endDate.toEpochMillisLong());
    }

    List<ConfigDocument<T>> result = new ArrayList<ConfigDocument<T>>();

    BasicDBObject query = new BasicDBObject(OID_FUDGE_FIELD_NAME, oid);
    query.put(CREATION_INSTANT_FUDGE_FIELD_NAME, new BasicDBObject("$lte", end).append("$gte", new Date(startDate
        .toEpochMillisLong())));

    s_logger.debug("query = {}", query);

    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    DBCursor cursor = dbCollection.find(query).sort(new BasicDBObject(CREATION_INSTANT_FUDGE_FIELD_NAME, -1));

    while (cursor.hasNext()) {
      DBObject next = cursor.next();
      ConfigDocument<T> conficDoc = dBObjectToConficDoc(next);
      result.add(conficDoc);
    }

    return result;
  }

  @Override
  public ConfigDocument<T> insertNewItem(String name, T value) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(value, "value");
    String objectId = ObjectId.get().toString();
    Date now = new Date(_timeSource.instant().toEpochMillisLong());
    int version = 1;
    MutableFudgeFieldContainer msg = _fudgeContext.newMessage();
    msg.add("_id", objectId);
    msg.add(OID_FUDGE_FIELD_NAME, objectId);
    msg.add(VERSION_FUDGE_FIELD_NAME, version);
    msg.add(NAME_FUDGE_FIELD_NAME, name);
    msg.add(CREATION_INSTANT_FUDGE_FIELD_NAME, now);
    msg.add(LAST_READ_INSTANT_FUDGE_FIELD_NAME, now);
    msg.add(VALUE_FUDGE_FIELD_NAME, _fudgeContext.toFudgeMsg(value).getMessage());

    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    FudgeDeserializationContext fdc = new FudgeDeserializationContext(getFudgeContext());

    DBObject doc = fdc.fudgeMsgToObject(DBObject.class, msg);
    s_logger.debug("inserting new doc {}", doc);
    dbCollection.insert(doc);
    DBObject lastErr = getMongoDB().getLastError();
    if (lastErr.get("err") != null) {
      throw new OpenGammaRuntimeException("Error: " + lastErr.toString());
    }

    Instant creationInstant = Instant.ofEpochMillis(now.getTime());
    return new DefaultConfigDocument<T>(objectId, objectId, version, name, creationInstant, creationInstant,
        value);
  }

  @Override
  public ConfigDocument<T> insertNewVersion(String oid, T value) {
    ArgumentChecker.notNull(oid, "oid");
    ArgumentChecker.notNull(value, "value");
    //get latest version
    DBObject query = new BasicDBObject();
    query.put(OID_FUDGE_FIELD_NAME, oid);

    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    DBObject fields = new BasicDBObject();
    fields.put(VERSION_FUDGE_FIELD_NAME, 1);
    fields.put(NAME_FUDGE_FIELD_NAME, 1);
    DBCursor cursor = dbCollection.find(query, fields).sort(new BasicDBObject(VERSION_FUDGE_FIELD_NAME, -1)).limit(1);

    //should return the latest version
    DBObject previousDocObj = cursor.next();
    int previousVersion = (Integer) previousDocObj.get(VERSION_FUDGE_FIELD_NAME);
    String name = (String) previousDocObj.get(NAME_FUDGE_FIELD_NAME);

    return insertVersionDocument(oid, value, previousVersion, name);
  }

  private ConfigDocument<T> insertVersionDocument(String oid, T value, int previousVersion, String name) {
    DBCollection dbCollection;
    String objectId = ObjectId.get().toString();
    Date now = new Date(_timeSource.instant().toEpochMillisLong());
    MutableFudgeFieldContainer msg = _fudgeContext.newMessage();
    msg.add("_id", objectId);
    msg.add(OID_FUDGE_FIELD_NAME, oid);
    msg.add(VERSION_FUDGE_FIELD_NAME, ++previousVersion);
    msg.add(NAME_FUDGE_FIELD_NAME, name);
    msg.add(CREATION_INSTANT_FUDGE_FIELD_NAME, now);
    msg.add(LAST_READ_INSTANT_FUDGE_FIELD_NAME, now);
    msg.add(VALUE_FUDGE_FIELD_NAME, _fudgeContext.toFudgeMsg(value).getMessage());

    dbCollection = getMongoDB().getCollection(getCollectionName());
    FudgeDeserializationContext fdc = new FudgeDeserializationContext(getFudgeContext());

    DBObject doc = fdc.fudgeMsgToObject(DBObject.class, msg);
    s_logger.debug("inserting new version {}", doc);
    dbCollection.insert(doc);

    DBObject lastErr = getMongoDB().getLastError();
    if (lastErr.get("err") != null) {
      throw new OpenGammaRuntimeException("Error: " + lastErr.toString());
    }
    Instant creationInstant = Instant.ofEpochMillis(now.getTime());
    return new DefaultConfigDocument<T>(objectId, oid, previousVersion, name, creationInstant, creationInstant,
        value);
  }

  @Override
  public ConfigDocument<T> insertNewVersion(String oid, String name, T value) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(value, "value");
    //get latest version
    DBObject query = new BasicDBObject();
    query.put(OID_FUDGE_FIELD_NAME, oid);

    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    DBObject fields = new BasicDBObject();
    fields.put(VERSION_FUDGE_FIELD_NAME, 1);
    DBCursor cursor = dbCollection.find(query, fields).sort(new BasicDBObject(VERSION_FUDGE_FIELD_NAME, -1)).limit(1);

    //should return the latest version
    DBObject previousDocObj = cursor.next();
    int previousVersion = (Integer) previousDocObj.get(VERSION_FUDGE_FIELD_NAME);

    return insertVersionDocument(oid, value, previousVersion, name);
  }

  @Override
  public Set<String> getNames() {
    Set<String> result = new TreeSet<String>();
    DBObject fields = new BasicDBObject();
    fields.put(NAME_FUDGE_FIELD_NAME, 1);
    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    DBCursor cursor = dbCollection.find(new BasicDBObject(), fields);
    while (cursor.hasNext()) {
      DBObject next = cursor.next();
      s_logger.debug("returned obj = {}", next);
      result.add((String) next.get(NAME_FUDGE_FIELD_NAME));
    }
    return result;
  }

  @Override
  public ConfigDocument<T> getByOid(String oid, int version) {
    ArgumentChecker.notNull(oid, "oid");
    ArgumentChecker.isTrue(version > 0, "Version cannot be negative");

    DBObject queryObj = new BasicDBObject();
    queryObj.put(OID_FUDGE_FIELD_NAME, oid);
    queryObj.put(VERSION_FUDGE_FIELD_NAME, version);

    s_logger.debug("query = {}", queryObj);
    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    if (s_logger.isDebugEnabled()) {
      DBCursor find = dbCollection.find(queryObj);
      while (find.hasNext()) {
        DBObject next = find.next();
        s_logger.debug("found doc = {}", next);
      }
    }

    if (_updateLastReadTime) {
      return findAndUpdateLastRead(queryObj, null);
    } else {
      return findWithoutUpdate(queryObj, null);
    }
  }

}
