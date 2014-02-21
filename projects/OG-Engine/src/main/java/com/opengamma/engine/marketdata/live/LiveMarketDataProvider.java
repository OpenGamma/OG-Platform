/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicSPI;

/**
 * Represents a source of live market data from which the engine can obtain a consistent snapshot and receive
 * notifications of changes.
 */
@PublicSPI
public interface LiveMarketDataProvider extends MarketDataProvider {

  /**
   * Indicates whether there is an active subscription (in any state, possibly having failed) on a given specification.
   * 
   * @param specification  the specification, not null
   * @return true if there is an active subscription, false otherwise
   */
  boolean isActive(final ValueSpecification specification);
  
}
