/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.historicaldata;

import java.util.Map;
import java.util.WeakHashMap;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.id.DomainSpecificIdentifiers;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * Caches whole time series for CACHE_LIFE_FIXED + RND(CACHE_LIFE_VARIABLE) minutes before refreshing from underlying data source.
 * This is to prevent the whole cache being flushed a fixed time after startup, rather the cache will be flushed gradually.
 * Uses a WeakHashMap to discard entries if memory gets low.  This is really meant to be a temporary solution until something
 * better is implemented.
 *
 * @author jim
 */
public class SimpleInMemoryHistoricalDataProviderCache implements HistoricalDataProvider {
  private static final int CACHE_LIFE_FIXED = 60*12; // 12 hours
  private static final int CACHE_LIFE_VARIABLE = 60*12; // 0-12 hours
  private static final boolean INCLUDE_LAST_DAY = true;
  
  // WeakHashMap entries get garbage collected if memory runs low.
  Map<CacheKey, Pair<ZonedDateTime, LocalDateDoubleTimeSeries>> _timeSeriesCache = new WeakHashMap<CacheKey, Pair<ZonedDateTime, LocalDateDoubleTimeSeries>>();
  private Clock _clock;
  private HistoricalDataProvider _underlyingDataProvider; 
  
  public SimpleInMemoryHistoricalDataProviderCache(HistoricalDataProvider underlyingDataProvider) {
    _underlyingDataProvider = underlyingDataProvider;
    // REVIEW: jim 22-March-2010 -- change this to use the local time zone when it's fixed in JSR-310 on Unix (or rather, when the fix is in ivy).
    _clock = Clock.system(TimeZone.UTC);
  }
  
  public SimpleInMemoryHistoricalDataProviderCache(HistoricalDataProvider underlyingDataProvider, Clock clock) { // injectable clock for testing.
    _underlyingDataProvider = underlyingDataProvider;
    _clock = clock;
  }
  
  @Override
  public LocalDateDoubleTimeSeries getHistoricalTimeSeries(
      DomainSpecificIdentifiers dsids, String dataSource, String dataProvider,
      String field) {
    Pair<ZonedDateTime, LocalDateDoubleTimeSeries> entry = _timeSeriesCache.get(new CacheKey(dsids, dataSource, dataProvider, field));
    if (entry != null) { // remember no point in using containsKey because might have been GC'd by the time we pull it out.
      ZonedDateTime entryDate = entry.getFirst();
      ZonedDateTime now = getClock().zonedDateTime();
      if (entryDate.plusMinutes(CACHE_LIFE_FIXED + ((int)(Math.random() * CACHE_LIFE_VARIABLE))).isAfter(now)) { // is within a 12-24 hours (allows cache to slowly refresh rather than in big chunks).
        return entry.getSecond();
      }
    }
    LocalDateDoubleTimeSeries dts = _underlyingDataProvider.getHistoricalTimeSeries(dsids, dataSource, dataProvider, field);
    _timeSeriesCache.put(new CacheKey(dsids, dataSource, dataProvider, field), new Pair<ZonedDateTime, LocalDateDoubleTimeSeries>(getClock().zonedDateTime(), dts));
    return dts;
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalTimeSeries(
      DomainSpecificIdentifiers dsids, String dataSource, String dataProvider,
      String field, LocalDate start, LocalDate end) {
    LocalDateDoubleTimeSeries dts = getHistoricalTimeSeries(dsids, dataSource, dataProvider, field);
    return (LocalDateDoubleTimeSeries) dts.subSeries(start, true, end, INCLUDE_LAST_DAY);
  }
  
  public Clock getClock() {
    return _clock;
  }
  
  private class CacheKey {
    private DomainSpecificIdentifiers _dsids;
    private String _dataSource;
    private String _dataProvider;
    private String _field;
    
    public CacheKey(DomainSpecificIdentifiers dsids, String dataSource, String dataProvider, String field) {
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
