/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.test;

import com.opengamma.bbg.MongoDBCachingReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.util.mongo.MongoConnector;
import com.opengamma.util.test.MongoTestUtils;

/**
 * Encapsulates settings for writing Bloomberg reference data unit testing which
 * can run through the shared MongoDB cache.
 */
public class MongoCachedReferenceData {

  public static MongoDBCachingReferenceDataProvider makeMongoProvider(ReferenceDataProvider underlying, Class<?> testClass) {
    return makeMongoProvider(underlying, testClass, false);
  }

  public static MongoDBCachingReferenceDataProvider makeMongoProvider(ReferenceDataProvider underlying, Class<?> testClass, boolean clearData) {
    MongoDBCachingReferenceDataProvider mongoProvider = new MongoDBCachingReferenceDataProvider(
        underlying, getMongoConnector(testClass, clearData));
    return mongoProvider;
  }

  private static MongoConnector getMongoConnector(Class<?> testClass, boolean clearData) {
    return MongoTestUtils.makeTestConnector(testClass.getSimpleName(), clearData);
  }

}
