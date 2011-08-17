/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.web.LongPollingTest;

import java.util.List;

/**
 * Test subscription manager that can have a maximum of one connection.
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

  @Override
  public void subscribe(String userId, String clientId, UniqueId uid, String url) {
    throw new UnsupportedOperationException("subscribe not used in this test");
  }

  public void sendUpdate(String update) {
    _listener.itemUpdated(update);
  }
}
