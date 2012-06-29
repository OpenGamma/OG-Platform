/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
/* package */final class DispatchableJob implements JobInvocationReceiver, Cancelable {

  private static final Logger s_logger = LoggerFactory.getLogger(DispatchableJob.class);

  private final JobDispatcher _dispatcher;
  private final CalculationJob _rootJob;
  private final ConcurrentMap<CalculationJobSpecification, JobResultReceiver> _resultReceivers;
  private final AtomicBoolean _completed = new AtomicBoolean(false);
  private final long _jobCreationTime;
  private final CapabilityRequirements _capabilityRequirements;
  private final AtomicReference<DispatchableJobTimeout> _timeout = new AtomicReference<DispatchableJobTimeout>();
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

  public DispatchableJob(final JobDispatcher dispatcher, final CalculationJob job, final JobResultReceiver resultReceiver) {
    _dispatcher = dispatcher;
    _rootJob = job;
    _resultReceivers = new ConcurrentHashMap<CalculationJobSpecification, JobResultReceiver>();
    final List<CalculationJob> jobs = getAllJobs(job, null);
    for (CalculationJob jobref : jobs) {
      _resultReceivers.put(jobref.getSpecification(), resultReceiver);
    }
    _jobCreationTime = System.nanoTime();
    _capabilityRequirements = dispatcher.getCapabilityRequirementsProvider().getCapabilityRequirements(jobs);
  }

  private long getDurationNanos() {
    return System.nanoTime() - getJobCreationTime();
  }

  private CalculationJob getJob() {
    return _rootJob;
  }

  public JobDispatcher getDispatcher() {
    return _dispatcher;
  }

  @Override
  public void jobCompleted(final CalculationJobResult result) {
    final JobResultReceiver resultReceiver = _resultReceivers.remove(result.getSpecification());
    if (resultReceiver == null) {
      s_logger.warn("Job {} completed on node {} but is not currently pending", result.getSpecification().getJobId(), result.getComputeNodeId());
      // Note the above warning can happen if we've been retried
      extendTimeout(getDispatcher().getMaxJobExecutionTime(), true);
      return;
    }
    if (_resultReceivers.isEmpty()) {
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

  @Override
  public void jobFailed(final JobInvoker jobInvoker, final String computeNodeId, final Exception exception) {
    s_logger.warn("Job {} failed, {}", getJob().getSpecification().getJobId(), (exception != null) ? exception.getMessage() : "no exception passed");
    if (_completed.getAndSet(true) == false) {
      cancelTimeout(null);
      // TODO: [PLAT-2211] check if the job is watched and partition again to isolate the failing job item
      if ((_excludeJobInvoker != null) && _excludeJobInvoker.contains(jobInvoker.getInvokerId())) {
        _completed.set(false);
        abort(exception, "duplicate invoker failure from node " + computeNodeId);
        // TODO: [PLAT-2211] the job is now a watched job and should be partitioned to isolate the failing job item
      } else {
        _rescheduled++;
        if (_rescheduled >= getDispatcher().getMaxJobAttempts()) {
          _completed.set(false);
          abort(exception, "internal node error");
          // TODO: [PLAT-2211] the job is now a watched job and should be partitioned to isolate the failing job item
        } else {
          s_logger.info("Retrying job {} (attempt {})", this, _rescheduled);
          if (_excludeJobInvoker == null) {
            _excludeJobInvoker = new HashSet<String>();
          }
          _excludeJobInvoker.add(jobInvoker.getInvokerId());
          _completed.set(false);
          getDispatcher().dispatchJobImpl(this);
        }
      }
      if (getDispatcher().getStatisticsGatherer() != null) {
        getDispatcher().getStatisticsGatherer().jobFailed(computeNodeId, getDurationNanos());
      }
    } else {
      s_logger.warn("Job {} failed on node {} but we've already completed, aborted or failed", this, computeNodeId);
    }
  }

  private void failTree(final CalculationJob job, final CalculationJobResultItem failure) {
    final JobResultReceiver resultReceiver = _resultReceivers.remove(job.getSpecification());
    if (resultReceiver != null) {
      final int size = job.getJobItems().size();
      final List<CalculationJobResultItem> failureItems = new ArrayList<CalculationJobResultItem>(size);
      for (int i = 0; i < size; i++) {
        failureItems.add(failure);
      }
      final CalculationJobResult jobResult = new CalculationJobResult(job.getSpecification(), getDurationNanos(), failureItems, getDispatcher().getJobFailureNodeId());
      resultReceiver.resultReceived(jobResult);
    } else {
      s_logger.warn("Job {} already completed at propogation of failure", job.getSpecification().getJobId());
      // This can happen if the root job timed out but things had started to complete
    }
    if (job.getTail() != null) {
      for (CalculationJob tail : job.getTail()) {
        failTree(tail, failure);
      }
    }
  }

  public void abort(Exception exception, final String alternativeError) {
    s_logger.error("Aborted job {}", this);
    if (_completed.getAndSet(true) == false) {
      cancelTimeout(DispatchableJobTimeout.FINISHED);
      if (exception == null) {
        s_logger.error("Aborted job {} with {}", this, alternativeError);
        exception = new OpenGammaRuntimeException(alternativeError);
        exception.fillInStackTrace();
      }
      failTree(getJob(), CalculationJobResultItem.failure(exception));
    } else {
      s_logger.warn("Job {} aborted but we've already completed or aborted from another node", this);
    }
  }

  public void timeout(final long timeAccrued, final JobInvoker jobInvoker) {
    s_logger.debug("Timeout on {}, {}ms accrued", jobInvoker.getInvokerId(), timeAccrued);
    final long remaining = getDispatcher().getMaxJobExecutionTime() - timeAccrued;
    if (remaining > 0) {
      if (jobInvoker.isAlive(_resultReceivers.keySet())) {
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
        invoker.cancel(_resultReceivers.keySet());
      }
    }
    return true;
  }

  public boolean isCompleted() {
    return _completed.get();
  }

  @Override
  public String toString() {
    return "J" + getJob().getSpecification().getJobId() + "(" + _rescheduled + ")";
  }

  public boolean runOn(final JobInvoker jobInvoker) {
    return jobInvoker.invoke(getJob(), this);
  }

}
