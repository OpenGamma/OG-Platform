/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.List;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.util.PublicSPI;

/**
 * Factory for {@link MarketDataProvider}s backed by live data.
 */
@PublicSPI
public interface LiveMarketDataProviderFactory extends MarketDataProviderFactory {

  /**
   * Gets the names of the live market data providers.
   * 
   * @return the names of the live market data providers, not null
   */
  List<String> getProviderNames();
  
}
