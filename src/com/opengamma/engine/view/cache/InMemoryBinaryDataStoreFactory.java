/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

/**
 * Creates {@link InMemoryBinaryDataStore} objects.
 */
public class InMemoryBinaryDataStoreFactory implements BinaryDataStoreFactory {

  @Override
  public BinaryDataStore createDataStore(ViewComputationCacheKey cacheKey) {
    return new InMemoryBinaryDataStore();
  }

}
