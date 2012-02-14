/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link CombinedMarketDataProviderFactory}.
 */
public class CombinedMarketDataProviderFactoryFactoryBean extends SingletonFactoryBean<CombinedMarketDataProviderFactory> {

  private MarketDataProviderResolver _underlying;
  
  public MarketDataProviderResolver getUnderlying() {
    return _underlying;
  }
  
  public void setUnderlying(MarketDataProviderResolver underlying) {
    _underlying = underlying;
  }
  
  @Override
  protected CombinedMarketDataProviderFactory createObject() {
    CombinedMarketDataProviderFactory factory = new CombinedMarketDataProviderFactory();
    factory.setUnderlying(getUnderlying());
    return factory;
  }

}
