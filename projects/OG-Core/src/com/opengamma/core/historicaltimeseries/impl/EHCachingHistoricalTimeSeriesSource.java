/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

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
  private static final String DATA_CACHE_NAME = "HistoricalTimeSeriesDataCache";

  private static class MissHTS implements HistoricalTimeSeries, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public UniqueId getUniqueId() {
      throw new UnsupportedOperationException();
    }

    @Override
    public LocalDateDoubleTimeSeries getTimeSeries() {
      throw new UnsupportedOperationException();
    }

  };

  private static final MissHTS MISS = new MissHTS();

  /**
   * The underlying source.
   */
  private final HistoricalTimeSeriesSource _underlying;
  /**
   * The cache.
   */
  private final Cache _dataCache;

  /**
   * The clock.
   */
  private final Clock _clock = Clock.systemDefaultZone();  // TODO: TIMEZONE

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
    EHCacheUtils.addCache(cacheManager, DATA_CACHE_NAME, maxElementsInMemory, memoryStoreEvictionPolicy, overflowToDisk, diskStorePath,
        eternal, timeToLiveSeconds, timeToIdleSeconds, diskPersistent, diskExpiryThreadIntervalSeconds, registeredEventListeners);
    _dataCache = EHCacheUtils.getCacheFromManager(cacheManager, DATA_CACHE_NAME);
    
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
    EHCacheUtils.addCache(cacheManager, DATA_CACHE_NAME);
    _dataCache = EHCacheUtils.getCacheFromManager(cacheManager, DATA_CACHE_NAME);
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
  public CacheManager getDataCacheManager() {
    return _dataCache.getCacheManager();
  }

  /**
   * Gets the clock.
   * 
   * @return the clock, not null
   */
  public Clock getClock() {
    return _clock;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    HistoricalTimeSeries hts = getFromDataCache(uniqueId);
    if (hts != null) {
      if (hts == MISS) {
        hts = null;
      }
    } else {
      hts = _underlying.getHistoricalTimeSeries(uniqueId);
      if (hts != null) {
        s_logger.debug("Caching time-series {}", hts);
        _dataCache.put(new Element(uniqueId, hts));
      } else {
        s_logger.debug("Caching miss on {}", uniqueId);
        _dataCache.put(new Element(uniqueId, MISS));
      }
    }
    return hts;
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    return doGetHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    return doGetHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd, maxPoints);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId) {
    HistoricalTimeSeries hts = doGetHistoricalTimeSeries(uniqueId, null, true, null, true, -1);
    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }  

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    HistoricalTimeSeries hts = doGetHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd, -1);
    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(
      UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    SubSeriesKey subseriesKey = new SubSeriesKey(start, includeStart, end, includeEnd, maxPoints);
    ObjectsPair<UniqueId, SubSeriesKey> key = Pair.of(uniqueId, subseriesKey);
    Element element = _dataCache.get(key);
    HistoricalTimeSeries hts;
    if (element != null) {
      hts = (HistoricalTimeSeries) element.getValue();
      if (hts == MISS) {
        hts = null;
      }
    } else {
      // If we have the full series cached computing a sub-series could be faster
      Element fullHtsElement = _dataCache.get(uniqueId);
      if (fullHtsElement != null) {
        hts = getSubSeries((HistoricalTimeSeries) fullHtsElement.getValue(), start, includeStart, end, includeEnd, maxPoints);
      } else {
        if (maxPoints == null) {
          hts = _underlying.getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
        } else {
          hts = _underlying.getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd, maxPoints);
        }
        if (hts != null) {
          s_logger.debug("Caching sub time-series {}", hts);
          _dataCache.put(new Element(key, hts));
        } else {
          s_logger.debug("Caching miss {}", key);
          _dataCache.put(new Element(key, MISS));
        }
      }
    }
    return hts;
  }

  //-------------------------------------------------------------------------
  
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String dataField) {
    return getHistoricalTimeSeries(identifiers, LocalDate.now(getClock()), dataSource, dataProvider, dataField);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    HistoricalTimeSeriesKey key = new HistoricalTimeSeriesKey(null, identifierValidityDate, identifiers, dataSource, dataProvider, dataField);
    HistoricalTimeSeries hts = getFromDataCache(key);
    if (hts != null) {
      if (hts == MISS) {
        hts = null;
      }
    } else {
      hts = _underlying.getHistoricalTimeSeries(identifiers, identifierValidityDate, dataSource, dataProvider, dataField);
      if (hts != null) {
        s_logger.debug("Caching time-series {}", hts);
        _dataCache.put(new Element(key, hts));
        _dataCache.put(new Element(hts.getUniqueId(), hts));
      } else {
        s_logger.debug("Caching miss on {}", key);
        _dataCache.put(new Element(key, MISS));
      }
    }
    return hts;
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String dataField, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    return getHistoricalTimeSeries(
        identifiers, LocalDate.now(getClock()), dataSource, dataProvider, dataField,
        start, includeStart, end, includeEnd);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    return doGetHistoricalTimeSeries(identifiers, currentDate, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd, int maxPoints) {
    return getHistoricalTimeSeries(identifierBundle, LocalDate.now(getClock()), dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, maxPoints);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    return doGetHistoricalTimeSeries(identifiers, currentDate, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, maxPoints);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    HistoricalTimeSeries hts = doGetHistoricalTimeSeries(identifiers, currentDate, dataSource, dataProvider, dataField, null, true, null, true, -1);
    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    HistoricalTimeSeries hts = doGetHistoricalTimeSeries(identifiers, currentDate, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, -1);
    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    return getLatestDataPoint(identifierBundle, LocalDate.now(getClock()), dataSource, dataProvider, dataField);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    return getLatestDataPoint(identifierBundle, LocalDate.now(getClock()), dataSource, dataProvider, dataField,
        start, includeStart, end, includeEnd);
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(
      ExternalIdBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    HistoricalTimeSeriesKey seriesKey = new HistoricalTimeSeriesKey(null, currentDate, identifiers, dataSource, dataProvider, dataField);
    SubSeriesKey subseriesKey = new SubSeriesKey(start, includeStart, end, includeEnd, maxPoints);
    ObjectsPair<HistoricalTimeSeriesKey, SubSeriesKey> key = Pair.of(seriesKey, subseriesKey);
    Element element = _dataCache.get(key);
    HistoricalTimeSeries hts;
    if (element != null) {
      hts = (HistoricalTimeSeries) element.getValue();
      if (hts == MISS) {
        hts = null;
      }
    } else {
      // If we have the full series cached computing a sub-series could be faster
      Element fullHtsElement = _dataCache.get(seriesKey);
      if (fullHtsElement != null) {
        hts = getSubSeries((HistoricalTimeSeries) fullHtsElement.getValue(), start, includeStart, end, includeEnd, maxPoints);
      } else {
        if (maxPoints == null) {
          hts = _underlying.getHistoricalTimeSeries(identifiers, currentDate, dataSource, dataProvider, dataField, 
              start, includeStart, end, includeEnd);
        } else {
          hts = _underlying.getHistoricalTimeSeries(identifiers, currentDate, dataSource, dataProvider, dataField, 
              start, includeStart, end, includeEnd, maxPoints);
        }
        if (hts != null) {
          s_logger.debug("Caching sub time-series {}", hts);
          _dataCache.put(new Element(key, hts));
        } else {
          s_logger.debug("Caching miss {}", key);
          _dataCache.put(new Element(key, MISS));
        }
      }
    }
    return hts;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    return getHistoricalTimeSeries(dataField, identifierBundle, LocalDate.now(getClock()), resolutionKey);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notEmpty(identifierBundle, "identifierBundle");
    HistoricalTimeSeriesKey key = new HistoricalTimeSeriesKey(resolutionKey, identifierValidityDate, identifierBundle, null, null, dataField);
    HistoricalTimeSeries hts = getFromDataCache(key);
    if (hts != null) {
      if (hts == MISS) {
        hts = null;
      }
    } else {
      hts = _underlying.getHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey);
      if (hts != null) {
        s_logger.debug("Caching time-series {}", hts);
        _dataCache.put(new Element(key, hts));
        _dataCache.put(new Element(hts.getUniqueId(), hts));
      } else {
        s_logger.debug("Caching miss on {}", key);
        _dataCache.put(new Element(key, MISS));
      }
    }
    return hts;
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    return doGetHistoricalTimeSeries(
        dataField, identifierBundle, LocalDate.now(getClock()), resolutionKey, start, includeStart, end, includeEnd, null);
  }
  
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    return doGetHistoricalTimeSeries(
        dataField, identifierBundle, LocalDate.now(getClock()), resolutionKey, start, includeStart, end, includeEnd, maxPoints);
  }
  
  /*
   * PLAT-1589
   */
  private final class SubSeriesKey {
    private final LocalDate _start;
    private final boolean _includeStart;
    private final LocalDate _end;
    private final boolean _includeEnd;
    private final Integer _maxPoints;
    
    public SubSeriesKey(LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
      super();
      this._start = start;
      this._includeStart = includeStart;
      this._end = end;
      this._includeEnd = includeEnd;
      this._maxPoints = maxPoints;
    }
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ObjectUtils.hashCode(_end);
      result = prime * result + (_includeEnd ? 1231 : 1237);
      result = prime * result + (_includeStart ? 1231 : 1237);
      result = prime * result + ObjectUtils.hashCode(_start);
      result = prime * result + ObjectUtils.hashCode(_maxPoints);
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      SubSeriesKey other = (SubSeriesKey) obj;
      if (_includeEnd != other._includeEnd) {
        return false;
      }
      if (_includeStart != other._includeStart) {
        return false;
      }
      if (!ObjectUtils.equals(_end, other._end)) {
        return false;
      }
      if (!ObjectUtils.equals(_start, other._start)) {
        return false;
      }
      if (!ObjectUtils.equals(_maxPoints, other._maxPoints)) {
        return false;
      }      
      return true;
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle,
      LocalDate identifierValidityDate, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    return doGetHistoricalTimeSeries(
        dataField, identifierBundle, identifierValidityDate, resolutionKey, start, includeStart, end, includeEnd, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle,
      LocalDate identifierValidityDate, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    return doGetHistoricalTimeSeries(
        dataField, identifierBundle, identifierValidityDate, resolutionKey, start, includeStart, end, includeEnd, maxPoints);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    return getLatestDataPoint(dataField, identifierBundle, LocalDate.now(getClock()), resolutionKey);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    return getLatestDataPoint(dataField, identifierBundle, LocalDate.now(getClock()), resolutionKey,
        start, includeStart, end, includeEnd);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    return getLatestDataPoint(dataField, identifierBundle, identifierValidityDate, resolutionKey,
        (LocalDate) null, true, (LocalDate) null, true);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(
        dataField, identifierBundle, identifierValidityDate, resolutionKey, 
        start, includeStart, end, includeEnd, -1);
    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle,
        LocalDate identifierValidityDate, String resolutionKey,
        LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    HistoricalTimeSeriesKey seriesKey = new HistoricalTimeSeriesKey(resolutionKey, identifierValidityDate, identifierBundle, null, null, dataField);
    SubSeriesKey subseriesKey = new SubSeriesKey(start, includeStart, end, includeEnd, maxPoints);
    ObjectsPair<HistoricalTimeSeriesKey, SubSeriesKey> key = Pair.of(seriesKey, subseriesKey);
    Element element = _dataCache.get(key);
    HistoricalTimeSeries hts;
    if (element != null) {
      hts = (HistoricalTimeSeries) element.getValue();
      if (hts == MISS) {
        hts = null;
      }
    } else {
      // If we have the full series cached computing a sub-series could be faster
      Element fullHtsElement = _dataCache.get(seriesKey);
      if (fullHtsElement != null) {
        hts = getSubSeries((HistoricalTimeSeries) fullHtsElement.getValue(), start, includeStart, end, includeEnd, maxPoints);
      } else {
        if (maxPoints == null) {
          hts = _underlying.getHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey, start, includeStart, end, includeEnd);
        } else {
          hts = _underlying.getHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey, start, includeStart, end, includeEnd, maxPoints);          
        }
        if (hts != null) {
          s_logger.debug("Caching sub time-series {}", hts);
          _dataCache.put(new Element(key, hts));
          _dataCache.put(new Element(new ObjectsPair<UniqueId, SubSeriesKey>(hts.getUniqueId(), subseriesKey), hts)); 
        } else {
          s_logger.debug("Caching miss {}", key);
          _dataCache.put(new Element(key, MISS));
        }
      }      
    }
    return hts;
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
      Set<ExternalIdBundle> identifierSet, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    ArgumentChecker.notNull(identifierSet, "identifierSet");
    Map<ExternalIdBundle, HistoricalTimeSeries> result = new HashMap<ExternalIdBundle, HistoricalTimeSeries>();
    Set<ExternalIdBundle> remainingIds = new HashSet<ExternalIdBundle>();
    // caching works individually but all misses can be passed to underlying as one request
    for (ExternalIdBundle identifiers : identifierSet) {
      HistoricalTimeSeriesKey key = new HistoricalTimeSeriesKey(null, null, identifiers, dataSource, dataProvider, dataField);
      HistoricalTimeSeries hts = getFromDataCache(key);
      if (hts != null) {
        if (hts != MISS) {
          hts = getSubSeries(hts, start, includeStart, end, includeEnd, null);
          result.put(identifiers, hts);
        } else {
          result.put(identifiers, null);
        }
      } else {
        remainingIds.add(identifiers);
      }
    }
    if (remainingIds.size() > 0) {
      Map<ExternalIdBundle, HistoricalTimeSeries> remainingTsResults =
        _underlying.getHistoricalTimeSeries(remainingIds, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd);
      for (Map.Entry<ExternalIdBundle, HistoricalTimeSeries> tsResult : remainingTsResults.entrySet()) {
        ExternalIdBundle identifiers = tsResult.getKey();
        HistoricalTimeSeries hts = tsResult.getValue();
        HistoricalTimeSeriesKey key = new HistoricalTimeSeriesKey(null, null, identifiers, dataSource, dataProvider, dataField);
        if (hts != null) {
          s_logger.debug("Caching time-series {}", hts);
          _dataCache.put(new Element(key, hts));
          _dataCache.put(new Element(hts.getUniqueId(), hts));
          hts = getSubSeries(hts, start, includeStart, end, includeEnd, null);
        } else {
          s_logger.debug("Caching miss {}", key);
          _dataCache.put(new Element(key, MISS));
        }
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
  private HistoricalTimeSeries getFromDataCache(HistoricalTimeSeriesKey key) {
    Element element = _dataCache.get(key);
    if (element == null) {
      s_logger.debug("Cache miss on {}", key);
      return null;
    }
    s_logger.debug("Cache hit on {}", key);
    return (HistoricalTimeSeries) element.getValue();
  }

  /**
   * Attempts to retrieve the time-series with the given unique identifier from the cache.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the time-series, null if no match
   */
  private HistoricalTimeSeries getFromDataCache(UniqueId uniqueId) {
    Element element = _dataCache.get(uniqueId);
    if (element == null) {
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
   * @param includeStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @return the historical time-series, null if null input
   */
  private HistoricalTimeSeries getSubSeries(
      HistoricalTimeSeries hts, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, Integer maxPoints) {
    
    if (hts == null) {
      return null;
    }
    LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) hts.getTimeSeries();
    if (timeSeries == null || timeSeries.isEmpty()) { 
      return hts;
    }
    if (start == null || start.isBefore(timeSeries.getEarliestTime())) {
      start = timeSeries.getEarliestTime();
    }
    if (end == null || end.isAfter(timeSeries.getLatestTime())) {
      end = timeSeries.getLatestTime();
    }
    if (start.isAfter(timeSeries.getLatestTime()) || end.isBefore(timeSeries.getEarliestTime())) {
      return new SimpleHistoricalTimeSeries(hts.getUniqueId(), new ListLocalDateDoubleTimeSeries());
    }
    timeSeries = timeSeries.subSeries(start, includeStart, end, includeEnd);
    if (((maxPoints != null) && (Math.abs(maxPoints) < timeSeries.size()))) {
      timeSeries = maxPoints >= 0 ? timeSeries.head(maxPoints) : timeSeries.tail(-maxPoints);
    }
    return new SimpleHistoricalTimeSeries(hts.getUniqueId(), timeSeries);
  }

}
