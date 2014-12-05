/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * Market data factory backed by a historical time series source.
 */
public class HistoricalMarketDataFactory implements MarketDataFactory<FixedHistoricalMarketDataSpecification> {

  private final HistoricalTimeSeriesSource _timeSeriesSource;
  private final String _dataSource;
  private final String _dataProvider;

  /**
   * @param timeSeriesSource a source of historical time series
   * @param dataSource the data source name used when looking up time series
   * @param dataProvider the data provider name used when looking up time series, possibly null
   */
  public HistoricalMarketDataFactory(HistoricalTimeSeriesSource timeSeriesSource,
                                     String dataSource,
                                     @Nullable String dataProvider) {
    _timeSeriesSource = ArgumentChecker.notNull(timeSeriesSource, "timeSeriesSource");
    _dataSource = ArgumentChecker.notEmpty(dataSource, "dataSource");
    _dataProvider = dataProvider;
  }

  @Override
  public Class<FixedHistoricalMarketDataSpecification> getSpecificationType() {
    return FixedHistoricalMarketDataSpecification.class;
  }

  @Override
  public MarketDataSource create(FixedHistoricalMarketDataSpecification spec) {
    final LocalDate date = spec.getSnapshotDate();

    return new DataSource(date);
  }

  /**
   * Simple data source implementation that gets data for a single date from a time series source.
   */
  private class DataSource implements MarketDataSource {

    /** The date used for looking up data. */
    private final LocalDate _date;

    private DataSource(LocalDate date) {
      _date = date;
    }

    @Override
    public Map<MarketDataRequest, Result<?>> get(Set<MarketDataRequest> requests) {
      ImmutableMap.Builder<MarketDataRequest, Result<?>> builder = ImmutableMap.builder();

      for (MarketDataRequest request : requests) {
        HistoricalTimeSeries timeSeries =
            _timeSeriesSource.getHistoricalTimeSeries(request.getId(), _dataSource, _dataProvider,
                                                      request.getFieldName().getName(), _date, true, _date, true);
        if (timeSeries != null) {
          Double value = timeSeries.getTimeSeries().getValue(_date);

          if (value != null) {
            builder.put(request, Result.success(value));
          } else {
            builder.put(request, Result.failure(FailureStatus.MISSING_DATA, "No data available for {}/{}/{}/{}/{}",
                                                request.getId(), request.getFieldName().getName(),
                                                _date, _dataSource, _dataProvider));
          }
        }
      }
      return builder.build();
    }
  }
}
