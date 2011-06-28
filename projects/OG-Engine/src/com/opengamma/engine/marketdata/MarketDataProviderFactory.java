/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;

/**
 * A factory for {@link MarketDataProvider} instances.
 */
public interface MarketDataProviderFactory {

  /**
   * Creates a {@link MarketDataProvider} instance that is compatible with a given specification.
   * 
   * @param marketDataSpec  the market data specification, not {@code null}
   * @return a market data provider, not {@code null}
   */
  MarketDataProvider create(MarketDataSpecification marketDataSpec);
  
}
