/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import java.util.Collection;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;

// REVIEW kirk 2009-10-04 -- While this is fine initially, eventually this needs to be changed
// so that it can be fully asynchronous so that we're not blocking threads at all in a synchronous
// state waiting for distributed responses.

// REVIEW kirk 2009-10-04 -- The true/false nature here is too simplistic. Need some way
// to provide additional feedback to the user as to WHY so that they can have IT staff
// fix it easily. Otherwise all failures will result in expensive debugging and log-checking.

/**
 * Checks if a user is entitled to LiveData.
 */
public interface LiveDataEntitlementChecker {

  /**
   * Checks if a user is entitled to LiveData.
   *  
   * @param user  the user whose entitlements are being checked
   * @param requestedSpecification  what market data the user wants to view   
   * @return true if the user is entitled to the requested market data. false otherwise.
   * @throws OpenGammaRuntimeException if timeout was reached without reply from server 
   */
  boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification);

  /**
   * Equivalent to calling {@link #isEntitled(UserPrincipal, LiveDataSpecification)}
   * for each specification individually, but may be more efficient.
   * 
   * @param user  the user whose entitlements are being checked
   * @param requestedSpecifications  what market data the user wants to view
   * @return a Map telling, for each requested specification, whether the user is entitled to that market data.
   * The returned response will be complete, i.e., it will contain <code>requestedSpecifications.size()</code> entries.
   * @throws OpenGammaRuntimeException if timeout was reached without reply from server 
   */
  Map<LiveDataSpecification, Boolean> isEntitled(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications);

}
