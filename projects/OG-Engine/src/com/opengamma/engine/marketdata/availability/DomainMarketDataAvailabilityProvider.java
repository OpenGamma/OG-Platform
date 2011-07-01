/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Implements {@link MarketDataAvailabilityProvider} by providing an indication of whether an item of market data is
 * <i>likely</i> to be available based on the scheme of its unique identifier and other factors.
 */
public class DomainMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  /**
   * The security source to resolve against.
   */
  private final SecuritySource _securitySource;
  /**
   * The set of acceptable schemes.
   */
  private final Set<IdentificationScheme> _acceptableSchemes;
  /**
   * The set of acceptable market data fields.
   */
  private final Set<String> _validMarketDataRequirementNames;

  /**
   * Creates a provider.
   * 
   * @param securitySource  the security source, not null
   * @param acceptableSchemes  the acceptable schemes, not null
   * @param validMarketDataRequirementNames  the valid market data requirement names, not null
   */
  public DomainMarketDataAvailabilityProvider(final SecuritySource securitySource, final Collection<IdentificationScheme> acceptableSchemes, final Collection<String> validMarketDataRequirementNames) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(acceptableSchemes, "acceptableSchemes");
    ArgumentChecker.notNull(validMarketDataRequirementNames, "validMarketDataRequirementNames");
    _securitySource = securitySource;
    _acceptableSchemes = new HashSet<IdentificationScheme>(acceptableSchemes);
    _validMarketDataRequirementNames = new HashSet<String>(validMarketDataRequirementNames);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    if (!_validMarketDataRequirementNames.contains(requirement.getValueName())) {
      return false;
    }
    switch (requirement.getTargetSpecification().getType()) {
      case PRIMITIVE: {
        IdentificationScheme scheme = requirement.getTargetSpecification().getIdentifier().getScheme();
        return _acceptableSchemes.contains(scheme);
      }
      case SECURITY: {
        Security security = _securitySource.getSecurity(requirement.getTargetSpecification().getUniqueId());
        if (security != null) {
          for (Identifier identifier : security.getIdentifiers()) {
            if (_acceptableSchemes.contains(identifier.getScheme())) {
              return true;
            }
          }
        }
        return false;
      }
      default:
        return false;
    }
  }

}
