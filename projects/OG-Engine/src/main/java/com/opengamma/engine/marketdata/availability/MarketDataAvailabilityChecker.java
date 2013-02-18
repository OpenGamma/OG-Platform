/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Used to obtain the availability status of market data.
 * <p>
 * This availability check may be used as the basis for the {@link MarketDataAvailabilityProvider} implementation that a {@link MarketDataProvider} is coupled with. Most consumers of market data will
 * use a {@code MarketDataAvailabilityProvider} as they will also require value resolution rather than just availability indication.
 */
public interface MarketDataAvailabilityChecker {

  /**
   * Tests whether a data requirement can be satisfied.
   * <p>
   * The target described by the requirement is resolved and described formally as the {@code targetSpec} parameter. This may be null if the target reference could not be resolved. If provided, this
   * specification can be used to construct the returned {@code ValueSpecification}. The resolved form, if the object exists within the system, may be passed as the {@code target} parameter. If the
   * object cannot be resolved to an item of the type indicated by the target specification, a minimum of {@link ExternalBundleIdentifiable}, {@link ExternalIdentifiable} or {@link UniqueIdentifiable}
   * will be passed.
   * <p>
   * The {@code ValueSpecification} returned from this method will be the one used to establish the subscription from a relevant {@link MarketDataProvider} instance. TODO: CHANGE THIS ONE
   * 
   * @param targetSpec the resolved target specification from the data requirement, not null
   * @param target the resolved target the requirement corresponds to, not null (unless the target specification is {@link ComputationTargetSpecification#NULL})
   * @param desiredValue the market data requirement to test, not null
   * @return the satisfying value specification, or null if it cannot be satisfied
   * @throws MarketDataNotSatisfiableException if the requirement must not be satisfied
   */
  ValueSpecification getAvailability(ComputationTargetSpecification targetSpec, Object target, ValueRequirement desiredValue) throws MarketDataNotSatisfiableException;

}
