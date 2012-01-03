/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.CachingComputationTargetResolver;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.marketdata.live.LiveMarketDataSourceRegistry;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calc.EngineResourceManagerImpl;
import com.opengamma.engine.view.calc.EngineResourceManagerInternal;
import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGathererProvider;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientImpl;
import com.opengamma.engine.view.event.ViewProcessorEventListenerRegistry;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.Pair;

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
  private final Timer _clientResultTimer = new Timer("Shared ViewClient result timer");
  
  private final EngineResourceManagerInternal<SingleComputationCycle> _cycleManager = new EngineResourceManagerImpl<SingleComputationCycle>();
  
  private final ReentrantLock _processLock = new ReentrantLock();
  
  // Injected inputs
  private final UniqueId _uniqueId;
  private final ViewDefinitionRepository _viewDefinitionRepository;
  private final LiveMarketDataSourceRegistry _liveMarketDataSourceRegistry;
  private final SecuritySource _securitySource;
  private final PositionSource _positionSource;
  private final CachingComputationTargetResolver _computationTargetResolver;
  private final CompiledFunctionService _functionCompilationService;
  private final FunctionResolver _functionResolver;
  private final MarketDataProviderResolver _marketDataProviderFactoryResolver;
  private final ViewComputationCacheSource _computationCacheSource;
  private final JobDispatcher _computationJobDispatcher;
  private final ViewProcessorQueryReceiver _viewProcessorQueryReceiver;
  private final DependencyGraphExecutorFactory<?> _dependencyGraphExecutorFactory;
  private final GraphExecutorStatisticsGathererProvider _graphExecutionStatistics;
  private final ViewPermissionProvider _viewPermissionProvider;
  private final OverrideOperationCompiler _overrideOperationCompiler;
  
  // State
  /**
   * ConcurrentHashMap to allow access for querying processes independently and concurrently to client attachment.
   */
  private final ConcurrentMap<UniqueId, ViewProcessImpl> _allProcessesById = new ConcurrentHashMap<UniqueId, ViewProcessImpl>();
  private final Map<ViewProcessDescription, ViewProcessImpl> _sharedProcessesByDescription = new HashMap<ViewProcessDescription, ViewProcessImpl>();
  
  private final ConcurrentMap<UniqueId, ViewClientImpl> _allClientsById = new ConcurrentHashMap<UniqueId, ViewClientImpl>();
  
  private final Map<UniqueId, Pair<ViewProcessImpl, ViewResultListener>> _clientToProcess = new HashMap<UniqueId, Pair<ViewProcessImpl, ViewResultListener>>();
  
  /**
   * The view processor event listener registry
   */
  private final ViewProcessorEventListenerRegistry _viewProcessorEventListenerRegistry = new ViewProcessorEventListenerRegistry();
  
  private boolean _isStarted;
  private boolean _isSuspended;
  
  // TODO: would it be easier to pass in a ViewProcessContext to this constructor ?

  public ViewProcessorImpl(
      UniqueId uniqueId,
      ViewDefinitionRepository viewDefinitionRepository,
      LiveMarketDataSourceRegistry liveMarketDataSourceRegistry,
      SecuritySource securitySource,
      PositionSource positionSource,
      CachingComputationTargetResolver computationTargetResolver,
      CompiledFunctionService compiledFunctionService,
      FunctionResolver functionResolver,
      MarketDataProviderResolver marketDataProviderFactoryResolver,
      ViewComputationCacheSource computationCacheSource,
      JobDispatcher jobDispatcher,
      ViewProcessorQueryReceiver viewProcessorQueryReceiver,
      DependencyGraphExecutorFactory<?> dependencyGraphExecutorFactory,
      GraphExecutorStatisticsGathererProvider graphExecutionStatisticsProvider,
      ViewPermissionProvider viewPermissionProvider,
      OverrideOperationCompiler overrideOperationCompiler) {
    _uniqueId = uniqueId;
    _viewDefinitionRepository = viewDefinitionRepository;
    _liveMarketDataSourceRegistry = liveMarketDataSourceRegistry;
    _securitySource = securitySource;
    _positionSource = positionSource;
    _computationTargetResolver = computationTargetResolver;
    _functionCompilationService = compiledFunctionService;
    _functionResolver = functionResolver;
    _marketDataProviderFactoryResolver = marketDataProviderFactoryResolver;
    _computationCacheSource = computationCacheSource;
    _computationJobDispatcher = jobDispatcher;
    _viewProcessorQueryReceiver = viewProcessorQueryReceiver;
    _dependencyGraphExecutorFactory = dependencyGraphExecutorFactory;
    _graphExecutionStatistics = graphExecutionStatisticsProvider;
    _viewPermissionProvider = viewPermissionProvider;
    _overrideOperationCompiler = overrideOperationCompiler;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }
  
  @Override
  public ViewDefinitionRepository getViewDefinitionRepository() {
    return _viewDefinitionRepository;
  }
  

  @Override
  public LiveMarketDataSourceRegistry getLiveMarketDataSourceRegistry() {
    return _liveMarketDataSourceRegistry;
  }
  //-------------------------------------------------------------------------
  @Override
  public Collection<ViewProcessImpl> getViewProcesses() {
    return Collections.unmodifiableCollection(new ArrayList<ViewProcessImpl>(_allProcessesById.values()));
  }
  
  @Override
  public Collection<ViewClient> getViewClients() {
    return Collections.unmodifiableCollection(new ArrayList<ViewClient>(_allClientsById.values()));
  }
  
  @Override
  public ViewProcessImpl getViewProcess(UniqueId viewProcessId) {
    ArgumentChecker.notNull(viewProcessId, "viewProcessId");
    checkIdScheme(viewProcessId, PROCESS_SCHEME);
    ViewProcessImpl process = _allProcessesById.get(viewProcessId);
    if (process == null) {
      throw new DataNotFoundException("View process not found: " + viewProcessId);
    }
    return process;
  }
  
  /**
   * Obtains a shared view process matching the given arguments, creating the process if necessary, and associates the
   * client with that process.
   * 
   * @param clientId  the unique identifier of the client, not null
   * @param listener  the process listener, not null
   * @param viewDefinitionId  the id of the view definition, not null
   * @param executionOptions  the view execution options, not null
   * @return the permission provider to be used for access control, not null
   */
  public ViewPermissionProvider attachClientToSharedViewProcess(UniqueId clientId, ViewResultListener listener, UniqueId viewDefinitionId, ViewExecutionOptions executionOptions) {
    ArgumentChecker.notNull(clientId, "clientId");
    ArgumentChecker.notNull(viewDefinitionId, "viewDefinitionId");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ViewClientImpl client = getViewClient(clientId);
    
    _processLock.lock();
    ViewProcessImpl process = null;
    try {
      process = getOrCreateViewProcess(viewDefinitionId, executionOptions);
      return attachClientToViewProcessCore(client, listener, process, false);
    } catch (Exception e) {
      // Roll-back
      if (process != null) {
        removeViewProcessIfUnused(process);
      }
      s_logger.error("Error attaching client to shared view process", e);
      throw new OpenGammaRuntimeException("Error attaching client to shared view process", e);
    } finally {
      _processLock.unlock();
    }
  }
  
  /**
   * Obtains a new, private view process, and associates the client with that process.
   *  
   * @param clientId  the unique identifier of the client, not null  
   * @param listener  the process listener, not null
   * @param viewDefinitionId  the id of the view definition, not null
   * @param executionOptions  the view execution options, not null
   * @return the permission provider to be used for access control, not null
   */
  public ViewPermissionProvider attachClientToPrivateViewProcess(UniqueId clientId, ViewResultListener listener, UniqueId viewDefinitionId, ViewExecutionOptions executionOptions) {
    ArgumentChecker.notNull(viewDefinitionId, "definitionID");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ViewClientImpl client = getViewClient(clientId);
    
    ViewProcessImpl process = null;
    _processLock.lock();
    try {
      process = createViewProcess(viewDefinitionId, executionOptions, true);
      return attachClientToViewProcessCore(client, listener, process, true);
    } catch (Exception e) {
      // Roll-back
      if (process != null) {
        removeViewProcess(process);
      }
      s_logger.error("Error attaching client to private view process", e);
      throw new OpenGammaRuntimeException("Error attaching client to private view process", e);
    } finally {
      _processLock.unlock();
    }
  }
  
  /**
   * Obtains an existing view process, and associates the client with that process.
   * 
   * @param clientId  the unique identifier of the client, not null
   * @param listener  the process listener, not null
   * @param processId  the unique identifier of the existing process, not null
   * @return the permission provider to be used for access control, not null
   */
  public ViewPermissionProvider attachClientToViewProcess(UniqueId clientId, ViewResultListener listener, UniqueId processId) {
    ViewClientImpl client = getViewClient(clientId);
    
    _processLock.lock();
    try {
      ViewProcessImpl process = getViewProcess(processId);
      return attachClientToViewProcessCore(client, listener, process, false);
    } catch (Exception e) {
      // Nothing to roll back
      s_logger.error("Error attaching client to existing view process", e);
      throw new OpenGammaRuntimeException("Error attaching client to existing view process", e);
    } finally {
      _processLock.unlock();
    }
  }

  private ViewPermissionProvider attachClientToViewProcessCore(ViewClientImpl client, ViewResultListener listener, ViewProcessImpl process, boolean privateProcess) {
    Pair<ViewProcessImpl, ViewResultListener> processListenerPair = Pair.of(process, listener);
    _processLock.lock();
    try {
      Pair<ViewProcessImpl, ViewResultListener> existingAttachment = _clientToProcess.get(client.getUniqueId());
      if (existingAttachment != null) {
        throw new IllegalStateException("View client " + client.getUniqueId() + " is already attached to view process " + existingAttachment.getFirst().getUniqueId());
      }
      ViewPermissionProvider permissionProvider = process.attachListener(listener);
      _clientToProcess.put(client.getUniqueId(), processListenerPair);
      return permissionProvider;
    } finally {
      _processLock.unlock();
    }
  }
  
  /**
   * Removes the association, if any, between a client and a view process. This may allow the view process to be
   * terminated and removed.
   * 
   * @param clientId  the unique identifier of the client, not null
   */
  public void detachClientFromViewProcess(UniqueId clientId) {
    ArgumentChecker.notNull(clientId, "clientId");
    _processLock.lock();
    try {
      Pair<ViewProcessImpl, ViewResultListener> processAttachment = _clientToProcess.remove(clientId);
      if (processAttachment == null) {
        return;
      }
      ViewProcessImpl process = processAttachment.getFirst();
      ViewResultListener listener = processAttachment.getSecond();
      process.detachListener(listener);
      
      removeViewProcessIfUnused(process);
    } finally {
      _processLock.unlock();
    }
  }
  
  private ViewProcessImpl getOrCreateViewProcess(UniqueId viewDefinitionId, ViewExecutionOptions executionOptions) {
    _processLock.lock();
    try {
      ViewProcessDescription viewDescription = new ViewProcessDescription(viewDefinitionId, executionOptions);
      ViewProcessImpl process = _sharedProcessesByDescription.get(viewDescription);
      if (process == null) {
        process = createViewProcess(viewDefinitionId, executionOptions, false);
        _sharedProcessesByDescription.put(viewDescription, process);
      }
      return process;
    } finally {
      _processLock.unlock();
    }
  }

  private ViewProcessImpl createViewProcess(UniqueId definitionId, ViewExecutionOptions executionOptions, boolean privateProcess) {
    _processLock.lock();
    try {
      String idValue = generateIdValue(_processIdSource);
      UniqueId viewProcessId = UniqueId.of(PROCESS_SCHEME, idValue);
      ObjectId cycleObjectId = ObjectId.of(CYCLE_SCHEME, idValue);
      ViewProcessContext viewProcessContext = createViewProcessContext();
      ViewProcessImpl viewProcess = new ViewProcessImpl(viewProcessId, definitionId, executionOptions, viewProcessContext, getViewCycleManager(), cycleObjectId);
      
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
  
  private void removeViewProcess(ViewProcessImpl viewProcess) {
    s_logger.info("Removing view process {}", viewProcess);
    _processLock.lock();
    try {
      // Ignored if the process has already terminated (e.g. naturally)
      viewProcess.shutdown();
      
      _allProcessesById.remove(viewProcess.getUniqueId());
      ViewProcessDescription description = new ViewProcessDescription(viewProcess.getDefinitionId(), viewProcess.getExecutionOptions());
      ViewProcessImpl sharedProc = _sharedProcessesByDescription.get(description);
      if (sharedProc != null && sharedProc == viewProcess) { //PLAT-1287
        _sharedProcessesByDescription.remove(description);
      }
    } finally {
      _processLock.unlock();
    }
    
    _viewProcessorEventListenerRegistry.notifyViewProcessRemoved(viewProcess.getUniqueId());
  }
  
  private void removeViewProcessIfUnused(ViewProcessImpl process) {
    if (!process.hasExecutionDemand()) {
      // REVIEW jonathan 2011-03-25 -- could have rules for keeping processes around for some time in case new clients
      // come along, to avoid the overhead of reconstructing them. Batch and terminated processes would still want to 
      // be torn down straight away.
      removeViewProcess(process);
    }
  }
  
  //-------------------------------------------------------------------------
  
  /**
   * Gets the live data override injector for the view process currently associated with a client.
   * 
   * @param clientId  the unique identifier of the client, not null
   * @return the live data override injector, not null
   * @throws IllegalStateException if the client is not associated with a view process
   */
  public MarketDataInjector getLiveDataOverrideInjector(UniqueId clientId) {
    return getClientViewProcess(clientId).getLiveDataOverrideInjector();
  }
  
  /**
   * Gets the view definition being operated on by the process associated with a client.
   * 
   * @param clientId  the unique identifier of the client, not null
   * @return the view definition, not null
   * @throws IllegalStateException if the client is not associated with a view process
   */
  public ViewDefinition getLatestViewDefinition(UniqueId clientId) {
    return getClientViewProcess(clientId).getLatestViewDefinition();
  }
  
  /**
   * Requests that a computation cycle be run, even if none of the other triggers have fired since the last cycle.
   * 
   * @param clientId  the unique identifier of the client, not null
   * @throws IllegalStateException if the client is not associated with a view process
   */
  public void triggerCycle(UniqueId clientId) {
    getClientViewProcess(clientId).triggerCycle();
  }
  
  /**
   * Gets the version-correction for which the view process associated with a client is running.
   * 
   * @param clientId  the unique identifier of the client, not null
   * @return the version-correction, not null
   * @throws IllegalStateException if the client is not associated with a view process
   */
  public VersionCorrection getProcessVersionCorrection(UniqueId clientId) {
    return getClientViewProcess(clientId).getExecutionOptions().getVersionCorrection();
  }
  
  private ViewProcessImpl getClientViewProcess(UniqueId clientId) {
    checkIdScheme(clientId, CLIENT_SCHEME);
    _processLock.lock();
    try {
      Pair<ViewProcessImpl, ViewResultListener> clientAttachment = _clientToProcess.get(clientId);
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
  public ViewClient createViewClient(UserPrincipal clientUser) {
    ArgumentChecker.notNull(clientUser, "clientUser");
    String idValue = generateIdValue(_clientIdSource);
    UniqueId clientId = UniqueId.of(CLIENT_SCHEME, idValue);
    ViewClientImpl client = new ViewClientImpl(clientId, this, clientUser, _clientResultTimer);
    _allClientsById.put(clientId, client);
    _viewProcessorEventListenerRegistry.notifyViewClientAdded(clientId);
    return client;
  }

  @Override
  public ViewClientImpl getViewClient(UniqueId clientId) {
    ArgumentChecker.notNull(clientId, "clientId");
    checkIdScheme(clientId, CLIENT_SCHEME);
    ViewClientImpl client = _allClientsById.get(clientId);
    if (client == null) {
      throw new DataNotFoundException("View client not found: " + clientId);
    }
    return client;
  }
  
  /**
   * Must be called by the client when it is terminated to ensure that associated resources are cleaned up.
   * 
   * @param clientId  the unique identifier, not null
   */
  public void removeViewClient(UniqueId clientId) {
    ArgumentChecker.notNull(clientId, "clientId");
    checkIdScheme(clientId, CLIENT_SCHEME);
    s_logger.info("Removing view client {}", clientId);
    ViewClient client = _allClientsById.remove(clientId);
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
   * @param viewClientId  the unique identifier of the view client, not null
   * @return the view process, not null
   */
  public ViewProcessImpl getViewProcessForClient(UniqueId viewClientId) {
    _processLock.lock();
    try {
      ViewProcessImpl process = _clientToProcess.get(viewClientId).getFirst();
      if (process == null) {
        throw new OpenGammaRuntimeException("Client " + viewClientId + " is not attached to a process");
      }
      return process;
    } finally {
      _processLock.unlock();
    }
  }
  
  private void checkIdScheme(UniqueId id, String expectedScheme) {
    if (!expectedScheme.equals(id.getScheme())) {
      throw new IllegalArgumentException("Object is not from this view processor: expected scheme " + PROCESS_SCHEME + " but identifier was " + id);
    }
  }
  
  private ViewProcessContext createViewProcessContext() {
    return new ViewProcessContext(
        _viewDefinitionRepository,
        _viewPermissionProvider,
        _marketDataProviderFactoryResolver,
        _functionCompilationService,
        _functionResolver,
        _positionSource,
        _securitySource,
        _computationTargetResolver,
        _computationCacheSource,
        _computationJobDispatcher,
        _viewProcessorQueryReceiver,
        _dependencyGraphExecutorFactory,
        _graphExecutionStatistics,
        _overrideOperationCompiler);
  }
  
  private String generateIdValue(AtomicLong source) {
    return getUniqueId().getValue() + "-" + source.getAndIncrement();
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
    OperationTimer timer = new OperationTimer(s_logger, "Starting on lifecycle call");
    _lifecycleLock.lock();
    try {
      if (_isStarted) {
        return;
      }
      s_logger.info("Starting on lifecycle call.");
      _isStarted = true;
    } finally {
      _lifecycleLock.unlock();
    }
    timer.finished();
    _viewProcessorEventListenerRegistry.notifyViewProcessorStarted();
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
      final List<Future<?>> suspends = new ArrayList<Future<?>>(_allProcessesById.size());
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
            Future<?> suspend = suspends.remove(suspends.size() - 1);
            try {
              suspend.get(3000, TimeUnit.MILLISECONDS);
            } catch (TimeoutException t) {
              s_logger.debug("Timeout waiting for view to suspend", t);
              suspends.add(suspend);
            } catch (Throwable t) {
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
            for (ViewProcessInternal view : _allProcessesById.values()) {
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
    _processLock.lock();
    _lifecycleLock.lock();
    try {
      if (!_isStarted) {
        return;
      }
      s_logger.info("Stopping on lifecycle call - terminating all children");
      
      for (ViewProcessImpl viewProcess : getViewProcesses()) {
        removeViewProcess(viewProcess);
      }
      s_logger.info("All view processes terminated.");
      
      for (ViewClient viewClient : getViewClients()) {
        viewClient.shutdown();
      }
      _allClientsById.clear();
      
      _isStarted = false;
      
      // REVIEW Andrew 2010-03-25 -- It might be coincidence, but if this gets called during undeploy/stop within a container the Bloomberg API explodes with a ton of NPEs.
      _viewProcessorEventListenerRegistry.notifyViewProcessorStopped();
    } finally {
      _lifecycleLock.unlock();
      _processLock.unlock();
    }
  }
  
  //-------------------------------------------------------------------------
  private final class ViewProcessDescription {
    
    private final UniqueId _viewDefinitionId;
    private final ViewExecutionOptions _executionOptions;
    
    public ViewProcessDescription(UniqueId definitionId, ViewExecutionOptions executionOptions) {
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
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof ViewProcessDescription)) {
        return false;
      }
      ViewProcessDescription other = (ViewProcessDescription) obj;
      if (!_viewDefinitionId.equals(other._viewDefinitionId)) {
        return false;
      }
      if (!_executionOptions.equals(other._executionOptions)) {
        return false;
      }
      return true;
    }
    
  }

}
