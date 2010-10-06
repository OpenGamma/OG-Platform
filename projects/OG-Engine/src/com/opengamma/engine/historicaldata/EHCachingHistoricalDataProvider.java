/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.historicaldata;

import java.io.Serializable;

import javax.time.calendar.LocalDate;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class EHCachingHistoricalDataProvider implements HistoricalDataSource {
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingHistoricalDataProvider.class);

  private static final boolean INCLUDE_LAST_DAY = true;
  private static final String CACHE_NAME = "HistoricalDataCache";
  private final HistoricalDataSource _underlying;
  private final CacheManager _manager;
  private final Cache _cache;

  public EHCachingHistoricalDataProvider(HistoricalDataSource underlying, CacheManager cacheManager, int maxElementsInMemory, MemoryStoreEvictionPolicy memoryStoreEvictionPolicy,
      boolean overflowToDisk, String diskStorePath, boolean eternal, long timeToLiveSeconds, long timeToIdleSeconds, boolean diskPersistent, long diskExpiryThreadIntervalSeconds,
      RegisteredEventListeners registeredEventListeners) {
    ArgumentChecker.notNull(underlying, "Underlying Historical Data Provider");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    _manager = cacheManager;
    EHCacheUtils.addCache(_manager, CACHE_NAME, maxElementsInMemory, memoryStoreEvictionPolicy, overflowToDisk, diskStorePath, eternal, timeToLiveSeconds, timeToIdleSeconds, diskPersistent,
        diskExpiryThreadIntervalSeconds, registeredEventListeners);
    _cache = EHCacheUtils.getCacheFromManager(_manager, CACHE_NAME);
  }

  public EHCachingHistoricalDataProvider(HistoricalDataSource underlying, CacheManager manager) {
    ArgumentChecker.notNull(underlying, "Underlying Historical Data Provider");
    ArgumentChecker.notNull(manager, "Cache Manager");
    _underlying = underlying;
    EHCacheUtils.addCache(manager, CACHE_NAME);
    _cache = EHCacheUtils.getCacheFromManager(manager, CACHE_NAME);
    _manager = manager;
  }

  /**
   * @return the underlying
   */
  public HistoricalDataSource getUnderlying() {
    return _underlying;
  }

  /**
   * @return the CacheManager
   */
  public CacheManager getCacheManager() {
    return _manager;
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String dataSource, String dataProvider, String field) {
    MetaDataKey key = new MetaDataKey(identifiers, dataSource, dataProvider, field);
    Element element = _cache.get(key);
    if (element != null) {
      Serializable value = element.getValue();
      if (value instanceof UniqueIdentifier) {
        UniqueIdentifier uid = (UniqueIdentifier) value;
        s_logger.debug("retrieved UID: {} from cache", uid);
        LocalDateDoubleTimeSeries timeSeries = getHistoricalData(uid);
        return new ObjectsPair<UniqueIdentifier, LocalDateDoubleTimeSeries>(uid, timeSeries);
      } else {
        s_logger.warn("returned object {} from cache, not a UniqueIdentifier", value);
        return null;
      }
    } else {
      Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = _underlying.getHistoricalData(identifiers, dataSource, dataProvider, field);
      _cache.put(new Element(key, tsPair.getFirst()));
      _cache.put(new Element(tsPair.getFirst(), tsPair.getSecond()));
      return tsPair;
    }
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle dsids, String dataSource, String dataProvider, String field, LocalDate start, LocalDate end) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getHistoricalData(dsids, dataSource, dataProvider, field);
    if (tsPair != null) {
      LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) tsPair.getSecond().subSeries(start, true, end, INCLUDE_LAST_DAY);
      return Pair.of(tsPair.getKey(), timeSeries);
    } else {
      return null;
    }
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
        return null;
      }
    } else {
      LocalDateDoubleTimeSeries ts = _underlying.getHistoricalData(uid);
      _cache.put(new Element(uid, ts));
      return ts;
    }
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid, LocalDate start, LocalDate end) {
    LocalDateDoubleTimeSeries ts = getHistoricalData(uid);
    if (ts != null) {
      return (LocalDateDoubleTimeSeries) ts.subSeries(start, true, end, INCLUDE_LAST_DAY);
    } else {
      return null;
    }
  }

  private static class MetaDataKey implements Serializable {
    private final IdentifierBundle _dsids;
    private final String _dataSource;
    private final String _dataProvider;
    private final String _field;

    public MetaDataKey(IdentifierBundle dsids, String dataSource, String dataProvider, String field) {
      _dsids = dsids;
      _dataSource = dataSource;
      _dataProvider = dataProvider;
      _field = field;
    }

    @Override
    public int hashCode() {
      return _dsids.hashCode() ^ _field.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof MetaDataKey)) {
        return false;
      }
      MetaDataKey other = (MetaDataKey) obj;
      if (_field == null) {
        if (other._field != null) {
          return false;
        }
      } else if (!_field.equals(other._field)) {
        return false;
      }
      if (_dsids == null) {
        if (other._dsids != null) {
          return false;
        }
      } else if (!_dsids.equals(other._dsids)) {
        return false;
      }
      if (_dataProvider == null) {
        if (other._dataProvider != null) {
          return false;
        }
      } else if (!_dataProvider.equals(other._dataProvider)) {
        return false;
      }
      if (_dataSource == null) {
        if (other._dataSource != null) {
          return false;
        }
      } else if (!_dataSource.equals(other._dataSource)) {
        return false;
      }
      return true;
    }
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers) {
    return getHistoricalData(identifiers, null, null, null);
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate start, LocalDate end) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getHistoricalData(identifiers);
    if (tsPair != null) {
      LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) tsPair.getSecond().subSeries(start, true, end, INCLUDE_LAST_DAY);
      return Pair.of(tsPair.getKey(), timeSeries);
    } else {
      return null;
    }
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getHistoricalData(identifiers);
    if (tsPair != null && tsPair.getValue() != null) {
      LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) tsPair.getSecond().subSeries(start, inclusiveStart, end, !exclusiveEnd);
      return Pair.of(tsPair.getKey(), timeSeries);
    } else {
      return null;
    }
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String dataSource, String dataProvider, String field, LocalDate start,
      boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getHistoricalData(identifiers, dataSource, dataProvider, field);
    if (tsPair != null && tsPair.getValue() != null) {
      LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) tsPair.getSecond().subSeries(start, inclusiveStart, end, !exclusiveEnd);
      return Pair.of(tsPair.getKey(), timeSeries);
    } else {
      return null;
    }
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    LocalDateDoubleTimeSeries timeseries = getHistoricalData(uid);
    if (timeseries != null) {
      return (LocalDateDoubleTimeSeries) timeseries.subSeries(start, inclusiveStart, end, !exclusiveEnd);
    } else {
      return null;
    }
  }

}
