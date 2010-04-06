/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;


/**
 * 
 *
 * @author kirk
 */
public class PermissiveLiveDataEntitlementChecker implements
    LiveDataEntitlementChecker {

  @Override
  public boolean isEntitled(String userName,
      DistributionSpecification distributionSpec) {
    return true;
  }

}
