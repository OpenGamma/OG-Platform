/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link MarketDataAvailabilityProvider} which 
 */
public class ValueNameMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  /**
   * The computation target type.
   */
  private final ComputationTargetType _targetType;
  /**
   * The set of acceptable market data fields.
   */
  private final Set<String> _validMarketDataRequirementNames;

  /**
   * Creates a provider.
   * 
   * @param validMarketDataRequirementNames  the valid market data requirement names, not null
   */
  public ValueNameMarketDataAvailabilityProvider(Set<String> validMarketDataRequirementNames) {
    this(validMarketDataRequirementNames, null);
  }
  
  /**
   * Creates a provider.
   * 
   * @param targetType  the computation target type, null for any
   * @param validMarketDataRequirementNames  the valid market data requirement names, not null
   */
  public ValueNameMarketDataAvailabilityProvider(Set<String> validMarketDataRequirementNames, ComputationTargetType targetType) {
    ArgumentChecker.notNull(validMarketDataRequirementNames, "validMarketDataRequirementNames");
    _targetType = targetType;
    _validMarketDataRequirementNames = ImmutableSet.copyOf(validMarketDataRequirementNames);
  }
  
  //-------------------------------------------------------------------------
  private ComputationTargetType getTargetType() {
    return _targetType;
  }

  //-------------------------------------------------------------------------
  @Override
  public MarketDataAvailability getAvailability(ValueRequirement requirement) {
    if (getTargetType() != null && requirement.getTargetSpecification().getType() != getTargetType()) {
      return MarketDataAvailability.NOT_AVAILABLE;
    }
    return _validMarketDataRequirementNames.contains(requirement.getValueName()) ? MarketDataAvailability.AVAILABLE : MarketDataAvailability.NOT_AVAILABLE;
  }
  
}
