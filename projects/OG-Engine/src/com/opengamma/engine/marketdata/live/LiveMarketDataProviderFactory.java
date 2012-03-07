/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;

/**
 * Live data-source-based market data provider factory. 
 */
public class LiveMarketDataProviderFactory implements MarketDataProviderFactory {

  private final MarketDataProvider _defaultProvider;
  private final Map<String, MarketDataProvider> _sourceToProviderMap;
  
  public LiveMarketDataProviderFactory(MarketDataProvider defaultProvider, Map<String, MarketDataProvider> sourceToProviderMap) {
    _defaultProvider = defaultProvider;
    _sourceToProviderMap = ImmutableMap.copyOf(sourceToProviderMap);
  }
  
  @Override
  public MarketDataProvider create(MarketDataSpecification marketDataSpec) {
    LiveMarketDataSpecification liveSpec = (LiveMarketDataSpecification) marketDataSpec;
    if (liveSpec.getDataSource() == null) {
      return _defaultProvider;
    }
    MarketDataProvider provider = _sourceToProviderMap.get(liveSpec.getDataSource());
    if (provider == null) {
      throw new IllegalArgumentException("No provider found for data source name '" + liveSpec.getDataSource() + "'");
    }
    return provider;
  }

}
