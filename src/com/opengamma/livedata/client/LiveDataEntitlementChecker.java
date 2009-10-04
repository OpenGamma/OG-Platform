/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import com.opengamma.livedata.LiveDataSpecification;

// REVIEW kirk 2009-10-04 -- While this is fine initially, eventually this needs to be changed
// so that it can be fully asynchronous so that we're not blocking threads at all in a synchronous
// state waiting for distributed responses.
/**
 * 
 *
 * @author kirk
 */
public interface LiveDataEntitlementChecker {

  boolean isEntitled(String userName, LiveDataSpecification fullyQualifiedSpecification);
}
