/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache decorating a {@code CompressedBundleSource}.
 * 
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingCompressedBundleSource implements CompressedBundleSource {
  
  /**
   * Cache key for bundles
   */
  private static final String BUNDLE_CACHE = "bundle";
  
  /**
   * The underlying compressed bundle source
   */
  private final CompressedBundleSource _underlying;
  
  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  
  /**
   * The portfolio cache.
   */
  private final Cache _bundleCache;
  
  /**
   * Creates the cache around an underlying compressed bundle source.
   * 
   * @param underlying  the underlying data, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingCompressedBundleSource(final CompressedBundleSource underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, BUNDLE_CACHE);
    _bundleCache = EHCacheUtils.getCacheFromManager(cacheManager, BUNDLE_CACHE);
  }
  
  /**
   * Gets the underlying source of bundles
   * 
   * @return the underlying source of bundles, not null
   */
  protected CompressedBundleSource getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache manager.
   * 
   * @return the cache manager, not null
   */
  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  @Override
  public String getBundle(String bundleId) {
    Element e = _bundleCache.get(bundleId);
    if (e != null) {
      return (String) e.getValue();
    } else {
      String compressed = getUnderlying().getBundle(bundleId);
      if (compressed != null) {
        _bundleCache.put(new Element(bundleId, compressed));
      }
      return compressed;
    }
  }

}
