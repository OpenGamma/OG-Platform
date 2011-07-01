/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * Implements {@link MarketDataAvailabilityProvider} around a fixed set of available market data items.
 */
public class FixedMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {
  private final Set<ValueRequirement> _availableRequirements = new HashSet<ValueRequirement>();

  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    return _availableRequirements.contains(requirement);
  }
  
  public void addRequirement(ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "Value requirement");
    _availableRequirements.add(requirement);
  }

}
