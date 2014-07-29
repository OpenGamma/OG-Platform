/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.threeten.bp.LocalDate;

import com.google.common.base.Supplier;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * In memory source, typically used for testing.
 */
public class MockHistoricalTimeSeriesSource implements HistoricalTimeSeriesSource {

  /**
   * The store of unique identifiers.
   */
  private Map<HistoricalTimeSeriesKey, UniqueId> _metaUniqueIdStore = new ConcurrentHashMap<HistoricalTimeSeriesKey, UniqueId>();
  /**
   * The store of unique identifiers.
   */
  private Map<UniqueId, HistoricalTimeSeriesKey> _uniqueIdMetaStore = new ConcurrentHashMap<UniqueId, HistoricalTimeSeriesKey>();
  /**
   * The store of unique time-series.
   */
  private Map<UniqueId, HistoricalTimeSeries> _timeSeriesStore = new ConcurrentHashMap<UniqueId, HistoricalTimeSeries>();
  /**
   * The suppler of unique identifiers.
   */
  private final Supplier<UniqueId> _uniqueIdSupplier;

  /**
   * Creates an instance using the default scheme for each {@link UniqueId} created.
   */
  public MockHistoricalTimeSeriesSource() {
    _uniqueIdSupplier = new UniqueIdSupplier("MockHTS");
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uniqueIdSupplier the supplier of unique identifiers, not null
   */
  public MockHistoricalTimeSeriesSource(final Supplier<UniqueId> uniqueIdSupplier) {
    ArgumentChecker.notNull(uniqueIdSupplier, "uniqueIdSupplier");
    _uniqueIdSupplier = uniqueIdSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return _timeSeriesStore.get(uniqueId);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      UniqueId uniqueId, LocalDate start, boolean inclusiveStart, LocalDate end, boolean includeEnd) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(uniqueId);
    return getSubSeries(hts, start, inclusiveStart, end, includeEnd, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(uniqueId);
    return getSubSeries(hts, start, includeStart, end, includeEnd, maxPoints);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(uniqueId);
    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return Pairs.of(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    HistoricalTimeSeries hts = getSubSeries(getHistoricalTimeSeries(uniqueId), start, includeStart, end, includeEnd, -1);
    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return Pairs.of(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String dataField) {
    return getHistoricalTimeSeries(identifiers, (LocalDate) null, dataSource, dataProvider, dataField);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    HistoricalTimeSeriesKey key = new HistoricalTimeSeriesKey(null, null, identifiers, dataSource, dataProvider, dataField);
    UniqueId uniqueId = _metaUniqueIdStore.get(key);
    if (uniqueId == null) {
      return null;
    }
    return getHistoricalTimeSeries(uniqueId);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean includeEnd) {
    return getHistoricalTimeSeries(
        identifiers, (LocalDate) null, dataSource, dataProvider, dataField, start, inclusiveStart, end, includeEnd);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean includeEnd) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(identifiers, identifierValidityDate, dataSource, dataProvider, dataField);
    return getSubSeries(hts, start, inclusiveStart, end, includeEnd, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    return getHistoricalTimeSeries(
        identifiers, (LocalDate) null, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, maxPoints);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(identifiers, identifierValidityDate, dataSource, dataProvider, dataField);
    return getSubSeries(hts, start, includeStart, end, includeEnd, maxPoints);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifiers, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(identifiers, identifierValidityDate, dataSource, dataProvider, dataField);
    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return Pairs.of(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifiers, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    HistoricalTimeSeries hts = getSubSeries(
        getHistoricalTimeSeries(identifiers, identifierValidityDate, dataSource, dataProvider, dataField),
        start, includeStart, end, includeEnd, -1);
    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return Pairs.of(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(identifierBundle, (LocalDate) null, dataSource, dataProvider, dataField);
    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return Pairs.of(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    HistoricalTimeSeries hts = getSubSeries(
        getHistoricalTimeSeries(identifierBundle, (LocalDate) null, dataSource, dataProvider, dataField),
        start, includeStart, end, includeEnd, -1);

    if (hts == null || hts.getTimeSeries() == null || hts.getTimeSeries().isEmpty()) {
      return null;
    } else {
      return Pairs.of(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    }
  }

  private HistoricalTimeSeries getAnyMatching(final String dataField, final ExternalIdBundle identifiers) {
    for (Map.Entry<HistoricalTimeSeriesKey, UniqueId> ts : _metaUniqueIdStore.entrySet()) {
      if (dataField.equals(ts.getKey().getDataField()) && identifiers.equals(ts.getKey().getExternalIdBundle())) {
        return _timeSeriesStore.get(ts.getValue());
      }
    }
    return null;
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, String resolutionKey) {
    if (resolutionKey == null) {
      return getAnyMatching(dataField, identifiers);
    } else {
      throw new UnsupportedOperationException(getClass().getName() + " does not support resolved getHistoricalTimeSeries");
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, String configName,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean includeEnd) {
    if (configName == null) {
      return getSubSeries(getAnyMatching(dataField, identifiers), start, inclusiveStart, end, includeEnd, null);
    } else {
      throw new UnsupportedOperationException(getClass().getName() + " does not support resolved getHistoricalTimeSeries");
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, LocalDate identifierValidityDate, String resolutionKey) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support resolved getHistoricalTimeSeries");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, LocalDate identifierValidityDate, String resolutionKey,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support resolved getHistoricalTimeSeries");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support resolved getHistoricalTimeSeries");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support resolved getHistoricalTimeSeries");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support resolved getHistoricalTimeSeries");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support resolved getHistoricalTimeSeries");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support resolved getHistoricalTimeSeries");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support resolved getHistoricalTimeSeries");
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
      Set<ExternalIdBundle> identifiers, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean inclusiveStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support getHistoricalTimeSeries for multiple time-series");
  }

  //-------------------------------------------------------------------------
  /**
   * Stores a time-series in this source.
   * 
   * @param identifiers the identifier bundle, not null
   * @param dataSource the data source, not null
   * @param dataProvider the data provider, not null
   * @param dataField the dataField, not null
   * @param timeSeriesDataPoints the time-series data points, not null
   */
  public void storeHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String dataField, LocalDateDoubleTimeSeries timeSeriesDataPoints) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notNull(timeSeriesDataPoints, "timeSeriesDataPoints");
    HistoricalTimeSeriesKey metaKey = new HistoricalTimeSeriesKey(null, null, identifiers, dataSource, dataProvider, dataField);
    UniqueId uniqueId = null;
    synchronized (this) {
      uniqueId = _metaUniqueIdStore.get(metaKey);
      if (uniqueId == null) {
        uniqueId = _uniqueIdSupplier.get();
        _metaUniqueIdStore.put(metaKey, uniqueId);
        _uniqueIdMetaStore.put(uniqueId, metaKey);
      }
    }
    _timeSeriesStore.put(uniqueId, new SimpleHistoricalTimeSeries(uniqueId, timeSeriesDataPoints));
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

  @Override
  public ExternalIdBundle getExternalIdBundle(UniqueId uniqueId) {
    if (_uniqueIdMetaStore.containsKey(uniqueId)) {
      return _uniqueIdMetaStore.get(uniqueId).getExternalIdBundle();
    }
    return null;
  }

  @Override
  public ChangeManager changeManager() {
    return new BasicChangeManager();
  }
}
