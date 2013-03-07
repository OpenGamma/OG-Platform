/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;

/**
 * Historical market data provider which uses the latest point in a time-series.
 */
public class LatestHistoricalMarketDataProvider extends AbstractHistoricalMarketDataProvider {

  /**
   * Creates an instance.
   * 
   * @param historicalTimeSeriesSource the underlying source of historical data, not null
   * @param historicalTimeSeriesResolver the time series resolver, not null
   * @param timeSeriesResolverKey the source resolver key, or null to use the source default
   */
  protected LatestHistoricalMarketDataProvider(final HistoricalTimeSeriesSource historicalTimeSeriesSource,
      final HistoricalTimeSeriesResolver historicalTimeSeriesResolver, final String timeSeriesResolverKey) {
    super(historicalTimeSeriesSource, historicalTimeSeriesResolver, timeSeriesResolverKey);
  }

  /**
   * Creates an instance.
   * 
   * @param historicalTimeSeriesSource the underlying source of historical data, not null
   * @param historicalTimeSeriesResolver the time series resolver, not null
   */
  public LatestHistoricalMarketDataProvider(final HistoricalTimeSeriesSource historicalTimeSeriesSource, final HistoricalTimeSeriesResolver historicalTimeSeriesResolver) {
    super(historicalTimeSeriesSource, historicalTimeSeriesResolver);
  }

  @Override
  protected LocalDate getHistoricalResolutionDate(final MarketDataSpecification marketDataSpec) {
    return null;
  }

  @Override
  public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof LatestHistoricalMarketDataSpecification)) {
      return false;
    }
    return super.isCompatible(marketDataSpec);
  }

  @Override
  public MarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
    return new HistoricalMarketDataSnapshot(getTimeSeriesSource(), Instant.now(), null);
  }

}
