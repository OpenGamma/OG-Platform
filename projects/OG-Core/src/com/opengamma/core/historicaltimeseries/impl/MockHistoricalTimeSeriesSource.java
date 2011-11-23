/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.time.calendar.LocalDate;

import com.google.common.base.Supplier;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSummary;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * In memory source, typically used for testing.
 */
public class MockHistoricalTimeSeriesSource implements HistoricalTimeSeriesSource {

  /**
   * The store of unique identifiers.
   */
  private Map<HistoricalTimeSeriesKey, UniqueId> _metaUniqueIdStore = new ConcurrentHashMap<HistoricalTimeSeriesKey, UniqueId>();
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
   * @param uniqueIdSupplier  the supplier of unique identifiers, not null
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
    return getSubSeries(hts, start, inclusiveStart, end, includeEnd);
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
    return getSubSeries(hts, start, inclusiveStart, end, includeEnd);
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
      return getSubSeries(getAnyMatching(dataField, identifiers), start, inclusiveStart, end, includeEnd);
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

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesSummary getSummary(UniqueId uniqueId) {
    LocalDateDoubleTimeSeries ts = getHistoricalTimeSeries(uniqueId).getTimeSeries();
    HistoricalTimeSeriesSummary result = new HistoricalTimeSeriesSummary();
    result.setEarliestDate(ts.getEarliestTime());
    result.setLatestDate(ts.getLatestTime());
    result.setEarliestValue(ts.getEarliestValue());
    result.setLatestValue(ts.getLatestValue());
    return result;
  }

  @Override
  public HistoricalTimeSeriesSummary getSummary(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException("Does not support getting summary information by object id and version correction");
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
   * @param identifiers  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param timeSeriesDataPoints  the time-series data points, not null
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
      }
    }
    _timeSeriesStore.put(uniqueId, new SimpleHistoricalTimeSeries(uniqueId, timeSeriesDataPoints));
  }

  /**
   * Gets a sub-series based on the supplied dates.
   * 
   * @param hts  the time-series, null returns null
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param includeEnd  whether or not the end date is included in the result
   * @return the historical time-series, null if null input
   */
  private HistoricalTimeSeries getSubSeries(
      HistoricalTimeSeries hts, LocalDate start, boolean inclusiveStart, LocalDate end, boolean includeEnd) {
    if (hts == null) {
      return null;
    }
    if (hts.getTimeSeries().isEmpty()) {
      return hts;
    }
    LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) hts.getTimeSeries().subSeries(start, inclusiveStart, end, includeEnd);
    return new SimpleHistoricalTimeSeries(hts.getUniqueId(), timeSeries);
  }

}
