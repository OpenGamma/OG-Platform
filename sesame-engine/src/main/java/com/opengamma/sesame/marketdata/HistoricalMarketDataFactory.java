/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Set;

import javax.annotation.Nullable;

import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 *
 */
public class HistoricalMarketDataFactory implements MarketDataFactory<FixedHistoricalMarketDataSpecification> {

  private final HistoricalTimeSeriesSource _timeSeriesSource;
  private final String _dataSource;
  private final String _dataProvider;

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
    public MarketDataResponse get(Set<MarketDataRequest> requests) {
      MarketDataResponse.Builder builder = MarketDataResponse.builder();

      for (MarketDataRequest request : requests) {
        HistoricalTimeSeries timeSeries =
            _timeSeriesSource.getHistoricalTimeSeries(request.getId(), _dataSource, _dataProvider,
                                                      request.getFieldName().getName(), _date, true, _date, true);
        if (timeSeries != null) {
          Double value = timeSeries.getTimeSeries().getValue(_date);

          if (value != null) {
            builder.add(request, Result.success(value));
          } else {
            builder.add(request, Result.failure(FailureStatus.MISSING_DATA, "No data available for {}/{}/{}",
                                                request.getId(), request.getFieldName().getName(), _date));
          }
        }
      }
      return builder.build();
    }
  }
}
