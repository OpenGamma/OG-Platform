/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;

/**
 * A {@link MarketDataAvailabilityFilter} which determines availability based on the value names.
 */
public class ValueNameMarketDataAvailabilityFilter extends AbstractMarketDataAvailabilityFilter {

  /**
   * The set of acceptable market data fields.
   */
  private final Set<String> _validMarketDataRequirementNames;

  /**
   * Creates a provider.
   * 
   * @param validMarketDataRequirementNames the valid market data requirement names, not null
   */
  public ValueNameMarketDataAvailabilityFilter(final Collection<String> validMarketDataRequirementNames) {
    _validMarketDataRequirementNames = ImmutableSet.copyOf(validMarketDataRequirementNames);
  }

  @Override
  public boolean isAvailable(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
    return _validMarketDataRequirementNames.contains(desiredValue.getValueName());
  }

  @Override
  public boolean isAvailable(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
    return _validMarketDataRequirementNames.contains(desiredValue.getValueName());
  }

  @Override
  protected void populateAvailabilityHintKey(final Collection<Serializable> key) {
    key.add(new HashSet<String>(_validMarketDataRequirementNames));
  }

}
