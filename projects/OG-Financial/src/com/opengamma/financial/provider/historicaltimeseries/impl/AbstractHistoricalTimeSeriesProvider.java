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
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * Abstract implementation of a provider of time-series.
 * <p>
 * This provides default implementations of the interface methods that delegate to a
 * protected method that subclasses must implement.
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
    return result.getResultMap().get(externalIdBundle);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle externalIdBundle, String dataSource, String dataProvider, String dataField, LocalDateRange dateRange) {
    
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetLatest(externalIdBundle, dataSource, dataProvider, dataField, dateRange);
    HistoricalTimeSeriesProviderGetResult result = getHistoricalTimeSeries(request);
    LocalDateDoubleTimeSeries series = result.getResultMap().get(externalIdBundle);
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
    return result.getResultMap();
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
      dateRange = fixStartDate(dateRange);
    }
    if (dateRange.isEndDateMaximum()) {
      dateRange = fixEndDate(dateRange);
    }
    
    // get time-series
    return doBulkGet(request);
  }

  /**
   * Fixes the start date.
   * <p>
   * This method is only invoked if the start date is before the earliest start date passed into the constructor.
   * This implementation sets the start date to the earliest start date passed into the constructor.
   * 
   * @param dateRange  the date range to fix, not null
   * @return the fixed range, not null
   */
  protected LocalDateRange fixStartDate(LocalDateRange dateRange) {
    return dateRange.withStartDate(_earliestStartDate);
  }

  /**
   * Fixes the end date.
   * <p>
   * This method is only invoked if the end date is unbounded.
   * This implementation obtains the current date and chooses the previous Mon-Fri from it.
   * 
   * @param dateRange  the date range to fix, not null
   * @return the fixed range, not null
   */
  protected LocalDateRange fixEndDate(LocalDateRange dateRange) {
    return dateRange.withEndDate(DateUtils.previousWeekDay());
  }

  /**
   * Gets the time-series.
   * <p>
   * The data source is checked before this method is invoked.
   * 
   * @param request  the request, with a non-empty set of identifiers, not null
   * @return the result, not null
   */
  protected abstract HistoricalTimeSeriesProviderGetResult doBulkGet(HistoricalTimeSeriesProviderGetRequest request);

  //-------------------------------------------------------------------------
  /**
   * Filters the resulting bulk data map by the date range.
   * <p>
   * This is used to handle data providers that don't correctly filter.
   * 
   * @param result  the result to filter, not null
   * @param dateRange  the date range to filter by, not null
   * @param maxPoints  the maximum number of points required, negative back from the end date, null for all
   * @return the filtered map, not null
   */
  protected HistoricalTimeSeriesProviderGetResult filterBulkDates(
      HistoricalTimeSeriesProviderGetResult result, LocalDateRange dateRange, Integer maxPoints) {
    
    for (Map.Entry<ExternalIdBundle, LocalDateDoubleTimeSeries> entry : result.getResultMap().entrySet()) {
      LocalDateDoubleTimeSeries ts = entry.getValue();
      entry.setValue(ts.subSeries(dateRange.getStartDateInclusive(), dateRange.getEndDateExclusive()));
      if (maxPoints != null && ts.size() > Math.abs(maxPoints)) {
        if (maxPoints < 0) {
          ts = ts.tail(-maxPoints);
        } else {
          ts = ts.head(maxPoints);
        }
        entry.setValue(ts);
      }
    }
    return result;
  }

}
