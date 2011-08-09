/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

/**
 * TODO move more methods from SubscriptionManagerImpl into here
 */
public interface SubscriptionManager {

  // handshake method that returns the client ID, must be called before any of the long-polling subscribe methods
  String newConnection(String userId, SubscriptionListener listener);

  void closeConnection(String userId, String clientId);
}
