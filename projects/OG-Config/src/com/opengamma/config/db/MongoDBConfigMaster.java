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

import java.util.Date;

import javax.time.Instant;
import javax.time.TimeSource;

import org.bson.types.ObjectId;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
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
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigMaster;
import com.opengamma.config.ConfigSearchHistoricRequest;
import com.opengamma.config.ConfigSearchHistoricResult;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.config.DefaultConfigDocument;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * General purpose configuration data loader backed by MongoDB
 * MongoDBConnectionSettings specifies mongo host, port, database and collectionName to use
 * If collectionName is null, the document classname will be used
 * 
 * @param <T> Configuration Document EntityType
 */
public class MongoDBConfigMaster<T> implements ConfigMaster<T> {
  private static final Logger s_logger = LoggerFactory.getLogger(MongoDBConfigMaster.class);

  private static final String[] INDICES = {OID_FUDGE_FIELD_NAME, NAME_FUDGE_FIELD_NAME, CREATION_INSTANT_FUDGE_FIELD_NAME, LAST_READ_INSTANT_FUDGE_FIELD_NAME};
  
  private static final FudgeContext DEFAULT_FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();
  
  /**
   * The scheme used by the master by default.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "MongoConfigMaster";
  private static final String ACTIVE_FIELD = "active";
  private static final int ACTIVE_VALUE = 1;
  private static final String LAST_MODIFIED_INSTANT = "lastModifiedInstant";
  
  private String _identifierScheme = IDENTIFIER_SCHEME_DEFAULT;
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
  
  public MongoDBConfigMaster(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings,
      final FudgeContext fudgeContext, boolean updateLastRead) {
    ArgumentChecker.notNull(documentClazz, "document class");
    ArgumentChecker.notNull(mongoSettings, "MongoDB settings");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");

    _fudgeContext = fudgeContext;
    _entityClazz = documentClazz;
    
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

    ensureIndices();
    _updateLastReadTime = updateLastRead;
    String status = _updateLastReadTime ? "updateLastReadTime" : "readWithoutUpdate";
    s_logger.info("creating MongoDBConfigurationRepo for {}", status);
  }
  
  public MongoDBConfigMaster(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings, boolean updateLastRead) {
    this(documentClazz, mongoSettings, DEFAULT_FUDGE_CONTEXT, updateLastRead);
  }
  
  public MongoDBConfigMaster(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings) {
    this(documentClazz, mongoSettings, true);
  }
  
  private void ensureIndices() {
    //create necessary indices
    DBCollection dbCollection = _mongoDB.getCollection(_collectionName);
    for (String field : INDICES) {
      s_logger.info("ensuring index for {} {}:{}", new Object[] {field, getMongoDB().getName(), getCollectionName()});
      //create ascending and descending index
      dbCollection.ensureIndex(new BasicDBObject(field, 1), "ix_" + getCollectionName() + "_" + field + "_asc");
      dbCollection.ensureIndex(new BasicDBObject(field, -1), "ix_" + getCollectionName() + "_" + field + "_desc");
    }
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
    ArgumentChecker.notNull(timeSource, "timeSource");
    _timeSource = timeSource;
  }

  @Override
  public ConfigDocument<T> add(ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document name");
    ArgumentChecker.notNull(document.getValue(), "document value");
    
    String objectId = ObjectId.get().toString();
    Date creationDate = new Date(_timeSource.instant().toEpochMillisLong());
    Instant creationInstant = document.getCreationInstant();
    if (creationInstant != null) {
      creationDate = new Date(creationInstant.toEpochMillisLong());
    }
    int version = 1;
    MutableFudgeFieldContainer msg = _fudgeContext.newMessage();
    String name = document.getName();
    T value = document.getValue();
    msg.add("_id", objectId);
    msg.add(OID_FUDGE_FIELD_NAME, objectId);
    msg.add(VERSION_FUDGE_FIELD_NAME, version);
    msg.add(NAME_FUDGE_FIELD_NAME, name);
    msg.add(CREATION_INSTANT_FUDGE_FIELD_NAME, creationDate);
    msg.add(LAST_READ_INSTANT_FUDGE_FIELD_NAME, creationDate);
    
    msg.add(VALUE_FUDGE_FIELD_NAME, _fudgeContext.toFudgeMsg(value).getMessage());

    s_logger.debug("msg = {}", msg);
    
    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    FudgeDeserializationContext fdc = new FudgeDeserializationContext(getFudgeContext());

    DBObject doc = fdc.fudgeMsgToObject(DBObject.class, msg);
    doc.put(ACTIVE_FIELD, ACTIVE_VALUE);
    
    s_logger.debug("inserting new doc {}", doc);
    dbCollection.insert(doc);
    DBObject lastErr = getMongoDB().getLastError();
    if (lastErr.get("err") != null) {
      throw new OpenGammaRuntimeException("Error: " + lastErr.toString());
    }
    if (creationInstant == null) {
      creationInstant = Instant.ofEpochMillis(creationDate.getTime());
    }
    DefaultConfigDocument<T> configDocument = new DefaultConfigDocument<T>(objectId, objectId, version, name, creationInstant, creationInstant, value);
    configDocument.setUniqueIdentifier(UniqueIdentifier.of(_identifierScheme, objectId, String.valueOf(version)));
    return configDocument;
  }

  @Override
  public ConfigDocument<T> get(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    ArgumentChecker.isTrue(uid.getScheme().equals(_identifierScheme), "Uid not for MongoDBConfigMaster");
    ArgumentChecker.isTrue(uid.getValue() != null, "Uid value cannot be null");
    ArgumentChecker.isTrue(uid.getVersion() != null, "Uid version cannot be null");

    DBObject queryObj = new BasicDBObject(ACTIVE_FIELD, ACTIVE_VALUE);
    queryObj.put(OID_FUDGE_FIELD_NAME, uid.getValue());
    queryObj.put(VERSION_FUDGE_FIELD_NAME, Integer.parseInt(uid.getVersion()));

    s_logger.debug("query = {}", queryObj);
    ConfigDocument<T> result = null;
    if (_updateLastReadTime) {
      result = findAndUpdateLastRead(queryObj, null);
    } else {
      result = findWithoutUpdate(queryObj, null);
    }
    
    if (result == null) {
      throw new DataNotFoundException("No Config Doc with Uid " + uid);
    }
    
    return result;
  }

  @Override
  public void remove(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    ArgumentChecker.isTrue(uid.getScheme().equals(_identifierScheme), "Uid not for MongoDBConfigMaster");
    ArgumentChecker.isTrue(uid.getValue() != null, "Uid value cannot be null");
    ArgumentChecker.isTrue(uid.getVersion() != null, "Uid version cannot be null");

    DBObject queryObj = new BasicDBObject();
    queryObj.put(OID_FUDGE_FIELD_NAME, uid.getValue());
    queryObj.put(VERSION_FUDGE_FIELD_NAME, Integer.parseInt(uid.getVersion()));
    Date now = new Date(_timeSource.instant().toEpochMillisLong());
    DBObject updateFields = new BasicDBObject(ACTIVE_FIELD, 0);
    updateFields.put(LAST_MODIFIED_INSTANT, now);
    DBObject updateObj = new BasicDBObject("$set", updateFields);
    
    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    dbCollection.update(queryObj, updateObj);
    
    DBObject lastError = getMongoDB().getLastError();
    
    s_logger.debug("after remove lastErro = {}", lastError.toString());
    String errorMessage = (String) lastError.get("err");
    if (errorMessage != null) {
      s_logger.warn("mongo err = {} from removing uid = {}", errorMessage, uid);
      throw new DataNotFoundException("No Config Doc with Uid " + uid);
    }
    
  }

  @Override
  public ConfigSearchResult<T> search(ConfigSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    String name = request.getName();
    Instant effectiveTime = request.getEffectiveTime();
    DBObject queryObj = new BasicDBObject(ACTIVE_FIELD, ACTIVE_VALUE);
    queryObj.put(NAME_FUDGE_FIELD_NAME, name);
    if (effectiveTime != null) {
      DBObject filter = new BasicDBObject("$lte", new Date(effectiveTime.toEpochMillisLong()));
      queryObj.put(CREATION_INSTANT_FUDGE_FIELD_NAME, filter);
    }
    
    s_logger.debug("query = {}", queryObj);

    BasicDBObject sortObj = new BasicDBObject(CREATION_INSTANT_FUDGE_FIELD_NAME, -1);
    
    ConfigDocument<T> lookupByName = null;
    
    if (_updateLastReadTime) {
      lookupByName = findAndUpdateLastRead(queryObj, sortObj);
    } else {
      lookupByName = findWithoutUpdate(queryObj, sortObj);
    }
    
    ConfigSearchResult<T> configSearchResult = new ConfigSearchResult<T>();
    if (lookupByName != null) {
      configSearchResult.getDocuments().add(lookupByName);
    }
    
    return configSearchResult;
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
  public ConfigSearchHistoricResult<T> searchHistoric(ConfigSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.isTrue(request.getOid() != null, "ConfigSearchHistoricRequest must have an object identifier ");
    
    Date end = null;
    Instant requestEndTime = request.getEndTime();
    if (requestEndTime == null) {
      end = new Date(_timeSource.instant().toEpochMillisLong());
    } else {
      end = new Date(requestEndTime.toEpochMillisLong());
    }

    BasicDBObject query = new BasicDBObject(OID_FUDGE_FIELD_NAME, request.getOid());
    BasicDBObject timeFilter = new BasicDBObject("$lte", end);
    Instant requestStartTime = request.getStartTime();
    if (requestStartTime != null) {
      timeFilter.append("$gte", new Date(requestStartTime.toEpochMillisLong()));
    }
    query.put(CREATION_INSTANT_FUDGE_FIELD_NAME, timeFilter);

    s_logger.debug("query = {}", query);

    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    DBCursor cursor = dbCollection.find(query).sort(new BasicDBObject(CREATION_INSTANT_FUDGE_FIELD_NAME, -1));

    ConfigSearchHistoricResult<T> searchResult = new ConfigSearchHistoricResult<T>();
    while (cursor.hasNext()) {
      DBObject next = cursor.next();
      ConfigDocument<T> conficDoc = dBObjectToConficDoc(next);
      searchResult.getDocuments().add(conficDoc);
    }

    return searchResult;
  }

  @Override
  public ConfigDocument<T> update(ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    
    int previousVersion = document.getVersion();
    s_logger.debug("previousVersion = {}", previousVersion);
    String name = document.getName();
    String oid = document.getOid();
    T value = document.getValue();

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
    
    s_logger.debug("msg = {}", msg);

    dbCollection = getMongoDB().getCollection(getCollectionName());
    FudgeDeserializationContext fdc = new FudgeDeserializationContext(getFudgeContext());

    DBObject doc = fdc.fudgeMsgToObject(DBObject.class, msg);
    doc.put(ACTIVE_FIELD, ACTIVE_VALUE);
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
  
//@Override
//public Set<String> getNames() {
//  Set<String> result = new TreeSet<String>();
//  DBObject fields = new BasicDBObject();
//  fields.put(NAME_FUDGE_FIELD_NAME, 1);
//  DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
//  DBCursor cursor = dbCollection.find(new BasicDBObject(), fields);
//  while (cursor.hasNext()) {
//    DBObject next = cursor.next();
//    s_logger.debug("returned obj = {}", next);
//    result.add((String) next.get(NAME_FUDGE_FIELD_NAME));
//  }
//  return result;
//}

  public void close() {
    getMongo().close();
  }

}
