/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.view.calc.ViewComputationJob;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.calc.ViewCycleManagerImpl;
import com.opengamma.engine.view.client.ViewDeltaResultCalculator;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionImpl;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@link ViewProcess}.
 */
public class ViewProcessImpl implements ViewProcessInternal, Lifecycle {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcess.class);
  
  private final UniqueIdentifier _viewProcessId;
  private final ViewDefinition _definition;
  private final ViewExecutionOptions _executionOptions;
  private final ViewProcessContext _viewProcessContext;
  private final ObjectIdentifier _cycleObjectId;
  private final ViewCycleManagerImpl _viewCycleManager;
  private final boolean _isBatchProcess;
  
  private final AtomicLong _cycleVersion = new AtomicLong();

  /**
   * Manages access to critical regions of the process. Note that the use of {@link Semaphore} rather than, for example,
   * {@link ReentrantLock} allows one thread to acquire the lock and another thread to release it as part of the same
   * sequence of operations. This could be important for {@link #suspend()} and {@link #resume()}. 
   */
  private final Semaphore _processLock = new Semaphore(1);

  private final Set<ViewProcessListener> _allListeners = new HashSet<ViewProcessListener>();
  private final Set<ViewProcessListener> _listenerExecutionDemand = new HashSet<ViewProcessListener>();

  private volatile ViewProcessState _state = ViewProcessState.STOPPED;
  
  private volatile ViewComputationJob _computationJob;
  private volatile Thread _computationThread;

  private final AtomicReference<CompiledViewDefinitionImpl> _latestCompiledViewDefinition = new AtomicReference<CompiledViewDefinitionImpl>();
  private final AtomicReference<ViewComputationResultModel> _latestResult = new AtomicReference<ViewComputationResultModel>();

  /**
   * Constructs an instance.
   * 
   * @param viewProcessId  the unique identifier of the view process, not null
   * @param definition  the view definition, not null
   * @param executionOptions  the view execution options, not null
   * @param viewProcessContext  the process context, not null
   * @param viewCycleManager  the view cycle manager, not null
   * @param cycleObjectId  the object identifier of cycles, not null
   * @param isBatchProcess {@code true} if the process is for a batch computation, {@code false} otherwise 
   */
  public ViewProcessImpl(UniqueIdentifier viewProcessId, ViewDefinition definition,
      ViewExecutionOptions executionOptions, ViewProcessContext viewProcessContext, ViewCycleManagerImpl viewCycleManager,
      ObjectIdentifier cycleObjectId, boolean isBatchProcess) {
    ArgumentChecker.notNull(viewProcessId, "viewProcessId");
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ArgumentChecker.notNull(viewProcessContext, "viewProcessContext");
    ArgumentChecker.notNull(viewCycleManager, "viewCycleManager");

    _viewProcessId = viewProcessId;
    _definition = definition;
    _executionOptions = executionOptions;
    _viewProcessContext = viewProcessContext;
    _viewCycleManager = viewCycleManager;
    _cycleObjectId = cycleObjectId;
    _isBatchProcess = isBatchProcess;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public UniqueIdentifier getUniqueId() {
    return _viewProcessId;
  }
  
  @Override
  public boolean isBatchProcess() {
    return _isBatchProcess;
  }
  
  @Override
  public String getDefinitionName() {
    return getDefinition().getName();
  }

  @Override
  public ViewDefinition getDefinition() {
    return _definition;
  }
  
  @Override
  public LiveDataInjector getLiveDataOverrideInjector() {
    return getProcessContext().getLiveDataOverrideInjector();
  }

  @Override
  public ViewProcessState getState() {
    return _state;
  }
  
  @Override
  public void shutdown() {
    if (getState() == ViewProcessState.TERMINATED) {
      return;
    }
    shutdownCore();
  }

  //-------------------------------------------------------------------------
  // Lifecycle
  //-------------------------------------------------------------------------
  
  @Override
  public void start() {
    // Lifecycle method - nothing to start
  }

  @Override
  public void stop() {
    shutdown();
  }

  @Override
  public boolean isRunning() {
    // This is 'running' from a Lifecycle perspective, not whether the job is running.
    return getState() != ViewProcessState.TERMINATED;
  }

  @Override
  public void suspend() {
    s_logger.info("Suspending view process {}", getUniqueId());
    lock();
    if (getComputationJob() != null) {
      s_logger.debug("Suspending calculation job");
      getComputationJob().terminate();
      if (getComputationThread().getState() == Thread.State.TIMED_WAITING) {
        // In this case it might be waiting on a recalculation pass. Interrupt it.
        s_logger.debug("Interrupting calculation job thread");
        getComputationThread().interrupt();
      }
      setComputationJob(null);
      try {
        s_logger.debug("Waiting for calculation thread to finish");
        getComputationThread().wait();
      } catch (InterruptedException e) {
        s_logger.warn("Interrupted waiting for calculation thread");
        throw new OpenGammaRuntimeException("Couldn't suspend view process", e);
      } finally {
        setComputationThread(null);
      }
    }
    s_logger.info("View process {} suspended", getUniqueId());
  }

  @Override
  public void resume() {
    s_logger.info("Resuming view process {}", getUniqueId());
    unlock();
  }

  // -------------------------------------------------------------------------
  @Override
  public String toString() {
    return "ViewProcess[" + getUniqueId() + " on " + getDefinitionName() + "]";
  }

  //-------------------------------------------------------------------------
  public UniqueIdentifier generateCycleId() {
    String cycleVersion = Long.toString(_cycleVersion.getAndIncrement());
    return UniqueIdentifier.of(_cycleObjectId, cycleVersion);
  }
  
  public void viewCompiled(CompiledViewDefinitionImpl compiledViewDefinition) {
    _latestCompiledViewDefinition.set(compiledViewDefinition);
    for (ViewProcessListener listener : _allListeners) {
      try {
        listener.viewDefinitionCompiled(compiledViewDefinition);
      } catch (Exception e) {
        logListenerError(listener, e);
      }
    }
  }
  
  public void cycleCompleted(ViewCycle cycle) {
    // Caller MUST NOT hold the semaphore
    s_logger.debug("View cycle {} completed on view process {}", cycle.getUniqueId(), getUniqueId());
    lock();
    try {
      cycleCompletedCore(cycle);
    } finally {
      unlock();
    }
  }

  private void cycleCompletedCore(ViewCycle cycle) {
    // Caller MUST hold the semaphore

    // [PLAT-1158]
    // REVIEW kirk 2009-09-24 -- We need to consider this method for background execution
    // of some kind. It holds the lock and blocks the recalc thread, so a slow
    // callback implementation (or just the cost of computing the delta model) will
    // be an unnecessary burden.

    // We swap these first so that in the callback the process is consistent.
    ViewComputationResultModel result = cycle.getResultModel();
    ViewComputationResultModel previousResult = _latestResult.get();
    _latestResult.set(result);
    
    ViewDeltaResultModel deltaResult = null;
    if (!_allListeners.isEmpty()) {
      for (ViewProcessListener listener : _allListeners) {
        try {
          if (listener.isDeltaResultRequired()) {
            if (deltaResult == null) {
              deltaResult = ViewDeltaResultCalculator.computeDeltaModel(getDefinition(), previousResult, result);
            }
            listener.result(result, deltaResult);
          } else {
            listener.result(result, null);
          }
        } catch (Exception e) {
          logListenerError(listener, e);
        }
      }
    }
  }
  
  public void processError(ViewProcessErrorType errorType, String details, Exception exception) {
    s_logger.error("View process error " + errorType + " occurred: " + details, exception);
    if (!_allListeners.isEmpty()) {
      for (ViewProcessListener listener : _allListeners) {
        try {
          listener.error(errorType, details, exception);
        } catch (Exception e) {
          logListenerError(listener, e);
        }
      }
    }
  }
  
  public void processCompleted() {
    // Caller MUST NOT hold the semaphore
    s_logger.debug("Computation job completed on view {}. No further cycles to run.", this);
    lock();
    try {
      setState(ViewProcessState.FINISHED);
    } finally {
      unlock();
    }
    
    if (!_allListeners.isEmpty()) {
      for (ViewProcessListener listener : _allListeners) {
        try {
          listener.processCompleted();
        } catch (Exception e) {
          logListenerError(listener, e);
        }
      }
    }
  }
  
  //-------------------------------------------------------------------------
  private void lock() {
    try {
      s_logger.debug("Attempt to acquire lock by thread " + Thread.currentThread().getName());
      _processLock.acquire();
      s_logger.debug("Lock acquired by thread " + Thread.currentThread().getName());
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
  }

  private void unlock() {
    _processLock.release();
    s_logger.debug("Lock released by thread " + Thread.currentThread().getName());
  }

  /**
   * Sets the current view process state.
   * 
   * @param computationState  the new view process state
   */
  private void setState(ViewProcessState state) {
    _state = state;
  }

  /**
   * Sets the current view computation job.
   * <p>
   * External visibility for testing.
   * 
   * @return  the current view computation job
   */
  public ViewComputationJob getComputationJob() {
    return _computationJob;
  }

  /**
   * Sets the current computation job
   * 
   * @param computationJob  the current computation job
   */
  private void setComputationJob(ViewComputationJob computationJob) {
    _computationJob = computationJob;
  }

  /**
   * Gets the current computation job's thread
   * <p>
   * External visibility for testing.
   * 
   * @return  the current computation job thread
   */
  public Thread getComputationThread() {
    return _computationThread;
  }

  /**
   * Sets the current computation job's thread
   * 
   * @param recalcThread  the current computation job thread
   */
  private void setComputationThread(Thread computationJobThread) {
    _computationThread = computationJobThread;
  }
  
  private ViewProcessContext getProcessContext() {
    return _viewProcessContext;
  }
  
  private ViewCycleManagerImpl getCycleManager() {
    return _viewCycleManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Attaches a listener to the view process, optionally adding execution demand.
   * <p>
   * The method operates with set semantics, so duplicate notifications for the same listener have no effect.
   * 
   * @param listener  the listener, not null
   * @param addExecutionDemand  {@code true} if the listener should add execution demand to the process, {@code false} otherwise
   * @return the permission provider for the process, not null
   */
  public ViewPermissionProvider attachListener(ViewProcessListener listener, boolean addExecutionDemand) {
    ArgumentChecker.notNull(listener, "listener");
    // Caller MUST NOT hold the semaphore
    lock();
    try {
      _allListeners.add(listener);
      
      // Push initial state to listener
      CompiledViewDefinitionImpl latestCompilation = _latestCompiledViewDefinition.get();
      if (latestCompilation != null) {
        listener.viewDefinitionCompiled(_latestCompiledViewDefinition.get());
        listener.result(_latestResult.get(), null);
      }
      
      if (addExecutionDemand) {
        if (_listenerExecutionDemand.size() == 0) {
          startComputationJob();
        }
        _listenerExecutionDemand.add(listener);
      }
      
      return getProcessContext().getViewPermissionProvider();
    } finally {
      unlock();
    }
  }

  /**
   * Removes a listener from the view process. Removal of the last listener generating execution demand will cause the
   * process to stop.
   * <p>
   * The method operates with set semantics, so duplicate notifications for the same listener have no effect.
   * 
   * @param listener  the listener, not null 
   */
  public void detachListener(ViewProcessListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    // Caller MUST NOT hold the semaphore
    lock();
    try {
      _allListeners.remove(listener);
      if (_listenerExecutionDemand.remove(listener) && _listenerExecutionDemand.size() == 0) {
        stopComputationJob();
      }
    } finally {
      unlock();
    }
  }
  
  public boolean hasExecutionDemand() {
    return !_listenerExecutionDemand.isEmpty();
  }
  
  public ViewExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  /**
   * Starts the background job responsible for running computation cycles for this view process.
   */
  private void startComputationJob() {
    // Caller MUST hold the semaphore
    s_logger.info("Starting computation on view process {}...", this);
    switch (getState()) {
      case STOPPED:
        // Normal state of play. Continue as normal.
        break;
      case RUNNING:
        throw new IllegalStateException("Already running.");
      case TERMINATED:
        throw new IllegalStateException("A terminated view process cannot be used.");
    }
    
    ViewComputationJob computationJob = new ViewComputationJob(this, _executionOptions, getProcessContext(), getCycleManager());
    Thread computationJobThread = new Thread(computationJob, "Computation job for " + this);

    setComputationJob(computationJob);
    setComputationThread(computationJobThread);
    setState(ViewProcessState.RUNNING);
    computationJobThread.start();
    
    s_logger.info("Started.");
  }

  /**
   * Instructs the background computation job to finish. The background job might actually terminate asynchronously,
   * but any outstanding result will be discarded. A replacement background computation job job may be started immediately.
   */
  private void stopComputationJob() {
    // Caller MUST hold the semaphore
    s_logger.info("Stopping computation on view process {}...", this);
    if (getState() != ViewProcessState.RUNNING) {
      return;
    }
    terminateComputationJob();
    setState(ViewProcessState.STOPPED);
    s_logger.info("Stopped.");
  }

  //-------------------------------------------------------------------------
  private void shutdownCore() {
    // Caller MUST NOT hold the semaphore
    boolean isInterrupting;
    final Set<ViewProcessListener> listeners;
    lock();
    try {
      isInterrupting = getState() == ViewProcessState.RUNNING;
      setState(ViewProcessState.TERMINATED);
      
      listeners = new HashSet<ViewProcessListener>(_allListeners);
      _allListeners.clear();
      _listenerExecutionDemand.clear();
      terminateComputationJob();
    } finally {
      unlock();
    }

    // Don't hold semaphore while calling out
    if (isInterrupting) {
      for (ViewProcessListener listener : listeners) {
        try {
          listener.error(ViewProcessErrorType.VIEW_PROCESS_TERMINATED, "View process was shut down while running", null);
        } catch (Exception e) {
          logListenerError(listener, e);
        }
      }
    }
    
    for (ViewProcessListener listener : listeners) {
      try {
        listener.shutdown();
      } catch (Exception e) {
        logListenerError(listener, e);
      }
    }
  }

  private void logListenerError(ViewProcessListener listener, Exception e) {
    s_logger.error("Error while calling listener " + listener, e);
  }
  
  private void terminateComputationJob() {
    if (getComputationJob() == null) {
      return;
    }

    getComputationJob().terminate();
    if (getComputationThread().getState() == Thread.State.TIMED_WAITING) {
      // In this case it might be waiting for the next computation cycle to commence. Interrupt it.
      getComputationThread().interrupt();
    }

    // Let go of the job/thread and allow it to die on its own. A computation cycle might be taking place on this
    // thread, but it will not update the view process with its result because it has been terminated. As far as the
    // view process is concerned, live computation has now stopped, and it may be started again immediately in a new
    // thread. There is no need to slow things down by waiting for the thread to die.
    setComputationJob(null);
    setComputationThread(null);
  }
  
}
