/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.web.LongPollingTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test subscription manager that can have a maximum of one connection.
 */
public class TestSubscriptionManager implements SubscriptionManager {

  private long _nextClientId = 0;

  private final Map<String, SubscriptionListener> _listeners = new HashMap<String, SubscriptionListener>();
  private final Object _lock = new Object();

  @Override
  public String newConnection(String userId, SubscriptionListener listener) {
    synchronized (_lock) {
      String idStr = Long.toString(_nextClientId++);
      _listeners.put(idStr, listener);
      return idStr;
    }
  }

  @Override
  public void closeConnection(String userId, String clientId) {
    throw new UnsupportedOperationException("closeConnection not used in this test");
  }

  @Override
  public boolean subscribe(String userId, String clientId, UniqueId uid) {
    throw new UnsupportedOperationException("subscribe not used in this test");
  }

  public void sendUpdate(String clientId, String update) {
    synchronized (_lock) {
      SubscriptionListener listener = _listeners.get(clientId);
      if (listener == null) {
        throw new IllegalArgumentException("unknown client ID " + clientId);
      }
      listener.itemUpdated(new SubscriptionEvent(update));
    }
  }
}
