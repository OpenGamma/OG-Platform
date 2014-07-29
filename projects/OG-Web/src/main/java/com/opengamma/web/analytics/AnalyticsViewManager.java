/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.config.FunctionRepositoryFactory;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.financial.security.lookup.SecurityAttributeMapper;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.push.ClientConnection;
import com.opengamma.web.server.AggregatedViewDefinitionManager;

/**
 * Creates and manages {@link AnalyticsView} implementations.
 */
@SuppressWarnings("deprecation")
public class AnalyticsViewManager {

  /* TODO handle userId and clientId
  when view is created wrap in an impl that contains the IDs
  could just add them to add them to SimpleAnalyticsView
  when view is requested (including IDs) create another impl that wraps those IDs and delegates to the one that
  holds the real IDs. but before delegating it compares the IDs
  */

  private static final Logger s_logger = LoggerFactory.getLogger(AnalyticsViewManager.class);

  private final ViewProcessor _viewProcessor;
  private final ExecutionFlags.ParallelRecompilationMode _parallelViewRecompilation;
  private final AggregatedViewDefinitionManager _aggregatedViewDefManager;
  private final Map<String, AnalyticsViewClientConnection> _viewConnections = new ConcurrentHashMap<>();
  private final ComputationTargetResolver _targetResolver;
  private final FunctionRepositoryFactory _functions;
  private final NamedMarketDataSpecificationRepository _marketDataSpecificationRepository;
  private final SecurityAttributeMapper _blotterColumnMapper;
  private final PositionSource _positionSource;
  private final ConfigSource _configSource;
  private final SecuritySource _securitySource;
  private final SecurityMaster _securityMaster;
  private final PositionMaster _positionMaster;
  private final ExecutorService _portfolioResolutionExecutor;

  public AnalyticsViewManager(ViewProcessor viewProcessor, ExecutionFlags.ParallelRecompilationMode parallelViewRecompilation, AggregatedViewDefinitionManager aggregatedViewDefManager,
      ComputationTargetResolver targetResolver, FunctionRepositoryFactory functions, NamedMarketDataSpecificationRepository marketDataSpecificationRepository,
      SecurityAttributeMapper blotterColumnMapper, PositionSource positionSource, ConfigSource configSource, SecuritySource securitySource, SecurityMaster securityMaster,
      PositionMaster positionMaster) {
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(aggregatedViewDefManager, "aggregatedViewDefManager");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    ArgumentChecker.notNull(functions, "functions");
    ArgumentChecker.notNull(blotterColumnMapper, "blotterColumnMapper");
    ArgumentChecker.notNull(positionSource, "positionSource");
    ArgumentChecker.notNull(configSource, "configMaster");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    _positionSource = positionSource;
    _configSource = configSource;
    _securitySource = securitySource;
    _blotterColumnMapper = blotterColumnMapper;
    _targetResolver = targetResolver;
    _functions = functions;
    _viewProcessor = viewProcessor;
    _parallelViewRecompilation = parallelViewRecompilation;
    _aggregatedViewDefManager = aggregatedViewDefManager;
    _marketDataSpecificationRepository = marketDataSpecificationRepository;
    _securityMaster = securityMaster;
    _positionMaster = positionMaster;
    // TODO something more sophisticated / configurable here
    _portfolioResolutionExecutor = Executors.newFixedThreadPool(4);
  }
  
  public ViewRequest createViewRequest(UniqueId webId, List<String> aggregators,
      List<MarketDataSpecification> marketDataSpecs, Instant valuationTime, VersionCorrection portfolioVersionCorrection, boolean blotter) {
    if (isViewDefinitionId(webId)) {
      return new ViewRequest(webId, null, aggregators, marketDataSpecs, valuationTime, portfolioVersionCorrection, blotter);
    } else if (isViewProcessId(webId)) {
      // NOTE jonathan 2014-01-21 -- this is a hidden debugging feature. Parts of the UI make no sense in conjunction
      // with this, such as the ability to customise the aggregation.
      if (!aggregators.isEmpty()) {
        throw new OpenGammaRuntimeException("Cannot customise aggregation when attaching to an existing view process");
      }
      ViewProcess viewProcess = _viewProcessor.getViewProcess(webId);
      UniqueId viewDefinitionId = viewProcess.getDefinitionId();        
      return new ViewRequest(viewDefinitionId, webId, ImmutableList.<String>of(), marketDataSpecs, valuationTime, portfolioVersionCorrection, blotter);
    } else {
      throw new OpenGammaRuntimeException("Unknown identifier " + webId);
    }
  }

  private boolean isViewDefinitionId(UniqueId webId) {
    try {
      _configSource.get(webId);
      return true;
    } catch (IllegalArgumentException iae) {
      return false;
    }
  }
  
  private boolean isViewProcessId(UniqueId webId) {
    try {
      _viewProcessor.getViewProcess(webId);
      return true;
    } catch (DataNotFoundException e) {
      return false;
    }
  }

  /**
   * Creates a new view.
   * 
   * @param request Details of the view
   * @param clientId ID of the client connection
   * @param user User requesting the view
   * @param clientConnection Connection that will be notified of changes to the view
   * @param viewId ID of the view, must be unique
   * @param viewCallbackId ID that's passed to the listener when the view's portfolio grid structure changes
   * @param portfolioGridId ID that's passed to the listener when the view's portfolio grid structure changes
   * @param primitivesGridId ID that's passed to the listener when the view's primitives grid structure changes
   * @param errorId the error ID
   */
  public void createView(ViewRequest request, String clientId, UserPrincipal user, ClientConnection clientConnection, String viewId, Object viewCallbackId, String portfolioGridId,
      String primitivesGridId, String errorId) {
    if (_viewConnections.containsKey(viewId)) {
      throw new IllegalArgumentException("View ID " + viewId + " is already in use");
    }
    AggregatedViewDefinition aggregatedViewDef = new AggregatedViewDefinition(_aggregatedViewDefManager, request);
    ViewDefinition viewDef;
    VersionCorrection versionCorrection;
    if (request.getViewProcessId() == null) {
      viewDef = (ViewDefinition) _configSource.get(aggregatedViewDef.getUniqueId()).getValue();
      versionCorrection = request.getPortfolioVersionCorrection();
    } else {
      ViewProcess viewProcess = _viewProcessor.getViewProcess(request.getViewProcessId());
      viewDef = viewProcess.getLatestViewDefinition();
      versionCorrection = VersionCorrection.LATEST;
    }
    // this can be null for a primitives-only view
    UniqueId portfolioId = viewDef.getPortfolioId();
    Supplier<Portfolio> portfolioSupplier;
    ObjectId portfolioObjectId;
    if (portfolioId != null) {
      portfolioObjectId = portfolioId.getObjectId();
    } else {
      portfolioObjectId = null;
    }
    portfolioSupplier = new PortfolioSupplier(portfolioObjectId, versionCorrection, _positionSource, _securitySource, _portfolioResolutionExecutor);
    // TODO something a bit more sophisticated with the executor
    ViewClient viewClient = _viewProcessor.createViewClient(user);
    s_logger.debug("Client ID {} creating new view with ID {}", clientId, viewId);
    ViewportListener viewportListener = new LoggingViewportListener(viewClient);
    PortfolioEntityExtractor entityExtractor = new PortfolioEntityExtractor(versionCorrection, _securityMaster);
    // TODO add filtering change listener to portfolio master which calls portfolioChanged() on the outer view
    boolean primitivesOnly = portfolioId == null;
    ErrorManager errorManager = new ErrorManager(errorId);
    AnalyticsView view = new SimpleAnalyticsView(aggregatedViewDef.getUniqueId(), primitivesOnly, versionCorrection, viewId, portfolioGridId, primitivesGridId, _targetResolver, _functions,
        viewportListener, _blotterColumnMapper, portfolioSupplier, entityExtractor, request.showBlotterColumns(), errorManager);
    AnalyticsView lockingView = new LockingAnalyticsView(view);
    AnalyticsView notifyingView = new NotifyingAnalyticsView(lockingView, clientConnection);
    AnalyticsView timingView = new TimingAnalyticsView(notifyingView);
    AnalyticsView catchingView = new CatchingAnalyticsView(timingView, errorManager, clientConnection);
    AutoCloseable securityListener = new MasterNotificationListener<>(_securityMaster, catchingView);
    AutoCloseable positionListener = new MasterNotificationListener<>(_positionMaster, catchingView);
    AutoCloseable portfolioListener = new PortfolioListener(portfolioObjectId, catchingView, _positionSource);
    List<AutoCloseable> listeners = Lists.newArrayList(securityListener, positionListener, portfolioListener);
    AnalyticsViewClientConnection connection = new AnalyticsViewClientConnection(request, aggregatedViewDef, viewClient, catchingView, listeners, _parallelViewRecompilation,
        _marketDataSpecificationRepository, _portfolioResolutionExecutor, _securitySource);
    _viewConnections.put(viewId, connection);
    // need to notify the listener that the view has been created
    // TODO would it be neater to leave this to the constructor of NotifyingAnalyticsView
    clientConnection.itemUpdated(viewCallbackId);
    connection.start();
    clientConnection.addDisconnectionListener(new DisconnectionListener(viewId));
  }

  /**
   * Deletes a view.
   * 
   * @param viewId ID of the view
   * @throws DataNotFoundException If there's no view with the specified ID
   */
  public void deleteView(String viewId) {
    AnalyticsViewClientConnection connection = _viewConnections.remove(viewId);
    if (connection == null) {
      s_logger.debug("Received request to delete unknown view ID {}", viewId);
      throw new DataNotFoundException("No view found with ID " + viewId);
    }
    s_logger.debug("Closing view with ID {}", viewId);
    connection.close();
  }

  /**
   * Returns a view given its ID.
   * 
   * @param viewId ID of the view
   * @return The view
   * @throws DataNotFoundException If there's no view with the specified ID
   */
  public AnalyticsView getView(String viewId) {
    AnalyticsViewClientConnection connection = _viewConnections.get(viewId);
    if (connection == null) {
      s_logger.debug("Received request for unknown view ID {}", viewId);
      throw new DataNotFoundException("No view found with ID " + viewId);
    }
    return connection.getView();
  }

  /**
   * Returns a view client given its view ID.
   * 
   * @param viewId ID of the view
   * @return the view client.
   * @throws DataNotFoundException If there's no view with the specified ID
   */
  public ViewClient getViewCient(String viewId) {
    AnalyticsViewClientConnection connection = _viewConnections.get(viewId);
    if (connection == null) {
      s_logger.debug("Received request for unknown view ID {}", viewId);
      throw new DataNotFoundException("No view found with ID " + viewId);
    }
    return connection.getViewClient();
  }

  /**
   * Closes a view if it's still open when the associated client disconnects.
   */
  private final class DisconnectionListener implements ClientConnection.DisconnectionListener {

    private final String _viewId;

    private DisconnectionListener(String viewId) {
      _viewId = viewId;
    }

    @Override
    public void clientDisconnected() {
      AnalyticsViewClientConnection connection = _viewConnections.remove(_viewId);
      if (connection != null) {
        s_logger.debug("Client disconnected, closing view with ID {}", _viewId);
        connection.close();
      }
    }
  }
}
