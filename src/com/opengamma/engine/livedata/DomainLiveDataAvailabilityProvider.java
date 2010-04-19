/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.util.ArgumentChecker;


/**
 *
 *
 * @author kirk
 */
public class DomainLiveDataAvailabilityProvider
implements LiveDataAvailabilityProvider {
  
  private final SecurityMaster _securityMaster;
  private final Collection<IdentificationScheme> _acceptableDomains;
  
  public DomainLiveDataAvailabilityProvider(SecurityMaster secMaster, IdentificationScheme... acceptableDomains) {
    ArgumentChecker.checkNotNull(secMaster, "Security master");
    ArgumentChecker.checkNotNull(acceptableDomains, "Available domains");
    _securityMaster = secMaster;
    _acceptableDomains = Arrays.asList(acceptableDomains);
  }
  
  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    if(!ObjectUtils.equals(ValueRequirementNames.MARKET_DATA_HEADER, requirement.getValueName())) {
      return false;
    }
    
    switch (requirement.getTargetSpecification().getType()) {
    
    case PRIMITIVE:
      IdentificationScheme domain = requirement.getTargetSpecification().getIdentifier().getScheme();
      return _acceptableDomains.contains(domain);
    
    case SECURITY:
      Security security = _securityMaster.getSecurity(requirement.getTargetSpecification().getIdentifier());
      if (security == null) {
        return false;
      }
      for (Identifier identifier : security.getIdentifiers()) {
        if (_acceptableDomains.contains(identifier.getScheme())) {
          return true;
        }
      }
      return false;
    
    default:
      return false;
    }
  }

}
