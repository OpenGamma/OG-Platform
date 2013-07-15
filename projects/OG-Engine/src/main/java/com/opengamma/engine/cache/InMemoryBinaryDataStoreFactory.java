/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

/**
 * Creates {@link InMemoryBinaryDataStore} objects.
 */
public class InMemoryBinaryDataStoreFactory implements BinaryDataStoreFactory {

  @Override
  public BinaryDataStore createDataStore(ViewComputationCacheKey cacheKey) {
    return new InMemoryBinaryDataStore();
  }

}
