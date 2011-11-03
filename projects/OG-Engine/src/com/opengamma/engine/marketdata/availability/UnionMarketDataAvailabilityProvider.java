/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.Collection;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Indicates that market data is available if any of the underlyings claim that it is.
 * If none of the underlying claim availability, but at least one claims a {@link MarketDataAvailability.MISSING}
 * state, the market data is considered missing. Otherwise it is not available.
 */
public class UnionMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  private final Collection<? extends MarketDataAvailabilityProvider> _underlyings;
  
  /**
   * @param underlyings The availability providers to union
   */
  public UnionMarketDataAvailabilityProvider(Collection<? extends MarketDataAvailabilityProvider> underlyings) {
    _underlyings = underlyings;
  }

  @Override
  public MarketDataAvailability getAvailability(ValueRequirement requirement) {
    boolean missing = false;
    for (MarketDataAvailabilityProvider underlying : _underlyings) {
      switch (underlying.getAvailability(requirement)) {
        case AVAILABLE:
          return MarketDataAvailability.AVAILABLE;
        case MISSING:
          missing = true;
          break;
      }
    }
    return missing ? MarketDataAvailability.MISSING : MarketDataAvailability.NOT_AVAILABLE;
  }
}
