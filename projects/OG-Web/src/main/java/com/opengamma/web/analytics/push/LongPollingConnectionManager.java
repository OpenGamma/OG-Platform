/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.continuation.Continuation;

import com.google.common.base.Objects;

/**
 * Maps client IDs to Jetty continuations associated with a client connection.
 */
public class LongPollingConnectionManager {

  /** Listener for dispatching notifications to the clients, keyed by client ID. */
  private final Map<String, LongPollingUpdateListener> _updateListeners = new ConcurrentHashMap<String, LongPollingUpdateListener>();

  /**
   * Creates a new connection.
   *
   * @param userId The ID of the user who owns the connection
   * @param clientId The connection ID
   * @param timeoutTask Connection timeout task that the listener must reset every time the connection is set up
   * @return A listener that publishes to the client when it receives a notification
   */
  /* package */ LongPollingUpdateListener handshake(String userId, String clientId, ConnectionTimeoutTask timeoutTask) {
    LongPollingUpdateListener listener = new LongPollingUpdateListener(clientId, userId, timeoutTask);
    _updateListeners.put(clientId, listener);
    return listener;
  }

  /**
   * Associates a continuation with a client connection so asynchronous updates can be pushed to the client.
   * 
   * @param userId The ID of the user
   * @param clientId The client ID of the connection
   * @param continuation For sending an async response to the client
   * @return true if the connection was successful, false if the client ID doesn't correspond to
   * an existing connection
   */
  /* package */ boolean longPollHttpConnect(String userId, String clientId, Continuation continuation) {
    // TODO check args
    LongPollingUpdateListener listener = _updateListeners.get(clientId);
    if (listener != null) {
      if (!Objects.equal(userId, listener.getUserId())) {
        throw new IllegalArgumentException("User ID " + userId + " doesn't correspond to client ID: " + clientId);
      }
      listener.connect(continuation);
      return true;
    } else {
      return false;
    }
  }

  // for testing
  /* package */ boolean isClientConnected(String clientId) {
    LongPollingUpdateListener listener = _updateListeners.get(clientId);
    return listener != null && listener.isConnected();
  }

  /**
   * Called by the HTTP container when a long polling connection times out before any updates are sent.
   * This doesn't end the client's connection or remove the associated client ID.  Normally the client will immediately
   * establish a new long-polling HTTP connection.
   * 
   * @param clientId The client ID associated with the timed out connection
   * @param continuation The continuation associated with the timed out HTTP connection
   */
  /* package */ void longPollHttpTimeout(String clientId, Continuation continuation) {
    LongPollingUpdateListener listener = _updateListeners.get(clientId);
    if (listener != null) {
      listener.timeout(continuation);
    }
  }

  /**
   * Invoked when the client disconnects.
   * 
   * @param clientId ID of the client connection that disconnected
   */
  /* package */ void disconnect(String clientId) {
    LongPollingUpdateListener listener = _updateListeners.remove(clientId);
    if (listener != null) {
      listener.disconnect();
    }
  }

}

