/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.util.Collection;

/**
 * The core interface through which clients are able to interact
 * with the rest of the OpenGamma Live Data system.
 *
 * @author kirk
 */
public interface LiveDataClient {
  
  // REVIEW kirk 2009-09-29 -- When we have a better handle on security
  // principals, this needs to be changed.
  
  /**
   * If already subscribed under this user name, will not do anything.
   */
  void subscribe(String userName, LiveDataSpecification requestedSpecification, LiveDataListener listener);
  
  /**
   * Equivalent to calling {@link #subscribe(String userName, LiveDataSpecification requestedSpecification, LiveDataListener listener)}
   * for each specification individually, but may be more efficient. 
   */
  void subscribe(String userName, Collection<LiveDataSpecification> requestedSpecifications, LiveDataListener listener);
  
  
  // REVIEW kirk 2009-09-29 -- Once I figure out a cleaner way to implement these than the
  // original version, these will be re-added.
  /*
  void unsubscribeAll(String userName);
  void unsubscribeAll(LiveDataSpecification fullyQualifiedSpecification);
  void unsubscribeAll(String userName, LiveDataSpecification fullyQualifiedSpecification);
  */
  void unsubscribe(String userName, LiveDataSpecification fullyQualifiedSpecification, LiveDataListener listener);
  void unsubscribe(String userName, Collection<LiveDataSpecification> fullyQualifiedSpecifications, LiveDataListener listener);
  
  public String getDefaultNormalizationRuleSetId();
  
}
