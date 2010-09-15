/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.FunctionCompilationService;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.permission.ViewPermission;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.livedata.client.LiveDataClient;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;
import com.opengamma.util.monitor.OperationTimer;

/**
 * Default implementation of {@link ViewProcessor}.
 */
public class ViewProcessorImpl implements ViewProcessorInternal, Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessor.class);
  // Injected Inputs:
  private ViewDefinitionRepository _viewDefinitionRepository;
  private SecuritySource _securitySource;
  private PositionSource _positionSource;
  private FunctionCompilationService _functionCompilationService;
  private LiveDataClient _liveDataClient;
  private LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private LiveDataSnapshotProvider _liveDataSnapshotProvider;
  private ViewComputationCacheSource _computationCacheSource;
  private JobDispatcher _computationJobDispatcher;
  private ViewProcessorQueryReceiver _viewProcessorQueryReceiver;
  private DependencyGraphExecutorFactory<?> _dependencyGraphExecutorFactory;
  private ViewPermissionProvider _viewPermissionProvider;
  private Map<String, Object> _configurationResource;
  // State:
  private final ConcurrentMap<String, ViewImpl> _viewsByName = new ConcurrentHashMap<String, ViewImpl>();
  private final ReentrantLock _lifecycleLock = new ReentrantLock();
  private final Timer _clientResultTimer = new Timer("ViewProcessor client result timer");
  private boolean _isStarted /* = false */;
  private ExecutorService _executorService;
  private boolean _localExecutorService /* = false */;

  public ViewProcessorImpl() {
  }

  protected void assertNotStarted() {
    if (_isStarted) {
      throw new IllegalStateException("Cannot change injected properties once this ViewProcessor has been started.");
    }
  }

  /**
   * @return the viewDefinitionRepository
   */
  public ViewDefinitionRepository getViewDefinitionRepository() {
    return _viewDefinitionRepository;
  }

  /**
   * @param viewDefinitionRepository the viewDefinitionRepository to set
   */
  public void setViewDefinitionRepository(ViewDefinitionRepository viewDefinitionRepository) {
    assertNotStarted();
    _viewDefinitionRepository = viewDefinitionRepository;
  }

  @Override
  public FunctionCompilationService getFunctionCompilationService() {
    return _functionCompilationService;
  }

  /**
   * Sets the function compilation service
   * 
   * @param functionCompilationService  the function compilation service
   */
  public void setFunctionCompilationService(final FunctionCompilationService functionCompilationService) {
    assertNotStarted();
    _functionCompilationService = functionCompilationService;
  }

  @Override
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Sets the source of securities.
   * @param securitySource  the source of securities
   */
  public void setSecuritySource(SecuritySource securitySource) {
    assertNotStarted();
    _securitySource = securitySource;
  }

  @Override
  public PositionSource getPositionSource() {
    return _positionSource;
  }

  /**
   * Sets the source of positions.
   * @param positionSource  the source of positions
   */
  public void setPositionSource(PositionSource positionSource) {
    assertNotStarted();
    _positionSource = positionSource;
  }

  @Override
  public LiveDataClient getLiveDataClient() {
    return _liveDataClient;
  }

  public void setLiveDataClient(LiveDataClient liveDataClient) {
    _liveDataClient = liveDataClient;
  }

  /**
   * @return the liveDataAvailabilityProvider
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * @param liveDataAvailabilityProvider the liveDataAvailabilityProvider to set
   */
  public void setLiveDataAvailabilityProvider(LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    assertNotStarted();
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
  }

  @Override
  public LiveDataSnapshotProvider getLiveDataSnapshotProvider() {
    return _liveDataSnapshotProvider;
  }

  /**
   * @param liveDataSnapshotProvider the liveDataSnapshotProvider to set
   */
  public void setLiveDataSnapshotProvider(LiveDataSnapshotProvider liveDataSnapshotProvider) {
    assertNotStarted();
    _liveDataSnapshotProvider = liveDataSnapshotProvider;
  }

  @Override
  public ViewComputationCacheSource getComputationCacheSource() {
    return _computationCacheSource;
  }

  /**
   * @param computationCacheSource the computationCacheSource to set
   */
  public void setComputationCacheSource(ViewComputationCacheSource computationCacheSource) {
    assertNotStarted();
    _computationCacheSource = computationCacheSource;
  }

  @Override
  public JobDispatcher getComputationJobDispatcher() {
    return _computationJobDispatcher;
  }

  /**
   * @param computationJobDispatcher the computationJobDispatcher to set
   */
  public void setComputationJobDispatcher(JobDispatcher computationJobDispatcher) {
    assertNotStarted();
    _computationJobDispatcher = computationJobDispatcher;
  }

  @Override
  public ViewProcessorQueryReceiver getViewProcessorQueryReceiver() {
    return _viewProcessorQueryReceiver;
  }

  public void setViewProcessorQueryReceiver(ViewProcessorQueryReceiver calcNodeQueryReceiver) {
    assertNotStarted();
    _viewProcessorQueryReceiver = calcNodeQueryReceiver;
  }

  @Override
  public DependencyGraphExecutorFactory<?> getDependencyGraphExecutorFactory() {
    return _dependencyGraphExecutorFactory;
  }

  public void setDependencyGraphExecutorFactory(DependencyGraphExecutorFactory<?> dependencyGraphExecutorFactory) {
    _dependencyGraphExecutorFactory = dependencyGraphExecutorFactory;
  }

  @Override
  public ViewPermissionProvider getViewPermissionProvider() {
    return _viewPermissionProvider;
  }

  public void setViewPermissionProvider(ViewPermissionProvider viewPermissionProvider) {
    _viewPermissionProvider = viewPermissionProvider;
  }

  public void setConfigurationResource(final Map<String, Object> configurationResource) {
    _configurationResource = configurationResource;
  }

  // TODO DVI-101 -- this doesn't belong here, so has been excluded from the ViewProcessor interface
  public Map<String, Object> getConfigurationResource() {
    return _configurationResource;
  }

  @Override
  public ExecutorService getExecutorService() {
    return _executorService;
  }

  /**
   * @param executorService the executorService to set
   */
  public void setExecutorService(ExecutorService executorService) {
    assertNotStarted();
    _executorService = executorService;
  }

  /**
   * @return the localExecutorService
   */
  public boolean isLocalExecutorService() {
    return _localExecutorService;
  }

  /**
   * @param localExecutorService the localExecutorService to set
   */
  public void setLocalExecutorService(boolean localExecutorService) {
    assertNotStarted();
    _localExecutorService = localExecutorService;
  }

  @Override
  public Set<String> getViewNames() {
    return getViewDefinitionRepository().getDefinitionNames();
  }

  @Override
  public View getView(String name, UserPrincipal credentials) {
    ArgumentChecker.notNull(name, "View name");
    ArgumentChecker.notNull(credentials, "User credentials");

    ViewImpl view = _viewsByName.get(name);
    if (view == null) {
      ViewDefinition definition = getViewDefinitionRepository().getDefinition(name);
      if (definition == null) {
        throw new OpenGammaRuntimeException("No view definition with the name '" + name + "' could be found.");
      }
      
      ViewProcessingContext viewProcessingContext = createViewProcessingContext();
      view = new ViewImpl(definition, viewProcessingContext, _clientResultTimer);
      _viewsByName.put(name, view);
    }
    getViewPermissionProvider().assertPermission(ViewPermission.ACCESS, credentials, view);
    return view;
  }

  // --------------------------------------------------------------------------
  // LIFECYCLE METHODS
  // --------------------------------------------------------------------------

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
      s_logger.info("Starting on lifecycle call.");
      checkInjectedInputs();
      initializeExecutorService();
      getFunctionCompilationService().initialize(getExecutorService());
      // REVIEW kirk 2010-03-03 -- If we initialize all views or anything, this is
      // where we'd do it.

      _isStarted = true;
    } finally {
      _lifecycleLock.unlock();
    }
    timer.finished();
  }

  @Override
  public void stop() {
    _lifecycleLock.lock();
    try {
      s_logger.info("Stopping on lifecycle call, terminating all running views.");
      Collection<ViewImpl> views = _viewsByName.values();
      for (ViewImpl view : views) {
        if (view.isRunning()) {
          s_logger.info("Terminating view {} due to lifecycle call", view.getDefinition().getName());
          view.stop();
        }
      }
      _viewsByName.clear();
      s_logger.info("All views terminated.");
      if (isLocalExecutorService()) {
        s_logger.info("Shutting down local executor service.");
        getExecutorService().shutdown();
        try {
          getExecutorService().awaitTermination(90, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          s_logger.info("Interrupted while attempting to shutdown local executor service");
          Thread.interrupted();
        }
        s_logger.info("Executor service shut down.");
      }
      _isStarted = false;
      // REVIEW Andrew 2010-03-25 -- It might be coincidence, but if this gets called during undeploy/stop within a container the Bloomberg API explodes with a ton of NPEs.
    } finally {
      _lifecycleLock.unlock();
    }
  }

  // --------------------------------------------------------------------------
  // INITIALIZATION METHODS
  // For all methods that are ultimately called from start()
  // --------------------------------------------------------------------------

  protected void initializeExecutorService() {
    if (getExecutorService() == null) {
      OperationTimer timer = new OperationTimer(s_logger, "Initializing View Processor");
      ThreadFactory tf = new NamedThreadPoolFactory("ViewProcessor", true);
      int nThreads = Runtime.getRuntime().availableProcessors() - 1;
      if (nThreads == 0) {
        nThreads = 1;
      }
      s_logger.info("No injected executor service; starting one with {} max threads", nThreads);
      // REVIEW kirk 2010-03-07 -- Is this the right queue to use here?
      ThreadPoolExecutor executor = new ThreadPoolExecutor(0, nThreads, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), tf);
      setExecutorService(executor);
      setLocalExecutorService(true);
      timer.finished();
    } else {
      setLocalExecutorService(false);
    }
  }

  protected void checkInjectedInputs() {
    s_logger.debug("Checking injected inputs.");
    ArgumentChecker.notNullInjected(getViewDefinitionRepository(), "viewDefinitionRepository");
    ArgumentChecker.notNullInjected(getFunctionCompilationService(), "functionCompilationService");
    ArgumentChecker.notNullInjected(getSecuritySource(), "securitySource");
    ArgumentChecker.notNullInjected(getPositionSource(), "positionSource");
    ArgumentChecker.notNullInjected(getLiveDataAvailabilityProvider(), "liveDataAvailabilityProvider");
    ArgumentChecker.notNullInjected(getLiveDataSnapshotProvider(), "liveDataSnapshotProvider");
    ArgumentChecker.notNullInjected(getComputationCacheSource(), "computationCacheSource");
    ArgumentChecker.notNullInjected(getComputationJobDispatcher(), "computationJobRequestSender");
  }
  
  private ViewProcessingContext createViewProcessingContext() {
    FunctionRepository functionRepository = getFunctionCompilationService().getFunctionRepository();
    FunctionResolver functionResolver = new DefaultFunctionResolver(functionRepository);
    
    return new ViewProcessingContext(
        getLiveDataClient(),
        getLiveDataAvailabilityProvider(),
        getLiveDataSnapshotProvider(),
        functionRepository,
        functionResolver,
        getPositionSource(),
        getSecuritySource(),
        getComputationCacheSource(),
        getComputationJobDispatcher(),
        getViewProcessorQueryReceiver(),
        getFunctionCompilationService().getFunctionCompilationContext(),
        getExecutorService(),
        getDependencyGraphExecutorFactory(),
        getViewPermissionProvider());
  }

}
