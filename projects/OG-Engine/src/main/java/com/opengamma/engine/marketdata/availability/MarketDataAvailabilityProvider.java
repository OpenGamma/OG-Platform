/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.io.Serializable;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicSPI;

/**
 * Used to obtain the availability status of market data for a given {@link MarketDataProvider} instance.
 * <p>
 * For example, in dependency graph building, this is used to decide whether a requirement can be satisfied directly from market data, or whether a function is required to produce the requirement.
 */
@PublicSPI
public interface MarketDataAvailabilityProvider {

  /**
   * Tests whether a data requirement can be satisfied by this availability provider. If the data is available then a suitable value specification to describe it, that satisfies the original value
   * requirement, must be returned. If the value requirement cannot be satisfied by this provider then a return value of null will cause the dependency graph builder to continue its construction. If
   * the requirement must not be satisfied then the method may throw an exception to abort the graph construction.
   * <p>
   * The target described by the requirement is resolved and described formally as the {@code targetSpec} parameter. This may be null if the target reference could not be resolved. If provided, this
   * specification can be used to construct the returned {@code ValueSpecification}. The resolved form, if the object exists within the system, may be passed as the {@code target} parameter. If the
   * object cannot be resolved to an item of the type indicated by the target specification, a minimum of {@link ExternalBundleIdentifiable}, {@link ExternalIdentifiable} or {@link UniqueIdentifiable}
   * will be passed.
   * <p>
   * The {@code ValueSpecification} returned from this method will be the one used to establish the subscription from a relevant {@link MarketDataProvider} instance.
   * 
   * @param targetSpec the resolved target specification from the data requirement, null if the requirement target could not be resolved
   * @param target the resolved target the requirement corresponds to, not null (unless the target specification is {@link ComputationTargetSpecification#NULL})
   * @param desiredValue the market data requirement to test, not null
   * @return the satisfying value specification, or null if it cannot be satisfied
   * @throws MarketDataNotSatisfiableException if the requirement must not be satisfied
   */
  ValueSpecification getAvailability(ComputationTargetSpecification targetSpec, Object target, ValueRequirement desiredValue) throws MarketDataNotSatisfiableException;

  /**
   * Returns an associated (or equivalent) {@link MarketDataAvailabilityFilter} for a possibly cheaper data requirement test or to expose any externally configured rules for determining availability.
   * 
   * @return a {@link MarketDataAvailabilityFilter} instance.
   */
  MarketDataAvailabilityFilter getAvailabilityFilter();

  /**
   * Returns an opaque descriptor that approximately describes the availability. This is intended to support the caching of data that has been derived from the availability returned by a market data
   * provider.
   * <p>
   * For example if there is a cached dependency graph build based on the results of one availability provider, then it may be possible to take that as the seed for an incremental build, or share the
   * whole graph, when another provider that returns the same descriptor is used. Note that this is only an approximate indication; any resolutions must be tested to confirm that the cached object is
   * valid for the new provider.
   * 
   * @return a key that could be used for caching objects derived from this provider's availability
   */
  Serializable getAvailabilityHintKey();

}
