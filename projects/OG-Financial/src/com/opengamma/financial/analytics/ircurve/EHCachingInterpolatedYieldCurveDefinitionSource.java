/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.InstantProvider;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

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
    ObjectsPair<Currency, String> cacheKey = Pair.of(currency, name);
    Element e = _latestDefinitionCache.get(cacheKey);
    if (e != null) {
      YieldCurveDefinition doc = (YieldCurveDefinition) e.getValue();
      return doc;
    } else {
      YieldCurveDefinition doc = _underlying.getDefinition(currency, name);
      Element element = new Element(cacheKey, doc);
      element.setTimeToLive(10); // TODO PLAT-1308: I've set TTL short to hide the fact that we return stale data
      _latestDefinitionCache.put(element);   
      return doc;
    }
  }

  @Override
  public YieldCurveDefinition getDefinition(Currency currency, String name, InstantProvider version) {
    return _underlying.getDefinition(currency, name, version); // TODO PLAT-1308: I'm not caching this because this cache doesn't version things properly
  }

}
