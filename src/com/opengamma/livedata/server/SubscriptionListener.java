/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

/**
 * 
 *
 * @author pietari
 */
public interface SubscriptionListener {
  
  /**
   * Called on initial subscription. 
   */
  public void subscribed(Subscription subscription);
  
  /**
   * Called if a non-persistent subscription is modified to become persistent,
   * or vice versa.
   */
  public void persistentChanged(Subscription subscription);
  
  /**
   * Called on unsubscription.
   */
  public void unsubscribed(Subscription subscription);

}
