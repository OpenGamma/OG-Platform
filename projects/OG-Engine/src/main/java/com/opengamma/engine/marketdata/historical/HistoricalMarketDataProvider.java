/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;

/**
 * Historical market data provider that requires data from a specific date.
 */
public class HistoricalMarketDataProvider extends AbstractHistoricalMarketDataProvider {

  /**
   * Creates an instance.
   * 
   * @param historicalTimeSeriesSource the underlying source of historical data, not null
   * @param historicalTimeSeriesResolver the time series resolver, not null
   * @param timeSeriesResolverKey the source resolver key, or null to use the source default
   */
  public HistoricalMarketDataProvider(final HistoricalTimeSeriesSource historicalTimeSeriesSource,
      final HistoricalTimeSeriesResolver historicalTimeSeriesResolver, final String timeSeriesResolverKey) {
    super(historicalTimeSeriesSource, historicalTimeSeriesResolver, timeSeriesResolverKey);
  }

  /**
   * Creates an instance.
   * 
   * @param historicalTimeSeriesSource the underlying source of historical data, not null
   * @param historicalTimeSeriesResolver the time series resolver, not null
   */
  public HistoricalMarketDataProvider(final HistoricalTimeSeriesSource historicalTimeSeriesSource, final HistoricalTimeSeriesResolver historicalTimeSeriesResolver) {
    super(historicalTimeSeriesSource, historicalTimeSeriesResolver);
  }

  @Override
  protected LocalDate getHistoricalResolutionDate(final MarketDataSpecification marketDataSpec) {
    return ((FixedHistoricalMarketDataSpecification) marketDataSpec).getSnapshotDate();
  }

  @Override
  public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof FixedHistoricalMarketDataSpecification)) {
      return false;
    }
    return super.isCompatible(marketDataSpec);
  }

  @Override
  public MarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
    final FixedHistoricalMarketDataSpecification historicalSpec = (FixedHistoricalMarketDataSpecification) marketDataSpec;
    // TODO something better thought-out here
    //Instant snapshotInstant = historicalSpec.getSnapshotDate().atMidnight().atZone(ZoneOffset.UTC).toInstant();
    final Instant snapshotInstant = historicalSpec.getSnapshotDate().atTime(16, 0).atZone(ZoneOffset.UTC).toInstant();
    final LocalDate snapshotDate = historicalSpec.getSnapshotDate();
    return new HistoricalMarketDataSnapshot(getTimeSeriesSource(), snapshotInstant, snapshotDate);
  }

}
