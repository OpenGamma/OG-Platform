/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.Collection;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.async.BlockingOperation;

/**
 * Indicates that market data is available if any of the underlyings claim that it is. If none of the underlying claim availability, but at least one claims a {@link MarketDataAvailability.MISSING}
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
  public MarketDataAvailability getAvailability(final ValueRequirement requirement) {
    boolean missing = false;
    boolean failed = false;
    for (MarketDataAvailabilityProvider underlying : _underlyings) {
      try {
        switch (underlying.getAvailability(requirement)) {
          case AVAILABLE:
            return MarketDataAvailability.AVAILABLE;
          case MISSING:
            missing = true;
            break;
        }
      } catch (BlockingOperation e) {
        failed = true;
      }
    }
    if (failed) {
      // Blocking mode is off, nothing declared AVAILABLE, and at least one wanted to block
      throw BlockingOperation.block();
    } else {
      return missing ? MarketDataAvailability.MISSING : MarketDataAvailability.NOT_AVAILABLE;
    }
  }

}
