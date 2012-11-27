/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.push;

import static org.mockito.Mockito.mock;

import com.opengamma.id.UniqueId;
import com.opengamma.web.analytics.rest.MasterType;

/**
 * Test subscription manager that can have a maximum of one connection.
 */
public class TestConnectionManager implements ConnectionManager {

  private volatile UpdateListener _listener;
  private final LongPollingConnectionManager _longPollingConnectionManager;

  public TestConnectionManager() {
    this(null);
  }

  public TestConnectionManager(LongPollingConnectionManager longPollingConnectionManager) {
    _longPollingConnectionManager = longPollingConnectionManager;
  }

  @Override
  public String clientConnected(String userId) {
    ConnectionTimeoutTask timeoutTask = new ConnectionTimeoutTask(mock(ConnectionManager.class), "user", "client", 60000);
    _listener = _longPollingConnectionManager.handshake(userId, LongPollingTest.CLIENT_ID, timeoutTask);
    return LongPollingTest.CLIENT_ID;
  }

  @Override
  public void clientDisconnected(String userId, String clientId) {
    throw new UnsupportedOperationException("closeViewport not used in this test");
  }

  @Override
  public void subscribe(String userId, String clientId, UniqueId uid, String url) {
    throw new UnsupportedOperationException("subscribe not used in this test");
  }

  @Override
  public void subscribe(String userId, String clientId, MasterType masterType, String url) {
    throw new UnsupportedOperationException("subscribe not implemented");
  }

  @Override
  public ClientConnection getConnectionByClientId(String userId, String clientId) {
    throw new UnsupportedOperationException("getConnectionByClientId not implemented");
  }

  public void sendUpdate(String update) {
    _listener.itemUpdated(update);
  }
}
