/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import javax.time.Instant;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.SecuritySource;
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
   * @param historicalTimeSeriesSource  the underlying source of historical data, not null
   * @param securitySource  the source of securities, not null
   * @param timeSeriesResolverKey  the source resolver key, or null to use the source default
   */
  protected LatestHistoricalMarketDataProvider(HistoricalTimeSeriesSource historicalTimeSeriesSource,
                                               SecuritySource securitySource,
                                               String timeSeriesResolverKey) {
    super(historicalTimeSeriesSource, securitySource, timeSeriesResolverKey);
  }
  
  /**
   * Creates an instance.
   * 
   * @param historicalTimeSeriesSource  the underlying source of historical data, not null
   * @param securitySource  the source of securities, not null
   */
  public LatestHistoricalMarketDataProvider(HistoricalTimeSeriesSource historicalTimeSeriesSource, SecuritySource securitySource) {
    super(historicalTimeSeriesSource, securitySource);
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
    return new HistoricalMarketDataSnapshot(getTimeSeriesSource(), Instant.now(), null,
                                            historicalSpec.getTimeSeriesResolverKey(), this);
  }

}
