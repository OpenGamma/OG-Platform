/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;

import javax.time.calendar.LocalDate;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.tuple.Triple;

/**
 * A <code>HistoricalTimeSeriesResolver</code> that tries to find the distribution spec in a cache. If it doesn't find it, it will delegate to an underlying <code>HistoricalTimeSeriesResolver</code>.
 */
public class EHCachingHistoricalTimeSeriesResolver implements HistoricalTimeSeriesResolver {

  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingHistoricalTimeSeriesResolver.class);

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

  private static final int OPTIMISTIC_ON = 1;
  private static final int OPTIMISTIC_AUTO = 2;

  private volatile int _optimisticFieldResolution = OPTIMISTIC_ON | OPTIMISTIC_AUTO;
  private final AtomicInteger _optimisticFieldMetric1 = new AtomicInteger();
  private final AtomicInteger _optimisticFieldMetric2 = new AtomicInteger();

  // TODO: Do we need optimistic identifier resolution? E.g. are there graphs with large number of currency identifiers as their targets and nothing in HTS for them?

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

  /**
   * Sets whether to assume the time series is likely to exist or not. Optimistic resolution will always go to the underlying immediately. Pessimistic resolution will cache source/provider/field
   * combinations that are known to exist or not exist and check these first to avoid hitting the underlying so heavily. If the resolutions will mostly succeed, use an optimistic mode. If the
   * resolutions will mostly fail, use a pessimistic mode.
   * 
   * @param optimisticResolution the mode to set
   */
  public void setOptimisticFieldResolution(final boolean optimisticResolution) {
    if (optimisticResolution) {
      _optimisticFieldResolution |= OPTIMISTIC_ON;
    } else {
      _optimisticFieldResolution &= ~OPTIMISTIC_ON;
    }
  }

  public boolean isOptimisticFieldResolution() {
    return (_optimisticFieldResolution & OPTIMISTIC_ON) != 0;
  }

  /**
   * Turns the automatic setting of the optimistic field resolution flag on/off.
   * 
   * @param auto true to use the automatic setting algorithm, false to disable - call {@link #setOptimisticFieldResolution} to set a mode
   */
  public void setAutomaticFieldResolutionOptimisation(final boolean auto) {
    if (auto) {
      _optimisticFieldResolution |= OPTIMISTIC_AUTO;
    } else {
      _optimisticFieldResolution &= ~OPTIMISTIC_AUTO;
    }
  }

  public boolean isAutomaticFieldResolutionOptimisation() {
    return (_optimisticFieldResolution & OPTIMISTIC_AUTO) != 0;
  }

  private void updateAutoFieldResolutionOptimisation() {
    if (_optimisticFieldMetric2.incrementAndGet() == 1000) {
      _optimisticFieldMetric2.set(0);
      final int cmp = _optimisticFieldMetric1.getAndSet(0);
      if (cmp < -500) {
        boolean opt = !isOptimisticFieldResolution();
        s_logger.info("Switching to {} field resolution ({})", opt ? "optimistic" : "pessimistic", cmp);
        setOptimisticFieldResolution(opt);
      } else {
        if (s_logger.isDebugEnabled()) {
          s_logger.debug("Staying with {} field resolution ({})", isOptimisticFieldResolution() ? "optimistic" : "pessimistic", cmp);
        }
      }
    }
  }

  private boolean verifyInDatabase(final String dataSource, final String dataProvider, final String dataField) {
    final Triple<String, String, String> key = Triple.of(dataSource, dataProvider, dataField);
    if (_underlying.resolve(null, null, dataSource, dataProvider, dataField, null) != null) {
      // There is something in the database
      s_logger.debug("Verified {} in database", key);
      _cache.put(new Element(key, Boolean.TRUE));
      return true;
    } else {
      // There is nothing in the database for this combination
      s_logger.debug("Verified {} absent from database", key);
      _cache.put(new Element(key, null));
      if (isAutomaticFieldResolutionOptimisation()) {
        updateAutoFieldResolutionOptimisation();
      }
      return false;
    }
  }

  @Override
  public HistoricalTimeSeriesResolutionResult resolve(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate,
      final String dataSource, final String dataProvider, final String dataField, final String resolutionKey) {
    HistoricalTimeSeriesResolutionResult resolveResult;
    boolean knownPresent = false;
    Element e;
    if ((identifierBundle != null) && isOptimisticFieldResolution()) {
      knownPresent = true;
    } else {
      e = _cache.get(Triple.of(dataSource, dataProvider, dataField));
      if (e != null) {
        if (e.getObjectValue() == null) {
          // We've already checked and there are NO time series with this source/provider/field combo
          if (isAutomaticFieldResolutionOptimisation()) {
            _optimisticFieldMetric1.incrementAndGet();
            updateAutoFieldResolutionOptimisation();
          }
          return null;
        } else {
          // We know there's at least one time-series
          knownPresent = true;
          if (isAutomaticFieldResolutionOptimisation()) {
            _optimisticFieldMetric1.decrementAndGet();
            updateAutoFieldResolutionOptimisation();
          }
        }
      } else {
        s_logger.debug("No lookup information for {}", dataField);
      }
    }
    if (identifierBundle == null) {
      if (knownPresent || verifyInDatabase(dataSource, dataProvider, dataField)) {
        return new HistoricalTimeSeriesResolutionResult(null);
      } else {
        return null;
      }
    }
    final String validityDate = (identifierValidityDate != null) ? identifierValidityDate.toString() : "";
    for (ExternalId id : identifierBundle) {
      String key = id.toString() + SEPARATOR +
                dataField + SEPARATOR +
                (dataSource != null ? dataSource : "") + SEPARATOR +
                (dataProvider != null ? dataProvider : "") + SEPARATOR +
                resolutionKey + SEPARATOR +
                validityDate;
      e = _cache.get(key);
      if (e != null) {
        resolveResult = (HistoricalTimeSeriesResolutionResult) e.getObjectValue();
        if (isAutomaticFieldResolutionOptimisation()) {
          if (isOptimisticFieldResolution()) {
            if (resolveResult != null) {
              _optimisticFieldMetric1.incrementAndGet();
            } else {
              _optimisticFieldMetric1.decrementAndGet();
            }
          }
          updateAutoFieldResolutionOptimisation();
        }
        return resolveResult;
      }
    }
    if (!knownPresent) {
      if (!verifyInDatabase(dataSource, dataProvider, dataField)) {
        return null;
      }
    }
    resolveResult = _underlying.resolve(identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField, resolutionKey);
    if (resolveResult != null) {
      ManageableHistoricalTimeSeriesInfo info = resolveResult.getHistoricalTimeSeriesInfo();
      for (ExternalIdWithDates id : info.getExternalIdBundle()) {
        String key;
        if (id.isValidOn(identifierValidityDate)) {
          key = id.getExternalId().toString() + SEPARATOR +
                  dataField + SEPARATOR +
                  info.getDataSource() + SEPARATOR +
                  info.getDataProvider() + SEPARATOR +
                  resolutionKey + SEPARATOR +
                  validityDate;
          _cache.put(new Element(key, resolveResult));
        }
        if (id.isValidOn(identifierValidityDate)) {
          key = id.getExternalId().toString() + SEPARATOR +
                  dataField + SEPARATOR +
                  SEPARATOR +
                  info.getDataProvider() + SEPARATOR +
                  resolutionKey + SEPARATOR +
                  validityDate;
          _cache.put(new Element(key, resolveResult));
        }
        if (id.isValidOn(identifierValidityDate)) {
          key = id.getExternalId().toString() + SEPARATOR +
                dataField + SEPARATOR +
                info.getDataSource() + SEPARATOR +
                SEPARATOR +
                resolutionKey + SEPARATOR +
                validityDate;
          _cache.put(new Element(key, resolveResult));
        }
        if (id.isValidOn(identifierValidityDate)) {
          key = id.getExternalId().toString() + SEPARATOR +
                dataField + SEPARATOR +
                SEPARATOR +
                SEPARATOR +
                resolutionKey + SEPARATOR +
                validityDate;
          _cache.put(new Element(key, resolveResult));
        }
      }
      if (isAutomaticFieldResolutionOptimisation()) {
        if (isOptimisticFieldResolution()) {
          _optimisticFieldMetric1.incrementAndGet();
        }
        updateAutoFieldResolutionOptimisation();
      }
    } else {
      // PLAT-2633: Record resolution failures (misses) in the cache as well
      for (ExternalId id : identifierBundle) {
        String key = id.toString() + SEPARATOR +
                dataField + SEPARATOR +
                (dataSource != null ? dataSource : "") + SEPARATOR +
                (dataProvider != null ? dataProvider : "") + SEPARATOR +
                resolutionKey + SEPARATOR +
                validityDate;
        _cache.put(new Element(key, null));
      }
      if (isAutomaticFieldResolutionOptimisation()) {
        if (isOptimisticFieldResolution()) {
          _optimisticFieldMetric1.decrementAndGet();
        }
        updateAutoFieldResolutionOptimisation();
      }
    }
    return resolveResult;
  }

}
