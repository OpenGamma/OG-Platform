/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import java.util.List;

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
   * Subscribes for updates for entities and viewports.  The {@link SubscriptionListener} associated with the
   * client will be called when something is updated.
   * @param userId ID of the user
   * @param clientId ID of the connection
   * @param urls REST URLs of the objects the client wants to see updates for
   * @return {@code false} if {@code clientId} doesn't correspond to an existing client connection
   */
  boolean subscribe(String userId, String clientId, List<String> urls); 
}
