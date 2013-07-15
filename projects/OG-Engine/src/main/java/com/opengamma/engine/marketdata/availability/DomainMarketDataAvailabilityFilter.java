/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Implements {@link MarketDataAvailabilityFilter} by providing an indication of whether an item of market data is <i>likely</i> to be available based on the scheme of its external identifiers and
 * other factors.
 */
public class DomainMarketDataAvailabilityFilter extends ValueNameMarketDataAvailabilityFilter {

  /**
   * The set of acceptable schemes.
   */
  private final Set<ExternalScheme> _acceptableSchemes;

  /**
   * Creates a provider.
   * 
   * @param acceptableSchemes the acceptable schemes, not null
   * @param validMarketDataRequirementNames the valid market data requirement names, not null
   */
  public DomainMarketDataAvailabilityFilter(final Collection<ExternalScheme> acceptableSchemes, final Collection<String> validMarketDataRequirementNames) {
    super(validMarketDataRequirementNames);
    ArgumentChecker.notNull(acceptableSchemes, "acceptableSchemes");
    _acceptableSchemes = ImmutableSet.copyOf(acceptableSchemes);
  }

  @Override
  public boolean isAvailable(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
    return _acceptableSchemes.contains(identifier.getScheme()) && super.isAvailable(targetSpec, identifier, desiredValue);
  }

  @Override
  public boolean isAvailable(final ComputationTargetSpecification targetSpec, final UniqueId identifier, final ValueRequirement desiredValue) {
    // There is no external identifier, so the scheme can never match
    return false;
  }

}
