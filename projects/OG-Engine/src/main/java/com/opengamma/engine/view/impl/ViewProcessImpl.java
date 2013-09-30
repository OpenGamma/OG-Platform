/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.engine.management.InternalViewResultListener;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessState;
import com.opengamma.engine.view.client.ViewDeltaResultCalculator;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.engine.view.permission.ViewPermissionContext;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.engine.view.worker.ViewExecutionDataProvider;
import com.opengamma.engine.view.worker.ViewProcessWorker;
import com.opengamma.engine.view.worker.ViewProcessWorkerContext;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Default implementation of {@link ViewProcess}.
 */
public class ViewProcessImpl implements ViewProcessInternal, Lifecycle, ViewProcessWorkerContext {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcess.class);

  private final UniqueId _viewDefinitionId;
  private final ViewExecutionOptions _executionOptions;
  private final ViewProcessContext _viewProcessContext;
  private final ViewProcessorImpl _viewProcessor;

  /**
   * Manages access to critical regions of the process. Note that the use of {@link Semaphore} rather than, for example, {@link ReentrantLock} allows one thread to acquire the lock and another thread
   * to release it as part of the same sequence of operations. This could be important for {@link #suspend()} and {@link #resume()}.
   */
  private final Semaphore _processLock = new Semaphore(1);

  /**
   * Key is the listener to which events will be dispatched.
   * Value is true iff that listener requires delta calculations to be performed.
   * When there are no listeners remaining that require delta calculations,
   * they will stop being computed to save CPU and heap.
   */
  private final Map<ViewResultListener, Boolean> _listeners = new HashMap<ViewResultListener, Boolean>();
  private volatile int _internalListenerCount; // only safe if used within lock

  private volatile ViewDefinition _currentViewDefinition;

  private volatile ViewProcessState _state = ViewProcessState.STOPPED;

  private volatile ViewProcessWorker _worker;

  private final AtomicReference<Pair<CompiledViewDefinitionWithGraphs, MarketDataPermissionProvider>> _latestCompiledViewDefinition =
      new AtomicReference<Pair<CompiledViewDefinitionWithGraphs, MarketDataPermissionProvider>>();

  private final AtomicReference<ViewComputationResultModel> _latestResult = new AtomicReference<ViewComputationResultModel>();
  
  private final AtomicBoolean _mustCalculateDeltas = new AtomicBoolean(false);

  private final ChangeListener _viewDefinitionChangeListener;

  // BEGIN TEMPORARY -- See ViewProcessorImpl

  private volatile Object _description;

  public Object getDescriptionKey() {
    return _description;
  }

  public void setDescriptionKey(final Object description) {
    _description = description;
  }

  // END TEMPORARY CODE

  /**
   * Constructs an instance.
   * 
   * @param viewDefinitionId the name of the view definition, not null
   * @param executionOptions the view execution options, not null
   * @param viewProcessContext the process context, not null
   * @param viewProcessor the parent view processor, not null
   */
  public ViewProcessImpl(final UniqueId viewDefinitionId, final ViewExecutionOptions executionOptions,
      final ViewProcessContext viewProcessContext, final ViewProcessorImpl viewProcessor) {
    ArgumentChecker.notNull(viewDefinitionId, "viewDefinitionId");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ArgumentChecker.notNull(viewProcessContext, "viewProcessContext");
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    _viewDefinitionId = viewDefinitionId;
    _executionOptions = executionOptions;
    _viewProcessContext = viewProcessContext;
    _viewProcessor = viewProcessor;
    if (_viewDefinitionId.isVersioned()) {
      _viewDefinitionChangeListener = null;
    } else {
      final ObjectId viewDefinitionObject = viewDefinitionId.getObjectId();
      _viewDefinitionChangeListener = new ChangeListener() {
        @SuppressWarnings("incomplete-switch")
        @Override
        public void entityChanged(ChangeEvent event) {
          if (viewDefinitionObject.equals(event.getObjectId())) {
            switch (event.getType()) {
              case REMOVED:
                s_logger.error("Shutting down view process after removal of view definition {}", viewDefinitionObject);
                shutdown();
                break;
              case CHANGED:
                viewDefinitionChanged();
                break;
            }
          }
        }
      };
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId getUniqueId() {
    return getProcessContext().getProcessId();
  }

  @Override
  public UniqueId getDefinitionId() {
    return _viewDefinitionId;
  }

  @Override
  public ViewDefinition getLatestViewDefinition() {
    if (_currentViewDefinition == null) {
      _currentViewDefinition = getProcessContext().getConfigSource().getConfig(ViewDefinition.class, getDefinitionId());
    }
    return _currentViewDefinition;
  }

  /**
   * Forces the dependency graph to be rebuilt. Invoked when a market data provider becomes available and failed
   * subscriptions need to be retried.
   * @deprecated There should be a better way to do this in the market data layer but PLAT-3908 is a problem.
   * This method will be removed once it's fixed
   */
  @Deprecated
  public void forceGraphRebuild() {
    ViewProcessWorker worker = getWorker();
    if (worker != null) {
      worker.forceGraphRebuild();
    }
  }

  private void viewDefinitionChanged() {
    final ViewDefinition viewDefinition = getProcessContext().getConfigSource().getConfig(ViewDefinition.class, getDefinitionId());
    _currentViewDefinition = viewDefinition;
    ViewProcessWorker worker = getWorker();
    if (worker != null) {
      worker.updateViewDefinition(viewDefinition);
    }
  }

  @Override
  public MarketDataInjector getLiveDataOverrideInjector() {
    return getProcessContext().getLiveDataOverrideInjector();
  }

  @Override
  public ViewProcessState getState() {
    return _state;
  }
  
  public ViewResultListener[] getListenerArray() {
    return _listeners.keySet().toArray(new ViewResultListener[_listeners.size()]);
  }

  @Override
  public void shutdown() {
    if (getState() == ViewProcessState.TERMINATED) {
      return;
    }
    // Must go through the view processor to prevent a client being attached to the terminated view process
    _viewProcessor.shutdownViewProcess(getUniqueId());
  }

  protected void shutdownCore() {
    // Caller MUST NOT hold the semaphore
    final boolean isInterrupting;
    final ViewResultListener[] listeners;
    lock();
    try {
      isInterrupting = (getState() == ViewProcessState.RUNNING);
      setState(ViewProcessState.TERMINATED);
      listeners = getListenerArray();
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
    final ViewProcessWorker worker = getWorker();
    if (worker != null) {
      worker.triggerCycle();
    }
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
    final ViewProcessWorker worker = getWorker();
    if (worker != null) {
      s_logger.debug("Suspending calculation job");
      setWorker(null);
      worker.terminate();
      try {
        s_logger.debug("Waiting for calculation thread(s) to finish");
        worker.join();
      } catch (final InterruptedException e) {
        s_logger.warn("Interrupted waiting for calculation thread(s)");
        throw new OpenGammaRuntimeException("Couldn't suspend view process", e);
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
   * Gets the execution log mode source.
   * 
   * @return the execution log mode source, not null
   */
  public ExecutionLogModeSource getExecutionLogModeSource() {
    return getProcessContext().getExecutionLogModeSource();
  }

  @Override
  public void viewDefinitionCompiled(final ViewExecutionDataProvider dataProvider, final CompiledViewDefinitionWithGraphs compiledViewDefinition) {
    // Caller MUST NOT hold the semaphore
    final ViewResultListener[] listeners;
    final MarketDataPermissionProvider permissionProvider = dataProvider.getPermissionProvider();
    lock();
    try {
      _latestCompiledViewDefinition.set(Pair.of(compiledViewDefinition, permissionProvider));
      listeners = getListenerArray();
    } finally {
      unlock();
    }
    final Set<ValueSpecification> marketData = compiledViewDefinition.getMarketDataRequirements();
    // [PLAT-1158] The notifications are performed outside of holding the lock which avoids the deadlock problem, but we'll still block
    // for completion which was the thing PLAT-1158 was trying to avoid. This is because the contracts for the order in which
    // notifications can be received is unclear and I don't want to risk introducing a change at this moment in time.
    for (final ViewResultListener listener : listeners) {
      try {
        final UserPrincipal listenerUser = listener.getUser();
        final boolean hasMarketDataPermissions = permissionProvider.checkMarketDataPermissions(listenerUser, marketData).isEmpty();
        listener.viewDefinitionCompiled(compiledViewDefinition, hasMarketDataPermissions);
      } catch (final Exception e) {
        logListenerError(listener, e);
      }
    }
    getExecutionLogModeSource().viewDefinitionCompiled(compiledViewDefinition);
  }

  @Override
  public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
    // Caller MUST NOT hold the semaphore
    s_logger.error("View definition compilation failed for " + valuationTime + ": ", exception);
    final ViewResultListener[] listeners;
    lock();
    try {
      listeners = getListenerArray();
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

  @Override
  public void cycleCompleted(final ViewCycle cycle) {
    // Caller MUST NOT hold the semaphore
    s_logger.debug("View cycle {} completed on view process {}", cycle.getUniqueId(), getUniqueId());
    final ViewComputationResultModel result;
    ViewDeltaResultModel deltaResult = null;
    final ViewResultListener[] listeners;
    lock();
    try {
      result = cycle.getResultModel();
      if (_mustCalculateDeltas.get()) {
        // We swap these first so that in the callback the process is consistent.
        final ViewComputationResultModel previousResult = _latestResult.getAndSet(result);
        // [PLAT-1158] Is the cost of computing the delta going to be high; should we offload that to a slave thread before dispatching to the listeners?
        deltaResult = ViewDeltaResultCalculator.computeDeltaModel(cycle.getCompiledViewDefinition().getViewDefinition(), previousResult, result);
      }
      listeners = getListenerArray();
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

  @Override
  public void cycleStarted(final ViewCycleMetadata cycleInfo) {
    // Caller MUST NOT hold the semaphore
    s_logger.debug("View cycle {} initiated on view process {}", cycleInfo, getUniqueId());
    final ViewResultListener[] listeners;
    lock();
    try {
      listeners = getListenerArray();
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

  @Override
  public void cycleFragmentCompleted(final ViewComputationResultModel fullFragment, ViewDefinition viewDefinition) {
    // Caller MUST NOT hold the semaphore
    s_logger.debug("Result fragment from cycle {} received on view process {}", fullFragment.getViewCycleId(), getUniqueId());
    final ViewDeltaResultModel deltaFragment;
    final ViewResultListener[] listeners;
    lock();
    try {
      // [PLAT-1158] Is the cost of computing the delta going to be high; should we offload that to a slave thread before dispatching to the listeners?
      final ViewComputationResultModel previousResult = _latestResult.get();
      deltaFragment = ViewDeltaResultCalculator.computeDeltaModel(viewDefinition, previousResult, fullFragment);
      listeners = getListenerArray();
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

  @Override
  public void cycleExecutionFailed(final ViewCycleExecutionOptions executionOptions, final Exception exception) {
    // Caller MUST NOT hold the semaphore
    s_logger.error("Cycle execution failed for " + executionOptions + ": ", exception);
    final ViewResultListener[] listeners;
    lock();
    try {
      listeners = getListenerArray();
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

  @Override
  public void workerCompleted() {
    // Caller MUST NOT hold the semaphore
    s_logger.debug("Computation job completed on view {}. No further cycles to run.", this);
    final ViewResultListener[] listeners;
    lock();
    try {
      setState(ViewProcessState.FINISHED);
      listeners = getListenerArray();
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
   * Sets the current view process worker. This is the component responsible for coordinating the execution of the process, for example this might be a single local thread, a pool of local threads or
   * a proxy to remotely executing code.
   * <p>
   * External visibility for testing.
   * 
   * @return the current view process worker.
   */
  public ViewProcessWorker getWorker() {
    return _worker;
  }

  /**
   * Sets the current worker
   * 
   * @param worker the current worker
   */
  private void setWorker(final ViewProcessWorker worker) {
    _worker = worker;
  }

  @Override
  public ViewProcessContext getProcessContext() {
    return _viewProcessContext;
  }

  /**
   * Attaches a listener to the view process.
   * <p>
   * The method operates with set semantics, so duplicate notifications for the same listener have no effect.
   * 
   *
   * @param listener the listener, not null
   * @param resultMode the result mode for the listener, not null
   * @param fragmentResultMode the fragment result mode for the listener, not null
   * @return the permission context for the process, not null
   */
  public ViewPermissionContext attachListener(final ViewResultListener listener,
                                              final ViewResultMode resultMode,
                                              final ViewResultMode fragmentResultMode) {
    ArgumentChecker.notNull(listener, "listener");
    ArgumentChecker.notNull(resultMode, "resultMode");
    ArgumentChecker.notNull(fragmentResultMode, "fragmentResultMode");
    // Caller MUST NOT hold the semaphore
    Pair<CompiledViewDefinitionWithGraphs, MarketDataPermissionProvider> latestCompilation = null;
    ViewComputationResultModel latestResult = null;
    boolean listenerRequiresDeltas = doesListenerRequireDeltas(resultMode, fragmentResultMode);
    lock();
    try {
      if (_listeners.put(listener, listenerRequiresDeltas) == null) {
        if (listenerRequiresDeltas) {
          _mustCalculateDeltas.set(true);
        }
        
        // keep track of number of internal listeners
        if (listener instanceof InternalViewResultListener) { 
          _internalListenerCount++;
        }
        // exclude internal listeners from test
        if ((_listeners.size() - _internalListenerCount) == 1) {
          try {
            startComputationJobIfRequired();
          } catch (final Exception e) {
            // Roll-back
            _listeners.remove(listener);
            s_logger.error("Failed to start computation job while adding listener for view process {}", this);
            throw new OpenGammaRuntimeException("Failed to start computation job while adding listener for view process " + toString(), e);
          }
        }
        // Push any initial state to listener
        latestCompilation = _latestCompiledViewDefinition.get();
        if (latestCompilation != null) {
          latestResult = _latestResult.get();
        }
      }
    } finally {
      unlock();
    }
    // REVIEW 2013-04-01 Andrew -- The listener is in the set, but has not received its compilation message yet; it's possible for a calc thread
    // to post its first result before the compilation notification has happened.
    if (latestCompilation != null) {
      // [PLAT-1158] The initial notification is performed outside of holding the lock which avoids the deadlock problem, but we'll still
      // block for completion which was the thing PLAT-1158 was trying to avoid. This is because the contracts for the order in which
      // notifications can be received is unclear and I don't want to risk introducing a change at this moment in time.
      try {
        final CompiledViewDefinitionWithGraphs compiledViewDefinition = latestCompilation.getFirst();
        final MarketDataPermissionProvider permissionProvider = latestCompilation.getSecond();
        final Set<ValueSpecification> marketData = compiledViewDefinition.getMarketDataRequirements();
        final Set<ValueSpecification> deniedRequirements =
            permissionProvider.checkMarketDataPermissions(listener.getUser(), marketData);
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
    return new ViewPermissionContext(
        getProcessContext().getViewPermissionProvider(),
        getProcessContext().getViewPortfolioPermissionProvider());
  }

  private static boolean doesListenerRequireDeltas(ViewResultMode resultMode, ViewResultMode fragmentResultMode) {
    boolean requiresDeltas = false;
    switch(resultMode) {
      case BOTH:
      case DELTA_ONLY:
      case FULL_THEN_DELTA:
        requiresDeltas = true;
    }
    switch(fragmentResultMode) {
      case BOTH:
      case DELTA_ONLY:
      case FULL_THEN_DELTA:
        requiresDeltas = true;
    }
    return requiresDeltas;
  }

  /**
   * Removes a listener from the view process. Removal of the last listener generating execution demand will cause the process to stop.
   * We allow instances extending InternalViewResultListener to be ignored for the purposes of reference counting.  This allows e.g. JMX MBeans
   * to track view events without affecting execution.
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
      if (_listeners.remove(listener) != null) {
        // keep track of internal listeners so they can be excluded from reference count
        if (listener instanceof InternalViewResultListener) {
          _internalListenerCount--;
        }
        // exclude internal listeners from the count
        if ((_listeners.size() - _internalListenerCount) == 0) {
          stopComputationJobIfRequired();
        }
        
        checkIfDeltasRequired();
      }
    } finally {
      unlock();
    }
  }
  
  protected void checkIfDeltasRequired() {
    boolean deltasRequired = false;
    for (Boolean requiresDeltas : _listeners.values()) {
      if (requiresDeltas) {
        deltasRequired = true;
        break;
      }
    }
    if (!deltasRequired) {
      _latestResult.set(null);
    }
    _mustCalculateDeltas.set(deltasRequired);
  }

  public boolean hasExecutionDemand() {
    // Caller MUST NOT hold the semaphore
    lock();
    try {
      return (_listeners.size() - _internalListenerCount) > 0;
    } finally {
      unlock();
    }
  }

  public ViewExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  private void startComputationJobImpl() {
    // Caller MUST hold the semaphore
    if (_viewDefinitionChangeListener != null) {
      getProcessContext().getConfigSource().changeManager().addChangeListener(_viewDefinitionChangeListener);
    }
    final ViewDefinition viewDefinition = getLatestViewDefinition();
    boolean rollback = true;
    try {
      final ViewProcessWorker worker = getProcessContext().getViewProcessWorkerFactory().createWorker(this, getExecutionOptions(), viewDefinition);
      setWorker(worker);
      rollback = false;
    } catch (final Exception e) {
      s_logger.error("Failed to start computation job for view process " + toString(), e);
      throw new OpenGammaRuntimeException("Failed to start computation job for view process " + toString(), e);
    } finally {
      if (rollback && (_viewDefinitionChangeListener != null)) {
        getProcessContext().getConfigSource().changeManager().removeChangeListener(_viewDefinitionChangeListener);
      }
    }
  }

  /**
   * Starts the background job responsible for running computation cycles for this view process.
   */
  private void startComputationJobIfRequired() {
    // Caller MUST hold the semaphore
    s_logger.info("Starting computation on view process {}...", this);
    switch (getState()) {
      case STOPPED:
        // Normal state of play. Continue as normal.
        break;
      case RUNNING:
        return;
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
  private void stopComputationJobIfRequired() {
    // Caller MUST hold the semaphore
    if (getLatestViewDefinition().isPersistent()) {
      return;
    }
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
    final ViewProcessWorker worker = getWorker();
    if (worker == null) {
      return;
    }
    if (_viewDefinitionChangeListener != null) {
      getProcessContext().getConfigSource().changeManager().removeChangeListener(_viewDefinitionChangeListener);
    }
    worker.terminate();
    // Let go of the worker and allow it to die on its own. A computation cycle might be taking place, but it will
    // not update the view process with its result because it has been terminated. As far as the view process is
    // concerned, live computation has now stopped, and it may be started again immediately in a new thread.
    // There is no need to slow things down by attempting to join the job.
    setWorker(null);
  }

}
