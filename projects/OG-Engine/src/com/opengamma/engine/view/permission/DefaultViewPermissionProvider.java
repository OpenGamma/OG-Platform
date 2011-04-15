/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;

/**
 * Default implementation of {@code ViewPermissionProvider}.
 */
public class DefaultViewPermissionProvider implements ViewPermissionProvider {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultViewPermissionProvider.class);
  
  private final SecuritySource _securitySource;
  private final LiveDataEntitlementChecker _entitlementChecker;
  
  public DefaultViewPermissionProvider(SecuritySource securitySource, LiveDataEntitlementChecker entitlementChecker) {
    _securitySource = securitySource;
    _entitlementChecker = entitlementChecker;
  }
  
  private SecuritySource getSecuritySource() {
    return _securitySource;
  }
  
  private LiveDataEntitlementChecker getEntitlementChecker() {
    return _entitlementChecker;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public boolean canAccessCompiledViewDefinition(UserPrincipal user, CompiledViewDefinition viewEvaluationModel) {
    // REVIEW jonathan 2011-03-28 -- if/when we have fine-grained per-user permissions on view definitions or view
    // processes, then this would need to check against those.
    return true;
  }

  @Override
  public boolean canAccessComputationResults(UserPrincipal user, CompiledViewDefinition viewEvaluationModel) {
    s_logger.info("Checking that {} is entitled to computation results from {}", user, viewEvaluationModel);
    Collection<LiveDataSpecification> requiredLiveData = getRequiredLiveDataSpecifications(viewEvaluationModel);
    Map<LiveDataSpecification, Boolean> entitlements = getEntitlementChecker().isEntitled(user, requiredLiveData);
    ArrayList<LiveDataSpecification> failures = new ArrayList<LiveDataSpecification>();
    for (Map.Entry<LiveDataSpecification, Boolean> entry : entitlements.entrySet()) {
      if (!entry.getValue().booleanValue()) {
        failures.add(entry.getKey());
      }
    }

    if (!failures.isEmpty()) {
      s_logger.warn("User {} is not entitled to view computation results from {} because they do not have permission to: {}", new Object[] {user, viewEvaluationModel, failures});
      return false;
    }
    
    return true;
  }

  //-------------------------------------------------------------------------
  private Collection<LiveDataSpecification> getRequiredLiveDataSpecifications(CompiledViewDefinition viewEvaluationModel) {
    Set<LiveDataSpecification> returnValue = new HashSet<LiveDataSpecification>();
    Set<ValueRequirement> liveDataRequirements = viewEvaluationModel.getLiveDataRequirements().keySet();
    for (ValueRequirement requirement : liveDataRequirements) {
      LiveDataSpecification liveDataSpec = requirement.getRequiredLiveData(getSecuritySource());
      returnValue.add(liveDataSpec);
    }
    return returnValue;
  }
  
}
