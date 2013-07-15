/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.util.async.Cancelable;

/**
 * A job and all of its tails that can be dispatched to calculation nodes via an invoker. If the job fails (the local JobInvoker instance will make sure a failure is reported when loss of a remote
 * node is detected) it may be rescheduled. If the maximum number of reschedules is exceeded, or the job is failed on the same node twice, it becomes a "watched" job.
 * <p>
 * A watched job may not have a tail. If a job has a tail, the tails are extracted and rewritten, along with the root job, to use shared cache for values. The original job may then be resubmitted to
 * the dispatcher with a job result receiver that will submit the tail job(s) on original job completion. When those tails have complete the original result receiver will be notified.
 * <p>
 * When a watched job fails, if it contains a single job item that item can be reported to a blacklist maintainer. If the job contains multiple items it is split into two parts, rewritten to use the
 * shared cache for values. The first will then be resubmitted with a job result receiver that will submit the second part of the job on first part completion. When the second part of the job
 * completed the original callback will be notified.
 */
/* package */abstract class DispatchableJob implements JobInvocationReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(DispatchableJob.class);

  protected static final class CancelHandle implements Cancelable {

    private volatile DispatchableJob[] _jobs;

    private CancelHandle(final DispatchableJob job) {
      _jobs = new DispatchableJob[] {job };
    }

    public synchronized void addCallback(final DispatchableJob job) {
      DispatchableJob[] jobs = new DispatchableJob[_jobs.length + 1];
      System.arraycopy(_jobs, 0, jobs, 1, _jobs.length);
      jobs[0] = job;
      _jobs = jobs;
    }

    public synchronized void removeCallback(final DispatchableJob job) {
      final DispatchableJob[] oldJobs = _jobs;
      for (int i = 0; i < oldJobs.length; i++) {
        if (job == oldJobs[i]) {
          final DispatchableJob[] newJobs = new DispatchableJob[oldJobs.length - 1];
          if (i > 0) {
            System.arraycopy(oldJobs, 0, newJobs, 0, i);
          }
          if (i < oldJobs.length - 1) {
            System.arraycopy(oldJobs, i + 1, newJobs, i, oldJobs.length - (i + 1));
          }
          _jobs = newJobs;
          break;
        }
      }
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      DispatchableJob[] jobs = _jobs;
      if (jobs.length == 0) {
        // Nothing here to cancel - probably already finished
        return false;
      } else {
        boolean result = true;
        for (DispatchableJob job : jobs) {
          result &= job.cancel(mayInterruptIfRunning);
        }
        return result;
      }
    }

  }

  private final JobDispatcher _dispatcher;
  private final CalculationJob _job;
  private final AtomicBoolean _completed = new AtomicBoolean(false);
  private final long _jobCreationTime;
  private final CapabilityRequirements _capabilityRequirements;
  private final AtomicReference<DispatchableJobTimeout> _timeout = new AtomicReference<DispatchableJobTimeout>();
  private final CancelHandle _cancelHandle;

  /**
   * Creates a new dispatchable job for submission to the invokers.
   * 
   * @param dispatcher the parent dispatcher that manages the invokers
   * @param job the root job to send
   */
  protected DispatchableJob(final JobDispatcher dispatcher, final CalculationJob job) {
    _dispatcher = dispatcher;
    _job = job;
    _jobCreationTime = System.nanoTime();
    _capabilityRequirements = dispatcher.getCapabilityRequirementsProvider().getCapabilityRequirements(job);
    _cancelHandle = new CancelHandle(this);
  }

  /**
   * Creates a new dispatchable job for submission to the invokers.
   * 
   * @param creater the source job with the parameters for construction
   * @param job the root job to send
   */
  protected DispatchableJob(final DispatchableJob creater, final CalculationJob job) {
    _dispatcher = creater.getDispatcher();
    _job = job;
    _jobCreationTime = System.nanoTime();
    _capabilityRequirements = _dispatcher.getCapabilityRequirementsProvider().getCapabilityRequirements(job);
    _cancelHandle = creater.getCancelHandle();
    _cancelHandle.addCallback(this);
  }

  protected long getDurationNanos() {
    return System.nanoTime() - getJobCreationTime();
  }

  protected CalculationJob getJob() {
    return _job;
  }

  public JobDispatcher getDispatcher() {
    return _dispatcher;
  }

  protected CancelHandle getCancelHandle() {
    return _cancelHandle;
  }

  protected abstract JobResultReceiver getResultReceiver(CalculationJobResult result);

  protected abstract boolean isLastResult();

  @Override
  public void jobCompleted(final CalculationJobResult result) {
    final JobResultReceiver resultReceiver = getResultReceiver(result);
    if (resultReceiver == null) {
      s_logger.warn("Job {} completed on node {} but is not currently pending", this, result.getComputeNodeId());
      // Note the above warning can happen if we've been retried
      extendTimeout(getDispatcher().getMaxJobExecutionTime(), true);
      return;
    }
    if (isLastResult()) {
      // This is the last one to complete. Note that if the last few jobs complete concurrently, both may execute this code.
      _completed.set(true);
      cancelTimeout(DispatchableJobTimeout.FINISHED);
    } else {
      // Others are still running, but we can extend the timeout period
      extendTimeout(getDispatcher().getMaxJobExecutionTime(), true);
    }
    s_logger.info("Job {} completed on node {}", this, result.getComputeNodeId());
    resultReceiver.resultReceived(result);
    final long durationNanos = getDurationNanos();
    s_logger.debug("Reported time = {}ms, non-executing job time = {}ms", (double) result.getDuration() / 1000000d, ((double) durationNanos - (double) result.getDuration()) / 1000000d);
    if (getDispatcher().getStatisticsGatherer() != null) {
      final int size = result.getResultItems().size();
      getDispatcher().getStatisticsGatherer().jobCompleted(result.getComputeNodeId(), size, result.getDuration(), getDurationNanos());
    }
  }

  protected abstract DispatchableJob prepareRetryJob(JobInvoker jobInvoker);

  @Override
  public void jobFailed(final JobInvoker jobInvoker, final String computeNodeId, final Exception exception) {
    s_logger.warn("Job {} failed, {}", this, (exception != null) ? exception.getMessage() : "no exception passed");
    if (_completed.getAndSet(true) == false) {
      cancelTimeout(null);
      DispatchableJob retry = prepareRetryJob(jobInvoker);
      if (retry != null) {
        if (retry == this) {
          // We're scheduled to retry
          _completed.set(false);
          getDispatcher().dispatchJobImpl(this);
        } else {
          // A replacement has been constructed
          getCancelHandle().removeCallback(this);
          cancelTimeout(DispatchableJobTimeout.REPLACED);
          _completed.set(false);
          getDispatcher().dispatchJobImpl(retry);
        }
      } else {
        _completed.set(false);
        abort(exception, "node invocation error");
      }
      if (getDispatcher().getStatisticsGatherer() != null) {
        getDispatcher().getStatisticsGatherer().jobFailed(computeNodeId, getDurationNanos());
      }
    } else {
      s_logger.warn("Job {} failed on node {} but we've already completed, aborted or failed", this, computeNodeId);
    }
  }

  protected void notifyFailure(final CalculationJob job, final CalculationJobResultItem failure, final JobResultReceiver resultReceiver) {
    final int size = job.getJobItems().size();
    final List<CalculationJobResultItem> failureItems = new ArrayList<CalculationJobResultItem>(size);
    for (int i = 0; i < size; i++) {
      failureItems.add(failure);
    }
    final CalculationJobResult jobResult = new CalculationJobResult(job.getSpecification(), getDurationNanos(), failureItems, getDispatcher().getJobFailureNodeId());
    resultReceiver.resultReceived(jobResult);
  }

  protected abstract void fail(final CalculationJob job, final CalculationJobResultItem failure);

  public void abort(Exception exception, final String alternativeError) {
    if (_completed.getAndSet(true) == false) {
      cancelTimeout(DispatchableJobTimeout.FINISHED);
      if (exception == null) {
        s_logger.error("Aborted job {} with {}", this, alternativeError);
        exception = new OpenGammaRuntimeException(alternativeError);
        exception.fillInStackTrace();
      } else {
        s_logger.error("Aborted job {} with {}", this, exception);
      }
      // REVIEW jonathan 2012-11-01 -- where's the 'real' execution log here?
      MutableExecutionLog executionLog = new MutableExecutionLog(ExecutionLogMode.INDICATORS);
      executionLog.setException(exception);
      fail(getJob(), CalculationJobResultItemBuilder.of(executionLog).toResultItem());
    } else {
      s_logger.warn("Job {} aborted but we've already completed or aborted from another node", this);
    }
  }

  protected abstract boolean isAlive(final JobInvoker jobInvoker);

  public void timeout(final long timeAccrued, final JobInvoker jobInvoker) {
    s_logger.debug("Timeout on {}, {}ms accrued", jobInvoker.getInvokerId(), timeAccrued);
    final long remaining = getDispatcher().getMaxJobExecutionTime() - timeAccrued;
    if (remaining > 0) {
      if (isAlive(jobInvoker)) {
        s_logger.debug("Invoker {} reports job {} still alive", jobInvoker.getInvokerId(), this);
        extendTimeout(remaining, false);
        return;
      } else {
        s_logger.warn("Invoker {} reports job {} failure", jobInvoker.getInvokerId(), this);
        jobFailed(jobInvoker, "node on " + jobInvoker.getInvokerId(), new OpenGammaRuntimeException("Node reported failure at " + timeAccrued + "ms keepalive"));
      }
    } else {
      jobFailed(jobInvoker, "node on " + jobInvoker.getInvokerId(), new OpenGammaRuntimeException("Invocation limit of " + getDispatcher().getMaxJobExecutionTime() + "ms exceeded"));
    }
  }

  private DispatchableJobTimeout cancelTimeout(final DispatchableJobTimeout flagState) {
    final DispatchableJobTimeout timeout = _timeout.getAndSet(flagState);
    if (timeout == null) {
      s_logger.debug("Job {} timeout transition null to {}", this, flagState);
    } else if (timeout.isActive()) {
      s_logger.debug("Cancelling timeout for {} on transition to {}", this, flagState);
      timeout.cancel();
      return timeout;
    } else {
      s_logger.debug("Job {} timeout transition from {} to {}", new Object[] {this, timeout, flagState });
    }
    return null;
  }

  private void extendTimeout(final long remainingMillis, final boolean resetTimeAccrued) {
    final DispatchableJobTimeout timeout = _timeout.get();
    if ((timeout != null) && timeout.isActive()) {
      s_logger.debug("Extending timeout on job {}", getJob().getSpecification().getJobId());
      timeout.extend(Math.min(remainingMillis, getDispatcher().getMaxJobExecutionTime()), resetTimeAccrued);
    }
  }

  private CapabilityRequirements getRequirements() {
    return _capabilityRequirements;
  }

  public long getJobCreationTime() {
    return _jobCreationTime;
  }

  public boolean canRunOn(final JobInvoker jobInvoker) {
    // TODO: aiwg -- not happy with this approach for capabilities
    return getRequirements().satisfiedBy(jobInvoker.getCapabilities());
  }

  protected abstract void cancel(final JobInvoker jobInvoker);

  private boolean cancel(boolean mayInterruptIfRunning) {
    s_logger.info("Cancelling job {}", this);
    while (_completed.getAndSet(true) != false) {
      Thread.yield();
      final DispatchableJobTimeout timeout = _timeout.get();
      if (timeout.isActive()) {
        s_logger.debug("Job {} - currently failing, cancelling or aborting", this);
      } else {
        if (timeout == DispatchableJobTimeout.CANCELLED) {
          s_logger.info("Job {} already cancelled", this);
          return true;
        } else {
          s_logger.info("Job {} - can't cancel: {}", this, timeout);
          return false;
        }
      }
    }
    final DispatchableJobTimeout timeout = cancelTimeout(DispatchableJobTimeout.CANCELLED);
    if (timeout != null) {
      final JobInvoker invoker = timeout.getInvoker();
      if (invoker != null) {
        cancel(invoker);
      }
    }
    return true;
  }

  public boolean isCompleted() {
    return _completed.get();
  }

  public boolean runOn(final JobInvoker jobInvoker) {
    if (!jobInvoker.invoke(getJob(), this)) {
      return false;
    }
    DispatchableJobTimeout timeout = new DispatchableJobTimeout(this, jobInvoker);
    if (_timeout.compareAndSet(null, timeout)) {
      s_logger.debug("Timeout set for job {}", this);
    } else {
      timeout.cancel();
      if (s_logger.isDebugEnabled()) {
        timeout = _timeout.get();
        s_logger.debug("Timeout {} for job {}", timeout, this);
      }
    }
    return true;
  }

}
