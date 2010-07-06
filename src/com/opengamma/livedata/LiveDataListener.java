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
  
  void valueUpdate(LiveDataValueUpdate valueUpdate);

}
