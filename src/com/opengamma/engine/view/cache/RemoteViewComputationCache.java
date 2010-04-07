/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.EHCacheUtils;

/**
 * 
 *
 * @author kirk
 */
public class RemoteViewComputationCache implements ViewComputationCache {
  private final RemoteCacheClient _remoteClient;
  private final ViewComputationCacheKey _cacheKey;
  private final Cache _localCache;
  
  public RemoteViewComputationCache(RemoteCacheClient remoteClient, ViewComputationCacheKey cacheKey, int maxLocalCachedElements) {
    ArgumentChecker.checkNotNull(remoteClient, "Remote cache client");
    ArgumentChecker.checkNotNull(cacheKey, "Computation cache key");
    _remoteClient = remoteClient;
    _cacheKey = cacheKey;
    
    CacheManager cacheManager = EHCacheUtils.createCacheManager();
    String cacheName = cacheKey.toString();
    EHCacheUtils.addCache(
        cacheManager,
        cacheKey.toString(),
        maxLocalCachedElements,
        MemoryStoreEvictionPolicy.LFU,
        false, null,
        true, 0,
        0, false, 0, null);
    _localCache = EHCacheUtils.getCacheFromManager(cacheManager, cacheName);
  }

  /**
   * @return the remoteClient
   */
  public RemoteCacheClient getRemoteClient() {
    return _remoteClient;
  }

  /**
   * @return the cacheKey
   */
  public ViewComputationCacheKey getCacheKey() {
    return _cacheKey;
  }

  /**
   * @return the localCache
   */
  public Cache getLocalCache() {
    return _localCache;
  }

  @Override
  public Object getValue(ValueSpecification specification) {
    Element cacheElement = getLocalCache().get(specification);
    if(cacheElement != null) {
      return cacheElement.getValue();
      //return cacheElement.getValue();
    }
    Object value = getRemoteClient().getValue(getCacheKey().getViewName(), getCacheKey().getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), specification);
    getLocalCache().put(new Element(specification, value));
    return value;
  }

  @Override
  public void putValue(ComputedValue value) {
    getRemoteClient().putValue(getCacheKey().getViewName(), getCacheKey().getCalculationConfigurationName(), getCacheKey().getSnapshotTimestamp(), value);
    // Do this AFTER as it MIGHT fail.
    getLocalCache().put(new Element(value.getSpecification(), value.getValue()));
  }

}
