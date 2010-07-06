/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * This LiveDataEntitlementChecker maps the given LiveDataSpecification
 * (which contains unresolved information, i.e., just IDs) to
 * a DistributionSpecification (which contains resolved information,
 * i.e., IDs mapped to actual objects). It is then easier to
 * make a rich, complex entitlement check.
 */
public abstract class DistributionSpecEntitlementChecker extends AbstractEntitlementChecker {
  
  private final DistributionSpecificationResolver _resolver;
  
  /**
   * 
   * @param resolver Used to get a DistributionSpecification from a LiveDataSpecification
   */
  public DistributionSpecEntitlementChecker(DistributionSpecificationResolver resolver) {
    ArgumentChecker.notNull(resolver, "Distribution Specification Resolver");
    _resolver = resolver;    
  }

  @Override
  public boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification) {
    DistributionSpecification distributionSpecification = _resolver.getDistributionSpecification(requestedSpecification); 
    return isEntitled(user, distributionSpecification);
  }
  
  /**
   * Override this method in your subclasses
   * 
   * @param user User requesting access to market data
   * @param distributionSpecification Market data that the user requested
   * @return Whether the user is entitled to the given market data
   */
  public abstract boolean isEntitled(UserPrincipal user, DistributionSpecification distributionSpecification);

}
