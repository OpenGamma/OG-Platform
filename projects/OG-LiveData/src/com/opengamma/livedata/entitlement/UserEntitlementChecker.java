/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.security.user.User;
import com.opengamma.security.user.UserManager;
import com.opengamma.util.ArgumentChecker;

/**
 * Checks user permissions against a user database (as represented by
 * {@link com.opengamma.security.user.UserManager}).
 * <p>
 * For access to be granted, the user must have a permission to the JMS topic
 * name, with dots in the name replaced by slashes.  
 * <p>
 * Say {@link #isEntitled(UserPrincipal, LiveDataSpecification)} is called with a
 * {@link DistributionSpecification} with JMS topic name LiveData.Reuters.AAPL.O.
 * If the user for example has <code>Authority</code>
 * LiveData/Reuters/&#42;, access is granted. But if the user has no
 * compatible <code>Authority</code>, access is denied.
 * 
 */
public class UserEntitlementChecker extends AbstractEntitlementChecker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(UserEntitlementChecker.class);

  private final UserManager _userManager;
  private final DistributionSpecificationResolver _resolver;

  /**
   * @param userManager
   *          Used to load users (their permissions really)
   * @param resolver Used to map from {@link LiveDataSpecification} to {@link DistributionSpecification} 
   */
  public UserEntitlementChecker(UserManager userManager, DistributionSpecificationResolver resolver) {
    ArgumentChecker.notNull(userManager, "User manager");
    ArgumentChecker.notNull(resolver, "Distribution Specification Resolver");
    _userManager = userManager;
    _resolver = resolver;
  }

  @Override
  public Map<LiveDataSpecification, Boolean> isEntitled(UserPrincipal userPrincipal, Collection<LiveDataSpecification> requestedSpecifications) {
    Map<LiveDataSpecification, Boolean> returnValue = new HashMap<LiveDataSpecification, Boolean>();
    
    User user = _userManager.getUser(userPrincipal.getUserName());
    if (user == null) {
      s_logger.debug("User {} does not exist - no permissions are granted", userPrincipal.getUserName());
      for (LiveDataSpecification spec : requestedSpecifications) {
        returnValue.put(spec, false);                
      }
      return returnValue;
    }

    Map<LiveDataSpecification, DistributionSpecification> distributionSpecs = _resolver.resolve(requestedSpecifications);
    
    for (LiveDataSpecification requestedSpec : requestedSpecifications) {
      DistributionSpecification distributionSpec = distributionSpecs.get(requestedSpec);
      if (distributionSpec != null) {
        String permission = distributionSpec.getJmsTopic().replace('.', '/');
        boolean hasPermission = user.hasPermission(permission);
        returnValue.put(requestedSpec, hasPermission);                
      } else {
        s_logger.debug("Could not resolve live data spec {} - no permissions are granted", userPrincipal.getUserName(), requestedSpec);
        returnValue.put(requestedSpec, false);
      }
    }
    
    return returnValue;
  }

}
