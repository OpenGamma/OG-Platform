/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
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
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;

// REVIEW kirk 2010-03-02 -- View initialization is really slow, and right now there's no
// asynchronous support in any type of RESTful call for a super-slow (1-2 minutes) call.
// Should we pre-load all views? Have an option for pre-loaded ones? What?

// TODO kirk 2010-03-04 -- Needs a way to Spring-based inject things into the
// compilation context.
/**
 * 
 *
 * @author kirk
 */
public class ViewProcessor implements Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessor.class);
  // Injected Inputs:
  private ViewDefinitionRepository _viewDefinitionRepository;
  private FunctionRepository _functionRepository;
  private SecurityMaster _securityMaster;
  private PositionMaster _positionMaster;
  private LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private LiveDataSnapshotProvider _liveDataSnapshotProvider;
  private ViewComputationCacheSource _computationCacheSource;
  private FudgeRequestSender _computationJobRequestSender;
  // State:
  private final ConcurrentMap<String, View> _viewsByName = new ConcurrentHashMap<String, View>();
  private final ReentrantLock _lifecycleLock = new ReentrantLock();
  private boolean _isStarted = false;
  private final FunctionCompilationContext _compilationContext = new FunctionCompilationContext();
  private ExecutorService _executorService;
  private boolean _localExecutorService = false;
  
  public ViewProcessor() {
  }
  
  protected void assertNotStarted() {
    if(_isStarted) {
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
   * @return the securityMaster
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * @param securityMaster the securityMaster to set
   */
  public void setSecurityMaster(SecurityMaster securityMaster) {
    assertNotStarted();
    _securityMaster = securityMaster;
  }

  /**
   * @return the positionMaster
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * @param positionMaster the positionMaster to set
   */
  public void setPositionMaster(PositionMaster positionMaster) {
    assertNotStarted();
    _positionMaster = positionMaster;
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
   * @return the computationJobRequestSender
   */
  public FudgeRequestSender getComputationJobRequestSender() {
    return _computationJobRequestSender;
  }

  /**
   * @param computationJobRequestSender the computationJobRequestSender to set
   */
  public void setComputationJobRequestSender(
      FudgeRequestSender computationJobRequestSender) {
    assertNotStarted();
    _computationJobRequestSender = computationJobRequestSender;
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
   * This method will only return a view if it has already been initialized.
   * If there is a view definition available, but this method returns
   * {@code null}, the view needs to be initialized using {@link #initializeView(String)}.
   * 
   * @param name The name of the view to obtain.
   * @return     The initialized view, or {@code null}.
   */
  public View getView(String name) {
    View view = _viewsByName.get(name);
    return view;
  }
  
  public View initializeView(String viewName) {
    ArgumentChecker.checkNotNull(viewName, "View name");
    if(_viewsByName.containsKey(viewName)) {
      return _viewsByName.get(viewName);
    }
    ViewDefinition viewDefinition = getViewDefinitionRepository().getDefinition(viewName);
    if(viewDefinition == null) {
      throw new IllegalArgumentException("No view available with name \"" + viewName + "\"");
    }
    // NOTE kirk 2010-03-02 -- We construct a bespoke ViewProcessingContext because the resolvers
    // might be based on the view definition (particularly for functions and the like).
    ViewProcessingContext vpc = new ViewProcessingContext(
        getLiveDataAvailabilityProvider(),
        getLiveDataSnapshotProvider(),
        getFunctionRepository(),
        new DefaultFunctionResolver(getFunctionRepository()),
        getPositionMaster(),
        getSecurityMaster(),
        getComputationCacheSource(),
        getComputationJobRequestSender(),
        getCompilationContext()
        );
    View freshView = new View(viewDefinition, vpc);
    View actualView = _viewsByName.putIfAbsent(viewName, freshView);
    if(actualView == null) {
      actualView = freshView;
    }
    switch(actualView.getCalculationState()) {
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
    return actualView;
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
    ArgumentChecker.checkNotNull(viewName, "View name");
    if(_viewsByName.containsKey(viewName)) {
      return _viewsByName.get(viewName);
    }
    ViewDefinition viewDefinition = getViewDefinitionRepository().getDefinition(viewName);
    if(viewDefinition == null) {
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
    _lifecycleLock.lock();
    try {
      checkInjectedInputs();
      initializeExecutorService();
      initializeAllFunctionDefinitions();
      // REVIEW kirk 2010-03-03 -- If we initialize all views or anything, this is
      // where we'd do it.
      
      _isStarted = true;
    } finally {
      _lifecycleLock.unlock();
    }
  }

  @Override
  public void stop() {
    _lifecycleLock.lock();
    try {
      s_logger.info("Stopping on lifecycle call, terminating all running views.");
      Collection<View> views = _viewsByName.values();
      for(View view : views) {
        if(view.isRunning()) {
          s_logger.info("Terminating view {} due to lifecycle call", view.getDefinition().getName());
          view.stop();
        }
      }
      _viewsByName.clear();
      if(isLocalExecutorService()) {
        getExecutorService().shutdown();
        try {
          getExecutorService().awaitTermination(90, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          s_logger.info("Interrupted while attempting to shutdown local executor service");
          Thread.interrupted();
        }
      }
      _isStarted = false;
    } finally {
      _lifecycleLock.unlock();
    }
  }
  
  // --------------------------------------------------------------------------
  // INITIALIZATION METHODS
  // For all methods that are ultimately called from start()
  // --------------------------------------------------------------------------

  protected void initializeAllFunctionDefinitions() {
    s_logger.info("Initializing all function definitions.");
    // TODO kirk 2010-03-07 -- Better error handling.
    ExecutorCompletionService<FunctionDefinition> completionService = new ExecutorCompletionService<FunctionDefinition>(getExecutorService());
    int nFunctions = getFunctionRepository().getAllFunctions().size();
    for(FunctionDefinition definition : getFunctionRepository().getAllFunctions()) {
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
    for(int i = 0; i < nFunctions; i++) {
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
  }
  
  protected void initializeExecutorService() {
    if(getExecutorService() == null) {
      s_logger.info("No injected executor service; starting one.");
      ThreadFactory tf = new NamedThreadPoolFactory("ViewProcessor", true);
      int nThreads = Runtime.getRuntime().availableProcessors() - 1;
      if(nThreads == 0) {
        nThreads = 1;
      }
      // REVIEW kirk 2010-03-07 -- Is this the right queue to use here?
      ThreadPoolExecutor executor = new ThreadPoolExecutor(0, nThreads, 5l, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), tf);
      setExecutorService(executor);
      setLocalExecutorService(true);
    } else {
      setLocalExecutorService(false);
    }
  }
  
  protected void checkInjectedInputs() {
    s_logger.debug("Checking injected inputs.");
    ArgumentChecker.checkNotNullInjected(getViewDefinitionRepository(), "viewDefinitionRepository");
    ArgumentChecker.checkNotNullInjected(getFunctionRepository(), "functionRepository");
    ArgumentChecker.checkNotNullInjected(getSecurityMaster(), "securityMaster");
    ArgumentChecker.checkNotNullInjected(getPositionMaster(), "positionMaster");
    ArgumentChecker.checkNotNullInjected(getLiveDataAvailabilityProvider(), "liveDataAvailabilityProvider");
    ArgumentChecker.checkNotNullInjected(getLiveDataSnapshotProvider(), "liveDataSnapshotProvider");
    ArgumentChecker.checkNotNullInjected(getComputationCacheSource(), "computationCacheSource");
    ArgumentChecker.checkNotNullInjected(getComputationJobRequestSender(), "computationJobRequestSender");
  }
  
}
