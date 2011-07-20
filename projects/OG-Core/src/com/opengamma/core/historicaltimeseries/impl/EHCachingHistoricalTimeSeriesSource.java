/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A cache decorating a {@code HistoricalTimeSeriesSource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingHistoricalTimeSeriesSource implements HistoricalTimeSeriesSource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingHistoricalTimeSeriesSource.class);
  /**
   * The cache name.
   */
  private static final String CACHE_NAME = "HistoricalTimeSeriesCache";

  /**
   * The underlying source.
   */
  private final HistoricalTimeSeriesSource _underlying;
  /**
   * The cache.
   */
  private final Cache _cache;

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying source, not null
   * @param cacheManager  the cache manager, not null
   * @param maxElementsInMemory  cache configuration
   * @param memoryStoreEvictionPolicy  cache configuration
   * @param overflowToDisk  cache configuration
   * @param diskStorePath  cache configuration
   * @param eternal  cache configuration
   * @param timeToLiveSeconds  cache configuration
   * @param timeToIdleSeconds  cache configuration
   * @param diskPersistent  cache configuration
   * @param diskExpiryThreadIntervalSeconds  cache configuration
   * @param registeredEventListeners  cache configuration
   */
  public EHCachingHistoricalTimeSeriesSource(
      final HistoricalTimeSeriesSource underlying, final CacheManager cacheManager, final int maxElementsInMemory,
      final MemoryStoreEvictionPolicy memoryStoreEvictionPolicy, final boolean overflowToDisk, final String diskStorePath,
      final boolean eternal, final long timeToLiveSeconds, final long timeToIdleSeconds, final boolean diskPersistent,
      final long diskExpiryThreadIntervalSeconds, final RegisteredEventListeners registeredEventListeners) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    EHCacheUtils.addCache(cacheManager, CACHE_NAME, maxElementsInMemory, memoryStoreEvictionPolicy, overflowToDisk, diskStorePath,
        eternal, timeToLiveSeconds, timeToIdleSeconds, diskPersistent, diskExpiryThreadIntervalSeconds, registeredEventListeners);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_NAME);
  }

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying source, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingHistoricalTimeSeriesSource(HistoricalTimeSeriesSource underlying, CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "Cache Manager");
    _underlying = underlying;
    EHCacheUtils.addCache(cacheManager, CACHE_NAME);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_NAME);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying source.
   * 
   * @return the underlying source, not null
   */
  public HistoricalTimeSeriesSource getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache manager.
   * 
   * @return the cache manager, not null
   */
  public CacheManager getCacheManager() {
    return _cache.getCacheManager();
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    HistoricalTimeSeries hts = getFromCache(uniqueId);
    if (hts == null) {
      hts = _underlying.getHistoricalTimeSeries(uniqueId);
      if (hts != null) {
        s_logger.debug("Caching time-series {}", hts);
        _cache.put(new Element(uniqueId, hts));
      }
    }
    return hts;
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      UniqueIdentifier uniqueId, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(uniqueId);
    return getSubSeries(hts, start, inclusiveStart, end, exclusiveEnd);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField) {
    return getHistoricalTimeSeries(identifiers, (LocalDate) null, dataSource, dataProvider, dataField);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    HistoricalTimeSeriesKey key = new HistoricalTimeSeriesKey(null, currentDate, identifiers, dataSource, dataProvider, dataField);
    HistoricalTimeSeries hts = getFromCache(key);
    if (hts == null) {
      hts = _underlying.getHistoricalTimeSeries(identifiers, currentDate, dataSource, dataProvider, dataField);
      if (hts != null) {
        s_logger.debug("Caching time-series {}", hts);
        _cache.put(new Element(key, hts.getUniqueId()));
        _cache.put(new Element(hts.getUniqueId(), hts));
      }
    }
    return hts;
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    return getHistoricalTimeSeries(
        identifiers, (LocalDate) null, dataSource, dataProvider, dataField,
        start, inclusiveStart, end, exclusiveEnd);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    HistoricalTimeSeries tsPair = getHistoricalTimeSeries(identifiers, currentDate, dataSource, dataProvider, dataField);
    return getSubSeries(tsPair, start, inclusiveStart, end, exclusiveEnd);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, String resolutionKey) {
    return getHistoricalTimeSeries(dataField, identifierBundle, null, resolutionKey);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notEmpty(identifierBundle, "identifierBundle");
    HistoricalTimeSeriesKey key = new HistoricalTimeSeriesKey(resolutionKey, identifierValidityDate, identifierBundle, null, null, null);
    HistoricalTimeSeries hts = getFromCache(key);
    if (hts == null) {
      hts = _underlying.getHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey);
      if (hts != null) {
        s_logger.debug("Caching time-series {}", hts);
        _cache.put(new Element(key, hts.getUniqueId()));
        _cache.put(new Element(hts.getUniqueId(), hts));
      }
    }
    return hts;
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, String resolutionKey, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    return getHistoricalTimeSeries(dataField, identifierBundle, (LocalDate) null, resolutionKey, start, inclusiveStart, end, exclusiveEnd);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    HistoricalTimeSeries tsPair = getHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey);
    return getSubSeries(tsPair, start, inclusiveStart, end, exclusiveEnd);
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<IdentifierBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
      Set<IdentifierBundle> identifierSet, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    ArgumentChecker.notNull(identifierSet, "identifierSet");
    Map<IdentifierBundle, HistoricalTimeSeries> result = new HashMap<IdentifierBundle, HistoricalTimeSeries>();
    Set<IdentifierBundle> remainingIdentifiers = new HashSet<IdentifierBundle>();
    // caching works individually but all misses can be passed to underlying as one request
    for (IdentifierBundle identifiers : identifierSet) {
      HistoricalTimeSeriesKey key = new HistoricalTimeSeriesKey(null, null, identifiers, dataSource, dataProvider, dataField);
      HistoricalTimeSeries hts = getFromCache(key);
      if (hts != null) {
        hts = getSubSeries(hts, start, inclusiveStart, end, exclusiveEnd);
        result.put(identifiers, hts);
      } else {
        remainingIdentifiers.add(identifiers);
      }
    }
    if (remainingIdentifiers.size() > 0) {
      Map<IdentifierBundle, HistoricalTimeSeries> remainingTsResults =
        _underlying.getHistoricalTimeSeries(remainingIdentifiers, dataSource, dataProvider, dataField, start, inclusiveStart, end, exclusiveEnd);
      for (Map.Entry<IdentifierBundle, HistoricalTimeSeries> tsResult : remainingTsResults.entrySet()) {
        IdentifierBundle identifiers = tsResult.getKey();
        HistoricalTimeSeries hts = tsResult.getValue();
        hts = getSubSeries(hts, start, inclusiveStart, end, exclusiveEnd);
        result.put(identifiers, hts);
      }
    }
    return result;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Attempts to retrieve the time-series with the given key from the cache.
   * 
   * @param key  the key, not null
   * @return the time-series, null if no match
   */
  private HistoricalTimeSeries getFromCache(HistoricalTimeSeriesKey key) {
    Element element = _cache.get(key);
    if (element == null || element.getValue() instanceof UniqueIdentifier == false) {
      s_logger.debug("Cache miss on {}", key.getIdentifiers());
      return null;
    }
    s_logger.debug("Cache hit on {}", key.getIdentifiers());
    return getFromCache((UniqueIdentifier) element.getValue());
  }

  /**
   * Attempts to retrieve the time-series with the given unique identifier from the cache.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the time-series, null if no match
   */
  private HistoricalTimeSeries getFromCache(UniqueIdentifier uniqueId) {
    Element element = _cache.get(uniqueId);
    if (element == null || element.getValue() instanceof HistoricalTimeSeries == false) {
      s_logger.debug("Cache miss on {}", uniqueId);
      return null;
    }
    s_logger.debug("Cache hit on {}", uniqueId);
    return (HistoricalTimeSeries) element.getValue();
  }

  /**
   * Gets a sub-series based on the supplied dates.
   * 
   * @param hts  the time-series, null returns null
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return the historical time-series, null if null input
   */
  private HistoricalTimeSeries getSubSeries(
      HistoricalTimeSeries hts, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (hts == null) {
      return null;
    }
    if (hts.getTimeSeries().isEmpty()) {
      return hts;
    }
    LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) hts.getTimeSeries().subSeries(start, inclusiveStart, end, exclusiveEnd);
    return new HistoricalTimeSeriesImpl(hts.getUniqueId(), timeSeries);
  }

}
