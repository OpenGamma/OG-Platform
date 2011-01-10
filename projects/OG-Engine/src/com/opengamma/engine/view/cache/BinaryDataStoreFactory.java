/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

/**
 * A source of {@link BinaryDataStore} objects for a given cache key.
 */
public interface BinaryDataStoreFactory {

  BinaryDataStore createDataStore(ViewComputationCacheKey cacheKey);

}
