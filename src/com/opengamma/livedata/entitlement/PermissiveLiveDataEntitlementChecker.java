/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.livedata.server.DistributionSpecification;


/**
 * 
 *
 * @author kirk
 */
public class PermissiveLiveDataEntitlementChecker implements
    LiveDataEntitlementChecker {

  @Override
  public boolean isEntitled(UserPrincipal user,
      DistributionSpecification distributionSpec) {
    return true;
  }

}
