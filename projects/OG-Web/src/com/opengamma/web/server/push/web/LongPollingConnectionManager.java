/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.web.server.push.subscription.SubscriptionManager;
import org.eclipse.jetty.continuation.Continuation;

import java.util.HashMap;
import java.util.Map;

/**
 * maps clientIds to Jetty continuations associated with a client connection
 * TODO needs some serious concurrency thought
 */
public class LongPollingConnectionManager {

  // TODO this needs to be initialized by Spring
  private final SubscriptionManager _subscriptionManager;

  // TODO what value type?
  private final Map<String, LongPollingSubscriptionListener> _connections =
          new HashMap<String, LongPollingSubscriptionListener>();

  public LongPollingConnectionManager(SubscriptionManager subscriptionManager) {
    _subscriptionManager = subscriptionManager;
  }

  String handshake(String userId) {
    LongPollingSubscriptionListener connection = new LongPollingSubscriptionListener();
    String clientId = _subscriptionManager.newConnection(userId, connection);
    _connections.put(clientId, connection);
    return clientId;
  }

  // invoked when a long-polling http connection is established
  // TODO do we need the response for setting the status when things go wrong?
  void connectionEstablished(String userId, String clientId, Continuation continuation) {
    // TODO check userId and clientId correspond
    LongPollingSubscriptionListener connection = _connections.get(clientId);
    if (connection != null) {
      connection.connectionEstablished(continuation);
    } else {
      // TODO what here? exception? response status
    }
  }

  // for testing
  boolean isClientConnected(String clientId) {
    LongPollingSubscriptionListener listener = _connections.get(clientId);
    return listener != null && listener.isConnected();
  }
}

