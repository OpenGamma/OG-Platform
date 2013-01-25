/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.google.common.collect.Maps;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.LocalDateRange;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * Loads time-series from Bloomberg.
 * This class is now implemented on top of HistoricalTimeSeriesProvider and is effectively deprecated. 
 */
public class BloombergHistoricalTimeSeriesSource implements HistoricalTimeSeriesSource {

  /**
   * Default start date for loading time-series
   */
  public static final LocalDate DEFAULT_START_DATE = LocalDate.of(1900, Month.JANUARY, 01);

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergHistoricalTimeSeriesSource.class);
  /**
   * The Bloomberg service.
   */
  private static final UniqueIdSupplier UID_SUPPLIER = new UniqueIdSupplier("BbgHTS");

  /**
   * The provider.
   */
  private final HistoricalTimeSeriesProvider _provider;

  /**
   * Creates an instance.
   * 
   * @param provider  the time-series provider, not null
   */
  public BloombergHistoricalTimeSeriesSource(HistoricalTimeSeriesProvider provider) {
    ArgumentChecker.notNull(provider, "provider");
    _provider = provider;
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException("Change events not supported");
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(
      ExternalIdBundle externalIdBundle, String dataSource, String dataProvider, String dataField,
      LocalDateRange dateRange, Integer maxPoints) {
    
    s_logger.info("Getting HistoricalTimeSeries for security {}", externalIdBundle);
    
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(externalIdBundle, dataSource, dataProvider, dataField, dateRange);
    request.setMaxPoints(maxPoints);
    HistoricalTimeSeriesProviderGetResult result = _provider.getHistoricalTimeSeries(request);
    LocalDateDoubleTimeSeries timeSeries = result.getResultMap().get(externalIdBundle);
    if (timeSeries == null) {
      s_logger.info("Unable to get HistoricalTimeSeries for {}", externalIdBundle);
      return null;
    }
    return new SimpleHistoricalTimeSeries(UID_SUPPLIER.get(), timeSeries);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using unique identifier");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using unique identifier");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using unique identifier");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using unique identifier");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using unique identifier");
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String dataField) {
    return doGetHistoricalTimeSeries(identifiers, dataSource, dataProvider, dataField, LocalDateRange.ALL, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    if (!includeStart && start != null) {
      start = start.plusDays(1);
    }
    LocalDateRange dateRange = LocalDateRange.ofNullUnbounded(start, end, includeEnd);
    return doGetHistoricalTimeSeries(identifiers, dataSource, dataProvider, dataField, dateRange, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, String dataSource, String dataProvider, String dataField, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    if (!includeStart && start != null) {
      start = start.plusDays(1);
    }
    LocalDateRange dateRange = LocalDateRange.ofNullUnbounded(start, end, includeEnd);
    Integer maxPointsVal = (maxPoints == 0 ? null : maxPoints);
    return doGetHistoricalTimeSeries(identifiers, dataSource, dataProvider, dataField, dateRange, maxPointsVal);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifiers, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifiers,
      LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using identifier validity date");
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, String resolutionKey) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, LocalDate identifierValidityDate, String resolutionKey) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifiers, LocalDate identifierValidityDate, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
      Set<ExternalIdBundle> externalIdBundles, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    
    if (!includeStart && start != null) {
      start = start.plusDays(1);
    }
    LocalDateRange dateRange = LocalDateRange.ofNullUnbounded(start, end, includeEnd);
    s_logger.info("Getting HistoricalTimeSeries for securities {}", externalIdBundles);
    
    Map<ExternalIdBundle, LocalDateDoubleTimeSeries> map = _provider.getHistoricalTimeSeries(externalIdBundles, dataSource, dataProvider, dataField, dateRange);
    Map<ExternalIdBundle, HistoricalTimeSeries> result = Maps.newHashMap();
    for (ExternalIdBundle bundle : map.keySet()) {
      LocalDateDoubleTimeSeries ts = map.get(bundle);
      HistoricalTimeSeries hts = null;
      if (ts != null) {
        hts = new SimpleHistoricalTimeSeries(UID_SUPPLIER.get(), ts);
      }
      result.put(bundle, hts);
    }
    return result;
  }

  @Override
  public ExternalIdBundle getExternalIdBundle(UniqueId uniqueId) {
    throw new UnsupportedOperationException("Unable to retrieve historical time-series from Bloomberg using config");
  }

}
