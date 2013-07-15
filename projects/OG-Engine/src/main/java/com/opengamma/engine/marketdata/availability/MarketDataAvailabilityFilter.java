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
import com.opengamma.util.PublicSPI;

/**
 * Used to obtain the availability status of market data.
 * <p>
 * This availability check may be external to a {@link MarketDataProvider} implementation to allow arbitrary filtering rules to be configured independently to the data connection. It may then be used
 * as the basis to create a more closely coupled {@link MarketDataAvailabilityProvider} using the {@link #withProvider} method. This is intended for additional configuration of a
 * {@code MarketDataProvider} instance. Most consumers of market data will use a {@code MarketDataAvailabilityProvider} as they will also require value resolution rather than just availability
 * indication.
 */
@PublicSPI
public interface MarketDataAvailabilityFilter {

  /**
   * Tests whether a data requirement can be satisfied.
   * <p>
   * The target described by the requirement is resolved and described formally as the {@code targetSpec} parameter. This may be null if the target reference could not be resolved. The resolved form,
   * if the object exists within the system, may be passed as the {@code target} parameter. If the object cannot be resolved to an item of the type indicated by the target specification, a minimum of
   * {@link ExternalBundleIdentifiable}, {@link ExternalIdentifiable} or {@link UniqueIdentifiable} will be passed.
   * <p>
   * If this returns true the related {@link MarketDataAvailabilityProvider} instance would return a suitable {@link ValueSpecification} for the data provider.
   * 
   * @param targetSpec the resolved target specification from the data requirement, not null
   * @param target the resolved target the requirement corresponds to, not null (unless the target specification is {@link ComputationTargetSpecification#NULL})
   * @param desiredValue the market data requirement to test, not null
   * @return true if the value can be satisfied, false otherwise
   * @throws MarketDataNotSatisfiableException if the requirement must not be satisfied
   */
  boolean isAvailable(ComputationTargetSpecification targetSpec, Object target, ValueRequirement desiredValue) throws MarketDataNotSatisfiableException;

  /**
   * Combines this filter with a {@link MarketDataAvailabilityProvider} to produce resolved value specifications for items that would pass the filter.
   * <p>
   * Typically this will be used by a {@link MarketDataProvider} that has been initialized with an externally provided availability filter. This should be used in place of a provider just calling
   * {@link #isAvailable} on the filter as the filter may need to perform conversion of the resolution parameters before they are passed to the underlying provider instance.
   * 
   * @param provider the base provider that can construct value specifications corresponding to the {@code MarketDataProvider}
   * @return a {@link MarketDataAvailabilityProvider} instance
   */
  MarketDataAvailabilityProvider withProvider(MarketDataAvailabilityProvider provider);

}
