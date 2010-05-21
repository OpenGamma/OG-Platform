/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Data provider for live data.
 */
public class DomainLiveDataAvailabilityProvider implements LiveDataAvailabilityProvider {

  /**
   * The security master to resolve against.
   */
  private final SecurityMaster _securityMaster;
  /**
   * The list of acceptable schemes.
   */
  private final Collection<IdentificationScheme> _acceptableSchemes;

  /**
   * Creates a provider.
   * @param secMaster  the security master, not null
   * @param acceptableSchemes  the acceptable schemes, not null
   */
  public DomainLiveDataAvailabilityProvider(SecurityMaster secMaster, IdentificationScheme... acceptableSchemes) {
    this (secMaster, Arrays.asList(acceptableSchemes));
  }
  
  /**
   * Creates a provider.
   * @param secMaster the security master, not null
   * @param acceptableSchemes the acceptable schemes, not null
   */
  public DomainLiveDataAvailabilityProvider(final SecurityMaster secMaster, final Collection<IdentificationScheme> acceptableSchemes) {
    ArgumentChecker.notNull(secMaster, "Security master");
    ArgumentChecker.notNull(acceptableSchemes, "Acceptable schemes");
    _securityMaster = secMaster;
    _acceptableSchemes = new HashSet<IdentificationScheme>(acceptableSchemes);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    if (!ObjectUtils.equals(ValueRequirementNames.MARKET_DATA_HEADER, requirement.getValueName())) {
      return false;
    }
    switch (requirement.getTargetSpecification().getType()) {
      case PRIMITIVE: {
        IdentificationScheme scheme = requirement.getTargetSpecification().getIdentifier().getScheme();
        return _acceptableSchemes.contains(scheme);
      }
      case SECURITY: {
        Security security = _securityMaster.getSecurity(requirement.getTargetSpecification().getUniqueIdentifier());
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
