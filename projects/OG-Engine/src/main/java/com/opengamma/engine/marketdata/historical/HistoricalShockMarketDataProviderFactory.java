/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.util.Objects;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.HistoricalShockMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory for creating instances of {@link HistoricalShockMarketDataProvider}.
 */
public class HistoricalShockMarketDataProviderFactory implements MarketDataProviderFactory {

  private final MarketDataProviderResolver _resolver;

  public HistoricalShockMarketDataProviderFactory(MarketDataProviderResolver resolver) {
    ArgumentChecker.notNull(resolver, "resolver");
    _resolver = resolver;
  }

  @Override
  public HistoricalShockMarketDataProvider create(UserPrincipal user, MarketDataSpecification marketDataSpec) {
    if (!(marketDataSpec instanceof HistoricalShockMarketDataSpecification)) {
      throw new IllegalArgumentException("Market data spec must be a HistoricalShockMarketDataSpecification: " + marketDataSpec);
    }
    HistoricalShockMarketDataSpecification shockSpec = (HistoricalShockMarketDataSpecification) marketDataSpec;
    MarketDataProvider provider1 = resolve(user, shockSpec.getHistoricalSpecification1());
    MarketDataProvider provider2 = resolve(user, shockSpec.getHistoricalSpecification2());
    MarketDataProvider baseProvider = resolve(user, shockSpec.getBaseSpecification());
    return new HistoricalShockMarketDataProvider(provider1, provider2, baseProvider);
  }

  private MarketDataProvider resolve(UserPrincipal user, MarketDataSpecification spec) {
    return Objects.requireNonNull(_resolver.resolve(user, spec));
  }
}
