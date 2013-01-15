/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.marketdata.MarketDataUtils;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Implements {@link MarketDataAvailabilityProvider} around a fixed set of available market data items.
 */
public class FixedMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  private final Set<ValueRequirement> _missing = new HashSet<ValueRequirement>();
  private final Map<ValueRequirement, ValueSpecification> _requirements = new HashMap<ValueRequirement, ValueSpecification>();

  @Override
  public synchronized ValueSpecification getAvailability(final ValueRequirement requirement) {
    if (_missing.contains(requirement)) {
      throw new MarketDataNotSatisfiableException(requirement);
    }
    return _requirements.get(requirement);
  }

  public synchronized void addAvailableRequirement(final ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "requirement");
    _requirements.put(requirement, MarketDataUtils.createMarketDataValue(requirement, MarketDataUtils.DEFAULT_EXTERNAL_ID));
  }
  
  public synchronized void addMissingRequirement(final ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "requirement");
    _missing.add(requirement);
  }

}
