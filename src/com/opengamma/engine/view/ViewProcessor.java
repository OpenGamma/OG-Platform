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
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.engine.function.DefaultFunctionResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.position.PositionMaster;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.ArgumentChecker;

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
  private final ViewDefinitionRepository _viewDefinitionRepository;
  private final FunctionRepository _functionRepository;
  private final SecurityMaster _securityMaster;
  private final PositionMaster _positionMaster;
  private final LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private final LiveDataSnapshotProvider _liveDataSnapshotProvider;
  private final ViewComputationCacheSource _computationCacheSource;
  private final FudgeRequestSender _computationJobRequestSender;
  // State:
  private final ConcurrentMap<String, View> _viewsByName = new ConcurrentHashMap<String, View>();
  private final FunctionCompilationContext _compilationContext = new FunctionCompilationContext();
  private final ReentrantLock _lifecycleLock = new ReentrantLock();
  private boolean _isStarted = false;
  
  public ViewProcessor(
      ViewDefinitionRepository viewDefinitionRepository,
      FunctionRepository functionRepository,
      SecurityMaster securityMaster,
      PositionMaster positionMaster,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider,
      LiveDataSnapshotProvider liveDataSnapshotProvider,
      ViewComputationCacheSource computationCacheSource,
      FudgeRequestSender computationJobRequestSender) {
    ArgumentChecker.checkNotNull(viewDefinitionRepository, "View definition repository");
    ArgumentChecker.checkNotNull(functionRepository, "Function repository");
    ArgumentChecker.checkNotNull(securityMaster, "Security master");
    ArgumentChecker.checkNotNull(positionMaster, "Position master");
    // TODO kirk 2010-03-02 -- Finish checking inputs.
    _viewDefinitionRepository = viewDefinitionRepository;
    _functionRepository = functionRepository;
    _securityMaster = securityMaster;
    _positionMaster = positionMaster;
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
    _liveDataSnapshotProvider = liveDataSnapshotProvider;
    _computationCacheSource = computationCacheSource;
    _computationJobRequestSender = computationJobRequestSender;
  }
  
  /**
   * @return the viewDefinitionRepository
   */
  public ViewDefinitionRepository getViewDefinitionRepository() {
    return _viewDefinitionRepository;
  }

  /**
   * @return the functionRepository
   */
  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  /**
   * @return the securityMaster
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  /**
   * @return the positionMaster
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  /**
   * @return the liveDataAvailabilityProvider
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * @return the liveDataSnapshotProvider
   */
  public LiveDataSnapshotProvider getLiveDataSnapshotProvider() {
    return _liveDataSnapshotProvider;
  }

  /**
   * @return the computationCacheSource
   */
  public ViewComputationCacheSource getComputationCacheSource() {
    return _computationCacheSource;
  }

  /**
   * @return the computationJobRequestSender
   */
  public FudgeRequestSender getComputationJobRequestSender() {
    return _computationJobRequestSender;
  }

  public Set<String> getViewNames() {
    return getViewDefinitionRepository().getDefinitionNames();
  }
  
  /**
   * @return the compilationContext
   */
  public FunctionCompilationContext getCompilationContext() {
    return _compilationContext;
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
      // REVIEW kirk 2010-03-03 -- If we initialize all views or anything, this is
      // where we'd do it.
      getFunctionRepository().initFunctions(getCompilationContext());
      
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
      _isStarted = false;
    } finally {
      _lifecycleLock.unlock();
    }
  }

}
