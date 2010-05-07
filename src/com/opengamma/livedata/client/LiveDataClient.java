/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.Collection;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.UserPrincipal;


/**
 * The core interface through which clients are able to interact
 * with the rest of the OpenGamma Live Data system.
 *
 * @author kirk
 */
public interface LiveDataClient {
  
  /**
   * Creates a non-persistent subscription to market data.
   * Returns immediately without waiting for a reply from the server.
   * The reply will be sent later to the listener.
   */
  void subscribe(UserPrincipal user,
      LiveDataSpecification requestedSpecification, 
      LiveDataListener listener);
  
  /**
   * Equivalent to calling {@link #subscribe(String userName, LiveDataSpecification requestedSpecification, LiveDataListener listener)}
   * for each specification individually, but may be more efficient. 
   */
  void subscribe(UserPrincipal user,
      Collection<LiveDataSpecification> requestedSpecifications, 
      LiveDataListener listener);
  
  void unsubscribe(UserPrincipal user, LiveDataSpecification fullyQualifiedSpecification, LiveDataListener listener);
  void unsubscribe(UserPrincipal user, Collection<LiveDataSpecification> fullyQualifiedSpecifications, LiveDataListener listener);
  
  /**
   * Asks for a snapshot from the server. 
   * Waits for a reply from the server before returning.
   * <p>
   * Always contacts the server, even if there is an active
   * subscription to this market data.   
   * 
   * @param timeout In milliseconds. If the timeout is non-positive, this method will not wait at all, so null will be returned.
   * @throws OpenGammaRuntimeException If timeout was reached without reply from server
   */
  LiveDataSubscriptionResponse snapshot(UserPrincipal user,
      LiveDataSpecification requestedSpecification,
      long timeout);
  
  /**
   * Equivalent to calling {@link #snapshot(String userName, LiveDataSpecification requestedSpecification, long timeout)}
   * for each specification individually, but may be more efficient.
   * 
   * @return The returned response will be complete, i.e., it will contain <code>requestedSpecifications.size()</code> entries.
   * @throws OpenGammaRuntimeException If timeout was reached without reply from server 
   */
  Collection<LiveDataSubscriptionResponse> snapshot(UserPrincipal user,
      Collection<LiveDataSpecification> requestedSpecifications,
      long timeout);
  
  String getDefaultNormalizationRuleSetId();
  
  // REVIEW kirk 2009-09-29 -- Once I figure out a cleaner way to implement these than the
  // original version, these will be re-added.
  /*
  void unsubscribeAll(String userName);
  void unsubscribeAll(LiveDataSpecification fullyQualifiedSpecification);
  void unsubscribeAll(String userName, LiveDataSpecification fullyQualifiedSpecification);
  */
  
}
