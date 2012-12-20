/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.resolver;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;

/**
 * Resolves a {@link MarketDataSpecification} into a {@link MarketDataProvider} by specification type.
 */
public class TypeBasedMarketDataProviderResolver implements MarketDataProviderResolver {

  private final Map<Class<?>, MarketDataProviderFactory> _providerFactoryMap = new HashMap<Class<?>, MarketDataProviderFactory>();
  
  public void addProvider(Class<?> marketDataSpecType, MarketDataProviderFactory provider) {
    _providerFactoryMap.put(marketDataSpecType, provider);
  }
  
  @Override
  public MarketDataProvider resolve(UserPrincipal user, MarketDataSpecification marketDataSpec) {
    MarketDataProviderFactory providerFactory = _providerFactoryMap.get(marketDataSpec.getClass());
    if (providerFactory == null) {
      return null;
    }
    return providerFactory.create(user, marketDataSpec);
  }

}
