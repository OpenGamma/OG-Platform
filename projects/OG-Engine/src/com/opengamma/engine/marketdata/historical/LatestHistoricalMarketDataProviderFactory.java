/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * A factory for {@link LatestHistoricalMarketDataProvider} instances.
 */
public class LatestHistoricalMarketDataProviderFactory implements MarketDataProviderFactory {

  private final HistoricalTimeSeriesSource _timeSeriesSource;
  private final SecuritySource _securitySource;

  public LatestHistoricalMarketDataProviderFactory(final HistoricalTimeSeriesSource timeSeriesSource, SecuritySource securitySource) {
    ArgumentChecker.notNull(timeSeriesSource, "timeSeriesSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _timeSeriesSource = timeSeriesSource;
    _securitySource = securitySource;
  }

  @Override
  public MarketDataProvider create(MarketDataSpecification marketDataSpec) {
    HistoricalMarketDataSpecification historicalMarketDataSpec = (HistoricalMarketDataSpecification) marketDataSpec;
    return new LatestHistoricalMarketDataProvider(getTimeSeriesSource(), getSecuritySource(),
        historicalMarketDataSpec.getTimeSeriesResolverKey(), historicalMarketDataSpec.getTimeSeriesFieldResolverKey());
  }

  private HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }
  
  private SecuritySource getSecuritySource() {
    return _securitySource;
  }

}
