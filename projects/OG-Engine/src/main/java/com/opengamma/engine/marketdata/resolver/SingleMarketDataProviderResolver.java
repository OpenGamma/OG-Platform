/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.resolver;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.SingletonMarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;

/**
 * Implements {@link MarketDataProviderResolver} by using a single {@link MarketDataProviderFactory}.
 */
public class SingleMarketDataProviderResolver implements MarketDataProviderResolver {

  private final MarketDataProviderFactory _providerFactory;
  
  public SingleMarketDataProviderResolver(MarketDataProvider provider) {
    this(new SingletonMarketDataProviderFactory(provider));
  }
  
  public SingleMarketDataProviderResolver(MarketDataProviderFactory providerFactory) {
    _providerFactory = providerFactory;
  }
  
  @Override
  public MarketDataProvider resolve(UserPrincipal user, MarketDataSpecification marketDataSpec) {
    return _providerFactory.create(user, marketDataSpec);
  }

}
