/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

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
   * @param historicalTimeSeriesSource underlying source
   * @param timeSeriesResolverKey the source resolver key, or null to use the source default
   * @param fieldResolverKey the field name resolver resolution key, or null to use the resolver default
   */
  public HistoricalMarketDataProvider(final HistoricalTimeSeriesSource historicalTimeSeriesSource, final String timeSeriesResolverKey, 
      final String fieldResolverKey) {
    super(historicalTimeSeriesSource, timeSeriesResolverKey, fieldResolverKey);
  }
  
  /**
   * Creates an instance.
   * 
   * @param historicalTimeSeriesSource underlying source
   */
  public HistoricalMarketDataProvider(HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    super(historicalTimeSeriesSource);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof FixedHistoricalMarketDataSpecification)) {
      return false;
    }
    return super.isCompatible(marketDataSpec);
  }
  
  @Override
  public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
    FixedHistoricalMarketDataSpecification historicalSpec = (FixedHistoricalMarketDataSpecification) marketDataSpec;
    // TODO something better thought-out here
    Instant snapshotInstant = historicalSpec.getSnapshotDate().atMidnight().atZone(TimeZone.UTC).toInstant();
    LocalDate snapshotDate = historicalSpec.getSnapshotDate();
    return new HistoricalMarketDataSnapshot(getTimeSeriesSource(), snapshotInstant, snapshotDate, historicalSpec.getTimeSeriesFieldResolverKey());
  }
  
}
