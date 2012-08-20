/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.List;

import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;

/**
 *
 */
public class CompositeMarketDataProviderFactory {

  private final MarketDataProviderResolver _resolver;

  public CompositeMarketDataProviderFactory(MarketDataProviderResolver resolver) {
    _resolver = resolver;
  }

  public CompositeMarketDataProvider create(UserPrincipal user, List<MarketDataSpecification> specs) {
    // TODO should this method do the resolution and pass in the MarketDataProviders?
    return new CompositeMarketDataProvider(user, specs, _resolver);
  }
}
