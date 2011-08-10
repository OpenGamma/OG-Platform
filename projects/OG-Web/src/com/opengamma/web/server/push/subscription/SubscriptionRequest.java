/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

/**
 * TODO double dispatch between this class and the ClientSubscriptions?
 * TODO what's the difference between a Subscription and SubscriptionRequest? can they be merged?
 */
public abstract class SubscriptionRequest {

  public abstract void submit(String userId, String clientId, SubscriptionManagerImpl subscriptionManager);

  // this should allow Jersey to create the object from JSON
  public SubscriptionRequest valueOf(String json) {
    throw new UnsupportedOperationException("TODO");
  }
}
