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
 * TODO refactor this to just return a ClientConnection and move the subscription and viewport methods to that?
 * TODO this is a misleading name
 * TODO should this be split into 2 interfaces?  (dis)connection methods & subscription / viewport methods
 */
public interface ConnectionManager {

  /**
   * @param userId
   * @param clientId
   */
  void clientDisconnected(String userId, String clientId);

  /**
   * Handshake method that returns the client ID needed to identify the connection when calling all other operations.
   * @param userId
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
   *
   * @param userId
   * @param clientId
   * @param masterType
   * @param url
   */
  void subscribe(String userId, String clientId, MasterType masterType, String url);

  /**
   *
   * @param userId
   * @param clientId
   * @param viewportId
   * @return
   */
  Viewport getViewport(String userId, String clientId, String viewportId);

  /**
   *
   * @param userId
   * @param clientId
   * @param viewportDefinition
   * @param viewportId
   * @param dataUrl
   * @param gridUrl
   */
  void createViewport(String userId,
                      String clientId,
                      ViewportDefinition viewportDefinition,
                      String viewportId,
                      String dataUrl,
                      String gridUrl);
}
