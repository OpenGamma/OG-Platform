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
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.AggregatedViewDefinitionManager;

/**
 * TODO handle userId and clientId
 * when view is created wrap in an impl that contains the IDs
 * could just add them to add them to SimpleAnalyticsView
 * when view is requested (including IDs) create another impl that wraps those IDs and delegates to the one that
 * holds the real IDs. but before delegating it compares the IDs
 */
public class AnalyticsViewManager {

  private static final Logger s_logger = LoggerFactory.getLogger(AnalyticsViewManager.class);

  private final ViewProcessor _viewProcessor;
  private final AggregatedViewDefinitionManager _aggregatedViewDefManager;
  private final MarketDataSnapshotMaster _snapshotMaster;
  private final Map<String, AnalyticsViewClientConnection> _viewConnections = new ConcurrentHashMap<String, AnalyticsViewClientConnection>();
  private final ComputationTargetResolver _targetResolver;
  private final ResultsFormatter _formatter;


  public AnalyticsViewManager(ViewProcessor viewProcessor,
                              AggregatedViewDefinitionManager aggregatedViewDefManager,
                              MarketDataSnapshotMaster snapshotMaster,
                              ComputationTargetResolver targetResolver,
                              ResultsFormatter formatter) {
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(aggregatedViewDefManager, "aggregatedViewDefManager");
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    ArgumentChecker.notNull(formatter, "formatter");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    _targetResolver = targetResolver;
    _formatter = formatter;
    _viewProcessor = viewProcessor;
    _aggregatedViewDefManager = aggregatedViewDefManager;
    _snapshotMaster = snapshotMaster;
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
    SimpleAnalyticsView view = new SimpleAnalyticsView(listener, portfolioGridId, primitivesGridId, _targetResolver, _formatter);
    LockingAnalyticsView lockingView = new LockingAnalyticsView(view);
    NamedMarketDataSpecificationRepository marketDataSpecRepo = _viewProcessor.getNamedMarketDataSpecificationRepository();
    AnalyticsViewClientConnection connection = new AnalyticsViewClientConnection(request,
                                                                                 viewClient,
                                                                                 lockingView,
                                                                                 marketDataSpecRepo,
                                                                                 _aggregatedViewDefManager,
                                                                                 _snapshotMaster);
    _viewConnections.put(viewId, connection);
    connection.start();
    s_logger.debug("Created new view with ID {}", viewId);
  }

  public void deleteView(String viewId) {
    AnalyticsViewClientConnection connection = _viewConnections.remove(viewId);
    if (connection == null) {
      throw new DataNotFoundException("No view found with ID " + viewId);
    }
    connection.close();
  }

  public AnalyticsView getView(String viewId) {
    AnalyticsViewClientConnection connection = _viewConnections.get(viewId);
    if (connection == null) {
      throw new DataNotFoundException("No view found with ID " + viewId);
    }
    return connection.getView();
  }
}
