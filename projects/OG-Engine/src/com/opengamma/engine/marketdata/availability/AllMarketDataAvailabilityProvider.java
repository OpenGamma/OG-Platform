/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Implementation of {@link MarketDataAvailabilityProvider} which always returns a positive result.
 */
public class AllMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    return true;
  }

}
