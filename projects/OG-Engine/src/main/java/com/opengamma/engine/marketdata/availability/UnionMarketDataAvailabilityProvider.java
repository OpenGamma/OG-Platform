/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.Collection;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
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
  public UnionMarketDataAvailabilityProvider(final Collection<? extends MarketDataAvailabilityProvider> underlyings) {
    _underlyings = underlyings;
  }

  @Override
  public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    MarketDataNotSatisfiableException missing = null;
    boolean failed = false;
    try {
      for (final MarketDataAvailabilityProvider underlying : _underlyings) {
        try {
          final ValueSpecification result = underlying.getAvailability(targetSpec, target, desiredValue);
          if (result != null) {
            return result;
          }
        } catch (final BlockingOperation e) {
          failed = true;
        }
      }
    } catch (final MarketDataNotSatisfiableException e) {
      missing = e;
    }
    if (failed) {
      // Blocking mode is off, nothing declared AVAILABLE, and at least one wanted to block
      throw BlockingOperation.block();
    } else {
      if (missing != null) {
        throw missing;
      } else {
        return null;
      }
    }
  }

}
