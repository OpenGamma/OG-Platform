/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A factory for {@link MapBackedInMemoryViewComputationCache}.
 * It fully supports cache releasing for garbage collection.
 */
public class MapBackedInMemoryViewComputationCacheSource implements ViewComputationCacheSource {
  /**
   * All current caches. This is not a concurrent map as access to it is controlled by
   * a separate lock.
   */
  private final Map<ViewComputationCacheKey, MapBackedInMemoryViewComputationCache> _currentCaches =
      new HashMap<ViewComputationCacheKey, MapBackedInMemoryViewComputationCache>();
  /**
   * Controls access to the cache.
   * We do it this way because the initial size of the underlying map in
   * MapBackedInMemoryViewComputationCache is very large, so it's an expensive object
   * to construct. We don't want to use putIfAbsent in this case.
   */
  private final ReadWriteLock _cacheLock = new ReentrantReadWriteLock();

  @Override
  public ViewComputationCache getCache(UniqueId viewCycleId, String calculationConfigurationName) {
    ViewComputationCacheKey key = new ViewComputationCacheKey(viewCycleId, calculationConfigurationName);
    MapBackedInMemoryViewComputationCache cache = null;
    _cacheLock.readLock().lock();
    try {
      cache = _currentCaches.get(key);
      if (cache != null) {
        return cache;
      }
    } finally {
      _cacheLock.readLock().unlock();
    }

    _cacheLock.writeLock().lock();
    try {
      cache = _currentCaches.get(key);
      if (cache == null) {
        cache = new MapBackedInMemoryViewComputationCache();
        _currentCaches.put(key, cache);
      }
      return cache;
    } finally {
      _cacheLock.writeLock().unlock();
    }
  }

  @Override
  public ViewComputationCache cloneCache(UniqueId viewCycleId, String calculationConfigurationName) {
    ViewComputationCacheKey key = new ViewComputationCacheKey(viewCycleId, calculationConfigurationName);
    _cacheLock.readLock().lock();
    try {
      MapBackedInMemoryViewComputationCache cache = _currentCaches.get(key);
      if (cache == null) {
        return null;
      }
      return new MapBackedInMemoryViewComputationCache(cache);
    } finally {
      _cacheLock.readLock().unlock();
    }
  }

  @Override
  public void releaseCaches(UniqueId viewCycleId) {
    ArgumentChecker.notNull(viewCycleId, "viewCycleId");
    _cacheLock.writeLock().lock();
    try {
      Iterator<Map.Entry<ViewComputationCacheKey, MapBackedInMemoryViewComputationCache>> iterator = _currentCaches.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<ViewComputationCacheKey, MapBackedInMemoryViewComputationCache> entry = iterator.next();
        if (entry.getKey().getViewCycleId().equals(viewCycleId)) {
          // This is just to give a little extra help to the GC in the event that it doesn't
          // clear up the parent object.
          entry.getValue().clear();
          iterator.remove();
        }
      }
    } finally {
      _cacheLock.writeLock().unlock();
    }
  }

}
