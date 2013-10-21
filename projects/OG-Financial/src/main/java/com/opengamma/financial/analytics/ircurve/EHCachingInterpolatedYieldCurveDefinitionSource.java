/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A cache to optimize the results of {@code InterpolatedYieldCurveDefinitionSource}.
 */
public class EHCachingInterpolatedYieldCurveDefinitionSource implements InterpolatedYieldCurveDefinitionSource {

  /**
   * Cache key for latest definitions.
   */
  private static final String LATEST_DEFINITION_CACHE = "interpolatedYieldCurveDefinitionLatest";

  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;
  /**
   * The result cache.
   */
  private final Cache _latestDefinitionCache;
  /**
   * The underlying source.
   */
  private final InterpolatedYieldCurveDefinitionSource _underlying;

  /**
   * Creates the cache around an underlying definition source.
   * 
   * @param underlying  the underlying data, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingInterpolatedYieldCurveDefinitionSource(final InterpolatedYieldCurveDefinitionSource underlying, final CacheManager cacheManager) {
    _underlying = underlying;
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, LATEST_DEFINITION_CACHE);
    _latestDefinitionCache = EHCacheUtils.getCacheFromManager(cacheManager, LATEST_DEFINITION_CACHE);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the cache manager.
   * 
   * @return the cache manager, not null
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinition getDefinition(Currency currency, String name) {
    Pair<Currency, String> cacheKey = Pairs.of(currency, name);
    Element e = _latestDefinitionCache.get(cacheKey);
    if (e != null) {
      YieldCurveDefinition doc = (YieldCurveDefinition) e.getObjectValue();
      return doc;
    } else {
      YieldCurveDefinition doc = _underlying.getDefinition(currency, name);
      Element element = new Element(cacheKey, doc);
      // REVIEW: jim 19-Apr-2013 -- I've increased this from 10s to 5-10m
      element.setTimeToLive((int) (Math.random() * 150d) + 150); // TODO PLAT-1308: I've set TTL short to hide the fact that we return stale data
      
      _latestDefinitionCache.put(element);   
      return doc;
    }
  }

  @Override
  public YieldCurveDefinition getDefinition(Currency currency, String name, VersionCorrection version) {
    return _underlying.getDefinition(currency, name, version); // TODO PLAT-1308: I'm not caching this because this cache doesn't version things properly
  }

  @Override
  public ChangeManager changeManager() {
    return _underlying.changeManager();
  }

}
