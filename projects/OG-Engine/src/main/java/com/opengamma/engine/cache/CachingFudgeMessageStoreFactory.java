/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import net.sf.ehcache.CacheManager;

/**
 * A data store factory that wraps an underlying factory's generation with a local caching layer.
 */
public class CachingFudgeMessageStoreFactory implements FudgeMessageStoreFactory {

  private final FudgeMessageStoreFactory _underlying;
  private final CacheManager _cacheManager;

  public CachingFudgeMessageStoreFactory(final FudgeMessageStoreFactory underlying, final CacheManager cacheManager) {
    _underlying = underlying;
    _cacheManager = cacheManager;
  }

  protected FudgeMessageStoreFactory getUnderlying() {
    return _underlying;
  }

  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  @Override
  public FudgeMessageStore createMessageStore(ViewComputationCacheKey cacheKey) {
    return new CachingFudgeMessageStore(getUnderlying().createMessageStore(cacheKey), getCacheManager(), cacheKey);
  }

}
