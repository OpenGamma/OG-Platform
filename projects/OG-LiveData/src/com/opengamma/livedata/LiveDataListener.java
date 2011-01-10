/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.util.PublicAPI;


/**
 * An interface through which clients can receive updates on their
 * LiveData subscriptions.
 * <p> 
 * This is the main interface you  need to implement in your LiveData client application.
 * 
 */
@PublicAPI
public interface LiveDataListener {
  
  /**
   * Called when a subscription result is received from a LiveData server.
   * 
   * @param subscriptionResult subscription result is received from server
   */
  void subscriptionResultReceived(LiveDataSubscriptionResponse subscriptionResult);
  
  /**
   * Used to indicate that a subscription will stop providing updates to this listener.
   * <p/>
   * For concurrency reasons, it is possible and plausible that calls to
   * {@link #valueUpdate(LiveDataValueUpdate)} corresponding to this specification
   * will come in after an invocation of this method.
   * 
   * @param fullyQualifiedSpecification subscription that was stopped. 
   * @see LiveDataSubscriptionResponse#getFullyQualifiedSpecification
   */
  void subscriptionStopped(LiveDataSpecification fullyQualifiedSpecification);
  
  /**
   * Called when a market data update is received from the LiveData server. 
   * <p> 
   * <i>This method must execute quickly.</i> The way the Live Data Client works,
   * several market data lines may be handled by the same Live Data Client
   * thread. If the execution of this method is slow, for example
   * because it uses external resources such as files or the network
   * in some way, or executes a complicated algorithm, then 
   * market data updates for <i>other</i> market data lines will not be received
   * until this method returns. Thus, if you need to execute a
   * non-trivial operation when you receive new market data, 
   * do it in a new thread.      
   * 
   * @param valueUpdate new market data
   */
  void valueUpdate(LiveDataValueUpdate valueUpdate);

}
