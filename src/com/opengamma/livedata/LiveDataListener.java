/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;


/**
 * An interface through which clients can receive updates on their
 * live data subscriptions. This is the main interface you
 * need to implement in your LiveData client application.
 * 
 */
public interface LiveDataListener {
  
  void subscriptionResultReceived(LiveDataSubscriptionResponse subscriptionResult);
  
  /**
   * Used to indicate that a subscription will stop providing updates to this listener.
   * <p/>
   * For concurrency reasons, it is possible and plausible that calls to
   * {@link #valueUpdate(LiveDataValueUpdate)} corresponding to this specification
   * will come in after an invocation of this method.
   * 
   * @param fullyQualifiedSpecification Matches the {@link LiveDataSubscriptionResponse#getFullyQualifiedSpecification()}
   * in {@link #subscriptionResultReceived(LiveDataSubscriptionResponse)}.
   */
  void subscriptionStopped(LiveDataSpecification fullyQualifiedSpecification);
  
  /**
   * Called when a market data update is received from the LiveData server. 
   * <p> 
   * Important implementation note to listener implementors:
   * THIS METHOD MUST EXECUTE QUICKLY. The way the Live Data Client works,
   * several market data lines may be handled by the same Live Data Client
   * thread. If the execution of this method is slow, for example
   * because it uses external resources such as files or the network
   * in some way, or executes a complicated algorithm, then 
   * market data updates for OTHER market data lines will not received
   * until this method returns. Thus, if you need to execute a
   * non-trivial operation when you receive new market data, 
   * do it in a new thread.      
   * 
   * @param valueUpdate New market data
   */
  void valueUpdate(LiveDataValueUpdate valueUpdate);

}
