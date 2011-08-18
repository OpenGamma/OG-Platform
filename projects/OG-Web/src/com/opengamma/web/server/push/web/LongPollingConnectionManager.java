/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.web.server.push.subscription.RestUpdateManager;
import org.eclipse.jetty.continuation.Continuation;

import java.util.HashMap;
import java.util.Map;

/**
 * maps clientIds to Jetty continuations associated with a client connection
 * TODO needs some serious concurrency thought
 */
public class LongPollingConnectionManager {

  // TODO this needs to be initialized by Spring
  private final RestUpdateManager _restUpdateManager;

  // TODO what value type?
  private final Map<String, LongPollingUpdateListener> _connections =
          new HashMap<String, LongPollingUpdateListener>();

  public LongPollingConnectionManager(RestUpdateManager restUpdateManager) {
    _restUpdateManager = restUpdateManager;
  }

  String handshake(String userId) {
    LongPollingUpdateListener connection = new LongPollingUpdateListener();
    String clientId = _restUpdateManager.newConnection(userId, connection);
    _connections.put(clientId, connection);
    return clientId;
  }

  // invoked when a long-polling http connection is established

  /**
   * Associates a continuation with a client connection so asynchronous updates can be pushed to the client.
   * @param userId The ID of the user
   * @param clientId The client ID of the connection
   * @param continuation For sending an async response to the client
   * @return {@code true} if the connection was successful, {@code false} if the client ID doesn't correspond to
   * an existing connection
   */
  boolean connect(String userId, String clientId, Continuation continuation) {
    // TODO check userId and clientId correspond
    LongPollingUpdateListener connection = _connections.get(clientId);
    if (connection != null) {
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
}

