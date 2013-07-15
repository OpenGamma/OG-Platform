/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicSPI;

/**
 * Provides mutator methods for live data, allowing customisation of live data.
 */
@PublicSPI
public interface MarketDataInjector {

  /**
   * Injects a live data value by {@link ValueRequirement}. The requirement will be resolved using the same logic as the related {@link MarketDataAvailabilityProvider} to determine the
   * {@link ValueSpecification} that describes the injected data.
   *
   * @param valueRequirement the value requirement, not null
   * @param value the value to add
   */
  void addValue(ValueRequirement valueRequirement, Object value);

  /**
   * Injects a live data value by {@link ValueSpecification}.
   *
   * @param valueSpecification the value specification, not null
   * @param value the value to add
   */
  void addValue(ValueSpecification valueSpecification, Object value);

  /**
   * Removes a previously-added live data value by {@link ValueRequirement}. The requirement will be resolved using the same logic as the related {@link MarketDataAvailabilityProvider} to determine
   * the {@link ValueSpecification} that describes the injected data to be removed. It does not need to be the same as the original value requirement that added the data, as long as it resolves to the
   * same value specification.
   *
   * @param valueRequirement the value requirement, not null
   */
  void removeValue(ValueRequirement valueRequirement);

  /**
   * Removes a previously-added live data value by {@link ValueSpecification}.
   *
   * @param valueSpecification the value specification, not null
   */
  void removeValue(ValueSpecification valueSpecification);

}
