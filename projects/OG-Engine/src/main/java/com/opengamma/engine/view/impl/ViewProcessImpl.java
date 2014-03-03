/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.threeten.bp.Instant;
import org.threeten.bp.temporal.ChronoUnit;

import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.engine.management.InternalViewResultListener;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
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
import com.opengamma.engine.view.worker.ViewExecutionDataProvider;
import com.opengamma.engine.view.worker.ViewProcessWorker;
import com.opengamma.engine.view.worker.ViewProcessWorkerContext;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

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
   * Interval (in seconds) at which to check permissions for market data. Permissions are checked on view compilation. Then, when each cycle is run, a check is made to see if a further permission
   * check should be undertaken. A zero value indicates that no additional permission checks should be done.
   */
  private final int _permissionCheckInterval;

  /**
   * The time that a market data permission check was last done (on the market data server).
   */
  private final AtomicReference<Instant> _lastPermissionCheck = new AtomicReference<>();

  /**
   * Indicates whether a user is entitled to the market data. This is maintained across cycles and is updated each time the entitlement check is made on the market data server.
   */
  private Map<UserPrincipal, Boolean> _userPermissions = new ConcurrentHashMap<>();

  /**
   * Manages access to critical regions of the process with respect to its observable behavior. Note that the use of {@link Semaphore} rather than, for example, {@link ReentrantLock} allows one thread
   * to acquire the lock and another thread to release it as part of the same sequence of operations. This could be important for {@link #suspend()} and {@link #resume()}.
   */
  private final Semaphore _processLock = new Semaphore(1);

  /**
   * Manages access to critical regions of the process with respect to its internal data structures.
   */
  private final Lock _internalLock = new ReentrantLock();

  /**
   * Key is the listener to which events will be dispatched. Value is true iff that listener requires delta calculations to be performed. When there are no listeners remaining that require delta
   * calculations, they will stop being computed to save CPU and heap.
   */
  private final Map<ViewResultListener, Boolean> _listeners = new HashMap<>();
  private volatile int _internalListenerCount; // only safe if used within lock

  private volatile ViewDefinition _currentViewDefinition;

  private volatile ViewProcessState _state = ViewProcessState.STOPPED;

  private volatile ViewProcessWorker _worker;

  private final AtomicReference<Pair<CompiledViewDefinitionWithGraphs, MarketDataPermissionProvider>> _latestCompiledViewDefinition = new AtomicReference<>();

  private final AtomicReference<ViewComputationResultModel> _latestResult = new AtomicReference<>();

  private final AtomicBoolean _mustCalculateDeltas = new AtomicBoolean(false);

  private final ChangeListener _viewDefinitionChangeListener;

  // BEGIN TEMPORARY -- See ViewProcessorImpl

  private volatile Object _description;

  /**
   * Indicates if this view process should be run persistently. If true, then even if all clients detach, the process will be kept running.
   */
  private final boolean _isPersistentViewProcess;

  public Object getDescriptionKey() {
    return _description;
  }

  public void setDescriptionKey(final Object description) {
    _description = description;
  }

  // END TEMPORARY CODE

  /**
   * Constructs an instance. Note that if runPersistently is true, the view process will start calculating immediately rather than waiting for a listener to attach. It will continue running regardless
   * of the number of attached listeners.
   * 
   * @param viewDefinitionId the identifier of the view definition, not null. If this is versioned the process will be locked to the specific instance. If this is an object identifier at "latest" then
   *          changes to the view definition will be watched for and the process updated when the view definition changes.
   * @param executionOptions the view execution options, not null
   * @param viewProcessContext the process context, not null
   * @param viewProcessor the parent view processor, not null
   * @param permissionCheckInterval the interval (in seconds) at which to check permissions for market data
   * @param runPersistently if true, then the process will start running and continue running, regardless of the number of attached listeners @throws DataNotFoundException if the view definition
   *          identifier is invalid
   */
  public ViewProcessImpl(final UniqueId viewDefinitionId, final ViewExecutionOptions executionOptions, final ViewProcessContext viewProcessContext, final ViewProcessorImpl viewProcessor,
      int permissionCheckInterval, boolean runPersistently) {
    _viewDefinitionId = ArgumentChecker.notNull(viewDefinitionId, "viewDefinitionId");
    _executionOptions = ArgumentChecker.notNull(executionOptions, "executionOptions");
    _viewProcessContext = ArgumentChecker.notNull(viewProcessContext, "viewProcessContext");
    _viewProcessor = ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    _permissionCheckInterval = permissionCheckInterval;
    _isPersistentViewProcess = runPersistently;

    if (_viewDefinitionId.isVersioned()) {
      _viewDefinitionChangeListener = null;
    } else {
      final ObjectId viewDefinitionObject = viewDefinitionId.getObjectId();
      ViewDefinition viewDefinition = getProcessContext().getConfigSource().getConfig(ViewDefinition.class, getDefinitionId());
      final Set<ObjectId> scenarioIds = Sets.newHashSet(viewDefinitionObject);
      for (ViewCalculationConfiguration calcConfig : viewDefinition.getAllCalculationConfigurations()) {
        UniqueId scenarioId = calcConfig.getScenarioId();
        UniqueId parametersId = calcConfig.getScenarioParametersId();
        if (scenarioId != null) {
          scenarioIds.add(scenarioId.getObjectId());
        }
        if (parametersId != null) {
          scenarioIds.add(parametersId.getObjectId());
        }
      }
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
          } else if (scenarioIds.contains(event.getObjectId())) {
            viewDefinitionChanged();
            forceGraphRebuild();
          }
        }
      };
    }
    _currentViewDefinition = getProcessContext().getConfigSource().getConfig(ViewDefinition.class, getDefinitionId());
    // Start up immediately if persistent, otherwise we'll wait for
    // the first listener to be attached
    if (runPersistently) {
      startComputationJobIfRequired();
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
    return _currentViewDefinition;
  }

  /**
   * Forces the dependency graph to be rebuilt. Invoked when a market data provider becomes available and failed subscriptions need to be retried.
   * 
   * @deprecated There should be a better way to do this in the market data layer but PLAT-3908 is a problem. This method will be removed once it's fixed
   */
  @Deprecated
  public void forceGraphRebuild() {
    ViewProcessWorker worker = getWorker();
    if (worker != null) {
      worker.forceGraphRebuild();
    }
  }

  private void viewDefinitionChanged() {
    final ViewDefinition viewDefinition;
    final UniqueId viewDefinitionId = getDefinitionId();
    try {
      viewDefinition = getProcessContext().getConfigSource().getConfig(ViewDefinition.class, viewDefinitionId);
    } catch (DataNotFoundException e) {
      s_logger.error("Shutting down view process after failure to retrieve view definition {}", viewDefinitionId);
      shutdown();
      return;
    }
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
    try {
      _processLock.acquire();
      try {
        _internalLock.lock();
        try {
          isInterrupting = (getState() == ViewProcessState.RUNNING);
          setState(ViewProcessState.TERMINATED);
          listeners = getListenerArray();
          _listeners.clear();
          terminateComputationJob();
        } finally {
          _internalLock.unlock();
        }
      } finally {
        _processLock.release();
      }
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
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
    try {
      _processLock.acquire();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
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
        _processLock.release();
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
    _processLock.release();
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
    final ViewResultListener[] listeners;
    final MarketDataPermissionProvider permissionProvider = dataProvider.getPermissionProvider();
    _internalLock.lock();
    try {
      _latestCompiledViewDefinition.set(Pairs.of(compiledViewDefinition, permissionProvider));
      listeners = getListenerArray();
    } finally {
      _internalLock.unlock();
    }
    final Set<ValueSpecification> marketData = compiledViewDefinition.getMarketDataRequirements();
    // [PLAT-1158] The notifications are performed outside of holding the lock which avoids the deadlock problem, but we'll still block
    // for completion which was the thing PLAT-1158 was trying to avoid. This is because the contracts for the order in which
    // notifications can be received is unclear and I don't want to risk introducing a change at this moment in time.
    for (final ViewResultListener listener : listeners) {
      try {
        final UserPrincipal listenerUser = listener.getUser();
        // todo - response to a failed permission check is handled by the client rather than here. Is there a reason for this (cf cycleCompleted method)
        final boolean hasMarketDataPermissions = userIsPermitted(true, listenerUser, permissionProvider, marketData);
        listener.viewDefinitionCompiled(compiledViewDefinition, hasMarketDataPermissions);
      } catch (final Exception e) {
        logListenerError(listener, e);
      }
    }
    _lastPermissionCheck.set(Instant.now());
    getExecutionLogModeSource().viewDefinitionCompiled(compiledViewDefinition);
  }

  @Override
  public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
    s_logger.error("View definition compilation failed for " + valuationTime + ": ", exception);
    final ViewResultListener[] listeners;
    _internalLock.lock();
    try {
      listeners = getListenerArray();
    } finally {
      _internalLock.unlock();
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
    s_logger.debug("View cycle {} completed on view process {}", cycle.getUniqueId(), getUniqueId());
    final ViewComputationResultModel result;
    ViewDeltaResultModel deltaResult = null;
    final ViewResultListener[] listeners;
    Pair<CompiledViewDefinitionWithGraphs, MarketDataPermissionProvider> latest;
    _internalLock.lock();
    try {
      result = cycle.getResultModel();
      // We swap these first so that in the callback the process is consistent.
      final ViewComputationResultModel previousResult = _latestResult.getAndSet(result);
      if (_mustCalculateDeltas.get()) {
        // [PLAT-1158] Is the cost of computing the delta going to be high; should we offload that to a slave thread before dispatching to the listeners?
        deltaResult = ViewDeltaResultCalculator.computeDeltaModel(cycle.getCompiledViewDefinition().getViewDefinition(), previousResult, result);
      }
      listeners = getListenerArray();
      latest = _latestCompiledViewDefinition.get();
    } finally {
      _internalLock.unlock();
    }
    // [PLAT-1158] The notifications are performed outside of holding the lock which avoids the deadlock problem, but we'll still block
    // for completion which was the thing PLAT-1158 was trying to avoid. This is because the contracts for the order in which
    // notifications can be received is unclear and I don't want to risk introducing a change at this moment in time.
    boolean isPermissionCheckDue = isPermissionCheckDue();
    for (final ViewResultListener listener : listeners) {
      try {
        UserPrincipal user = listener.getUser();
        final MarketDataPermissionProvider permissionProvider = latest.getValue();
        final Set<ValueSpecification> marketDataRequirements = latest.getKey().getMarketDataRequirements();
        if (userIsPermitted(isPermissionCheckDue, user, permissionProvider, marketDataRequirements)) {
          listener.cycleCompleted(result, deltaResult);
        } else {
          listener.cycleExecutionFailed(cycle.getExecutionOptions(), new Exception("User: " + user + " does not have permission for data in this view"));
        }
      } catch (final Exception e) {
        logListenerError(listener, e);
      }
    }
    if (isPermissionCheckDue) {
      _lastPermissionCheck.set(Instant.now());
    }
  }

  private boolean userIsPermitted(boolean isPermissionCheckDue, UserPrincipal user, MarketDataPermissionProvider permissionProvider, Set<ValueSpecification> marketDataRequirements) {

    if (user == null) {
      // No permissions checking if we're not logging in
      return true;
    } else if (isPermissionCheckDue || !_userPermissions.containsKey(user)) {
      s_logger.info("Performing permissions check for market data");
      final boolean allowed = permissionProvider.checkMarketDataPermissions(user, marketDataRequirements).isEmpty();
      _userPermissions.put(user, allowed);
      return allowed;
    } else {
      return _userPermissions.get(user);
    }
  }

  private boolean isPermissionCheckDue() {
    return _permissionCheckInterval > 0 && Instant.now().isAfter(_lastPermissionCheck.get().plus(_permissionCheckInterval, ChronoUnit.SECONDS));
  }

  @Override
  public void cycleStarted(final ViewCycleMetadata cycleInfo) {
    s_logger.debug("View cycle {} initiated on view process {}", cycleInfo, getUniqueId());
    final ViewResultListener[] listeners;
    _internalLock.lock();
    try {
      listeners = getListenerArray();
    } finally {
      _internalLock.unlock();
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
    s_logger.debug("Result fragment from cycle {} received on view process {}", fullFragment.getViewCycleId(), getUniqueId());
    final ViewDeltaResultModel deltaFragment;
    final ViewResultListener[] listeners;
    _internalLock.lock();
    try {
      // [PLAT-1158] Is the cost of computing the delta going to be high; should we offload that to a slave thread before dispatching to the listeners?
      final ViewComputationResultModel previousResult = _latestResult.get();
      deltaFragment = ViewDeltaResultCalculator.computeDeltaModel(viewDefinition, previousResult, fullFragment);
      listeners = getListenerArray();
    } finally {
      _internalLock.unlock();
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
    s_logger.error("Cycle execution failed for " + executionOptions + ": ", exception);
    final ViewResultListener[] listeners;
    _internalLock.lock();
    try {
      listeners = getListenerArray();
    } finally {
      _internalLock.unlock();
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
    s_logger.debug("Computation job completed on view {}. No further cycles to run.", this);
    final ViewResultListener[] listeners;
    _internalLock.lock();
    try {
      setState(ViewProcessState.FINISHED);
      listeners = getListenerArray();
    } finally {
      _internalLock.unlock();
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
   * @param listener the listener, not null
   * @param resultMode the result mode for the listener, not null
   * @param fragmentResultMode the fragment result mode for the listener, not null
   * @return the permission context for the process, not null
   */
  public ViewPermissionContext attachListener(final ViewResultListener listener, final ViewResultMode resultMode, final ViewResultMode fragmentResultMode) {
    ArgumentChecker.notNull(listener, "listener");
    ArgumentChecker.notNull(resultMode, "resultMode");
    ArgumentChecker.notNull(fragmentResultMode, "fragmentResultMode");
    // Caller MUST NOT hold the semaphore
    Pair<CompiledViewDefinitionWithGraphs, MarketDataPermissionProvider> latestCompilation = null;
    ViewComputationResultModel latestResult = null;
    boolean listenerRequiresDeltas = doesListenerRequireDeltas(resultMode, fragmentResultMode);
    try {
      _processLock.acquire();
      try {
        _internalLock.lock();
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
          _internalLock.unlock();
        }
      } finally {
        _processLock.release();
      }
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
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
        final UserPrincipal user = listener.getUser();
        final boolean hasMarketDataPermissions = userIsPermitted(true, user, permissionProvider, marketData);
        listener.viewDefinitionCompiled(compiledViewDefinition, hasMarketDataPermissions);

        if (latestResult != null) {
          if (hasMarketDataPermissions) {
            listener.cycleCompleted(latestResult, null);
          } else {
            listener.cycleExecutionFailed(latestResult.getViewCycleExecutionOptions(), new Exception("User: " + user + " does not have permission for data in this view"));
          }
        }
      } catch (final Exception e) {
        s_logger.error("Failed to push initial state to listener during attachment");
        logListenerError(listener, e);
      }
    }
    return new ViewPermissionContext(getProcessContext().getViewPermissionProvider(), getProcessContext().getViewPortfolioPermissionProvider());
  }

  private static boolean doesListenerRequireDeltas(ViewResultMode resultMode, ViewResultMode fragmentResultMode) {
    boolean requiresDeltas = false;
    switch (resultMode) {
      case BOTH:
      case DELTA_ONLY:
      case FULL_THEN_DELTA:
        requiresDeltas = true;
    }
    switch (fragmentResultMode) {
      case BOTH:
      case DELTA_ONLY:
      case FULL_THEN_DELTA:
        requiresDeltas = true;
    }
    return requiresDeltas;
  }

  /**
   * Removes a listener from the view process. Removal of the last listener generating execution demand will cause the process to stop. We allow instances extending InternalViewResultListener to be
   * ignored for the purposes of reference counting. This allows e.g. JMX MBeans to track view events without affecting execution.
   * <p>
   * The method operates with set semantics, so duplicate notifications for the same listener have no effect.
   * 
   * @param listener the listener, not null
   */
  public void detachListener(final ViewResultListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    // Caller MUST NOT hold the semaphore
    try {
      _processLock.acquire();
      try {
        _internalLock.lock();
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
          _internalLock.unlock();
        }
      } finally {
        _processLock.release();
      }
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
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
    _mustCalculateDeltas.set(deltasRequired);
  }

  public boolean hasExecutionDemand() {
    _internalLock.lock();
    try {
      return (_listeners.size() - _internalListenerCount) > 0;
    } finally {
      _internalLock.unlock();
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
    if (getLatestViewDefinition().isPersistent() || _isPersistentViewProcess) {
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
