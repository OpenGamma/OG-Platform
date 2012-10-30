/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.CombinedMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * A factory for {@link CombinedMarketDataProvider} instances.
 */
public class CombinedMarketDataProviderFactory implements MarketDataProviderFactory {

  private MarketDataProviderResolver _underlying;
  
  public CombinedMarketDataProviderFactory() {
  }
  
  public CombinedMarketDataProviderFactory(MarketDataProviderResolver underlyingResolver) {
    setUnderlying(underlyingResolver);
  }
  
  @Override
  public MarketDataProvider create(UserPrincipal user, MarketDataSpecification marketDataSpec) {
    ArgumentChecker.notNullInjected(_underlying, "underlying");
    CombinedMarketDataSpecification combinedMarketDataSpec = (CombinedMarketDataSpecification) marketDataSpec;
    MarketDataProvider preferred = getUnderlying().resolve(user, combinedMarketDataSpec.getPreferredSpecification());
    MarketDataProvider fallBack = getUnderlying().resolve(user, combinedMarketDataSpec.getFallbackSpecification());
    return new CombinedMarketDataProvider(preferred, fallBack);
  }
  
  //-------------------------------------------------------------------------
  private MarketDataProviderResolver getUnderlying() {
    return _underlying;
  }

  public void setUnderlying(MarketDataProviderResolver underlying) {
    _underlying = underlying;
  }
  
}
