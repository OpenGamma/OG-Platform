/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

/**
 * The core interface through which clients are able to interact
 * with the rest of the OpenGamma Live Data system.
 *
 * @author kirk
 */
public interface LiveDataClient {
  
  // REVIEW kirk 2009-09-29 -- When we have a better handle on security
  // principals, this needs to be changed.
  
  void subscribe(String userName, LiveDataSpecification spec, LiveDataListener listener);
  
}
