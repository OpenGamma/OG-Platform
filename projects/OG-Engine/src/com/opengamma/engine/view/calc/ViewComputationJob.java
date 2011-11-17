/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.time.Duration;
import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
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
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.monitor.OperationTimer;

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
  
  private volatile boolean _wakeOnMarketDataChanged;
  private volatile boolean _marketDataChanged = true;
  private volatile boolean _forceTriggerCycle;
  private volatile boolean _viewDefinitionDirty = true;
  private volatile boolean _compilationDirty;
  
  /**
   * Nanoseconds
   */
  private double _totalTimeNanos;

  private MarketDataProvider _marketDataProvider;
  
  public ViewComputationJob(ViewProcessImpl viewProcess, ViewExecutionOptions executionOptions,
      ViewProcessContext processContext, EngineResourceManagerInternal<SingleComputationCycle> cycleManager) {
    ArgumentChecker.notNull(viewProcess, "viewProcess");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ArgumentChecker.notNull(processContext, "processContext");
    ArgumentChecker.notNull(cycleManager, "cycleManager");
    _viewProcess = viewProcess;
    _executionOptions = executionOptions;
    _processContext = processContext;
    _cycleManager = cycleManager;
    _marketDataChanged = !executionOptions.getFlags().contains(ViewExecutionFlags.WAIT_FOR_INITIAL_TRIGGER);
    _compilationExpiryCycleTrigger = new FixedTimeTrigger();
    _masterCycleTrigger = createViewCycleTrigger(executionOptions);
    _executeCycles = !getExecutionOptions().getFlags().contains(ViewExecutionFlags.COMPILE_ONLY);
    updateViewDefinitionIfRequired();
    subscribeToViewDefinition();
  }

  private ViewCycleTrigger createViewCycleTrigger(ViewExecutionOptions executionOptions) {
    CombinedViewCycleTrigger trigger = new CombinedViewCycleTrigger();
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

  //-------------------------------------------------------------------------
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
   * 
   * <ul>
   *   <li>A computation cycle can only be triggered if the relevant minimum computation period has passed since the
   *   start of the previous cycle.
   *   <li>A computation cycle will be forced if the relevant maximum computation period has passed since the start of
   *   the previous cycle.
   *   <li>A full computation is preferred over a delta computation if both are possible.
   *   <li>Performing a full computation also updates the times to the next delta computation; i.e. a full computation
   *   is considered to be as good as a delta.
   * </ul>
   */
  @Override
  protected void runOneCycle() {
    // Exception handling is important here to ensure that computation jobs do not just die quietly while consumers are
    // potentially blocked, waiting for results.
    
    ViewCycleType cycleType;
    try {
      cycleType = waitForNextCycle();
    } catch (InterruptedException e) {
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
    } catch (Exception e) {
      s_logger.error("Error obtaining next view cycle execution options from sequence for view process " + getViewProcess(), e);
      return;
    }
    
    if (executionOptions.getMarketDataSpecification() == null) {
      s_logger.error("No market data specification for cycle");
      cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException("No market data specification for cycle"));
      return;
    }
    
    MarketDataSnapshot marketDataSnapshot;
    try {
      if (getMarketDataProvider() == null || !getMarketDataProvider().isCompatible(executionOptions.getMarketDataSpecification())) {
        // A different market data provider is required. We support this because we can, but changing provider is not the
        // most efficient operation.
        if (getMarketDataProvider() != null) {
          s_logger.info("Replacing market data provider between cycles");
        }
        replaceMarketDataProvider(executionOptions.getMarketDataSpecification());
      }
      
      // Obtain the snapshot in case it is needed, but don't explicitly initialise it until the data is required
      marketDataSnapshot = getMarketDataProvider().snapshot(executionOptions.getMarketDataSpecification());
    } catch (Exception e) {
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
    } catch (Exception e) {
      s_logger.error("Error obtaining compilation valuation time", e);
      cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException("Error obtaining compilation valuation time", e));
      return;
    }
    
    VersionCorrection versionCorrection = getResolvedVersionCorrection();
    CompiledViewDefinitionWithGraphsImpl compiledViewDefinition;
    try {
      compiledViewDefinition = getCompiledViewDefinition(compilationValuationTime, versionCorrection);
    } catch (Exception e) {
      String message = MessageFormat.format("Error obtaining compiled view definition {0} for time {1} at version-correction {2}",
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
        executionOptions.setValuationTime(marketDataSnapshot.getSnapshotTime());
      }
    } catch (Exception e) {
      s_logger.error("Error initializing snapshot {}", marketDataSnapshot);
      cycleExecutionFailed(executionOptions, new OpenGammaRuntimeException("Error initializing snapshot" + marketDataSnapshot, e));
    }
    
    EngineResourceReference<SingleComputationCycle> cycleReference;
    try {
      cycleReference = createCycle(executionOptions, compiledViewDefinition, versionCorrection);
    } catch (Exception e) {
      s_logger.error("Error creating next view cycle for view process " + getViewProcess(), e);
      return;
    }
    
    if (_executeCycles) {
      try {
        executeViewCycle(cycleType, cycleReference, marketDataSnapshot, getViewProcess().getCalcJobResultExecutorService());
      } catch (InterruptedException e) {
        // Execution interrupted - don't propagate as failure
        s_logger.info("View cycle execution interrupted for view process {}", getViewProcess());
        cycleReference.release();
        return;
      } catch (Exception e) {
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

  private void cycleCompleted(ViewCycle cycle) {
    try {
      getViewProcess().cycleCompleted(cycle);
    } catch (Exception e) {
      s_logger.error("Error notifying view process " + getViewProcess() + " of view cycle completion", e);
    }
  }

  private void cycleFragmentCompleted(ViewComputationResultModel result) {
    try {
      getViewProcess().cycleFragmentCompleted(result);
    } catch (Exception e) {
      s_logger.error("Error notifying view process " + getViewProcess() + " of cycle fragment completion", e);
    }
  }

  private void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
    try {
      getViewProcess().cycleExecutionFailed(executionOptions, exception);
    } catch (Exception vpe) {
      s_logger.error("Error notifying the view process " + getViewProcess() + " of the cycle execution error", vpe);
    }
  }
  
  private void viewDefinitionCompiled(CompiledViewDefinitionWithGraphsImpl compiledViewDefinition) {
    try {
      getViewProcess().viewDefinitionCompiled(compiledViewDefinition, getMarketDataProvider().getPermissionProvider());
    } catch (Exception vpe) {
      s_logger.error("Error notifying view process " + getViewProcess() + " of view definition compilation");
    }
  }
  
  private void viewDefinitionCompilationFailed(Instant compilationTime, Exception e) {
    try {
      getViewProcess().viewDefinitionCompilationFailed(compilationTime, e);
    } catch (Exception vpe) {
      s_logger.error("Error notifying the view process " + getViewProcess() + " of the view definition compilation failure", vpe);
    }
  }
  
  
  private synchronized ViewCycleType waitForNextCycle() throws InterruptedException {
    while (true) {
      long currentTimeNanos = System.nanoTime();
      ViewCycleTriggerResult triggerResult = getMasterCycleTrigger().query(currentTimeNanos);
      
      ViewCycleEligibility cycleEligibility = triggerResult.getCycleEligibility();
      if (_forceTriggerCycle) {
        cycleEligibility = ViewCycleEligibility.FORCE;
        _forceTriggerCycle = false;
      }
      if (cycleEligibility == ViewCycleEligibility.FORCE || cycleEligibility == ViewCycleEligibility.ELIGIBLE && _marketDataChanged) {
        _marketDataChanged = false;
        ViewCycleType cycleType = triggerResult.getCycleType();
        if (_previousCycleReference == null) {
          // Cannot do a delta if we have no previous cycle
          cycleType = ViewCycleType.FULL;
        }
        try {
          getMasterCycleTrigger().cycleTriggered(currentTimeNanos, cycleType);
        } catch (Exception e) {
          s_logger.error("Error notifying trigger of intention to execute cycle", e);
        }
        s_logger.debug("Eligible for {} cycle", cycleType);
        return cycleType;
      }
      
      // Going to sleep
      long wakeUpTime = triggerResult.getNextStateChangeNanos();
      if (_marketDataChanged) {
        s_logger.debug("Sleeping until eligible to perform the next computation cycle");
        // No amount of market data can make us eligible for a computation cycle any sooner.
        _wakeOnMarketDataChanged = false;
      } else {
        s_logger.debug("Sleeping until forced to perform the next computation cycle");
        _wakeOnMarketDataChanged = cycleEligibility == ViewCycleEligibility.ELIGIBLE;
      }
      
      long sleepTime = wakeUpTime - currentTimeNanos;
      sleepTime = Math.max(0, sleepTime);
      sleepTime /= NANOS_PER_MILLISECOND;
      sleepTime += 1; // Could have been rounded down during division so ensure only woken after state change 
      s_logger.debug("Waiting for {} ms", sleepTime);
      try {
        // This could wait until end of time. In this case, only marketDataChanged() or triggerCycle() will wake it up
        wait(sleepTime);
      } catch (InterruptedException e) {
        // We support interruption as a signal that we have been terminated. If we're interrupted without having been
        // terminated, we'll just return to this method and go back to sleep.
        Thread.interrupted();
        s_logger.info("Interrupted while delaying. Continuing operation.");
        throw e;
      }
    }
  }
  
  private void executeViewCycle(ViewCycleType cycleType, 
                                EngineResourceReference<SingleComputationCycle> cycleReference, 
                                MarketDataSnapshot marketDataSnapshot,
                                ExecutorService calcJobResultExecutorService) throws Exception {
    SingleComputationCycle deltaCycle;
    if (cycleType == ViewCycleType.FULL) {
      s_logger.info("Performing full computation");
      deltaCycle = null;
    } else {
      s_logger.info("Performing delta computation");
      deltaCycle = _previousCycleReference.get();
    }
    
    try {
      cycleReference.get().execute(deltaCycle, marketDataSnapshot, calcJobResultExecutorService);
    } catch (InterruptedException e) {
      Thread.interrupted();
      // In reality this means that the job has been terminated, and it will end as soon as we return from this method.
      // In case the thread has been interrupted without terminating the job, we tidy everything up as if the
      // interrupted cycle never happened so that deltas will be calculated from the previous cycle.
      s_logger.info("Interrupted while executing a computation cycle. No results will be output from this cycle.");
      throw e;
    } catch (Exception e) {
      s_logger.error("Error while executing view cycle", e);
      throw e;
    }
    
    long durationNanos = cycleReference.get().getDuration().toNanosLong();
    _totalTimeNanos += durationNanos;
    _cycleCount += 1;
    s_logger.info("Last latency was {} ms, Average latency is {} ms", durationNanos / NANOS_PER_MILLISECOND, (_totalTimeNanos / _cycleCount) / NANOS_PER_MILLISECOND);
  }
    
  @Override
  protected void postRunCycle() {
    if (_previousCycleReference != null) {
      _previousCycleReference.release();
    }
    unsubscribeFromViewDefinition();
    removeMarketDataProvider();
    invalidateCachedCompiledViewDefinition();
  }
  
  private void processCompleted() {
    s_logger.info("Computation job completed for view process {}", getViewProcess());
    try {
      getViewProcess().processCompleted();
    } catch (Exception e) {
      s_logger.error("Error notifying view process " + getViewProcess() + " of computation job completion", e);
    }
    terminate();
  }
  
  /**
   * Indicates that the view definition itself has changed. It is not necessary to call {@link #dirtyCompilation()}
   * as well.
   */
  public void dirtyViewDefinition() {
    s_logger.info("Marking view definition as dirty for view process {}", getViewProcess());
    _viewDefinitionDirty = true;
  }
  
  /**
   * Indicates that changes have occurred which may affect the compilation, and the view definition should be
   * recompiled at the earliest opportunity.
   */
  public void dirtyCompilation() {
    s_logger.info("Marking compilation as dirty for view process {}", getViewProcess());
    _compilationDirty = true;
  }
  
  public synchronized void triggerCycle() {
    s_logger.debug("Cycle triggered manually");
    _forceTriggerCycle = true;
    notifyAll();
  }
  
  public synchronized void marketDataChanged() {
    // REVIEW jonathan 2010-10-04 -- this synchronisation is necessary, but it feels very heavyweight for
    // high-frequency market data. See how it goes, but we could take into account the recalc periods and apply a
    // heuristic (e.g. only wake up due to market data if max - min < e, for some e) which tries to see whether it's
    // worth doing all this.
    
    s_logger.debug("Market Data changed");
    _marketDataChanged = true;
    if (!_wakeOnMarketDataChanged) {
      return;
    }
    notifyAll();
  }
  
  //-------------------------------------------------------------------------
  private EngineResourceReference<SingleComputationCycle> createCycle(ViewCycleExecutionOptions executionOptions,
      CompiledViewDefinitionWithGraphsImpl compiledViewDefinition, VersionCorrection versionCorrection) {
    // View definition was compiled based on compilation options, which might have only included an indicative
    // valuation time. A further check ensures that the compiled view definition is still valid.
    if (!compiledViewDefinition.isValidFor(executionOptions.getValuationTime())) {
      throw new OpenGammaRuntimeException("Compiled view definition " + compiledViewDefinition + " not valid for execution options " + executionOptions);
    }
    UniqueId cycleId = getViewProcess().generateCycleId();
    
    ComputationResultListener streamingResultListener = new ComputationResultListener() {
      @Override
      public void resultAvailable(ViewComputationResultModel result) {
        cycleFragmentCompleted(result);
      }
    };
    SingleComputationCycle cycle = new SingleComputationCycle(cycleId, getViewProcess().getUniqueId(),
        streamingResultListener, getProcessContext(), compiledViewDefinition, executionOptions, versionCorrection);
    return getCycleManager().manage(cycle);
  }

  private VersionCorrection getResolvedVersionCorrection() {
    VersionCorrection versionCorrection = getExecutionOptions().getVersionCorrection();
    if (!versionCorrection.containsLatest()) {
      return versionCorrection;
    }
    return versionCorrection.withLatestFixed(Instant.now());
  }

  private CompiledViewDefinitionWithGraphsImpl getCompiledViewDefinition(Instant valuationTime, VersionCorrection versionCorrection) {
    long functionInitId = getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getFunctionInitId();
    CompiledViewDefinitionWithGraphsImpl compiledViewDefinition;
    updateViewDefinitionIfRequired();
    if (_compilationDirty) {
      _compilationDirty = false;
      invalidateCachedCompiledViewDefinition();
      compiledViewDefinition = null;
    } else {
      compiledViewDefinition = getCachedCompiledViewDefinition();
    }
    if (compiledViewDefinition != null && compiledViewDefinition.isValidFor(valuationTime) && functionInitId == compiledViewDefinition.getFunctionInitId()) {
      // Existing cached model is valid (an optimisation for the common case of similar, increasing valuation times)
      return compiledViewDefinition;
    }
    
    try {
      MarketDataAvailabilityProvider availabilityProvider = getMarketDataProvider().getAvailabilityProvider();
      ViewCompilationServices compilationServices = getProcessContext().asCompilationServices(availabilityProvider);
      compiledViewDefinition = ViewDefinitionCompiler.compile(_viewDefinition, compilationServices, valuationTime, versionCorrection);
    } catch (Exception e) {
      String message = MessageFormat.format("Error compiling view definition {0} for time {1}", getViewProcess().getDefinitionId(), valuationTime);
      viewDefinitionCompilationFailed(valuationTime, new OpenGammaRuntimeException(message, e));
      throw new OpenGammaRuntimeException(message, e);
    }
    setCachedCompiledViewDefinition(compiledViewDefinition);
    // [PLAT-984]
    // Assume that valuation times are increasing in real-time towards the expiry of the view definition, so that we
    // can predict the time to expiry. If this assumption is wrong then the worst we do is trigger an unnecessary
    // cycle. In the predicted case, we trigger a cycle on expiry so that any new market data subscriptions are made
    // straight away.
    if (compiledViewDefinition.getValidTo() != null) {
      Duration durationToExpiry = getMarketDataProvider().getRealTimeDuration(valuationTime, compiledViewDefinition.getValidTo());
      long expiryNanos = System.nanoTime() + durationToExpiry.toNanosLong();
      _compilationExpiryCycleTrigger.set(expiryNanos, ViewCycleTriggerResult.forceFull());
    } else {
      _compilationExpiryCycleTrigger.reset();
    }
    
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
   * @param latestCompiledViewDefinition  the compiled view definition, may be null
   */
  public void setCachedCompiledViewDefinition(CompiledViewDefinitionWithGraphsImpl latestCompiledViewDefinition) {
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
    getProcessContext().getViewDefinitionRepository().changeManager().addChangeListener(_viewDefinitionChangeListener);
  }
  
  private void unsubscribeFromViewDefinition() {
    if (_viewDefinitionChangeListener == null) {
      return;
    }
    getProcessContext().getViewDefinitionRepository().changeManager().removeChangeListener(_viewDefinitionChangeListener);
    _viewDefinitionChangeListener = null;
  }
  
  //-------------------------------------------------------------------------
  private void replaceMarketDataProvider(MarketDataSpecification marketDataSpec) {
    removeMarketDataProvider();
    // A different market data provider may change the availability of market data, altering the dependency graph
    invalidateCachedCompiledViewDefinition();
    setMarketDataProvider(marketDataSpec);
  }
  
  private void removeMarketDataProvider() {
    if (_marketDataProvider == null) {
      return;
    }
    removeMarketDataSubscriptions();
    _marketDataProvider.removeListener(this);
    _marketDataProvider = null;
  }
  
  private MarketDataProvider getMarketDataProvider() {
    return _marketDataProvider;
  }
  
  private void setMarketDataProvider(MarketDataSpecification marketDataSpec) {
    _marketDataProvider = getProcessContext().getMarketDataProviderResolver().resolve(marketDataSpec);
    _marketDataProvider.addListener(this);
  }
  
  private void setMarketDataSubscriptions(final Set<ValueRequirement> requiredSubscriptions) {
    final Set<ValueRequirement> currentSubscriptions = _marketDataSubscriptions;
    final Set<ValueRequirement> unusedMarketData = Sets.difference(currentSubscriptions, requiredSubscriptions);
    if (!unusedMarketData.isEmpty()) {
      s_logger.debug("{} unused market data subscriptions: {}", unusedMarketData.size(), unusedMarketData);
      removeMarketDataSubscriptions(unusedMarketData);
    }
    final Set<ValueRequirement> newMarketData = Sets.difference(requiredSubscriptions, currentSubscriptions);
    if (!newMarketData.isEmpty()) {
      s_logger.debug("{} new market data requirements: {}", newMarketData.size(), newMarketData);
      addMarketDataSubscriptions(newMarketData);
    } 
  }

  //-------------------------------------------------------------------------
  private void addMarketDataSubscriptions(final Set<ValueRequirement> requiredSubscriptions) {
    final OperationTimer timer = new OperationTimer(s_logger, "Adding {} market data subscriptions", requiredSubscriptions.size());
    _pendingSubscriptions.addAll(requiredSubscriptions);
    _pendingSubscriptionLatch = new CountDownLatch(requiredSubscriptions.size());
    getMarketDataProvider().subscribe(getViewDefinition().getMarketDataUser(), requiredSubscriptions);
    _marketDataSubscriptions.addAll(requiredSubscriptions);
    try {
      if (!_pendingSubscriptionLatch.await(MARKET_DATA_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
        long remainingCount = _pendingSubscriptionLatch.getCount();
        s_logger.warn("Timed out after {} ms waiting for market data subscriptions to be made. The market data " +
            "snapshot used in the computation cycle could be incomplete. Still waiting for {} out of {} market data " +
            "subscriptions",
          new Object[] {MARKET_DATA_TIMEOUT_MILLIS, remainingCount, _marketDataSubscriptions.size()});
      }
    } catch (InterruptedException ex) {
      s_logger.info("Interrupted while waiting for subscription results.");
    } finally {
      _pendingSubscriptions.clear();
      _pendingSubscriptionLatch = null;
    }
    timer.finished();
  }
  
  private void removePendingSubscription(ValueRequirement requirement) {
    CountDownLatch pendingSubscriptionLatch = _pendingSubscriptionLatch;
    if (_pendingSubscriptions.remove(requirement) && pendingSubscriptionLatch != null) {
      pendingSubscriptionLatch.countDown();
    }
  }
  
  private void removeMarketDataSubscriptions() {
    removeMarketDataSubscriptions(_marketDataSubscriptions);
  }

  private void removeMarketDataSubscriptions(final Set<ValueRequirement> unusedSubscriptions) {
    final OperationTimer timer = new OperationTimer(s_logger, "Removing {} market data subscriptions", unusedSubscriptions.size());
    getMarketDataProvider().unsubscribe(getViewDefinition().getMarketDataUser(), _marketDataSubscriptions);
    _marketDataSubscriptions.removeAll(unusedSubscriptions);
    timer.finished();
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void subscriptionSucceeded(ValueRequirement requirement) {
    // REVIEW jonathan 2011-01-07
    // Can't tell in general whether this subscription message was relating to a subscription that we made or one that
    // a concurrent user of the MarketDataProvider made.
    s_logger.debug("Subscription succeeded: {}", requirement);
    removePendingSubscription(requirement);
  }

  @Override
  public void subscriptionFailed(ValueRequirement requirement, String msg) {
    s_logger.debug("Market data subscription to {} failed. This market data may be missing from computation cycles.", requirement);
    removePendingSubscription(requirement);
  }

  @Override
  public void subscriptionStopped(ValueRequirement requirement) {   
  }

  @Override
  public void valuesChanged(Collection<ValueRequirement> values) {
    if (!getExecutionOptions().getFlags().contains(ViewExecutionFlags.TRIGGER_CYCLE_ON_MARKET_DATA_CHANGED)) {
      return;
    }
    
    CompiledViewDefinitionWithGraphsImpl compiledView = getCachedCompiledViewDefinition();
    if (compiledView == null) {
      return;
    }
    //Since this happens for every tick, for every job, we need to use the quick call here 
    if (compiledView.hasAnyMarketDataRequirements(values)) {
      marketDataChanged();
    }
  }

}
