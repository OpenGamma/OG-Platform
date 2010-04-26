/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import com.opengamma.OpenGammaRuntimeException;

/**
 * 
 *
 * @author kirk
 */
public final class EHCacheUtils {
  private EHCacheUtils() {
  }

  /**
   * @param manager
   * @return
   */
  public static CacheManager createCacheManager() {
    CacheManager manager = null;
    try {
      manager = CacheManager.create();
    } catch (CacheException e) {
      throw new OpenGammaRuntimeException("Unable to create CacheManager", e);
    }
    return manager;
  }

  /**
   * @param manager
   * @param cache
   */
  public static void addCache(CacheManager manager, Cache cache) {
    ArgumentChecker.notNull(manager, "CacheManager");
    ArgumentChecker.notNull(cache, "Cache");
    if (!manager.cacheExists(cache.getName())) {
      try {
        manager.addCache(cache);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Unable to add cache " + cache.getName(), e);
      }
    }

  }

  /**
   * @param manager
   */
  public static void addCache(final CacheManager manager, final String name) {
    if (!manager.cacheExists(name)) {
      try {
        manager.addCache(name);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Unable to create cache " + name, e);
      }
    }
  }

  /**
   * @param manager
   * @param name
   * @param maxElementsInMemory
   * @param memoryStoreEvictionPolicy
   * @param overflowToDisk
   * @param diskStorePath
   * @param eternal
   * @param timeToLiveSeconds
   * @param timeToIdleSeconds
   * @param diskPersistent
   * @param diskExpiryThreadIntervalSeconds
   * @param registeredEventListeners
   */
  public static void addCache(CacheManager manager, String name,
      int maxElementsInMemory,
      MemoryStoreEvictionPolicy memoryStoreEvictionPolicy,
      boolean overflowToDisk, String diskStorePath, boolean eternal,
      long timeToLiveSeconds, long timeToIdleSeconds, boolean diskPersistent,
      long diskExpiryThreadIntervalSeconds,
      RegisteredEventListeners registeredEventListeners) {
    ArgumentChecker.notNull(manager, "CacheManager");
    ArgumentChecker.notNull(name, "CacheName");
    if (!manager.cacheExists(name)) {
      try {
        manager.addCache(new Cache(name, maxElementsInMemory,
            memoryStoreEvictionPolicy, overflowToDisk, diskStorePath, eternal,
            timeToLiveSeconds, timeToIdleSeconds, diskPersistent,
            diskExpiryThreadIntervalSeconds, registeredEventListeners));
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Unable to create cache " + name, e);
      }
    }
  }

  /**
   * @param manager
   * @param name
   * @return
   */
  public static Cache getCacheFromManager(CacheManager manager, String name) {
    Cache cache = null;
    try {
      cache = manager.getCache(name);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException(
          "Unable to retrieve from CacheManager, cache: " + name, e);
    }
    return cache;
  }

}
