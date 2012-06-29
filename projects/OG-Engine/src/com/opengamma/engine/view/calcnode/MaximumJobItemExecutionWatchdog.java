/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Watchdog for dealing with job items that run for too long. Detected at this level, recovery actions are limited.
 */
public class MaximumJobItemExecutionWatchdog {

  private static final Logger s_logger = LoggerFactory.getLogger(MaximumJobItemExecutionWatchdog.class);

  /**
   * Callback for the action to take when the watchdog is triggered.
   */
  public interface Action {

    /**
     * The time limit for job item execution has been exceeded.
     * 
     * @param jobItem the job item involved
     * @param thread the thread that is running the job item
     */
    void jobItemExecutionLimitExceeded(CalculationJobItem jobItem, Thread thread);

  }

  private long _maxExecutionTime;
  private Action _action = new Action() {
    @Override
    public void jobItemExecutionLimitExceeded(final CalculationJobItem jobItem, final Thread thread) {
      s_logger.error("Job item execution limit exceeded on {} by {}", jobItem, thread);
      thread.interrupt();
    }
  };

  public void setMaximumJobItemExecutionTime(final long milliseconds) {
    _maxExecutionTime = milliseconds;
  }

  public long getMaximumJobItemExecutionTime() {
    return _maxExecutionTime;
  }

  public void setTimeoutAction(final Action action) {
    ArgumentChecker.notNull(action, "action");
    _action = action;
  }

  public Action getTimeoutAction() {
    return _action;
  }

  /**
   * The calling thread is about to start executing the job item. This call must be paired with a call to {@link #jobExecutionStopped} when the thread has finished, before the time limit elapses, to
   * avoid the watchdog triggering.
   * 
   * @param jobItem the item
   */
  protected void jobExecutionStarted(final CalculationJobItem jobItem) {
    if (_maxExecutionTime > 0) {
      s_logger.debug("TODO: job execution started on {}", jobItem);
      // TODO: update a data structure
    }
  }

  /**
   * The calling thread has finished executing the job item from the previous call to {@link #jobExecutionStarted}.
   */
  protected void jobExecutionStopped() {
    if (_maxExecutionTime > 0) {
      s_logger.debug("TODO: job execution stopped");
      // TODO: update a data structure
    }
  }

}