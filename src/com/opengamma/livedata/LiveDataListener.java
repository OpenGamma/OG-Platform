/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

/**
 * An interface through which clients can receive updates on their
 * live data subscriptions.
 *
 * @author kirk
 */
public interface LiveDataListener {
  
  void subscriptionResultReceived(LiveDataSubscriptionResponse subscriptionResult);

}
