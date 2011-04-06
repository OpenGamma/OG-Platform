/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.livedata.LiveDataSnapshotListener;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewProcessContext;
import com.opengamma.engine.view.ViewProcessImpl;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionImpl;
import com.opengamma.engine.view.compilation.ViewDefinitionCompiler;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.monitor.OperationTimer;

/**
 * The job which schedules and executes computation cycles for a view process.
 */
public class ViewComputationJob extends TerminatableJob implements LiveDataSnapshotListener {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewComputationJob.class);
  
  private static final long LIVE_DATA_SUBSCRIPTION_TIMEOUT_MILLIS = 10000;
  private static final long NANOS_PER_MILLISECOND = 1000000;

  private final ViewProcessImpl _viewProcess;
  private final ViewExecutionOptions _executionOptions;
  private final ViewProcessContext _processContext;
  private final ViewCycleManagerImpl _cycleManager;
  private final boolean _executeCycles;
  
  private double _numExecutions;
  private ViewCycleReferenceImpl _previousCycleReference;
  
  // Nanoseconds - all 0 initially so that a full computation will run 
  private long _eligibleForDeltaComputationFromNanos;
  private long _deltaComputationRequiredByNanos; 
  private long _eligibleForFullComputationFromNanos;
  private long _fullComputationRequiredByNanos;
  private int _deltasSinceLastFull;
  
  private CompiledViewDefinitionImpl _latestCompiledViewDefinition;
  private final Set<ValueRequirement> _liveDataSubscriptions = new HashSet<ValueRequirement>();
  private final Set<ValueRequirement> _pendingSubscriptions = Collections.newSetFromMap(new ConcurrentHashMap<ValueRequirement, Boolean>());
  private CountDownLatch _pendingSubscriptionLatch;
  
  private volatile boolean _wakeOnLiveDataChanged;
  private volatile boolean _liveDataChanged;
  
  private enum ViewCycleType { FULL, DELTA, NONE }
  
  /**
   * Nanoseconds
   */
  private double _totalTimeNanos;
  
  public ViewComputationJob(ViewProcessImpl viewProcess, ViewExecutionOptions executionOptions,
      ViewProcessContext processContext, ViewCycleManagerImpl cycleManager) {
    ArgumentChecker.notNull(viewProcess, "viewProcess");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ArgumentChecker.notNull(processContext, "processContext");
    ArgumentChecker.notNull(cycleManager, "cycleManager");
    _viewProcess = viewProcess;
    _executionOptions = executionOptions;
    _processContext = processContext;
    _cycleManager = cycleManager;

    _executeCycles = !getExecutionOptions().isCompileOnly();

    processContext.getLiveDataSnapshotProvider().addListener(this);
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
  
  private ViewCycleManagerImpl getCycleManager() {
    return _cycleManager;
  }
  
  private void updateComputationTimes(long currentNanos, boolean deltaOnly) {
    _eligibleForDeltaComputationFromNanos = getUpdatedTime(currentNanos, getViewProcess().getDefinition().getMinDeltaCalculationPeriod(), 0);
    _deltaComputationRequiredByNanos = getUpdatedTime(currentNanos, getViewProcess().getDefinition().getMaxDeltaCalculationPeriod(), Long.MAX_VALUE);
    
    if (!deltaOnly) {
      _eligibleForFullComputationFromNanos = getUpdatedTime(currentNanos, getViewProcess().getDefinition().getMinFullCalculationPeriod(), 0);
      _fullComputationRequiredByNanos = getUpdatedTime(currentNanos, getViewProcess().getDefinition().getMaxFullCalculationPeriod(), Long.MAX_VALUE);
    }
  }
  
  private long getUpdatedTime(long currentNanos, Long computationPeriod, long nullEquivalent) {
    if (computationPeriod == null) {
      return nullEquivalent;
    }

    return currentNanos + NANOS_PER_MILLISECOND * computationPeriod;
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
    
    ViewCycleType cycleType = waitForNextCycle();
    if (cycleType == ViewCycleType.NONE) {
      // Will get back to runOneCycle() straight away unless the job has been terminated
      return;
    }
    
    ViewCycleExecutionOptions executionOptions = null;
    try {
      if (!getExecutionOptions().getExecutionSequence().isEmpty()) {
        executionOptions = getExecutionOptions().getExecutionSequence().getNext();
        s_logger.debug("Next cycle execution options: {}", executionOptions);
      }
      if (executionOptions == null) {
        s_logger.info("No more view cycle execution options");
        jobCompleted();
        return;
      }
    } catch (Exception e) {
      s_logger.error("Error obtaining next view cycle execution options from sequence for view process " + getViewProcess(), e);
      return;
    }
    
    ViewCycleReferenceImpl cycleReference;
    try {
      cycleReference = createCycle(executionOptions);
    } catch (Exception e) {
      s_logger.error("Error creating next view cycle for view process " + getViewProcess(), e);
      return;
    }
    
    if (_executeCycles) {
      try {
        executeViewCycle(cycleType, cycleReference);
      } catch (Exception e) {
        // Execution failed
        s_logger.error("View cycle execution failed for view process " + getViewProcess(), e);
        cycleReference.release();
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
      try {
        getViewProcess().cycleCompleted(cycleReference.getCycle());
      } catch (Exception e) {
        s_logger.error("Error notifying view process " + getViewProcess() + " of view cycle completion", e);
      }
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
  
  private synchronized ViewCycleType waitForNextCycle() {
    long currentTime = System.nanoTime();
    
    boolean doFullRecalc = false;
    boolean doDeltaRecalc = false;
    
    if (requireFullCycleNext(currentTime)) {
      s_logger.debug("Forcing a full computation");
      doFullRecalc = true;
    } else if (requireDeltaCycleNext(currentTime)) {
      s_logger.debug("Forcing a delta computation");
      doDeltaRecalc = true;
    }
    
    if (_liveDataChanged) {
      s_logger.debug("Live data has changed");
      if (currentTime >= _eligibleForFullComputationFromNanos) {
        // Do (or upgrade to) a full computation because we're eligible for one
        s_logger.debug("Performing a full computation for the live data change");
        doFullRecalc = true;
        _liveDataChanged = false;
      } else if (currentTime >= _eligibleForDeltaComputationFromNanos) {
        // Do a delta computation
        s_logger.debug("Performing a delta computation for the live data change");
        doDeltaRecalc = true;
        _liveDataChanged = false;
      }
    }
    
    if (doFullRecalc || doDeltaRecalc) {
      // Set the times for the next computation cycle. These might have passed by the time this cycle completes, in
      // which case another cycle will run straight away.
      updateComputationTimes(currentTime, !doFullRecalc);
      
      return doFullRecalc ? ViewCycleType.FULL : ViewCycleType.DELTA;
    }
    
    // Going to sleep
    long minWakeUpTime = Math.min(_eligibleForDeltaComputationFromNanos, _eligibleForFullComputationFromNanos);
    long wakeUpTime;
    if (_liveDataChanged) {
      s_logger.debug("Sleeping until eligible to perform the next computation cycle");
      
      // Live data has arrived but we decided not to perform a computation cycle; this must be because we're not
      // eligible for one right now. We'll do one as soon as we are.
      wakeUpTime = minWakeUpTime;
      
      // No amount of live data can make us eligible for a computation cycle any sooner.
      _wakeOnLiveDataChanged = false;
    } else {
      s_logger.debug("Sleeping until forced to perform the next computation cycle");
      
      // Only *plan* to wake up when we really have to
      wakeUpTime = Math.min(_deltaComputationRequiredByNanos, _fullComputationRequiredByNanos);
      
      // If we're not scheduled to wake up until after we're eligible for a computation cycle, then allow us to be
      // woken sooner by live data changing. The benefit of this is when min=max, meaning live data will never wake
      // us up.
      _wakeOnLiveDataChanged = wakeUpTime > minWakeUpTime;
    }
    
    long sleepTime = wakeUpTime - currentTime;
    sleepTime = Math.max(0, sleepTime);
    sleepTime /= NANOS_PER_MILLISECOND;
    sleepTime += 1; // round up a bit to make sure it'll be enough
    s_logger.debug("Waiting for {} ms", sleepTime);
    try {
      // This could wait until end of time if both full and delta maximum recalc periods are null.
      // In this case, only liveDataChanged() will wake it up
      wait(sleepTime);
    } catch (InterruptedException e) {
      // We support interruption as a signal that we have been terminated. If we're interrupted without having been
      // terminated, we'll just return to this method and go back to sleep.
      Thread.interrupted();
      s_logger.info("Interrupted while delaying. Continuing operation.");
    }

    return ViewCycleType.NONE;
  }
  
  private void executeViewCycle(ViewCycleType cycleType, ViewCycleReferenceImpl cycleReference) throws Exception {
    ViewCycleInternal deltaCycle;
    if (cycleType == ViewCycleType.FULL) {
      s_logger.info("Performing full computation");
      _deltasSinceLastFull = 0;
      deltaCycle = null;
    } else {
      s_logger.info("Performing delta computation");
      _deltasSinceLastFull++;
      deltaCycle = _previousCycleReference.getCycle();
    }
    
    try {
      cycleReference.getCycle().execute(deltaCycle);
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
    
    long duration = cycleReference.getCycle().getDurationNanos();
    _totalTimeNanos += duration;
    _numExecutions += 1.0;
    s_logger.info("Last latency was {} ms, Average latency is {} ms", duration / NANOS_PER_MILLISECOND, (_totalTimeNanos / _numExecutions) / NANOS_PER_MILLISECOND);
  }
    
  @Override
  protected void postRunCycle() {
    if (_previousCycleReference != null) {
      _previousCycleReference.release();
    }
    _processContext.getLiveDataSnapshotProvider().removeListener(this);
    removeLiveDataSubscriptions();
    _latestCompiledViewDefinition = null;
  }
  
  private void jobCompleted() {
    s_logger.info("Computation job completed for view process {}", getViewProcess());
    try {
      getViewProcess().jobCompleted();
    } catch (Exception e) {
      s_logger.error("Error notifying view process " + getViewProcess() + " of computation job completion", e);
    }
    terminate();
  }
  
  public synchronized void liveDataChanged() {
    // REVIEW jonathan 2010-10-04 -- this synchronisation is necessary, but it feels very heavyweight for
    // high-frequency live data. See how it goes, but we could take into account the recalc periods and apply a
    // heuristic (e.g. only wake up due to live data if max - min < e, for some e) which tries to see whether it's
    // worth doing all this.
    
    s_logger.debug("Live Data changed");
    _liveDataChanged = true;
    if (!_wakeOnLiveDataChanged) {
      return;
    }
    notifyAll();
  }
  
  //-------------------------------------------------------------------------
  private ViewCycleReferenceImpl createCycle(ViewCycleExecutionOptions executionOptions) {
    UniqueIdentifier cycleId = getViewProcess().generateCycleId();
    CompiledViewDefinitionImpl compiledView = getCompiledViewDefinition(executionOptions.getValuationTime());
    return getCycleManager().createViewCycle(cycleId, getViewProcess().getUniqueId(), getProcessContext(),
        compiledView, executionOptions);
  }
  
  private CompiledViewDefinitionImpl getCompiledViewDefinition(Instant valuationTime) {
    long functionInitId = getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getFunctionInitId();
    CompiledViewDefinitionImpl compiledView = getLatestCompiledViewDefinition();
    if (compiledView != null && compiledView.isValidFor(valuationTime) && functionInitId == compiledView.getFunctionInitId()) {
      // Existing cached model is valid (an optimisation for the common case of similar, increasing evaluation times)
      return compiledView;
    }
    
    compiledView = ViewDefinitionCompiler.compile(getViewProcess().getDefinition(), getProcessContext().asCompilationServices(), valuationTime);
    setLatestCompiledViewDefinition(compiledView);
    
    // Notify the view that a (re)compilation has taken place before going on to do any time-consuming work.
    // This might contain enough for clients to e.g. render an empty grid in which results will later appear. 
    getViewProcess().viewCompiled(compiledView);
    
    // Update the live data subscriptions to whatever is now required, ensuring the computation cycle can find the
    // required input data when it is executed.
    setLiveDataSubscriptions(compiledView.getLiveDataRequirements().keySet());
    return compiledView;
  }
  
  /**
   * Gets the cached compiled view definition which may be re-used in subsequent computation cycles.
   * <p>
   * External visibility for tests.
   * 
   * @return the latest compiled view definition, or {@code null} if no cycles have yet completed
   */
  public CompiledViewDefinitionImpl getLatestCompiledViewDefinition() {
    return _latestCompiledViewDefinition;
  }
  
  /**
   * Replaces the cached compiled view definition.
   * <p>
   * External visibility for tests.
   * 
   * @param latestCompiledViewDefinition  the compiled view definition, may be {@code null}
   */
  public void setLatestCompiledViewDefinition(CompiledViewDefinitionImpl latestCompiledViewDefinition) {
    _latestCompiledViewDefinition = latestCompiledViewDefinition;
  }
  
  private boolean requireFullCycleNext(long currentTime) {
    if (currentTime >= _fullComputationRequiredByNanos) {
      return true;
    }
    if (getExecutionOptions().getMaxSuccessiveDeltaCycles() == null) {
      return false;
    }
    return getExecutionOptions().getMaxSuccessiveDeltaCycles() <= _deltasSinceLastFull;
  }
  
  private boolean requireDeltaCycleNext(long currentTime) {
    if (getExecutionOptions().isRunAsFastAsPossible()) {
      // Run as fast as possible on delta cycles, with full cycles as required by the view definition and execution options
      return true;
    }
    return currentTime >= _deltaComputationRequiredByNanos;
  }

  //-------------------------------------------------------------------------
  private void setLiveDataSubscriptions(final Set<ValueRequirement> requiredSubscriptions) {
    final Set<ValueRequirement> currentSubscriptions = _liveDataSubscriptions;
    final Set<ValueRequirement> unusedLiveData = Sets.difference(currentSubscriptions, requiredSubscriptions);
    if (!unusedLiveData.isEmpty()) {
      s_logger.debug("{} unused live data subscriptions: {}", unusedLiveData.size(), unusedLiveData);
      removeLiveDataSubscriptions(unusedLiveData);
    }
    final Set<ValueRequirement> newLiveData = Sets.difference(requiredSubscriptions, currentSubscriptions);
    if (!newLiveData.isEmpty()) {
      s_logger.debug("{} new live data requirements: {}", newLiveData.size(), newLiveData);
      addLiveDataSubscriptions(newLiveData);
    }
  }

  private void addLiveDataSubscriptions(final Set<ValueRequirement> requiredSubscriptions) {
    final OperationTimer timer = new OperationTimer(s_logger, "Adding {} live data subscriptions", requiredSubscriptions.size());
    _pendingSubscriptions.addAll(requiredSubscriptions);
    _pendingSubscriptionLatch = new CountDownLatch(requiredSubscriptions.size());
    getProcessContext().getLiveDataSnapshotProvider().addSubscription(getViewProcess().getDefinition().getLiveDataUser(), requiredSubscriptions);
    _liveDataSubscriptions.addAll(requiredSubscriptions);
    try {
      if (!_pendingSubscriptionLatch.await(LIVE_DATA_SUBSCRIPTION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
        long remainingCount = _pendingSubscriptionLatch.getCount();
        s_logger.warn("Timed out after {} ms waiting for live data subscriptions to be made. The live data snapshot " +
          "used in the computation cycle could be incomplete. Still waiting for {} out of {} live data subscriptions",
          new Object[] {LIVE_DATA_SUBSCRIPTION_TIMEOUT_MILLIS, remainingCount, _liveDataSubscriptions.size()});
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
  
  private void removeLiveDataSubscriptions() {
    removeLiveDataSubscriptions(_liveDataSubscriptions);
  }

  private void removeLiveDataSubscriptions(final Set<ValueRequirement> unusedSubscriptions) {
    final OperationTimer timer = new OperationTimer(s_logger, "Removing {} live data subscriptions", unusedSubscriptions.size());
    // [ENG-251] TODO getLiveDataSnapshotProvider().removeSubscription(getDefinition().getLiveDataUser(), requiredLiveData);
    _liveDataSubscriptions.removeAll(unusedSubscriptions);
    timer.finished();
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void subscriptionSucceeded(ValueRequirement requirement) {
    // REVIEW jonathan 2011-01-07
    // Can't tell in general whether this subscription message was relating to a subscription that we made or one that
    // a concurrent user of the LiveDataSnapshotProvider made.
    s_logger.debug("Subscription succeeded: {}", requirement);
    removePendingSubscription(requirement);
  }

  @Override
  public void subscriptionFailed(ValueRequirement requirement, String msg) {
    s_logger.warn("Live data subscription to {} failed. This live data may be missing from computation cycles.", requirement);
    removePendingSubscription(requirement);
  }

  @Override
  public void subscriptionStopped(ValueRequirement requirement) {   
  }

  @Override
  public void valueChanged(ValueRequirement value) {
    if (!getExecutionOptions().isLiveDataTriggerEnabled()) {
      return;
    }
    
    ValueSpecification valueSpecification = new ValueSpecification(value, LiveDataSourcingFunction.UNIQUE_ID);
    CompiledViewDefinitionImpl compiledView = getLatestCompiledViewDefinition();
    if (compiledView == null) {
      return;
    }
    Map<ValueRequirement, ValueSpecification> liveDataRequirements = compiledView.getLiveDataRequirements();
    if (liveDataRequirements.containsKey(valueSpecification)) {
      liveDataChanged();
    }
  }

}
