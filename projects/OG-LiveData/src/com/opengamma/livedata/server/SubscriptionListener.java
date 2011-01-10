/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

/**
 * Listens to subscriptions made on the server.
 */
public interface SubscriptionListener {
  
  /**
   * Called on initial subscription. 
   * 
   * @param subscription new subscription
   */
  void subscribed(Subscription subscription);
  
  /**
   * Called on unsubscription.
   * 
   * @param subscription subscription that was just stopped
   */
  void unsubscribed(Subscription subscription);

}
