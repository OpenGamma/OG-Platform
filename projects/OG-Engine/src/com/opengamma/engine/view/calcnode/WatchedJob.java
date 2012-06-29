/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A job that is suspected of containing a job item that causes failure and must be reported to a blacklist maintainer.
 * <p>
 * See {@link DispatchableJob} for a description of a "watched" job.
 */
/* package */final class WatchedJob extends DispatchableJob {

  private static final Logger s_logger = LoggerFactory.getLogger(WatchedJob.class);

  private final JobResultReceiver _resultReceiver;

  /**
   * Creates a new watched job for submission to the invokers.
   * 
   * @param dispatcher the parent dispatcher that manages the invokers
   * @param job the job to send
   * @param resultReceiver the callback for when the job completes
   */
  public WatchedJob(final JobDispatcher dispatcher, final CalculationJob job, final JobResultReceiver resultReceiver) {
    super(dispatcher, job);
    _resultReceiver = resultReceiver;
  }

  @Override
  protected JobResultReceiver getResultReceiver(final CalculationJobResult result) {
    return _resultReceiver;
  }

  @Override
  protected boolean isLastResult() {
    return true;
  }

  @Override
  protected boolean isAbort(final JobInvoker jobInvoker) {
    if (getJob().getJobItems().size() <= 1) {
      // Report the failed job item to the blacklist maintainer
      getDispatcher().getFunctionBlacklistMaintainer().failedJobItem(getJob().getJobItems().get(0));
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void retry() {
    // TODO: Partition the job into to two fragments
    throw new UnsupportedOperationException("PLAT-2211");
  }

  @Override
  protected void fail(final CalculationJob job, final CalculationJobResultItem failure) {
    notifyFailure(job, failure, _resultReceiver);
  }

  @Override
  protected boolean isAlive(final JobInvoker jobInvoker) {
    return jobInvoker.isAlive(getJob().getSpecification());
  }

  @Override
  protected void cancel(final JobInvoker jobInvoker) {
    jobInvoker.cancel(getJob().getSpecification());
  }

  @Override
  public String toString() {
    return "W" + getJob().getSpecification().getJobId();
  }

  public boolean runOn(final JobInvoker jobInvoker) {
    return jobInvoker.invoke(getJob(), this);
  }

}
