/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataUtils;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Market data availability provider that assumes market values will be available.
 */
public class OptimisticMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  @Override
  public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    // [PLAT-3044] Do this properly
    return desiredValue.getValueName().startsWith("Market_") ? MarketDataUtils.createMarketDataValue(desiredValue, MarketDataUtils.DEFAULT_EXTERNAL_ID) : null;
  }

}
