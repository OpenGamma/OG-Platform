/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.io.Serializable;
import java.util.Collection;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;

/**
 * Market data availability checker that assumes market values will be available.
 */
public class OptimisticMarketDataAvailabilityFilter extends AbstractMarketDataAvailabilityFilter {

  @Override
  public boolean isAvailable(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
    return desiredValue.getValueName().startsWith("Market");
  }

  @Override
  public boolean isAvailable(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
    return desiredValue.getValueName().startsWith("Market");
  }

  @Override
  protected void populateAvailabilityHintKey(final Collection<Serializable> key) {
    // No extra parameters
  }

}
