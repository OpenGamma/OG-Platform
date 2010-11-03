/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.resolver.DistributionSpecificationResolver;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * A base class that makes it easier to
 * create a rich, complex entitlement check.
 * <p>
 * The class maps {@link LiveDataSpecification}
 * (which contains unresolved information, i.e., just IDs) to
 * {@link DistributionSpecification} (which contains resolved information,
 * i.e., IDs mapped to actual objects). This additional information can be used
 * to create a richer entitlement check. 
 */
public abstract class DistributionSpecEntitlementChecker extends AbstractEntitlementChecker {
  
  private static final Logger s_logger = LoggerFactory.getLogger(DistributionSpecEntitlementChecker.class);
  
  private final DistributionSpecificationResolver _resolver;
  
  /**
   * 
   * @param resolver used to get a {@code DistributionSpecification} from a {@code LiveDataSpecification}
   */
  public DistributionSpecEntitlementChecker(DistributionSpecificationResolver resolver) {
    ArgumentChecker.notNull(resolver, "Distribution Specification Resolver");
    _resolver = resolver;    
  }

  @Override
  public boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification) {
    DistributionSpecification distributionSpecification;
    try {
      distributionSpecification = _resolver.getDistributionSpecification(requestedSpecification);
    } catch (IllegalArgumentException e) {
      s_logger.info("User not entitled as distribution specification could not be built", e);
      return false;
    }
    return isEntitled(user, distributionSpecification);
  }
  
  /**
   * Override this method in your subclasses.
   * 
   * @param user user requesting access to market data
   * @param distributionSpecification market data that the user requested
   * @return true if the user is entitled to the given market data, false otherwise
   */
  public abstract boolean isEntitled(UserPrincipal user, DistributionSpecification distributionSpecification);

}
