/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;

/**
 * Permission provider for live market data which delegates to a {@link LiveDataEntitlementChecker}.
 */
public class LiveMarketDataPermissionProvider implements MarketDataPermissionProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(LiveMarketDataPermissionProvider.class);
  
  private final LiveDataEntitlementChecker _entitlementChecker;
  private final SecuritySource _securitySource;
  
  public LiveMarketDataPermissionProvider(LiveDataEntitlementChecker entitlementChecker, SecuritySource securitySource) {
    _entitlementChecker = entitlementChecker;
    _securitySource = securitySource;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public boolean canAccessMarketData(UserPrincipal user, Set<ValueRequirement> requirements) {
    s_logger.info("Checking that {} is entitled to computation results", user);
    Collection<LiveDataSpecification> requiredLiveData = getRequiredLiveDataSpecifications(requirements);
    Map<LiveDataSpecification, Boolean> entitlements;
    try {
      entitlements = getEntitlementChecker().isEntitled(user, requiredLiveData);
    } catch (Exception e) {
      s_logger.warn("Failed to perform entitlement checking. Failing open - assuming entitled.", e);
      return true;
    }
    ArrayList<LiveDataSpecification> failures = new ArrayList<LiveDataSpecification>();
    for (Map.Entry<LiveDataSpecification, Boolean> entry : entitlements.entrySet()) {
      if (!entry.getValue().booleanValue()) {
        failures.add(entry.getKey());
      }
    }

    if (!failures.isEmpty()) {
      s_logger.warn("User {} does not have permission to access {}/{}", new Object[] {user, failures.size(), requirements.size()});
      return false;
    }
    
    return true;
  }
  
  //-------------------------------------------------------------------------
  private LiveDataEntitlementChecker getEntitlementChecker() {
    return _entitlementChecker;
  }
  
  private SecuritySource getSecuritySource() {
    return _securitySource;
  }
  
  private Collection<LiveDataSpecification> getRequiredLiveDataSpecifications(Set<ValueRequirement> requirements) {
    Set<LiveDataSpecification> returnValue = new HashSet<LiveDataSpecification>();
    for (ValueRequirement requirement : requirements) {
      LiveDataSpecification liveDataSpec = requirement.getTargetSpecification().getRequiredLiveData(getSecuritySource());
      returnValue.add(liveDataSpec);
    }
    return returnValue;
  }
  
}
