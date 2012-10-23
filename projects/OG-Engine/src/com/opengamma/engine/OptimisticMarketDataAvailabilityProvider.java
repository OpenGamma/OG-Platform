/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine;

import com.opengamma.engine.marketdata.MarketDataUtils;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Market data availability provider that assumes market values will be available.
 */
public class OptimisticMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  @Override
  public ValueSpecification getAvailability(final ValueRequirement requirement) {
    return requirement.getValueName().startsWith("Market_") ? MarketDataUtils.createMarketDataValue(requirement, MarketDataUtils.DEFAULT_EXTERNAL_ID) : null;
  }

}
