/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaldata.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.time.calendar.LocalDate;

import com.google.common.base.Supplier;
import com.opengamma.core.historicaldata.HistoricalDataSource;
import com.opengamma.core.historicaldata.HistoricalTimeSeries;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * In memory data source, typically used for testing.
 */
public class MockHistoricalDataSource implements HistoricalDataSource {

  /**
   * The store of unique identifiers.
   */
  private Map<HistoricalDataKey, UniqueIdentifier> _metaUniqueIdentifierStore = new ConcurrentHashMap<HistoricalDataKey, UniqueIdentifier>();
  /**
   * The store of unique time-series.
   */
  private Map<UniqueIdentifier, HistoricalTimeSeries> _timeSeriesStore = new ConcurrentHashMap<UniqueIdentifier, HistoricalTimeSeries>();
  /**
   * The suppler of unique identifiers.
   */
  private final Supplier<UniqueIdentifier> _uidSupplier;

  /**
   * Creates an instance using the default scheme for each {@link UniqueIdentifier} created.
   */
  public MockHistoricalDataSource() {
    _uidSupplier = new UniqueIdentifierSupplier("MockHTS");
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uidSupplier  the supplier of unique identifiers, not null
   */
  public MockHistoricalDataSource(final Supplier<UniqueIdentifier> uidSupplier) {
    ArgumentChecker.notNull(uidSupplier, "uidSupplier");
    _uidSupplier = uidSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalData(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return _timeSeriesStore.get(uniqueId);
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(
      UniqueIdentifier uniqueId, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    HistoricalTimeSeries hts = getHistoricalData(uniqueId);
    return getSubSeries(hts, start, inclusiveStart, end, exclusiveEnd);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField) {
    return getHistoricalData(identifiers, (LocalDate) null, dataSource, dataProvider, dataField);
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    HistoricalDataKey key = new HistoricalDataKey(null, currentDate, identifiers, dataSource, dataProvider, dataField);
    UniqueIdentifier uniqueId = _metaUniqueIdentifierStore.get(key);
    if (uniqueId == null) {
      return null;
    }
    return getHistoricalData(uniqueId);
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    return getHistoricalData(
        identifiers, (LocalDate) null, dataSource, dataProvider, dataField, start, inclusiveStart, end, exclusiveEnd);
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(
      IdentifierBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    HistoricalTimeSeries hts = getHistoricalData(identifiers, currentDate, dataSource, dataProvider, dataField);
    return getSubSeries(hts, start, inclusiveStart, end, exclusiveEnd);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalData(IdentifierBundle identifiers, String configDocName) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support getHistorical without metadata");
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(IdentifierBundle identifiers, String configName, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support getHistorical without metadata");
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String configDocName) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support getHistorical without metadata");
  }

  @Override
  public HistoricalTimeSeries getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String configDocName,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support getHistorical without metadata");
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<IdentifierBundle, HistoricalTimeSeries> getHistoricalData(
      Set<IdentifierBundle> identifiers, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    throw new UnsupportedOperationException(getClass().getName() + " does not support getHistoricalData for multiple time-series");
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
      IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField, LocalDateDoubleTimeSeries timeSeriesDataPoints) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notNull(timeSeriesDataPoints, "timeSeriesDataPoints");
    HistoricalDataKey metaKey = new HistoricalDataKey(null, null, identifiers, dataSource, dataProvider, dataField);
    UniqueIdentifier uid = null;
    synchronized (this) {
      uid = _metaUniqueIdentifierStore.get(metaKey);
      if (uid == null) {
        uid = _uidSupplier.get();
        _metaUniqueIdentifierStore.put(metaKey, uid);
      }
    }
    _timeSeriesStore.put(uid, new HistoricalTimeSeriesImpl(uid, timeSeriesDataPoints));
  }

  /**
   * Gets a sub-series based on the supplied dates.
   * 
   * @param hts  the time-series, null returns null
   * @param start  the start date, null will load the earliest date 
   * @param inclusiveStart  whether or not the start date is included in the result
   * @param end  the end date, null will load the latest date
   * @param exclusiveEnd  whether or not the end date is included in the result
   * @return the historical time-series, null if null input
   */
  private HistoricalTimeSeries getSubSeries(
      HistoricalTimeSeries hts, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (hts == null) {
      return null;
    }
    if (hts.getTimeSeries().isEmpty()) {
      return hts;
    }
    LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) hts.getTimeSeries().subSeries(start, inclusiveStart, end, exclusiveEnd);
    return new HistoricalTimeSeriesImpl(hts.getUniqueId(), timeSeries);
  }

}
