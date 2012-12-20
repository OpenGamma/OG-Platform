/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataNotSatisfiableException;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicAPI;

/**
 * Helper methods for working with the market data interfaces.
 */
@PublicAPI
public class MarketDataUtils {

  /**
   * Tests whether the requirement can be satisfied by the availability provider.
   * 
   * @param provider the provider to test, not null
   * @param requirement the requirement to test, not null
   * @return true if the requirement can be satisfied by the provider, false otherwise
   */
  public static boolean isAvailable(final MarketDataAvailabilityProvider provider, final ValueRequirement requirement) {
    try {
      return provider.getAvailability(requirement) != null;
    } catch (MarketDataNotSatisfiableException e) {
      return false;
    }
  }

  /**
   * Tests whether the requirement can be satisfied by the availability provider.
   * 
   * @param provider the provider to test, not null
   * @param requirement the requirement to test, not null
   * @return one of the three availability states - see {@link MarketDataAvailability} for more details, not null
   */
  public static MarketDataAvailability getAvailability(final MarketDataAvailabilityProvider provider, final ValueRequirement requirement) {
    try {
      return (provider.getAvailability(requirement) != null) ? MarketDataAvailability.AVAILABLE : MarketDataAvailability.NOT_AVAILABLE;
    } catch (MarketDataNotSatisfiableException e) {
      return MarketDataAvailability.MISSING;
    }
  }

  /**
   * Creates a specification that can be returned as a result by a data provider that satisfies the given requirement.
   * 
   * @param requirement the requirement that has been satisfied, not null
   * @return a satisfying value specification
   */
  public static ValueSpecification createMarketDataValue(final ValueRequirement requirement) {
    return createMarketDataValue(requirement.getValueName(), requirement.getTargetSpecification(), requirement.getConstraints());
  }

  /**
   * Creates a specification that can be returned as a result by a data provider that satisfies the given requirement.
   * 
   * @param valueName the value name that is satisfied, not null
   * @param target the computation target, not null
   * @return a satisfying value specification
   */
  public static ValueSpecification createMarketDataValue(final String valueName, final ComputationTargetSpecification target) {
    return createMarketDataValue(valueName, target, ValueProperties.none());
  }

  /**
   * Creates a specification that can be returned as a result by a data provider that satisfies the given requirement.
   * 
   * @param valueName the value name that is satisfied, not null
   * @param target the computation target, not null
   * @param properties the properties of the satisfying result, not null
   * @return a satisfying value specification
   */
  public static ValueSpecification createMarketDataValue(final String valueName, final ComputationTargetSpecification target, final ValueProperties properties) {
    return new ValueSpecification(valueName, target, properties.copy().with(ValuePropertyNames.FUNCTION, MarketDataSourcingFunction.UNIQUE_ID).get());
  }

}
