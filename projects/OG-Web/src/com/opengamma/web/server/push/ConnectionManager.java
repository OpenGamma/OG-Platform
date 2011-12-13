/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.rest.MasterType;

/**
 * Provides the operations needed by clients connected to the web interface.  In this context "client" means
 * one connection to the server, e.g. one browser tab.  A user can have multiple simultaneous clients connected.
 * TODO This isn't a good name, it's too vague and generic to have much meaning
 * @see ClientConnection
 */
public interface ConnectionManager {

  /**
   * Closes the connection for a client and cleans up its resources.
   * @param userId The ID of the user that owns the connection
   * @param clientId The ID of the client
   */
  void clientDisconnected(String userId, String clientId);

  /**
   * Handshake method that returns the client ID needed to identify the connection when calling all other operations.
   * @param userId The ID of the user creating the connection
   * @return The client ID needed to identify the connection when calling all other operations
   */
  String clientConnected(String userId);

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
   * Returns an existing viewport which must belong to the specified user and client.
   * @param userId ID of the user
   * @param clientId ID of the connection, can be null if asynchronous updates aren't needed when the viewport data changes
   * @param viewportId ID of the viewport
   * @return The viewport
   * @throws com.opengamma.DataNotFoundException If any of the IDs are invalid or the viewport or client
   * aren't associated with the specified user
   */
  Viewport getViewport(String userId, String clientId, String viewportId);

  /**
   * Creates a new viewport
   * @param userId ID of the user
   * @param clientId ID of the connection
   * @param viewportDefinition Definition of the viewport contents (cells, timestampts, dependency graphs)
   * @param viewportId ID that should be assigned to the new viewport.  Must be unique
   * @param dataUrl REST URL for requesting the viewport data.  This is published in the asynchronous notification
   *                when the viewport's data changes
   * @param gridStructureUrl REST URL for requesting the viewport grid structure.  This is published in the
   *                         asynchronous notification when the viewport's grid structure changes
   */
  void createViewport(String userId,
                      String clientId,
                      ViewportDefinition viewportDefinition,
                      String viewportId,
                      String dataUrl,
                      String gridStructureUrl);
}
