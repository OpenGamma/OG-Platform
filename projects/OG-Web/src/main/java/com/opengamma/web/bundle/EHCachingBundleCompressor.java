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
 * Cache decorating the compressor of bundle source code.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingBundleCompressor implements BundleCompressor {

  /**
   * The cache key for bundles.
   */
  private static final String BUNDLE_CACHE = "bundle";

  /**
   * The underlying compressed bundle source
   */
  private final BundleCompressor _underlying;
  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  /**
   * The cache.
   */
  private final Cache _bundleCache;

  /**
   * Creates the cache around an underlying compressed bundle source.
   * 
   * @param underlying  the underlying data, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingBundleCompressor(final BundleCompressor underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, BUNDLE_CACHE);
    _bundleCache = EHCacheUtils.getCacheFromManager(cacheManager, BUNDLE_CACHE);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying compressor.
   * 
   * @return the underlying compressor, not null
   */
  protected BundleCompressor getUnderlying() {
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

  //-------------------------------------------------------------------------
  @Override
  public String compressBundle(Bundle bundle) {
    Element e = _bundleCache.get(bundle.getId());
    if (e != null) {
      return (String) e.getObjectValue();
    } else {
      String compressed = getUnderlying().compressBundle(bundle);
      if (compressed != null) {
        _bundleCache.put(new Element(bundle.getId(), compressed));
      }
      return compressed;
    }
  }

}
