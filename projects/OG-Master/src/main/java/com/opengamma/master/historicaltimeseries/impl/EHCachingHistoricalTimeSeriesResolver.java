/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.text.MessageFormat;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolverWithBasicChangeManager;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.tuple.Triple;

/**
 * A <code>HistoricalTimeSeriesResolver</code> that tries to find the distribution spec in a cache. If it doesn't find it, it will delegate to an underlying <code>HistoricalTimeSeriesResolver</code>.
 */
public class EHCachingHistoricalTimeSeriesResolver extends HistoricalTimeSeriesResolverWithBasicChangeManager {

  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingHistoricalTimeSeriesResolver.class);

  private final class ThreadLocalWorker {

    private final Queue<ThreadLocalWorker> _waiting = new ConcurrentLinkedQueue<ThreadLocalWorker>();
    private ExternalIdBundle _identifierBundle;
    private LocalDate _identifierValidityDate;
    private String _dataSource;
    private String _dataProvider;
    private String _dataField;
    private String _resolutionKey;

    private boolean _haveResult;
    private HistoricalTimeSeriesResolutionResult _result;
    private RuntimeException _error;

    public void init(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate, final String dataSource, final String dataProvider, final String dataField,
        final String resolutionKey) {
      _identifierBundle = identifierBundle;
      _identifierValidityDate = identifierValidityDate;
      _dataSource = dataSource;
      _dataProvider = dataProvider;
      _dataField = dataField;
      _resolutionKey = resolutionKey;
    }

    // Caller must hold the monitor
    public void setResult(final HistoricalTimeSeriesResolutionResult result) {
      ThreadLocalWorker waiting = _waiting.poll();
      if (waiting != null) {
        do {
          waiting._haveResult = true;
          waiting._result = result;
          waiting = _waiting.poll();
        } while (waiting != null);
        notifyAll();
      }
    }

    // Caller must hold the monitor
    public void setError(final RuntimeException error) {
      ThreadLocalWorker waiting = _waiting.poll();
      if (waiting != null) {
        do {
          waiting._haveResult = true;
          waiting._error = error;
          waiting = _waiting.poll();
        } while (waiting != null);
        notifyAll();
      }
    }

    // Caller must hold the monitor on the delegate
    public HistoricalTimeSeriesResolutionResult getResult(ThreadLocalWorker delegate) {
      _haveResult = false;
      delegate._waiting.add(this);
      while (!_haveResult) {
        try {
          delegate.wait();
        } catch (InterruptedException e) {
          throw new OpenGammaRuntimeException("Interrupted", e);
        }
      }
      final RuntimeException e = _error;
      if (e != null) {
        _error = null;
        throw e;
      }
      HistoricalTimeSeriesResolutionResult result = _result;
      _result = null;
      return result;
    }

    @Override
    public boolean equals(final Object o) {
      ThreadLocalWorker other = (ThreadLocalWorker) o;
      return ObjectUtils.equals(_identifierBundle, other._identifierBundle)
          && ObjectUtils.equals(_identifierValidityDate, other._identifierValidityDate)
          && ObjectUtils.equals(_dataSource, other._dataSource)
          && ObjectUtils.equals(_dataProvider, other._dataProvider)
          && ObjectUtils.equals(_dataField, other._dataField)
          && ObjectUtils.equals(_resolutionKey, other._resolutionKey);
    }

    @Override
    public int hashCode() {
      int hc = ObjectUtils.hashCode(_identifierBundle);
      hc += (hc << 4) + ObjectUtils.hashCode(_identifierValidityDate);
      hc += (hc << 4) + ObjectUtils.hashCode(_dataSource);
      hc += (hc << 4) + ObjectUtils.hashCode(_dataProvider);
      hc += (hc << 4) + ObjectUtils.hashCode(_dataField);
      hc += (hc << 4) + ObjectUtils.hashCode(_resolutionKey);
      return hc;
    }

  }

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

  private final ThreadLocal<ThreadLocalWorker> _worker = new ThreadLocal<ThreadLocalWorker>() {
    @Override
    protected ThreadLocalWorker initialValue() {
      return new ThreadLocalWorker();
    }
  };

  private final ConcurrentMap<ThreadLocalWorker, ThreadLocalWorker> _workers = new ConcurrentHashMap<ThreadLocalWorker, ThreadLocalWorker>();

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
    underlying.changeManager().addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        _cache.removeAll();
      }
    });
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

  protected HistoricalTimeSeriesResolutionResult resolveImpl(final ThreadLocalWorker worker, final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate,
      final String dataSource, final String dataProvider, final String dataField, final String resolutionKey) {
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
    HistoricalTimeSeriesResolutionResult resolveResult;
    for (ExternalId id : identifierBundle) {
      String key = id.toString() + SEPARATOR +
          dataField + SEPARATOR +
          (dataSource != null ? dataSource : "") + SEPARATOR +
          (dataProvider != null ? dataProvider : "") + SEPARATOR +
          resolutionKey;
      e = _cache.get(key);
      HistoricalTimeSeriesResolutionCacheItem cacheItem = e != null ? (HistoricalTimeSeriesResolutionCacheItem) e.getObjectValue() : null;
      if (cacheItem != null) {
        boolean isInvalid = cacheItem.isInvalid(identifierValidityDate);
        resolveResult = !isInvalid ? cacheItem.get(identifierValidityDate) : null;
        if (isInvalid || resolveResult != null) {
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
        if (id.isValidOn(identifierValidityDate)) {
          String key = id.getExternalId().toString() + SEPARATOR +
              dataField + SEPARATOR +
              info.getDataSource() + SEPARATOR +
              info.getDataProvider() + SEPARATOR +
              resolutionKey;
          addResultToCache(key, id, resolveResult);

          key = id.getExternalId().toString() + SEPARATOR +
              dataField + SEPARATOR +
              SEPARATOR +
              info.getDataProvider() + SEPARATOR +
              resolutionKey;
          addResultToCache(key, id, resolveResult);

          key = id.getExternalId().toString() + SEPARATOR +
              dataField + SEPARATOR +
              info.getDataSource() + SEPARATOR +
              SEPARATOR +
              resolutionKey;
          addResultToCache(key, id, resolveResult);

          key = id.getExternalId().toString() + SEPARATOR +
              dataField + SEPARATOR +
              SEPARATOR +
              SEPARATOR +
              resolutionKey;
          addResultToCache(key, id, resolveResult);
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
            resolutionKey;
        addInvalidDateToCache(key, identifierValidityDate, id);
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

  private void addResultToCache(String key, ExternalIdWithDates externalIdWithDates, HistoricalTimeSeriesResolutionResult result) {
    Element cacheElement = _cache.get(key);
    if (cacheElement == null) {
      HistoricalTimeSeriesResolutionCacheItem cacheItem = new HistoricalTimeSeriesResolutionCacheItem(externalIdWithDates.getExternalId());
      cacheElement = new Element(key, cacheItem);
      Element existingCacheElement = _cache.putIfAbsent(cacheElement);
      if (existingCacheElement != null) {
        cacheElement = existingCacheElement;
      }
    }
    HistoricalTimeSeriesResolutionCacheItem cacheItem = (HistoricalTimeSeriesResolutionCacheItem) cacheElement.getObjectValue();
    cacheItem.put(externalIdWithDates, result);
  }

  private void addInvalidDateToCache(String key, LocalDate identifierValidityDate, ExternalId externalId) {
    Element cacheElement = _cache.get(key);
    if (cacheElement == null) {
      HistoricalTimeSeriesResolutionCacheItem cacheItem = new HistoricalTimeSeriesResolutionCacheItem(externalId);
      cacheElement = new Element(key, cacheItem);
      Element existingCacheElement = _cache.putIfAbsent(cacheElement);
      if (existingCacheElement != null) {
        cacheElement = existingCacheElement;
      }
    }
    HistoricalTimeSeriesResolutionCacheItem cacheItem = (HistoricalTimeSeriesResolutionCacheItem) cacheElement.getObjectValue();
    cacheItem.putInvalid(identifierValidityDate);
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle method.
   */
  protected void shutdown() {
    _cacheManager.removeCache(_cache.getName());
  }

  // HistoricalTimeSeriesResolver

  @Override
  public HistoricalTimeSeriesResolutionResult resolve(final ExternalIdBundle identifierBundle, final LocalDate identifierValidityDate,
      final String dataSource, final String dataProvider, final String dataField, final String resolutionKey) {
    final HistoricalTimeSeriesResolutionResult result;
    ThreadLocalWorker worker = _worker.get();
    worker.init(identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField, resolutionKey);
    do {
      final ThreadLocalWorker delegate = _workers.putIfAbsent(worker, worker);
      if (delegate == null) {
        break;
      } else {
        synchronized (delegate) {
          if (_workers.get(worker) == delegate) {
            return worker.getResult(delegate);
          }
        }
      }
    } while (true);
    try {
      result = resolveImpl(worker, identifierBundle, identifierValidityDate, dataSource, dataProvider, dataField, resolutionKey);
      synchronized (worker) {
        worker.setResult(result);
        _workers.remove(worker);
      }
    } catch (Throwable t) {
      RuntimeException e;
      if (t instanceof RuntimeException) {
        e = (RuntimeException) t;
      } else {
        e = new OpenGammaRuntimeException("Checked exception", t);
      }
      synchronized (worker) {
        worker.setError(e);
        _workers.remove(worker);
      }
      throw e;
    }
    return result;
  }
}
