/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;

/**
 * Makes implementing {@link LiveDataEntitlementChecker} easier.
 * Implements {@link #isEntitled(UserPrincipal, Collection)} so you don't need to.
 */
public abstract class AbstractEntitlementChecker implements LiveDataEntitlementChecker {
  
  @Override
  public Map<LiveDataSpecification, Boolean> isEntitled(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications) {
    Map<LiveDataSpecification, Boolean> returnValue = new HashMap<LiveDataSpecification, Boolean>();
    for (LiveDataSpecification spec : requestedSpecifications) {
      boolean entitled = isEntitled(user,  spec);
      returnValue.put(spec, entitled);                  
    }
    return returnValue;
  }

}
