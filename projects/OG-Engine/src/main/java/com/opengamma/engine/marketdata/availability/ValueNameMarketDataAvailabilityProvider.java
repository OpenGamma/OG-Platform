/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * A {@link MarketDataAvailabilityProvider} which determines availability based on the value names.
 */
public class ValueNameMarketDataAvailabilityProvider extends AbstractMarketDataAvailabilityProvider {

  /**
   * The set of acceptable market data fields.
   */
  private final Set<String> _validMarketDataRequirementNames;

  /**
   * Creates a provider.
   *
   * @param validMarketDataRequirementNames  the valid market data requirement names, not null
   */
  public ValueNameMarketDataAvailabilityProvider(final Collection<String> validMarketDataRequirementNames) {
    _validMarketDataRequirementNames = ImmutableSet.copyOf(validMarketDataRequirementNames);
  }

  protected ValueNameMarketDataAvailabilityProvider(final Delegate delegate, final ValueNameMarketDataAvailabilityProvider copyFrom) {
    super(delegate);
    _validMarketDataRequirementNames = copyFrom._validMarketDataRequirementNames;
  }

  @Override
  protected MarketDataAvailabilityProvider withDelegate(final Delegate delegate) {
    return new ValueNameMarketDataAvailabilityProvider(delegate, this);
  }

  @Override
  public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    if (_validMarketDataRequirementNames.contains(desiredValue.getValueName())) {
      return super.getAvailability(targetSpec, target, desiredValue);
    } else {
      return null;
    }
  }

}
