/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import javax.time.Instant;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;

/**
 * Historical market data provider which uses the latest point in a time-series.
 */
public class LatestHistoricalMarketDataProvider extends AbstractHistoricalMarketDataProvider {

  /**
   * Creates an instance.
   * 
   * @param historicalTimeSeriesSource underlying source
   * @param timeSeriesResolverKey the source resolver key, or null to use the source default
   * @param fieldResolverKey the field name resolver resolution key, or null to use the resolver default
   */
  protected LatestHistoricalMarketDataProvider(final HistoricalTimeSeriesSource historicalTimeSeriesSource, final String timeSeriesResolverKey, 
      final String fieldResolverKey) {
    super(historicalTimeSeriesSource, timeSeriesResolverKey, fieldResolverKey);
  }
  
  /**
   * Creates an instance.
   * 
   * @param historicalTimeSeriesSource underlying source
   */
  public LatestHistoricalMarketDataProvider(HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    super(historicalTimeSeriesSource);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public boolean isCompatible(MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof LatestHistoricalMarketDataSpecification)) {
      return false;
    }
    return super.isCompatible(marketDataSpec);
  }  
  
  @Override
  public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
    HistoricalMarketDataSpecification historicalSpec = (HistoricalMarketDataSpecification) marketDataSpec;
    return new HistoricalMarketDataSnapshot(getTimeSeriesSource(), Instant.now(), null, historicalSpec.getTimeSeriesFieldResolverKey());
  }

}
