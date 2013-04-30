/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.joda.beans.JodaBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.time.LocalDateRange;

/**
 * A cache decorating a time-series provider.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingHistoricalTimeSeriesProvider extends AbstractHistoricalTimeSeriesProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingHistoricalTimeSeriesProvider.class);

  /**
   * The cache name.
   */
  private static final String DATA_CACHE_NAME = "HistoricalTimeSeriesProviderCache";
  /**
   * The object representing a cache miss.
   */
  private static final LocalDateDoubleTimeSeries NO_HTS = ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;

  /**
   * The underlying provider.
   */
  private final HistoricalTimeSeriesProvider _underlying;
  /**
   * The cache.
   */
  private final Cache _cache;

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying source, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingHistoricalTimeSeriesProvider(HistoricalTimeSeriesProvider underlying, CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "Cache Manager");
    _underlying = underlying;
    EHCacheUtils.addCache(cacheManager, DATA_CACHE_NAME);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, DATA_CACHE_NAME);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying provider.
   * 
   * @return the underlying provider, not null
   */
  public HistoricalTimeSeriesProvider getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache.
   * 
   * @return the cache, not null
   */
  public Cache getCache() {
    return _cache;
  }

  //-------------------------------------------------------------------------
  @Override
  protected HistoricalTimeSeriesProviderGetResult doBulkGet(HistoricalTimeSeriesProviderGetRequest request) {
    HistoricalTimeSeriesProviderGetResult result = new HistoricalTimeSeriesProviderGetResult();
    
    // find in cache
    Set<ExternalIdBundle> remainingIds = new HashSet<ExternalIdBundle>();
    for (ExternalIdBundle bundle : request.getExternalIdBundles()) {
      HistoricalTimeSeriesProviderGetRequest key = createCacheKey(request, bundle, false);
      LocalDateDoubleTimeSeries cached = doSingleGetInCache(key);
      if (cached != null) {
        if (cached == NO_HTS) {
          result.getResultMap().put(bundle, null);
        } else {
          result.getResultMap().put(bundle, cached);
        }
      } else {
        remainingIds.add(bundle);
      }
    }
    
    // find in underlying
    if (remainingIds.size() > 0) {
      HistoricalTimeSeriesProviderGetRequest underlyingAllRequest = JodaBeanUtils.clone(request);
      underlyingAllRequest.setExternalIdBundles(remainingIds);
      underlyingAllRequest.setDateRange(LocalDateRange.ALL);
      underlyingAllRequest.setMaxPoints(null);
      HistoricalTimeSeriesProviderGetResult underlyingAllResult = _underlying.getHistoricalTimeSeries(underlyingAllRequest);
      
      // cache result for whole time-series
      for (ExternalIdBundle bundle : remainingIds) {
        LocalDateDoubleTimeSeries underlyingWholeHts = underlyingAllResult.getResultMap().get(bundle);
        if (underlyingWholeHts == null) {
          underlyingWholeHts = NO_HTS;
        }
        HistoricalTimeSeriesProviderGetRequest wholeHtsKey = createCacheKey(underlyingAllRequest, bundle, true);
        _cache.put(new Element(wholeHtsKey, underlyingWholeHts));
      }
      
      // cache result for requested time-series
      HistoricalTimeSeriesProviderGetResult fiteredResult = filterResult(underlyingAllResult, request.getDateRange(), request.getMaxPoints());
      for (ExternalIdBundle bundle : remainingIds) {
        LocalDateDoubleTimeSeries filteredHts = fiteredResult.getResultMap().get(bundle);
        result.getResultMap().put(bundle, filteredHts);
        if (filteredHts == null) {
          filteredHts = NO_HTS;
        }
        HistoricalTimeSeriesProviderGetRequest key = createCacheKey(request, bundle, false);
        _cache.put(new Element(key, filteredHts));
      }
    }
    return result;
  }

  /**
   * Lookup when there is only one bundle in the request.
   * 
   * @param requestKey  the request suitable for use as the cache key, not null
   * @return the result, not null
   */
  protected LocalDateDoubleTimeSeries doSingleGetInCache(HistoricalTimeSeriesProviderGetRequest requestKey) {
    // find in cache
    Element cacheElement = _cache.get(requestKey);
    if (cacheElement != null) {
      s_logger.debug("Found time-series in cache: {}", requestKey);
      return (LocalDateDoubleTimeSeries) cacheElement.getObjectValue();
    }
    
    // find whole time-series in cache
    if (requestKey.getMaxPoints() != null || requestKey.getDateRange().equals(LocalDateRange.ALL) == false) {
      HistoricalTimeSeriesProviderGetRequest wholeHtsKey = createCacheKey(requestKey, null, true);
      cacheElement = _cache.get(wholeHtsKey);
      if (cacheElement != null) {
        if (cacheElement.getObjectValue() == NO_HTS) {
          return NO_HTS;
        }
        LocalDateDoubleTimeSeries wholeHts = (LocalDateDoubleTimeSeries) cacheElement.getObjectValue();
        LocalDateDoubleTimeSeries filteredHts = filterResult(wholeHts, requestKey.getDateRange(), requestKey.getMaxPoints());
        _cache.put(new Element(requestKey, filteredHts));  // re-cache under filtered values
        s_logger.debug("Derived time-series from cache: {}", requestKey);
        return filteredHts;
      }
    }
    
    // not in cache
    return null;
  }

  /**
   * Creates a cache key.
   * 
   * @param request  the base request object, not null
   * @param bundle  the bundle to set, null to leave as is (already one key)
   * @param allDataPoints  true to create a key for all data points
   * @return a clone of the request with the bundle set, not null
   */
  protected HistoricalTimeSeriesProviderGetRequest createCacheKey(HistoricalTimeSeriesProviderGetRequest request, ExternalIdBundle bundle, boolean allDataPoints) {
    HistoricalTimeSeriesProviderGetRequest key = JodaBeanUtils.clone(request);
    if (bundle != null) {
      key.setExternalIdBundles(Collections.singleton(bundle));
    }
    if (allDataPoints) {
      key.setDateRange(LocalDateRange.ALL);
      key.setMaxPoints(null);
    }
    return key;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUnderlying() + "]";
  }

}
