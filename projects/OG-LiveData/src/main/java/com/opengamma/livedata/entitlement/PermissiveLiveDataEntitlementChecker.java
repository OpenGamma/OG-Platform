/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;

/**
 * Always allows the user access to LiveData.
 */
public class PermissiveLiveDataEntitlementChecker extends AbstractEntitlementChecker {

  @Override
  public boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification) {
    return true;
  }

}
