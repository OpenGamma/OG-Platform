/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.ExternalIdBundleLookup;
import com.opengamma.engine.marketdata.MarketDataUtils;
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
public class DomainMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  /**
   * The set of acceptable schemes.
   */
  private final Set<ExternalScheme> _acceptableSchemes;
  /**
   * The set of acceptable market data fields.
   */
  private final Set<String> _validMarketDataRequirementNames;
  /**
   * A lookup service for finding external identifiers from a target specification.
   */
  private final ExternalIdBundleLookup _externalIdLookup;

  /**
   * Creates a provider.
   * 
   * @param securitySource the security source, not null
   * @param acceptableSchemes the acceptable schemes, not null
   * @param validMarketDataRequirementNames the valid market data requirement names, not null
   */
  public DomainMarketDataAvailabilityProvider(final SecuritySource securitySource, final Collection<ExternalScheme> acceptableSchemes, final Collection<String> validMarketDataRequirementNames) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(acceptableSchemes, "acceptableSchemes");
    ArgumentChecker.notNull(validMarketDataRequirementNames, "validMarketDataRequirementNames");
    _acceptableSchemes = new HashSet<ExternalScheme>(acceptableSchemes);
    _validMarketDataRequirementNames = new HashSet<String>(validMarketDataRequirementNames);
    _externalIdLookup = new ExternalIdBundleLookup(securitySource);
  }

  @Override
  public ValueSpecification getAvailability(final ValueRequirement requirement) {
    if (!_validMarketDataRequirementNames.contains(requirement.getValueName())) {
      return null;
    }
    final ExternalIdBundle bundle = _externalIdLookup.getExternalIds(requirement.getTargetReference());
    if (bundle == null) {
      return null;
    }
    for (ExternalId identifier : bundle) {
      if (_acceptableSchemes.contains(identifier.getScheme())) {
        return MarketDataUtils.createMarketDataValue(requirement, identifier);
      }
    }
    return null;
  }

}
