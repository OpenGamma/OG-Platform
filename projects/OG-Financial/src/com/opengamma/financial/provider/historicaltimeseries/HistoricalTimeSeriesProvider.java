/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.provider.historicaltimeseries;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.time.LocalDateRange;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * A provider of daily historical time-series.
 * <p>
 * The interface is implemented to access data provider of daily time-series information.
 * For example, major data providers provide historical time-series data, such as
 * the closing price or volume traded each day for an equity.
 * <p>
 * This interface has a minimal and simple API designed to be easy to implement on top
 * of new data providers.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface HistoricalTimeSeriesProvider {

  /**
   * Finds a time-series from identifierBundle, source, provider and field checking
   * the validity of the identifierBundle by date.
   * <p>
   * This returns a subset of the data points filtered by the dates provided and limited to the 
   * specified maximum number of points:
   * +ve maxPoints returns at most maxPoints data points counting forwards from the earliest date
   * -ve maxPoints returns at most -maxPoints data points counting backwards from the latest date 
   * 
   * @param externalIdBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param dateRange  the date range to obtain, not null
   * @return the historical time-series, null if not found
   */
  LocalDateDoubleTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle externalIdBundle,
      String dataSource, String dataProvider, String dataField, LocalDateRange dateRange);

  /**
   * Returns the latest data point from the specified date range in the time series.
   * 
   * @param externalIdBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param dateRange  the date range to obtain, not null
   * @return a pair containing the latest data point value and its date, null if not found
   */
  Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle externalIdBundle,
      String dataSource, String dataProvider, String dataField, LocalDateRange dateRange);

  /**
   * Finds multiple time-series for the same source, provider and field, with all data
   * points between start and end date. 
   * <p>
   * This returns a subset of the data points filtered by the dates provided.
   * 
   * @param externalIdBundleSet  a set containing an identifier bundle for each time-series required, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @param dateRange  the date range to obtain, not null
   * @return a map of each supplied identifier bundle to the corresponding time-series, not null
   */
  Map<ExternalIdBundle, LocalDateDoubleTimeSeries> getHistoricalTimeSeries(
      Set<ExternalIdBundle> externalIdBundleSet,
      String dataSource, String dataProvider, String dataField, LocalDateRange dateRange);

  /**
   * Gets one or more historical time-series from the data source.
   * <p>
   * This is the underlying operation.
   * All other methods delegate to this one.
   * 
   * @param request  the request, not null
   * @return the historical time-series result, null if not found
   */
  HistoricalTimeSeriesProviderGetResult getHistoricalTimeSeries(HistoricalTimeSeriesProviderGetRequest request);

}
