/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.ehcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Utilities for working with EHCache.
 */
public final class EHCacheUtils {

  /**
   * Restrictive constructor.
   */
  private EHCacheUtils() {
  }

  /**
   * Creates a cache manager.
   * @return the cache manager, not null
   */
  public static CacheManager createCacheManager() {
    try {
      return CacheManager.create();
    } catch (CacheException ex) {
      throw new OpenGammaRuntimeException("Unable to create CacheManager", ex);
    }
  }

  /**
   * Adds a cache to the cache manager if necessary.
   * @param manager  the cache manager, not null
   * @param cache  the cache, not null
   */
  public static void addCache(CacheManager manager, Cache cache) {
    ArgumentChecker.notNull(manager, "manager");
    ArgumentChecker.notNull(cache, "cache");
    if (!manager.cacheExists(cache.getName())) {
      try {
        manager.addCache(cache);
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException("Unable to add cache " + cache.getName(), ex);
      }
    }
  }

  /**
   * Adds a cache to the cache manager if necessary.
   * @param manager  the cache manager, not null
   * @param name  the cache name, not null
   */
  public static void addCache(final CacheManager manager, final String name) {
    if (!manager.cacheExists(name)) {
      try {
        manager.addCache(name);
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException("Unable to create cache " + name, ex);
      }
    }
  }

  /**
   * Adds a cache to the cache manager if necessary.
   * @param manager  the cache manager, not null
   * @param name  the cache name, not null
   * @param maxElementsInMemory  the maximum elements in memory
   * @param memoryStoreEvictionPolicy  the eviction policy
   * @param overflowToDisk  whether to overflow to disk
   * @param diskStorePath  the path on disk
   * @param eternal  eternal
   * @param timeToLiveSeconds  the time to live in seconds
   * @param timeToIdleSeconds  the time to idle in seconds
   * @param diskPersistent  whether the disk is persistent
   * @param diskExpiryThreadIntervalSeconds  the expiry interval in seconds
   * @param registeredEventListeners  the listeners
   */
  public static void addCache(CacheManager manager, String name,
      int maxElementsInMemory,
      MemoryStoreEvictionPolicy memoryStoreEvictionPolicy,
      boolean overflowToDisk, String diskStorePath, boolean eternal,
      long timeToLiveSeconds, long timeToIdleSeconds, boolean diskPersistent,
      long diskExpiryThreadIntervalSeconds,
      RegisteredEventListeners registeredEventListeners) {
    ArgumentChecker.notNull(manager, "manager");
    ArgumentChecker.notNull(name, "name");
    if (!manager.cacheExists(name)) {
      try {
        manager.addCache(new Cache(name, maxElementsInMemory,
            memoryStoreEvictionPolicy, overflowToDisk, diskStorePath, eternal,
            timeToLiveSeconds, timeToIdleSeconds, diskPersistent,
            diskExpiryThreadIntervalSeconds, registeredEventListeners));
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException("Unable to create cache " + name, ex);
      }
    }
  }

  /**
   * Gets a cache from the manager.
   * @param manager  the manager, not null
   * @param name  the cache name, not null
   * @return the cache, not null
   */
  public static Cache getCacheFromManager(CacheManager manager, String name) {
    try {
      return manager.getCache(name);
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException(
          "Unable to retrieve from CacheManager, cache: " + name, ex);
    }
  }
  
  /**
   * Clears the contents of all caches (wihtout deleting the caches
   * themselves). Should be called e.g. between tests.
   */
  public static void clearAll() {
    CacheManager.create().clearAll();
  }

}
