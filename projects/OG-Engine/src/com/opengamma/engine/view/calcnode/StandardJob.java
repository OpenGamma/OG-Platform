/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard job with an execution tail that can be retried in the event of failure.
 * <p>
 * See {@link DispatchableJob} for a description of standard and "watched" jobs.
 */
/* package */final class StandardJob extends DispatchableJob {

  private static final Logger s_logger = LoggerFactory.getLogger(StandardJob.class);

  private final ConcurrentMap<CalculationJobSpecification, JobResultReceiver> _resultReceivers;
  private Set<String> _excludeJobInvoker;
  private int _rescheduled;

  private static List<CalculationJob> getAllJobs(CalculationJob job, List<CalculationJob> jobs) {
    if (jobs == null) {
      jobs = new LinkedList<CalculationJob>();
    }
    jobs.add(job);
    if (job.getTail() != null) {
      for (CalculationJob tail : job.getTail()) {
        getAllJobs(tail, jobs);
      }
    }
    return jobs;
  }

  /**
   * Creates a new job for submission to the invokers.
   * 
   * @param dispatcher the parent dispatcher that manages the invokers
   * @param job the root job to send
   * @param resultReceiver the callback for when the job and it's tail completes
   */
  public StandardJob(final JobDispatcher dispatcher, final CalculationJob job, final JobResultReceiver resultReceiver) {
    super(dispatcher, job);
    _resultReceivers = new ConcurrentHashMap<CalculationJobSpecification, JobResultReceiver>();
    final List<CalculationJob> jobs = getAllJobs(job, null);
    for (CalculationJob jobref : jobs) {
      _resultReceivers.put(jobref.getSpecification(), resultReceiver);
    }
  }

  @Override
  protected JobResultReceiver getResultReceiver(final CalculationJobResult result) {
    return _resultReceivers.remove(result.getSpecification());
  }

  @Override
  protected boolean isLastResult() {
    return _resultReceivers.isEmpty();
  }

  @Override
  protected boolean isAbort(final JobInvoker jobInvoker) {
    if ((_excludeJobInvoker != null) && _excludeJobInvoker.contains(jobInvoker.getInvokerId())) {
      // TODO: [PLAT-2211] turn into a watched job
      return true;
    } else {
      _rescheduled++;
      if (_rescheduled >= getDispatcher().getMaxJobAttempts()) {
        // TODO: [PLAT-2211] turn into a watched job
        return true;
      } else {
        s_logger.info("Retrying job {} (attempt {})", this, _rescheduled);
        if (_excludeJobInvoker == null) {
          _excludeJobInvoker = new HashSet<String>();
        }
        _excludeJobInvoker.add(jobInvoker.getInvokerId());
        return false;
      }
    }
  }

  @Override
  protected void retry() {
    getDispatcher().dispatchJobImpl(this);
  }

  @Override
  protected void fail(final CalculationJob job, final CalculationJobResultItem failure) {
    final JobResultReceiver resultReceiver = _resultReceivers.remove(job.getSpecification());
    if (resultReceiver != null) {
      notifyFailure(job, failure, resultReceiver);
    } else {
      s_logger.warn("Job {} already completed at propogation of failure", job.getSpecification().getJobId());
      // This can happen if the root job timed out but things had started to complete
    }
    if (job.getTail() != null) {
      for (CalculationJob tail : job.getTail()) {
        fail(tail, failure);
      }
    }
  }
  
  @Override
  protected boolean isAlive(final JobInvoker jobInvoker) {
    return jobInvoker.isAlive(_resultReceivers.keySet());
  }

  @Override
  protected void cancel(final JobInvoker jobInvoker) {
    jobInvoker.cancel(_resultReceivers.keySet());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder('S').append(getJob().getSpecification().getJobId());
    if (_rescheduled > 0) {
      sb.append('(').append(_rescheduled).append(')');
    }
    return sb.toString();
  }

}
