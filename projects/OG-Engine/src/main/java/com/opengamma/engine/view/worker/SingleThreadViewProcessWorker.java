/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.compilation.ViewCompilationServices;
import com.opengamma.engine.view.compilation.ViewDefinitionCompiler;
import com.opengamma.engine.view.cycle.DefaultViewCycleMetadata;
import com.opengamma.engine.view.cycle.SingleComputationCycle;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.cycle.ViewCycleState;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessContext;
import com.opengamma.engine.view.listener.ComputationResultListener;
import com.opengamma.engine.view.worker.cache.PLAT3249;
import com.opengamma.engine.view.worker.cache.ViewExecutionCacheKey;
import com.opengamma.engine.view.worker.trigger.CombinedViewCycleTrigger;
import com.opengamma.engine.view.worker.trigger.FixedTimeTrigger;
import com.opengamma.engine.view.worker.trigger.RecomputationPeriodTrigger;
import com.opengamma.engine.view.worker.trigger.RunAsFastAsPossibleTrigger;
import com.opengamma.engine.view.worker.trigger.SuccessiveDeltaLimitTrigger;
import com.opengamma.engine.view.worker.trigger.ViewCycleEligibility;
import com.opengamma.engine.view.worker.trigger.ViewCycleTrigger;
import com.opengamma.engine.view.worker.trigger.ViewCycleTriggerResult;
import com.opengamma.engine.view.worker.trigger.ViewCycleType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.Pair;

/**
 * The job which schedules and executes computation cycles for a view process. See {@link SingleThreadViewProcessWorkerFactory} for a more detailed description.
 */
public class SingleThreadViewProcessWorker implements MarketDataListener, ViewProcessWorker {

  private static final Logger s_logger = LoggerFactory.getLogger(SingleThreadViewProcessWorker.class);

  private static final ExecutorService s_executor = Executors.newCachedThreadPool(new NamedThreadPoolFactory("Worker"));

  /**
   * Wrapper that allows a thread to be "borrowed" from an executor service.
   */
  /* package*/static final class BorrowedThread implements Runnable {

    private final String _name;
    private final Runnable _job;
    private final CountDownLatch _join = new CountDownLatch(1);
    private Thread _thread;
    private String _originalName;

    public BorrowedThread(final String name, final Runnable job) {
      _name = name;
      _job = job;
    }

    public synchronized Thread.State getState() {
      if (_thread != null) {
        return _thread.getState();
      } else {
        return (_originalName != null) ? Thread.State.TERMINATED : Thread.State.NEW;
      }
    }

    public void join() throws InterruptedException {
      _join.await();
    }

    public void join(long timeout) throws InterruptedException {
      _join.await(timeout, TimeUnit.MILLISECONDS);
    }

    public synchronized void interrupt() {
      if (_thread != null) {
        _thread.interrupt();
      }
    }

    public synchronized boolean isAlive() {
      return _thread != null;
    }

    // Runnable

    @Override
    public void run() {
      synchronized (this) {
        _thread = Thread.currentThread();
        _originalName = _thread.getName();
      }
      try {
        _thread.setName(_originalName + "-" + _name);
        _job.run();
      } finally {
        _thread.setName(_originalName);
        synchronized (this) {
          _thread = null;
        }
        _join.countDown();
      }
    }

  }

  private static final long NANOS_PER_MILLISECOND = 1000000;
  private static final long MARKET_DATA_TIMEOUT_MILLIS = 10000;

  private final ViewProcessWorkerContext _context;
  private final ViewExecutionOptions _executionOptions;
  private final ViewCycleTrigger _masterCycleTrigger;
  private final FixedTimeTrigger _compilationExpiryCycleTrigger;
  private final boolean _executeCycles;

  private int _cycleCount;
  private EngineResourceReference<SingleComputationCycle> _previousCycleReference;

  /**
   * The current view definition the worker must calculate on.
   */
  private ViewDefinition _viewDefinition;

  /**
   * The most recently compiled form of the view definition. This may have been compiled by this worker, or retrieved from the cache and is being reused.
   */
  private CompiledViewDefinitionWithGraphs _latestCompiledViewDefinition;

  /**
   * The key to use for storing the compiled view definition, or querying it, from the cache shared with other workers. Whenever the market data provider or view definition changes, this must be
   * updated.
   */
  private ViewExecutionCacheKey _executionCacheKey;

  private final Set<ValueSpecification> _marketDataSubscriptions = new HashSet<ValueSpecification>();
  private final Set<ValueSpecification> _pendingSubscriptions = Collections.newSetFromMap(new ConcurrentHashMap<ValueSpecification, Boolean>());

  /**
   * Marker for the state of watched targets.
   */
  private static enum TargetState {
    /**
     * Notification of changes to the target are required, but it must be checked for any changes between when it was last queried and this state was stored. After such a check, the state may be
     * changed to {@link #WAITING}.
     */
    REQUIRED,
    /**
     * Notification of changes to the target are required, none have been received, and it will not be checked unless notified. After a change is received, the state may be changed to {@link #CHANGED}
     * .
     */
    WAITING,
    /**
     * Notification of changes to the target are required, at least one is pending, and it must now be checked. Before the check is made, the state may be changed to {@link #WAITING}.
     */
    CHANGED
  }

  private ConcurrentMap<ObjectId, TargetState> _changedTargets;
  private ChangeListener _targetResolverChangeListener;

  private volatile boolean _wakeOnCycleRequest;
  private volatile boolean _cycleRequested;
  private volatile boolean _forceTriggerCycle;

  /**
   * An updated view definition pushed in by the execution coordinator. When the next cycle runs, this should be used instead of the previous one.
   */
  private final AtomicReference<ViewDefinition> _newViewDefinition = new AtomicReference<ViewDefinition>();

  private volatile Future<CompiledViewDefinitionWithGraphsImpl> _compilationTask;

  /**
   * Total time the job has spent "working". This does not include time spent waiting for a trigger. It is a real time spent on all I/O involved in a cycle (e.g. database accesses), graph compilation,
   * market data subscription, graph execution, result dispatch, etc.
   */
  private double _totalTimeNanos;

  /**
   * The market data provider(s) for the current cycles.
   */
  private SnapshottingViewExecutionDataProvider _marketDataProvider;

  /**
   * Flag indicating the market data provider has changed and any nodes sourcing market data into the dependency graph may now be invalid.
   */
  private boolean _marketDataProviderDirty;

  /**
   * The terminatable job wrapper.
   */
  private final TerminatableJob _job;

  /**
   * The thread running this job.
   */
  private final BorrowedThread _thread;

  public SingleThreadViewProcessWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    _context = context;
    _executionOptions = executionOptions;
    _cycleRequested = !executionOptions.getFlags().contains(ViewExecutionFlags.WAIT_FOR_INITIAL_TRIGGER);
    _compilationExpiryCycleTrigger = new FixedTimeTrigger();
    _masterCycleTrigger = createViewCycleTrigger(executionOptions);
    _executeCycles = !getExecutionOptions().getFlags().contains(ViewExecutionFlags.COMPILE_ONLY);
    _viewDefinition = viewDefinition;
    _job = new Job();
    _thread = new BorrowedThread(context.toString(), _job);
    s_executor.submit(_thread);
  }

  private ViewCycleTrigger createViewCycleTrigger(final ViewExecutionOptions executionOptions) {
    final CombinedViewCycleTrigger trigger = new CombinedViewCycleTrigger();
    trigger.addTrigger(_compilationExpiryCycleTrigger);
    if (executionOptions.getFlags().contains(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE)) {
      trigger.addTrigger(new RunAsFastAsPossibleTrigger());
    }
    if (executionOptions.getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_TIME_ELAPSED)) {
      trigger.addTrigger(new RecomputationPeriodTrigger(new Supplier<ViewDefinition>() {
        @Override
        public ViewDefinition get() {
          return getViewDefinition();
        }
      }));
    }
    if (executionOptions.getMaxSuccessiveDeltaCycles() != null) {
      trigger.addTrigger(new SuccessiveDeltaLimitTrigger(executionOptions.getMaxSuccessiveDeltaCycles()));
    }
    return trigger;
  }

  private ViewProcessWorkerContext getWorkerContext() {
    return _context;
  }

  private ViewExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  private ViewProcessContext getProcessContext() {
    return getWorkerContext().getProcessContext();
  }

  private ViewCycleTrigger getMasterCycleTrigger() {
    return _masterCycleTrigger;
  }

  public FixedTimeTrigger getCompilationExpiryCycleTrigger() {
    return _compilationExpiryCycleTrigger;
  }

  protected BorrowedThread getThread() {
    return _thread;
  }

  protected TerminatableJob getJob() {
    return _job;
  }

  private final class Job extends TerminatableJob {

    /**
     * Determines whether to run, and runs if required, a single computation cycle using the following rules:
     * <ul>
     * <li>A computation cycle can only be triggered if the relevant minimum computation period has passed since the start of the previous cycle.
     * <li>A computation cycle will be forced if the relevant maximum computation period has passed since the start of the previous cycle.
     * <li>A full computation is preferred over a delta computation if both are possible.
     * <li>Performing a full computation also updates the times to the next delta computation; i.e. a full computation is considered to be as good as a delta.
     * </ul>
     */
    @Override
    protected void runOneCycle() {
      // Exception handling is important here to ensure that computation jobs do not just die quietly while consumers are
      // potentially blocked, waiting for results.

      ViewCycleType cycleType;
      try {
        cycleType = waitForNextCycle();
      } catch (final InterruptedException e) {
        return;
      }

      ViewCycleExecutionOptions executionOptions = null;
      try {
        if (!getExecutionOptions().getExecutionSequence().isEmpty()) {
          executionOptions = getExecutionOptions().getExecutionSequence().getNext(getExecutionOptions().getDefaultExecutionOptions());
          s_logger.debug("Next cycle execution options: {}", executionOptions);
        }
        if (executionOptions == null) {
          s_logger.info("No more view cycle execution options");
          jobCompleted();
          return;
        }
      } catch (final Exception e) {
        s_logger.error("Error obtaining next view cycle execution options from sequence for " + getWorkerContext(), e);
        return;
      }

      if (executionOptions.getMarketDataSpecifications().isEmpty()) {
        s_logger.error("No market data specifications for cycle");
        cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException("No market data specifications for cycle"));
        return;
      }

      MarketDataSnapshot marketDataSnapshot;
      try {
        SnapshottingViewExecutionDataProvider marketDataProvider = getMarketDataProvider();
        if (marketDataProvider == null ||
            !marketDataProvider.getSpecifications().equals(executionOptions.getMarketDataSpecifications())) {
          if (marketDataProvider != null) {
            s_logger.info("Replacing market data provider between cycles");
          }
          replaceMarketDataProvider(executionOptions.getMarketDataSpecifications());
          marketDataProvider = getMarketDataProvider();
        }
        // Obtain the snapshot in case it is needed, but don't explicitly initialise it until the data is required
        marketDataSnapshot = marketDataProvider.snapshot();
      } catch (final Exception e) {
        s_logger.error("Error with market data provider", e);
        cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException("Error with market data provider", e));
        return;
      }

      Instant compilationValuationTime;
      try {
        if (executionOptions.getValuationTime() != null) {
          compilationValuationTime = executionOptions.getValuationTime();
        } else {
          // Neither the cycle-specific options nor the defaults have overridden the valuation time so use the time
          // associated with the market data snapshot. To avoid initialising the snapshot perhaps before the required
          // inputs are known or even subscribed to, only ask for an indication at the moment.
          compilationValuationTime = marketDataSnapshot.getSnapshotTimeIndication();
          if (compilationValuationTime == null) {
            throw new OpenGammaRuntimeException("Market data snapshot " + marketDataSnapshot + " produced a null indication of snapshot time");
          }
        }
      } catch (final Exception e) {
        s_logger.error("Error obtaining compilation valuation time", e);
        cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException("Error obtaining compilation valuation time", e));
        return;
      }

      final VersionCorrection versionCorrection = getResolverVersionCorrection(executionOptions);
      final CompiledViewDefinitionWithGraphs compiledViewDefinition;
      try {
        // Don't query the cache so that the process gets a "compiled" message even if a cached compilation is used
        final CompiledViewDefinitionWithGraphs previous = _latestCompiledViewDefinition;
        compiledViewDefinition = getCompiledViewDefinition(compilationValuationTime, versionCorrection);
        if (compiledViewDefinition == null) {
          s_logger.warn("Job terminated during view compilation");
          return;
        }
        if (previous != compiledViewDefinition) {
          if (_changedTargets != null) {
            // We'll try to register for changes that will wake us up for a cycle if market data is not ticking
            if (previous != null) {
              final Set<UniqueId> subscribedIds = new HashSet<UniqueId>(previous.getResolvedIdentifiers().values());
              for (UniqueId uid : compiledViewDefinition.getResolvedIdentifiers().values()) {
                if (!subscribedIds.contains(uid)) {
                  _changedTargets.putIfAbsent(uid.getObjectId(), TargetState.REQUIRED);
                }
              }
            } else {
              for (UniqueId uid : compiledViewDefinition.getResolvedIdentifiers().values()) {
                _changedTargets.putIfAbsent(uid.getObjectId(), TargetState.REQUIRED);
              }
            }
          }
          viewDefinitionCompiled(executionOptions, compiledViewDefinition);
        }
      } catch (final Exception e) {
        final String message = MessageFormat.format("Error obtaining compiled view definition {0} for time {1} at version-correction {2}", getViewDefinition().getUniqueId(),
            compilationValuationTime, versionCorrection);
        s_logger.error(message);
        cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException(message, e));
        return;
      }

      setMarketDataSubscriptions(compiledViewDefinition.getMarketDataRequirements());
      try {
        if (getExecutionOptions().getFlags().contains(ViewExecutionFlags.AWAIT_MARKET_DATA)) {
          marketDataSnapshot.init(compiledViewDefinition.getMarketDataRequirements(), MARKET_DATA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } else {
          marketDataSnapshot.init();
        }
        if (executionOptions.getValuationTime() == null) {
          executionOptions = ViewCycleExecutionOptions.builder().setValuationTime(marketDataSnapshot.getSnapshotTime()).setMarketDataSpecifications(executionOptions.getMarketDataSpecifications())
              .create();
        }
      } catch (final Exception e) {
        s_logger.error("Error initializing snapshot {}", marketDataSnapshot);
        cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException("Error initializing snapshot" + marketDataSnapshot, e));
      }

      EngineResourceReference<SingleComputationCycle> cycleReference;
      try {
        cycleReference = createCycle(executionOptions, compiledViewDefinition, versionCorrection);
      } catch (final Exception e) {
        s_logger.error("Error creating next view cycle for " + getWorkerContext(), e);
        return;
      }

      if (_executeCycles) {
        try {
          final SingleComputationCycle singleComputationCycle = cycleReference.get();
          final HashMap<String, Collection<ComputationTargetSpecification>> configToComputationTargets = new HashMap<String, Collection<ComputationTargetSpecification>>();
          final HashMap<String, Map<ValueSpecification, Set<ValueRequirement>>> configToTerminalOutputs = new HashMap<String, Map<ValueSpecification, Set<ValueRequirement>>>();
          for (DependencyGraphExplorer graphExp : compiledViewDefinition.getDependencyGraphExplorers()) {
            final DependencyGraph graph = graphExp.getWholeGraph();
            configToComputationTargets.put(graph.getCalculationConfigurationName(), graph.getAllComputationTargets());
            configToTerminalOutputs.put(graph.getCalculationConfigurationName(), graph.getTerminalOutputs());
          }
          cycleStarted(new DefaultViewCycleMetadata(
              cycleReference.get().getUniqueId(),
              marketDataSnapshot.getUniqueId(),
              compiledViewDefinition.getViewDefinition().getUniqueId(),
              versionCorrection,
              executionOptions.getValuationTime(),
              singleComputationCycle.getAllCalculationConfigurationNames(),
              configToComputationTargets,
              configToTerminalOutputs));
          executeViewCycle(cycleType, cycleReference, marketDataSnapshot);
        } catch (final InterruptedException e) {
          // Execution interrupted - don't propagate as failure
          s_logger.info("View cycle execution interrupted for {}", getWorkerContext());
          cycleReference.release();
          return;
        } catch (final Exception e) {
          // Execution failed
          s_logger.error("View cycle execution failed for " + getWorkerContext(), e);
          cycleReference.release();
          cycleExecutionFailed(executionOptions, e);
          return;
        }
      }

      // Don't push the results through if we've been terminated, since another computation job could be running already
      // and the fact that we've been terminated means the view is no longer interested in the result. Just die quietly.
      if (isTerminated()) {
        cycleReference.release();
        return;
      }

      if (_executeCycles) {
        cycleCompleted(cycleReference.get());
      }

      if (getExecutionOptions().getExecutionSequence().isEmpty()) {
        jobCompleted();
      }

      if (_executeCycles) {
        if (_previousCycleReference != null) {
          _previousCycleReference.release();
        }
        _previousCycleReference = cycleReference;
      }

    }

    @Override
    protected void postRunCycle() {
      if (_previousCycleReference != null) {
        _previousCycleReference.release();
      }
      unsubscribeFromTargetResolverChanges();
      removeMarketDataProvider();
      cacheCompiledViewDefinition(null);
    }

    @Override
    public void terminate() {
      super.terminate();
      final Future<CompiledViewDefinitionWithGraphsImpl> task = _compilationTask;
      if (task != null) {
        task.cancel(true);
      }
    }

  }

  private void cycleCompleted(final ViewCycle cycle) {
    try {
      getWorkerContext().cycleCompleted(cycle);
    } catch (final Exception e) {
      s_logger.error("Error notifying " + getWorkerContext() + " of view cycle completion", e);
    }
  }

  private void cycleStarted(final ViewCycleMetadata cycleMetadata) {
    try {
      getWorkerContext().cycleStarted(cycleMetadata);
    } catch (final Exception e) {
      s_logger.error("Error notifying " + getWorkerContext() + " of view cycle starting", e);
    }
  }

  private void cycleFragmentCompleted(final ViewComputationResultModel result) {
    try {
      getWorkerContext().cycleFragmentCompleted(result, getViewDefinition());
    } catch (final Exception e) {
      s_logger.error("Error notifying " + getWorkerContext() + " of cycle fragment completion", e);
    }
  }

  private void cycleExecutionFailed(final ViewCycleExecutionOptions executionOptions, final Exception exception) {
    try {
      getWorkerContext().cycleExecutionFailed(executionOptions, exception);
    } catch (final Exception vpe) {
      s_logger.error("Error notifying " + getWorkerContext() + " of the cycle execution error", vpe);
    }
  }

  private void viewDefinitionCompiled(final ViewCycleExecutionOptions executionOptions, final CompiledViewDefinitionWithGraphs compiledViewDefinition) {
    try {
      getWorkerContext().viewDefinitionCompiled(getMarketDataProvider(), compiledViewDefinition);
    } catch (final Exception vpe) {
      s_logger.error("Error notifying " + getWorkerContext() + " of view definition compilation");
    }
  }

  private void viewDefinitionCompilationFailed(final Instant compilationTime, final Exception e) {
    try {
      getWorkerContext().viewDefinitionCompilationFailed(compilationTime, e);
    } catch (final Exception vpe) {
      s_logger.error("Error notifying " + getWorkerContext() + " of the view definition compilation failure", vpe);
    }
  }

  private synchronized ViewCycleType waitForNextCycle() throws InterruptedException {
    while (true) {
      final long currentTimeNanos = System.nanoTime();
      final ViewCycleTriggerResult triggerResult = getMasterCycleTrigger().query(currentTimeNanos);

      ViewCycleEligibility cycleEligibility = triggerResult.getCycleEligibility();
      if (_forceTriggerCycle) {
        cycleEligibility = ViewCycleEligibility.FORCE;
        _forceTriggerCycle = false;
      }
      if (cycleEligibility == ViewCycleEligibility.FORCE || (cycleEligibility == ViewCycleEligibility.ELIGIBLE && _cycleRequested)) {
        _cycleRequested = false;
        ViewCycleType cycleType = triggerResult.getCycleType();
        if (_previousCycleReference == null) {
          // Cannot do a delta if we have no previous cycle
          cycleType = ViewCycleType.FULL;
        }
        try {
          getMasterCycleTrigger().cycleTriggered(currentTimeNanos, cycleType);
        } catch (final Exception e) {
          s_logger.error("Error notifying trigger of intention to execute cycle", e);
        }
        s_logger.debug("Eligible for {} cycle", cycleType);
        return cycleType;
      }

      // Going to sleep
      final long wakeUpTime = triggerResult.getNextStateChangeNanos();
      if (_cycleRequested) {
        s_logger.debug("Sleeping until eligible to perform the next computation cycle");
        // No amount of market data can make us eligible for a computation cycle any sooner.
        _wakeOnCycleRequest = false;
      } else {
        s_logger.debug("Sleeping until forced to perform the next computation cycle");
        _wakeOnCycleRequest = cycleEligibility == ViewCycleEligibility.ELIGIBLE;
      }

      long sleepTime = wakeUpTime - currentTimeNanos;
      sleepTime = Math.max(0, sleepTime);
      sleepTime /= NANOS_PER_MILLISECOND;
      sleepTime += 1; // Could have been rounded down during division so ensure only woken after state change
      s_logger.debug("Waiting for {} ms", sleepTime);
      try {
        // This could wait until end of time. In this case, only marketDataChanged() or triggerCycle() will wake it up
        wait(sleepTime);
      } catch (final InterruptedException e) {
        // We support interruption as a signal that we have been terminated. If we're interrupted without having been
        // terminated, we'll just return to this method and go back to sleep.
        Thread.interrupted();
        s_logger.info("Interrupted while delaying. Continuing operation.");
        throw e;
      }
    }
  }

  private void executeViewCycle(final ViewCycleType cycleType,
      final EngineResourceReference<SingleComputationCycle> cycleReference,
      final MarketDataSnapshot marketDataSnapshot) throws Exception {
    SingleComputationCycle deltaCycle;
    if (cycleType == ViewCycleType.FULL) {
      s_logger.info("Performing full computation");
      deltaCycle = null;
    } else {
      s_logger.info("Performing delta computation");
      deltaCycle = _previousCycleReference.get();
      if ((deltaCycle != null) && (deltaCycle.getState() != ViewCycleState.EXECUTED)) {
        // Can only do a delta cycle if the previous was valid
        deltaCycle = null;
      }
    }

    try {
      cycleReference.get().execute(deltaCycle, marketDataSnapshot, s_executor);
    } catch (final InterruptedException e) {
      Thread.interrupted();
      // In reality this means that the job has been terminated, and it will end as soon as we return from this method.
      // In case the thread has been interrupted without terminating the job, we tidy everything up as if the
      // interrupted cycle never happened so that deltas will be calculated from the previous cycle.
      s_logger.info("Interrupted while executing a computation cycle. No results will be output from this cycle.");
      throw e;
    } catch (final Exception e) {
      s_logger.error("Error while executing view cycle", e);
      throw e;
    }

    final long durationNanos = cycleReference.get().getDuration().toNanos();
    _totalTimeNanos += durationNanos;
    _cycleCount += 1;
    s_logger.info("Last latency was {} ms, Average latency is {} ms",
        durationNanos / NANOS_PER_MILLISECOND,
        (_totalTimeNanos / _cycleCount) / NANOS_PER_MILLISECOND);
  }

  private void jobCompleted() {
    s_logger.info("Computation job completed for {}", getWorkerContext());
    try {
      getWorkerContext().workerCompleted();
    } catch (final Exception e) {
      s_logger.error("Error notifying " + getWorkerContext() + " of computation job completion", e);
    }
    getJob().terminate();
  }

  private EngineResourceReference<SingleComputationCycle> createCycle(final ViewCycleExecutionOptions executionOptions,
      final CompiledViewDefinitionWithGraphs compiledViewDefinition, final VersionCorrection versionCorrection) {
    // View definition was compiled based on compilation options, which might have only included an indicative
    // valuation time. A further check ensures that the compiled view definition is still valid.
    if (!CompiledViewDefinitionWithGraphsImpl.isValidFor(compiledViewDefinition, executionOptions.getValuationTime())) {
      throw new OpenGammaRuntimeException("Compiled view definition " + compiledViewDefinition + " not valid for execution options " + executionOptions);
    }
    final UniqueId cycleId = getProcessContext().getCycleIdentifiers().get();

    final ComputationResultListener streamingResultListener = new ComputationResultListener() {
      @Override
      public void resultAvailable(final ViewComputationResultModel result) {
        cycleFragmentCompleted(result);
      }
    };
    final SingleComputationCycle cycle = new SingleComputationCycle(cycleId, streamingResultListener, getProcessContext(), compiledViewDefinition, executionOptions, versionCorrection);
    return getProcessContext().getCycleManager().manage(cycle);
  }

  private void subscribeToTargetResolverChanges() {
    if (_changedTargets == null) {
      assert _targetResolverChangeListener == null;
      final ConcurrentMap<ObjectId, TargetState> changed = new ConcurrentHashMap<ObjectId, TargetState>();
      _changedTargets = changed;
      _targetResolverChangeListener = new ChangeListener() {
        @Override
        public void entityChanged(final ChangeEvent event) {
          final ObjectId oid = event.getObjectId();
          TargetState state = changed.get(oid);
          if (state == null) {
            return;
          }
          if ((state == TargetState.WAITING) || (state == TargetState.REQUIRED)) {
            if (changed.replace(oid, state, TargetState.CHANGED)) {
              // If the state changed to anything else, we either don't need the notification or another change message overtook
              // this one and a cycle has already been triggered.
              s_logger.info("Received change notification for {}", event.getObjectId());
              requestCycle();
              return;
            }
          }
        }
      };
      getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getRawComputationTargetResolver().changeManager().addChangeListener(_targetResolverChangeListener);
    }
  }

  private void unsubscribeFromTargetResolverChanges() {
    if (_changedTargets != null) {
      assert _targetResolverChangeListener != null;
      getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getRawComputationTargetResolver().changeManager().removeChangeListener(_targetResolverChangeListener);
      _targetResolverChangeListener = null;
      _changedTargets = null;
    }
  }

  private VersionCorrection getResolverVersionCorrection(final ViewCycleExecutionOptions viewCycleOptions) {
    VersionCorrection vc = null;
    do {
      vc = viewCycleOptions.getResolverVersionCorrection();
      if (vc != null) {
        break;
      }
      final ViewCycleExecutionOptions options = getExecutionOptions().getDefaultExecutionOptions();
      if (options != null) {
        vc = options.getResolverVersionCorrection();
        if (vc != null) {
          break;
        }
      }
      vc = VersionCorrection.LATEST;
    } while (false);
    // Note: NOW means NOW as the caller has requested LATEST. We should not be using the valuation time.
    if (vc.getCorrectedTo() == null) {
      if (vc.getVersionAsOf() == null) {
        subscribeToTargetResolverChanges();
        return vc.withLatestFixed(Instant.now());
      } else {
        vc = vc.withLatestFixed(Instant.now());
      }
    } else if (vc.getVersionAsOf() == null) {
      vc = vc.withLatestFixed(Instant.now());
    }
    unsubscribeFromTargetResolverChanges();
    return vc;
  }

  private void removePortfolioTerminalOutputs(final Map<String, Pair<DependencyGraph, Set<ValueRequirement>>> previousGraphs, final CompiledViewDefinitionWithGraphs compiledViewDefinition) {
    for (final ViewCalculationConfiguration calcConfig : compiledViewDefinition.getViewDefinition().getAllCalculationConfigurations()) {
      final Set<ValueRequirement> specificRequirements = calcConfig.getSpecificRequirements();
      final Pair<DependencyGraph, Set<ValueRequirement>> previousGraphEntry = previousGraphs.get(calcConfig.getName());
      if (previousGraphEntry == null) {
        continue;
      }
      final DependencyGraph previousGraph = previousGraphEntry.getFirst();
      final Map<ValueSpecification, Set<ValueRequirement>> terminalOutputs = previousGraph.getTerminalOutputs();
      final ValueSpecification[] removeSpecifications = new ValueSpecification[terminalOutputs.size()];
      @SuppressWarnings("unchecked")
      final List<ValueRequirement>[] removeRequirements = new List[terminalOutputs.size()];
      int remove = 0;
      for (final Map.Entry<ValueSpecification, Set<ValueRequirement>> entry : terminalOutputs.entrySet()) {
        List<ValueRequirement> removal = null;
        for (final ValueRequirement requirement : entry.getValue()) {
          if (!specificRequirements.contains(requirement)) {
            if (removal == null) {
              removal = new ArrayList<ValueRequirement>(entry.getValue().size());
            }
            removal.add(requirement);
          }
        }
        if (removal != null) {
          removeSpecifications[remove] = entry.getKey();
          removeRequirements[remove++] = removal;
        }
      }
      for (int i = 0; i < remove; i++) {
        previousGraph.removeTerminalOutputs(removeRequirements[i], removeSpecifications[i]);
      }
    }
  }

  /**
   * Returns the set of unique identifiers that were previously used as targets in the dependency graph for object identifiers (or external identifiers) that now resolve differently.
   * 
   * @param previousResolutions the previous cycle's resolution of identifiers, not null
   * @param versionCorrection the resolver version correction for this cycle, not null
   * @return the invalid identifier set, or null if none are invalid, this is a map from the old unique identifier to the new resolution
   */
  private Map<UniqueId, ComputationTargetSpecification> getInvalidIdentifiers(final Map<ComputationTargetReference, UniqueId> previousResolutions, final VersionCorrection versionCorrection) {
    long t = -System.nanoTime();
    // TODO [PLAT-349] Checking all of these identifiers is costly. Can we fork this out as a "job"? Can we use existing infrastructure? Should the bulk resolver operations use a thread pool?
    final Set<ComputationTargetReference> toCheck;
    if (_changedTargets == null) {
      // Change notifications aren't relevant for historical iteration; must recheck all of the resolutions
      toCheck = previousResolutions.keySet();
    } else {
      // Subscribed to LATEST/LATEST so change manager notifications can filter the set to be checked
      toCheck = Sets.newHashSetWithExpectedSize(previousResolutions.size());
      final Set<ObjectId> allObjectIds = Sets.newHashSetWithExpectedSize(previousResolutions.size());
      for (final Map.Entry<ComputationTargetReference, UniqueId> previousResolution : previousResolutions.entrySet()) {
        final ObjectId oid = previousResolution.getValue().getObjectId();
        if (_changedTargets.replace(oid, TargetState.CHANGED, TargetState.WAITING)) {
          // A change was seen on this target
          s_logger.debug("Change observed on {}", oid);
          toCheck.add(previousResolution.getKey());
        } else if (_changedTargets.putIfAbsent(oid, TargetState.WAITING) == null) {
          // We've not been monitoring this target for changes - better start doing so
          s_logger.debug("Added {} to change observation set", oid);
          toCheck.add(previousResolution.getKey());
        }
        allObjectIds.add(oid);
      }
      _changedTargets.keySet().retainAll(allObjectIds);
      if (toCheck.isEmpty()) {
        s_logger.debug("No resolutions (from {}) to check", previousResolutions.size());
        return null;
      } else {
        s_logger.debug("Checking {} of {} resolutions for changed objects", toCheck.size(), previousResolutions.size());
      }
    }
    final Map<ComputationTargetReference, ComputationTargetSpecification> specifications = getProcessContext().getFunctionCompilationService().getFunctionCompilationContext()
        .getRawComputationTargetResolver().getSpecificationResolver().getTargetSpecifications(toCheck, versionCorrection);
    t += System.nanoTime();
    Map<UniqueId, ComputationTargetSpecification> invalidIdentifiers = null;
    for (final Map.Entry<ComputationTargetReference, UniqueId> target : previousResolutions.entrySet()) {
      final ComputationTargetSpecification resolved = specifications.get(target.getKey());
      if ((resolved != null) && target.getValue().equals(resolved.getUniqueId())) {
        // No change
        s_logger.debug("No change resolving {}", target);
      } else if (toCheck.contains(target.getKey())) {
        // Identifier no longer resolved, or resolved differently
        s_logger.info("New resolution of {} to {}", target, resolved);
        if (invalidIdentifiers == null) {
          invalidIdentifiers = new HashMap<UniqueId, ComputationTargetSpecification>();
        }
        invalidIdentifiers.put(target.getValue(), resolved);
      }
    }
    s_logger.debug("{} resolutions checked in {}ms", toCheck.size(), t / 1e6);
    return invalidIdentifiers;
  }

  private void getInvalidMarketData(final DependencyGraph graph, final InvalidMarketDataDependencyNodeFilter filter) {
    // 32 was chosen fairly arbitrarily. Before doing this 502 node checks was taking 700ms. After this it is taking 180ms. 
    final int jobSize = 32;
    InvalidMarketDataDependencyNodeFilter.VisitBatch visit = filter.visit(jobSize);
    LinkedList<Future<?>> futures = new LinkedList<Future<?>>();
    for (ValueSpecification marketData : graph.getAllRequiredMarketData()) {
      if (visit.isFull()) {
        futures.add(getProcessContext().getFunctionCompilationService().getExecutorService().submit(visit));
        visit = filter.visit(jobSize);
      }
      final DependencyNode node = graph.getNodeProducing(marketData);
      visit.add(marketData, node);
    }
    visit.run();
    Future<?> future = futures.poll();
    while (future != null) {
      try {
        future.get();
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }
      future = futures.poll();
    }
  }

  /**
   * Returns the set of value specifications from Market Data sourcing nodes that are not valid for the new data provider.
   * <p>
   * The cost of applying a filter can be quite high and in the historical simulation case seldom excludes nodes. To optimise this case we consider the market data sourcing nodes first to determine
   * whether the filter should be applied.
   * 
   * @param previousGraphs the previous graphs that have already been part processed, null if no preprocessing has occurred
   * @param compiledViewDefinition the cached compilation containing previous graphs if {@code previousGraphs} is null
   * @param filter the filter to pass details of the nodes to
   * @return the invalid specification set, or null if none are invalid
   */
  private void getInvalidMarketData(final Map<String, Pair<DependencyGraph, Set<ValueRequirement>>> previousGraphs,
      final CompiledViewDefinitionWithGraphs compiledViewDefinition, final InvalidMarketDataDependencyNodeFilter filter) {
    if (previousGraphs != null) {
      for (Pair<DependencyGraph, Set<ValueRequirement>> previousGraph : previousGraphs.values()) {
        getInvalidMarketData(previousGraph.getKey(), filter);
      }
    } else {
      for (DependencyGraphExplorer graphExp : compiledViewDefinition.getDependencyGraphExplorers()) {
        getInvalidMarketData(graphExp.getWholeGraph(), filter);
      }
    }
  }

  /**
   * Mark a set of nodes for inclusion (TRUE) or exclusion (FALSE) based on the filter. A node is included if the filter accepts it and all of its inputs are also marked for inclusion. A node is
   * excluded if the filter rejects it or any of its inputs are rejected. This will operate recursively, processing all nodes to the leaves of the graph.
   * <p>
   * The {@link DependencyGraph#subGraph} operation doesn't work for us as it can leave nodes in the sub-graph that have inputs that aren't in the graph. Invalid nodes identified by the filter need to
   * remove all the graph up to the terminal output root so that we can rebuild it.
   * 
   * @param include the map to build the result into
   * @param nodes the nodes to process
   * @param filter the filter to apply to the nodes
   */
  private static boolean includeNodes(final Map<DependencyNode, Boolean> include, final Collection<DependencyNode> nodes, final DependencyNodeFilter filter) {
    boolean includedAll = true;
    for (final DependencyNode node : nodes) {
      final Boolean match = include.get(node);
      if (match == null) {
        if (filter.accept(node)) {
          if (includeNodes(include, node.getInputNodes(), filter)) {
            include.put(node, Boolean.TRUE);
          } else {
            includedAll = false;
            include.put(node, Boolean.FALSE);
          }
        } else {
          includedAll = false;
          include.put(node, Boolean.FALSE);
        }
      } else {
        if (match == Boolean.FALSE) {
          includedAll = false;
        }
      }
    }
    return includedAll;
  }

  private Map<String, Pair<DependencyGraph, Set<ValueRequirement>>> getPreviousGraphs(Map<String, Pair<DependencyGraph, Set<ValueRequirement>>> previousGraphs,
      final CompiledViewDefinitionWithGraphs compiledViewDefinition) {
    if (previousGraphs == null) {
      final Collection<DependencyGraphExplorer> graphExps = compiledViewDefinition.getDependencyGraphExplorers();
      previousGraphs = Maps.newHashMapWithExpectedSize(graphExps.size());
      for (DependencyGraphExplorer graphExp : graphExps) {
        final DependencyGraph graph = graphExp.getWholeGraph();
        previousGraphs.put(graph.getCalculationConfigurationName(), Pair.<DependencyGraph, Set<ValueRequirement>>of(graph, new HashSet<ValueRequirement>()));
      }
    }
    return previousGraphs;
  }

  /**
   * Maintain the previously used dependency graphs by applying a node filter that identifies invalid nodes that must be recalculated (implying everything dependent on them must also be rebuilt). The
   * first call will extract the previously compiled graphs, subsequent calls will update the structure invalidating more nodes and increasing the number of missing requirements.
   * 
   * @param previousGraphs the previously used graphs as a map from calculation configuration name to the graph and the value requirements that need to be recalculated, not null
   * @param filter the filter to identify invalid nodes, not null
   */
  private void filterPreviousGraphs(final Map<String, Pair<DependencyGraph, Set<ValueRequirement>>> previousGraphs, final DependencyNodeFilter filter) {
    final Iterator<Map.Entry<String, Pair<DependencyGraph, Set<ValueRequirement>>>> itr = previousGraphs.entrySet().iterator();
    while (itr.hasNext()) {
      final Map.Entry<String, Pair<DependencyGraph, Set<ValueRequirement>>> entry = itr.next();
      final DependencyGraph graph = entry.getValue().getFirst();
      if (graph.getSize() == 0) {
        continue;
      }
      final Collection<DependencyNode> nodes = graph.getDependencyNodes();
      final Map<DependencyNode, Boolean> include = Maps.newHashMapWithExpectedSize(nodes.size());
      includeNodes(include, nodes, filter);
      assert nodes.size() == include.size();
      final Map<ValueSpecification, Set<ValueRequirement>> terminalOutputs = graph.getTerminalOutputs();
      final Set<ValueRequirement> missingRequirements = entry.getValue().getSecond();
      final DependencyGraph filtered = graph.subGraph(new DependencyNodeFilter() {
        @Override
        public boolean accept(final DependencyNode node) {
          if (include.get(node) == Boolean.TRUE) {
            return true;
          } else {
            s_logger.debug("Discarding {} from dependency graph for {}", node, entry.getKey());
            for (final ValueSpecification output : node.getOutputValues()) {
              final Set<ValueRequirement> terminal = terminalOutputs.get(output);
              if (terminal != null) {
                missingRequirements.addAll(terminal);
              }
            }
            return false;
          }
        }
      });
      if (filtered.getSize() == 0) {
        s_logger.info("Discarded total dependency graph for {}", entry.getKey());
        itr.remove();
      } else {
        s_logger.info("Removed {} nodes from dependency graph for {}", nodes.size() - filtered.getSize(), entry.getKey());
        entry.setValue(Pair.of(filtered, missingRequirements));
      }
    }
  }

  private CompiledViewDefinitionWithGraphs getCompiledViewDefinition(final Instant valuationTime, final VersionCorrection versionCorrection) {
    final long functionInitId = getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getFunctionInitId();
    updateViewDefinitionIfRequired();
    CompiledViewDefinitionWithGraphs compiledViewDefinition = null;
    final Lock executionCacheLock = getProcessContext().getExecutionCacheLock().get(_executionCacheKey);
    executionCacheLock.lock();
    boolean locked = true;
    try {
      compiledViewDefinition = getCachedCompiledViewDefinition();
      Map<String, Pair<DependencyGraph, Set<ValueRequirement>>> previousGraphs = null;
      ConcurrentMap<ComputationTargetReference, UniqueId> previousResolutions = null;
      boolean portfolioFull = false;
      Set<UniqueId> changedPositions = null;
      boolean marketDataProviderDirty = _marketDataProviderDirty;
      _marketDataProviderDirty = false;
      if (compiledViewDefinition != null) {
        executionCacheLock.unlock();
        locked = false;
        do {
          // The cast below is bad, but only temporary -- the function initialiser id needs to go
          if (functionInitId != ((CompiledViewDefinitionWithGraphsImpl) compiledViewDefinition).getFunctionInitId()) {
            // The function repository has been reinitialized which invalidates any previous graphs
            // TODO: [PLAT-2237, PLAT-1623, PLAT-2240] Get rid of this
            break;
          }
          final Map<ComputationTargetReference, UniqueId> resolvedIdentifiers = compiledViewDefinition.getResolvedIdentifiers();
          // TODO: The check below works well for the historical valuation case, but if the resolver v/c is different for two workers in the 
          // group for an otherwise identical cache key then including it in the caching detail may become necessary to handle those cases.
          if (!versionCorrection.equals(compiledViewDefinition.getResolverVersionCorrection())) {
            final Map<UniqueId, ComputationTargetSpecification> invalidIdentifiers = getInvalidIdentifiers(resolvedIdentifiers, versionCorrection);
            if (invalidIdentifiers != null) {
              previousGraphs = getPreviousGraphs(previousGraphs, compiledViewDefinition);
              if (invalidIdentifiers.containsKey(compiledViewDefinition.getPortfolio().getUniqueId())) {
                // The portfolio resolution is different, invalidate all PORTFOLIO and PORTFOLIO_NODE nodes in the graph
                removePortfolioTerminalOutputs(previousGraphs, compiledViewDefinition);
                filterPreviousGraphs(previousGraphs, new InvalidPortfolioDependencyNodeFilter());
                portfolioFull = true;
              }
              // Invalidate any dependency graph nodes on the invalid targets
              filterPreviousGraphs(previousGraphs, new InvalidTargetDependencyNodeFilter(invalidIdentifiers.keySet()));
              previousResolutions = new ConcurrentHashMap<ComputationTargetReference, UniqueId>(resolvedIdentifiers.size());
              for (final Map.Entry<ComputationTargetReference, UniqueId> resolvedIdentifier : resolvedIdentifiers.entrySet()) {
                if (invalidIdentifiers.containsKey(resolvedIdentifier.getValue())) {
                  if (!portfolioFull && resolvedIdentifier.getKey().getType().isTargetType(ComputationTargetType.POSITION)) {
                    // At least one position has changed, add all portfolio targets
                    ComputationTargetSpecification ctspec = invalidIdentifiers.get(resolvedIdentifier.getValue());
                    if (ctspec != null) {
                      if (changedPositions == null) {
                        changedPositions = new HashSet<UniqueId>();
                      }
                      changedPositions.add(ctspec.getUniqueId());
                    }
                  }
                } else {
                  previousResolutions.put(resolvedIdentifier.getKey(), resolvedIdentifier.getValue());
                }
              }
            }
          }
          if (!CompiledViewDefinitionWithGraphsImpl.isValidFor(compiledViewDefinition, valuationTime)) {
            // Invalidate any dependency graph nodes that use functions that are no longer valid
            previousGraphs = getPreviousGraphs(previousGraphs, compiledViewDefinition);
            filterPreviousGraphs(previousGraphs, new InvalidFunctionDependencyNodeFilter(valuationTime));
          }
          if (marketDataProviderDirty) {
            // Invalidate any market data sourcing nodes that are no longer valid
            final InvalidMarketDataDependencyNodeFilter filter = new InvalidMarketDataDependencyNodeFilter(getProcessContext().getFunctionCompilationService().getFunctionCompilationContext()
                .getRawComputationTargetResolver().atVersionCorrection(versionCorrection), getMarketDataProvider().getAvailabilityProvider());
            getInvalidMarketData(previousGraphs, compiledViewDefinition, filter);
            if (filter.hasInvalidNodes()) {
              previousGraphs = getPreviousGraphs(previousGraphs, compiledViewDefinition);
              filterPreviousGraphs(previousGraphs, filter);
            }
          }
          if (previousGraphs == null) {
            // Existing cached model is valid (an optimization for the common case of similar, increasing valuation times)
            return compiledViewDefinition;
          }
          if (previousResolutions == null) {
            previousResolutions = new ConcurrentHashMap<ComputationTargetReference, UniqueId>(resolvedIdentifiers);
          }
        } while (false);
        executionCacheLock.lock();
        locked = true;
      }
      final MarketDataAvailabilityProvider availabilityProvider = getMarketDataProvider().getAvailabilityProvider();
      final ViewCompilationServices compilationServices = getProcessContext().asCompilationServices(availabilityProvider);
      if (previousGraphs != null) {
        if (portfolioFull) {
          s_logger.info("Performing incremental graph compilation with portfolio resolution");
          _compilationTask = ViewDefinitionCompiler.incrementalCompileTask(getViewDefinition(), compilationServices, valuationTime, versionCorrection, previousGraphs, previousResolutions);
        } else {
          s_logger.info("Performing incremental graph compilation");
          _compilationTask = ViewDefinitionCompiler.incrementalCompileTask(getViewDefinition(), compilationServices, valuationTime, versionCorrection, previousGraphs, previousResolutions,
              changedPositions);
        }
      } else {
        s_logger.info("Performing full graph compilation");
        _compilationTask = ViewDefinitionCompiler.fullCompileTask(getViewDefinition(), compilationServices, valuationTime, versionCorrection);
      }
      try {
        if (!getJob().isTerminated()) {
          compiledViewDefinition = _compilationTask.get();
          cacheCompiledViewDefinition(compiledViewDefinition);
        } else {
          return null;
        }
      } finally {
        _compilationTask = null;
      }
    } catch (final Exception e) {
      final String message = MessageFormat.format("Error compiling view definition {0} for time {1}", getViewDefinition().getUniqueId(), valuationTime);
      viewDefinitionCompilationFailed(valuationTime, new OpenGammaRuntimeException(message, e));
      throw new OpenGammaRuntimeException(message, e);
    } finally {
      if (locked) {
        executionCacheLock.unlock();
      }
    }
    // [PLAT-984]
    // Assume that valuation times are increasing in real-time towards the expiry of the view definition, so that we
    // can predict the time to expiry. If this assumption is wrong then the worst we do is trigger an unnecessary
    // cycle. In the predicted case, we trigger a cycle on expiry so that any new market data subscriptions are made
    // straight away.
    if ((compiledViewDefinition.getValidTo() != null) && getExecutionOptions().getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED)) {
      final Duration durationToExpiry = getMarketDataProvider().getRealTimeDuration(valuationTime, compiledViewDefinition.getValidTo());
      final long expiryNanos = System.nanoTime() + durationToExpiry.toNanos();
      _compilationExpiryCycleTrigger.set(expiryNanos, ViewCycleTriggerResult.forceFull());
      // REVIEW Andrew 2012-11-02 -- If we are ticking live, then this is almost right (System.nanoTime will be close to valuationTime, depending on how
      // long the compilation took). If we are running through historical data then this is quite a meaningless trigger.
    } else {
      _compilationExpiryCycleTrigger.reset();
    }
    return compiledViewDefinition;
  }

  /**
   * Gets the cached compiled view definition which may be re-used in subsequent computation cycles.
   * <p>
   * External visibility for tests.
   * 
   * @return the cached compiled view definition, or null if nothing is currently cached
   */
  public CompiledViewDefinitionWithGraphs getCachedCompiledViewDefinition() {
    if (_latestCompiledViewDefinition == null) {
      _latestCompiledViewDefinition = getProcessContext().getExecutionCache().getCompiledViewDefinitionWithGraphs(_executionCacheKey);
      if (_latestCompiledViewDefinition != null) {
        _latestCompiledViewDefinition = PLAT3249.deepClone(_latestCompiledViewDefinition);
      }
    }
    return _latestCompiledViewDefinition;
  }

  /**
   * Replaces the cached compiled view definition.
   * <p>
   * External visibility for tests.
   * 
   * @param latestCompiledViewDefinition the compiled view definition, may be null
   */
  public void cacheCompiledViewDefinition(final CompiledViewDefinitionWithGraphs latestCompiledViewDefinition) {
    if (latestCompiledViewDefinition != null) {
      getProcessContext().getExecutionCache().setCompiledViewDefinitionWithGraphs(_executionCacheKey, latestCompiledViewDefinition);
    }
    _latestCompiledViewDefinition = latestCompiledViewDefinition;
  }

  /**
   * Gets the view definition currently in use by the computation job.
   * 
   * @return the view definition, not null
   */
  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  private void updateViewDefinitionIfRequired() {
    final ViewDefinition newViewDefinition = _newViewDefinition.getAndSet(null);
    if (newViewDefinition != null) {
      _viewDefinition = newViewDefinition;
      // TODO [PLAT-3215] Might not need to discard the entire compilation at this point
      cacheCompiledViewDefinition(null);
      SnapshottingViewExecutionDataProvider marketDataProvider = getMarketDataProvider();
      _executionCacheKey = ViewExecutionCacheKey.of(newViewDefinition, marketDataProvider.getAvailabilityProvider());
      // A change in view definition might mean a change in market data user which could invalidate the resolutions
      if (marketDataProvider != null) {
        if (!marketDataProvider.getMarketDataUser().equals(newViewDefinition.getMarketDataUser())) {
          replaceMarketDataProvider(marketDataProvider.getSpecifications());
        }
      }
    }
  }

  private void replaceMarketDataProvider(final List<MarketDataSpecification> marketDataSpecs) {
    // [PLAT-3186] Not a huge overhead, but we could check compatability with the new specs and keep the same provider
    removeMarketDataProvider();
    setMarketDataProvider(marketDataSpecs);
  }

  private void removeMarketDataProvider() {
    if (_marketDataProvider == null) {
      return;
    }
    removeMarketDataSubscriptions();
    _marketDataProvider.removeListener(this);
    _marketDataProvider = null;
    _marketDataProviderDirty = true;
    _executionCacheKey = null;
  }

  private void setMarketDataProvider(final List<MarketDataSpecification> marketDataSpecs) {
    try {
      _marketDataProvider = new SnapshottingViewExecutionDataProvider(getViewDefinition().getMarketDataUser(),
          marketDataSpecs, getProcessContext().getMarketDataProviderResolver());
    } catch (final Exception e) {
      s_logger.error("Failed to create data provider", e);
      _marketDataProvider = null;
    }
    if (_marketDataProvider != null) {
      _marketDataProvider.addListener(this);
      _executionCacheKey = ViewExecutionCacheKey.of(getViewDefinition(), _marketDataProvider.getAvailabilityProvider());
    }
    _marketDataProviderDirty = true;
  }

  private SnapshottingViewExecutionDataProvider getMarketDataProvider() {
    return _marketDataProvider;
  }

  private void setMarketDataSubscriptions(final Set<ValueSpecification> requiredSubscriptions) {
    final Set<ValueSpecification> currentSubscriptions = _marketDataSubscriptions;
    final Set<ValueSpecification> unusedMarketData = Sets.difference(currentSubscriptions, requiredSubscriptions);
    if (!unusedMarketData.isEmpty()) {
      s_logger.debug("{} unused market data subscriptions: {}", unusedMarketData.size(), unusedMarketData);
      removeMarketDataSubscriptions(new ArrayList<ValueSpecification>(unusedMarketData));
    }
    final Set<ValueSpecification> newMarketData = Sets.difference(requiredSubscriptions, currentSubscriptions);
    if (!newMarketData.isEmpty()) {
      s_logger.debug("{} new market data requirements: {}", newMarketData.size(), newMarketData);
      addMarketDataSubscriptions(new HashSet<ValueSpecification>(newMarketData));
    }
  }

  //-------------------------------------------------------------------------
  private void addMarketDataSubscriptions(final Set<ValueSpecification> requiredSubscriptions) {
    final OperationTimer timer = new OperationTimer(s_logger, "Adding {} market data subscriptions", requiredSubscriptions.size());
    _pendingSubscriptions.addAll(requiredSubscriptions);
    _marketDataProvider.subscribe(requiredSubscriptions);
    _marketDataSubscriptions.addAll(requiredSubscriptions);
    try {
      synchronized (_pendingSubscriptions) {
        if (!_pendingSubscriptions.isEmpty()) {
          final long finish = System.currentTimeMillis() + MARKET_DATA_TIMEOUT_MILLIS;
          _pendingSubscriptions.wait(MARKET_DATA_TIMEOUT_MILLIS);
          do {
            int remainingCount = _pendingSubscriptions.size();
            if (remainingCount == 0) {
              break;
            }
            final long remainingWait = finish - System.currentTimeMillis();
            if (remainingWait > 0) {
              _pendingSubscriptions.wait(remainingWait);
            } else {
              s_logger.warn("Timed out after {} ms waiting for market data subscriptions to be made. The market data " +
                  "snapshot used in the computation cycle could be incomplete. Still waiting for {} out of {} market data " +
                  "subscriptions",
                  new Object[] {MARKET_DATA_TIMEOUT_MILLIS, remainingCount, _marketDataSubscriptions.size() });
              break;
            }
          } while (true);
        }
      }
    } catch (final InterruptedException ex) {
      s_logger.info("Interrupted while waiting for subscription results.");
    } finally {
      _pendingSubscriptions.clear();
    }
    timer.finished();
  }

  private void removePendingSubscription(final ValueSpecification specification) {
    if (_pendingSubscriptions.remove(specification) && _pendingSubscriptions.isEmpty()) {
      synchronized (_pendingSubscriptions) {
        if (_pendingSubscriptions.isEmpty()) {
          _pendingSubscriptions.notifyAll();
        }
      }
    }
  }

  private void removePendingSubscriptions(final Collection<ValueSpecification> specifications) {
    if (_pendingSubscriptions.removeAll(specifications) && _pendingSubscriptions.isEmpty()) {
      synchronized (_pendingSubscriptions) {
        if (_pendingSubscriptions.isEmpty()) {
          _pendingSubscriptions.notifyAll();
        }
      }
    }
  }

  private void removeMarketDataSubscriptions() {
    removeMarketDataSubscriptions(_marketDataSubscriptions);
  }

  private void removeMarketDataSubscriptions(final Collection<ValueSpecification> unusedSubscriptions) {
    final OperationTimer timer = new OperationTimer(s_logger, "Removing {} market data subscriptions", unusedSubscriptions.size());
    _marketDataProvider.unsubscribe(_marketDataSubscriptions);
    _marketDataSubscriptions.removeAll(unusedSubscriptions);
    timer.finished();
  }

  // MarketDataListener

  @Override
  public void subscriptionsSucceeded(final Collection<ValueSpecification> valueSpecifications) {
    s_logger.debug("Subscription succeeded: {}", valueSpecifications.size());
    removePendingSubscriptions(valueSpecifications);
  }

  @Override
  public void subscriptionFailed(final ValueSpecification valueSpecification, final String msg) {
    s_logger.debug("Market data subscription to {} failed. This market data may be missing from computation cycles.", valueSpecification);
    removePendingSubscription(valueSpecification);
  }

  @Override
  public void subscriptionStopped(final ValueSpecification valueSpecification) {
  }

  @Override
  public void valuesChanged(final Collection<ValueSpecification> valueSpecifications) {
    if (!getExecutionOptions().getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED)) {
      return;
    }
    final CompiledViewDefinitionWithGraphs compiledView = getCachedCompiledViewDefinition();
    if (compiledView == null) {
      return;
    }
    if (CollectionUtils.containsAny(compiledView.getMarketDataRequirements(), valueSpecifications)) {
      requestCycle();
    }
  }

  // ViewComputationJob

  @Override
  public synchronized void triggerCycle() {
    s_logger.debug("Cycle triggered manually");
    _forceTriggerCycle = true;
    notifyAll();
  }

  @Override
  public synchronized void requestCycle() {
    // REVIEW jonathan 2010-10-04 -- this synchronisation is necessary, but it feels very heavyweight for
    // high-frequency market data. See how it goes, but we could take into account the recalc periods and apply a
    // heuristic (e.g. only wake up due to market data if max - min < e, for some e) which tries to see whether it's
    // worth doing all this.

    _cycleRequested = true;
    if (!_wakeOnCycleRequest) {
      return;
    }
    notifyAll();
  }

  @Override
  public void updateViewDefinition(final ViewDefinition viewDefinition) {
    s_logger.debug("Received new view definition {} for next cycle", viewDefinition.getUniqueId());
    _newViewDefinition.getAndSet(viewDefinition);
  }

  @Override
  public void terminate() {
    getJob().terminate();
    s_logger.debug("Interrupting calculation job thread");
    getThread().interrupt();
  }

  @Override
  public void join() throws InterruptedException {
    getThread().join();
  }

  @Override
  public boolean join(final long timeout) throws InterruptedException {
    getThread().join(timeout);
    return !getThread().isAlive();
  }

  @Override
  public boolean isTerminated() {
    return getJob().isTerminated() && !getThread().isAlive();
  }

}
