/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.rest.MasterType;

/**
 * Test subscription manager that can have a maximum of one connection.
 */
public class TestConnectionManager implements ConnectionManager {

  private volatile RestUpdateListener _listener;

  private final LongPollingConnectionManager _longPollingConnectionManager;

  public TestConnectionManager() {
    this(null);
  }

  public TestConnectionManager(LongPollingConnectionManager longPollingConnectionManager) {
    _longPollingConnectionManager = longPollingConnectionManager;
  }

  @Override
  public String openConnection(String userId) {
    _listener = _longPollingConnectionManager.handshake(userId, LongPollingTest.CLIENT_ID);
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

  @Override
  public void subscribe(String userId, String clientId, MasterType masterType, String url) {
    throw new UnsupportedOperationException("subscribe not implemented");
  }

  @Override
  public Viewport getViewport(String userId, String clientId, String viewportUrl) {
    throw new UnsupportedOperationException("getViewport not implemented");
  }

  @Override
  public void createViewport(String userId,
                             String clientId,
                             ViewportDefinition viewportDefinition,
                             String viewportId,
                             String dataUrl,
                             String gridUrl) {
    throw new UnsupportedOperationException("createViewport not implemented");
  }

  public void sendUpdate(String update) {
    _listener.itemUpdated(update);
  }
}
