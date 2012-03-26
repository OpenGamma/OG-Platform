/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Market data availability provider that assumes market values will be available.
 */
public class OptimisticMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  @Override
  public MarketDataAvailability getAvailability(final ValueRequirement requirement) {
    return requirement.getValueName().startsWith("Market_") ? MarketDataAvailability.AVAILABLE : MarketDataAvailability.NOT_AVAILABLE;
  }

}
