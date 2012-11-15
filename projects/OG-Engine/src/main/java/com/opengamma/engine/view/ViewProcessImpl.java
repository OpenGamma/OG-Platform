/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calc.EngineResourceManagerInternal;
import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.engine.view.calc.ViewComputationJob;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.calc.ViewCycleMetadata;
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
public class ViewProcessImpl implements ViewProcessInternal, ExecutionLogModeSource, Lifecycle {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcess.class);

  private final UniqueId _viewProcessId;
  private final UniqueId _viewDefinitionId;
  private final ViewExecutionOptions _executionOptions;
  private final ViewProcessContext _viewProcessContext;
  private final ObjectId _cycleObjectId;
  private final EngineResourceManagerInternal<SingleComputationCycle> _cycleManager;

  private final AtomicLong _cycleVersion = new AtomicLong();

  /**
   * Manages access to critical regions of the process. Note that the use of {@link Semaphore} rather than, for example, {@link ReentrantLock} allows one thread to acquire the lock and another thread
   * to release it as part of the same sequence of operations. This could be important for {@link #suspend()} and {@link #resume()}.
   */
  private final Semaphore _processLock = new Semaphore(1);

  private final Set<ViewResultListener> _listeners = new HashSet<ViewResultListener>();

  private volatile ViewProcessState _state = ViewProcessState.STOPPED;

  private volatile ViewComputationJob _computationJob;
  private volatile Thread _computationThread;

  private final Map<ValueSpecification, Integer> _elevatedResultSpecs = new ConcurrentHashMap<ValueSpecification, Integer>();

  private final AtomicReference<Pair<CompiledViewDefinitionWithGraphsImpl, MarketDataPermissionProvider>> _latestCompiledViewDefinition =
      new AtomicReference<Pair<CompiledViewDefinitionWithGraphsImpl, MarketDataPermissionProvider>>();
  private final AtomicReference<ViewComputationResultModel> _latestResult = new AtomicReference<ViewComputationResultModel>();

  /**
   * Constructs an instance.
   *
   * @param viewProcessId the unique identifier of the view process, not null
   * @param viewDefinitionId the name of the view definition, not null
   * @param executionOptions the view execution options, not null
   * @param viewProcessContext the process context, not null
   * @param cycleManager the view cycle manager, not null
   * @param cycleObjectId the object identifier of cycles, not null
   */
  public ViewProcessImpl(final UniqueId viewProcessId, final UniqueId viewDefinitionId, final ViewExecutionOptions executionOptions,
                         final ViewProcessContext viewProcessContext,
                         final EngineResourceManagerInternal<SingleComputationCycle> cycleManager, final ObjectId cycleObjectId) {
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
    return getProcessContext().getConfigSource().getConfig(ViewDefinition.class, getDefinitionId());
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
    // Caller MUST NOT hold the semaphore
    final boolean isInterrupting;
    final ViewResultListener[] listeners;
    lock();
    try {
      isInterrupting = (getState() == ViewProcessState.RUNNING);
      setState(ViewProcessState.TERMINATED);
      listeners = _listeners.toArray(new ViewResultListener[_listeners.size()]);
      _listeners.clear();
      terminateComputationJob();
    } finally {
      unlock();
    }
    // [PLAT-1158] The notifications are performed outside of holding the lock which avoids the deadlock problem, but we'll still block
    // for completion which was the thing PLAT-1158 was trying to avoid. This is because the contracts for the order in which
    // notifications can be received is unclear and I don't want to risk introducing a change at this moment in time.
    for (final ViewResultListener listener : listeners) {
      try {
        listener.processTerminated(isInterrupting);
      } catch (final Exception e) {
        logListenerError(listener, e);
      }
    }
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
    // Caller MUST NOT hold the semaphore
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
        getComputationThread().join();
      } catch (final InterruptedException e) {
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
    // Caller MUST still hold the semaphore (from the previous call to suspend)
    s_logger.info("Resuming view process {}", getUniqueId());
    if (getState() == ViewProcessState.RUNNING) {
      s_logger.info("Restarting computation job for view process {}", getUniqueId());
      startComputationJobImpl();
      s_logger.info("Restarted computation job for view process {}", getUniqueId());
    }
    unlock();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "ViewProcess[" + getUniqueId() + " on " + getDefinitionId() + "]";
  }

  //-------------------------------------------------------------------------
  /**
   * Ensures at least a minimum level of logging output is present in the results for the given value specifications. Changes will take effect from the next computation cycle.
   * <p>
   * Each call to elevate the minimum level of logging output for a result must be paired with exactly one call to reduce the level of logging output, if required.
   *
   * @param minimumLogMode the minimum log mode to ensure, not null
   * @param resultSpecifications the result specifications affected, not null or empty
   */
  public void setMinimumLogMode(final ExecutionLogMode minimumLogMode, final Set<ValueSpecification> resultSpecifications) {
    // Synchronization ensures only one writer, while getExecutionLogMode is allowed to read from the ConcurrentHashMap
    // without further locking.
    switch (minimumLogMode) {
      case INDICATORS:
        for (final ValueSpecification valueSpec : resultSpecifications) {
          synchronized (_elevatedResultSpecs) {
            final Integer value = _elevatedResultSpecs.get(valueSpec);
            if (value == null) {
              continue;
            }
            if (value == 1) {
              _elevatedResultSpecs.remove(valueSpec);
            } else {
              _elevatedResultSpecs.put(valueSpec, value - 1);
            }
          }
        }
        break;
      case FULL:
        for (final ValueSpecification valueSpec : resultSpecifications) {
          synchronized (_elevatedResultSpecs) {
            final Integer value = _elevatedResultSpecs.get(valueSpec);
            if (value == null) {
              _elevatedResultSpecs.put(valueSpec, 1);
            } else {
              _elevatedResultSpecs.put(valueSpec, value + 1);
            }
          }
        }
        break;
    }
  }

  @Override
  public ExecutionLogMode getLogMode(final ValueSpecification valueSpec) {
    return _elevatedResultSpecs.containsKey(valueSpec) ? ExecutionLogMode.FULL : ExecutionLogMode.INDICATORS;
  }

  @Override
  public ExecutionLogMode getLogMode(final Set<ValueSpecification> valueSpecs) {
    for (final ValueSpecification valueSpec : valueSpecs) {
      if (getLogMode(valueSpec) == ExecutionLogMode.FULL) {
        return ExecutionLogMode.FULL;
      }
    }
    return ExecutionLogMode.INDICATORS;
  }

  //-------------------------------------------------------------------------
  public UniqueId generateCycleId() {
    final String cycleVersion = Long.toString(_cycleVersion.getAndIncrement());
    return UniqueId.of(_cycleObjectId, cycleVersion);
  }

  public void viewDefinitionCompiled(final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition, final MarketDataPermissionProvider permissionProvider) {
    // Caller MUST NOT hold the semaphore
    final ViewResultListener[] listeners;
    lock();
    try {
      _latestCompiledViewDefinition.set(Pair.of(compiledViewDefinition, permissionProvider));
      listeners = _listeners.toArray(new ViewResultListener[_listeners.size()]);
    } finally {
      unlock();
    }
    final Set<ValueRequirement> marketDataRequirements = compiledViewDefinition.getMarketDataRequirements().keySet();
    // [PLAT-1158] The notifications are performed outside of holding the lock which avoids the deadlock problem, but we'll still block
    // for completion which was the thing PLAT-1158 was trying to avoid. This is because the contracts for the order in which
    // notifications can be received is unclear and I don't want to risk introducing a change at this moment in time.
    for (final ViewResultListener listener : listeners) {
      try {
        final UserPrincipal listenerUser = listener.getUser();
        final boolean hasMarketDataPermissions = permissionProvider.checkMarketDataPermissions(listenerUser, marketDataRequirements).isEmpty();
        listener.viewDefinitionCompiled(compiledViewDefinition, hasMarketDataPermissions);
      } catch (final Exception e) {
        logListenerError(listener, e);
      }
    }
  }

  public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
    // Caller MUST NOT hold the semaphore
    s_logger.error("View definition compilation failed for " + valuationTime + ": ", exception);
    final ViewResultListener[] listeners;
    lock();
    try {
      listeners = _listeners.toArray(new ViewResultListener[_listeners.size()]);
    } finally {
      unlock();
    }
    // [PLAT-1158] The notifications are performed outside of holding the lock which avoids the deadlock problem, but we'll still block
    // for completion which was the thing PLAT-1158 was trying to avoid. This is because the contracts for the order in which
    // notifications can be received is unclear and I don't want to risk introducing a change at this moment in time.
    for (final ViewResultListener listener : listeners) {
      try {
        listener.viewDefinitionCompilationFailed(valuationTime, exception);
      } catch (final Exception e) {
        logListenerError(listener, e);
      }
    }
  }

  public void cycleCompleted(final ViewCycle cycle) {
    // Caller MUST NOT hold the semaphore
    s_logger.debug("View cycle {} completed on view process {}", cycle.getUniqueId(), getUniqueId());
    final ViewComputationResultModel result;
    final ViewDeltaResultModel deltaResult;
    final ViewResultListener[] listeners;
    lock();
    try {
      result = cycle.getResultModel();
      // We swap these first so that in the callback the process is consistent.
      final ViewComputationResultModel previousResult = _latestResult.getAndSet(result);
      // [PLAT-1158] Is the cost of computing the delta going to be high; should we offload that to a slave thread before dispatching to the listeners?
      deltaResult = ViewDeltaResultCalculator.computeDeltaModel(cycle.getCompiledViewDefinition().getViewDefinition(), previousResult, result);
      listeners = _listeners.toArray(new ViewResultListener[_listeners.size()]);
    } finally {
      unlock();
    }
    // [PLAT-1158] The notifications are performed outside of holding the lock which avoids the deadlock problem, but we'll still block
    // for completion which was the thing PLAT-1158 was trying to avoid. This is because the contracts for the order in which
    // notifications can be received is unclear and I don't want to risk introducing a change at this moment in time.
    for (final ViewResultListener listener : listeners) {
      try {
        listener.cycleCompleted(result, deltaResult);
      } catch (final Exception e) {
        logListenerError(listener, e);
      }
    }
  }

  public void cycleStarted(final ViewCycleMetadata cycleInfo) {
    // Caller MUST NOT hold the semaphore
    s_logger.debug("View cycle {} initiated on view process {}", cycleInfo, getUniqueId());
    final ViewResultListener[] listeners;
    lock();
    try {
      listeners = _listeners.toArray(new ViewResultListener[_listeners.size()]);
    } finally {
      unlock();
    }
    // [PLAT-1158] The notifications are performed outside of holding the lock which avoids the deadlock problem, but we'll still block
    // for completion which was the thing PLAT-1158 was trying to avoid. This is because the contracts for the order in which
    // notifications can be received is unclear and I don't want to risk introducing a change at this moment in time.
    for (final ViewResultListener listener : listeners) {
      try {
        listener.cycleStarted(cycleInfo);
      } catch (final Exception e) {
        logListenerError(listener, e);
      }
    }
  }

  public void cycleFragmentCompleted(final ViewComputationResultModel fullFragment, final ViewDefinition viewDefinition) {
    // Caller MUST NOT hold the semaphore
    s_logger.debug("Result fragment from cycle {} received on view process {}", fullFragment.getViewCycleId(), getUniqueId());
    final ViewDeltaResultModel deltaFragment;
    final ViewResultListener[] listeners;
    lock();
    try {
      // [PLAT-1158] Is the cost of computing the delta going to be high; should we offload that to a slave thread before dispatching to the listeners?
      final ViewComputationResultModel previousResult = _latestResult.get();
      deltaFragment = ViewDeltaResultCalculator.computeDeltaModel(viewDefinition, previousResult, fullFragment);
      listeners = _listeners.toArray(new ViewResultListener[_listeners.size()]);
    } finally {
      unlock();
    }
    // [PLAT-1158] The notifications are performed outside of holding the lock which avoids the deadlock problem, but we'll still block
    // for completion which was the thing PLAT-1158 was trying to avoid. This is because the contracts for the order in which
    // notifications can be received is unclear and I don't want to risk introducing a change at this moment in time.
    for (final ViewResultListener listener : listeners) {
      try {
        listener.cycleFragmentCompleted(fullFragment, deltaFragment);
      } catch (final Exception e) {
        logListenerError(listener, e);
      }
    }
  }

  public void cycleExecutionFailed(final ViewCycleExecutionOptions executionOptions, final Exception exception) {
    // Caller MUST NOT hold the semaphore
    s_logger.error("Cycle execution failed for " + executionOptions + ": ", exception);
    final ViewResultListener[] listeners;
    lock();
    try {
      listeners = _listeners.toArray(new ViewResultListener[_listeners.size()]);
    } finally {
      unlock();
    }
    // [PLAT-1158] The notifications are performed outside of holding the lock which avoids the deadlock problem, but we'll still block
    // for completion which was the thing PLAT-1158 was trying to avoid. This is because the contracts for the order in which
    // notifications can be received is unclear and I don't want to risk introducing a change at this moment in time.
    for (final ViewResultListener listener : listeners) {
      try {
        listener.cycleExecutionFailed(executionOptions, exception);
      } catch (final Exception e) {
        logListenerError(listener, e);
      }
    }
  }

  public void processCompleted() {
    // Caller MUST NOT hold the semaphore
    s_logger.debug("Computation job completed on view {}. No further cycles to run.", this);
    final ViewResultListener[] listeners;
    lock();
    try {
      setState(ViewProcessState.FINISHED);
      listeners = _listeners.toArray(new ViewResultListener[_listeners.size()]);
    } finally {
      unlock();
    }
    // [PLAT-1158] The notifications are performed outside of holding the lock which avoids the deadlock problem, but we'll still block
    // for completion which was the thing PLAT-1158 was trying to avoid. This is because the contracts for the order in which
    // notifications can be received is unclear and I don't want to risk introducing a change at this moment in time.
    for (final ViewResultListener listener : listeners) {
      try {
        listener.processCompleted();
      } catch (final Exception e) {
        logListenerError(listener, e);
      }
    }
  }

  //-------------------------------------------------------------------------
  private void lock() {
    try {
      s_logger.debug("Attempt to acquire lock by thread {}", Thread.currentThread().getName());
      _processLock.acquire();
      s_logger.debug("Lock acquired by thread {}", Thread.currentThread().getName());
    } catch (final InterruptedException e) {
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
   * @param state the new view process state
   */
  private void setState(final ViewProcessState state) {
    _state = state;
  }

  /**
   * Sets the current view computation job.
   * <p>
   * External visibility for testing.
   *
   * @return the current view computation job
   */
  public ViewComputationJob getComputationJob() {
    return _computationJob;
  }

  /**
   * Sets the current computation job
   *
   * @param computationJob the current computation job
   */
  private void setComputationJob(final ViewComputationJob computationJob) {
    _computationJob = computationJob;
  }

  /**
   * Gets the current computation job's thread
   * <p>
   * External visibility for testing.
   *
   * @return the current computation job thread
   */
  public Thread getComputationThread() {
    return _computationThread;
  }

  /**
   * Sets the current computation job's thread
   *
   * @param computationJobThread the current computation job thread
   */
  private void setComputationThread(final Thread computationJobThread) {
    _computationThread = computationJobThread;
  }

  private ViewProcessContext getProcessContext() {
    return _viewProcessContext;
  }

  private EngineResourceManagerInternal<SingleComputationCycle> getCycleManager() {
    return _cycleManager;
  }

  /**
   * Attaches a listener to the view process.
   * <p>
   * The method operates with set semantics, so duplicate notifications for the same listener have no effect.
   *
   * @param listener the listener, not null
   * @return the permission provider for the process, not null
   */
  public ViewPermissionProvider attachListener(final ViewResultListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    // Caller MUST NOT hold the semaphore
    Pair<CompiledViewDefinitionWithGraphsImpl, MarketDataPermissionProvider> latestCompilation = null;
    ViewComputationResultModel latestResult = null;
    lock();
    try {
      if (_listeners.add(listener)) {
        if (_listeners.size() == 1) {
          try {
            startComputationJob();
          } catch (final Exception e) {
            // Roll-back
            _listeners.remove(listener);
            s_logger.error("Failed to start computation job while adding listener for view process {}", this);
            throw new OpenGammaRuntimeException("Failed to start computation job while adding listener for view process " + toString(), e);
          }
        } else {
          // Push initial state to listener
          latestCompilation = _latestCompiledViewDefinition.get();
          if (latestCompilation != null) {
            latestResult = _latestResult.get();
          }
        }
      }
    } finally {
      unlock();
    }
    if (latestCompilation != null) {
      // [PLAT-1158] The initial notification is performed outside of holding the lock which avoids the deadlock problem, but we'll still
      // block for completion which was the thing PLAT-1158 was trying to avoid. This is because the contracts for the order in which
      // notifications can be received is unclear and I don't want to risk introducing a change at this moment in time.
      try {
        final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = latestCompilation.getFirst();
        final MarketDataPermissionProvider permissionProvider = latestCompilation.getSecond();
        final Set<ValueRequirement> marketDataRequirements = compiledViewDefinition.getMarketDataRequirements().keySet();
        final Set<ValueRequirement> deniedRequirements =
              permissionProvider.checkMarketDataPermissions(listener.getUser(), marketDataRequirements);
        final boolean hasMarketDataPermissions = deniedRequirements.isEmpty();
        listener.viewDefinitionCompiled(compiledViewDefinition, hasMarketDataPermissions);
        if (latestResult != null) {
          listener.cycleCompleted(latestResult, null);
        }
      } catch (final Exception e) {
        s_logger.error("Failed to push initial state to listener during attachment");
        logListenerError(listener, e);
      }
    }
    return getProcessContext().getViewPermissionProvider();
  }

  /**
   * Removes a listener from the view process. Removal of the last listener generating execution demand will cause the process to stop.
   * <p>
   * The method operates with set semantics, so duplicate notifications for the same listener have no effect.
   *
   * @param listener the listener, not null
   */
  public void detachListener(final ViewResultListener listener) {
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
    // Caller MUST NOT hold the semaphore
    lock();
    try {
      return !_listeners.isEmpty();
    } finally {
      unlock();
    }
  }

  public ViewExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  private void startComputationJobImpl() {
    // Caller MUST hold the semaphore
    try {
      final ViewComputationJob computationJob = new ViewComputationJob(this, _executionOptions, getProcessContext(), getCycleManager());
      final Thread computationJobThread = new Thread(computationJob, "Computation job for " + this);

      setComputationJob(computationJob);
      setComputationThread(computationJobThread);
      computationJobThread.start();
    } catch (final Exception e) {
      // Roll-back
      terminateComputationJob();
      s_logger.error("Failed to start computation job for view process " + toString(), e);
      throw new OpenGammaRuntimeException("Failed to start computation job for view process " + toString(), e);
    }
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
      case FINISHED:
        throw new IllegalStateException("The computation job has already been run.");
      case TERMINATED:
        throw new IllegalStateException("A terminated view process cannot be used.");
    }
    setState(ViewProcessState.RUNNING);
    startComputationJobImpl();
    s_logger.info("Started computation job for view process {}", this);
  }

  /**
   * Instructs the background computation job to finish. The background job might actually terminate asynchronously, but any outstanding result will be discarded. A replacement background computation
   * job may be started immediately.
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

  private void logListenerError(final ViewResultListener listener, final Exception e) {
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
