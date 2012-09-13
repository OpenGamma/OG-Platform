/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.test;

import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.cache.MongoDBValueCachingReferenceDataProvider;
import com.opengamma.util.mongo.MongoConnector;
import com.opengamma.util.test.MongoTestUtils;

/**
 * Encapsulates settings for writing Bloomberg reference data unit testing which
 * can run through the shared MongoDB cache.
 */
public class MongoCachedReferenceData {

  /**
   * Wraps a Bloomberg reference data provider with Mongo for caching.
   * 
   * @param underlying  the underlying provider
   * @param testClass  the test class, not null
   * @return the wrapped provider, not null
   */
  public static MongoDBValueCachingReferenceDataProvider makeMongoProvider(ReferenceDataProvider underlying, Class<?> testClass) {
    return makeMongoProvider(underlying, testClass, false);
  }

  /**
   * Wraps a Bloomberg reference data provider with Mongo for caching.
   * 
   * @param underlying  the underlying provider
   * @param testClass  the test class, not null
   * @param makeUnique  whether to make the database totally unique
   * @return the wrapped provider, not null
   */
  public static MongoDBValueCachingReferenceDataProvider makeMongoProvider(ReferenceDataProvider underlying, Class<?> testClass, boolean makeUnique) {
    MongoConnector mongoConnector = getMongoConnector(testClass, makeUnique);
    MongoDBValueCachingReferenceDataProvider mongoProvider = new MongoDBValueCachingReferenceDataProvider(underlying, mongoConnector);
    return mongoProvider;
  }

  /**
   * Creates a Mongo connector specific to the specified class.
   * 
   * @param testClass  the test class, not null
   * @param makeUnique  whether to make the database totally unique
   * @return the connector, not null
   */
  private static MongoConnector getMongoConnector(Class<?> testClass, boolean makeUnique) {
    return MongoTestUtils.makeTestConnector(testClass.getSimpleName(), makeUnique);
  }

}
