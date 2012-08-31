/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.provider.historicaltimeseries.impl;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.financial.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.financial.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.LocalDateRange;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * Abstract implementation of a provider of time-series.
 */
public abstract class AbstractHistoricalTimeSeriesProvider implements HistoricalTimeSeriesProvider {

  /**
   * The data source name.
   */
  private final String _dataSourceRegex;
  /**
   * The earliest start date.
   */
  private final LocalDate _earliestStartDate;

  /**
   * Creates an instance.
   * 
   * @param dataSourceRegex  the data source regex, not null
   * @param earliestStartDate  the earliest start date, not null
   */
  public AbstractHistoricalTimeSeriesProvider(String dataSourceRegex, LocalDate earliestStartDate) {
    ArgumentChecker.notNull(dataSourceRegex, "dataSourceRegex");
    ArgumentChecker.notNull(earliestStartDate, "earliestStartDate");
    _dataSourceRegex = dataSourceRegex;
    _earliestStartDate = earliestStartDate;
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle externalIdBundle, String dataSource, String dataProvider, String dataField, LocalDateRange dateRange) {
    
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(externalIdBundle, dataSource, dataProvider, dataField, dateRange);
    HistoricalTimeSeriesProviderGetResult result = getHistoricalTimeSeries(request);
    return result.getTimeSeries().get(externalIdBundle);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle externalIdBundle, String dataSource, String dataProvider, String dataField, LocalDateRange dateRange) {
    
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetLatest(externalIdBundle, dataSource, dataProvider, dataField, dateRange);
    HistoricalTimeSeriesProviderGetResult result = getHistoricalTimeSeries(request);
    LocalDateDoubleTimeSeries series = result.getTimeSeries().get(externalIdBundle);
    if (series == null || series.isEmpty()) {
      return null;
    }
    return Pair.of(series.getLatestTime(), series.getLatestValue());
  }

  @Override
  public Map<ExternalIdBundle, LocalDateDoubleTimeSeries> getHistoricalTimeSeries(
      Set<ExternalIdBundle> externalIdBundleSet, String dataSource, String dataProvider, String dataField, LocalDateRange dateRange) {
    
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetBulk(externalIdBundleSet, dataSource, dataProvider, dataField, dateRange);
    HistoricalTimeSeriesProviderGetResult result = getHistoricalTimeSeries(request);
    return result.getTimeSeries();
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesProviderGetResult getHistoricalTimeSeries(HistoricalTimeSeriesProviderGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.isTrue(request.getDataSource().matches(_dataSourceRegex), "Unsupported data source: " + request.getDataSource());
    
    // short-cut empty case
    if (request.getExternalIdBundles().isEmpty()) {
      return new HistoricalTimeSeriesProviderGetResult();
    }
    
    // fix dates
    LocalDateRange dateRange = request.getDateRange();
    if (dateRange.getStartDateInclusive().isBefore(_earliestStartDate)) {
      dateRange = dateRange.withStartDate(_earliestStartDate);
    }
    if (dateRange.getEndDateExclusive().equals(LocalDate.MAX_DATE)) {
      dateRange = dateRange.withEndDate(DateUtils.previousWeekDay());
    }
    
    // get time-series
    return doBulkGet(request.getExternalIdBundles(), request.getDataProvider(),
        request.getDataField(), dateRange, request.isLatestValueOnly());
  }

  /**
   * Gets the time-series.
   * 
   * @param externalIdBundleSet  a set containing an identifier bundle for each time-series required, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @param dateRange  the date range to obtain, not null
   * @param isLatestOnly  true to get the l
   * @return a map of each supplied identifier bundle to the corresponding time-series, not null
   */
  protected abstract HistoricalTimeSeriesProviderGetResult doBulkGet(
      Set<ExternalIdBundle> externalIdBundleSet, String dataProvider, String dataField, LocalDateRange dateRange, boolean isLatestOnly);

  //-------------------------------------------------------------------------
  /**
   * Filters the resulting bulk data map by the date range.
   * <p>
   * This is used to handle data providers that don't correctly filter.
   * 
   * @param result  the result to filter, not null
   * @param dateRange  the date range to filter by, not null
   * @param isLatestOnly  whether to only return the latest point in the series
   * @return the filtered map, not null
   */
  protected HistoricalTimeSeriesProviderGetResult filterBulkDates(
      HistoricalTimeSeriesProviderGetResult result, LocalDateRange dateRange, boolean isLatestOnly) {
    
    for (Map.Entry<ExternalIdBundle, LocalDateDoubleTimeSeries> entry : result.getTimeSeries().entrySet()) {
      entry.setValue(entry.getValue().subSeries(dateRange.getStartDateInclusive(), dateRange.getEndDateExclusive()));
      if (isLatestOnly && entry.getValue().size() > 0) {
        LocalDate date = entry.getValue().getLatestTime();
        double value = entry.getValue().getLatestValue();
        entry.setValue(new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {date}, new double[] {value}));
      }
    }
    return result;
  }

}
