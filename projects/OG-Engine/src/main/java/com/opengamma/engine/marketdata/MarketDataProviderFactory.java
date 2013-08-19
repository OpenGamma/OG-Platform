/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicSPI;

/**
 * A factory for {@link MarketDataProvider} instances.
 */
@PublicSPI
public interface MarketDataProviderFactory {

  /**
   * Creates a {@link MarketDataProvider} instance that is compatible with a given specification.
   * 
   * @param marketDataUser the market data user
   * @param marketDataSpec the market data specification, not null
   * @return a market data provider, not null
   */
  MarketDataProvider create(UserPrincipal marketDataUser, MarketDataSpecification marketDataSpec);

}
