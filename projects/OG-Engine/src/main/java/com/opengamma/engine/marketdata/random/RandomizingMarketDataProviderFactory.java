/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.random;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.RandomizingMarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory for instances of {@link RandomizingMarketDataProvider}.
 */
public class RandomizingMarketDataProviderFactory implements MarketDataProviderFactory {

  private final MarketDataProviderResolver _resolver;

  /**
   * @param resolver For resolving the underlying market data provider
   */
  public RandomizingMarketDataProviderFactory(MarketDataProviderResolver resolver) {
    ArgumentChecker.notNull(resolver, "resolver");
    _resolver = resolver;
  }

  @Override
  public RandomizingMarketDataProvider create(UserPrincipal marketDataUser, MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof RandomizingMarketDataSpecification)) {
      throw new IllegalArgumentException("Spec must be a RandomizingMarketDataSpecification but is " + marketDataSpec);
    }
    RandomizingMarketDataSpecification randomSpec = (RandomizingMarketDataSpecification) marketDataSpec;
    MarketDataProvider underlying = _resolver.resolve(marketDataUser, randomSpec.getUnderlying());
    return new RandomizingMarketDataProvider(randomSpec, underlying);
  }
}
