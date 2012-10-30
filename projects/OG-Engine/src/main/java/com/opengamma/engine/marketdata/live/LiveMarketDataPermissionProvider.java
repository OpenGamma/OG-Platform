/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;
import com.opengamma.util.ArgumentChecker;

/**
 * Permission provider for live market data which delegates to a {@link LiveDataEntitlementChecker}.
 */
public class LiveMarketDataPermissionProvider implements MarketDataPermissionProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(LiveMarketDataPermissionProvider.class);
  
  private final LiveDataEntitlementChecker _entitlementChecker;
  private final SecuritySource _securitySource;
  
  public LiveMarketDataPermissionProvider(LiveDataEntitlementChecker entitlementChecker, SecuritySource securitySource) {
    ArgumentChecker.notNull(entitlementChecker, "entitlementChecker");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _entitlementChecker = entitlementChecker;
    _securitySource = securitySource;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public Set<ValueRequirement> checkMarketDataPermissions(UserPrincipal user, Set<ValueRequirement> requirements) {
    s_logger.info("Checking that {} is entitled to computation results", user);
    Map<LiveDataSpecification, ValueRequirement> requiredLiveData = getRequiredLiveDataSpecifications(requirements);
    Map<LiveDataSpecification, Boolean> entitlements;
    try {
      entitlements = _entitlementChecker.isEntitled(user, requiredLiveData.keySet());
    } catch (Exception e) {
      // TODO is this really the right thing to do?
      s_logger.warn("Failed to perform entitlement checking. Failing open - assuming entitled.", e);
      return requirements;
    }
    Set<ValueRequirement> failures = Sets.newHashSet();
    for (Map.Entry<LiveDataSpecification, Boolean> entry : entitlements.entrySet()) {
      if (!entry.getValue()) {
        failures.add(requiredLiveData.get(entry.getKey()));
      }
    }
    if (!failures.isEmpty()) {
      s_logger.warn("User {} does not have permission to access {} out of {} market data requirements",
                    new Object[] {user, failures.size(), requirements.size()});
      s_logger.info("User {} does not have permission to access {}", user, failures);
    }
    return failures;
  }

  private Map<LiveDataSpecification, ValueRequirement> getRequiredLiveDataSpecifications(Set<ValueRequirement> requirements) {
    Map<LiveDataSpecification, ValueRequirement> returnValue = Maps.newHashMap();
    for (ValueRequirement requirement : requirements) {
      LiveDataSpecification liveDataSpec = requirement.getTargetSpecification().getRequiredLiveData(_securitySource);
      returnValue.put(liveDataSpec, requirement);
    }
    return returnValue;
  }
  
}
