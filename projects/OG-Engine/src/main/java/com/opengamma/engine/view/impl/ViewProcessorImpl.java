/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.cache.ViewComputationCacheSource;
import com.opengamma.engine.calcnode.JobDispatcher;
import com.opengamma.engine.depgraph.DependencyGraphBuilderFactory;
import com.opengamma.engine.exec.DependencyGraphExecutorFactory;
import com.opengamma.engine.exec.stats.GraphExecutorStatisticsGathererProvider;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.resource.EngineResourceManagerImpl;
import com.opengamma.engine.resource.EngineResourceManagerInternal;
import com.opengamma.engine.view.ViewAutoStartManager;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessState;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientImpl;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.cycle.SingleComputationCycle;
import com.opengamma.engine.view.event.ViewProcessorEventListenerRegistry;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.engine.view.listener.ViewResultListenerFactory;
import com.opengamma.engine.view.permission.ViewPermissionContext;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.engine.view.permission.ViewPortfolioPermissionProvider;
import com.opengamma.engine.view.worker.ViewProcessWorkerFactory;
import com.opengamma.engine.view.worker.cache.ViewExecutionCache;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionedUniqueIdSupplier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

import net.sf.ehcache.util.NamedThreadFactory;

/**
 * Default implementation of {@link ViewProcessor}.
 */
public class ViewProcessorImpl implements ViewProcessorInternal {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessor.class);

  private static final String CLIENT_SCHEME = "ViewClient";
  private static final String PROCESS_SCHEME = "ViewProcess";
  private static final String CYCLE_SCHEME = "ViewCycle";

  private final AtomicLong _processIdSource = new AtomicLong();
  private final AtomicLong _clientIdSource = new AtomicLong();
  private final ReentrantLock _lifecycleLock = new ReentrantLock();
  private ScheduledExecutorService _clientResultTimer;

  private final EngineResourceManagerInternal<SingleComputationCycle> _cycleManager = new EngineResourceManagerImpl<>();

  private final ReentrantLock _processLock = new ReentrantLock();

  // Injected inputs
  private final String _name;
  private final ConfigSource _configSource;
  private final NamedMarketDataSpecificationRepository _namedMarketDataSpecificationRepository;
  private final CompiledFunctionService _functionCompilationService;
  private final FunctionResolver _functionResolver;
  private final MarketDataProviderResolver _marketDataProviderFactoryResolver;
  private final ViewComputationCacheSource _computationCacheSource;
  private final JobDispatcher _computationJobDispatcher;
  private final DependencyGraphBuilderFactory _dependencyGraphBuilderFactory;
  private final DependencyGraphExecutorFactory _dependencyGraphExecutorFactory;
  private final GraphExecutorStatisticsGathererProvider _graphExecutionStatistics;
  private final ViewPermissionProvider _viewPermissionProvider;
  private final ViewPortfolioPermissionProvider _viewPortfolioPermissionProvider;
  private final OverrideOperationCompiler _overrideOperationCompiler;
  private final ViewResultListenerFactory _viewResultListenerFactory;
  private final ViewProcessWorkerFactory _viewProcessWorkerFactory;
  private final ViewExecutionCache _executionCache;

  // State
  /**
   * ConcurrentHashMap to allow access for querying processes independently and concurrently to client attachment.
   */
  private final ConcurrentMap<UniqueId, ViewProcessImpl> _allProcessesById = new ConcurrentHashMap<>();
  private final Map<ViewProcessDescription, ViewProcessImpl> _sharedProcessesByDescription = new HashMap<>();

  private final ConcurrentMap<UniqueId, ViewClientImpl> _allClientsById = new ConcurrentHashMap<>();

  private final Map<UniqueId, Pair<ViewProcessImpl, ViewResultListener>> _clientToProcess = new HashMap<>();

  /**
   * The view processor event listener registry
   */
  private final ViewProcessorEventListenerRegistry _viewProcessorEventListenerRegistry = new ViewProcessorEventListenerRegistry();

  /**
   * Responsible for tracking which views should be automatically started when the engine starts.
   */
  private final ViewAutoStartManager _viewAutoStartManager;

  private boolean _isStarted;
  private boolean _isSuspended;

  /**
   * Interval (in seconds) at which to check permissions for market data.
   */
  private final int _permissionCheckInterval;

  public ViewProcessorImpl(final String name, final ConfigSource configSource, final NamedMarketDataSpecificationRepository namedMarketDataSpecificationRepository,
                           final CompiledFunctionService compiledFunctionService, final FunctionResolver functionResolver, final MarketDataProviderResolver marketDataProviderFactoryResolver,
                           final ViewComputationCacheSource computationCacheSource, final JobDispatcher jobDispatcher, final DependencyGraphBuilderFactory dependencyGraphBuilderFactory,
                           final DependencyGraphExecutorFactory dependencyGraphExecutorFactory, final GraphExecutorStatisticsGathererProvider graphExecutionStatisticsProvider,
                           final ViewPermissionProvider viewPermissionProvider, final ViewPortfolioPermissionProvider viewPortfolioPermissionProvider, final OverrideOperationCompiler overrideOperationCompiler,
                           final ViewResultListenerFactory viewResultListenerFactory, final ViewProcessWorkerFactory workerFactory, final ViewExecutionCache executionCache, final boolean useAutoStartViews) {

    this(name, configSource, namedMarketDataSpecificationRepository, compiledFunctionService,
         functionResolver, marketDataProviderFactoryResolver, computationCacheSource, jobDispatcher,
         dependencyGraphBuilderFactory, dependencyGraphExecutorFactory, graphExecutionStatisticsProvider,
         viewPermissionProvider, viewPortfolioPermissionProvider, overrideOperationCompiler,
         viewResultListenerFactory, workerFactory, executionCache, 0, useAutoStartViews);
  }

  public ViewProcessorImpl(final String name, final ConfigSource configSource, final NamedMarketDataSpecificationRepository namedMarketDataSpecificationRepository,
                           final CompiledFunctionService compiledFunctionService, final FunctionResolver functionResolver, final MarketDataProviderResolver marketDataProviderFactoryResolver,
                           final ViewComputationCacheSource computationCacheSource, final JobDispatcher jobDispatcher, final DependencyGraphBuilderFactory dependencyGraphBuilderFactory,
                           final DependencyGraphExecutorFactory dependencyGraphExecutorFactory, final GraphExecutorStatisticsGathererProvider graphExecutionStatisticsProvider,
                           final ViewPermissionProvider viewPermissionProvider, final ViewPortfolioPermissionProvider viewPortfolioPermissionProvider, final OverrideOperationCompiler overrideOperationCompiler,
                           final ViewResultListenerFactory viewResultListenerFactory, final ViewProcessWorkerFactory workerFactory, final ViewExecutionCache executionCache,
                           final int permissionCheckInterval, final boolean useAutoStartViews) {
    _name = name;
    _configSource = configSource;
    _namedMarketDataSpecificationRepository = namedMarketDataSpecificationRepository;
    _functionCompilationService = compiledFunctionService;
    _functionResolver = functionResolver;
    _marketDataProviderFactoryResolver = marketDataProviderFactoryResolver;
    _computationCacheSource = computationCacheSource;
    _computationJobDispatcher = jobDispatcher;
    _dependencyGraphBuilderFactory = dependencyGraphBuilderFactory;
    _dependencyGraphExecutorFactory = dependencyGraphExecutorFactory;
    _graphExecutionStatistics = graphExecutionStatisticsProvider;
    _viewPermissionProvider = viewPermissionProvider;
    _viewPortfolioPermissionProvider = viewPortfolioPermissionProvider;
    _overrideOperationCompiler = overrideOperationCompiler;
    _viewResultListenerFactory = viewResultListenerFactory;
    _viewProcessWorkerFactory = workerFactory;
    _executionCache = executionCache;
    _viewAutoStartManager = useAutoStartViews ? new ListeningViewAutoStartManager(configSource) : new NoOpViewAutoStartManager();
    _permissionCheckInterval = permissionCheckInterval;
  }

  //-------------------------------------------------------------------------
  @Override
  public String getName() {
    return _name;
  }

  @Override
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  @Override
  @SuppressWarnings("deprecation")
  public NamedMarketDataSpecificationRepository getNamedMarketDataSpecificationRepository() {
    return _namedMarketDataSpecificationRepository;
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<ViewProcessImpl> getViewProcesses() {
    return Collections.unmodifiableCollection(new ArrayList<>(_allProcessesById.values()));
  }

  @Override
  public Collection<ViewClient> getViewClients() {
    return Collections.unmodifiableCollection(new ArrayList<ViewClient>(_allClientsById.values()));
  }

  @Override
  public ViewProcessImpl getViewProcess(final UniqueId viewProcessId) {
    ArgumentChecker.notNull(viewProcessId, "viewProcessId");
    checkIdScheme(viewProcessId, PROCESS_SCHEME);
    final ViewProcessImpl process = _allProcessesById.get(viewProcessId);
    if (process == null) {
      throw new DataNotFoundException("View process not found: " + viewProcessId);
    }
    return process;
  }

  /**
   * Obtains a shared view process matching the given arguments, creating the process if necessary, and associates the client with that process.
   * 
   * @param clientId the unique identifier of the client, not null
   * @param listener the process listener, not null
   * @param viewDefinitionId the id of the view definition, not null
   * @param executionOptions the view execution options, not null
   * @param viewProcessContextMap contextual information to be added to log statements via MDC, may be null
   * @return the permission context to be used for access control, not null
   */
  public ViewPermissionContext attachClientToSharedViewProcess(final UniqueId clientId, final ViewResultListener listener, final UniqueId viewDefinitionId,
      final ViewExecutionOptions executionOptions, final Map<String, String> viewProcessContextMap) {
    ArgumentChecker.notNull(clientId, "clientId");
    ArgumentChecker.notNull(viewDefinitionId, "viewDefinitionId");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    final ViewPermissionContext result;
    final ViewClientImpl client = getViewClient(clientId);
    _processLock.lock();
    ViewProcessImpl process = null;
    try {
      process = getOrCreateSharedViewProcess(viewDefinitionId, executionOptions, client.getResultMode(), client.getFragmentResultMode(), viewProcessContextMap, false);
      result = attachClientToViewProcessCore(client, listener, process);
      process = null;
    } catch (final RuntimeException e) {
      s_logger.error("Error attaching client to shared view process", e);
      throw e;
    } finally {
      try {
        // Roll-back
        if (process != null) {
          removeViewProcessIfUnused(process);
        }
      } finally {
        _processLock.unlock();
      }
    }
    return result;
  }

  /**
   * Obtains a new, private view process, and associates the client with that process.
   * 
   * @param clientId the unique identifier of the client, not null
   * @param listener the process listener, not null
   * @param viewDefinitionId the id of the view definition, not null
   * @param executionOptions the view execution options, not null
   * @param viewProcessContextMap contextual information to be added to log statements via MDC
   * @return the permission provider to be used for access control, not null
   * @throws DataNotFoundException if the view definition identifier is invalid
   */
  public ViewPermissionContext attachClientToPrivateViewProcess(final UniqueId clientId, final ViewResultListener listener, final UniqueId viewDefinitionId,
      final ViewExecutionOptions executionOptions, final Map<String, String> viewProcessContextMap) {
    ArgumentChecker.notNull(viewDefinitionId, "definitionID");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    final ViewClientImpl client = getViewClient(clientId);
    final ViewPermissionContext result;
    ViewProcessImpl process = null;
    _processLock.lock();
    try {
      process = createViewProcess(viewDefinitionId, executionOptions, client.getResultMode(), client.getFragmentResultMode(), viewProcessContextMap, false);
      result = attachClientToViewProcessCore(client, listener, process);
      process = null;
    } catch (final RuntimeException e) {
      s_logger.error("Error attaching client to private view process", e);
      throw e;
    } finally {
      // Roll-back
      try {
        if (process != null) {
          shutdownViewProcess(process);
        }
      } finally {
        _processLock.unlock();
      }
    }
    return result;
  }

  /**
   * Obtains an existing view process, and associates the client with that process.
   * 
   * @param clientId the unique identifier of the client, not null
   * @param listener the process listener, not null
   * @param processId the unique identifier of the existing process, not null
   * @return the permission provider to be used for access control, not null
   */
  public ViewPermissionContext attachClientToViewProcess(final UniqueId clientId, final ViewResultListener listener, final UniqueId processId) {
    final ViewClientImpl client = getViewClient(clientId);

    _processLock.lock();
    try {
      final ViewProcessImpl process = getViewProcess(processId);
      return attachClientToViewProcessCore(client, listener, process);
    } catch (final Exception e) {
      // Nothing to roll back
      s_logger.error("Error attaching client to existing view process", e);
      throw new OpenGammaRuntimeException("Error attaching client to existing view process", e);
    } finally {
      _processLock.unlock();
    }
  }

  private ViewPermissionContext attachClientToViewProcessCore(final ViewClientImpl client, final ViewResultListener listener, final ViewProcessImpl process) {
    final Pair<ViewProcessImpl, ViewResultListener> processListenerPair = Pairs.of(process, listener);
    _processLock.lock();
    try {
      final Pair<ViewProcessImpl, ViewResultListener> existingAttachment = _clientToProcess.get(client.getUniqueId());
      if (existingAttachment != null) {
        throw new IllegalStateException("View client " + client.getUniqueId() + " is already attached to view process " + existingAttachment.getFirst().getUniqueId());
      }
      final ViewPermissionContext permissionProvider = process.attachListener(listener, client.getResultMode(), client.getFragmentResultMode());
      _clientToProcess.put(client.getUniqueId(), processListenerPair);
      return permissionProvider;
    } finally {
      _processLock.unlock();
    }
  }

  /**
   * Removes the association, if any, between a client and a view process. This may allow the view process to be terminated and removed.
   * 
   * @param clientId the unique identifier of the client, not null
   */
  public void detachClientFromViewProcess(final UniqueId clientId) {
    ArgumentChecker.notNull(clientId, "clientId");
    _processLock.lock();
    try {
      final Pair<ViewProcessImpl, ViewResultListener> processAttachment = _clientToProcess.remove(clientId);
      if (processAttachment == null) {
        return;
      }
      final ViewProcessImpl process = processAttachment.getFirst();
      final ViewResultListener listener = processAttachment.getSecond();
      process.detachListener(listener);

      removeViewProcessIfUnused(process);
    } finally {
      _processLock.unlock();
    }
  }

  private ViewProcessImpl getOrCreateSharedViewProcess(UniqueId viewDefinitionId, ViewExecutionOptions executionOptions, ViewResultMode resultMode, ViewResultMode fragmentResultMode,
      Map<String, String> viewProcessContextMap, boolean runPersistently) {
    _processLock.lock();
    try {
      final ViewProcessDescription viewDescription = new ViewProcessDescription(viewDefinitionId, executionOptions);
      ViewProcessImpl process = _sharedProcessesByDescription.get(viewDescription);
      if (process == null) {
        process = createViewProcess(viewDefinitionId, executionOptions, resultMode, fragmentResultMode, viewProcessContextMap, runPersistently);
        process.setDescriptionKey(viewDescription); // TEMPORARY - the execution options in the key might not match what the process was created with
        _sharedProcessesByDescription.put(viewDescription, process);
      }
      return process;
    } finally {
      _processLock.unlock();
    }
  }

  private ViewProcessImpl createViewProcess(UniqueId definitionId, ViewExecutionOptions viewExecutionOptions, ViewResultMode resultMode, ViewResultMode fragmentResultMode,
      Map<String, String> viewProcessContextMap, boolean runPersistently) {

    // TEMPORARY CODE - This method should be removed post credit work and supports Excel (Jim)
    ViewExecutionOptions executionOptions = verifyLiveDataViewExecutionOptions(viewExecutionOptions);
    // END TEMPORARY CODE

    _processLock.lock();

    try {
      // Either set or clear the map to ensure that anything there from a
      // previous run is removed. This can only happen where we have re-use
      // of threads e.g. remote clients managed by Jetty
      if (viewProcessContextMap != null && !viewProcessContextMap.isEmpty()) {
        MDC.setContextMap(viewProcessContextMap);
      } else {
        MDC.clear();
      }

      final String idValue = generateIdValue(_processIdSource);
      final UniqueId viewProcessId = UniqueId.of(PROCESS_SCHEME, idValue);
      final ViewProcessContext viewProcessContext = createViewProcessContext(viewProcessId, new VersionedUniqueIdSupplier(CYCLE_SCHEME, idValue));
      final ViewProcessImpl viewProcess =
          new ViewProcessImpl(definitionId, executionOptions, viewProcessContext, this, _permissionCheckInterval, runPersistently);

      // If executing in batch mode then attach a special listener to write incoming results into the batch db
      if (executionOptions.getFlags().contains(ViewExecutionFlags.BATCH)) {
        if (_viewResultListenerFactory == null) {
          throw new IllegalStateException("Batch mode requires a ViewResultListenerFactory");
        }
        viewProcess.attachListener(_viewResultListenerFactory.createViewResultListener(viewProcess.getLatestViewDefinition().getMarketDataUser()), resultMode, fragmentResultMode);
      }

      // The view must be created in a locked state if this view processor is suspended
      _lifecycleLock.lock();
      try {
        if (_isSuspended) {
          viewProcess.suspend();
        }
      } finally {
        _lifecycleLock.unlock();
      }

      _allProcessesById.put(viewProcessId, viewProcess);
      _viewProcessorEventListenerRegistry.notifyViewProcessAdded(viewProcessId);

      return viewProcess;
    } finally {
      _processLock.unlock();
    }
  }

  // TEMPORARY CODE - This method should be removed post credit work and supports Excel (Jim)
  private ViewExecutionOptions verifyLiveDataViewExecutionOptions(final ViewExecutionOptions executionOptions) {
    ViewCycleExecutionOptions defaultExecutionOptions = executionOptions.getDefaultExecutionOptions();
    if (defaultExecutionOptions != null) {
      List<MarketDataSpecification> specifications = defaultExecutionOptions.getMarketDataSpecifications();
      if (!specifications.isEmpty()) {
        specifications = new ArrayList<>(specifications);
        boolean changed = false;
        for (int i = 0; i < specifications.size(); i++) {
          final MarketDataSpecification specification = specifications.get(i);
          if (specification instanceof LiveMarketDataSpecification) {
            final String dataSource = ((LiveMarketDataSpecification) specification).getDataSource();
            if (dataSource != null && getNamedMarketDataSpecificationRepository() != null) {
              final MarketDataSpecification namedSpec = getNamedMarketDataSpecificationRepository().getSpecification(dataSource);
              if (namedSpec != null && !namedSpec.equals(specification)) {
                s_logger.info("Replacing live data {} with named spec {}", dataSource, namedSpec);
                specifications.set(i, namedSpec);
                changed = true;
              }
            }
          }
        }
        if (changed) {
          ViewCycleExecutionOptions defaultOptions = defaultExecutionOptions.copy().setMarketDataSpecifications(specifications).create();
          return new ExecutionOptions(executionOptions.getExecutionSequence(), executionOptions.getFlags(), executionOptions.getMaxSuccessiveDeltaCycles(), defaultOptions);
        }
      }
    }
    return executionOptions;
  }

  /**
   * Forcibly shuts down a view process and cleans up all resources.
   * 
   * @param viewProcessId the identifier of the view process
   */
  public void shutdownViewProcess(final UniqueId viewProcessId) {
    ViewProcessImpl viewProcess = getViewProcess(viewProcessId);
    shutdownViewProcess(viewProcess);
  }

  private void shutdownViewProcess(final ViewProcessImpl viewProcess) {
    s_logger.info("Removing view process {}", viewProcess);
    _processLock.lock();
    try {
      // Ignored if the process has already terminated (e.g. naturally)
      viewProcess.shutdownCore();

      _allProcessesById.remove(viewProcess.getUniqueId());
      final ViewProcessDescription description = (ViewProcessDescription) viewProcess.getDescriptionKey();
      final ViewProcessImpl sharedProc = _sharedProcessesByDescription.get(description);
      if (sharedProc != null && sharedProc == viewProcess) { //PLAT-1287
        _sharedProcessesByDescription.remove(description);
      }
    } finally {
      _processLock.unlock();
    }

    _viewProcessorEventListenerRegistry.notifyViewProcessRemoved(viewProcess.getUniqueId());
  }

  private void removeViewProcessIfUnused(ViewProcessImpl process) {
    if (process.getState() == ViewProcessState.RUNNING && isShared(process)) {
      return;
    }
    if (!process.hasExecutionDemand()) {
      shutdownViewProcess(process);
    }
  }

  private boolean isShared(ViewProcessImpl process) {
    ViewProcessDescription description = (ViewProcessDescription) process.getDescriptionKey();
    _processLock.lock();
    try {
      return _sharedProcessesByDescription.containsKey(description);
    } finally {
      _processLock.unlock();
    }
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the live data override injector for the view process currently associated with a client.
   * 
   * @param clientId the unique identifier of the client, not null
   * @return the live data override injector, not null
   * @throws IllegalStateException if the client is not associated with a view process
   */
  public MarketDataInjector getLiveDataOverrideInjector(final UniqueId clientId) {
    return getClientViewProcess(clientId).getLiveDataOverrideInjector();
  }

  /**
   * Gets the view definition being operated on by the process associated with a client.
   * 
   * @param clientId the unique identifier of the client, not null
   * @return the view definition, not null
   * @throws IllegalStateException if the client is not associated with a view process
   */
  public ViewDefinition getLatestViewDefinition(final UniqueId clientId) {
    return getClientViewProcess(clientId).getLatestViewDefinition();
  }

  /**
   * Requests that a computation cycle be run, even if none of the other triggers have fired since the last cycle.
   * 
   * @param clientId the unique identifier of the client, not null
   * @throws IllegalStateException if the client is not associated with a view process
   */
  public void triggerCycle(final UniqueId clientId) {
    getClientViewProcess(clientId).triggerCycle();
  }

  private ViewProcessImpl getClientViewProcess(final UniqueId clientId) {
    checkIdScheme(clientId, CLIENT_SCHEME);
    _processLock.lock();
    try {
      final Pair<ViewProcessImpl, ViewResultListener> clientAttachment = _clientToProcess.get(clientId);
      if (clientAttachment == null) {
        throw new IllegalStateException("Client " + clientId + " is not attached to a view process");
      }
      return clientAttachment.getFirst();
    } finally {
      _processLock.unlock();
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ViewClient createViewClient(final UserPrincipal clientUser) {
    ArgumentChecker.notNull(clientUser, "clientUser");
    final String idValue = generateIdValue(_clientIdSource);
    final UniqueId clientId = UniqueId.of(CLIENT_SCHEME, idValue);
    final ViewClientImpl client = new ViewClientImpl(clientId, this, clientUser, _clientResultTimer);
    _allClientsById.put(clientId, client);
    _viewProcessorEventListenerRegistry.notifyViewClientAdded(clientId);
    return client;
  }

  @Override
  public ViewClientImpl getViewClient(final UniqueId clientId) {
    ArgumentChecker.notNull(clientId, "clientId");
    checkIdScheme(clientId, CLIENT_SCHEME);
    final ViewClientImpl client = _allClientsById.get(clientId);
    if (client == null) {
      throw new DataNotFoundException("View client not found: " + clientId);
    }
    return client;
  }

  /**
   * Must be called by the client when it is terminated to ensure that associated resources are cleaned up.
   * 
   * @param clientId the unique identifier, not null
   */
  public void removeViewClient(final UniqueId clientId) {
    ArgumentChecker.notNull(clientId, "clientId");
    checkIdScheme(clientId, CLIENT_SCHEME);
    s_logger.info("Removing view client {}", clientId);
    final ViewClient client = _allClientsById.remove(clientId);
    if (client == null) {
      throw new DataNotFoundException("View client not found: " + clientId);
    }
    detachClientFromViewProcess(clientId);
    _viewProcessorEventListenerRegistry.notifyViewClientRemoved(clientId);
  }

  //-------------------------------------------------------------------------
  @Override
  public CompiledFunctionService getFunctionCompilationService() {
    return _functionCompilationService;
  }

  @Override
  public ViewProcessorEventListenerRegistry getViewProcessorEventListenerRegistry() {
    return _viewProcessorEventListenerRegistry;
  }

  //-------------------------------------------------------------------------
  @Override
  public EngineResourceManagerInternal<SingleComputationCycle> getViewCycleManager() {
    return _cycleManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Intended for testing.
   * 
   * @param viewClientId the unique identifier of the view client, not null
   * @return the view process, not null
   */
  public ViewProcessImpl getViewProcessForClient(final UniqueId viewClientId) {
    _processLock.lock();
    try {
      final ViewProcessImpl process = _clientToProcess.get(viewClientId).getFirst();
      if (process == null) {
        throw new OpenGammaRuntimeException("Client " + viewClientId + " is not attached to a process");
      }
      return process;
    } finally {
      _processLock.unlock();
    }
  }

  private void checkIdScheme(final UniqueId id, final String expectedScheme) {
    if (!expectedScheme.equals(id.getScheme())) {
      throw new IllegalArgumentException("Object is not from this view processor: expected scheme " + PROCESS_SCHEME + " but identifier was " + id);
    }
  }

  private ViewProcessContext createViewProcessContext(UniqueId processId, Supplier<UniqueId> cycleIds) {
    return new ViewProcessContext(processId, _configSource, _viewPermissionProvider, _viewPortfolioPermissionProvider, _marketDataProviderFactoryResolver, _functionCompilationService,
        _functionResolver, _computationCacheSource, _computationJobDispatcher, _viewProcessWorkerFactory, _dependencyGraphBuilderFactory, _dependencyGraphExecutorFactory,
        _graphExecutionStatistics, _overrideOperationCompiler, _cycleManager, cycleIds, _executionCache);
  }

  private String generateIdValue(final AtomicLong source) {
    return getName() + "-" + source.getAndIncrement();
  }

  //-------------------------------------------------------------------------
  // Lifecycle
  //-------------------------------------------------------------------------

  @Override
  public boolean isRunning() {
    _lifecycleLock.lock();
    try {
      return _isStarted;
    } finally {
      _lifecycleLock.unlock();
    }
  }

  @Override
  public void start() {
    final OperationTimer timer = new OperationTimer(s_logger, "Starting on lifecycle call");
    _lifecycleLock.lock();
    try {
      if (_isStarted) {
        return;
      }
      s_logger.info("Starting on lifecycle call.");
      _clientResultTimer = Executors.newScheduledThreadPool(1, new NamedThreadFactory("Shared ViewClient result timer"));
      _viewAutoStartManager.initialize();
      for (Map.Entry<String, AutoStartViewDefinition> entry : _viewAutoStartManager.getAutoStartViews().entrySet()) {
        autoStartView(entry.getKey(), entry.getValue());
      }
      _isStarted = true;
    } finally {
      _lifecycleLock.unlock();
    }
    timer.finished();
    _viewProcessorEventListenerRegistry.notifyViewProcessorStarted();
  }

  private void autoStartView(String viewName, AutoStartViewDefinition view) {
    UniqueId viewDefinitionId = view.getViewDefinitionId();
    try {
      ViewProcessImpl process = getOrCreateSharedViewProcess(viewDefinitionId, view.getExecutionOptions(),
      // These result mode options will be ignored so shouldn't really matter
      // but set to what web client would
          ViewResultMode.FULL_THEN_DELTA, ViewResultMode.NONE, null,
          // Run persistently
          true);

      s_logger.info("Auto-started view: {}", viewName);
      _viewProcessorEventListenerRegistry.notifyViewAutomaticallyStarted(process.getUniqueId(), viewName);

    } catch (RuntimeException e) {
      s_logger.error("Unable to auto-start view definition with id: {}", viewDefinitionId);
    }
  }

  @Override
  public Future<Runnable> suspend(final ExecutorService executor) {
    _lifecycleLock.lock();
    try {
      s_logger.info("Suspending running views.");
      if (_isSuspended) {
        throw new IllegalStateException("Already suspended");
      }
      _isSuspended = true;
      final List<Future<?>> suspends = new ArrayList<>(_allProcessesById.size());
      // Request all the views suspend
      for (final ViewProcessInternal view : _allProcessesById.values()) {
        suspends.add(executor.submit(new Runnable() {
          @Override
          public void run() {
            view.suspend();
          }
        }, null));
      }
      return executor.submit(new Runnable() {
        @Override
        public void run() {
          // Wait for all of the suspend operations to complete
          while (!suspends.isEmpty()) {
            final Future<?> suspend = suspends.remove(suspends.size() - 1);
            try {
              suspend.get(3000, TimeUnit.MILLISECONDS);
            } catch (final TimeoutException t) {
              s_logger.debug("Timeout waiting for view to suspend", t);
              suspends.add(suspend);
            } catch (final Throwable t) {
              s_logger.warn("Couldn't suspend view", t);
            }
          }
        }
      }, (Runnable) new Runnable() {
        @Override
        public void run() {
          // Resume all of the views
          _lifecycleLock.lock();
          try {
            _isSuspended = false;
            for (final ViewProcessInternal view : _allProcessesById.values()) {
              view.resume();
            }
          } finally {
            _lifecycleLock.unlock();
          }
        }
      });
    } finally {
      _lifecycleLock.unlock();
    }
  }

  @Override
  public void stop() {
    final ScheduledExecutorService clientResultTimer;
    _processLock.lock();
    _lifecycleLock.lock();
    try {
      if (!_isStarted) {
        return;
      }
      s_logger.info("Stopping on lifecycle call - terminating all children");

      for (final ViewProcessImpl viewProcess : getViewProcesses()) {
        shutdownViewProcess(viewProcess);
      }
      s_logger.info("All view processes terminated.");

      for (final ViewClient viewClient : getViewClients()) {
        viewClient.shutdown();
      }
      _allClientsById.clear();
      clientResultTimer = _clientResultTimer;
      _clientResultTimer = null;
      _isStarted = false;

      // REVIEW Andrew 2010-03-25 -- It might be coincidence, but if this gets called during undeploy/stop within a container the Bloomberg API explodes with a ton of NPEs.
      _viewProcessorEventListenerRegistry.notifyViewProcessorStopped();
    } finally {
      _lifecycleLock.unlock();
      _processLock.unlock();
    }
    if (clientResultTimer != null) {
      clientResultTimer.shutdown();
    }
  }

  //-------------------------------------------------------------------------
  private final class ViewProcessDescription {

    private final UniqueId _viewDefinitionId;
    private final ViewExecutionOptions _executionOptions;

    public ViewProcessDescription(final UniqueId definitionId, final ViewExecutionOptions executionOptions) {
      _viewDefinitionId = definitionId;
      _executionOptions = executionOptions;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _executionOptions.hashCode();
      result = prime * result + _viewDefinitionId.hashCode();
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof ViewProcessDescription)) {
        return false;
      }
      final ViewProcessDescription other = (ViewProcessDescription) obj;
      return _viewDefinitionId.equals(other._viewDefinitionId) && _executionOptions.equals(other._executionOptions);
    }

  }

}
