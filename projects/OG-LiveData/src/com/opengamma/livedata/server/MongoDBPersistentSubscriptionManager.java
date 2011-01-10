/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
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
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.MongoDBConnectionSettings;

/**
 * Stores persistent subscriptions in MongoDB.  
 */
public class MongoDBPersistentSubscriptionManager extends AbstractPersistentSubscriptionManager {
  
  private static final Logger s_logger = LoggerFactory.getLogger(MongoDBPersistentSubscriptionManager.class);
  
  private static final String MONGO_COLLECTION = "PersistentSubscription";
  
  private final Mongo _mongo;
  private final DB _mongoDB;
  
  public MongoDBPersistentSubscriptionManager(AbstractLiveDataServer server, MongoDBConnectionSettings mongoSettings) {
    super(server);
    
    s_logger.info("Connecting to {}", mongoSettings);
    try {
      _mongo = new Mongo(mongoSettings.getHost(), mongoSettings.getPort());
      _mongoDB = _mongo.getDB(mongoSettings.getDatabase());
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Unable to connect to MongoDB at " + mongoSettings, e);
    }
  }

  @Override
  protected void readFromStorage() {
    FudgeSerializationContext fsc = new FudgeSerializationContext(FudgeContext.GLOBAL_DEFAULT);
    DBCollection dbCollection = _mongoDB.getCollection(MONGO_COLLECTION);
    
    DBCursor cursor = dbCollection.find();
    while (cursor.hasNext()) {
      DBObject mainObject = cursor.next();
      DBObject fieldData = (DBObject) mainObject.get("fieldData");
      MutableFudgeFieldContainer msg = fsc.objectToFudgeMsg(fieldData);
      LiveDataSpecification spec = LiveDataSpecification.fromFudgeMsg(msg);
      addPersistentSubscription(new PersistentSubscription(spec));
    }
    
  }

  @Override
  public void saveToStorage(Set<PersistentSubscription> newState) {
    clean();
    
    FudgeDeserializationContext context = new FudgeDeserializationContext(FudgeContext.GLOBAL_DEFAULT);
    DBCollection dbCollection = _mongoDB.getCollection(MONGO_COLLECTION);
    
    List<DBObject> objects = new ArrayList<DBObject>();
    for (PersistentSubscription sub : newState) {
      FudgeFieldContainer msg = sub.getFullyQualifiedSpec().toFudgeMsg(FudgeContext.GLOBAL_DEFAULT);
      DBObject fieldData = context.fudgeMsgToObject(DBObject.class, msg);
      BasicDBObject mainObject = new BasicDBObject();
      mainObject.append("fieldData", fieldData);
      objects.add(mainObject);
    }
    dbCollection.insert(objects);
  }
  
  void clean() {
    DBCollection dbCollection = _mongoDB.getCollection(MONGO_COLLECTION);
    dbCollection.drop();
  }
  
}
