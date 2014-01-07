/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesResolver;
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
  private final HistoricalTimeSeriesResolver _timeSeriesResolver;

  public LatestHistoricalMarketDataProviderFactory(final HistoricalTimeSeriesSource timeSeriesSource, final HistoricalTimeSeriesResolver timeSeriesResolver) {
    ArgumentChecker.notNull(timeSeriesSource, "timeSeriesSource");
    ArgumentChecker.notNull(timeSeriesResolver, "timeSeriesResolver");
    _timeSeriesSource = timeSeriesSource;
    _timeSeriesResolver = timeSeriesResolver;
  }

  @Override
  public MarketDataProvider create(final UserPrincipal marketDataUser, final MarketDataSpecification marketDataSpec) {
    if (marketDataSpec instanceof HistoricalMarketDataSpecification) {
      return new LatestHistoricalMarketDataProvider(_timeSeriesSource, _timeSeriesResolver, ((HistoricalMarketDataSpecification) marketDataSpec).getTimeSeriesResolverKey());
    }
    return new LatestHistoricalMarketDataProvider(_timeSeriesSource, _timeSeriesResolver, null);
  }
}
