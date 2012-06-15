/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class AnalyticsViewManager {

  private static final Logger s_logger = LoggerFactory.getLogger(AnalyticsViewManager.class);

  private final ViewProcessor _viewProcessor;
  private final Map<String, AnalyticsViewClientConnection> _viewConnections =
      new ConcurrentHashMap<String, AnalyticsViewClientConnection>();

  public AnalyticsViewManager(ViewProcessor viewProcessor) {
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    _viewProcessor = viewProcessor;
  }

  public void createView(ViewRequest request,
                         UserPrincipal user,
                         AnalyticsViewListener listener,
                         String viewId,
                         String portfolioGridId,
                         String primitivesGridId) {
    if (_viewConnections.containsKey(viewId)) {
      throw new IllegalArgumentException("View ID " + viewId + " is already in use");
    }
    ViewClient viewClient = _viewProcessor.createViewClient(user);
    SimpleAnalyticsView view = new SimpleAnalyticsView(listener, portfolioGridId, primitivesGridId);
    AnalyticsViewClientConnection connection = new AnalyticsViewClientConnection(request, viewClient, view);
    _viewConnections.put(viewId, connection);
    connection.start();
    s_logger.debug("Created new view with ID {}", viewId);
  }

  public void deleteView(String viewId) {
    AnalyticsViewClientConnection connection = _viewConnections.remove(viewId);
    if (connection == null) {
      throw new DataNotFoundException("No view found with ID " + viewId);
    }
    connection.stop();
  }

  public AnalyticsView getView(String viewId) {
    AnalyticsViewClientConnection connection = _viewConnections.get(viewId);
    if (connection == null) {
      throw new DataNotFoundException("No view found with ID " + viewId);
    }
    return connection.getView();
  }
}
