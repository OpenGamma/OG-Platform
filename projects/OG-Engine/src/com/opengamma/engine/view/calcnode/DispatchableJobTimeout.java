/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;

/* package */final class DispatchableJobTimeout implements Runnable {

  public static final DispatchableJobTimeout FINISHED = new DispatchableJobTimeout();
  public static final DispatchableJobTimeout CANCELLED = new DispatchableJobTimeout();

  private DispatchableJob _dispatchJob;
  private JobInvoker _jobInvoker;
  private RunnableScheduledFuture<?> _future;
  private long _timeAccrued;

  private DispatchableJobTimeout() {
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

}
