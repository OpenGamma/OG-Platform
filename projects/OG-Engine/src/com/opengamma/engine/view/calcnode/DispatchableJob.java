/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
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
/* package */abstract class DispatchableJob implements JobInvocationReceiver, Cancelable {

  private static final Logger s_logger = LoggerFactory.getLogger(DispatchableJob.class);

  private final JobDispatcher _dispatcher;
  private final CalculationJob _job;
  private final AtomicBoolean _completed = new AtomicBoolean(false);
  private final long _jobCreationTime;
  private final CapabilityRequirements _capabilityRequirements;
  private final AtomicReference<DispatchableJobTimeout> _timeout = new AtomicReference<DispatchableJobTimeout>();

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

  protected abstract JobResultReceiver getResultReceiver(CalculationJobResult result);

  protected abstract boolean isLastResult();

  @Override
  public void jobCompleted(final CalculationJobResult result) {
    final JobResultReceiver resultReceiver = getResultReceiver(result);
    if (resultReceiver == null) {
      s_logger.warn("Job {} completed on node {} but is not currently pending", result.getSpecification().getJobId(), result.getComputeNodeId());
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
    s_logger.info("Job {} completed on node {}", result.getSpecification().getJobId(), result.getComputeNodeId());
    resultReceiver.resultReceived(result);
    final long durationNanos = getDurationNanos();
    s_logger.debug("Reported time = {}ms, non-executing job time = {}ms", (double) result.getDuration() / 1000000d, ((double) durationNanos - (double) result.getDuration()) / 1000000d);
    if (getDispatcher().getStatisticsGatherer() != null) {
      final int size = result.getResultItems().size();
      getDispatcher().getStatisticsGatherer().jobCompleted(result.getComputeNodeId(), size, result.getDuration(), getDurationNanos());
    }
  }

  protected abstract boolean isAbort(JobInvoker jobInvoker);

  protected abstract void retry();

  @Override
  public void jobFailed(final JobInvoker jobInvoker, final String computeNodeId, final Exception exception) {
    s_logger.warn("Job {} failed, {}", getJob().getSpecification().getJobId(), (exception != null) ? exception.getMessage() : "no exception passed");
    if (_completed.getAndSet(true) == false) {
      cancelTimeout(null);
      boolean abort = isAbort(jobInvoker);
      _completed.set(false);
      if (abort) {
        abort(exception, "internal node error");
      } else {
        retry();
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
    s_logger.error("Aborted job {}", this);
    if (_completed.getAndSet(true) == false) {
      cancelTimeout(DispatchableJobTimeout.FINISHED);
      if (exception == null) {
        s_logger.error("Aborted job {} with {}", this, alternativeError);
        exception = new OpenGammaRuntimeException(alternativeError);
        exception.fillInStackTrace();
      }
      fail(getJob(), CalculationJobResultItem.failure(exception));
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

  public void setTimeout(final JobInvoker jobInvoker) {
    DispatchableJobTimeout timeout = new DispatchableJobTimeout(this, jobInvoker);
    if (_timeout.compareAndSet(null, timeout)) {
      s_logger.debug("Timeout set for job {}", this);
    } else {
      timeout.cancel();
      timeout = _timeout.get();
      if (timeout == DispatchableJobTimeout.FINISHED) {
        s_logger.debug("Job {} already completed", this);
      } else if (timeout == DispatchableJobTimeout.CANCELLED) {
        s_logger.debug("Job {} cancelled", this);
      } else {
        s_logger.debug("Job {} timeout already set", this);
      }
    }
  }

  private DispatchableJobTimeout cancelTimeout(final DispatchableJobTimeout flagState) {
    final DispatchableJobTimeout timeout = _timeout.getAndSet(flagState);
    if (timeout == null) {
      s_logger.debug("Cancel timeout for {} - no timeout set", this);
    } else if (timeout == DispatchableJobTimeout.FINISHED) {
      s_logger.debug("Cancel timeout for {} - job finished", this);
    } else if (timeout == DispatchableJobTimeout.CANCELLED) {
      s_logger.debug("Cancel timeout for {} - job cancelled", this);
    } else {
      s_logger.debug("Cancelling timeout for {}", this);
      timeout.cancel();
      return timeout;
    }
    return null;
  }

  private void extendTimeout(final long remainingMillis, final boolean resetTimeAccrued) {
    final DispatchableJobTimeout timeout = _timeout.get();
    if ((timeout != null) && (timeout != DispatchableJobTimeout.FINISHED) && (timeout != DispatchableJobTimeout.CANCELLED)) {
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

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    s_logger.info("Cancelling job {}", this);
    while (_completed.getAndSet(true) != false) {
      Thread.yield();
      if (_timeout.get() == DispatchableJobTimeout.FINISHED) {
        s_logger.warn("Can't cancel job {} - already finished", this);
        return false;
      } else if (_timeout.get() == DispatchableJobTimeout.CANCELLED) {
        s_logger.info("Job {} already cancelled", this);
        return true;
      } else {
        s_logger.debug("Job {} - currently failing, cancelling or aborting", this);
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
    return jobInvoker.invoke(getJob(), this);
  }

}
