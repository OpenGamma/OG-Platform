/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

/**
 * TODO double dispatch between this class and the ClientSubscriptions?
 * TODO what's the difference between a Subscription and SubscriptionRequest? can they be merged?
 * TODO this is probably redundant
 */
public abstract class SubscriptionRequest {

  public abstract void submit(String userId, String clientId, RestUpdateManagerImpl updateManager);

}
