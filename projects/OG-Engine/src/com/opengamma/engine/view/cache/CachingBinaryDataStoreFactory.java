/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import net.sf.ehcache.CacheManager;

/**
 * A data store factory that wraps an underlying factory's generation with a local caching layer.
 */
public class CachingBinaryDataStoreFactory implements BinaryDataStoreFactory {
  private static final int DEFAULT_MAX_LOCAL_CACHED_ELEMENTS = 100000;
  private final BinaryDataStoreFactory _underlying;
  private final int _maxLocalCachedElements;
  private final CacheManager _cacheManager;

  public CachingBinaryDataStoreFactory(final BinaryDataStoreFactory underlying, final CacheManager cacheManager) {
    this(underlying, cacheManager, DEFAULT_MAX_LOCAL_CACHED_ELEMENTS);
  }

  public CachingBinaryDataStoreFactory(final BinaryDataStoreFactory underlying, final CacheManager cacheManager, int maxLocalCachedElements) {
    _underlying = underlying;
    _maxLocalCachedElements = maxLocalCachedElements;
    _cacheManager = cacheManager;
  }

  /**
   * @return the maxLocalCachedElements
   */
  public int getMaxLocalCachedElements() {
    return _maxLocalCachedElements;
  }

  protected BinaryDataStoreFactory getUnderlying() {
    return _underlying;
  }

  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  @Override
  public BinaryDataStore createDataStore(ViewComputationCacheKey cacheKey) {
    return new CachingBinaryDataStore(getUnderlying().createDataStore(cacheKey), getCacheManager(), cacheKey, getMaxLocalCachedElements());
  }

}
