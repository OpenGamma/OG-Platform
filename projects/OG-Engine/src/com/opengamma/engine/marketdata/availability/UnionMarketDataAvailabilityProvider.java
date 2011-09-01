/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.Arrays;
import java.util.Collection;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Indicates that market data is available if any of the underlyings claim that it is
 */
public class UnionMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  private final Collection<? extends MarketDataAvailabilityProvider> _underlyings;
  
  
  /**
   * @param underlyings The availability providers to union
   */
  public UnionMarketDataAvailabilityProvider(MarketDataAvailabilityProvider... underlyings) {
    _underlyings = Arrays.asList(underlyings);
  }
  /**
   * @param underlyings The availability providers to union
   */
  public UnionMarketDataAvailabilityProvider(Collection<? extends MarketDataAvailabilityProvider> underlyings) {
    super();
    _underlyings = underlyings;
  }

  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    for (MarketDataAvailabilityProvider underlying : _underlyings) {
      if (underlying.isAvailable(requirement)) {
        return true;
      }
    }
    return false;
  }
}
