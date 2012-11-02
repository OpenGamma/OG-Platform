/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.time.Duration;
import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessContext;
import com.opengamma.engine.view.ViewProcessImpl;
import com.opengamma.engine.view.calc.trigger.CombinedViewCycleTrigger;
import com.opengamma.engine.view.calc.trigger.FixedTimeTrigger;
import com.opengamma.engine.view.calc.trigger.RecomputationPeriodTrigger;
import com.opengamma.engine.view.calc.trigger.RunAsFastAsPossibleTrigger;
import com.opengamma.engine.view.calc.trigger.SuccessiveDeltaLimitTrigger;
import com.opengamma.engine.view.calc.trigger.ViewCycleEligibility;
import com.opengamma.engine.view.calc.trigger.ViewCycleTrigger;
import com.opengamma.engine.view.calc.trigger.ViewCycleTriggerResult;
import com.opengamma.engine.view.calc.trigger.ViewCycleType;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.compilation.ViewCompilationServices;
import com.opengamma.engine.view.compilation.ViewDefinitionCompiler;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.ComputationResultListener;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.Pair;

/**
 * The job which schedules and executes computation cycles for a view process.
 */
public class ViewComputationJob extends TerminatableJob implements MarketDataListener {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewComputationJob.class);

  private static final long NANOS_PER_MILLISECOND = 1000000;
  private static final long MARKET_DATA_TIMEOUT_MILLIS = 10000;

  private final ViewProcessImpl _viewProcess;
  private final ViewExecutionOptions _executionOptions;
  private final ViewProcessContext _processContext;
  private final EngineResourceManagerInternal<SingleComputationCycle> _cycleManager;
  private final ViewCycleTrigger _masterCycleTrigger;
  private final FixedTimeTrigger _compilationExpiryCycleTrigger;
  private final boolean _executeCycles;

  private int _cycleCount;
  private EngineResourceReference<SingleComputationCycle> _previousCycleReference;

  private ViewDefinition _viewDefinition;
  private CompiledViewDefinitionWithGraphsImpl _latestCompiledViewDefinition;
  private final Set<ValueRequirement> _marketDataSubscriptions = new HashSet<ValueRequirement>();
  private final Set<ValueRequirement> _pendingSubscriptions = Collections.newSetFromMap(new ConcurrentHashMap<ValueRequirement, Boolean>());
  private CountDownLatch _pendingSubscriptionLatch;

  private ChangeListener _viewDefinitionChangeListener;
  private ConcurrentMap<ObjectId, Boolean> _changedTargets;
  private ChangeListener _targetResolverChangeListener;

  private volatile boolean _wakeOnCycleRequest;
  private volatile boolean _cycleRequested;
  private volatile boolean _forceTriggerCycle;
  private volatile boolean _viewDefinitionDirty = true;
  private volatile boolean _compilationDirty;
  private volatile Future<CompiledViewDefinitionWithGraphsImpl> _compilationTask;

  /**
   * Nanoseconds
   */
  private double _totalTimeNanos;

  private ViewComputationJobDataProvider _marketDataProvider;

  public ViewComputationJob(final ViewProcessImpl viewProcess,
                            final ViewExecutionOptions executionOptions,
                            final ViewProcessContext processContext,
                            final EngineResourceManagerInternal<SingleComputationCycle> cycleManager) {
    ArgumentChecker.notNull(viewProcess, "viewProcess");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ArgumentChecker.notNull(processContext, "processContext");
    ArgumentChecker.notNull(cycleManager, "cycleManager");
    _viewProcess = viewProcess;
    _executionOptions = executionOptions;
    _processContext = processContext;
    _cycleManager = cycleManager;
    _cycleRequested = !executionOptions.getFlags().contains(ViewExecutionFlags.WAIT_FOR_INITIAL_TRIGGER);
    _compilationExpiryCycleTrigger = new FixedTimeTrigger();
    _masterCycleTrigger = createViewCycleTrigger(executionOptions);
    _executeCycles = !getExecutionOptions().getFlags().contains(ViewExecutionFlags.COMPILE_ONLY);
    updateViewDefinitionIfRequired();
    subscribeToViewDefinition();
  }

  private ViewCycleTrigger createViewCycleTrigger(final ViewExecutionOptions executionOptions) {
    final CombinedViewCycleTrigger trigger = new CombinedViewCycleTrigger();
    trigger.addTrigger(_compilationExpiryCycleTrigger);
    if (executionOptions.getFlags().contains(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE)) {
      trigger.addTrigger(new RunAsFastAsPossibleTrigger());
    }
    if (executionOptions.getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_TIME_ELAPSED)) {
      trigger.addTrigger(new RecomputationPeriodTrigger(this));
    }
    if (executionOptions.getMaxSuccessiveDeltaCycles() != null) {
      trigger.addTrigger(new SuccessiveDeltaLimitTrigger(executionOptions.getMaxSuccessiveDeltaCycles()));
    }
    return trigger;
  }

  // TODO get rid of the private getters
  private ViewProcessImpl getViewProcess() {
    return _viewProcess;
  }

  private ViewExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  private ViewProcessContext getProcessContext() {
    return _processContext;
  }

  private EngineResourceManagerInternal<SingleComputationCycle> getCycleManager() {
    return _cycleManager;
  }

  private ViewCycleTrigger getMasterCycleTrigger() {
    return _masterCycleTrigger;
  }

  public FixedTimeTrigger getCompilationExpiryCycleTrigger() {
    return _compilationExpiryCycleTrigger;
  }

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
        processCompleted();
        return;
      }
    } catch (final Exception e) {
      s_logger.error("Error obtaining next view cycle execution options from sequence for view process " + getViewProcess(), e);
      return;
    }

    if (executionOptions.getMarketDataSpecifications().isEmpty()) {
      s_logger.error("No market data specifications for cycle");
      cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException("No market data specifications for cycle"));
      return;
    }

    MarketDataSnapshot marketDataSnapshot;
    try {
      if (_marketDataProvider == null ||
          !_marketDataProvider.getMarketDataSpecifications().equals(executionOptions.getMarketDataSpecifications())) {
        // A different market data provider is required. We support this because we can, but changing provider is not the
        // most efficient operation.
        if (_marketDataProvider != null) {
          s_logger.info("Replacing market data provider between cycles");
        }
        replaceMarketDataProvider(executionOptions.getMarketDataSpecifications());
      }

      // Obtain the snapshot in case it is needed, but don't explicitly initialise it until the data is required
      marketDataSnapshot = _marketDataProvider.snapshot();
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
    final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition;
    try {
      compiledViewDefinition = getCompiledViewDefinition(compilationValuationTime, versionCorrection);
      if (compiledViewDefinition == null) {
        s_logger.warn("Job terminated during view compilation");
        return;
      }
    } catch (final Exception e) {
      final String message = MessageFormat.format("Error obtaining compiled view definition {0} for time {1} at version-correction {2}",
          getViewProcess().getDefinitionId(), compilationValuationTime, versionCorrection);
      s_logger.error(message);
      cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException(message, e));
      return;
    }

    try {
      if (getExecutionOptions().getFlags().contains(ViewExecutionFlags.AWAIT_MARKET_DATA)) {
        marketDataSnapshot.init(compiledViewDefinition.getMarketDataRequirements().keySet(), MARKET_DATA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
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
      s_logger.error("Error creating next view cycle for view process " + getViewProcess(), e);
      return;
    }

    if (_executeCycles) {
      try {
        final SingleComputationCycle singleComputationCycle = cycleReference.get();
        final HashMap<String, Collection<ComputationTargetSpecification>> configToComputationTargets = new HashMap<String, Collection<ComputationTargetSpecification>>();
        final HashMap<String, Map<ValueSpecification, Set<ValueRequirement>>> configToTerminalOutputs = new HashMap<String, Map<ValueSpecification, Set<ValueRequirement>>>();
        for (final DependencyGraph graph : compiledViewDefinition.getAllDependencyGraphs()) {
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
        executeViewCycle(cycleType, cycleReference, marketDataSnapshot, getViewProcess().getCalcJobResultExecutorService());
      } catch (final InterruptedException e) {
        // Execution interrupted - don't propagate as failure
        s_logger.info("View cycle execution interrupted for view process {}", getViewProcess());
        cycleReference.release();
        return;
      } catch (final Exception e) {
        // Execution failed
        s_logger.error("View cycle execution failed for view process " + getViewProcess(), e);
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
      processCompleted();
    }

    if (_executeCycles) {
      if (_previousCycleReference != null) {
        _previousCycleReference.release();
      }
      _previousCycleReference = cycleReference;
    }
  }

  private void cycleCompleted(final ViewCycle cycle) {
    try {
      getViewProcess().cycleCompleted(cycle);
    } catch (final Exception e) {
      s_logger.error("Error notifying view process " + getViewProcess() + " of view cycle completion", e);
    }
  }

  private void cycleStarted(final ViewCycleMetadata cycleMetadata) {
    try {
      getViewProcess().cycleStarted(cycleMetadata);
    } catch (final Exception e) {
      s_logger.error("Error notifying view process " + getViewProcess() + " of view cycle starting", e);
    }
  }

  private void cycleFragmentCompleted(final ViewComputationResultModel result) {

    try {
      getViewProcess().cycleFragmentCompleted(result, _viewDefinition);
    } catch (final Exception e) {
      s_logger.error("Error notifying view process " + getViewProcess() + " of cycle fragment completion", e);
    }
  }

  private void cycleExecutionFailed(final ViewCycleExecutionOptions executionOptions, final Exception exception) {
    try {
      getViewProcess().cycleExecutionFailed(executionOptions, exception);
    } catch (final Exception vpe) {
      s_logger.error("Error notifying the view process " + getViewProcess() + " of the cycle execution error", vpe);
    }
  }

  private void viewDefinitionCompiled(final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition) {
    try {
      getViewProcess().viewDefinitionCompiled(compiledViewDefinition, _marketDataProvider.getPermissionProvider());
    } catch (final Exception vpe) {
      s_logger.error("Error notifying view process " + getViewProcess() + " of view definition compilation");
    }
  }

  private void viewDefinitionCompilationFailed(final Instant compilationTime, final Exception e) {
    try {
      getViewProcess().viewDefinitionCompilationFailed(compilationTime, e);
    } catch (final Exception vpe) {
      s_logger.error("Error notifying the view process " + getViewProcess() + " of the view definition compilation failure", vpe);
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
      if (cycleEligibility == ViewCycleEligibility.FORCE || cycleEligibility == ViewCycleEligibility.ELIGIBLE && _cycleRequested) {
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
                                final MarketDataSnapshot marketDataSnapshot,
                                final ExecutorService calcJobResultExecutorService) throws Exception {
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
      cycleReference.get().execute(deltaCycle, marketDataSnapshot, calcJobResultExecutorService);
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

    final long durationNanos = cycleReference.get().getDuration().toNanosLong();
    _totalTimeNanos += durationNanos;
    _cycleCount += 1;
    s_logger.info("Last latency was {} ms, Average latency is {} ms",
                  durationNanos / NANOS_PER_MILLISECOND,
                  (_totalTimeNanos / _cycleCount) / NANOS_PER_MILLISECOND);
  }

  @Override
  protected void postRunCycle() {
    if (_previousCycleReference != null) {
      _previousCycleReference.release();
    }
    unsubscribeFromViewDefinition();
    unsubscribeFromTargetResolverChanges();
    removeMarketDataProvider();
    invalidateCachedCompiledViewDefinition();
  }

  @Override
  public void terminate() {
    super.terminate();
    final Future<CompiledViewDefinitionWithGraphsImpl> task = _compilationTask;
    if (task != null) {
      task.cancel(true);
    }
  }

  private void processCompleted() {
    s_logger.info("Computation job completed for view process {}", getViewProcess());
    try {
      getViewProcess().processCompleted();
    } catch (final Exception e) {
      s_logger.error("Error notifying view process " + getViewProcess() + " of computation job completion", e);
    }
    terminate();
  }

  /**
   * Indicates that the view definition itself has changed. It is not necessary to call {@link #dirtyCompilation()} as well.
   */
  public void dirtyViewDefinition() {
    s_logger.info("Marking view definition as dirty for view process {}", getViewProcess());
    _viewDefinitionDirty = true;
    triggerCycle();
  }

  /**
   * Indicates that changes have occurred which may affect the compilation, and the view definition should be recompiled at the earliest opportunity.
   */
  public void dirtyCompilation() {
    s_logger.info("Marking compilation as dirty for view process {}", getViewProcess());
    _compilationDirty = true;
  }

  /**
   * Forces a cycle to run, regardless of how long has elapsed since the previous cycle.
   */
  public synchronized void triggerCycle() {
    s_logger.debug("Cycle triggered manually");
    _forceTriggerCycle = true;
    notifyAll();
  }

  /**
   * Requests a cycle to run, honouring minimum times between cycles.
   */
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

  private EngineResourceReference<SingleComputationCycle> createCycle(final ViewCycleExecutionOptions executionOptions,
      final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition, final VersionCorrection versionCorrection) {
    // View definition was compiled based on compilation options, which might have only included an indicative
    // valuation time. A further check ensures that the compiled view definition is still valid.
    if (!compiledViewDefinition.isValidFor(executionOptions.getValuationTime())) {
      throw new OpenGammaRuntimeException("Compiled view definition " + compiledViewDefinition + " not valid for execution options " + executionOptions);
    }
    final UniqueId cycleId = getViewProcess().generateCycleId();

    final ComputationResultListener streamingResultListener = new ComputationResultListener() {
      @Override
      public void resultAvailable(final ViewComputationResultModel result) {
        cycleFragmentCompleted(result);
      }
    };
    final SingleComputationCycle cycle = new SingleComputationCycle(cycleId, getViewProcess().getUniqueId(),
        streamingResultListener, getProcessContext(), compiledViewDefinition, executionOptions, versionCorrection);
    return getCycleManager().manage(cycle);
  }

  private void subscribeToTargetResolverChanges() {
    if (_changedTargets == null) {
      assert _targetResolverChangeListener == null;
      final ConcurrentMap<ObjectId, Boolean> changed = new ConcurrentHashMap<ObjectId, Boolean>();
      _changedTargets = changed;
      _targetResolverChangeListener = new ChangeListener() {
        @Override
        public void entityChanged(final ChangeEvent event) {
          final ObjectId oid = event.getObjectId();
          if (changed.replace(oid, Boolean.FALSE, Boolean.TRUE)) {
            s_logger.info("Received change notification for {}", event.getObjectId());
            requestCycle();
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

  private void removePortfolioTerminalOutputs(final Map<String, Pair<DependencyGraph, Set<ValueRequirement>>> previousGraphs, final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition) {
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
   * @return the invalid identifier set, or null if none are invalid
   */
  private Set<UniqueId> getInvalidIdentifiers(final Map<ComputationTargetReference, UniqueId> previousResolutions, final VersionCorrection versionCorrection) {
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
        if (_changedTargets.replace(oid, Boolean.TRUE, Boolean.FALSE)) {
          // A change was seen on this target
          s_logger.debug("Change observed on {}", oid);
          toCheck.add(previousResolution.getKey());
        } else if (_changedTargets.putIfAbsent(oid, Boolean.FALSE) == null) {
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
    Set<UniqueId> invalidIdentifiers = null;
    for (final Map.Entry<ComputationTargetReference, UniqueId> target : previousResolutions.entrySet()) {
      final ComputationTargetSpecification resolved = specifications.get(target.getKey());
      if ((resolved != null) && target.getValue().equals(resolved.getUniqueId())) {
        // No change
        s_logger.debug("No change resolving {}", target);
      } else if (toCheck.contains(target.getKey())) {
        // Identifier no longer resolved, or resolved differently
        s_logger.info("New resolution of {} to {}", target, resolved);
        if (invalidIdentifiers == null) {
          invalidIdentifiers = new HashSet<UniqueId>();
        }
        invalidIdentifiers.add(target.getValue());
      }
    }
    s_logger.debug("{} resolutions checked in {}ms", toCheck.size(), t / 1e6);
    return invalidIdentifiers;
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
      final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition) {
    if (previousGraphs == null) {
      final Map<String, DependencyGraph> graphs = compiledViewDefinition.getDependencyGraphsByConfiguration();
      previousGraphs = Maps.newHashMapWithExpectedSize(graphs.size());
      for (final Map.Entry<String, DependencyGraph> graph : graphs.entrySet()) {
        previousGraphs.put(graph.getKey(), Pair.<DependencyGraph, Set<ValueRequirement>>of(graph.getValue(), new HashSet<ValueRequirement>()));
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

  private CompiledViewDefinitionWithGraphsImpl getCompiledViewDefinition(final Instant valuationTime, final VersionCorrection versionCorrection) {
    final long functionInitId = getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getFunctionInitId();
    CompiledViewDefinitionWithGraphsImpl compiledViewDefinition;
    updateViewDefinitionIfRequired();
    if (_compilationDirty) {
      _compilationDirty = false;
      invalidateCachedCompiledViewDefinition();
      compiledViewDefinition = null;
    } else {
      compiledViewDefinition = getCachedCompiledViewDefinition();
    }
    Map<String, Pair<DependencyGraph, Set<ValueRequirement>>> previousGraphs = null;
    ConcurrentMap<ComputationTargetReference, UniqueId> previousResolutions = null;
    boolean portfolioFull = false;
    if (compiledViewDefinition != null) {
      do {
        if (functionInitId != compiledViewDefinition.getFunctionInitId()) {
          // The function repository has been reinitialized which invalidates any previous graphs
          // TODO: [PLAT-2237, PLAT-1623, PLAT-2240] Get rid of this
          break;
        }
        final Map<ComputationTargetReference, UniqueId> resolvedIdentifiers = compiledViewDefinition.getResolvedIdentifiers();
        final Set<UniqueId> invalidIdentifiers = getInvalidIdentifiers(resolvedIdentifiers, versionCorrection);
        if (invalidIdentifiers != null) {
          previousGraphs = getPreviousGraphs(previousGraphs, compiledViewDefinition);
          if (invalidIdentifiers.contains(compiledViewDefinition.getPortfolio().getUniqueId())) {
            // The portfolio resolution is different, invalidate all PORTFOLIO and PORTFOLIO_NODE nodes in the graph
            removePortfolioTerminalOutputs(previousGraphs, compiledViewDefinition);
            filterPreviousGraphs(previousGraphs, new InvalidPortfolioDependencyNodeFilter());
            portfolioFull = true;
          }
          // Invalidate any dependency graph nodes on the invalid targets
          filterPreviousGraphs(previousGraphs, new InvalidTargetDependencyNodeFilter(invalidIdentifiers));
          previousResolutions = new ConcurrentHashMap<ComputationTargetReference, UniqueId>(resolvedIdentifiers.size());
          for (final Map.Entry<ComputationTargetReference, UniqueId> resolvedIdentifier : resolvedIdentifiers.entrySet()) {
            if (!invalidIdentifiers.contains(resolvedIdentifier.getValue())) {
              previousResolutions.put(resolvedIdentifier.getKey(), resolvedIdentifier.getValue());
            }
          }
        }
        if (!compiledViewDefinition.isValidFor(valuationTime)) {
          // Invalidate any dependency graph nodes that use functions that are no longer valid
          previousGraphs = getPreviousGraphs(previousGraphs, compiledViewDefinition);
          filterPreviousGraphs(previousGraphs, new InvalidFunctionDependencyNodeFilter(valuationTime));
        }
        if (previousGraphs == null) {
          // Existing cached model is valid (an optimization for the common case of similar, increasing valuation times)
          return compiledViewDefinition;
        }
        if (previousResolutions == null) {
          previousResolutions = new ConcurrentHashMap<ComputationTargetReference, UniqueId>(resolvedIdentifiers);
        }
      } while (false);
    }
    try {
      final MarketDataAvailabilityProvider availabilityProvider = _marketDataProvider.getAvailabilityProvider();
      final ViewCompilationServices compilationServices = getProcessContext().asCompilationServices(availabilityProvider);
      if (previousGraphs != null) {
        _compilationTask = ViewDefinitionCompiler.incrementalCompileTask(_viewDefinition, compilationServices, valuationTime, versionCorrection, previousGraphs, previousResolutions, portfolioFull);
      } else {
        _compilationTask = ViewDefinitionCompiler.fullCompileTask(_viewDefinition, compilationServices, valuationTime, versionCorrection);
      }
      try {
        if (!isTerminated()) {
          compiledViewDefinition = _compilationTask.get();
        } else {
          return null;
        }
      } finally {
        _compilationTask = null;
      }
    } catch (final Exception e) {
      final String message = MessageFormat.format("Error compiling view definition {0} for time {1}", getViewProcess().getDefinitionId(), valuationTime);
      viewDefinitionCompilationFailed(valuationTime, new OpenGammaRuntimeException(message, e));
      throw new OpenGammaRuntimeException(message, e);
    }
    setCachedCompiledViewDefinition(compiledViewDefinition);
    // [PLAT-984]
    // Assume that valuation times are increasing in real-time towards the expiry of the view definition, so that we
    // can predict the time to expiry. If this assumption is wrong then the worst we do is trigger an unnecessary
    // cycle. In the predicted case, we trigger a cycle on expiry so that any new market data subscriptions are made
    // straight away.
    if ((compiledViewDefinition.getValidTo() != null) && getExecutionOptions().getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED)) {
      final Duration durationToExpiry = _marketDataProvider.getRealTimeDuration(valuationTime, compiledViewDefinition.getValidTo());
      final long expiryNanos = System.nanoTime() + durationToExpiry.toNanosLong();
      _compilationExpiryCycleTrigger.set(expiryNanos, ViewCycleTriggerResult.forceFull());
      // REVIEW Andrew 2012-11-02 -- If we are ticking live, then this is almost right (System.nanoTime will be close to valuationTime, depending on how
      // long the compilation took). If we are running through historical data then this is quite a meaningless trigger.
    } else {
      _compilationExpiryCycleTrigger.reset();
    }

    // TODO reorder the next two calls so the subscriptions are known before the compilation callback?
    // this would mean the actual subscriptions are known before the permissions provider is queried
    // so the provider that will be providing each piece of data is queried for its permissions.
    // otherwise there's there possibility that a provider will

    // Notify the view that a (re)compilation has taken place before going on to do any time-consuming work.
    // This might contain enough for clients to e.g. render an empty grid in which results will later appear.
    viewDefinitionCompiled(compiledViewDefinition);

    // Update the market data subscriptions to whatever is now required, ensuring the computation cycle can find the
    // required input data when it is executed.
    setMarketDataSubscriptions(compiledViewDefinition.getMarketDataRequirements().keySet());
    return compiledViewDefinition;
  }

  /**
   * Gets the cached compiled view definition which may be re-used in subsequent computation cycles.
   * <p>
   * External visibility for tests.
   *
   * @return the cached compiled view definition, or null if nothing is currently cached
   */
  public CompiledViewDefinitionWithGraphsImpl getCachedCompiledViewDefinition() {
    return _latestCompiledViewDefinition;
  }

  private void invalidateCachedCompiledViewDefinition() {
    _latestCompiledViewDefinition = null;
  }

  /**
   * Replaces the cached compiled view definition.
   * <p>
   * External visibility for tests.
   *
   * @param latestCompiledViewDefinition the compiled view definition, may be null
   */
  public void setCachedCompiledViewDefinition(final CompiledViewDefinitionWithGraphsImpl latestCompiledViewDefinition) {
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
    if (_viewDefinitionDirty) {
      _viewDefinition = getViewProcess().getLatestViewDefinition();
      invalidateCachedCompiledViewDefinition();
      if (_viewDefinition == null) {
        throw new DataNotFoundException("View definition " + getViewProcess().getDefinitionId() + " not found");
      }
      _viewDefinitionDirty = false;
    }
  }

  private void subscribeToViewDefinition() {
    if (_viewDefinitionChangeListener != null) {
      return;
    }
    _viewDefinitionChangeListener = new ViewDefinitionChangeListener(this, getViewProcess().getDefinitionId());
    getProcessContext().getConfigSource().changeManager().addChangeListener(_viewDefinitionChangeListener);
  }

  private void unsubscribeFromViewDefinition() {
    if (_viewDefinitionChangeListener == null) {
      return;
    }
    getProcessContext().getConfigSource().changeManager().removeChangeListener(_viewDefinitionChangeListener);
    _viewDefinitionChangeListener = null;
  }

  private void replaceMarketDataProvider(final List<MarketDataSpecification> marketDataSpecs) {
    removeMarketDataProvider();
    // A different market data provider may change the availability of market data, altering the dependency graph
    invalidateCachedCompiledViewDefinition();
    setMarketDataProvider(marketDataSpecs);
  }

  private void removeMarketDataProvider() {
    if (_marketDataProvider == null) {
      return;
    }
    removeMarketDataSubscriptions();
    _marketDataProvider.removeListener(this);
    _marketDataProvider = null;
  }

  private void setMarketDataProvider(final List<MarketDataSpecification> marketDataSpecs) {
    try {
      _marketDataProvider = new ViewComputationJobDataProvider(_viewDefinition.getMarketDataUser(),
                                                               marketDataSpecs,
                                                               _processContext.getMarketDataProviderResolver());
    } catch (final Exception e) {
      s_logger.error("Failed to create data provider", e);
      _marketDataProvider = null;
    }
    if (_marketDataProvider != null) {
      _marketDataProvider.addListener(this);
    }
  }

  private void setMarketDataSubscriptions(final Set<ValueRequirement> requiredSubscriptions) {
    final Set<ValueRequirement> currentSubscriptions = _marketDataSubscriptions;
    final Set<ValueRequirement> unusedMarketData = Sets.difference(currentSubscriptions, requiredSubscriptions);
    if (!unusedMarketData.isEmpty()) {
      s_logger.debug("{} unused market data subscriptions: {}", unusedMarketData.size(), unusedMarketData);
      removeMarketDataSubscriptions(new ArrayList<ValueRequirement>(unusedMarketData));
    }
    final Set<ValueRequirement> newMarketData = Sets.difference(requiredSubscriptions, currentSubscriptions);
    if (!newMarketData.isEmpty()) {
      s_logger.debug("{} new market data requirements: {}", newMarketData.size(), newMarketData);
      addMarketDataSubscriptions(new HashSet<ValueRequirement>(newMarketData));
    }
  }

  //-------------------------------------------------------------------------
  private void addMarketDataSubscriptions(final Set<ValueRequirement> requiredSubscriptions) {
    final OperationTimer timer = new OperationTimer(s_logger, "Adding {} market data subscriptions", requiredSubscriptions.size());
    _pendingSubscriptions.addAll(requiredSubscriptions);
    _pendingSubscriptionLatch = new CountDownLatch(requiredSubscriptions.size());
    _marketDataProvider.subscribe(requiredSubscriptions);
    _marketDataSubscriptions.addAll(requiredSubscriptions);
    try {
      if (!_pendingSubscriptionLatch.await(MARKET_DATA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
        final long remainingCount = _pendingSubscriptionLatch.getCount();
        s_logger.warn("Timed out after {} ms waiting for market data subscriptions to be made. The market data " +
            "snapshot used in the computation cycle could be incomplete. Still waiting for {} out of {} market data " +
            "subscriptions",
            new Object[] {MARKET_DATA_TIMEOUT_MILLIS, remainingCount, _marketDataSubscriptions.size() });
      }
    } catch (final InterruptedException ex) {
      s_logger.info("Interrupted while waiting for subscription results.");
    } finally {
      _pendingSubscriptions.clear();
      _pendingSubscriptionLatch = null;
    }
    timer.finished();
  }

  private void removePendingSubscription(final ValueRequirement requirement) {
    final CountDownLatch pendingSubscriptionLatch = _pendingSubscriptionLatch;
    if (_pendingSubscriptions.remove(requirement) && pendingSubscriptionLatch != null) {
      pendingSubscriptionLatch.countDown();
    }
  }

  private void removeMarketDataSubscriptions() {
    removeMarketDataSubscriptions(_marketDataSubscriptions);
  }

  private void removeMarketDataSubscriptions(final Collection<ValueRequirement> unusedSubscriptions) {
    final OperationTimer timer = new OperationTimer(s_logger, "Removing {} market data subscriptions", unusedSubscriptions.size());
    _marketDataProvider.unsubscribe(_marketDataSubscriptions);
    _marketDataSubscriptions.removeAll(unusedSubscriptions);
    timer.finished();
  }

  //-------------------------------------------------------------------------
  @Override
  public void subscriptionSucceeded(final ValueRequirement requirement) {
    s_logger.debug("Subscription succeeded: {}", requirement);
    removePendingSubscription(requirement);
  }

  @Override
  public void subscriptionFailed(final ValueRequirement requirement, final String msg) {
    s_logger.debug("Market data subscription to {} failed. This market data may be missing from computation cycles.", requirement);
    removePendingSubscription(requirement);
  }

  @Override
  public void subscriptionStopped(final ValueRequirement requirement) {
  }

  @Override
  public void valuesChanged(final Collection<ValueRequirement> values) {
    if (!getExecutionOptions().getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED)) {
      return;
    }

    final CompiledViewDefinitionWithGraphsImpl compiledView = getCachedCompiledViewDefinition();
    if (compiledView == null) {
      return;
    }
    //Since this happens for every tick, for every job, we need to use the quick call here
    if (compiledView.hasAnyMarketDataRequirements(values)) {
      requestCycle();
    }
  }

}
