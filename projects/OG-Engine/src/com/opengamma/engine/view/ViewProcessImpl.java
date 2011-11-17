/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.marketdata.permission.MarketDataPermissionProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.calc.EngineResourceManagerInternal;
import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.engine.view.calc.ViewComputationJob;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewDeltaResultCalculator;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Default implementation of {@link ViewProcess}.
 */
public class ViewProcessImpl implements ViewProcessInternal, Lifecycle {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcess.class);

  private final UniqueId _viewProcessId;
  private final UniqueId _viewDefinitionId;
  private final ViewExecutionOptions _executionOptions;
  private final ViewProcessContext _viewProcessContext;
  private final ObjectId _cycleObjectId;
  private final EngineResourceManagerInternal<SingleComputationCycle> _cycleManager;

  private final AtomicLong _cycleVersion = new AtomicLong();

  /**
   * Manages access to critical regions of the process. Note that the use of {@link Semaphore} rather than, for example,
   * {@link ReentrantLock} allows one thread to acquire the lock and another thread to release it as part of the same
   * sequence of operations. This could be important for {@link #suspend()} and {@link #resume()}. 
   */
  private final Semaphore _processLock = new Semaphore(1);

  private final Set<ViewResultListener> _listeners = new HashSet<ViewResultListener>();

  private volatile ViewProcessState _state = ViewProcessState.STOPPED;

  private volatile ViewComputationJob _computationJob;
  private volatile Thread _computationThread;

  private final AtomicReference<Pair<CompiledViewDefinitionWithGraphsImpl, MarketDataPermissionProvider>> _latestCompiledViewDefinition =
      new AtomicReference<Pair<CompiledViewDefinitionWithGraphsImpl, MarketDataPermissionProvider>>();
  private final AtomicReference<ViewComputationResultModel> _latestResult = new AtomicReference<ViewComputationResultModel>();

  private ExecutorService _calcJobResultExecutorService = Executors.newSingleThreadExecutor();



  /**
   * Constructs an instance.
   *
   * @param viewProcessId  the unique identifier of the view process, not null
   * @param viewDefinitionId  the name of the view definition, not null
   * @param executionOptions  the view execution options, not null
   * @param viewProcessContext  the process context, not null
   * @param cycleManager  the view cycle manager, not null
   * @param cycleObjectId  the object identifier of cycles, not null
   */
  public ViewProcessImpl(UniqueId viewProcessId, UniqueId viewDefinitionId, ViewExecutionOptions executionOptions,
                         ViewProcessContext viewProcessContext, EngineResourceManagerInternal<SingleComputationCycle> cycleManager,
                         ObjectId cycleObjectId) {
    ArgumentChecker.notNull(viewProcessId, "viewProcessId");
    ArgumentChecker.notNull(viewDefinitionId, "viewDefinitionID");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ArgumentChecker.notNull(viewProcessContext, "viewProcessContext");
    ArgumentChecker.notNull(cycleManager, "cycleManager");
    ArgumentChecker.notNull(cycleObjectId, "cycleObjectId");

    _viewProcessId = viewProcessId;
    _viewDefinitionId = viewDefinitionId;
    _executionOptions = executionOptions;
    _viewProcessContext = viewProcessContext;
    _cycleManager = cycleManager;
    _cycleObjectId = cycleObjectId;
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId getUniqueId() {
    return _viewProcessId;
  }

  @Override
  public UniqueId getDefinitionId() {
    return _viewDefinitionId;
  }

  @Override
  public ViewDefinition getLatestViewDefinition() {
    return getProcessContext().getViewDefinitionRepository().getDefinition(getDefinitionId());
  }

  @Override
  public MarketDataInjector getLiveDataOverrideInjector() {
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

  public void triggerCycle() {
    getComputationJob().triggerCycle();
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

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "ViewProcess[" + getUniqueId() + " on " + getDefinitionId() + "]";
  }

  //-------------------------------------------------------------------------
  public UniqueId generateCycleId() {
    String cycleVersion = Long.toString(_cycleVersion.getAndIncrement());
    return UniqueId.of(_cycleObjectId, cycleVersion);
  }

  public void viewDefinitionCompiled(CompiledViewDefinitionWithGraphsImpl compiledViewDefinition, MarketDataPermissionProvider permissionProvider) {
    lock();
    try {
      _latestCompiledViewDefinition.set(Pair.of(compiledViewDefinition, permissionProvider));
      final Set<ValueRequirement> marketDataRequirements = compiledViewDefinition.getMarketDataRequirements().keySet();
      for (ViewResultListener listener : _listeners) {
        try {
          UserPrincipal listenerUser = listener.getUser();
          boolean hasMarketDataPermissions = permissionProvider.canAccessMarketData(listenerUser, marketDataRequirements);
          listener.viewDefinitionCompiled(compiledViewDefinition, hasMarketDataPermissions);
        } catch (Exception e) {
          logListenerError(listener, e);
        }
      }
    } finally {
      unlock();
    }
  }

  public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
    s_logger.error("View definition compilation failed for " + valuationTime + ": ", exception);
    for (ViewResultListener listener : _listeners) {
      try {
        listener.viewDefinitionCompilationFailed(valuationTime, exception);
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

  public void cycleFragmentCompleted(ViewComputationResultModel result) {
    // Caller MUST NOT hold the semaphore
    s_logger.debug("Result fragment from cycle {} received on view process {}", result.getViewCycleId(), getUniqueId());
    lock();
    try {
      cycleFragmentCompletedCore(result);
    } finally {
      unlock();
    }
  }

  private void cycleFragmentCompletedCore(ViewComputationResultModel fullFragment) {
    // Caller MUST hold the semaphore

    // [PLAT-1158]
    // REVIEW kirk 2009-09-24 -- We need to consider this method for background execution
    // of some kind. It holds the lock and blocks the recalc thread, so a slow
    // callback implementation (or just the cost of computing the delta model) will
    // be an unnecessary burden.

    // We swap these first so that in the callback the process is consistent.
    ViewComputationResultModel previousResult = _latestResult.get();
    ViewDefinition latestViewDefinition = getLatestViewDefinition();

    ViewDeltaResultModel deltaFragment = ViewDeltaResultCalculator.computeDeltaModel(latestViewDefinition, previousResult, fullFragment);
    for (ViewResultListener listener : _listeners) {
      try {
        listener.cycleFragmentCompleted(fullFragment, deltaFragment);
      } catch (Exception e) {
        logListenerError(listener, e);
      }
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

    ViewDeltaResultModel deltaResult = ViewDeltaResultCalculator.computeDeltaModel(cycle.getCompiledViewDefinition().getViewDefinition(), previousResult, result);
    for (ViewResultListener listener : _listeners) {
      try {
        listener.cycleCompleted(result, deltaResult);
      } catch (Exception e) {
        logListenerError(listener, e);
      }
    }
  }

  public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
    s_logger.error("Cycle execution failed for " + executionOptions + ": ", exception);
    for (ViewResultListener listener : _listeners) {
      try {
        listener.cycleExecutionFailed(executionOptions, exception);
      } catch (Exception e) {
        logListenerError(listener, e);
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

    for (ViewResultListener listener : _listeners) {
      try {
        listener.processCompleted();
      } catch (Exception e) {
        logListenerError(listener, e);
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

  private EngineResourceManagerInternal<SingleComputationCycle> getCycleManager() {
    return _cycleManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Attaches a listener to the view process.
   * <p>
   * The method operates with set semantics, so duplicate notifications for the same listener have no effect.
   *
   * @param listener  the listener, not null
   * @return the permission provider for the process, not null
   */
  public ViewPermissionProvider attachListener(ViewResultListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    // Caller MUST NOT hold the semaphore
    lock();
    try {
      if (_listeners.add(listener)) {
        if (_listeners.size() == 1) {
          try {
            startComputationJob();
          } catch (Exception e) {
            // Roll-back
            _listeners.remove(listener);
            s_logger.error("Failed to start computation job while adding listener for view process {}", this);
            throw new OpenGammaRuntimeException("Failed to start computation job while adding listener for view process " + toString(), e);
          }
        } else {
          // Push initial state to listener
          try {
            Pair<CompiledViewDefinitionWithGraphsImpl, MarketDataPermissionProvider> latestCompilation = _latestCompiledViewDefinition.get();
            if (latestCompilation != null) {
              CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = latestCompilation.getFirst();
              MarketDataPermissionProvider permissionProvider = latestCompilation.getSecond();
              boolean hasMarketDataPermissions = permissionProvider.canAccessMarketData(listener.getUser(), compiledViewDefinition.getMarketDataRequirements().keySet());
              listener.viewDefinitionCompiled(compiledViewDefinition, hasMarketDataPermissions);
              ViewComputationResultModel latestResult = _latestResult.get();
              if (latestResult != null) {
                listener.cycleCompleted(latestResult, null);
              }
            }
          } catch (Exception e) {
            s_logger.error("Failed to push initial state to listener during attachment");
            logListenerError(listener, e);
          }
        }
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
  public void detachListener(ViewResultListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    // Caller MUST NOT hold the semaphore
    lock();
    try {
      if (_listeners.remove(listener) && _listeners.size() == 0) {
        stopComputationJob();
      }
    } finally {
      unlock();
    }
  }

  public boolean hasExecutionDemand() {
    return !_listeners.isEmpty();
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

    try {
      ViewComputationJob computationJob = new ViewComputationJob(this, _executionOptions, getProcessContext(), getCycleManager());
      Thread computationJobThread = new Thread(computationJob, "Computation job for " + this);

      setComputationJob(computationJob);
      setComputationThread(computationJobThread);
      setState(ViewProcessState.RUNNING);
      computationJobThread.start();
    } catch (Exception e) {
      // Roll-back
      terminateComputationJob();
      s_logger.error("Failed to start computation job for view process " + toString(), e);
      throw new OpenGammaRuntimeException("Failed to start computation job for view process " + toString(), e);
    }

    s_logger.info("Started computation job for view process {}", this);
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
    final Set<ViewResultListener> listeners;
    lock();
    try {
      isInterrupting = getState() == ViewProcessState.RUNNING;
      setState(ViewProcessState.TERMINATED);

      listeners = new HashSet<ViewResultListener>(_listeners);
      _listeners.clear();
      terminateComputationJob();
    } finally {
      unlock();
    }

    for (ViewResultListener listener : listeners) {
      try {
        listener.processTerminated(isInterrupting);
      } catch (Exception e) {
        logListenerError(listener, e);
      }
    }
  }

  private void logListenerError(ViewResultListener listener, Exception e) {
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

  public ExecutorService getCalcJobResultExecutorService() {
    return _calcJobResultExecutorService;
  }
}
