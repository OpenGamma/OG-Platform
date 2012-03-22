/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.opengamma.bbg.PerSecurityReferenceDataResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.mongo.MongoConnector;

/**
 * A cache of String -> {@link PerSecurityReferenceDataResult} in Mongo.
 */
public class MongoDBReferenceDataCache {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MongoDBReferenceDataCache.class);
  /**
   * Mongo field name.
   */
  private static final String SECURITY_DES_KEY_NAME = "Security Description";
  /**
   * Mongo field name.
   */
  private static final String FIELD_DATA_KEY_NAME = "Field Data";

  /**
   * The Mongo connector.
   */
  private final MongoConnector _mongoConnector;
  /**
   * The Mongo collection.
   */
  private final DBCollection _mongoCollection;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance.
   * 
   * @param mongoConnector  the Mongo connector, not null
   * @param collectionName  the Mongo collection name, not null
   */
  public MongoDBReferenceDataCache(final MongoConnector mongoConnector, final String collectionName) {
    this(mongoConnector, collectionName, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance.
   * 
   * @param mongoConnector  the Mongo connector, not null
   * @param collectionName  the Mongo collection name, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public MongoDBReferenceDataCache(final MongoConnector mongoConnector, final String collectionName, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(mongoConnector, "mongoConnector");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
    _mongoConnector = mongoConnector;
    _mongoCollection = _mongoConnector.getDBCollection(collectionName);
    _mongoCollection.ensureIndex(SECURITY_DES_KEY_NAME);
  }

  //-------------------------------------------------------------------------
  public void saveCachedResult(PerSecurityReferenceDataResult securityResult) {
    FudgeDeserializer deserializer = new FudgeDeserializer(_fudgeContext);
    
    String securityDes = securityResult.getSecurity();
    FudgeMsg fieldData = securityResult.getFieldData();
    
    if (securityDes != null && fieldData != null) {
      s_logger.info("Persisting fields for \"{}\": {}", securityDes, securityResult.getFieldData());
      DBObject mongoDBObject = createMongoDBForResult(deserializer, securityResult);
      s_logger.debug("dbObject={}", mongoDBObject);
      BasicDBObject query = new BasicDBObject();
      query.put(SECURITY_DES_KEY_NAME, securityDes);
      _mongoCollection.update(query, mongoDBObject, true, false);
    }
  }

  public Set<String> getAllCachedSecurities() {
    BasicDBObject query = new BasicDBObject();
    query.put(SECURITY_DES_KEY_NAME, new BasicDBObject("$exists", 1));
    
    BasicDBObject fields = new BasicDBObject();
    fields.put(SECURITY_DES_KEY_NAME, 1);
    DBCursor cursor = _mongoCollection.find(query, fields);
    Set<String> result = new HashSet<String>();
    while (cursor.hasNext()) {
      DBObject dbObject = cursor.next();
      String securityDes = (String) dbObject.get(SECURITY_DES_KEY_NAME);
      result.add(securityDes);
    }
    return result;
  }
  
  public Map<String, PerSecurityReferenceDataResult> loadCachedResults(Set<String> securities) {
    Map<String, PerSecurityReferenceDataResult> result = new TreeMap<String, PerSecurityReferenceDataResult>();
    FudgeSerializer serializer = new FudgeSerializer(_fudgeContext);
    
    BasicDBObject query = new BasicDBObject();
    query.put(SECURITY_DES_KEY_NAME, new BasicDBObject("$in", securities));
    DBCursor cursor = _mongoCollection.find(query);
    while (cursor.hasNext()) {
      DBObject dbObject = cursor.next();
      s_logger.debug("dbObject={}", dbObject);
      
      String securityDes = (String) dbObject.get(SECURITY_DES_KEY_NAME);
      s_logger.debug("Have security data for des {} in MongoDB", securityDes);
      PerSecurityReferenceDataResult perSecResult = parseDBObject(serializer, securityDes, dbObject);
      if (result.put(securityDes, perSecResult) != null) {
        s_logger.warn("{}/{} Querying on des {} gave more than one document", 
            new Object[] {_mongoConnector.getName(), _mongoCollection.getName(), securityDes });
      }
    }
    return result;
  }

  private PerSecurityReferenceDataResult parseDBObject(FudgeSerializer serializer, String securityDes, DBObject fromDB) {
    PerSecurityReferenceDataResult result = new PerSecurityReferenceDataResult(securityDes);
    DBObject fieldData = (DBObject) fromDB.get(FIELD_DATA_KEY_NAME);
    result.setFieldData(serializer.objectToFudgeMsg(fieldData));
    return result;
  }

  protected DBObject createMongoDBForResult(FudgeDeserializer deserializer, PerSecurityReferenceDataResult refDataResult) {
    BasicDBObject result = new BasicDBObject();
    result.put(SECURITY_DES_KEY_NAME, refDataResult.getSecurity());
    DBObject fieldData = deserializer.fudgeMsgToObject(DBObject.class, refDataResult.getFieldData());
    result.put(FIELD_DATA_KEY_NAME, fieldData);
    return result;
  }

}
