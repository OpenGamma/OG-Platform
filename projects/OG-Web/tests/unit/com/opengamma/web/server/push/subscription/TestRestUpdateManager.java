/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.web.LongPollingTest;

/**
 * Test subscription manager that can have a maximum of one connection.
 */
public class TestRestUpdateManager implements RestUpdateManager {

  private volatile RestUpdateListener _listener;

  @Override
  public String newConnection(String userId,
                              RestUpdateListener updateListener,
                              TimeoutListener disconnectionListener) {
    _listener = updateListener;
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
