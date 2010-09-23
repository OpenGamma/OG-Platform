/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

/**
 * 
 *
 */
public interface SubscriptionListener {
  
  /**
   * Called on initial subscription. 
   * 
   * @param subscription New subscription
   */
  void subscribed(Subscription subscription);
  
  /**
   * Called on unsubscription.
   * 
   * @param subscription Subscription that was just stopped
   */
  void unsubscribed(Subscription subscription);

}
