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
import com.opengamma.engine.view.compilation.ViewDefinitionCompiler;
import com.opengamma.engine.view.compilation.ViewEvaluationModel;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
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
  private final ViewCycleManager _cycleManager;
  
  private double _numExecutions;
  private ViewCycleReferenceImpl _previousCycleReference;
  
  // Nanoseconds - all 0 initially so that a full computation will run 
  private long _eligibleForDeltaComputationFromNanos;
  private long _deltaComputationRequiredByNanos; 
  private long _eligibleForFullComputationFromNanos;
  private long _fullComputationRequiredByNanos;
  private int _deltasSinceLastFull;
  
  private ViewEvaluationModel _viewEvaluationModel;
  private final Set<ValueRequirement> _liveDataSubscriptions = new HashSet<ValueRequirement>();
  private final Set<ValueRequirement> _pendingSubscriptions = Collections.newSetFromMap(new ConcurrentHashMap<ValueRequirement, Boolean>());
  private CountDownLatch _pendingSubscriptionLatch;
  
  private volatile boolean _wakeOnLiveDataChanged;
  private volatile boolean _liveDataChanged;
  
  /**
   * Nanoseconds
   */
  private double _totalTimeNanos;
  
  public ViewComputationJob(ViewProcessImpl viewProcess, ViewExecutionOptions executionOptions,
      ViewProcessContext processContext, ViewCycleManager cycleManager) {
    ArgumentChecker.notNull(viewProcess, "viewProcess");
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    ArgumentChecker.notNull(processContext, "processContext");
    ArgumentChecker.notNull(cycleManager, "cycleManager");
    _viewProcess = viewProcess;
    _executionOptions = executionOptions;
    _processContext = processContext;
    _cycleManager = cycleManager;
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
  
  private ViewCycleManager getCycleManager() {
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
    long currentTime = System.nanoTime();
    boolean doFullRecalc = false;
    boolean doDeltaRecalc = false;
    
    synchronized (this) {
      // The actual computation cycle must occur outside of the synchronized block to allow calls to liveDataChanged()
      // to return as quickly as possible.
      
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
      
      if (!doFullRecalc && !doDeltaRecalc) {
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
  
        // Will get back to runOneCycle() straight away unless the job has been terminated
        return;
      }
    }

    // Set the times for the next computation cycle. These might have passed by the time this cycle completes, in which
    // case another cycle will run straight away.
    updateComputationTimes(currentTime, !doFullRecalc);
    
    ViewCycleExecutionOptions executionOptions = getExecutionOptions().getExecutionSequence().getNext();
    s_logger.debug("Next cycle execution options: {}", executionOptions);
    
    ViewCycleReferenceImpl cycleReference = createCycle(executionOptions);
    ViewCycleInternal cycle = cycleReference.getCycle();
    
    if (doFullRecalc) {
      s_logger.info("Performing full computation");
      _deltasSinceLastFull = 0;
    } else {
      s_logger.info("Performing delta computation");
      _deltasSinceLastFull++;
    }
    
    try {
      ViewCycleInternal deltaCycle = doFullRecalc ? null : _previousCycleReference.getCycle();
      cycle.execute(deltaCycle);
    } catch (InterruptedException e) {
      Thread.interrupted();
      // In reality this means that the job has been terminated, and it will end as soon as we return from this method.
      // In case the thread has been interrupted without terminating the job, we tidy everything up as if the
      // interrupted cycle never happened so that deltas will be calculated from the previous cycle. 
      cycleReference.release();
      s_logger.info("Interrupted while executing a computation cycle. No results will be output from this cycle.");
      return;
    }
    
    long duration = cycle.getDurationNanos();
    _totalTimeNanos += duration;
    _numExecutions += 1.0;
    s_logger.info("Last latency was {} ms, Average latency is {} ms", duration / NANOS_PER_MILLISECOND, (_totalTimeNanos / _numExecutions) / NANOS_PER_MILLISECOND);
    
    // Don't push the results through if we've been terminated, since another computation job could be running already
    // and the fact that we've been terminated means the view is no longer interested in the result. Just die quietly.
    if (isTerminated()) {
      cycleReference.release();
      return;
    }
    
    getViewProcess().cycleCompleted(cycle);
    if (getExecutionOptions().getExecutionSequence().isEmpty()) {
      s_logger.debug("Computation job completed for view process {}", getViewProcess());
      getViewProcess().jobCompleted();
      terminate();
    }
    
    if (_previousCycleReference != null) {
      _previousCycleReference.release();
    }
    _previousCycleReference = cycleReference;
  }
    
  @Override
  protected void postRunCycle() {
    if (_previousCycleReference != null) {
      _previousCycleReference.release();
    }
    removeLiveDataSubscriptions();
    _viewEvaluationModel = null;
  }
  
  public void liveDataChanged() {
    // REVIEW jonathan 2010-10-04 -- this synchronisation is necessary, but it feels very heavyweight for
    // high-frequency live data. See how it goes, but we could take into account the recalc periods and apply a
    // heuristic (e.g. only wake up due to live data if max - min < e, for some e) which tries to see whether it's
    // worth doing all this.
    
    s_logger.debug("Live Data changed");
    synchronized (this) {
      _liveDataChanged = true;
      if (!_wakeOnLiveDataChanged) {
        return;
      }
      notifyAll();
    }
  }
  
  //-------------------------------------------------------------------------
  private ViewCycleReferenceImpl createCycle(ViewCycleExecutionOptions executionOptions) {
    UniqueIdentifier cycleId = getViewProcess().generateCycleId();
    ViewEvaluationModel evaluationModel = getOrCompileViewEvaluationModel(executionOptions.getValuationTime());
    return getCycleManager().createViewCycle(cycleId, getViewProcess().getUniqueId(), getProcessContext(),
        evaluationModel, executionOptions);
  }
  
  private ViewEvaluationModel getOrCompileViewEvaluationModel(Instant valuationTime) {
    long functionInitId = getProcessContext().getFunctionCompilationService().getFunctionCompilationContext().getFunctionInitId();
    ViewEvaluationModel viewEvaluationModel = getViewEvaluationModel();
    if (viewEvaluationModel != null && viewEvaluationModel.isValidFor(valuationTime) && functionInitId == viewEvaluationModel.getFunctionInitId()) {
      // Existing cached model is valid (an optimisation for the common case of similar, increasing evaluation times)
      return viewEvaluationModel;
    }
    
    viewEvaluationModel = ViewDefinitionCompiler.compile(getViewProcess().getDefinition(), getProcessContext().asCompilationServices(), valuationTime);
    setViewEvaluationModel(viewEvaluationModel);
    
    // Notify the view that a (re)compilation has taken place before going on to do any time-consuming work.
    // This might contain enough for clients to e.g. render an empty grid in which results will later appear. 
    getViewProcess().viewCompiled(viewEvaluationModel);
    
    setLiveDataSubscriptions(viewEvaluationModel.getAllLiveDataRequirements().keySet());
    return viewEvaluationModel;
  }
  
  /**
   * Gets the cached view evaluation model which may be re-used in subsequent computation cycles.
   * <p>
   * External visibility for tests.
   * 
   * @return the view evaluation model
   */
  public ViewEvaluationModel getViewEvaluationModel() {
    return _viewEvaluationModel;
  }
  
  /**
   * Replaces the cached view evaluation model.
   * <p>
   * External visibility for tests.
   * 
   * @param viewEvaluationModel  the view evaluation model
   */
  public void setViewEvaluationModel(ViewEvaluationModel viewEvaluationModel) {
    _viewEvaluationModel = viewEvaluationModel;
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
        s_logger.warn("Timed out after {} ms waiting for live data subscriptions to be made. The live data snapshot" +
          "used in the computation cycle could be incomplete.", LIVE_DATA_SUBSCRIPTION_TIMEOUT_MILLIS);
        _pendingSubscriptions.clear();
        _pendingSubscriptionLatch = null;
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
    ValueSpecification valueSpecification = new ValueSpecification(value, LiveDataSourcingFunction.UNIQUE_ID);
    ViewEvaluationModel viewEvaluationModel = _viewEvaluationModel;
    if (viewEvaluationModel == null) {
      return;
    }
    Map<ValueRequirement, ValueSpecification> liveDataRequirements = viewEvaluationModel.getAllLiveDataRequirements();
    if (liveDataRequirements.containsKey(valueSpecification)) {
      liveDataChanged();
    }
  }

}
