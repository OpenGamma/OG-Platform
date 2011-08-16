/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

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
   * The {@link SubscriptionListener} associated with the client will be called when something is updated.
   * @param userId ID of the user
   * @param clientId ID of the connection
   * @param uid The {@code UniqueId} of the entity for which updates are required
   * @return {@code false} if {@code clientId} doesn't correspond to an existing client connection
   */
  boolean subscribe(String userId, String clientId, UniqueId uid);
}
