/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link MarketDataProviderFactory} implementation which returns a singleton instance.
 */
public class SingletonMarketDataProviderFactory implements MarketDataProviderFactory {

  private final MarketDataProvider _instance;
  
  public SingletonMarketDataProviderFactory(MarketDataProvider instance) {
    ArgumentChecker.notNull(instance, "instance");
    _instance = instance;
  }

  @Override
  public MarketDataProvider create(UserPrincipal marketDataUser,
                                   MarketDataSpecification marketDataSpec) {
    return _instance;
  }
  
}
