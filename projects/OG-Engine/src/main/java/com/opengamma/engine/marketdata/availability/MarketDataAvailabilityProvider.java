/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalBundleIdentifiable;
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
   * <p>
   * The target described by the requirement is resolved and described formally as the {@code targetSpec} parameter. This specification should normally be used to construct the returned
   * {@code ValueSpecification}. The resolved form, if the object exists within the system, may be passed as the {@code target} parameter. If the object cannot be resolved to an item of the type
   * indicated by the target specification, a minimum of {@link ExternalBundleIdentifiable} will be passed.
   * 
   * @param targetSpec the resolved target specification from the data requirement, not null
   * @param target the resolved target the requirement corresponds to, not null
   * @param desiredValue the market data requirement to test, not null
   * @return the satisfying value specification, or null if it cannot be satisfied
   * @throws MarketDataNotSatisfiableException if the requirement must not be satisfied
   */
  ValueSpecification getAvailability(ComputationTargetSpecification targetSpec, Object target, ValueRequirement desiredValue) throws MarketDataNotSatisfiableException;

}
