/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.google.common.base.Objects;
import com.opengamma.web.server.push.subscription.RestUpdateManager;
import com.opengamma.web.server.push.subscription.TimeoutListener;
import org.eclipse.jetty.continuation.Continuation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps client IDs to Jetty continuations associated with a client connection.
 */
/* package */ class LongPollingConnectionManager {

  private final RestUpdateManager _restUpdateManager;
  private final Map<String, LongPollingUpdateListener> _connections = new ConcurrentHashMap<String, LongPollingUpdateListener>();

  public LongPollingConnectionManager(RestUpdateManager restUpdateManager) {
    _restUpdateManager = restUpdateManager;
  }

  String handshake(String userId) {
    LongPollingUpdateListener connection = new LongPollingUpdateListener(userId);
    String clientId = _restUpdateManager.newConnection(userId, connection, new DisconnectionListener());
    _connections.put(clientId, connection);
    return clientId;
  }

  /**
   * Associates a continuation with a client connection so asynchronous updates can be pushed to the client.
   * @param userId The ID of the user
   * @param clientId The client ID of the connection
   * @param continuation For sending an async response to the client
   * @return {@code true} if the connection was successful, {@code false} if the client ID doesn't correspond to
   * an existing connection
   */
  boolean connect(String userId, String clientId, Continuation continuation) {
    // TODO check args
    LongPollingUpdateListener connection = _connections.get(clientId);
    if (connection != null) {
      if (!Objects.equal(userId, connection.getUserId())) {
        throw new IllegalArgumentException("User ID " + userId + " doesn't correspond to client ID: " + clientId);
      }
      connection.connect(continuation);
      return true;
    } else {
      return false;
    }
  }

  // for testing
  boolean isClientConnected(String clientId) {
    LongPollingUpdateListener listener = _connections.get(clientId);
    return listener != null && listener.isConnected();
  }

  public void timeout(String clientId) {
    // TODO implement LongPollingConnectionManager.timeout()
    LongPollingUpdateListener listener = _connections.get(clientId);
    if (listener != null) {
      listener.timeout();
    }
  }

  private class DisconnectionListener implements TimeoutListener {

    @Override
    public void timeout(String clientId) {
      LongPollingUpdateListener connection = _connections.remove(clientId);
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
}

