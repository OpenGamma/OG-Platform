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
  
  void subscribe(String userName, LiveDataSpecification requestedSpecification, LiveDataListener listener);
  
  // REVIEW kirk 2009-09-29 -- Once I figure out a cleaner way to implement these than the
  // original version, these will be re-added.
  /*
  void unsubscribeAll(String userName);
  void unsubscribeAll(LiveDataSpecification fullyQualifiedSpecification);
  void unsubscribeAll(String userName, LiveDataSpecification fullyQualifiedSpecification);
  */
  void unsubscribe(String userName, LiveDataSpecification fullyQualifiedSpecification, LiveDataListener listener);
  
}
