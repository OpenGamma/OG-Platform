/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.marketdata;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.core.value.MarketDataRequirementNamesHelper;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;

/**
 * Example market data provider
 */
public class ExampleMarketDataProvider extends InMemoryLKVMarketDataProvider {
  
  /**
   * The set of acceptable schemes.
   */
  private final Set<ExternalScheme> _acceptableSchemes;
  /**
   * The set of acceptable market data fields.
   */
  private final Set<String> _validMarketDataRequirementNames;
  
  /**
   * Constructs an instance.
   * 
   * @param securitySource  the security source for resolution of Identifiers, null to prevent this support
   */
  public ExampleMarketDataProvider(final SecuritySource securitySource) {
    this(securitySource, ImmutableSet.of(SecurityUtils.OG_SYNTHETIC_TICKER), MarketDataRequirementNamesHelper.constructValidRequirementNames());
  }
  
  /**
   * Creates a provider.
   * 
   * @param securitySource  the security source, not null
   * @param acceptableSchemes  the acceptable schemes, not null
   * @param validMarketDataRequirementNames  the valid market data requirement names, not null
   */
  public ExampleMarketDataProvider(final SecuritySource securitySource, final Collection<ExternalScheme> acceptableSchemes, final Collection<String> validMarketDataRequirementNames) {
    super(securitySource);
    ArgumentChecker.notNull(acceptableSchemes, "acceptableSchemes");
    ArgumentChecker.notNull(validMarketDataRequirementNames, "validMarketDataRequirementNames");
    _acceptableSchemes = new HashSet<ExternalScheme>(acceptableSchemes);
    _validMarketDataRequirementNames = new HashSet<String>(validMarketDataRequirementNames);
  }

  @Override
  public MarketDataAvailability getAvailability(ValueRequirement requirement) {
    if (!_validMarketDataRequirementNames.contains(requirement.getValueName())) {
      return MarketDataAvailability.NOT_AVAILABLE;
    }
    switch (requirement.getTargetSpecification().getType()) {
      case PRIMITIVE: {
        ExternalScheme scheme = requirement.getTargetSpecification().getIdentifier().getScheme();
        return _acceptableSchemes.contains(scheme) ? MarketDataAvailability.AVAILABLE : MarketDataAvailability.NOT_AVAILABLE;
      }
      case SECURITY: {
        try {
          Security security = getSecuritySource().getSecurity(requirement.getTargetSpecification().getUniqueId());
          for (ExternalId identifier : security.getExternalIdBundle()) {
            if (_acceptableSchemes.contains(identifier.getScheme())) {
              return MarketDataAvailability.AVAILABLE;
            }
          }
          return MarketDataAvailability.NOT_AVAILABLE;
        } catch (DataNotFoundException ex) {
          return MarketDataAvailability.NOT_AVAILABLE;
        }
      }
      default:
        return MarketDataAvailability.NOT_AVAILABLE;
    }
  }

}
