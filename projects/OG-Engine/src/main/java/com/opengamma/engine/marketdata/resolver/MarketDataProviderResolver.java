/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.resolver;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicSPI;

/**
 * Resolver for {@link MarketDataProvider}s
 */
@PublicSPI
public interface MarketDataProviderResolver {

  /**
   * Resolves a {@link MarketDataSpecification} into a {@link MarketDataProvider} which is able to work with the
   * specification.
   * 
   * @param marketDataUser the market data user any subscriptions should be made from
   * @param snapshotSpec the snapshot specification for which a provider is required, not null
   * @return the resolved snapshot provider or null if a provider can't be resolved
   */
  MarketDataProvider resolve(UserPrincipal marketDataUser, MarketDataSpecification snapshotSpec);

}
