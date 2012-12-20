/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.web.analytics.rest.MasterType;

/**
 * Provides the operations needed by clients connected to the web interface.  In this context "client" means
 * one connection to the server, e.g. one browser tab.  A user can have multiple simultaneous clients connected.
 * TODO This isn't a good name, it's too vague and generic to have much meaning
 * @see ClientConnection
 */
public interface ConnectionManager {

  /**
   * Handshake method that returns the client ID needed to identify the connection when calling all other operations.
   * @param userId The ID of the user creating the connection
   * @return The client ID needed to identify the connection when calling all other operations
   */
  String clientConnected(String userId);

  /**
   * Closes the connection for a client and cleans up its resources.
   * @param userId The ID of the user that owns the connection
   * @param clientId The ID of the client
   */
  void clientDisconnected(String userId, String clientId);

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

  /**
   * Creates a subscription for any changes to the data in a master.  If the master's data changes a notification
   * will be sent over the long-polling HTTP connection identified by {@code clientId}.
   * The subscription will be automatically cancelled after one update.
   * @param userId ID of the user
   * @param clientId ID of the connection
   * @param url REST URL which should be published with the notification.  This is typically the URL of a REST
   *            query whose result <em>might</em> be invalid when the data in the master changes
   * @param masterType The type of master which should trigger the update
   */
  void subscribe(String userId, String clientId, MasterType masterType, String url);

  /**
   * Returns the {@link ClientConnection} for a given client ID.
   * @param userId ID of the user
   * @param clientId ID of the connection
   * @return The connection for the specified client ID
   * @throws DataNotFoundException If there is no connection with the specified client ID or if the connection
   * doesn't belong to the specified user
   */
  ClientConnection getConnectionByClientId(String userId, String clientId);

}
