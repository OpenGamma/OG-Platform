/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewInternal;
import com.opengamma.util.TerminatableJob;

/**
 * The primary job which will recalculate a {@link View}'s output data.
 */
public class ViewRecalculationJob extends TerminatableJob {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewRecalculationJob.class);
  
  private static final long NANOS_PER_MILLISECOND = 1000000;
  
  private final ViewInternal _view;
  private double _numExecutions;
  private SingleComputationCycle _previousCycle;
  
  // Nanoseconds - all 0 initially so that a full computation will run 
  private long _eligibleForDeltaCalculationFromNanos;
  private long _deltaCalculationRequiredByNanos; 
  private long _eligibleForFullCalculationFromNanos;
  private long _fullCalculationRequiredByNanos;
  
  private volatile boolean _wakeOnLiveDataChanged;
  private volatile boolean _liveDataChanged;
  
  /**
   * Nanoseconds
   */
  private double _totalTimeNanos;
  
  public ViewRecalculationJob(ViewInternal view) {
    if (view == null) {
      throw new NullPointerException("Must provide a backing view.");
    }
    _view = view;
  }

  /**
   * @return the view
   */
  public ViewInternal getView() {
    return _view;
  }
  
  private void updateRecalculationTimes(long currentNanos, boolean deltaOnly) {
    _eligibleForDeltaCalculationFromNanos = getUpdatedTime(currentNanos, getView().getDefinition().getMinDeltaCalculationPeriod(), 0);
    _deltaCalculationRequiredByNanos = getUpdatedTime(currentNanos, getView().getDefinition().getMaxDeltaCalculationPeriod(), Long.MAX_VALUE);
    
    if (!deltaOnly) {
      _eligibleForFullCalculationFromNanos = getUpdatedTime(currentNanos, getView().getDefinition().getMinFullCalculationPeriod(), 0);
      _fullCalculationRequiredByNanos = getUpdatedTime(currentNanos, getView().getDefinition().getMaxFullCalculationPeriod(), Long.MAX_VALUE);
    }
  }
  
  private long getUpdatedTime(long currentNanos, Long calculationPeriod, long nullEquivalent) {
    if (calculationPeriod == null) {
      return nullEquivalent;
    }

    return currentNanos + NANOS_PER_MILLISECOND * calculationPeriod;
  }
  
  /**
   * Determines whether to run, and runs if required, a single computation cycle using the following rules:
   * 
   * <ul>
   *   <li>A calculation can only be triggered if the relevant minimum calculation period has passed since the start of
   *   the previous cycle.
   *   <li>A calculation will be forced if the relevant maximum calculation period has passed since the start of the
   *   previous cycle.
   *   <li>A full calculation is preferred over a delta calculation if both are possible.
   *   <li>Performing a full calculation also updates the times to the next delta calculation; i.e. a full calculation
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
      
      if (currentTime >= _fullCalculationRequiredByNanos) {
        s_logger.debug("Forcing a full calculation");
        doFullRecalc = true;
      } else if (currentTime >= _deltaCalculationRequiredByNanos) {
        s_logger.debug("Forcing a delta calculation");
        doDeltaRecalc = true;
      }
      
      if (_liveDataChanged) {
        s_logger.debug("Live data has changed");
        if (currentTime >= _eligibleForFullCalculationFromNanos) {
          // Do (or upgrade to) a full calculation because we're eligible for one
          s_logger.debug("Performing a full calculation for the live data change");
          doFullRecalc = true;
          _liveDataChanged = false;
        } else if (currentTime >= _eligibleForDeltaCalculationFromNanos) {
          // Do a delta calculation
          s_logger.debug("Performing a delta calculation for the live data change");
          doDeltaRecalc = true;
          _liveDataChanged = false;
        }
      }
      
      if (!doFullRecalc && !doDeltaRecalc) {
        // Going to sleep
        
        long minWakeUpTime = Math.min(_eligibleForDeltaCalculationFromNanos, _eligibleForFullCalculationFromNanos);
        long wakeUpTime;
        if (_liveDataChanged) {
          s_logger.debug("Sleeping until eligible to perform a recalculation");
          
          // Live data has arrived but we decided not to perform a computation cycle; this must be because we're not
          // eligible for one right now. We'll do one as soon as we are.
          wakeUpTime = minWakeUpTime;
          
          // No amount of live data can make us eligible for a computation cycle any sooner.
          _wakeOnLiveDataChanged = false;
        } else {
          s_logger.debug("Sleeping until forced to perform a recalculation");
          
          // Only *plan* to wake up when we really have to
          wakeUpTime = Math.min(_deltaCalculationRequiredByNanos, _fullCalculationRequiredByNanos);
          
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
    updateRecalculationTimes(currentTime, !doFullRecalc);
    
    long snapshotTime = getView().getProcessingContext().getLiveDataSnapshotProvider().snapshot();
    SingleComputationCycle cycle = getView().createCycle(snapshotTime);
    cycle.prepareInputs();

    if (doFullRecalc) {  
      s_logger.info("Performing full recalculation");
    } else {
      s_logger.info("Performing delta recalculation");
      cycle.computeDelta(_previousCycle);
    }
    
    try {
      cycle.executePlans();
    } catch (InterruptedException e) {
      Thread.interrupted();
      // In reality this means that the job has been terminated, and it will end as soon as we return from this method.
      // In case the thread has been interrupted without terminating the job, we tidy everything up as if the
      // interrupted cycle never happened so that deltas will be calculated from the previous cycle. 
      cycle.releaseResources();
      s_logger.info("Interrupted while executing a computation cycle. No results will be output from this cycle.");
      return;
    }
    cycle.populateResultModel();
    
    long duration = cycle.getDurationNanos();
    _totalTimeNanos += duration;
    _numExecutions += 1.0;
    s_logger.info("Last latency was {} ms, Average latency is {} ms", duration / NANOS_PER_MILLISECOND, (_totalTimeNanos / _numExecutions) / NANOS_PER_MILLISECOND);
    
    // Don't push the results through if we've been terminated, since another recalculation thread could be running
    // already and the fact that we've been terminated means the view is no longer interested in the result. Just die
    // quietly.
    if (!isTerminated()) {
      getView().recalculationPerformed(cycle.getResultModel());
    }
    
    if (_previousCycle != null) {
      _previousCycle.releaseResources();
    }
    _previousCycle = cycle;
  }
    
  @Override
  protected void postRunCycle() {
    if (_previousCycle != null) {
      _previousCycle.releaseResources();
    }
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

}
