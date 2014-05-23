/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.http.concurrent.Cancellable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.calcnode.stats.CalculationNodeStatisticsGatherer;
import com.opengamma.engine.calcnode.stats.DiscardingNodeStatisticsGatherer;
import com.opengamma.engine.function.blacklist.DummyFunctionBlacklistMaintainer;
import com.opengamma.engine.function.blacklist.FunctionBlacklistMaintainer;
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

  private final Queue<DispatchableJob> _pending = new LinkedList<DispatchableJob>();
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

  protected Queue<DispatchableJob> getPending() {
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
    final Iterator<DispatchableJob> iterator = getPending().iterator();
    while (iterator.hasNext()) {
      final DispatchableJob job = iterator.next();
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
          job.abort(null, "no invokers available after timeout");
        }
      }
    }
  }

  // TODO [ENG-42] schedule retryPending to be called periodically with failJobsBefore set to `System.nanoTime() - a timeout` to cancel jobs which can't be executed at all
  // TODO [ENG-42] the invoker selection logic is inefficient; it's likely that capability requirements objects won't vary much so comparison against the capabilities of invokers should be cached
  // TODO [ENG-42] job dispatch should not be O(n) on number of invokers; the caching of capabilities should allow a nearer O(1) selection

  // caller must already own monitor
  private boolean invoke(final DispatchableJob job) {
    if (job.isCompleted()) {
      s_logger.info("Job {} cancelled", job);
      return true;
    }
    Collection<JobInvoker> retry = null;
    do {
      final Iterator<JobInvoker> iterator = getInvokers().iterator();
      while (iterator.hasNext()) {
        final JobInvoker jobInvoker = iterator.next();
        if (job.canRunOn(jobInvoker)) {
          if (job.runOn(jobInvoker)) {
            s_logger.debug("Invoker {} accepted job {}", jobInvoker, job);
            // put invoker to the end of the list
            iterator.remove();
            getInvokers().add(jobInvoker);
            return true;
          } else {
            s_logger.debug("Invoker {} refused to execute job {}", jobInvoker, job);
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
    s_logger.debug("No invokers available for job {}", job);
    return false;
  }

  protected synchronized void dispatchJobImpl(final DispatchableJob job) {
    if (!invoke(job)) {
      s_logger.debug("Adding job to pending set");
      getPending().add(job);
      if (getInvokers() != null) {
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
    final DispatchableJob dispatchJob = new StandardJob(this, job, resultReceiver);
    dispatchJobImpl(dispatchJob);
    return dispatchJob.getCancelHandle();
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
