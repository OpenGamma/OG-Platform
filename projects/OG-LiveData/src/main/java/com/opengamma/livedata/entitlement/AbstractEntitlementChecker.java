/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;

/**
 * Base class to aid with the implementation of an entitlement checker.
 * <p>
 * To use this class, override one or other of the two {@code isEntitled()} methods.
 * <p>
 * The simpler option is to override {@link #isEntitled(UserPrincipal, LiveDataSpecification)}.
 * <p> 
 * The more complex option is to override the bulk {@link #isEntitled(UserPrincipal, Collection)}.
 * This is advisable if you need to make a remote call to an external service and want your
 * implementation to be efficient.
 */
public abstract class AbstractEntitlementChecker implements LiveDataEntitlementChecker {

  @Override
  public boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification) {
    Map<LiveDataSpecification, Boolean> result = isEntitled(user, Collections.singleton(requestedSpecification));
    return result.get(requestedSpecification);
  }

  @Override
  public Map<LiveDataSpecification, Boolean> isEntitled(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications) {
    Map<LiveDataSpecification, Boolean> returnValue = new HashMap<LiveDataSpecification, Boolean>();
    for (LiveDataSpecification spec : requestedSpecifications) {
      boolean entitled = isEntitled(user, spec);
      returnValue.put(spec, entitled);
    }
    return returnValue;
  }

}
