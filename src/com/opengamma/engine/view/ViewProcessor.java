/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.DefaultFunctionResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.ResultWriterFactory;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.livedata.client.LiveDataClient;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;
import com.opengamma.util.monitor.OperationTimer;

// REVIEW kirk 2010-03-02 -- View initialization is really slow, and right now there's no
// asynchronous support in any type of RESTful call for a super-slow (1-2 minutes) call.
// Should we pre-load all views? Have an option for pre-loaded ones? What?

/**
 * 
 */
public class ViewProcessor implements Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessor.class);
  // Injected Inputs:
  private ViewDefinitionRepository _viewDefinitionRepository;
  private FunctionRepository _functionRepository;
  private SecuritySource _securitySource;
  private PositionSource _positionSource;
  private LiveDataClient _liveDataClient;
  private LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private LiveDataSnapshotProvider _liveDataSnapshotProvider;
  private ViewComputationCacheSource _computationCacheSource;
  private JobDispatcher _computationJobDispatcher;
  private ViewProcessorQueryReceiver _viewProcessorQueryReceiver;
  private DependencyGraphExecutorFactory _dependencyGraphExecutorFactory;
  private ResultWriterFactory _resultWriterFactory;
  // State:
  private final ConcurrentMap<String, View> _viewsByName = new ConcurrentHashMap<String, View>();
  private final ReentrantLock _lifecycleLock = new ReentrantLock();
  private boolean _isStarted /*= false*/;
  private final FunctionCompilationContext _compilationContext = new FunctionCompilationContext();
  private ExecutorService _executorService;
  private boolean _localExecutorService /*= false*/;
  
  public ViewProcessor() {
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
  public void setViewDefinitionRepository(
      ViewDefinitionRepository viewDefinitionRepository) {
    assertNotStarted();
    _viewDefinitionRepository = viewDefinitionRepository;
  }

  /**
   * @return the functionRepository
   */
  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  /**
   * @param functionRepository the functionRepository to set
   */
  public void setFunctionRepository(FunctionRepository functionRepository) {
    assertNotStarted();
    _functionRepository = functionRepository;
  }

  /**
   * Gets the source of securities.
   * @return the source of securities
   */
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

  /**
   * Gets the source of positions.
   * @return the source of positions
   */
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
  public void setLiveDataAvailabilityProvider(
      LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    assertNotStarted();
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
  }

  /**
   * @return the liveDataSnapshotProvider
   */
  public LiveDataSnapshotProvider getLiveDataSnapshotProvider() {
    return _liveDataSnapshotProvider;
  }

  /**
   * @param liveDataSnapshotProvider the liveDataSnapshotProvider to set
   */
  public void setLiveDataSnapshotProvider(
      LiveDataSnapshotProvider liveDataSnapshotProvider) {
    assertNotStarted();
    _liveDataSnapshotProvider = liveDataSnapshotProvider;
  }
  
  /**
   * @return the computationCacheSource
   */
  public ViewComputationCacheSource getComputationCacheSource() {
    return _computationCacheSource;
  }

  /**
   * @param computationCacheSource the computationCacheSource to set
   */
  public void setComputationCacheSource(
      ViewComputationCacheSource computationCacheSource) {
    assertNotStarted();
    _computationCacheSource = computationCacheSource;
  }

  /**
   * @return the computationJobDispatcher
   */
  public JobDispatcher getComputationJobDispatcher() {
    return _computationJobDispatcher;
  }

  /**
   * @param computationJobDispatcher the computationJobDispatcher to set
   */
  public void setComputationJobDispatcher(
      JobDispatcher computationJobDispatcher) {
    assertNotStarted();
    _computationJobDispatcher = computationJobDispatcher;
  }
  
  /**
   * @return the calculation node query receiver (for messages back from the calc node).
   */
  public ViewProcessorQueryReceiver getViewProcessorQueryReceiver() {
    return _viewProcessorQueryReceiver;
  }
  
  public void setViewProcessorQueryReceiver(ViewProcessorQueryReceiver calcNodeQueryReceiver) {
    assertNotStarted();
    _viewProcessorQueryReceiver = calcNodeQueryReceiver;
  }
  
  public DependencyGraphExecutorFactory getDependencyGraphExecutorFactory() {
    return _dependencyGraphExecutorFactory;
  }

  public void setDependencyGraphExecutorFactory(DependencyGraphExecutorFactory dependencyGraphExecutorFactory) {
    _dependencyGraphExecutorFactory = dependencyGraphExecutorFactory;
  }
  
  public ResultWriterFactory getResultWriterFactory() {
    return _resultWriterFactory;
  }

  public void setResultWriterFactory(ResultWriterFactory resultWriterFactory) {
    _resultWriterFactory = resultWriterFactory;
  }

  /**
   * @return the executorService
   */
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
  
  public void setViewProcessorConfigBean(final ViewProcessorConfigBean configBean) {
    ArgumentChecker.notNull(configBean, "configBean");
    configBean.visitCompilationContext(getCompilationContext());
  }

  /**
   * @return the compilationContext
   */
  public FunctionCompilationContext getCompilationContext() {
    return _compilationContext;
  }

  public Set<String> getViewNames() {
    return getViewDefinitionRepository().getDefinitionNames();
  }
  
  /**
   * Obtain an already-initialized {@link View} instance.
   * <p/>
   * This method will only return a view if it has already been initialized
   * and if the given user has access to the view.
   * <p/>
   * If there is a view definition available, and the user has access to it,
   * but this method returns {@code null}, the view needs to be 
   * initialized using {@link #initializeView(String)}.
   * 
   * @param name The name of the view to obtain.
   * @param credentials The user who should have access to the view. 
   * Not null.
   * @return     The initialized view, or {@code null}.
   * @throws ViewAccessException If the view exists and is initialized,
   * but the user has no access to it. 
   */
  public View getView(String name, UserPrincipal credentials) {
    ArgumentChecker.notNull(name, "View name");
    ArgumentChecker.notNull(credentials, "User credentials");
    
    View view = _viewsByName.get(name);
    if (view == null) {
      return null;
    }
    view.checkIsEntitledToAccess(credentials);
    return view;
  }
  
  /**
   *
   * @param viewName the name of the view to initialize
   * @throws NoSuchElementException if a view with the name is not available
   */
  public void initializeView(String viewName) {
    ArgumentChecker.notNull(viewName, "View name");
    if (_viewsByName.containsKey(viewName)) {
      return;
    }
    ViewDefinition viewDefinition = getViewDefinitionRepository().getDefinition(viewName);
    if (viewDefinition == null) {
      throw new NoSuchElementException("No view available with name \"" + viewName + "\"");
    }
    // NOTE kirk 2010-03-02 -- We construct a bespoke ViewProcessingContext because the resolvers
    // might be based on the view definition (particularly for functions and the like).
    getCompilationContext().setSecuritySource(getSecuritySource());
    ViewProcessingContext vpc = new ViewProcessingContext(
        getLiveDataClient(),
        getLiveDataAvailabilityProvider(),
        getLiveDataSnapshotProvider(),
        getFunctionRepository(),
        new DefaultFunctionResolver(getFunctionRepository()),
        getPositionSource(),
        getSecuritySource(),
        getComputationCacheSource(),
        getComputationJobDispatcher(),
        getViewProcessorQueryReceiver(),
        getCompilationContext(),
        getExecutorService(),
        getDependencyGraphExecutorFactory());
    View freshView = new View(viewDefinition, vpc);
    View actualView = _viewsByName.putIfAbsent(viewName, freshView);
    if (actualView == null) {
      actualView = freshView;
    }
    switch (actualView.getCalculationState()) {
      case INITIALIZING:
        // Do nothing, another thread is taking care of this.
        s_logger.debug("Not initializing {} as another thread already doing it.", viewName);
        break;
      case NOT_INITIALIZED:
        // We want to initialize it.
        actualView.init();
        break;
      default:
        // REVIEW kirk 2010-03-02 -- Is this the right thing to do?
        s_logger.warn("Asked to initialize view {} but in state {}. Doing nothing.", viewName, actualView.getCalculationState());
    }
  }
  
  public void startProcessing(String viewName) {
    View view = getViewInternal(viewName);
    switch(view.getCalculationState()) {
      case NOT_INITIALIZED:
        throw new IllegalStateException("View constructed but not yet initialized.");
      case INITIALIZING:
        throw new IllegalStateException("View still initializing.");
      case TERMINATED:
      case TERMINATING:
        throw new IllegalStateException("Restarts of Views not supported right now.");
      case NOT_STARTED:
        s_logger.info("Starting view {}", viewName);
        view.start();
        break;
      case RUNNING:
      case STARTING:
        s_logger.info("Requested start of {} but either running or starting. No action taken.", viewName);
    }
  }
  
  public void stopProcessing(String viewName) {
    View view = getViewInternal(viewName);
    switch(view.getCalculationState()) {
      case NOT_INITIALIZED:
      case INITIALIZING:
      case NOT_STARTED:
        s_logger.info("View {} not started so not stopping.", viewName);
        break;
      case TERMINATED:
      case TERMINATING:
        s_logger.info("View {} requested termination, but already terminated or terminating", viewName);
        break;
      case STARTING:
        throw new IllegalStateException("Attempted to terminate view \"" + viewName + "\" while starting.");
      case RUNNING:
        s_logger.info("Terminating view {}", viewName);
        view.stop();
        _viewsByName.remove(viewName);
    }
  }
  
  protected View getViewInternal(String viewName) {
    ArgumentChecker.notNull(viewName, "View name");
    View view = _viewsByName.get(viewName);
    if (view != null) {
      return view; 
    }
    ViewDefinition viewDefinition = getViewDefinitionRepository().getDefinition(viewName);
    if (viewDefinition == null) {
      throw new IllegalArgumentException("No view available with name \"" + viewName + "\"");
    }
    throw new IllegalStateException("View \"" + viewName + "\" available, but not initialized. Must be initialized first.");
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
      initializeAllFunctionDefinitions();
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
      Collection<View> views = _viewsByName.values();
      for (View view : views) {
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

  protected void initializeAllFunctionDefinitions() {
    OperationTimer timer = new OperationTimer(s_logger, "Initializing function definitions");
    s_logger.info("Initializing all function definitions.");
    // TODO kirk 2010-03-07 -- Better error handling.
    ExecutorCompletionService<FunctionDefinition> completionService = new ExecutorCompletionService<FunctionDefinition>(getExecutorService());
    int nFunctions = getFunctionRepository().getAllFunctions().size();
    for (FunctionDefinition definition : getFunctionRepository().getAllFunctions()) {
      final FunctionDefinition finalDefinition = definition;
      completionService.submit(new Runnable() {
        @Override
        public void run() {
          try {
            finalDefinition.init(getCompilationContext());
          } catch (RuntimeException e) {
            s_logger.warn("Exception thrown while initializing FunctionDefinition {}-{}", new Object[]{finalDefinition, finalDefinition.getShortName()}, e);
            throw e;
          }
        }
      }, definition);
    }
    for (int i = 0; i < nFunctions; i++) {
      Future<FunctionDefinition> future = null;
      try {
        future = completionService.take();
      } catch (InterruptedException e1) {
        Thread.interrupted();
        s_logger.warn("Interrupted while initializing function definitions.");
        throw new OpenGammaRuntimeException("Interrupted while initializing function definitions. ViewProcessor not safe to use.");
      }
      try {
        future.get();
      } catch (Exception e) {
        s_logger.warn("Got exception check back on future for initializing FunctionDefinition. See above log entries", e);
        // REVIEW kirk 2010-03-07 -- What do we do here?
      }
    }
    timer.finished();
  }
  
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
    ArgumentChecker.notNullInjected(getFunctionRepository(), "functionRepository");
    ArgumentChecker.notNullInjected(getSecuritySource(), "securitySource");
    ArgumentChecker.notNullInjected(getPositionSource(), "positionSource");
    ArgumentChecker.notNullInjected(getLiveDataAvailabilityProvider(), "liveDataAvailabilityProvider");
    ArgumentChecker.notNullInjected(getLiveDataSnapshotProvider(), "liveDataSnapshotProvider");
    ArgumentChecker.notNullInjected(getComputationCacheSource(), "computationCacheSource");
    ArgumentChecker.notNullInjected(getComputationJobDispatcher(), "computationJobRequestSender");
  }

}
