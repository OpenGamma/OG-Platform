/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.rest.MasterType;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Test subscription manager that can have a maximum of one connection.
 */
public class TestConnectionManager implements ConnectionManager {

  private volatile RestUpdateListener _listener;

  private final ConcurrentHashMap<String, Viewport> _viewports = new ConcurrentHashMap<String, Viewport>();
  private final LongPollingConnectionManager _longPollingConnectionManager;

  public TestConnectionManager() {
    this(null);
  }

  public TestConnectionManager(LongPollingConnectionManager longPollingConnectionManager) {
    _longPollingConnectionManager = longPollingConnectionManager;
  }

  @Override
  public String clientConnected(String userId) {
    _listener = _longPollingConnectionManager.handshake(userId, LongPollingTest.CLIENT_ID);
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
  public Viewport getViewport(String userId, String clientId, String viewportId) {
    return _viewports.get(viewportId);
  }

  @Override
  public void createViewport(String userId,
                             String clientId,
                             ViewportDefinition viewportDefinition,
                             String viewportId,
                             String dataUrl,
                             String gridStructureUrl) {
    throw new UnsupportedOperationException("createViewport not implemented");
  }

  public void addViewport(String viewportId, Viewport viewport) {
    _viewports.put(viewportId, viewport);
  }
  
  public void sendUpdate(String update) {
    _listener.itemUpdated(update);
  }
}
