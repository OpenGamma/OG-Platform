/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.InfiniteViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.AggregatedViewDefinitionManager;

/**
 * Connects the engine to an {@link AnalyticsView}. Contains the logic for setting up a {@link ViewClient},
 * connecting it to a view process, handling events from the engine and forwarding data to the {@code ViewClient}.
 */
/* package */ class AnalyticsViewClientConnection {

  private final AnalyticsView _view;
  private final ViewClient _viewClient;
  private final AggregatedViewDefinition _aggregatedViewDef;
  private final ViewExecutionOptions _executionOptions;
  private final NamedMarketDataSpecificationRepository _marketDataSpecRepo;

  private EngineResourceReference<? extends ViewCycle> _cycleReference = EmptyViewCycle.REFERENCE;

  /**
   * @param viewRequest Defines the view that should be created
   * @param viewClient Connects this class to the calculation engine
   * @param view The object that encapsulates the state of the view user interface
   * @param marketDataSpecRepo For looking up sources of market data
   * @param aggregatedViewDefManager For looking up view definitions
   * @param snapshotMaster For looking up snapshots
   */
  /* package */ AnalyticsViewClientConnection(ViewRequest viewRequest,
                                              ViewClient viewClient,
                                              AnalyticsView view,
                                              NamedMarketDataSpecificationRepository marketDataSpecRepo,
                                              AggregatedViewDefinitionManager aggregatedViewDefManager,
                                              MarketDataSnapshotMaster snapshotMaster) {
    ArgumentChecker.notNull(viewRequest, "viewRequest");
    ArgumentChecker.notNull(viewClient, "viewClient");
    ArgumentChecker.notNull(view, "view");
    ArgumentChecker.notNull(marketDataSpecRepo, "marketDataSpecRepo");
    ArgumentChecker.notNull(aggregatedViewDefManager, "aggregatedViewDefManager");
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    _view = view;
    _viewClient = viewClient;
    _aggregatedViewDef = new AggregatedViewDefinition(aggregatedViewDefManager, viewRequest);
    _marketDataSpecRepo = marketDataSpecRepo;
    List<MarketDataSpecification> requestedMarketDataSpecs = viewRequest.getMarketDataSpecs();
    List<MarketDataSpecification> actualMarketDataSpecs = fixMarketDataSpecs(requestedMarketDataSpecs);
    ViewCycleExecutionOptions defaultOptions =
        new ViewCycleExecutionOptions(viewRequest.getValuationTime(), actualMarketDataSpecs);
    _executionOptions = ExecutionOptions.of(new InfiniteViewCycleExecutionSequence(),
                                            defaultOptions,
                                            // this recalcs periodically or when market data changes. might need to give
                                            // the user the option to specify the behaviour
                                            ExecutionFlags.triggersEnabled().get(),
                                            viewRequest.getPortfolioVersionCorrection());
  }

  /**
   * This is a temporary hack to allow the old and new web interfaces to run side by side. The old UI shows a mixture
   * of data sources including live sources, multiple live sources combined, live sources backed by historical data
   * and pure historical data. The new UI only shows live sources, and the names of those sources don't match the
   * names in the old UI (which include something to tell the user it's a live source). The real data sources
   * are looked up using the old names so this method rebuilds the list of data sources and replaces the new source
   * specs with the old ones.
   * @param requestedMarketDataSpecs The market data sources requested by the user
   * @return The specs needed to look up the sources the user requested
   */
  private List<MarketDataSpecification> fixMarketDataSpecs(List<MarketDataSpecification> requestedMarketDataSpecs) {
    List<MarketDataSpecification> specs = Lists.newArrayListWithCapacity(requestedMarketDataSpecs.size());
    for (MarketDataSpecification spec : requestedMarketDataSpecs) {
      if (spec instanceof LiveMarketDataSpecification) {
        LiveMarketDataSpecification liveSpec = (LiveMarketDataSpecification) spec;
        MarketDataSpecification oldSpec = _marketDataSpecRepo.getSpecification(liveSpec.getDataSource());
        if (oldSpec == null) {
          throw new IllegalArgumentException("No live data source found called " + liveSpec.getDataSource());
        }
        specs.add(oldSpec);
      } else {
        specs.add(spec);
      }
    }
    return specs;
  }

  /**
   * Connects to the engine in order to start receiving results. This should only be called once.
   */
  /* package */ void start() {
    _viewClient.setResultListener(new Listener());
    _viewClient.setViewCycleAccessSupported(true);
    _viewClient.setResultMode(ViewResultMode.FULL_THEN_DELTA);
    try {
      _viewClient.attachToViewProcess(_aggregatedViewDef.getUniqueId(), _executionOptions);
    } catch (Exception e) {
      _aggregatedViewDef.close();
      throw new OpenGammaRuntimeException("Failed to attach view client to view process", e);
    }
  }

  /**
   * Disconects from the engine and releases all resources. This should only be called once.
   */
  /* package */ void close() {
    try {
      _viewClient.detachFromViewProcess();
    } finally {
      _cycleReference.release();
      _aggregatedViewDef.close();
    }
  }

  /**
   * @return The view to which this object sends data received from the engine.
   */
  /* package */ AnalyticsView getView() {
    return _view;
  }

  /**
   * Listener for view results. This is an inner class to avoid polluting the interface of the parent class with
   * public callback methods.
   */
  private class Listener extends AbstractViewResultListener {

    @Override
    public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
      _cycleReference.release();
      ViewResultModel results = deltaResult != null ? deltaResult : fullResult;
      // always retain a reference to the most recent cycle so the dependency graphs are available at all times.
      // without this it would be necessary to wait at least one cycle before it would be possible to access the graphs.
      // this allows dependency graphs grids to be opened and populated without any delay
      EngineResourceReference<? extends ViewCycle> cycleReference = _viewClient.createCycleReference(results.getViewCycleId());
      if (cycleReference == null) {
        // this shouldn't happen if everything in the engine is working as it should
        _cycleReference = EmptyViewCycle.REFERENCE;
      } else {
        _cycleReference = cycleReference;
      }
      _view.updateResults(results, _cycleReference.get());
    }

    @Override
    public UserPrincipal getUser() {
      return _viewClient.getUser();
    }

    @Override
    public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
      _view.updateStructure(compiledViewDefinition);
    }
  }

  /**
   * Wrapper that hides a bit of the ugliness of {@link AggregatedViewDefinitionManager}.
   */
  private static final class AggregatedViewDefinition {

    private final AggregatedViewDefinitionManager _aggregatedViewDefManager;
    private final UniqueId _baseViewDefId;
    private final List<String> _aggregatorNames;
    private final UniqueId _id;

    private AggregatedViewDefinition(AggregatedViewDefinitionManager aggregatedViewDefManager, ViewRequest viewRequest) {
      ArgumentChecker.notNull(aggregatedViewDefManager, "aggregatedViewDefManager");
      ArgumentChecker.notNull(viewRequest, "viewRequest");
      _aggregatedViewDefManager = aggregatedViewDefManager;
      _baseViewDefId = viewRequest.getViewDefinitionId();
      _aggregatorNames = viewRequest.getAggregators();
      try {
        _id = _aggregatedViewDefManager.getViewDefinitionId(_baseViewDefId, _aggregatorNames);
      } catch (Exception e) {
        close();
        throw new OpenGammaRuntimeException("Failed to get aggregated view definition", e);
      }
    }

    private UniqueId getUniqueId() {
      return _id;
    }

    private void close() {
      _aggregatedViewDefManager.releaseViewDefinition(_baseViewDefId, _aggregatorNames);
    }
  }
}
