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
   * @param subscription  the new subscription, not null
   */
  void subscribed(Subscription subscription);

  /**
   * Called on unsubscription.
   * 
   * @param subscription  the subscription that was just stopped, not null
   */
  void unsubscribed(Subscription subscription);

}
