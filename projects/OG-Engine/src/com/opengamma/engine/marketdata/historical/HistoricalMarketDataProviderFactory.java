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
import com.opengamma.util.ArgumentChecker;

/**
 * A factory for {@link HistoricalMarketDataProvider} instances.
 */
public class HistoricalMarketDataProviderFactory implements MarketDataProviderFactory {

  private final HistoricalTimeSeriesSource _timeSeriesSource;
  private final HistoricalMarketDataFieldResolver _fieldResolver;
  private final HistoricalMarketDataNormalizer _normalizer;

  public HistoricalMarketDataProviderFactory(final HistoricalTimeSeriesSource timeSeriesSource, final HistoricalMarketDataFieldResolver fieldResolver,
      final HistoricalMarketDataNormalizer normalizer) {
    ArgumentChecker.notNull(timeSeriesSource, "timeSeriesSource");
    ArgumentChecker.notNull(fieldResolver, "fieldResolver");
    ArgumentChecker.notNull(normalizer, "normalizer");
    _timeSeriesSource = timeSeriesSource;
    _fieldResolver = fieldResolver;
    _normalizer = normalizer;
  }

  @Override
  public MarketDataProvider create(MarketDataSpecification marketDataSpec) {
    HistoricalMarketDataSpecification historicalMarketDataSpec = (HistoricalMarketDataSpecification) marketDataSpec;
    return new HistoricalMarketDataProvider(getTimeSeriesSource(), historicalMarketDataSpec.getTimeSeriesResolverKey(), getFieldResolver(), historicalMarketDataSpec.getTimeSeriesFieldResolverKey(),
        getNormalizer());
  }

  private HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }

  private HistoricalMarketDataFieldResolver getFieldResolver() {
    return _fieldResolver;
  }

  private HistoricalMarketDataNormalizer getNormalizer() {
    return _normalizer;
  }

}
