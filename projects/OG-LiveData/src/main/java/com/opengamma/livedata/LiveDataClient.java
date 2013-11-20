/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * <p>
 * Access to data relies on the user having sufficient credentials, thus each method requires a user.
 * If the user is not entitled to subscribe or obtain a snapshot, then the message
 * {@link com.opengamma.livedata.msg.LiveDataSubscriptionResult#NOT_AUTHORIZED NOT_AUTHORIZED} will be returned.
 */
@PublicAPI
public interface LiveDataClient extends LiveDataEntitlementChecker {

  /**
   * Creates a non-persistent subscription to market data.
   * Returns immediately without waiting for a reply from the server.
   * The reply will be sent later to the listener.
   * 
   * @param user  the user credentials, checked to ensure user is authorized to access the data, not null
   * @param requestedSpecification  what market data you want to subscribe to, and in which 
   *  standardized format you want the server to give it to you.
   * @param listener  the listener that receives the results of the subscription request
   */
  void subscribe(UserPrincipal user, LiveDataSpecification requestedSpecification, LiveDataListener listener);

  /**
   * Creates a number of non-persistent subscriptions to market data.
   * <p>
   * Equivalent to calling {@link #subscribe(UserPrincipal, LiveDataSpecification, LiveDataListener)}
   * for each specification individually, but may be more efficient. 
   * 
   * @param user  the user credentials, checked to ensure user is authorized to access the data, not null
   * @param requestedSpecifications  what market data you want to subscribe to, and in which 
   *  standardized format you want the server to give it to you.
   * @param listener  the listener that receives the results of the subscription request
   */
  void subscribe(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications, LiveDataListener listener);

  //-------------------------------------------------------------------------
  /**
   * Deletes a market data subscription.
   * 
   * @param user  the user credentials, not null
   * @param fullyQualifiedSpecification what market data you no longer want to subscribe to.
   * @param listener  the listener that receives the results of the unsubscription request
   */
  void unsubscribe(UserPrincipal user, LiveDataSpecification fullyQualifiedSpecification, LiveDataListener listener);

  /**
   * Deletes a number of market data subscriptions.
   * <p>
   * Equivalent to calling {@link #unsubscribe(UserPrincipal, LiveDataSpecification, LiveDataListener)}
   * for each specification individually, but may be more efficient. 
   * 
   * @param user  the user credentials, not null
   * @param fullyQualifiedSpecifications  what market data you no longer want to subscribe to.
   * @param listener  the listener that receives the results of the unsubscription request
   */
  void unsubscribe(UserPrincipal user, Collection<LiveDataSpecification> fullyQualifiedSpecifications, LiveDataListener listener);

  //-------------------------------------------------------------------------
  /**
   * Asks for a snapshot from the server, waiting for a reply.
   * <p>
   * This method waits for a reply from the server before returning.
   * Always contacts the server, even if there is an active subscription to this market data.   
   * 
   * @param user  the user credentials, checked to ennsure user is authorized to access the data, not null
   * @param requestedSpecification  what market data you want to subscribe to, and in which 
   *  standardized format you want the server to give it to you
   * @param timeout  the timeout in milliseconds. If the timeout is non-positive, this method will not wait at all, so null will be returned.
   * @return the snapshot
   * @throws OpenGammaRuntimeException if timeout was reached without reply from server
   */
  LiveDataSubscriptionResponse snapshot(UserPrincipal user, LiveDataSpecification requestedSpecification, long timeout);

  /**
   * Asks for a number of snapshots from the server.
   * <p>
   * Equivalent to calling {@link #snapshot(UserPrincipal, LiveDataSpecification, long)}
   * for each specification individually, but may be more efficient.
   * 
   * @param user  the user credentials, checked to ennsure user is authorized to access the data, not null
   * @param requestedSpecifications  what market data you want to subscribe to, and in which 
   *  standardized format you want the server to give it to you
   * @param timeout  the timeout in milliseconds. If the timeout is non-positive, this method will not wait at all, so null will be returned.
   * @return the snapshot, which will be complete, with {@code requestedSpecifications.size()} entries
   * @throws OpenGammaRuntimeException if timeout was reached without reply from server
   */
  Collection<LiveDataSubscriptionResponse> snapshot(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications, long timeout);

  //-------------------------------------------------------------------------
  /**
   * Gets the client's default normalization rule set ID.
   * <p>
   * If you do not particularly care what format the data should be returned in
   * (as in certain automated JUnit tests), this method can be used to choose a default
   * normalization scheme when building a {@link LiveDataSpecification}. 
   * 
   * @return the default normalization rule set ID for the client
   */
  String getDefaultNormalizationRuleSetId();

  /**
   * Shuts down the client, releasing any underlying resources used to connect to the server.
   */
  void close();

}
