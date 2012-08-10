/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.resolver;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderWithOverride;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link MarketDataProviderResolver} which delegates resolution to an underlying provider, but wraps any resolved
 * provider in a {@link MarketDataProviderResolverWithOverride}.
 */
public class MarketDataProviderResolverWithOverride implements MarketDataProviderResolver {

  private final MarketDataProviderResolver _underlying;
  private final MarketDataProvider _overrideProvider;
  
  public MarketDataProviderResolverWithOverride(MarketDataProviderResolver underlying, MarketDataProvider overrideProvider) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(overrideProvider, "overrideProvider");
    _underlying = underlying;
    _overrideProvider = overrideProvider;
  }
  
  @Override
  public MarketDataProvider resolve(UserPrincipal user, MarketDataSpecification marketDataSpec) {
    MarketDataProvider provider = _underlying.resolve(user, marketDataSpec);
    if (provider == null) {
      return null;
    }
    return new MarketDataProviderWithOverride(provider, _overrideProvider);
  }
}
