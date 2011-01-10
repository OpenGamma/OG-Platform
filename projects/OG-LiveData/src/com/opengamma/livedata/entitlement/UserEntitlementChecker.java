/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Say {@link #isEntitled(UserPrincipal, DistributionSpecification)} is called with a
 * {@link DistributionSpecification} with JMS topic name LiveData.Reuters.AAPL.O.
 * If the user for example has <code>Authority</code>
 * LiveData/Reuters/&#42;, access is granted. But if the user has no
 * compatible <code>Authority</code>, access is denied.
 * 
 */
public class UserEntitlementChecker extends DistributionSpecEntitlementChecker {
  
  private static final Logger s_logger = LoggerFactory.getLogger(UserEntitlementChecker.class);

  private final UserManager _userManager;
  
  /**
   * @param resolver 
   * @param userManager
   *          Used to load users (their permissions really)
   */
  public UserEntitlementChecker(UserManager userManager, DistributionSpecificationResolver resolver) {
    super(resolver);
    
    ArgumentChecker.notNull(userManager, "User manager");
    _userManager = userManager;
  }

  @Override
  public boolean isEntitled(UserPrincipal userPrincipal, DistributionSpecification distributionSpec) {
    
    User user = _userManager.getUser(userPrincipal.getUserName());
    if (user == null) {
      return false;
    }
    
    String permission = distributionSpec.getJmsTopic().replace('.', '/');
    s_logger.debug("Checking permission against {}", permission);
    
    return user.hasPermission(permission);
  }

}
