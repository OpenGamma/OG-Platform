/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.TerminatableJob;

/**
 * The primary job which will recalculate a {@link View}'s output data.
 */
public class ViewRecalculationJob extends TerminatableJob {
  private static final Logger s_logger = LoggerFactory.getLogger(ViewRecalculationJob.class);
  
  private static final long NANOS_PER_MILLISECOND = 1000000;
  
  private final View _view;
  private double _numExecutions;
  private SingleComputationCycle _previousCycle;
  
  /** Nanoseconds */
  private long _nextDeltaRecalculationTime = Long.MIN_VALUE;
  
  /** Nanoseconds */
  private long _nextFullRecalculationTime = Long.MIN_VALUE;
  
  /**
   * Nanoseconds
   */
  private double _totalTime;
  
  public ViewRecalculationJob(View view) {
    if (view == null) {
      throw new NullPointerException("Must provide a backing view.");
    }
    _view = view;
  }

  /**
   * @return the view
   */
  public View getView() {
    return _view;
  }
  
  private void setNextDeltaRecalculationTime(long currentNanos) {
    if (getView().getDefinition().getDeltaRecalculationPeriod() == null) {
      _nextDeltaRecalculationTime = Long.MAX_VALUE;
    } else {
      _nextDeltaRecalculationTime = currentNanos + NANOS_PER_MILLISECOND * getView().getDefinition().getDeltaRecalculationPeriod();
    }
  }
  
  private void setNextFullRecalculationTime(long currentNanos) {
    if (getView().getDefinition().getFullRecalculationPeriod() == null) {
      _nextFullRecalculationTime = Long.MAX_VALUE;
    } else {
      _nextFullRecalculationTime = currentNanos + NANOS_PER_MILLISECOND * getView().getDefinition().getFullRecalculationPeriod();
    }
  }
  
  @Override
  protected void runOneCycle() {
    
    boolean doFullRecalc = false;
    long currentTime = System.nanoTime();
    if (currentTime >= _nextFullRecalculationTime) {
      doFullRecalc = true;
      setNextFullRecalculationTime(currentTime);
      setNextDeltaRecalculationTime(currentTime); // make sure delta and full are in sync
    }
    
    boolean doDeltaRecalc = false;
    if (currentTime >= _nextDeltaRecalculationTime) {
      doDeltaRecalc = true;
      setNextDeltaRecalculationTime(currentTime);
    }
    
    if (!doFullRecalc && !doDeltaRecalc) {
      long nextTimeToCheck = Math.min(_nextDeltaRecalculationTime, _nextFullRecalculationTime);
      long delay = nextTimeToCheck - currentTime;
      delay = Math.max(0, delay);
      delay /= NANOS_PER_MILLISECOND;
      delay += 1; // round up a bit to make sure it'll be enough
      s_logger.info("Waiting for {} ms", delay);
      try {
        synchronized (this) {
          // This could wait until end of time if both minimum and full recalc periods are null.
          // In this case, you need to call liveDataChanged() to wake it up
          wait(delay);
        }
      } catch (InterruptedException e) {
        Thread.interrupted();
        s_logger.info("Interrupted while delaying. Continuing operation.");
      }

      return; // will get back to runOneCycle() soon enough
    }
    
    long snapshotTime = getView().getProcessingContext().getLiveDataSnapshotProvider().snapshot();
    
    SingleComputationCycle cycle = getView().createCycle(snapshotTime);
    
    cycle.prepareInputs();

    if (doFullRecalc) {  
      s_logger.info("Performing full recalculation");
    } else {
      s_logger.info("Performing delta recalculation");
      cycle.computeDelta(_previousCycle);
    }
    
    cycle.executePlans();
    cycle.populateResultModel();
    
    long endNanoTime = System.nanoTime();
    long delta = endNanoTime - cycle.getStartTime();
    _totalTime += delta;
    _numExecutions += 1.0;
    s_logger.info("Last latency was {} ms, Average latency is {} ms", delta / NANOS_PER_MILLISECOND, (_totalTime / _numExecutions) / NANOS_PER_MILLISECOND);
    getView().recalculationPerformed(cycle.getResultModel());
    
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
    s_logger.debug("Live Data changed");
    synchronized (this) {
      notifyAll();
    }
  }
}
