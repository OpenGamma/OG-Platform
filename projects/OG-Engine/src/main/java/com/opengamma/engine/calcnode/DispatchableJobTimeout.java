/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;

/* package */class DispatchableJobTimeout implements Runnable {

  private static final class Placeholder extends DispatchableJobTimeout {

    private final String _label;

    public Placeholder(final String label) {
      _label = label;
    }

    @Override
    public boolean isActive() {
      return false;
    }

    @Override
    public String toString() {
      return _label;
    }
  }

  /**
   * Timeout placeholder for a job that has finished.
   */
  public static final DispatchableJobTimeout FINISHED = new Placeholder("finished");
  /**
   * Timeout placeholder for a job that has been canceled.
   */
  public static final DispatchableJobTimeout CANCELLED = new Placeholder("cancelled");
  /**
   * Timeout placeholder for a job that has been substituted by a rewritten replacement in order to isolate a failure.
   */
  public static final DispatchableJobTimeout REPLACED = new Placeholder("replaced");

  private DispatchableJob _dispatchJob;
  private JobInvoker _jobInvoker;
  private RunnableScheduledFuture<?> _future;
  private long _timeAccrued;

  private DispatchableJobTimeout() {
  }

  public boolean isActive() {
    return true;
  }

  public DispatchableJobTimeout(final DispatchableJob dispatchJob, final JobInvoker jobInvoker) {
    _dispatchJob = dispatchJob;
    _jobInvoker = jobInvoker;
    long timeoutMillis = Math.min(dispatchJob.getDispatcher().getMaxJobExecutionTime(), dispatchJob.getDispatcher().getMaxJobExecutionTimeQuery());
    _timeAccrued = timeoutMillis;
    setTimeout(timeoutMillis);
  }

  private void setTimeout(final long timeoutMillis) {
    if (timeoutMillis > 0) {
      _future = (RunnableScheduledFuture<?>) _dispatchJob.getDispatcher().getJobTimeoutExecutor().schedule(this, timeoutMillis, TimeUnit.MILLISECONDS);
    } else {
      _future = null;
    }
  }

  @Override
  public synchronized void run() {
    _dispatchJob.timeout(_timeAccrued, _jobInvoker);
  }

  public synchronized void cancel() {
    if (_future != null) {
      _dispatchJob.getDispatcher().getJobTimeoutExecutor().remove(_future);
      _future = null;
    }
  }

  public synchronized void extend(final long timeoutMillis, final boolean resetAccruedTime) {
    if (_future != null) {
      _dispatchJob.getDispatcher().getJobTimeoutExecutor().remove(_future);
      if (resetAccruedTime) {
        _timeAccrued = timeoutMillis;
      } else {
        _timeAccrued += timeoutMillis;
      }
      setTimeout(timeoutMillis);
    }
  }

  public synchronized JobInvoker getInvoker() {
    return _jobInvoker;
  }

  @Override
  public String toString() {
    return "running";
  }

}
