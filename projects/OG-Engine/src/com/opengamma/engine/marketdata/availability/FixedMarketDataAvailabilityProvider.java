/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * Implements {@link MarketDataAvailabilityProvider} around a fixed set of available market data items.
 */
public class FixedMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  private final Map<ValueRequirement, MarketDataAvailability> _requirements = new HashMap<ValueRequirement, MarketDataAvailability>();

  @Override
  public MarketDataAvailability getAvailability(ValueRequirement requirement) {
    final MarketDataAvailability availability = _requirements.get(requirement);
    if (availability == null) {
      return MarketDataAvailability.NOT_AVAILABLE;
    }
    return availability;
  }

  public void addAvailableRequirement(final ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "requirement");
    _requirements.put(requirement, MarketDataAvailability.AVAILABLE);
  }
  
  public void addMissingRequirement(final ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "requirement");
    _requirements.put(requirement, MarketDataAvailability.MISSING);
  }

}
