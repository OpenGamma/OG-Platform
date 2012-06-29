/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.blacklist.DummyFunctionBlacklistMaintainer;
import com.opengamma.engine.function.blacklist.FunctionBlacklistMaintainer;
import com.opengamma.engine.view.calcnode.stats.CalculationNodeStatisticsGatherer;
import com.opengamma.engine.view.calcnode.stats.DiscardingNodeStatisticsGatherer;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.Cancelable;

/**
 * Manages a set of JobInvokers and dispatches jobs to them for execution.
 */
public class JobDispatcher implements JobInvokerRegister {

  private static final Logger s_logger = LoggerFactory.getLogger(JobDispatcher.class);
  /* package */static final int DEFAULT_MAX_JOB_ATTEMPTS = 2;
  /* package */static final long DEFAULT_MAX_JOB_EXECUTION_QUERY_TIMEOUT = 5000;
  /* package */static final String DEFAULT_JOB_FAILURE_NODE_ID = "NOT EXECUTED";

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

  private static class Timeout implements Runnable {

    public static final Timeout FINISHED = new Timeout();
    public static final Timeout CANCELLED = new Timeout();

    private final ScheduledThreadPoolExecutor _executor;
    private DispatchJob _dispatchJob;
    private JobInvoker _jobInvoker;
    private RunnableScheduledFuture<?> _future;
    private long _timeAccrued;

    private Timeout() {
      _executor = null;
    }

    public Timeout(final DispatchJob dispatchJob, final JobInvoker jobInvoker, final ScheduledThreadPoolExecutor executor, final long timeoutMillis) {
      _dispatchJob = dispatchJob;
      _jobInvoker = jobInvoker;
      _executor = executor;
      _timeAccrued = timeoutMillis;
      setTimeout(timeoutMillis);
    }

    private void setTimeout(final long timeoutMillis) {
      if (timeoutMillis > 0) {
        _future = (RunnableScheduledFuture<?>) _executor.schedule(this, timeoutMillis, TimeUnit.MILLISECONDS);
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
        _executor.remove(_future);
        _future = null;
      }
    }

    public synchronized void extend(final long timeoutMillis, final boolean resetAccruedTime) {
      if (_future != null) {
        _executor.remove(_future);
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

  private final class DispatchJob implements JobInvocationReceiver, Cancelable {

    private final CalculationJob _rootJob;
    private final ConcurrentMap<CalculationJobSpecification, JobResultReceiver> _resultReceivers;
    private final AtomicBoolean _completed = new AtomicBoolean(false);
    private final long _jobCreationTime;
    private final CapabilityRequirements _capabilityRequirements;
    private final AtomicReference<Timeout> _timeout = new AtomicReference<Timeout>();
    private Set<JobInvoker> _excludeJobInvoker;
    private int _rescheduled;

    private DispatchJob(final CalculationJob job, final JobResultReceiver resultReceiver) {
      _rootJob = job;
      _resultReceivers = new ConcurrentHashMap<CalculationJobSpecification, JobResultReceiver>();
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
      final JobResultReceiver resultReceiver = _resultReceivers.remove(result.getSpecification());
      if (resultReceiver == null) {
        s_logger.warn("Job {} completed on node {} but is not currently pending", result.getSpecification().getJobId(), result.getComputeNodeId());
        // Note the above warning can happen if we've been retried
        extendTimeout(getMaxJobExecutionTime(), true);
        return;
      }
      if (_resultReceivers.isEmpty()) {
        // This is the last one to complete. Note that if the last few jobs complete concurrently, both may execute this code.
        _completed.set(true);
        cancelTimeout(Timeout.FINISHED);
      } else {
        // Others are still running, but we can extend the timeout period
        extendTimeout(getMaxJobExecutionTime(), true);
      }
      s_logger.info("Job {} completed on node {}", result.getSpecification().getJobId(), result.getComputeNodeId());
      resultReceiver.resultReceived(result);
      final long durationNanos = getDurationNanos();
      s_logger.debug("Reported time = {}ms, non-executing job time = {}ms", (double) result.getDuration() / 1000000d, ((double) durationNanos - (double) result.getDuration()) / 1000000d);
      if (getStatisticsGatherer() != null) {
        final int size = result.getResultItems().size();
        getStatisticsGatherer().jobCompleted(result.getComputeNodeId(), size, result.getDuration(), getDurationNanos());
      }
    }

    @Override
    public void jobFailed(final JobInvoker jobInvoker, final String computeNodeId, final Exception exception) {
      s_logger.warn("Job {} failed, {}", getJob().getSpecification().getJobId(), (exception != null) ? exception.getMessage() : "no exception passed");
      if (_completed.getAndSet(true) == false) {
        cancelTimeout(null);
        // TODO: [PLAT-2211] check if the job is watched and partition again to isolate the failing job item
        if ((_excludeJobInvoker != null) && _excludeJobInvoker.contains(jobInvoker)) {
          _completed.set(false);
          jobAbort(exception, "duplicate invoker failure from node " + computeNodeId);
          // TODO: [PLAT-2211] the job is now a watched job and should be partitioned to isolate the failing job item
        } else {
          _rescheduled++;
          if (_rescheduled >= getMaxJobAttempts()) {
            _completed.set(false);
            jobAbort(exception, "internal node error");
            // TODO: [PLAT-2211] the job is now a watched job and should be partitioned to isolate the failing job item
          } else {
            s_logger.info("Retrying job {} (attempt {})", getJob().getSpecification().getJobId(), _rescheduled);
            if (_excludeJobInvoker == null) {
              _excludeJobInvoker = new HashSet<JobInvoker>();
            }
            _excludeJobInvoker.add(jobInvoker);
            _completed.set(false);
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

    private void failTree(final CalculationJob job, final CalculationJobResultItem failure) {
      final JobResultReceiver resultReceiver = _resultReceivers.remove(job.getSpecification());
      if (resultReceiver != null) {
        final int size = job.getJobItems().size();
        final List<CalculationJobResultItem> failureItems = new ArrayList<CalculationJobResultItem>(size);
        for (int i = 0; i < size; i++) {
          failureItems.add(failure);
        }
        final CalculationJobResult jobResult = new CalculationJobResult(job.getSpecification(), getDurationNanos(), failureItems, getJobFailureNodeId());
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

    private void jobAbort(Exception exception, final String alternativeError) {
      s_logger.error("Aborted job {} after {} attempts", getJob().getSpecification().getJobId(), _rescheduled);
      if (_completed.getAndSet(true) == false) {
        cancelTimeout(Timeout.FINISHED);
        if (exception == null) {
          s_logger.error("Aborted job {} with {}", getJob().getSpecification().getJobId(), alternativeError);
          exception = new OpenGammaRuntimeException(alternativeError);
          exception.fillInStackTrace();
        }
        failTree(getJob(), CalculationJobResultItem.failure(exception));
      } else {
        s_logger.warn("Job {} aborted but we've already completed or aborted from another node", getJob().getSpecification().getJobId());
      }
    }

    public void timeout(final long timeAccrued, final JobInvoker jobInvoker) {
      s_logger.debug("Timeout on {}, {}ms accrued", jobInvoker.getInvokerId(), timeAccrued);
      final long remaining = getMaxJobExecutionTime() - timeAccrued;
      if (remaining > 0) {
        if (jobInvoker.isAlive(_resultReceivers.keySet())) {
          s_logger.debug("Invoker {} reports job {} still alive", jobInvoker.getInvokerId(), getJob().getSpecification().getJobId());
          extendTimeout(remaining, false);
          return;
        } else {
          s_logger.warn("Invoker {} reports job {} failure", jobInvoker.getInvokerId(), getJob().getSpecification().getJobId());
          jobFailed(jobInvoker, "node on " + jobInvoker.getInvokerId(), new OpenGammaRuntimeException("Node reported failure at " + timeAccrued + "ms keepalive"));
        }
      } else {
        jobFailed(jobInvoker, "node on " + jobInvoker.getInvokerId(), new OpenGammaRuntimeException("Invocation limit of " + getMaxJobExecutionTime() + "ms exceeded"));
      }
    }

    private void setTimeout(final JobInvoker jobInvoker) {
      Timeout timeout = new Timeout(this, jobInvoker, getJobTimeoutExecutor(), Math.min(getMaxJobExecutionTimeQuery(), getMaxJobExecutionTime()));
      if (_timeout.compareAndSet(null, timeout)) {
        s_logger.debug("Timeout set for job {}", getJob().getSpecification().getJobId());
      } else {
        timeout.cancel();
        timeout = _timeout.get();
        if (timeout == Timeout.FINISHED) {
          s_logger.debug("Job {} already completed", getJob().getSpecification().getJobId());
        } else if (timeout == Timeout.CANCELLED) {
          s_logger.debug("Job {} cancelled", getJob().getSpecification().getJobId());
        } else {
          s_logger.debug("Job {} timeout already set", getJob().getSpecification().getJobId());
        }
      }
    }

    private Timeout cancelTimeout(final Timeout flagState) {
      final Timeout timeout = _timeout.getAndSet(flagState);
      if (timeout == null) {
        s_logger.debug("Cancel timeout for {} - no timeout set", getJob().getSpecification().getJobId());
      } else if (timeout == Timeout.FINISHED) {
        s_logger.debug("Cancel timeout for {} - job finished", getJob().getSpecification().getJobId());
      } else if (timeout == Timeout.CANCELLED) {
        s_logger.debug("Cancel timeout for {} - job cancelled", getJob().getSpecification().getJobId());
      } else {
        s_logger.debug("Cancelling timeout for {}", getJob().getSpecification().getJobId());
        timeout.cancel();
        return timeout;
      }
      return null;
    }

    private void extendTimeout(final long remainingMillis, final boolean resetTimeAccrued) {
      final Timeout timeout = _timeout.get();
      if ((timeout != null) && (timeout != Timeout.FINISHED) && (timeout != Timeout.CANCELLED)) {
        s_logger.debug("Extending timeout on job {}", getJob().getSpecification().getJobId());
        timeout.extend(Math.min(remainingMillis, getMaxJobExecutionTime()), resetTimeAccrued);
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

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      s_logger.info("Cancelling job {}", getJob().getSpecification().getJobId());
      while (_completed.getAndSet(true) != false) {
        Thread.yield();
        if (_timeout.get() == Timeout.FINISHED) {
          s_logger.warn("Can't cancel job {} - already finished", getJob().getSpecification().getJobId());
          return false;
        } else if (_timeout.get() == Timeout.CANCELLED) {
          s_logger.info("Job {} already cancelled", getJob().getSpecification().getJobId());
          return true;
        } else {
          s_logger.debug("Job {} - currently failing, cancelling or aborting", getJob().getSpecification().getJobId());
        }
      }
      final Timeout timeout = cancelTimeout(Timeout.CANCELLED);
      if (timeout != null) {
        final JobInvoker invoker = timeout.getInvoker();
        if (invoker != null) {
          invoker.cancel(_resultReceivers.keySet());
        }
      }
      return true;
    }

  }

  private final Queue<DispatchJob> _pending = new LinkedList<DispatchJob>();
  private final Queue<JobInvoker> _invokers = new ConcurrentLinkedQueue<JobInvoker>();
  private final Map<JobInvoker, Collection<Capability>> _capabilityCache = new ConcurrentHashMap<JobInvoker, Collection<Capability>>();

  /**
   * Maximum number of times a job will be submitted in its entirety to remote nodes before it gets partitioned to isolate an individual failure.
   */
  private int _maxJobAttempts = DEFAULT_MAX_JOB_ATTEMPTS;
  private String _jobFailureNodeId = DEFAULT_JOB_FAILURE_NODE_ID;
  private CapabilityRequirementsProvider _capabilityRequirementsProvider = new StaticCapabilityRequirementsProvider();
  /**
   * Maximum number of milliseconds a job can be with an invoker for before it is abandoned.
   */
  private long _maxJobExecutionTime;
  /**
   * How often to query an invoker that has outstanding jobs.
   */
  private long _maxJobExecutionTimeQuery = DEFAULT_MAX_JOB_EXECUTION_QUERY_TIMEOUT;
  private ScheduledThreadPoolExecutor _jobTimeoutExecutor;
  private CalculationNodeStatisticsGatherer _statisticsGatherer = new DiscardingNodeStatisticsGatherer();
  private FunctionBlacklistMaintainer _blacklistUpdate = new DummyFunctionBlacklistMaintainer();

  public JobDispatcher() {
  }

  public JobDispatcher(final JobInvoker invoker) {
    registerJobInvoker(invoker);
  }

  public JobDispatcher(final Collection<JobInvoker> invokers) {
    for (JobInvoker invoker : invokers) {
      registerJobInvoker(invoker);
    }
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

  public FunctionBlacklistMaintainer getFunctionBlacklistMaintainer() {
    return _blacklistUpdate;
  }

  public void setFunctionBlacklistMaintainer(final FunctionBlacklistMaintainer blacklistUpdate) {
    ArgumentChecker.notNull(blacklistUpdate, "blacklistUpdate");
    _blacklistUpdate = blacklistUpdate;
  }

  protected ScheduledThreadPoolExecutor getJobTimeoutExecutor() {
    return _jobTimeoutExecutor;
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
        _jobTimeoutExecutor = new ScheduledThreadPoolExecutor(1);
        _jobTimeoutExecutor.setMaximumPoolSize(1);
      }
    }
  }

  public void setMaxJobExecutionTimeQuery(final long maxJobExecutionTimeQuery) {
    if (maxJobExecutionTimeQuery <= 0) {
      throw new IllegalArgumentException("maxJobExecutionTimeQuery must be greater than 0ms");
    }
    _maxJobExecutionTimeQuery = maxJobExecutionTimeQuery;
  }

  public long getMaxJobExecutionTimeQuery() {
    return _maxJobExecutionTimeQuery;
  }

  public void setStatisticsGatherer(final CalculationNodeStatisticsGatherer statisticsGatherer) {
    _statisticsGatherer = statisticsGatherer;
  }

  public CalculationNodeStatisticsGatherer getStatisticsGatherer() {
    return _statisticsGatherer;
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

  protected Map<JobInvoker, Collection<Capability>> getCapabilityCache() {
    return _capabilityCache;
  }

  @Override
  public synchronized void registerJobInvoker(final JobInvoker invoker) {
    ArgumentChecker.notNull(invoker, "invoker");
    s_logger.debug("Registering job invoker {}", invoker);
    getInvokers().add(invoker);
    getCapabilityCache().put(invoker, invoker.getCapabilities());
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
  // TODO [ENG-42] the invoker selection logic is inefficient; it's likely that capability requirements objects won't vary much so comparison against the capabilities of invokers should be cached
  // TODO [ENG-42] job dispatch should not be O(n) on number of invokers; the caching of capabilities should allow a nearer O(1) selection

  // caller must already own monitor
  private boolean invoke(final DispatchJob job) {
    if (job._completed.get()) {
      s_logger.info("Job {} cancelled", job.getJob().getSpecification().getJobId());
      return true;
    }
    Collection<JobInvoker> retry = null;
    do {
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
            if (jobInvoker.notifyWhenAvailable(this)) {
              s_logger.info("Invoker {} requested immediate retry", jobInvoker);
              if (retry == null) {
                retry = new LinkedList<JobInvoker>();
              }
              retry.add(jobInvoker);
            }
          }
        }
      }
      if (retry != null) {
        getInvokers().addAll(retry);
        retry = null;
      } else {
        break;
      }
    } while (true);
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

  /**
   * Puts the job into the ready queue, sent to an invoker as soon as one is available. Completion (or timeout)
   * of the job will result in one or more callbacks to the result receiver. There is always the callback for the
   * main job. If the job had a tail, a callback will also occur for each tail job. The {@link Cancellable}
   * callback returned may be used to abort operation. If operation is aborted, results may still be received
   * if they were too far in the pipeline to be stopped.
   * 
   * @param job The job to dispatch
   * @param resultReceiver callback to receive the results
   * @return A {@link Cancellable} callback to attempt to abort the job
   */
  public Cancelable dispatchJob(final CalculationJob job, final JobResultReceiver resultReceiver) {
    ArgumentChecker.notNull(job, "job");
    ArgumentChecker.notNull(resultReceiver, "resultReceiver");
    s_logger.info("Dispatching job {}", job.getSpecification().getJobId());
    final DispatchJob dispatchJob = new DispatchJob(job, resultReceiver);
    dispatchJobImpl(dispatchJob);
    return dispatchJob;
  }

  /**
   * Returns capabilities from all available invokers.
   * 
   * @return Map of invoker identifier to capability set.
   */
  public Map<String, Collection<Capability>> getAllCapabilities() {
    final Iterator<Map.Entry<JobInvoker, Collection<Capability>>> invokerCapabilityIterator = getCapabilityCache().entrySet().iterator();
    final Map<String, Collection<Capability>> result = new HashMap<String, Collection<Capability>>();
    while (invokerCapabilityIterator.hasNext()) {
      final Map.Entry<JobInvoker, Collection<Capability>> invokerCapability = invokerCapabilityIterator.next();
      final String identifier = invokerCapability.getKey().getInvokerId();
      if (identifier == null) {
        invokerCapabilityIterator.remove();
      } else {
        result.put(identifier, invokerCapability.getValue());
      }
    }
    return result;
  }

}
