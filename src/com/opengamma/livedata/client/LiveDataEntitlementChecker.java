/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import com.opengamma.livedata.LiveDataSpecification;

/**
 * 
 *
 * @author kirk
 */
public interface LiveDataEntitlementChecker {

  boolean isEntitled(String userName, LiveDataSpecification fullyQualifiedSpecification);
}
