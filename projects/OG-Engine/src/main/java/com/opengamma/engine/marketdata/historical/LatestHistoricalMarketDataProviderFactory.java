/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * A factory for {@link LatestHistoricalMarketDataProvider} instances.
 */
public class LatestHistoricalMarketDataProviderFactory implements MarketDataProviderFactory {

  private final HistoricalTimeSeriesSource _timeSeriesSource;

  public LatestHistoricalMarketDataProviderFactory(final HistoricalTimeSeriesSource timeSeriesSource) {
    ArgumentChecker.notNull(timeSeriesSource, "timeSeriesSource");
    _timeSeriesSource = timeSeriesSource;
  }

  @Override
  public MarketDataProvider create(final UserPrincipal marketDataUser, final MarketDataSpecification marketDataSpec) {
    final HistoricalMarketDataSpecification historicalMarketDataSpec = (HistoricalMarketDataSpecification) marketDataSpec;
    return new LatestHistoricalMarketDataProvider(_timeSeriesSource, historicalMarketDataSpec.getTimeSeriesResolverKey());
  }
}
