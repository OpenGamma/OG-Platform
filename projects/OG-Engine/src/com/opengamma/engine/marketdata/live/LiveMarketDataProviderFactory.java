/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;

/**
 * Creates Live MarketDataProvider based on the dataSource
 */
public class LiveMarketDataProviderFactory implements MarketDataProviderFactory, LiveMarketDataSourceRegistry {
  //TODO PLAT-1080 - configure this from the live data service  
  
  private final Map<String, MarketDataProvider> _providersByDataSource;

  
  /**
   * @param providersByDataSource The provider to use for each data source
   */
  public LiveMarketDataProviderFactory(Map<String, MarketDataProvider> providersByDataSource) {
    super();
    this._providersByDataSource = providersByDataSource;
  }

  @Override
  public MarketDataProvider create(MarketDataSpecification marketDataSpec) {
    LiveMarketDataSpecification liveSpec = (LiveMarketDataSpecification) marketDataSpec;
    MarketDataProvider provider = _providersByDataSource.get(liveSpec.getDataSource());
    if (provider == null) {
      throw new IllegalArgumentException("No provider available for " + liveSpec.getDataSource());
    }
    return provider;
  }

  @Override
  public Collection<String> getDataSources() {
    return new ArrayList<String>(_providersByDataSource.keySet());
  }
}
