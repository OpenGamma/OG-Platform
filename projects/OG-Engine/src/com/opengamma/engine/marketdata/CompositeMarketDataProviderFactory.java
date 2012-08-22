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
 * Factory for creatoing instances of {@link CombinedMarketDataProvider}.
 */
public class CompositeMarketDataProviderFactory {

  private final MarketDataProviderResolver _resolver;

  /**
   * @param resolver For resolving {@link MarketDataSpecification}s into {@link MarketDataProvider}s.
   */
  public CompositeMarketDataProviderFactory(MarketDataProviderResolver resolver) {
    _resolver = resolver;
  }

  /**
   * Creates a new provider which aggregates the data from the specified underlying providers.
   * @param user The user requesting the data
   * @param specs Specifications of the underlying providers in priority order
   * @return A new provider which aggregates the data from the underlying providers
   */
  public CompositeMarketDataProvider create(UserPrincipal user, List<MarketDataSpecification> specs) {
    // TODO should this method do the resolution and pass in the MarketDataProviders?
    return new CompositeMarketDataProvider(user, specs, _resolver);
  }
}
