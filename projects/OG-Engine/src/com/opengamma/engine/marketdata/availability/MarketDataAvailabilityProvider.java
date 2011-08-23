/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.PublicSPI;

/**
 * Used to obtain the availability status of market data.
 * <p>
 * For example, in dependency graph building, this is used to decide whether a requirement can be
 * satisfied directly from market data, or whether a function is required to produce the requirement.
 */
@PublicSPI
public interface MarketDataAvailabilityProvider {

  /**
   * Gets whether an item of market data is available from the owner of this availability provider.
   * 
   * @param requirement  the market data requirement, not null
   * @return true if the item of market data is available, false otherwise
   */
  boolean isAvailable(ValueRequirement requirement);

}
