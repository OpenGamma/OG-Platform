/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaldata.impl;

import java.io.Serializable;
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

import com.opengamma.core.historicaldata.HistoricalDataSource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * A cache decorating a {@code HistoricalDataSource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingHistoricalDataSource implements HistoricalDataSource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingHistoricalDataSource.class);
  /**
   * An empty time series.
   */
  private static final LocalDateDoubleTimeSeries EMPTY_TIMESERIES = new ArrayLocalDateDoubleTimeSeries();
  /**
   * The cache name.
   */
  private static final String CACHE_NAME = "HistoricalDataCache";

  /**
   * The underlying source.
   */
  private final HistoricalDataSource _underlying;
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
  public EHCachingHistoricalDataSource(
      final HistoricalDataSource underlying, final CacheManager cacheManager, final int maxElementsInMemory,
      final MemoryStoreEvictionPolicy memoryStoreEvictionPolicy, final boolean overflowToDisk, final String diskStorePath,
      final boolean eternal, final long timeToLiveSeconds, final long timeToIdleSeconds, final boolean diskPersistent,
      final long diskExpiryThreadIntervalSeconds, final RegisteredEventListeners registeredEventListeners) {
    ArgumentChecker.notNull(underlying, "Underlying Historical Data Provider");
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
  public EHCachingHistoricalDataSource(HistoricalDataSource underlying, CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "Underlying Historical Data Provider");
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
  public HistoricalDataSource getUnderlying() {
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
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    MetaDataKey key = new MetaDataKey(null, currentDate, identifiers, dataSource, dataProvider, dataField);
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getFromCache(key);
    if (tsPair == null) {
      tsPair = _underlying.getHistoricalData(identifiers, currentDate, dataSource, dataProvider, dataField);
      _cache.put(new Element(key, tsPair.getFirst()));
      if (tsPair.getFirst() != null) {
        s_logger.debug("Retrieved {} for {}", tsPair.getFirst(), identifiers);
        _cache.put(new Element(tsPair.getFirst(), tsPair.getSecond()));
      } else {
        s_logger.debug("No data returned from underlying for {}", identifiers);
      }
    }
    return tsPair;
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField) {
    return getHistoricalData(identifiers, (LocalDate) null, dataSource, dataProvider, dataField);
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String field,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getHistoricalData(identifiers, currentDate, dataSource, dataProvider, field);
    return getSubseries(start, inclusiveStart, end, exclusiveEnd, tsPair);
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String dataSource, String dataProvider, String field, LocalDate start,
      boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getHistoricalData(identifiers, dataSource, dataProvider, field);
    return getSubseries(start, inclusiveStart, end, exclusiveEnd, tsPair);
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid) {
    Element element = _cache.get(uid);
    if (element != null) {
      Serializable value = element.getValue();
      if (value instanceof LocalDateDoubleTimeSeries) {
        LocalDateDoubleTimeSeries ts = (LocalDateDoubleTimeSeries) value;
        s_logger.debug("retrieved time series: {} from cache", ts);
        return ts;
      } else {
        s_logger.error("returned object {} from cache, not a LocalDateDoubleTimeSeries", value);
        return EMPTY_TIMESERIES;
      }
    } else {
      LocalDateDoubleTimeSeries ts = _underlying.getHistoricalData(uid);
      _cache.put(new Element(uid, ts));
      return ts;
    }
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    LocalDateDoubleTimeSeries timeseries = getHistoricalData(uid);
    if (!timeseries.isEmpty()) {
      return (LocalDateDoubleTimeSeries) timeseries.subSeries(start, inclusiveStart, end, exclusiveEnd);
    } else {
      return timeseries;
    }
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String configDocName) {
    MetaDataKey key = new MetaDataKey(configDocName, currentDate, identifiers, null, null, null);
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getFromCache(key);
    if (tsPair == null) {
      tsPair = _underlying.getHistoricalData(identifiers, currentDate, configDocName);
      _cache.put(new Element(key, tsPair.getFirst()));
      if (tsPair.getFirst() != null) {
        s_logger.debug("caching {} for {}", tsPair.getFirst(), identifiers);
        _cache.put(new Element(tsPair.getFirst(), tsPair.getSecond()));
      } else {
        s_logger.debug("no data returned from underlying for {}", identifiers);
      }
    }
    return tsPair;
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String configDocName) {
    return getHistoricalData(identifiers, null, configDocName);
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String configDocName, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getHistoricalData(identifiers, configDocName);
    return getSubseries(start, inclusiveStart, end, exclusiveEnd, tsPair);
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String configDocName, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getHistoricalData(identifiers, currentDate, configDocName);
    return getSubseries(start, inclusiveStart, end, exclusiveEnd, tsPair);
  }

  @Override
  public Map<IdentifierBundle, Pair<UniqueIdentifier, LocalDateDoubleTimeSeries>> getHistoricalData(
      Set<IdentifierBundle> identifierSet, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    Map<IdentifierBundle, Pair<UniqueIdentifier, LocalDateDoubleTimeSeries>> tsPairs = new HashMap<IdentifierBundle, Pair<UniqueIdentifier, LocalDateDoubleTimeSeries>>();
    Set<IdentifierBundle> remainingIdentifiers = new HashSet<IdentifierBundle>();
    // Caching works individually but all misses can be passed to underlying as one request
    for (IdentifierBundle identifiers : identifierSet) {
      MetaDataKey key = new MetaDataKey(null, null, identifiers, dataSource, dataProvider, dataField);
      Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getFromCache(key);
      if (tsPair != null) {
        tsPair = getSubseries(start, inclusiveStart, end, exclusiveEnd, tsPair);
        tsPairs.put(identifiers, tsPair);
      } else {
        remainingIdentifiers.add(identifiers);
      }
    }
    if (!remainingIdentifiers.isEmpty()) {
      Map<IdentifierBundle, Pair<UniqueIdentifier, LocalDateDoubleTimeSeries>> remainingTsResults =
        _underlying.getHistoricalData(remainingIdentifiers, dataSource, dataProvider, dataField, start, inclusiveStart, end, exclusiveEnd);
      for (Map.Entry<IdentifierBundle, Pair<UniqueIdentifier, LocalDateDoubleTimeSeries>> tsResult : remainingTsResults.entrySet()) {
        IdentifierBundle identifiers = tsResult.getKey();
        Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = tsResult.getValue();
        tsPair = getSubseries(start, inclusiveStart, end, exclusiveEnd, tsPair);
        tsPairs.put(identifiers, tsPair);
      }
    }
    return tsPairs;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Attempts to retrieve the time-series with the given key from the cache.
   * 
   * @param key  the key, not null
   * @return the time-series, or {@code null} if no match was found in the cache
   */
  private Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getFromCache(MetaDataKey key) {
    Element element = _cache.get(key);
    if (element == null) {
      return null;
    }
    Serializable value = element.getValue();
    if (value instanceof UniqueIdentifier) {
      UniqueIdentifier uid = (UniqueIdentifier) value;
      s_logger.debug("retrieved UID: {} from cache", uid);
      LocalDateDoubleTimeSeries timeSeries = getHistoricalData(uid);
      return new ObjectsPair<UniqueIdentifier, LocalDateDoubleTimeSeries>(uid, timeSeries);
    } else if (value == null) {
      s_logger.debug("cached miss on {}", key.getIdentifiers());
      return Pair.of(null, EMPTY_TIMESERIES);
    } else {
      s_logger.warn("returned object {} from cache, not a UniqueIdentifier", value);
      return Pair.of(null, EMPTY_TIMESERIES);
    }
  }

  private Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getSubseries(LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd,
      Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair) {
    if (tsPair != null) {
      LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) tsPair.getSecond().subSeries(start, inclusiveStart, end, exclusiveEnd);
      return Pair.of(tsPair.getKey(), timeSeries);
    } else {
      return Pair.of(null, EMPTY_TIMESERIES);
    }
  }

}
