/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * Market data availability provider that assumes market values will be available.
 */
public class OptimisticMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    return MarketDataRequirementNames.MARKET_VALUE.equals(requirement.getValueName());
  }

}
