/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import javax.time.calendar.LocalDate;
import java.text.MessageFormat;

/**
 * A <code>HistoricalTimeSeriesResolver</code> that tries to find
 * the distribution spec in a cache. If it doesn't find it, it will 
 * delegate to an underlying <code>HistoricalTimeSeriesResolver</code>.
 *
 */
public class EHCachingHistoricalTimeSeriesResolver implements HistoricalTimeSeriesResolver {

  private static final String SEPARATOR = "~";

  /**
   * Cache key format for hts resolution.
   */
  private static final String HISTORICAL_TIME_SERIES_RESOLUTION_CACHE_FORMAT = "htsResolution.{0}";
  /**
   * Default cache key format arg.
   */
  private static final String HISTORICAL_TIME_SERIES_RESOLUTION_CACHE_DEFAULT_ARG = "DEFAULT";

  private final HistoricalTimeSeriesResolver _underlying;

  /**
   * The cache manager.
   */
  private final CacheManager _cacheManager;

  /**
   * The reference data cache.
   */
  private final Cache _cache;

  public EHCachingHistoricalTimeSeriesResolver(final HistoricalTimeSeriesResolver underlying, final CacheManager cacheManager) {
    this(underlying, cacheManager, HISTORICAL_TIME_SERIES_RESOLUTION_CACHE_DEFAULT_ARG);
  }

  public EHCachingHistoricalTimeSeriesResolver(final HistoricalTimeSeriesResolver underlying, final CacheManager cacheManager, String cacheName) {
    ArgumentChecker.notNull(underlying, "Underlying HistoricalTimeSeriesResolver");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    ArgumentChecker.notNull(cacheName, "cacheName");
    _underlying = underlying;
    _cacheManager = cacheManager;
    String combinedCacheName = MessageFormat.format(HISTORICAL_TIME_SERIES_RESOLUTION_CACHE_FORMAT, cacheName);
    EHCacheUtils.addCache(cacheManager, combinedCacheName);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, combinedCacheName);
  }
  
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  @Override
  public HistoricalTimeSeriesResolutionResult resolve(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, String resolutionKey) {

    for (ExternalId id : identifierBundle) {
      String key =
          id.toString() + SEPARATOR +
          dataField + SEPARATOR +
          (dataSource != null ? dataSource : "") + SEPARATOR +
          (dataProvider != null ? dataProvider : "") + SEPARATOR +
          resolutionKey + SEPARATOR +
          (identifierValidityDate != null ? identifierValidityDate.toString() : "");
      Element cachedHtsInfo = _cache.get(key);
      if (cachedHtsInfo != null) {
        return (HistoricalTimeSeriesResolutionResult) cachedHtsInfo.getObjectValue();
      }
    }

    HistoricalTimeSeriesResolutionResult returnValue =
        _underlying.resolve(identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField, resolutionKey);

    if (returnValue != null) {
      ManageableHistoricalTimeSeriesInfo info = returnValue.getHistoricalTimeSeriesInfo();
      for (ExternalId id : info.getExternalIdBundle().toBundle()) {

        String key =
            id.toString() + SEPARATOR +
            dataField + SEPARATOR +
            info.getDataSource() + SEPARATOR +
            info.getDataProvider() + SEPARATOR +
            resolutionKey + SEPARATOR +
            (identifierValidityDate != null ? identifierValidityDate.toString() : "");
        _cache.put(new Element(key, returnValue));

        key =
            id.toString() + SEPARATOR +
            dataField + SEPARATOR +
            SEPARATOR +
            info.getDataProvider() + SEPARATOR +
            resolutionKey + SEPARATOR +
            (identifierValidityDate != null ? identifierValidityDate.toString() : "");
        _cache.put(new Element(key, returnValue));

        key =
            id.toString() + SEPARATOR +
            dataField + SEPARATOR +
            info.getDataSource() + SEPARATOR +
            SEPARATOR +
            resolutionKey + SEPARATOR +
            (identifierValidityDate != null ? identifierValidityDate.toString() : "");
        _cache.put(new Element(key, returnValue));

        key =
            id.toString() + SEPARATOR +
            dataField + SEPARATOR +
            SEPARATOR +
            SEPARATOR +
            resolutionKey + SEPARATOR +
            (identifierValidityDate != null ? identifierValidityDate.toString() : "");
        _cache.put(new Element(key, returnValue));

      }
    }

    return returnValue;
  }

}
