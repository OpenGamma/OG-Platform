/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class RemoteViewComputationCacheSource implements ViewComputationCacheSource {
  private static final Logger s_logger = LoggerFactory.getLogger(RemoteViewComputationCacheSource.class);
  private static final int DEFAULT_MAX_LOCAL_CACHED_ELEMENTS = 100000;
  private final RemoteCacheClient _remoteClient;
  private final int _maxLocalCachedElements;
  private final Lock _cacheCreationLock = new ReentrantLock();
  private final ConcurrentMap<ViewComputationCacheKey, RemoteViewComputationCache> _cachesByKey = new ConcurrentHashMap<ViewComputationCacheKey, RemoteViewComputationCache>();

  public RemoteViewComputationCacheSource(RemoteCacheClient remoteClient) {
    this(remoteClient, DEFAULT_MAX_LOCAL_CACHED_ELEMENTS);
  }

  public RemoteViewComputationCacheSource(RemoteCacheClient remoteClient, int maxLocalCachedElements) {
    ArgumentChecker.notNull(remoteClient, "Remote computation cache client");
    _remoteClient = remoteClient;
    _maxLocalCachedElements = maxLocalCachedElements;
  }

  /**
   * @return the remoteClient
   */
  public RemoteCacheClient getRemoteClient() {
    return _remoteClient;
  }

  /**
   * @return the maxLocalCachedElements
   */
  public int getMaxLocalCachedElements() {
    return _maxLocalCachedElements;
  }

  @Override
  public ViewComputationCache cloneCache(String viewName, String calculationConfigurationName, long timestamp) {
    throw new UnsupportedOperationException("Cloning not yet supported.");
  }

  @Override
  public ViewComputationCache getCache(String viewName, String calculationConfigurationName, long timestamp) {
    ViewComputationCacheKey cacheKey = new ViewComputationCacheKey(viewName, calculationConfigurationName, timestamp);
    RemoteViewComputationCache remoteCache = _cachesByKey.get(cacheKey);
    if (remoteCache == null) {
      _cacheCreationLock.lock();
      try {
        remoteCache = _cachesByKey.get(cacheKey);
        if (remoteCache == null) {
          remoteCache = new RemoteViewComputationCache(getRemoteClient(), cacheKey, getMaxLocalCachedElements());
          _cachesByKey.put(cacheKey, remoteCache);
        }
      } finally {
        _cacheCreationLock.unlock();
      }
    }
    assert remoteCache != null;
    return remoteCache;
  }

  @Override
  public void releaseCaches(String viewName, long timestamp) {
    _cacheCreationLock.lock();
    try {
      Iterator<Map.Entry<ViewComputationCacheKey, RemoteViewComputationCache>> entryIter = _cachesByKey.entrySet().iterator();
      while (entryIter.hasNext()) {
        Map.Entry<ViewComputationCacheKey, RemoteViewComputationCache> entry = entryIter.next();
        ViewComputationCacheKey cacheKey = entry.getKey();
        if (!ObjectUtils.equals(cacheKey.getViewName(), viewName)) {
          continue;
        }
        if (cacheKey.getSnapshotTimestamp() != timestamp) {
          continue;
        }
        entry.getValue().getLocalCache().dispose();
        entryIter.remove();
      }
    } finally {
      _cacheCreationLock.unlock();
    }
    // This is wrong; only the owner should purge
    getRemoteClient().purgeCache(viewName, timestamp);
  }

  public void releaseAllLocalCaches() {
    s_logger.info("Releasing all local caches.");
    _cacheCreationLock.lock();
    try {
      final CacheManager manager = RemoteViewComputationCache.getCacheManager();
      for (RemoteViewComputationCache cache : _cachesByKey.values()) {
        final Cache localCache = cache.getLocalCache();
        localCache.dispose();
        manager.removeCache(localCache.getName());
      }
      _cachesByKey.clear();
    } finally {
      _cacheCreationLock.unlock();
    }
  }
}
