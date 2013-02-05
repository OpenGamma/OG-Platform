/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;

/**
 * Implements {@link MarketDataAvailabilityProvider} by providing an indication of whether an item of market data is <i>likely</i> to be available based on the scheme of its external identifiers and
 * other factors.
 */
public class DomainMarketDataAvailabilityProvider extends ValueNameMarketDataAvailabilityProvider {

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
  public DomainMarketDataAvailabilityProvider(final Collection<ExternalScheme> acceptableSchemes, final Collection<String> validMarketDataRequirementNames) {
    super(validMarketDataRequirementNames);
    ArgumentChecker.notNull(acceptableSchemes, "acceptableSchemes");
    _acceptableSchemes = ImmutableSet.copyOf(acceptableSchemes);
  }

  protected DomainMarketDataAvailabilityProvider(final Delegate delegate, final DomainMarketDataAvailabilityProvider copyFrom) {
    super(delegate, copyFrom);
    _acceptableSchemes = copyFrom._acceptableSchemes;
  }

  @Override
  protected MarketDataAvailabilityProvider withDelegate(final Delegate delegate) {
    return new DomainMarketDataAvailabilityProvider(delegate, this);
  }

  @Override
  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalId identifier, final ValueRequirement desiredValue) {
    if (_acceptableSchemes.contains(identifier.getScheme())) {
      return super.getAvailability(targetSpec, identifier, desiredValue);
    } else {
      return null;
    }
  }

  @Override
  protected ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final ExternalIdBundle identifiers, final ValueRequirement desiredValue) {
    List<ExternalId> acceptable = null;
    for (final ExternalId identifier : identifiers) {
      if (_acceptableSchemes.contains(identifier.getScheme())) {
        if (acceptable == null) {
          acceptable = new ArrayList<ExternalId>(identifiers.size());
        }
        acceptable.add(identifier);
      }
    }
    if (acceptable != null) {
      return super.getAvailability(targetSpec, ExternalIdBundle.of(acceptable), desiredValue);
    } else {
      return null;
    }
  }

}
