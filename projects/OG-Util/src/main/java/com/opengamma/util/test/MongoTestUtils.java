/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.Properties;

import com.opengamma.util.mongo.MongoConnector;
import com.opengamma.util.mongo.MongoConnectorFactoryBean;

/**
 * Utility methods for working with Mongo in testing contexts.
 */
public final class MongoTestUtils {

  /**
   * Restricted constructor.
   */
  private MongoTestUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Makes a connector suitable for testing.
   * 
   * @param testName  the name of the testing database, used as a suffix, not null
   * @param makeUnique  whether to make the connector unique by date-time
   * @return the Mongo connector, not null
   */
  public static MongoConnector makeTestConnector(String testName, boolean makeUnique) {
    MongoConnectorFactoryBean factory = new MongoConnectorFactoryBean();
    factory.setName("MongoTestUtils");
    Properties properties = TestProperties.getTestProperties();
    factory.setHost(properties.getProperty("mongoServer.host"));
    factory.setPort(Integer.parseInt(properties.getProperty("mongoServer.port")));
    factory.setDatabaseName(System.getProperty("user.name").replace('.', '_') + "_unit");
    String collectionSuffix = "_" + testName;
    if (makeUnique) {
      collectionSuffix += "_" + System.currentTimeMillis();
    }
    factory.setCollectionSuffix(collectionSuffix);
    return factory.createObject();
  }

}
