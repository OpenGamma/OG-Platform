/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.util.Collection;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.util.PublicAPI;


/**
 * The core interface through which clients interact with the rest of the OpenGamma LiveData system.
 */
@PublicAPI
public interface LiveDataClient extends LiveDataEntitlementChecker {
  
  /**
   * Creates a non-persistent subscription to market data.
   * Returns immediately without waiting for a reply from the server.
   * The reply will be sent later to the listener.
   * 
   * @param user user credentials. As part of subscribing to market data, there will automatically be an 
   * entitlement check, and if it fails, a 
   * {@link com.opengamma.livedata.msg.LiveDataSubscriptionResult#NOT_AUTHORIZED}
   * response will be returned.
   * @param requestedSpecification what market data you want to subscribe to, and in which 
   * standardized format you want the server to give it to you.
   * @param listener will receive the results of the subscription request.
   */
  void subscribe(UserPrincipal user,
      LiveDataSpecification requestedSpecification, 
      LiveDataListener listener);
  
  /**
   * Creates a number of non-persistent subscriptions to market data.
   * <p>
   * Equivalent to calling {@link #subscribe(UserPrincipal, LiveDataSpecification, LiveDataListener)}
   * for each specification individually, but may be more efficient. 
   * 
   * @param user user credentials. As part of subscribing to market data, there will automatically be an 
   * entitlement check, and if it fails, a 
   * {@link com.opengamma.livedata.msg.LiveDataSubscriptionResult#NOT_AUTHORIZED}
   * response will be returned.
   * @param requestedSpecifications what market data you want to subscribe to, and in which 
   * standardized format you want the server to give it to you.
   * @param listener will receive the results of the subscription request.
   */
  void subscribe(UserPrincipal user,
      Collection<LiveDataSpecification> requestedSpecifications, 
      LiveDataListener listener);
  
  /**
   * Deletes a market data subscription.
   * 
   * @param user user credentials
   * @param fullyQualifiedSpecification what market data you no longer want to subscribe to.
   * @param listener will receive the results of the unsubscription request.
   */
  void unsubscribe(UserPrincipal user, LiveDataSpecification fullyQualifiedSpecification, LiveDataListener listener);
  
  /**
   * Deletes a number of market data subscriptions.
   * <p>
   * Equivalent to calling {@link #unsubscribe(UserPrincipal, LiveDataSpecification, LiveDataListener)}
   * for each specification individually, but may be more efficient. 
   * 
   * @param user user credentials
   * @param fullyQualifiedSpecifications what market data you no longer want to subscribe to.
   * @param listener will receive the results of the unsubscription request.
   */
  void unsubscribe(UserPrincipal user, Collection<LiveDataSpecification> fullyQualifiedSpecifications, LiveDataListener listener);
  
  /**
   * Asks for a snapshot from the server. 
   * Waits for a reply from the server before returning.
   * <p>
   * Always contacts the server, even if there is an active
   * subscription to this market data.   
   * 
   * @param user user credentials. As part of doing the snapshot, there will automatically be an 
   * entitlement check, and if it fails, a 
   * {@link com.opengamma.livedata.msg.LiveDataSubscriptionResult#NOT_AUTHORIZED}
   * response will be returned.
   * @param requestedSpecification what market data you want to subscribe to, and in which 
   * standardized format you want the server to give it to you
   * @param timeout milliseconds. If the timeout is non-positive, this method will not wait at all, so null will be returned.
   * @throws OpenGammaRuntimeException if timeout was reached without reply from server
   * @return the snapshot
   */
  LiveDataSubscriptionResponse snapshot(UserPrincipal user,
      LiveDataSpecification requestedSpecification,
      long timeout);
  
  /**
   * Asks for a number of snapshots from the server.
   * <p>
   * Equivalent to calling {@link #snapshot(UserPrincipal, LiveDataSpecification, long)}
   * for each specification individually, but may be more efficient.
   * 
   * @param user user credentials. As part of doing the snapshot, there will automatically be an 
   * entitlement check, and if it fails, a 
   * {@link com.opengamma.livedata.msg.LiveDataSubscriptionResult#NOT_AUTHORIZED}
   * response will be returned.
   * @param requestedSpecifications what market data you want to subscribe to, and in which 
   * standardized format you want the server to give it to you
   * @param timeout milliseconds. If the timeout is non-positive, this method will not wait at all, so null will be returned.
   * @throws OpenGammaRuntimeException if timeout was reached without reply from server
   * @return the snapshot. The response will be complete, i.e., it will contain <code>requestedSpecifications.size()</code> entries.
   */
  Collection<LiveDataSubscriptionResponse> snapshot(UserPrincipal user,
      Collection<LiveDataSpecification> requestedSpecifications,
      long timeout);
  
  /**
   * Gets the client's default normalization rule set ID.
   * <p>
   * If you do not particularly care what format the data should be returned in
   * (as in certain automated JUnit tests), this method can be used to choose a default
   * normalization scheme when building a {@link LiveDataSpecification}. 
   * 
   * @return the client's default normalization rule set ID 
   */
  String getDefaultNormalizationRuleSetId();
  
  /**
   * Shuts down the client, releasing any underlying resources used to connect to the server.
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
