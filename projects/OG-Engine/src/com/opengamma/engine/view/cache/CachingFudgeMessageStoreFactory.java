/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import net.sf.ehcache.CacheManager;

/**
 * A data store factory that wraps an underlying factory's generation with a local caching layer.
 */
public class CachingFudgeMessageStoreFactory implements FudgeMessageStoreFactory {
  private static final int DEFAULT_MAX_LOCAL_CACHED_ELEMENTS = 100000;
  private final FudgeMessageStoreFactory _underlying;
  private final int _maxLocalCachedElements;
  private final CacheManager _cacheManager;

  public CachingFudgeMessageStoreFactory(final FudgeMessageStoreFactory underlying, final CacheManager cacheManager) {
    this(underlying, cacheManager, DEFAULT_MAX_LOCAL_CACHED_ELEMENTS);
  }

  public CachingFudgeMessageStoreFactory(final FudgeMessageStoreFactory underlying, final CacheManager cacheManager,
      int maxLocalCachedElements) {
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

  protected FudgeMessageStoreFactory getUnderlying() {
    return _underlying;
  }

  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  @Override
  public FudgeMessageStore createMessageStore(ViewComputationCacheKey cacheKey) {
    return new CachingFudgeMessageStore(getUnderlying().createMessageStore(cacheKey), getCacheManager(), cacheKey,
        getMaxLocalCachedElements());
  }

}
