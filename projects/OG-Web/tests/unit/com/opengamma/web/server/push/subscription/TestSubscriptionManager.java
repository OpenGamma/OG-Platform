/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.opengamma.web.server.push.web.LongPollingTest;

/**
 *
 */
public class TestSubscriptionManager implements SubscriptionManager {

  private volatile SubscriptionListener _listener;

  @Override
  public String newConnection(String userId, SubscriptionListener listener) {
    _listener = listener;
    return LongPollingTest.CLIENT_ID;
  }

  @Override
  public void closeConnection(String userId, String clientId) {
    throw new UnsupportedOperationException("closeConnection not used in this test");
  }

  void sendUpdate(String update) {
    _listener.itemUpdated(new SubscriptionEvent(update));
  }

  // TODO how am I going to wangle this one? make sure there's no connection available somehow
  void sendUpdates(String... updates) {

  }
}
