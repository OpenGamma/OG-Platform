/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.google.common.base.Objects;
import org.eclipse.jetty.continuation.Continuation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps client IDs to Jetty continuations associated with a client connection.
 */
/* package */ class LongPollingConnectionManager {

  private final RestUpdateManager _restUpdateManager;
  /** Listener for dispatching notifications to the clients, keyed by client ID. */
  private final Map<String, LongPollingUpdateListener> _connections = new ConcurrentHashMap<String, LongPollingUpdateListener>();

  /* package */ LongPollingConnectionManager(RestUpdateManager restUpdateManager) {
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
  /* package */ boolean longPollHttpConnect(String userId, String clientId, Continuation continuation) {
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
  /* package */ boolean isClientConnected(String clientId) {
    LongPollingUpdateListener listener = _connections.get(clientId);
    return listener != null && listener.isConnected();
  }

  /**
   * Called by the HTTP container when a long polling connection times out before any updates are sent.
   * This doesn't end the client's connection or remove the associated client ID.  Normally the client will immediately
   * re-establish a long-polling HTTP connection.
   * @param clientId The client ID associated with the timed out connection
   * @param continuation The continuation associated with the timed out HTTP connection
   */
  /* package */ void longPollHttpTimeout(String clientId, Continuation continuation) {
    LongPollingUpdateListener listener = _connections.get(clientId);
    if (listener != null) {
      listener.timeout(continuation);
    }
  }

  /**
   * Listens for notifications that a client connection has been idle too long and has timed out.  This is
   * unrelated to an HTTP connection timing out which can happen repeatedly for a client connection.
   */
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

