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

  private final MarketDataProviderResolver _providerResolver;

  public CompositeMarketDataProviderFactory(MarketDataProviderResolver resolver) {
    _providerResolver = resolver;
  }

  public CompositeMarketDataProvider create(UserPrincipal user, List<MarketDataSpecification> specs) {
    throw new UnsupportedOperationException();
  }
}
