/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.mongo;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.Connector;

/**
 * Connector used to access Mongo databases.
 * <p>
 * This class is usually configured using the associated factory bean.
 */
public class MongoConnector implements Connector {

  /**
   * The configuration name.
   */
  private final String _name;
  /**
   * The Mongo instance.
   */
  private final MongoClient _mongo;
  /**
   * The database.
   */
  private final DB _database;
  /**
   * A suffix for the collection name.
   */
  private final String _collectionSuffix;

  /**
   * Creates an instance.
   * 
   * @param name  the configuration name, not null
   * @param mongo  the main Mongo instance, not null
   * @param database  the Mongo database, not null
   * @param collectionSuffix  the collection suffix, not null
   */
  public MongoConnector(String name, MongoClient mongo, DB database, String collectionSuffix) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(mongo, "mongo");
    ArgumentChecker.notNull(database, "database");
    ArgumentChecker.notNull(collectionSuffix, "collectionSuffix");
    _name = name;
    _mongo = mongo;
    _database = database;
    _collectionSuffix = collectionSuffix;
  }

  //-------------------------------------------------------------------------
  @Override
  public final String getName() {
    return _name;
  }

  @Override
  public final Class<? extends Connector> getType() {
    return MongoConnector.class;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Mongo instance.
   * <p>
   * Access to the Mongo instance is needed for advanced Mongo use cases.
   * 
   * @return the main Mongo instance, not null
   */
  public MongoClient getMongo() {
    return _mongo;
  }

  /**
   * Gets the Mongo database.
   * <p>
   * Access to the database is needed for advanced Mongo use cases.
   * A Mongo instance can hold multiple databases each with multiple collections.
   * 
   * @return the database, not null
   */
  public DB getDB() {
    return _database;
  }

  /**
   * Gets the suffix to add to the collection.
   * 
   * @return the collection suffix, not null
   */
  public String getCollectionSuffix() {
    return _collectionSuffix;
  }

  /**
   * Gets the Mongo database collection, which is the main method normally used by applications.
   * <p>
   * Most applications should use this method to obtain a properly configured collection.
   * A Mongo instance can hold multiple databases each with multiple collections.
   * 
   * @param collectionName  the collection name, not null
   * @return the database, not null
   * @throws IllegalStateException if no collection name is present
   */
  public DBCollection getDBCollection(String collectionName) {
    return _database.getCollection(collectionName + getCollectionSuffix());
  }

  //-------------------------------------------------------------------------
  @Override
  public void close() {
    // object can still be used after close
    getMongo().close();
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a description of this object suitable for debugging.
   * 
   * @return the description, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + _name + "]";
  }

}
