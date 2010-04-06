/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import com.opengamma.livedata.server.DistributionSpecification;


// REVIEW kirk 2009-10-04 -- While this is fine initially, eventually this needs to be changed
// so that it can be fully asynchronous so that we're not blocking threads at all in a synchronous
// state waiting for distributed responses.

// REVIEW kirk 2009-10-04 -- The true/false nature here is too simplistic. Need some way
// to provide additional feedback to the user as to WHY so that they can have IT staff
// fix it easily. Otherwise all failures will result in expensive debugging and log-checking.
/**
 * 
 *
 * @author kirk
 */
public interface LiveDataEntitlementChecker {

  boolean isEntitled(String userName, DistributionSpecification distributionSpec);
}
