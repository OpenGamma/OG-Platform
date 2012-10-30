/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.resolver;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link SingleMarketDataProviderResolver}.
 */
public class SingleMarketDataProviderResolverFactoryBean extends SingletonFactoryBean<SingleMarketDataProviderResolver> {

  private MarketDataProvider _provider;
  
  public MarketDataProvider getProvider() {
    return _provider;
  }

  public void setProvider(MarketDataProvider provider) {
    _provider = provider;
  }

  @Override
  protected SingleMarketDataProviderResolver createObject() {
    ArgumentChecker.notNullInjected(getProvider(), "provider");
    return new SingleMarketDataProviderResolver(getProvider());
  }

}
