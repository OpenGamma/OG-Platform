/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.calcnode.stats.CalculationNodeStatisticsGatherer;
import com.opengamma.engine.view.calcnode.stats.DiscardingStatisticsGatherer;
import com.opengamma.util.ArgumentChecker;

/**
 * Manages a set of JobInvokers and dispatches jobs to them for execution.
 */
public class JobDispatcher implements JobInvokerRegister {

  private static final Logger s_logger = LoggerFactory.getLogger(JobDispatcher.class);
  /* package */static final int DEFAULT_MAX_JOB_ATTEMPTS = 3;
  /* package */static final String DEFAULT_JOB_FAILURE_NODE_ID = "NOT EXECUTED";

  private static List<CalculationJob> getAllJobs(CalculationJob job, List<CalculationJob> jobs) {
    jobs = new LinkedList<CalculationJob>();
    while (job != null) {
      jobs.add(job);
      job = job.getTail();
    }
    return jobs;
  }

  private final class DispatchJob implements JobInvocationReceiver {

    private final CalculationJob _rootJob;
    private final ConcurrentMap<CalculationJobSpecification, JobResultReceiver> _resultReceivers;
    private final AtomicReference<JobResultReceiver> _resultReceiver;
    private final long _jobCreationTime;
    private final CapabilityRequirements _capabilityRequirements;
    private Set<JobInvoker> _excludeJobInvoker;
    private int _rescheduled;
    private Future<?> _timeout;

    private DispatchJob(final CalculationJob job, final JobResultReceiver resultReceiver) {
      _rootJob = job;
      _resultReceivers = new ConcurrentHashMap<CalculationJobSpecification, JobResultReceiver>();
      _resultReceiver = new AtomicReference<JobResultReceiver>(resultReceiver);
      final List<CalculationJob> jobs = getAllJobs(job, null);
      for (CalculationJob jobref : jobs) {
        _resultReceivers.put(jobref.getSpecification(), resultReceiver);
      }
      _jobCreationTime = System.nanoTime();
      _capabilityRequirements = getCapabilityRequirementsProvider().getCapabilityRequirements(jobs);
    }

    private long getDurationNanos() {
      return System.nanoTime() - getJobCreationTime();
    }

    private CalculationJob getJob() {
      return _rootJob;
    }

    @Override
    public void jobCompleted(final CalculationJobResult result) {
      JobResultReceiver resultReceiver = _resultReceivers.remove(result.getSpecification());
      if (resultReceiver == null) {
        s_logger.warn("Job {} completed on node {} but is not currently pending", result.getSpecification().getJobId(), result.getComputeNodeId());
        // Note the above warning can happen if we've been retried and tail jobs are being re-executed
        return;
      }
      if (_resultReceivers.isEmpty()) {
        // This is the last one to complete
        cancelTimeout();
        resultReceiver = _resultReceiver.getAndSet(null);
        if (resultReceiver == null) {
          // The whole batch has failed or aborted however
          s_logger.warn("Job {} completed on node {} but batch has already failed or aborted", result.getSpecification().getJobId(), result.getComputeNodeId());
          return;
        }
      }
      s_logger.debug("Job {} completed on node {}", result.getSpecification().getJobId(), result.getComputeNodeId());
      resultReceiver.resultReceived(result);
      final long durationNanos = getDurationNanos();
      s_logger.debug("Reported time = {}ms, non-executing job time = {}ms", (double) result.getDuration() / 1000000d, ((double) durationNanos - (double) result.getDuration()) / 1000000d);
      if (getStatisticsGatherer() != null) {
        final int size = result.getResultItems().size();
        // TODO [ENG-201] Report a better cost metric than the number of items; should we push the metric as part of the dispatch?
        getStatisticsGatherer().jobCompleted(result.getComputeNodeId(), size, size, result.getDuration(), getDurationNanos());
      }
    }

    @Override
    public void jobFailed(final JobInvoker jobInvoker, final String computeNodeId, final Exception exception) {
      cancelTimeout();
      final JobResultReceiver resultReceiver = _resultReceiver.getAndSet(null);
      if (resultReceiver != null) {
        s_logger.debug("Job {} failed, {}", getJob().getSpecification().getJobId(), (exception != null) ? exception.getMessage() : "no exception passed");
        if ((_excludeJobInvoker != null) && _excludeJobInvoker.contains(jobInvoker)) {
          _resultReceiver.set(resultReceiver);
          s_logger.debug("Duplicate invoker failure from node {}", computeNodeId);
        } else {
          _rescheduled++;
          if (_rescheduled >= getMaxJobAttempts()) {
            _resultReceiver.set(resultReceiver);
            jobAbort(exception, "internal node error");
          } else {
            s_logger.info("Retrying job {} (attempt {})", getJob().getSpecification().getJobId(), _rescheduled);
            if (_excludeJobInvoker == null) {
              _excludeJobInvoker = new HashSet<JobInvoker>();
            }
            _excludeJobInvoker.add(jobInvoker);
            _resultReceiver.set(resultReceiver);
            dispatchJobImpl(this);
          }
        }
        if (getStatisticsGatherer() != null) {
          getStatisticsGatherer().jobFailed(computeNodeId, getDurationNanos());
        }
      } else {
        s_logger.warn("Job {} failed on node {} but we've already completed, aborted or failed", getJob().getSpecification().getJobId(), computeNodeId);
      }
    }

    private void jobAbort(Exception exception, final String alternativeError) {
      cancelTimeout();
      final JobResultReceiver resultReceiver = _resultReceiver.getAndSet(null);
      if (resultReceiver != null) {
        s_logger.warn("Failed job {} after {} attempts", getJob().getSpecification().getJobId(), _rescheduled);
        if (exception == null) {
          s_logger.error("Failed job {} with {}", getJob().getSpecification().getJobId(), alternativeError);
          exception = new OpenGammaRuntimeException(alternativeError);
          exception.fillInStackTrace();
        }
        final List<CalculationJobResultItem> failureItems = new ArrayList<CalculationJobResultItem>(getJob().getJobItems().size());
        for (CalculationJobItem item : getJob().getJobItems()) {
          failureItems.add(new CalculationJobResultItem(item, exception));
        }
        final CalculationJobResult jobResult = new CalculationJobResult(getJob().getSpecification(), getDurationNanos(), failureItems, getJobFailureNodeId());
        resultReceiver.resultReceived(jobResult);
      } else {
        s_logger.warn("Job {} aborted but we've already completed or aborted from another node", getJob().getSpecification().getJobId());
      }
    }

    private synchronized void setTimeout(final JobInvoker jobInvoker) {
      if (_maxJobExecutionTime > 0) {
        _timeout = _jobTimeoutExecutor.schedule(new Runnable() {
          @Override
          public void run() {
            synchronized (JobDispatcher.this) {
              _timeout = null;
            }
            jobFailed(jobInvoker, "node on " + jobInvoker.toString(), new OpenGammaRuntimeException("Invocation limit of " + _maxJobExecutionTime + "ms exceeded"));
          }
        }, _maxJobExecutionTime, TimeUnit.MILLISECONDS);
      }
    }

    private synchronized void cancelTimeout() {
      if (_timeout != null) {
        _timeout.cancel(false);
        _timeout = null;
      }
    }

    private CapabilityRequirements getRequirements() {
      return _capabilityRequirements;
    }

    private long getJobCreationTime() {
      return _jobCreationTime;
    }

    private boolean canRunOn(final JobInvoker jobInvoker) {
      if (_excludeJobInvoker != null) {
        if (_excludeJobInvoker.contains(jobInvoker)) {
          return false;
        }
      }
      return getRequirements().satisfiedBy(jobInvoker.getCapabilities());
    }

  }

  private final Queue<DispatchJob> _pending = new LinkedList<DispatchJob>();
  private final Queue<JobInvoker> _invokers = new ConcurrentLinkedQueue<JobInvoker>();

  private int _maxJobAttempts = DEFAULT_MAX_JOB_ATTEMPTS;
  private String _jobFailureNodeId = DEFAULT_JOB_FAILURE_NODE_ID;
  private CapabilityRequirementsProvider _capabilityRequirementsProvider = new StaticCapabilityRequirementsProvider();
  /**
   * Maximum number of milliseconds a job can be with an invoker for before it is abandoned
   */
  private long _maxJobExecutionTime;
  private ScheduledExecutorService _jobTimeoutExecutor;
  private CalculationNodeStatisticsGatherer _statisticsGatherer = new DiscardingStatisticsGatherer();

  public JobDispatcher() {
  }

  public JobDispatcher(final JobInvoker invoker) {
    registerJobInvoker(invoker);
  }

  public JobDispatcher(final Collection<JobInvoker> invokers) {
    addInvokers(invokers);
  }

  public int getMaxJobAttempts() {
    return _maxJobAttempts;
  }

  public void setMaxJobAttempts(final int maxJobAttempts) {
    _maxJobAttempts = maxJobAttempts;
  }

  public void setJobFailureNodeId(final String jobFailureNodeId) {
    _jobFailureNodeId = jobFailureNodeId;
  }

  public String getJobFailureNodeId() {
    return _jobFailureNodeId;
  }

  public long getMaxJobExecutionTime() {
    return _maxJobExecutionTime;
  }

  /**
   * Sets the maximum time for a job to be with an invoker in milliseconds. To disable the upper limit,
   * pass 0 or negative. This doesn't affect jobs already launched; only ones that are invoked after
   * the call.
   * 
   * @param maxJobExecutionTime time in milliseconds
   */
  public synchronized void setMaxJobExecutionTime(final long maxJobExecutionTime) {
    _maxJobExecutionTime = maxJobExecutionTime;
    if (maxJobExecutionTime > 0) {
      if (_jobTimeoutExecutor == null) {
        _jobTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
      }
    }
  }

  public void setStatisticsGatherer(final CalculationNodeStatisticsGatherer statisticsGatherer) {
    _statisticsGatherer = statisticsGatherer;
  }

  public CalculationNodeStatisticsGatherer getStatisticsGatherer() {
    return _statisticsGatherer;
  }

  public synchronized void addInvokers(final Collection<JobInvoker> invokers) {
    _invokers.addAll(invokers);
  }

  public void setCapabilityRequirementsProvider(final CapabilityRequirementsProvider capabilityRequirementsProvider) {
    ArgumentChecker.notNull(capabilityRequirementsProvider, "capabilityRequirementsProvider");
    _capabilityRequirementsProvider = capabilityRequirementsProvider;
  }

  public CapabilityRequirementsProvider getCapabilityRequirementsProvider() {
    return _capabilityRequirementsProvider;
  }

  protected Queue<DispatchJob> getPending() {
    return _pending;
  }

  protected Queue<JobInvoker> getInvokers() {
    return _invokers;
  }

  @Override
  public synchronized void registerJobInvoker(final JobInvoker invoker) {
    ArgumentChecker.notNull(invoker, "invoker");
    s_logger.debug("Registering job invoker {}", invoker);
    getInvokers().add(invoker);
    if (!getPending().isEmpty()) {
      retryPending(0L);
    }
  }

  // caller must already own monitor
  private void retryPending(final long failJobsBefore) {
    s_logger.debug("Retrying pending operations");
    final Iterator<DispatchJob> iterator = getPending().iterator();
    while (iterator.hasNext()) {
      final DispatchJob job = iterator.next();
      if (invoke(job)) {
        iterator.remove();
      } else {
        if (failJobsBefore <= 0) {
          if (getInvokers().isEmpty()) {
            s_logger.debug("No invokers available - not retrying operations");
            break;
          }
        } else if (job.getJobCreationTime() < failJobsBefore) {
          iterator.remove();
          job.jobAbort(null, "no invokers available after timeout");
        }
      }
    }
  }

  // TODO [ENG-42] schedule retryPending to be called periodically with failJobsBefore set to `System.nanoTime() - a timeout` to cancel jobs which can't be executed at all
  // TODO [ENG-42] the invoker selection logic is inefficient; it's likely that capabilityrequirements objects won't vary much so comparison against the capabilities of invokers should be cached
  // TODO [ENG-42] job dispatch should not be O(n) on number of invokers; the caching of capabilities should allow a nearer O(1) selection

  // caller must already own monitor
  private boolean invoke(final DispatchJob job) {
    final Iterator<JobInvoker> iterator = getInvokers().iterator();
    while (iterator.hasNext()) {
      final JobInvoker jobInvoker = iterator.next();
      if (job.canRunOn(jobInvoker)) {
        if (jobInvoker.invoke(job.getJob(), job)) {
          s_logger.debug("Invoker {} accepted job {}", jobInvoker, job.getJob().getSpecification().getJobId());
          // request a job timeout
          job.setTimeout(jobInvoker);
          // put invoker to the end of the list
          iterator.remove();
          getInvokers().add(jobInvoker);
          return true;
        } else {
          s_logger.debug("Invoker {} refused to execute job {}", jobInvoker, job.getJob().getSpecification().getJobId());
          iterator.remove();
          jobInvoker.notifyWhenAvailable(this);
        }
      }
    }
    s_logger.debug("No invokers available for job {}", job.getJob().getSpecification().getJobId());
    return false;
  }

  private synchronized void dispatchJobImpl(final DispatchJob job) {
    if (!invoke(job)) {
      s_logger.debug("Adding job to pending set");
      getPending().add(job);
      if (_invokers != null) {
        retryPending(0L);
      }
    }
  }

  public void dispatchJob(final CalculationJob job, final JobResultReceiver resultReceiver) {
    ArgumentChecker.notNull(job, "job");
    ArgumentChecker.notNull(resultReceiver, "resultReceiver");
    s_logger.info("Dispatching job {}", job.getSpecification().getJobId());
    dispatchJobImpl(new DispatchJob(job, resultReceiver));
  }

}
