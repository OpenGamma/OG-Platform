/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.time.Instant;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.ObjectId;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.config.ConfigurationDocument;
import com.opengamma.config.ConfigurationDocumentRepo;
import com.opengamma.config.DefaultConfigurationDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.MongoDBConnectionSettings;

/**
 * 
 *@param <T> Configuration Document EntityType
 */
public class MongoDBConfigurationRepo<T> implements ConfigurationDocumentRepo<T> {

  private static final Logger s_logger = LoggerFactory.getLogger(MongoDBConfigurationRepo.class);

  private final FudgeContext _fudgeContext;

  private Class<T> _entityClazz;
  private final String _mongoHost;
  private final int _mongoPort;

  private final Mongo _mongo;
  private final DB _mongoDB;
  private final String _collectionName;

  public MongoDBConfigurationRepo(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings,
      final FudgeContext fudgeContext, final FudgeBuilder<T> messageBuilder) {
    ArgumentChecker.notNull(documentClazz, "document class");
    ArgumentChecker.notNull(mongoSettings, "MongoDB settings");
    ArgumentChecker.notNull(fudgeContext, "FudgeContext");

    _entityClazz = documentClazz;
    _fudgeContext = fudgeContext;
    s_logger.info("Connecting to {}", mongoSettings);
    _mongoHost = mongoSettings.getHost();
    _mongoPort = mongoSettings.getPort();
    if (mongoSettings.getCollectionName() != null) {
      _collectionName = mongoSettings.getCollectionName();
    } else {
      _collectionName = _entityClazz.getSimpleName();
    }
    try {
      _mongo = new Mongo(_mongoHost, _mongoPort);
      _mongoDB = _mongo.getDB(mongoSettings.getDatabase());
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Unable to connect to MongoDB at " + mongoSettings, e);
    }

    if (messageBuilder != null) {
      fudgeContext.getObjectDictionary().addBuilder(_entityClazz, messageBuilder);
    }

  }

  public MongoDBConfigurationRepo(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings,
      final FudgeBuilder<T> messageBuilder) {
    this(documentClazz, mongoSettings, new FudgeContext(), messageBuilder);
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

  @Override
  public ConfigurationDocument<T> getByName(String name) {
    ArgumentChecker.notNull(name, "name");
    //get latest version by name
    DBObject query = new BasicDBObject();
    query.put(NAME_FUDGE_FIELD_NAME, name);
    
    s_logger.debug("query = {}", query);
    
    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    
    if (s_logger.isDebugEnabled()) {
      DBCursor find = dbCollection.find(query);
      while (find.hasNext()) {
        DBObject next = find.next();
        s_logger.debug("found doc = {}", next);
      }
    }
    
    DBCursor cursor = dbCollection.find(query).sort(new BasicDBObject(CREATION_INSTANT_FUDGE_FIELD_NAME, -1)).limit(1);

    //should return the latest version
    if (cursor.hasNext()) {
      DBObject findByName = cursor.next();
      s_logger.debug("findByName = {}", findByName);
      String fromDbName = (String) findByName.get(NAME_FUDGE_FIELD_NAME);
      if (fromDbName.equals(name)) {
        DBObject valueData = (DBObject) findByName.get(VALUE_FUDGE_FIELD_NAME);
        FudgeSerializationContext fsc = new FudgeSerializationContext(getFudgeContext());
        MutableFudgeFieldContainer dbObjecToFudge = fsc.objectToFudgeMsg(valueData);
        FudgeDeserializationContext fdc = new FudgeDeserializationContext(getFudgeContext());
        T value = fdc.fudgeMsgToObject(_entityClazz, dbObjecToFudge);
        String objectId = (String) findByName.get("_id");
        String oid = (String) findByName.get(OID_FUDGE_FIELD_NAME);
        int version = (Integer) findByName.get(VERSION_FUDGE_FIELD_NAME);
        Date creationTime = (Date) findByName.get(CREATION_INSTANT_FUDGE_FIELD_NAME);
        return new DefaultConfigurationDocument<T>(objectId, oid, version, name, Instant.ofEpochMillis(creationTime
            .getTime()), value);
      }
    }
    return null;
  }

  @Override
  public ConfigurationDocument<T> getByName(String currentName, Instant effectiveInstant) {
    ArgumentChecker.notNull(currentName, "currentName");
    ArgumentChecker.notNull(effectiveInstant, "effectiveInstant");
    
    DBObject query = new BasicDBObject();
    DBObject filter = new BasicDBObject("$lte", new Date(effectiveInstant.toEpochMillisLong()));
    query.put(CREATION_INSTANT_FUDGE_FIELD_NAME, filter);
    query.put(NAME_FUDGE_FIELD_NAME, currentName);
    
    s_logger.debug("query = {}", query);
    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    
    if (s_logger.isDebugEnabled()) {
      DBCursor find = dbCollection.find(query);
      while (find.hasNext()) {
        DBObject next = find.next();
        s_logger.debug("found doc = {}", next);
      }
    }
    
    DBCursor cursor = dbCollection.find(query).sort(new BasicDBObject(CREATION_INSTANT_FUDGE_FIELD_NAME, -1))
        .limit(1);

    //should return the latest version if there is one
    if (cursor.hasNext()) {
      DBObject findByName = cursor.next();
      s_logger.debug("return doc = {}", findByName);
      String fromDbName = (String) findByName.get(NAME_FUDGE_FIELD_NAME);
      if (fromDbName.equals(currentName)) {
        DBObject valueData = (DBObject) findByName.get(VALUE_FUDGE_FIELD_NAME);
        FudgeSerializationContext fsc = new FudgeSerializationContext(getFudgeContext());
        MutableFudgeFieldContainer dbObjecToFudge = fsc.objectToFudgeMsg(valueData);
        FudgeDeserializationContext fdc = new FudgeDeserializationContext(getFudgeContext());
        T value = fdc.fudgeMsgToObject(_entityClazz, dbObjecToFudge);
        String objectId = (String) findByName.get("_id");
        String oid = (String) findByName.get(OID_FUDGE_FIELD_NAME);
        int version = (Integer) findByName.get(VERSION_FUDGE_FIELD_NAME);
        Date creationTime = (Date) findByName.get(CREATION_INSTANT_FUDGE_FIELD_NAME);
        return new DefaultConfigurationDocument<T>(objectId, oid, version, currentName, Instant
            .ofEpochMillis(creationTime.getTime()), value);
      }
    }
    return null;
  }

  @Override
  public List<ConfigurationDocument<T>> getSequence(String oid, Instant startDate, Instant endDate) {
    ArgumentChecker.notNull(oid, "oid");
    ArgumentChecker.notNull(startDate, "startDate");
    Date end = null;
    if (endDate == null) {
      end = new Date();
    } else {
      end = new Date(endDate.toEpochMillisLong());
    }
    
    List<ConfigurationDocument<T>> result = new ArrayList<ConfigurationDocument<T>>();

    BasicDBObject query = new BasicDBObject(OID_FUDGE_FIELD_NAME, oid);
    query.put(CREATION_INSTANT_FUDGE_FIELD_NAME, new BasicDBObject("$lte", end).append("$gte", new Date(startDate.toEpochMillisLong())));
//    query.put(CREATION_INSTANT_FUDGE_FIELD_NAME, new BasicDBObject("$gte", new Date(startDate.toEpochMillisLong())));
    
    s_logger.debug("query = {}", query);
    
    DBCollection dbCollection = getMongoDB().getCollection(getCollectionName());
    DBCursor cursor = dbCollection.find(query).sort(new BasicDBObject(CREATION_INSTANT_FUDGE_FIELD_NAME, -1));

    while (cursor.hasNext()) {
      DBObject next = cursor.next();
      s_logger.debug("returned doc = {}", next);
      String rtOid = (String) next.get(OID_FUDGE_FIELD_NAME);
      if (rtOid.equals(oid)) {
        DBObject valueData = (DBObject) next.get(VALUE_FUDGE_FIELD_NAME);
        FudgeSerializationContext fsc = new FudgeSerializationContext(getFudgeContext());
        MutableFudgeFieldContainer dbObjecToFudge = fsc.objectToFudgeMsg(valueData);
        FudgeDeserializationContext fdc = new FudgeDeserializationContext(getFudgeContext());
        T value = fdc.fudgeMsgToObject(_entityClazz, dbObjecToFudge);
        String objectId = (String) next.get("_id");
        int version = (Integer) next.get(VERSION_FUDGE_FIELD_NAME);
        Date creationTime = (Date) next.get(CREATION_INSTANT_FUDGE_FIELD_NAME);
        String name = (String) next.get(NAME_FUDGE_FIELD_NAME);
        result.add(new DefaultConfigurationDocument<T>(objectId, oid, version, name, Instant.ofEpochMillis(creationTime
            .getTime()), value));
      }
    }

    return result;
  }

  @Override
  public ConfigurationDocument<T> insertNewItem(String name, T value) {

    String objectId = ObjectId.get().toString();
    Date now = new Date();
    int version = 1;
    MutableFudgeFieldContainer msg = _fudgeContext.newMessage();
    msg.add("_id", objectId);
    msg.add(OID_FUDGE_FIELD_NAME, objectId);
    msg.add(VERSION_FUDGE_FIELD_NAME, version);
    msg.add(NAME_FUDGE_FIELD_NAME, name);
    msg.add(CREATION_INSTANT_FUDGE_FIELD_NAME, now);
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

    return new DefaultConfigurationDocument<T>(objectId, objectId, version, name, Instant.ofEpochMillis(now.getTime()),
        value);
  }

  @Override
  public ConfigurationDocument<T> insertNewVersion(String oid, T value) {

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

  /**
   * @param oid
   * @param value
   * @param previousVersion
   * @param name
   * @return
   */
  private ConfigurationDocument<T> insertVersionDocument(String oid, T value, int previousVersion, String name) {
    DBCollection dbCollection;
    String objectId = ObjectId.get().toString();
    Date now = new Date();
    MutableFudgeFieldContainer msg = _fudgeContext.newMessage();
    msg.add("_id", objectId);
    msg.add(OID_FUDGE_FIELD_NAME, oid);
    msg.add(VERSION_FUDGE_FIELD_NAME, ++previousVersion);
    msg.add(NAME_FUDGE_FIELD_NAME, name);
    msg.add(CREATION_INSTANT_FUDGE_FIELD_NAME, now);
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

    return new DefaultConfigurationDocument<T>(objectId, oid, previousVersion, name, Instant.ofEpochMillis(now
        .getTime()), value);
  }

  @Override
  public ConfigurationDocument<T> insertNewVersion(String oid, String name, T value) {
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

}
