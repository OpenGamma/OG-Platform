/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataSpecificationFudgeBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.mongo.MongoConnector;

/**
 * Stores persistent subscriptions in a Mongo database.  
 */
public class MongoDBPersistentSubscriptionManager extends AbstractPersistentSubscriptionManager {

  /**
   * The collection name.
   */
  private static final String PERSISTENT_SUBSCRIPTION = "PersistentSubscription";

  /**
   * The Mongo connector.
   */
  private final MongoConnector _mongoConnector;
  /**
   * The Mongo collection.
   */
  private final DBCollection _mongoCollection;

  /**
   * Creates an instance.
   * 
   * @param server  the live data server, not null
   * @param mongoConnector  the Mongo connector, not null
   */
  public MongoDBPersistentSubscriptionManager(AbstractLiveDataServer server, MongoConnector mongoConnector) {
    super(server);
    ArgumentChecker.notNull(mongoConnector, "mongoConnector");
    _mongoConnector = mongoConnector;
    _mongoCollection = _mongoConnector.getDBCollection(PERSISTENT_SUBSCRIPTION);
  }

  @Override
  protected void readFromStorage() {
    FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    DBCursor cursor = _mongoCollection.find();
    while (cursor.hasNext()) {
      DBObject mainObject = cursor.next();
      DBObject fieldData = (DBObject) mainObject.get("fieldData");
      MutableFudgeMsg msg = serializer.objectToFudgeMsg(fieldData);
      LiveDataSpecification spec = LiveDataSpecificationFudgeBuilder.fromFudgeMsg(deserializer, msg);
      addPersistentSubscription(new PersistentSubscription(spec));
    }
  }

  @Override
  public void saveToStorage(Set<PersistentSubscription> newState) {
    clean();
    FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    List<DBObject> objects = new ArrayList<DBObject>();
    for (PersistentSubscription sub : newState) {
      FudgeMsg msg = LiveDataSpecificationFudgeBuilder.toFudgeMsg(serializer, sub.getFullyQualifiedSpec());
      DBObject fieldData = deserializer.fudgeMsgToObject(DBObject.class, msg);
      BasicDBObject mainObject = new BasicDBObject();
      mainObject.append("fieldData", fieldData);
      objects.add(mainObject);
    }
    _mongoCollection.insert(objects);
  }

  void clean() {
    _mongoCollection.drop();
  }

}
