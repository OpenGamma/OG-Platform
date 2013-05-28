/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Maps;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.time.DateUtils;
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
   * The cache prefix.
   */
  /*package*/static final String CACHE_PREFIX = "HistoricalTimeSeries";

  /**
   * The cache name.
   */
  private static final String DATA_CACHE_NAME = CACHE_PREFIX + "DataCache";

  /**
   * Id bundle cache name.
   */
  private static final String ID_BUNDLE_CACHE_NAME = CACHE_PREFIX + "IdBundleCache";

  /**
   * Listens for changes in the underlying security source.
   */
  private ChangeListener _changeListener;
  /**
   * The local change manager.
   */
  private final ChangeManager _changeManager;

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

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

    @Override
    public boolean equals(final Object o) {
      return o instanceof MissHTS;
    }

    @Override
    public int hashCode() {
      return 0;
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
   * The identifier bundle cache
   */
  private final Cache _identifierBundleCache;
  /**
   * The clock.
   */
  private final Clock _clock = OpenGammaClock.getInstance();

  /**
   * Creates an instance.
   * 
   * @param underlying the underlying source, not null
   * @param cacheManager the cache manager, not null
   */
  public EHCachingHistoricalTimeSeriesSource(HistoricalTimeSeriesSource underlying, CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "Cache Manager");
    _underlying = underlying;
    EHCacheUtils.addCache(cacheManager, DATA_CACHE_NAME);
    _dataCache = EHCacheUtils.getCacheFromManager(cacheManager, DATA_CACHE_NAME);
    EHCacheUtils.addCache(cacheManager, ID_BUNDLE_CACHE_NAME);
    _identifierBundleCache = EHCacheUtils.getCacheFromManager(cacheManager, ID_BUNDLE_CACHE_NAME);

    _changeListener = createChangeListener();
    _underlying.changeManager().addChangeListener(_changeListener);
    _changeManager = new BasicChangeManager();
  }

  private ChangeListener createChangeListener() {
    return new ChangeListener() {

      @Override
      public void entityChanged(ChangeEvent event) {
        cleanCaches(event.getObjectId());
        changeManager().entityChanged(event.getType(), event.getObjectId(), event.getVersionFrom(), event.getVersionTo(), event.getVersionInstant());
      }

    };
  }

  private void cleanCaches(ObjectId oid) {
    // Only care where the unversioned ID has been cached since it now represents something else
    _dataCache.remove(oid);
    _identifierBundleCache.remove(oid);
    // Destroy all version/correction cached values for the object
    _dataCache.remove(oid);
    _identifierBundleCache.remove(oid);
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
   * Gets the id cache manager.
   * 
   * @return the id cache manager, not null
   */
  public CacheManager getIdentifierBundleCacheManager() {
    return _identifierBundleCache.getCacheManager();
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
      if (MISS.equals(hts)) {
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
    SubSeriesKey subseriesKey = new SubSeriesKey(start, end, maxPoints);
    ObjectsPair<UniqueId, SubSeriesKey> key = Pair.of(uniqueId, subseriesKey);
    Element element = _dataCache.get(key);
    HistoricalTimeSeries hts;
    if (element != null) {
      hts = (HistoricalTimeSeries) element.getObjectValue();
      if (MISS.equals(hts)) {
        hts = null;
      } else if (!subseriesKey.isMatch(start, includeStart, end, includeEnd, maxPoints)) {
        // Pick out the sub-series requested
        hts = getSubSeries(hts, start, includeStart, end, includeEnd, maxPoints);
      }
    } else {
      // If we have the full series cached computing a sub-series could be faster
      Element fullHtsElement = _dataCache.get(uniqueId);
      if (fullHtsElement != null) {
        hts = getSubSeries((HistoricalTimeSeries) fullHtsElement.getObjectValue(), start, includeStart, end, includeEnd, maxPoints);
      } else {
        if (maxPoints == null) {
          hts = _underlying.getHistoricalTimeSeries(uniqueId, subseriesKey.getStart(), true, subseriesKey.getEnd(), subseriesKey.getIncludeEnd());
        } else {
          hts = _underlying
              .getHistoricalTimeSeries(uniqueId, subseriesKey.getStart(), true, subseriesKey.getEnd(), subseriesKey.getIncludeEnd(), subseriesKey.getMaxPoints());
        }
        if (hts != null) {
          s_logger.debug("Caching sub time-series {}", hts);
          _dataCache.put(new Element(key, hts));
          if (!subseriesKey.isMatch(start, includeStart, end, includeEnd, maxPoints)) {
            // Pick out the sub-series requested
            hts = getSubSeries(hts, start, includeStart, end, includeEnd, maxPoints);
          }
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
      if (MISS.equals(hts)) {
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
    SubSeriesKey subseriesKey = new SubSeriesKey(start, end, maxPoints);
    ObjectsPair<HistoricalTimeSeriesKey, SubSeriesKey> key = Pair.of(seriesKey, subseriesKey);
    Element element = _dataCache.get(key);
    HistoricalTimeSeries hts;
    if (element != null) {
      hts = (HistoricalTimeSeries) element.getObjectValue();
      if (MISS.equals(hts)) {
        hts = null;
      } else if (!subseriesKey.isMatch(start, includeStart, end, includeEnd, maxPoints)) {
        // Pick out the sub-series requested
        hts = getSubSeries(hts, start, includeStart, end, includeEnd, maxPoints);
      }
    } else {
      // If we have the full series cached computing a sub-series could be faster
      Element fullHtsElement = _dataCache.get(seriesKey);
      if (fullHtsElement != null) {
        hts = getSubSeries((HistoricalTimeSeries) fullHtsElement.getObjectValue(), start, includeStart, end, includeEnd, maxPoints);
      } else {
        if (maxPoints == null) {
          hts = _underlying.getHistoricalTimeSeries(identifiers, currentDate, dataSource, dataProvider, dataField,
              subseriesKey.getStart(), true, subseriesKey.getEnd(), subseriesKey.getIncludeEnd());
        } else {
          hts = _underlying.getHistoricalTimeSeries(identifiers, currentDate, dataSource, dataProvider, dataField,
              subseriesKey.getStart(), true, subseriesKey.getEnd(), subseriesKey.getIncludeEnd(), subseriesKey.getMaxPoints());
        }
        if (hts != null) {
          s_logger.debug("Caching sub time-series {}", hts);
          _dataCache.put(new Element(key, hts));
          if (!subseriesKey.isMatch(start, includeStart, end, includeEnd, maxPoints)) {
            // Pick out the sub-series requested
            hts = getSubSeries(hts, start, includeStart, end, includeEnd, maxPoints);
          }
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
      if (MISS.equals(hts)) {
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
  private static final class SubSeriesKey implements Serializable {
    private static final long serialVersionUID = 2L;
    private final LocalDate _start;
    private final LocalDate _end;
    private final Integer _maxPoints;

    public SubSeriesKey(LocalDate start, LocalDate end, Integer maxPoints) {
      super();
      this._start = (start != null) ? start.withDayOfMonth(1).withMonth(1) : null;
      this._end = (end != null) ? end.plusYears(1).withMonth(1).withDayOfMonth(1) : null;
      if (maxPoints != null) {
        int mp = maxPoints;
        if (mp < 0) {
          int amp = -mp;
          if (end != null) {
            amp += DateUtils.getDaysBetween(end, _end);
          }
          this._maxPoints = -(amp + 1024 - (amp & 1023));
        } else if (mp > 0) {
          if (start != null) {
            mp += DateUtils.getDaysBetween(_start, start);
          }
          this._maxPoints = mp + 1024 - (mp & 1023);
        } else {
          this._maxPoints = maxPoints;
        }
      } else {
        this._maxPoints = null;
      }
    }

    public LocalDate getStart() {
      return _start;
    }

    public LocalDate getEnd() {
      return _end;
    }

    public Integer getMaxPoints() {
      return _maxPoints;
    }

    public boolean getIncludeEnd() {
      return getEnd() == null;
    }

    /**
     * Tests whether this key exactly matches the user request, or if it would be a larger time-series that needs to be cut down to match.
     * 
     * @param true if an exact match, false if it needs trimming
     */
    public boolean isMatch(final LocalDate start, final boolean includeStart, final LocalDate end, final boolean includeEnd, final Integer maxPoints) {
      return ObjectUtils.equals(start, _start)
          && ObjectUtils.equals(end, _end)
          && includeStart
          && (includeEnd == getIncludeEnd())
          && ObjectUtils.equals(maxPoints, _maxPoints);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ObjectUtils.hashCode(_end);
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
    SubSeriesKey subseriesKey = new SubSeriesKey(start, end, maxPoints);
    ObjectsPair<HistoricalTimeSeriesKey, SubSeriesKey> key = Pair.of(seriesKey, subseriesKey);
    Element element = _dataCache.get(key);
    HistoricalTimeSeries hts;
    if (element != null) {
      hts = (HistoricalTimeSeries) element.getObjectValue();
      if (MISS.equals(hts)) {
        hts = null;
      } else if (!subseriesKey.isMatch(start, includeStart, end, includeEnd, maxPoints)) {
        // Pick out the sub-series requested
        hts = getSubSeries(hts, start, includeStart, end, includeEnd, maxPoints);
      }
    } else {
      // If we have the full series cached computing a sub-series could be faster
      Element fullHtsElement = _dataCache.get(seriesKey);
      if (fullHtsElement != null) {
        hts = getSubSeries((HistoricalTimeSeries) fullHtsElement.getObjectValue(), start, includeStart, end, includeEnd, maxPoints);
      } else {
        if (maxPoints == null) {
          hts = _underlying.getHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey, subseriesKey.getStart(), true, subseriesKey.getEnd(),
              subseriesKey.getIncludeEnd());
        } else {
          hts = _underlying.getHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey, subseriesKey.getStart(), true, subseriesKey.getEnd(),
              subseriesKey.getIncludeEnd(), subseriesKey.getMaxPoints());
        }
        if (hts != null) {
          s_logger.debug("Caching sub time-series {}", hts);
          _dataCache.put(new Element(key, hts));
          _dataCache.put(new Element(new ObjectsPair<UniqueId, SubSeriesKey>(hts.getUniqueId(), subseriesKey), hts));
          if (!subseriesKey.isMatch(start, includeStart, end, includeEnd, maxPoints)) {
            // Pick out the sub-series requested
            hts = getSubSeries(hts, start, includeStart, end, includeEnd, maxPoints);
          }
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
    Map<ExternalIdBundle, HistoricalTimeSeries> result = Maps.newHashMap();
    Set<ExternalIdBundle> remainingIds = new HashSet<ExternalIdBundle>();
    // caching works individually but all misses can be passed to underlying as one request
    for (ExternalIdBundle identifiers : identifierSet) {
      HistoricalTimeSeriesKey key = new HistoricalTimeSeriesKey(null, null, identifiers, dataSource, dataProvider, dataField);
      HistoricalTimeSeries hts = getFromDataCache(key);
      if (hts != null) {
        if (!MISS.equals(hts)) {
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
   * @param key the key, not null
   * @return the time-series, null if no match
   */
  private HistoricalTimeSeries getFromDataCache(HistoricalTimeSeriesKey key) {
    Element element = _dataCache.get(key);
    if (element == null) {
      s_logger.debug("Cache miss on {}", key);
      return null;
    }
    s_logger.debug("Cache hit on {}", key);
    return (HistoricalTimeSeries) element.getObjectValue();
  }

  /**
   * Attempts to retrieve the time-series with the given unique identifier from the cache.
   * 
   * @param uniqueId the unique identifier, not null
   * @return the time-series, null if no match
   */
  private HistoricalTimeSeries getFromDataCache(UniqueId uniqueId) {
    Element element = _dataCache.get(uniqueId);
    if (element == null) {
      s_logger.debug("Cache miss on {}", uniqueId);
      return null;
    }
    s_logger.debug("Cache hit on {}", uniqueId);
    return (HistoricalTimeSeries) element.getObjectValue();
  }

  /**
   * Gets a sub-series based on the supplied dates.
   * 
   * @param hts the time-series, null returns null
   * @param start the start date, null will load the earliest date
   * @param includeStart whether or not the start date is included in the result
   * @param end the end date, null will load the latest date
   * @param includeEnd whether or not the end date is included in the result
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
    if (start == null) {
      start = timeSeries.getEarliestTime();
    } else {
      if (!includeStart) {
        start = start.plusDays(1);
      }
      if (start.isBefore(timeSeries.getEarliestTime())) {
        start = timeSeries.getEarliestTime();
      }
    }
    if (end == null) {
      end = timeSeries.getLatestTime();
    } else {
      if (!includeEnd) {
        end = end.minusDays(1);
      }
      if (end.isAfter(timeSeries.getLatestTime())) {
        end = timeSeries.getLatestTime();
      }
    }
    if (start.isAfter(timeSeries.getLatestTime()) || end.isBefore(timeSeries.getEarliestTime())) {
      return new SimpleHistoricalTimeSeries(hts.getUniqueId(), ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
    }
    timeSeries = timeSeries.subSeries(start, true, end, true);
    if (((maxPoints != null) && (Math.abs(maxPoints) < timeSeries.size()))) {
      timeSeries = maxPoints >= 0 ? timeSeries.head(maxPoints) : timeSeries.tail(-maxPoints);
    }
    return new SimpleHistoricalTimeSeries(hts.getUniqueId(), timeSeries);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUnderlying() + "]";
  }

  @Override
  public ExternalIdBundle getExternalIdBundle(UniqueId uniqueId) {
    Element idBundleCacheElement = _identifierBundleCache.get(uniqueId);
    if (idBundleCacheElement == null) {
      ExternalIdBundle idBundle = _underlying.getExternalIdBundle(uniqueId);
      _identifierBundleCache.put(new Element(uniqueId, idBundle));
      return idBundle;
    } else {
      return (ExternalIdBundle) idBundleCacheElement.getObjectValue();
    }
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle method.
   */
  protected void shutdown() {
    _dataCache.getCacheManager().removeCache(DATA_CACHE_NAME);
    _identifierBundleCache.getCacheManager().removeCache(ID_BUNDLE_CACHE_NAME);
  }

}
