/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Market data availability provider that assumes market values will be available.
 */
public class OptimisticMarketDataAvailabilityProvider extends AbstractMarketDataAvailabilityProvider {

  public OptimisticMarketDataAvailabilityProvider() {
  }

  protected OptimisticMarketDataAvailabilityProvider(final Delegate delegate) {
    super(delegate);
  }

  @Override
  protected MarketDataAvailabilityProvider withDelegate(final Delegate delegate) {
    return new OptimisticMarketDataAvailabilityProvider(delegate);
  }

  @Override
  public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    if (desiredValue.getValueName().startsWith("Market")) {
      return super.getAvailability(targetSpec, target, desiredValue);
    } else {
      return null;
    }
  }

}
