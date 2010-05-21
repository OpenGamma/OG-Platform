/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.historicaldata;

import java.io.Serializable;

import javax.time.calendar.LocalDate;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class EHCachingHistoricalDataProvider implements HistoricalDataProvider {
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingHistoricalDataProvider.class);
  
  private static final boolean INCLUDE_LAST_DAY = true;
  private static final String CACHE_NAME = "HistoricalDataCache";
  private final HistoricalDataProvider _underlying;
  private final CacheManager _manager;
  private final Cache _cache;
  
  
  public EHCachingHistoricalDataProvider(HistoricalDataProvider underlying) {
    ArgumentChecker.notNull(underlying, "Underlying Historical Data Provider");
    _underlying = underlying;
    CacheManager manager = createCacheManager();
    addCache(manager, CACHE_NAME);
    _cache = getCacheFromManager(manager, CACHE_NAME);
    _manager = manager;
  }
  
  public EHCachingHistoricalDataProvider(HistoricalDataProvider underlying, int maxElementsInMemory,
                                         MemoryStoreEvictionPolicy memoryStoreEvictionPolicy, boolean overflowToDisk,
                                         String diskStorePath, boolean eternal, long timeToLiveSeconds, long timeToIdleSeconds,
                                         boolean diskPersistent, long diskExpiryThreadIntervalSeconds, 
                                         RegisteredEventListeners registeredEventListeners) {
    ArgumentChecker.notNull(underlying, "Underlying Historical Data Provider");
    _underlying = underlying;
    CacheManager manager = createCacheManager();
    addCache(manager, CACHE_NAME, maxElementsInMemory, memoryStoreEvictionPolicy, overflowToDisk, diskStorePath, eternal,
             timeToLiveSeconds, timeToIdleSeconds, diskPersistent, diskExpiryThreadIntervalSeconds, registeredEventListeners);
    _cache = getCacheFromManager(manager, CACHE_NAME);
    _manager = manager;
  }
  
  public EHCachingHistoricalDataProvider(HistoricalDataProvider underlying, CacheManager manager) {
    ArgumentChecker.notNull(underlying, "Underlying Historical Data Provider");
    ArgumentChecker.notNull(manager, "Cache Manager");
    _underlying = underlying;
    addCache(manager, CACHE_NAME);
    _cache = getCacheFromManager(manager, CACHE_NAME);
    _manager = manager;
  }
  
  public EHCachingHistoricalDataProvider(HistoricalDataProvider underlying, Cache cache) {
    ArgumentChecker.notNull(underlying, "Underlying Historical Data Provider");
    ArgumentChecker.notNull(cache, "Cache");
    _underlying = underlying;
    CacheManager manager = createCacheManager();
    addCache(manager, cache);
    _cache = getCacheFromManager(manager, cache.getName());
    _manager = manager;
  }
  

  protected CacheManager createCacheManager() {
    CacheManager manager = null;
    try {
      manager = CacheManager.create();
    } catch (CacheException e) {
      throw new OpenGammaRuntimeException("Unable to create CacheManager", e);
    }
    return manager;
  }

  protected void addCache(CacheManager manager, Cache cache) {
    ArgumentChecker.notNull(manager, "CacheManager");
    ArgumentChecker.notNull(cache, "Cache");
    if (!manager.cacheExists(cache.getName())) {
      try {
        manager.addCache(cache);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Unable to add cache " + cache.getName(), e);
      }
    }

  }

  protected void addCache(CacheManager manager, String name,
      int maxElementsInMemory,
      MemoryStoreEvictionPolicy memoryStoreEvictionPolicy,
      boolean overflowToDisk, String diskStorePath, boolean eternal,
      long timeToLiveSeconds, long timeToIdleSeconds, boolean diskPersistent,
      long diskExpiryThreadIntervalSeconds,
      RegisteredEventListeners registeredEventListeners) {
    ArgumentChecker.notNull(manager, "CacheManager");
    ArgumentChecker.notNull(name, "CacheName");
    if (!manager.cacheExists(name)) {
      try {
        manager.addCache(new Cache(name, maxElementsInMemory,
            memoryStoreEvictionPolicy, overflowToDisk, diskStorePath, eternal,
            timeToLiveSeconds, timeToIdleSeconds, diskPersistent,
            diskExpiryThreadIntervalSeconds, registeredEventListeners));
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Unable to create cache " + name, e);
      }
    }
  }

  protected void addCache(final CacheManager manager, final String name) {
    if (!manager.cacheExists(name)) {
      try {
        manager.addCache(name);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Unable to create cache " + name, e);
      }
    }
  }

  protected Cache getCacheFromManager(CacheManager manager, String name) {
    Cache cache = null;
    try {
      cache = manager.getCache(name);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException(
          "Unable to retrieve from CacheManager, cache: " + name, e);
    }
    return cache;
  }
  

  /**
   * @return the underlying
   */
  public HistoricalDataProvider getUnderlying() {
    return _underlying;
  }

  /**
   * @return the CacheManager
   */
  public CacheManager getCacheManager() {
    return _manager;
  }
  
  @Override
  public LocalDateDoubleTimeSeries getHistoricalTimeSeries(
      IdentifierBundle dsids, String dataSource, String dataProvider,
      String field) {
    CacheKey key = new CacheKey(dsids, dataSource, dataProvider, field);
    Element element = _cache.get(key);
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
      LocalDateDoubleTimeSeries ts = _underlying.getHistoricalTimeSeries(dsids, dataSource, dataProvider, field);
      _cache.put(new Element(key, ts));
      return ts;
    }
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalTimeSeries(
      IdentifierBundle dsids, String dataSource, String dataProvider,
      String field, LocalDate start, LocalDate end) {
    LocalDateDoubleTimeSeries ts = getHistoricalTimeSeries(dsids, dataSource, dataProvider, field);
    if (ts != null) {
      return (LocalDateDoubleTimeSeries) ts.subSeries(start, true, end, INCLUDE_LAST_DAY);
    } else {
      return null;  
    }
  }

  private class CacheKey implements Serializable {
    private final IdentifierBundle _dsids;
    private final String _dataSource;
    private final String _dataProvider;
    private final String _field;
    
    public CacheKey(IdentifierBundle dsids, String dataSource, String dataProvider, String field) {
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
      if (!(obj instanceof CacheKey)) {
        return false;
      }
      CacheKey other = (CacheKey) obj;
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
}
