/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

/**
 *
 */
public class EntitySubscriptionRequest extends SubscriptionRequest {

  @Override
  public void submit(String userId, String clientId, RestUpdateManagerImpl updateManager) {
    throw new UnsupportedOperationException("submit not implemented");
  }
}
