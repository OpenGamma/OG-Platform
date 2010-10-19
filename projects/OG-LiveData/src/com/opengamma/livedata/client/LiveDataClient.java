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
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.UserPrincipal;


/**
 * The core interface through which clients are able to interact
 * with the rest of the OpenGamma Live Data system.
 */
public interface LiveDataClient extends LiveDataEntitlementChecker {
  
  /**
   * Creates a non-persistent subscription to market data.
   * Returns immediately without waiting for a reply from the server.
   * The reply will be sent later to the listener.
   * 
   * @param user User credentials. As part of subscribing to market data, there will automatically be an 
   * entitlement check, and if it fails, a 
   * {@link com.opengamma.livedata.msg.LiveDataSubscriptionResult#NOT_AUTHORIZED}
   * response will be returned.
   * @param requestedSpecification What market data you want to subscribe to, and in which 
   * standardized format you want the server to give it to you
   * @param listener Will receive the results of the subscription request
   */
  void subscribe(UserPrincipal user,
      LiveDataSpecification requestedSpecification, 
      LiveDataListener listener);
  
  /**
   * Equivalent to calling {@link #subscribe(UserPrincipal, LiveDataSpecification, LiveDataListener)}
   * for each specification individually, but may be more efficient. 
   * 
   * @param user See {@link #subscribe(UserPrincipal, LiveDataSpecification, LiveDataListener)}
   * @param requestedSpecifications See {@link #subscribe(UserPrincipal, LiveDataSpecification, LiveDataListener)}
   * @param listener See {@link #subscribe(UserPrincipal, LiveDataSpecification, LiveDataListener)}
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
   * @param user User credentials. As part of doing the snapshot, there will automatically be an 
   * entitlement check, and if it fails, a 
   * {@link com.opengamma.livedata.msg.LiveDataSubscriptionResult#NOT_AUTHORIZED}
   * response will be returned.
   * @param requestedSpecification What market data you want to subscribe to, and in which 
   * standardized format you want the server to give it to you
   * @param timeout In milliseconds. If the timeout is non-positive, this method will not wait at all, so null will be returned.
   * @throws OpenGammaRuntimeException If timeout was reached without reply from server
   * @return The snapshot
   */
  LiveDataSubscriptionResponse snapshot(UserPrincipal user,
      LiveDataSpecification requestedSpecification,
      long timeout);
  
  /**
   * Equivalent to calling {@link #snapshot(UserPrincipal, LiveDataSpecification, long)}
   * for each specification individually, but may be more efficient.
   * 
   * @param user See {@link #snapshot(UserPrincipal, LiveDataSpecification, long)}
   * @param requestedSpecifications See {@link #snapshot(UserPrincipal, LiveDataSpecification, long)}
   * @param timeout See {@link #snapshot(UserPrincipal, LiveDataSpecification, long)}
   * @return The returned response will be complete, i.e., it will contain <code>requestedSpecifications.size()</code> entries.
   * @throws OpenGammaRuntimeException If timeout was reached without reply from server 
   */
  Collection<LiveDataSubscriptionResponse> snapshot(UserPrincipal user,
      Collection<LiveDataSpecification> requestedSpecifications,
      long timeout);
  
  /**
   * If you do not particularly care what format the data should be returned in
   * (as in certain automated JUnit tests), this method can be used to choose a default
   * normalization scheme when building {@link LiveDataSpecifications LiveDataSpecification}. 
   * 
   * @return Default normalization rule set ID 
   */
  String getDefaultNormalizationRuleSetId();
  
  /**
   * Shut down the client, releasing any underlying resources used to connect to the server.
   */
  void close();

  // REVIEW kirk 2009-09-29 -- Once I figure out a cleaner way to implement these than the
  // original version, these will be re-added.
  /*
  void unsubscribeAll(String userName);
  void unsubscribeAll(LiveDataSpecification fullyQualifiedSpecification);
  void unsubscribeAll(String userName, LiveDataSpecification fullyQualifiedSpecification);
  */
  
}
