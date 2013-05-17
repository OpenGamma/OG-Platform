/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries;

import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.time.LocalDateRange;
import com.opengamma.util.tuple.Pair;

/**
 * A provider of daily historical time-series.
 * <p>
 * This provides access to a data source for daily time-series information.
 * For example, major data sources provide historical time-series data, such as
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
   * Gets the whole of a time-series from the underlying data source.
   * <p>
   * The time-series is specified by external identifier bundle.
   * The other parameters restrict and validate the search.
   * 
   * @param externalIdBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @return the historical time-series, null if not found
   * @throws RuntimeException if a problem occurs
   */
  LocalDateDoubleTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle externalIdBundle,
      String dataSource, String dataProvider, String dataField);

  /**
   * Gets a time-series from the underlying data source.
   * <p>
   * The time-series is specified by external identifier bundle.
   * The other parameters restrict and validate the search.
   * <p>
   * This returns a subset of the data points filtered by the dates provided.
   * 
   * @param externalIdBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param dateRange  the date range to obtain, not null
   * @return the historical time-series, null if not found
   * @throws RuntimeException if a problem occurs
   */
  LocalDateDoubleTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle externalIdBundle,
      String dataSource, String dataProvider, String dataField, LocalDateRange dateRange);

  /**
   * Gets the latest data point from the underlying data source.
   * <p>
   * The time-series is specified by external identifier bundle.
   * The other parameters restrict and validate the search.
   * 
   * @param externalIdBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @return a pair containing the latest data point value and its date, null if not found
   * @throws RuntimeException if a problem occurs
   */
  Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle externalIdBundle,
      String dataSource, String dataProvider, String dataField);

  /**
   * Gets multiple time-series from the underlying data source.
   * <p>
   * The time-series are specified by external identifier bundles.
   * The other parameters restrict and validate the search.
   * <p>
   * The result is keyed by the input bundles.
   * A missing entry in the result occurs if the time-series information could not be found
   * 
   * @param externalIdBundleSet  a set containing an identifier bundle for each time-series required, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @param dateRange  the date range to obtain, not null
   * @return a map of each supplied identifier bundle to the corresponding time-series, not null
   * @throws RuntimeException if a problem occurs
   */
  Map<ExternalIdBundle, LocalDateDoubleTimeSeries> getHistoricalTimeSeries(
      Set<ExternalIdBundle> externalIdBundleSet,
      String dataSource, String dataProvider, String dataField, LocalDateRange dateRange);

  /**
   * Gets one or more time-series from the underlying data source.
   * <p>
   * This is the underlying operation.
   * All other methods delegate to this one.
   * 
   * @param request  the request, not null
   * @return the historical time-series result, not null
   * @throws RuntimeException if a problem occurs
   */
  HistoricalTimeSeriesProviderGetResult getHistoricalTimeSeries(HistoricalTimeSeriesProviderGetRequest request);

}
