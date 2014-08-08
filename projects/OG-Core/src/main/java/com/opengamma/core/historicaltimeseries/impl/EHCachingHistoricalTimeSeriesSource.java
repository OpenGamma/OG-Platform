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

import net.sf.ehcache.CacheManager;

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
import com.opengamma.util.function.Supplier;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A cache decorating a {@code HistoricalTimeSeriesSource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 */
public class EHCachingHistoricalTimeSeriesSource implements HistoricalTimeSeriesSource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(EHCachingHistoricalTimeSeriesSource.class);

  /** The cache prefix. */
  /*package*/static final String CACHE_PREFIX = "HistoricalTimeSeries";

  private final HierarhicalEHCache<Object, HistoricalTimeSeries> _cache;

  /** Id bundle cache name. */
  private static final String ID_BUNDLE_CACHE_NAME = CACHE_PREFIX + "IdBundleCache";

  /** Listens for changes in the underlying security source. */
  private ChangeListener _changeListener;
  /** The local change manager. */
  private final ChangeManager _changeManager;

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  /** The underlying source. */
  private final HistoricalTimeSeriesSource _underlying;
  /** The identifier bundle cache */
  private final HierarhicalEHCache<UniqueId, ExternalIdBundle> _identifierBundleCache;
  /** The clock. */
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

    _cache = new HierarhicalEHCache<Object, HistoricalTimeSeries>(cacheManager) {
      @Override
      String getCachePrefix() {
        return CACHE_PREFIX;
      }

      @Override
      Object extractKey(Object ignored, HistoricalTimeSeries value) {
        return value.getUniqueId().getObjectId();
      }
    };

    _identifierBundleCache = new HierarhicalEHCache<UniqueId, ExternalIdBundle>(cacheManager) {
      @Override
      String getCachePrefix() {
        return ID_BUNDLE_CACHE_NAME;
      }

      @Override
      Object extractKey(UniqueId key, ExternalIdBundle value) {
        return key.getObjectId();
      }
    };

    EHCacheUtils.addCache(cacheManager, ID_BUNDLE_CACHE_NAME);

    _changeListener = createChangeListener();
    _underlying.changeManager().addChangeListener(_changeListener);
    _changeManager = new BasicChangeManager();
  }

  private ChangeListener createChangeListener() {
    return new ChangeListener() {

      @Override
      public void entityChanged(ChangeEvent event) {
        cleanCaches(event.getObjectId());
        changeManager().entityChanged(event.getType(),
                                      event.getObjectId(),
                                      event.getVersionFrom(),
                                      event.getVersionTo(),
                                      event.getVersionInstant());
      }

    };
  }

  private void cleanCaches(ObjectId oid) {
    //_uidCache.remove(oid);
    _cache.clear(oid);
    _identifierBundleCache.clear(oid);
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
   * Gets the clock.
   *
   * @return the clock, not null
   */
  public Clock getClock() {
    return _clock;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    return _cache.getBySecondKey(uniqueId.getObjectId(), new Supplier<HistoricalTimeSeries>() {
      @Override
      public HistoricalTimeSeries get() {
        return _underlying.getHistoricalTimeSeries(uniqueId);
      }
    });
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
      return Pairs.of(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId,
                                                    LocalDate start,
                                                    boolean includeStart,
                                                    LocalDate end,
                                                    boolean includeEnd) {
    HistoricalTimeSeries hts = doGetHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd, -1);
    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return Pairs.of(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(
      final UniqueId uniqueId,
      LocalDate start,
      boolean includeStart,
      LocalDate end,
      boolean includeEnd,
      final Integer maxPoints) {
    final SubSeriesKey subseriesKey = new SubSeriesKey(start, end, maxPoints);
    ObjectsPair<UniqueId, SubSeriesKey> key = ObjectsPair.of(uniqueId, subseriesKey);

    Supplier<HistoricalTimeSeries> fetchHts = new Supplier<HistoricalTimeSeries>() {
      @Override
      public HistoricalTimeSeries get() {
        if (maxPoints == null) {
          return _underlying.getHistoricalTimeSeries(uniqueId,
                                                     subseriesKey.getStart(),
                                                     true,
                                                     subseriesKey.getEnd(),
                                                     subseriesKey.getIncludeEnd());
        } else {
          return _underlying.getHistoricalTimeSeries(uniqueId,
                                                     subseriesKey.getStart(),
                                                     true,
                                                     subseriesKey.getEnd(),
                                                     subseriesKey.getIncludeEnd(),
                                                     subseriesKey.getMaxPoints());
        }
      }
    };

    HistoricalTimeSeries hts = _cache.get(key, fetchHts);
    if (hts != null && !subseriesKey.isMatch(start, includeStart, end, includeEnd, maxPoints)) {
      hts = getSubSeries(hts, start, includeStart, end, includeEnd, maxPoints);
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
      final ExternalIdBundle identifiers,
      final LocalDate identifierValidityDate,
      final String dataSource,
      final String dataProvider,
      final String dataField) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    HistoricalTimeSeriesKey key = new HistoricalTimeSeriesKey(null,
                                                              identifierValidityDate,
                                                              identifiers,
                                                              dataSource,
                                                              dataProvider,
                                                              dataField);

    return _cache.get(key, new Supplier<HistoricalTimeSeries>() {
      @Override
      public HistoricalTimeSeries get() {
        return _underlying.getHistoricalTimeSeries(identifiers,
                                                   identifierValidityDate,
                                                   dataSource,
                                                   dataProvider,
                                                   dataField);
      }
    });
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
    return doGetHistoricalTimeSeries(identifiers,
                                     currentDate,
                                     dataSource,
                                     dataProvider,
                                     dataField,
                                     start,
                                     includeStart,
                                     end,
                                     includeEnd,
                                     null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifierBundle,
      String dataSource,
      String dataProvider,
      String dataField,
      LocalDate start,
      boolean includeStart,
      LocalDate end,
      boolean includeEnd,
      int maxPoints) {
    return getHistoricalTimeSeries(identifierBundle,
                                   LocalDate.now(getClock()),
                                   dataSource,
                                   dataProvider,
                                   dataField,
                                   start,
                                   includeStart,
                                   end,
                                   includeEnd,
                                   maxPoints);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    return doGetHistoricalTimeSeries(identifiers,
                                     currentDate,
                                     dataSource,
                                     dataProvider,
                                     dataField,
                                     start,
                                     includeStart,
                                     end,
                                     includeEnd,
                                     maxPoints);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    HistoricalTimeSeries hts = doGetHistoricalTimeSeries(identifiers,
                                                         currentDate,
                                                         dataSource,
                                                         dataProvider,
                                                         dataField,
                                                         null,
                                                         true,
                                                         null,
                                                         true,
                                                         -1);
    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return Pairs.of(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    HistoricalTimeSeries hts = doGetHistoricalTimeSeries(identifiers,
                                                         currentDate,
                                                         dataSource,
                                                         dataProvider,
                                                         dataField,
                                                         start,
                                                         includeStart,
                                                         end,
                                                         includeEnd,
                                                         -1);
    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return Pairs.of(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
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
      final ExternalIdBundle identifiers,
      final LocalDate currentDate,
      final String dataSource,
      final String dataProvider,
      final String dataField,
      LocalDate start,
      boolean includeStart,
      LocalDate end,
      boolean includeEnd,
      final Integer maxPoints) {
    HistoricalTimeSeriesKey seriesKey = new HistoricalTimeSeriesKey(null,
                                                                    currentDate,
                                                                    identifiers,
                                                                    dataSource,
                                                                    dataProvider,
                                                                    dataField);
    final SubSeriesKey subseriesKey = new SubSeriesKey(start, end, maxPoints);
    ObjectsPair<HistoricalTimeSeriesKey, SubSeriesKey> key = ObjectsPair.of(seriesKey, subseriesKey);

    Supplier<HistoricalTimeSeries> fetchHts = new Supplier<HistoricalTimeSeries>() {
      @Override
      public HistoricalTimeSeries get() {
        if (maxPoints == null) {
          return _underlying.getHistoricalTimeSeries(identifiers,
                                                     currentDate,
                                                     dataSource,
                                                     dataProvider,
                                                     dataField,
                                                     subseriesKey.getStart(),
                                                     true,
                                                     subseriesKey.getEnd(),
                                                     subseriesKey.getIncludeEnd());
        } else {
          return _underlying.getHistoricalTimeSeries(identifiers,
                                                     currentDate,
                                                     dataSource,
                                                     dataProvider,
                                                     dataField,
                                                     subseriesKey.getStart(),
                                                     true,
                                                     subseriesKey.getEnd(),
                                                     subseriesKey.getIncludeEnd(),
                                                     subseriesKey.getMaxPoints());
        }
      }
    };

    HistoricalTimeSeries hts = _cache.get(key, fetchHts);

    if (hts == null) {
      hts = _cache.get(seriesKey, fetchHts);
    }
    if (hts != null) {
      // Pick out the sub-series requested
      hts = getSubSeries(hts, start, includeStart, end, includeEnd, maxPoints);
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
      final String dataField,
      final ExternalIdBundle identifierBundle,
      final LocalDate identifierValidityDate,
      final String resolutionKey) {
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notEmpty(identifierBundle, "identifierBundle");
    HistoricalTimeSeriesKey key = new HistoricalTimeSeriesKey(resolutionKey,
                                                              identifierValidityDate,
                                                              identifierBundle,
                                                              null,
                                                              null,
                                                              dataField);

    Supplier<HistoricalTimeSeries> fetchHts = new Supplier<HistoricalTimeSeries>() {
      @Override
      public HistoricalTimeSeries get() {
        return _underlying.getHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey);
      }
    };

    return _cache.get(key, fetchHts);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    return doGetHistoricalTimeSeries(
        dataField,
        identifierBundle,
        LocalDate.now(getClock()),
        resolutionKey,
        start,
        includeStart,
        end,
        includeEnd,
        null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    return doGetHistoricalTimeSeries(
        dataField,
        identifierBundle,
        LocalDate.now(getClock()),
        resolutionKey,
        start,
        includeStart,
        end,
        includeEnd,
        maxPoints);
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
     * Tests whether this key exactly matches the user request, or if it would be a larger time-series that needs to be
     * cut down to match.
     *
     * @return true if an exact match, false if it needs trimming
     */
    public boolean isMatch(final LocalDate start,
                           final boolean includeStart,
                           final LocalDate end,
                           final boolean includeEnd,
                           final Integer maxPoints) {
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
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField,
                                                      ExternalIdBundle identifierBundle,
                                                      LocalDate identifierValidityDate,
                                                      String resolutionKey,
                                                      LocalDate start,
                                                      boolean includeStart,
                                                      LocalDate end,
                                                      boolean includeEnd) {
    return doGetHistoricalTimeSeries(
        dataField, identifierBundle, identifierValidityDate, resolutionKey, start, includeStart, end, includeEnd, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField,
                                                      ExternalIdBundle identifierBundle,
                                                      LocalDate identifierValidityDate,
                                                      String resolutionKey,
                                                      LocalDate start,
                                                      boolean includeStart,
                                                      LocalDate end,
                                                      boolean includeEnd,
                                                      int maxPoints) {
    return doGetHistoricalTimeSeries(
        dataField,
        identifierBundle,
        identifierValidityDate,
        resolutionKey,
        start,
        includeStart,
        end,
        includeEnd,
        maxPoints);
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
      return Pairs.of(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(final String dataField,
                                                         final ExternalIdBundle identifierBundle,
                                                         final LocalDate identifierValidityDate,
                                                         final String resolutionKey,
                                                         LocalDate start,
                                                         boolean includeStart,
                                                         LocalDate end,
                                                         boolean includeEnd,
                                                         final Integer maxPoints) {
    HistoricalTimeSeriesKey seriesKey = new HistoricalTimeSeriesKey(resolutionKey,
                                                                    identifierValidityDate,
                                                                    identifierBundle,
                                                                    null,
                                                                    null,
                                                                    dataField);
    final SubSeriesKey subseriesKey = new SubSeriesKey(start, end, maxPoints);
    ObjectsPair<HistoricalTimeSeriesKey, SubSeriesKey> key = ObjectsPair.of(seriesKey, subseriesKey);

    Supplier<HistoricalTimeSeries> fetchHts = new Supplier<HistoricalTimeSeries>() {
      @Override
      public HistoricalTimeSeries get() {
        if (maxPoints == null) {
          return _underlying.getHistoricalTimeSeries(dataField,
                                                     identifierBundle,
                                                     identifierValidityDate,
                                                     resolutionKey,
                                                     subseriesKey.getStart(),
                                                     true,
                                                     subseriesKey.getEnd(),
                                                     subseriesKey.getIncludeEnd());
        } else {
          return _underlying.getHistoricalTimeSeries(dataField,
                                                     identifierBundle,
                                                     identifierValidityDate,
                                                     resolutionKey,
                                                     subseriesKey.getStart(),
                                                     true,
                                                     subseriesKey.getEnd(),
                                                     subseriesKey.getIncludeEnd(),
                                                     subseriesKey.getMaxPoints());
        }
      }
    };

    HistoricalTimeSeries hts = _cache.get(key, fetchHts);
    if (hts != null && !subseriesKey.isMatch(start, includeStart, end, includeEnd, maxPoints)) {
      hts = getSubSeries(hts, start, includeStart, end, includeEnd, maxPoints);
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
    Set<ExternalIdBundle> remainingIds = new HashSet<>();

    for (ExternalIdBundle identifiers : identifierSet) {
      HistoricalTimeSeriesKey key = new HistoricalTimeSeriesKey(null,
                                                                null,
                                                                identifiers,
                                                                dataSource,
                                                                dataProvider,
                                                                dataField);
      HistoricalTimeSeries hts = _cache.get(key, null);
      if (hts == null) {
        //TODO handle misses
        remainingIds.add(identifiers);
      } else {
        result.put(identifiers, hts);
      }
    }
    if (remainingIds.size() > 0) {
      Map<ExternalIdBundle, HistoricalTimeSeries> remainingTsResults =
          _underlying.getHistoricalTimeSeries(remainingIds,
                                              dataSource,
                                              dataProvider,
                                              dataField,
                                              start,
                                              includeStart,
                                              end,
                                              includeEnd);
      for (Map.Entry<ExternalIdBundle, HistoricalTimeSeries> tsResult : remainingTsResults.entrySet()) {
        ExternalIdBundle identifiers = tsResult.getKey();
        HistoricalTimeSeries hts = tsResult.getValue();
        HistoricalTimeSeriesKey key = new HistoricalTimeSeriesKey(null,
                                                                  null,
                                                                  identifiers,
                                                                  dataSource,
                                                                  dataProvider,
                                                                  dataField);
        if (hts != null) {
          s_logger.debug("Caching time-series {}", hts);
          _cache.deepInsert(key, hts.getUniqueId().getObjectId(), hts);
          hts = getSubSeries(hts, start, includeStart, end, includeEnd, null);
        } else {
          s_logger.debug("Caching miss {}", key);
          _cache.markMissed(key);
        }
        result.put(identifiers, hts);
      }
    }
    return result;
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
      HistoricalTimeSeries hts,
      LocalDate start,
      boolean includeStart,
      LocalDate end,
      boolean includeEnd,
      Integer maxPoints) {

    if (hts == null) {
      return null;
    }
    LocalDateDoubleTimeSeries timeSeries = hts.getTimeSeries();
    if (timeSeries == null || timeSeries.isEmpty()) {
      return hts;
    }
    LocalDate effectiveStart;
    if (start == null) {
      effectiveStart = timeSeries.getEarliestTime();
    } else {
      if (includeStart) {
        effectiveStart = start;
      } else {
        effectiveStart = start.plusDays(1);
      }
      if (start.isBefore(timeSeries.getEarliestTime())) {
        effectiveStart = timeSeries.getEarliestTime();
      }
    }
    LocalDate effectiveEnd;
    if (end == null) {
      effectiveEnd = timeSeries.getLatestTime();
    } else {
      if (includeEnd) {
        effectiveEnd = end;
      } else {
        effectiveEnd = end.minusDays(1);
      }
      if (end.isAfter(timeSeries.getLatestTime())) {
        effectiveEnd = timeSeries.getLatestTime();
      }
    }
    if (effectiveStart.isAfter(timeSeries.getLatestTime()) || effectiveEnd.isBefore(timeSeries.getEarliestTime())) {
      return new SimpleHistoricalTimeSeries(hts.getUniqueId(), ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
    }
    timeSeries = timeSeries.subSeries(effectiveStart, true, effectiveEnd, true);
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
  public ExternalIdBundle getExternalIdBundle(final UniqueId uniqueId) {

    return _identifierBundleCache.get(uniqueId, new Supplier<ExternalIdBundle>() {
      @Override
      public ExternalIdBundle get() {
        return _underlying.getExternalIdBundle(uniqueId);
      }
    });
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle
   * method.
   */
  protected void shutdown() {
    _cache.shutdown();
    _identifierBundleCache.shutdown();
  }

}
