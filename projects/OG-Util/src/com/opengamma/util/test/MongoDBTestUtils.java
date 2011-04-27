/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import com.opengamma.util.MongoDBConnectionSettings;

import java.util.Properties;

/**
 * Utility methods for working with MongoDB in testing contexts.
 */
public final class MongoDBTestUtils {

  private MongoDBTestUtils() {
  }

  public static MongoDBConnectionSettings makeTestSettings(String testName, boolean makeUnique) {
    MongoDBConnectionSettings settings = new MongoDBConnectionSettings();
    Properties properties = TestProperties.getTestProperties();
    settings.setHost(properties.getProperty("mongodb.host"));
    settings.setPort(Integer.parseInt(properties.getProperty("mongodb.port")));
    String dbName = System.getProperty("user.name").replace('.','_') + "-unit";
    String collectionName = testName;
    if (makeUnique) {
      collectionName += "-" + System.currentTimeMillis();
    }
    settings.setDatabase(dbName);
    settings.setCollectionName(collectionName);
    return settings;
  }

}
