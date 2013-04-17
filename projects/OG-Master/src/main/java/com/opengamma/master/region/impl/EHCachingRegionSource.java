/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.AbstractEHCachingSourceWithExternalBundle;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache decorating a {@code RegionSource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingRegionSource extends AbstractEHCachingSourceWithExternalBundle<Region, RegionSource> implements RegionSource {

  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingRegionSource.class);
  /**
   * The cache name.
   */
  private static final String CACHE_NAME = "RegionCache";

  /**
   * The cache.
   */
  private final Cache _cache;

  /**
   * Creates an instance.
   * 
   * @param underlying the underlying source, not null
   * @param cacheManager the cache manager, not null
   */
  public EHCachingRegionSource(RegionSource underlying, CacheManager cacheManager) {
    super(underlying, cacheManager);
    EHCacheUtils.addCache(cacheManager, CACHE_NAME);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_NAME);
  }

  @Override
  public Region getHighestLevelRegion(ExternalId externalId) {
    return getHighestLevelRegion(ExternalIdBundle.of(externalId));
  }

  @Override
  public Region getHighestLevelRegion(ExternalIdBundle bundle) {
    Region result = null;
    Element element = _cache.get(bundle);
    if (element != null) {
      s_logger.debug("Cache hit on {}", bundle);
      result = (Region) element.getObjectValue();
    } else {
      s_logger.debug("Cache miss on {}", bundle);
      result = getUnderlying().getHighestLevelRegion(bundle);
      s_logger.debug("Caching regions {}", result);
      element = new Element(bundle, result);
      _cache.put(element);
      if (result != null) {
        _cache.put(new Element(result.getUniqueId(), result));
      }
    }
    return result;
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle method.
   */
  @Override
  public void shutdown() {
    super.shutdown();
    _cache.getCacheManager().removeCache(CACHE_NAME);
  }

}
