/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.manipulator.MarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.NoOpMarketDataSelector;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.InfiniteViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Connects the engine to an {@link AnalyticsView}. Contains the logic for setting up a {@link ViewClient}, connecting
 * it to a view process, handling events from the engine and forwarding data to the
 * {@code ViewClient}.
 */
@SuppressWarnings("deprecation")
/* package */ class AnalyticsViewClientConnection {

  private static final Logger s_logger = LoggerFactory.getLogger(AnalyticsViewClientConnection.class);

  private final AnalyticsView _view;
  private final ViewClient _viewClient;
  private final AggregatedViewDefinition _aggregatedViewDef;
  private final ViewRequest _viewRequest;
  private final ExecutionFlags.ParallelRecompilationMode _parallelViewRecompilation;
  private final NamedMarketDataSpecificationRepository _marketDataSpecRepo;
  private final List<AutoCloseable> _listeners;
  private final ExecutorService _executor;
  private final SecuritySource _securitySource;

  private EngineResourceReference<? extends ViewCycle> _cycleReference = EmptyViewCycle.REFERENCE;

  /**
   * @param viewRequest Defines the view that should be created
   * @param aggregatedViewDef The view definition including any aggregation
   * @param viewClient Connects this class to the calculation engine
   * @param view The object that encapsulates the state of the view user interface
   * @param parallelViewRecompilation Whether to recompile the view whilst running the current version
   * @param marketDataSpecificationRepository For looking up market data specs
   * @param executor
   * @param securitySource
   */
  /* package */ AnalyticsViewClientConnection(ViewRequest viewRequest,
                                              AggregatedViewDefinition aggregatedViewDef,
                                              ViewClient viewClient,
                                              AnalyticsView view,
                                              List<AutoCloseable> listeners,
                                              ExecutionFlags.ParallelRecompilationMode parallelViewRecompilation,
                                              NamedMarketDataSpecificationRepository marketDataSpecificationRepository,
                                              ExecutorService executor,
                                              SecuritySource securitySource) {
    ArgumentChecker.notNull(viewRequest, "viewRequest");
    ArgumentChecker.notNull(viewClient, "viewClient");
    ArgumentChecker.notNull(view, "view");
    ArgumentChecker.notNull(listeners, "listeners");
    ArgumentChecker.notNull(executor, "executor");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _executor = executor;
    _securitySource = securitySource;
    _view = view;
    _viewRequest = viewRequest;
    _viewClient = viewClient;
    _aggregatedViewDef = aggregatedViewDef;
    _marketDataSpecRepo = marketDataSpecificationRepository;
    _listeners = listeners;
    _parallelViewRecompilation = parallelViewRecompilation;
    // this recalcs periodically or when market data changes. might need to give
    // the user the option to specify the behaviour
  }

  /**
   * This is a temporary hack to allow the old and new web interfaces to run side by side. The old UI shows a mixture of
   * data sources including live sources, multiple live sources combined, live
   * sources backed by historical data and pure historical data. The new UI only shows live sources, and the names of
   * those sources don't match the names in the old UI (which include something to tell
   * the user it's a live source). The real data sources are looked up using the old names so this method rebuilds the
   * list of data sources and replaces the new source specs with the old ones.
   *
   * @param requestedMarketDataSpecs The market data sources requested by the user
   * @return The specs needed to look up the sources the user requested
   */
  private List<MarketDataSpecification> fixMarketDataSpecs(List<MarketDataSpecification> requestedMarketDataSpecs) {
    if (_marketDataSpecRepo == null) {
      return requestedMarketDataSpecs;
    }
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
    s_logger.debug("Starting client connection");
    _viewClient.setResultListener(new Listener());
    _viewClient.setViewCycleAccessSupported(true);
    _viewClient.setResultMode(ViewResultMode.FULL_THEN_DELTA);
    try {
      if (_viewRequest.getViewProcessId() == null) {
        List<MarketDataSpecification> requestedMarketDataSpecs = _viewRequest.getMarketDataSpecs();
        List<MarketDataSpecification> actualMarketDataSpecs = fixMarketDataSpecs(requestedMarketDataSpecs);

        // TODO - At this point we need to pick up a shift specification from the UI - for now we'll add the NoOp
        MarketDataSelector marketDataSelector = NoOpMarketDataSelector.getInstance();

        ViewCycleExecutionOptions defaultOptions =
            ViewCycleExecutionOptions
                .builder()
                .setValuationTime(_viewRequest.getValuationTime())
                .setMarketDataSpecifications(actualMarketDataSpecs)
                .setMarketDataSelector(marketDataSelector)
                .setResolverVersionCorrection(_viewRequest.getPortfolioVersionCorrection())
                .create();
        EnumSet<ViewExecutionFlags> flags =
            ExecutionFlags.triggersEnabled().parallelCompilation(_parallelViewRecompilation).get();
        ViewExecutionOptions executionOptions = ExecutionOptions.of(new InfiniteViewCycleExecutionSequence(), defaultOptions, flags);
        _viewClient.attachToViewProcess(_aggregatedViewDef.getUniqueId(), executionOptions);
      } else {
        _viewClient.attachToViewProcess(_viewRequest.getViewProcessId());
      }
    } catch (Exception e) {
      _aggregatedViewDef.close();
      throw new OpenGammaRuntimeException("Failed to attach view client to view process", e);
    }
  }

  /**
   * Disconnects from the engine and releases all resources. This should only be called once.
   */
  /* package */ void close() {
    try {
      _viewClient.detachFromViewProcess();
      _viewClient.shutdown();
    } finally {
      _cycleReference.release();
      _aggregatedViewDef.close();
      for (AutoCloseable listener : _listeners) {
        try {
          listener.close();
        } catch (Exception e) {
          s_logger.warn("Failed to close listener " + listener, e);
        }
      }
    }
  }

  /**
   * @return The view to which this object sends data received from the engine.
   */
  /* package */ AnalyticsView getView() {
    return _view;
  }
  
  /**
   * Gets the viewClient.
   * @return the viewClient
   */
  /* package */ ViewClient getViewClient() {
    return _viewClient;
  }

  /**
   * Listener for view results. This is an inner class to avoid polluting the interface of the parent class with public
   * callback methods.
   */
  private class Listener extends AbstractViewResultListener {

    @Override
    public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
      EngineResourceReference<? extends ViewCycle> oldReference = _cycleReference;
      try {
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
      } finally {
        // don't release the reference to the previous cycle until the view has received an update containing
        // the new one. this prevents the view making a request to a released cycle
        oldReference.release();
      }
    }

    @Override
    public UserPrincipal getUser() {
      return _viewClient.getUser();
    }

    @Override
    public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
      s_logger.debug("View definition compiled: '{}'", compiledViewDefinition.getViewDefinition().getName());
      // resolve the portfolio, it won't be resolved if the engine is in a different VM from the web components
      // if it's in the same VM then resolution is fairly cheap and doesn't touch the DB
      Portfolio portfolio = compiledViewDefinition.getPortfolio();
      Portfolio resolvedPortfolio;
      if (portfolio != null) {
        resolvedPortfolio = PortfolioCompiler.resolvePortfolio(portfolio, _executor, _securitySource);
      } else {
        resolvedPortfolio = null;
      }
      _view.updateStructure(compiledViewDefinition, resolvedPortfolio);
    }

    @Override
    public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
      s_logger.warn("Compilation of the view definition failed", exception);
      viewFailed(exception);
    }

    @Override
    public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
      s_logger.warn("Execution of the view failed", exception);
      viewFailed(exception);
    }

    private void viewFailed(Exception exception) {
      // the underlying cause is likely more interesting when a view fails
      Throwable cause = exception.getCause();
      _view.viewCompilationFailed(cause != null ? cause : exception);
    }
  }

}
