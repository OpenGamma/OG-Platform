/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.AggregatedViewDefinitionManager;

/**
 *
 */
/* package */ class AnalyticsViewClientConnection extends AbstractViewResultListener {

  private final AnalyticsView _view;
  private final ViewClient _viewClient;
  private final AggregatedViewDefinition _aggregatedViewDef;
  private final ViewExecutionOptions _executionOptions;

  private EngineResourceReference<? extends ViewCycle> _cycleReference = EmptyViewCycle.REFERENCE;

  public AnalyticsViewClientConnection(ViewRequest viewRequest,
                                       ViewClient viewClient,
                                       AnalyticsView view,
                                       NamedMarketDataSpecificationRepository namedMarketDataSpecRepo,
                                       AggregatedViewDefinitionManager aggregatedViewDefManager,
                                       MarketDataSnapshotMaster snapshotMaster) {
    ArgumentChecker.notNull(viewRequest, "viewRequest");
    ArgumentChecker.notNull(viewClient, "viewClient");
    ArgumentChecker.notNull(view, "view");
    ArgumentChecker.notNull(namedMarketDataSpecRepo, "namedMarketDataSpecRepo");
    ArgumentChecker.notNull(aggregatedViewDefManager, "aggregatedViewDefManager");
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    _view = view;
    _viewClient = viewClient;
    _aggregatedViewDef = new AggregatedViewDefinition(aggregatedViewDefManager, viewRequest);
    _executionOptions = viewRequest.getMarketData().createExecutionOptions(snapshotMaster, namedMarketDataSpecRepo);
  }

  @Override
  public UserPrincipal getUser() {
    return _viewClient.getUser();
  }

  @Override
  public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
    _view.updateStructure(compiledViewDefinition);
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    // TODO use deltaResult and be more intelligent about viewport updates? do delta results work?
    EngineResourceReference<? extends ViewCycle> cycleReference = _viewClient.createCycleReference(fullResult.getViewCycleId());
    if (cycleReference == null) {
      // this shouldn't happen if everything in the engine is working as it should
      _cycleReference = EmptyViewCycle.REFERENCE;
    } else {
      _cycleReference = cycleReference;
    }
    _view.updateResults(fullResult, _cycleReference.get());
  }

  /* package */ void start() {
    _viewClient.setResultListener(this);
    _viewClient.setViewCycleAccessSupported(true);
    try {
      _viewClient.attachToViewProcess(_aggregatedViewDef.getUniqueId(), _executionOptions);
    } catch (Exception e) {
      _aggregatedViewDef.close();
      throw new OpenGammaRuntimeException("Failed to attach view client to view process", e);
    }
  }

  /* package */ void close() {
    try {
      _viewClient.detachFromViewProcess();
    } finally {
      _cycleReference.release();
      _aggregatedViewDef.close();
    }
  }

  /* package */ AnalyticsView getView() {
    return _view;
  }

  /**
   * Wrapper that hides a bit of the ugliness of {@link AggregatedViewDefinitionManager}.
   */
  private static class AggregatedViewDefinition {

    private final AggregatedViewDefinitionManager _aggregatedViewDefManager;
    private final UniqueId _baseViewDefId;
    private final String _aggregatorName;
    private final UniqueId _id;

    private AggregatedViewDefinition(AggregatedViewDefinitionManager aggregatedViewDefManager, ViewRequest viewRequest) {
      ArgumentChecker.notNull(aggregatedViewDefManager, "aggregatedViewDefManager");
      ArgumentChecker.notNull(viewRequest, "viewRequest");
      _aggregatedViewDefManager = aggregatedViewDefManager;
      _baseViewDefId = viewRequest.getViewDefinitionId();
      List<String> aggregators = viewRequest.getAggregators();
      if (!aggregators.isEmpty()) {
        // TODO support multiple aggregtors
        _aggregatorName = aggregators.get(0);
      } else {
        _aggregatorName = null;
      }
      try {
        _id = _aggregatedViewDefManager.getViewDefinitionId(_baseViewDefId, _aggregatorName);
      } catch (Exception e) {
        close();
        throw new OpenGammaRuntimeException("Failed to get aggregated view definition", e);
      }
    }

    private UniqueId getUniqueId() {
      return _id;
    }

    private void close() {
      _aggregatedViewDefManager.releaseViewDefinition(_baseViewDefId, _aggregatorName);
    }
  }
}
