/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.ehcache;

import java.util.Collection;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Utilities for working with EHCache.
 */
public final class EHCacheUtils {

  private static final Object NULL = new Object();

  /**
   * Restrictive constructor.
   */
  private EHCacheUtils() {
  }

  /**
   * Creates a cache manager using the default configuration. This should be used only in a test environment; in other
   * environments a shared, configured cache manager should be injected.
   *
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
   * <p>
   * The cache configuration is loaded from the manager's configuration, or the default is used.
   *
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
   * <p>
   * The cache configuration is loaded from the manager's configuration, or the default is used.
   *
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

  @SuppressWarnings("unchecked")
  public static <T> T get(final Element e) {
    final Object o = e.getObjectValue();
    if (o == NULL) {
      return null;
    }
    if (o instanceof RuntimeException) {
      throw (RuntimeException) o;
    }
    return (T) o;
  }

  public static <T> Collection<T> putValues(final Object key, final Collection<T> values, final Cache cache) {
    final Element e;
    if (values == null) {
      e = new Element(key, NULL);
    } else {
      e = new Element(key, values);
    }
    cache.put(e);
    return values;
  }

  public static <T> T putValue(final Object key, final T value, final Cache cache) {
    final Element e;
    if (value == null) {
      e = new Element(key, NULL);
    } else {
      e = new Element(key, value);
    }
    cache.put(e);
    return value;
  }

  public static <T> T putException(final Object key, final RuntimeException e, final Cache cache) {
    cache.put(new Element(key, e));
    throw e;
  }
}
