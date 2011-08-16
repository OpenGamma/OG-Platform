/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;

/**
 *
 */
public interface SubscriptionManager {

  /**
   * Handshake method that returns the client ID needed to identify the connection when calling all other operations.
   * @param userId
   * @param listener
   * @return The client ID needed to identify the connection when calling all other operations
   */
  String newConnection(String userId, SubscriptionListener listener);

  void closeConnection(String userId, String clientId);

  /**
   * Creates a subscription for changes to an entity that was requested via the REST interface.  If the entity
   * is updated a notification will be sent over the long-polling HTTP connection identified by {@code clientId}.
   * The subscription will be automatically cancelled after one update.
   * @param userId ID of the user
   * @param clientId ID of the connection
   * @param uid The {@code UniqueId} of the entity for which updates are required
   * @param url REST URL of the entity for which updates are required
   * @throws OpenGammaRuntimeException If {@code clientId} isn't valid or refers to a connection that isn't owned
   * by {@code userId}
   */
  void subscribe(String userId, String clientId, UniqueId uid, String url);
}
