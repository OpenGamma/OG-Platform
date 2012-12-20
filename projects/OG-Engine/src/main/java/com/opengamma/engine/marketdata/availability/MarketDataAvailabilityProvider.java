/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
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
   * Tests whether a data requirement can be satisfied by this availability provider. If the data is available then a suitable value specification to describe it, that satisfies the original value
   * requirement, must be returned. If the value requirement cannot be satisfied by this provider then a return value of null will cause the dependency graph builder to continue its construction. If
   * the requirement must not be satisfied then the method may throw an exception to abort the graph construction.
   * 
   * @param requirement the market data requirement to test, not null
   * @return the satisfying value specification, or null if it cannot be satisfied
   * @throws MarketDataNotSatisfiableException if the requirement must not be satisfied
   */
  ValueSpecification getAvailability(ValueRequirement requirement) throws MarketDataNotSatisfiableException;

}
