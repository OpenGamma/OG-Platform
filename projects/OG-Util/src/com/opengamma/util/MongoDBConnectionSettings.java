/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 */
public class MongoDBConnectionSettings {
  
  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 27017;
  private static final String DEFAULT_DATABASE = "OpenGamma";
  
  private String _host = DEFAULT_HOST;
  private int _port = DEFAULT_PORT;
  private String _database = DEFAULT_DATABASE;
  private String _collectionName;
  
  public MongoDBConnectionSettings() {
  }
  
  public String getHost() {
    return _host;
  }
  
  public void setHost(final String host) {
    ArgumentChecker.notNull(host, "host");
    _host = host;
  }
  
  public int getPort() {
    return _port;
  }
  
  public void setPort(final int port) {
    _port = port;
  }
  
  public String getDatabase() {
    return _database;
  }
  
  public void setDatabase(final String database) {
    ArgumentChecker.notNull(database, "database");
    _database = database;
  }
  
  public String getCollectionName() {
    return _collectionName;
  }
  
  public void setCollectionName(final String collectionName) {
    _collectionName = collectionName;
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getHost()).append(':').append(getPort()).append('/').append(getDatabase());
    if (getCollectionName() != null) {
      sb.append('[').append(getCollectionName()).append(']');
    }
    return sb.toString();
  }
  
}
