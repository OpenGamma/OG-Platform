/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.resolver;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;

/**
 * Implements {@link MarketDataProviderResolver} by using a single {@link MarketDataProviderFactory}.
 */
public class SingleMarketDataProviderResolver implements MarketDataProviderResolver {

  private final MarketDataProviderFactory _providerFactory;
  
  public SingleMarketDataProviderResolver(MarketDataProviderFactory providerFactory) {
    _providerFactory = providerFactory;
  }
  
  @Override
  public MarketDataProvider resolve(MarketDataSpecification marketDataSpec) {
    return _providerFactory.create(marketDataSpec);
  }

}
