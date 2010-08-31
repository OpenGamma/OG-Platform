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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.calcnode.stats.StatisticsGatherer;
import com.opengamma.util.ArgumentChecker;

/**
 * Manages a set of JobInvokers and dispatches jobs to them for execution.
 */
public class JobDispatcher implements JobInvokerRegister {

  private static final Logger s_logger = LoggerFactory.getLogger(JobDispatcher.class);
  /* package */static final int DEFAULT_MAX_JOB_ATTEMPTS = 3;
  /* package */static final String DEFAULT_JOB_FAILURE_NODE_ID = "NOT EXECUTED";
  
  private final class DispatchJob implements JobInvocationReceiver {

    private final CalculationJobSpecification _jobSpec;
    private final List<CalculationJobItem> _items;
    private final AtomicReference<JobResultReceiver> _resultReceiver = new AtomicReference<JobResultReceiver>();
    private final long _jobCreationTime;
    private final CapabilityRequirements _capabilityRequirements;
    private Set<JobInvoker> _excludeJobInvoker;
    private int _rescheduled;
    private Future<?> _timeout;

    private DispatchJob(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items, final JobResultReceiver resultReceiver) {
      _jobSpec = jobSpec;
      _items = items;
      _resultReceiver.set(resultReceiver);
      _jobCreationTime = System.nanoTime();
      _capabilityRequirements = getCapabilityRequirementsProvider().getCapabilityRequirements(jobSpec, items);
    }

    private long getDurationNanos() {
      return System.nanoTime() - getJobCreationTime();
    }

    @Override
    public void jobCompleted(final CalculationJobResult result) {
      assert getJobSpec().equals(result.getSpecification());
      cancelTimeout();
      JobResultReceiver resultReceiver = _resultReceiver.getAndSet(null);
      if (resultReceiver != null) {
        s_logger.debug("Job {} completed on node {}", getJobSpec().getJobId(), result.getComputeNodeId());
        resultReceiver.resultReceived(result);
        final long durationNanos = getDurationNanos();
        s_logger.debug("Reported time = {}ms, non-executing job time = {}ms", (double) result.getDuration() / 1000000d, ((double) durationNanos - (double) result.getDuration()) / 1000000d);
        if (getStatisticsGatherer() != null) {
          getStatisticsGatherer().jobCompleted(result.getComputeNodeId(), result.getDuration(), getDurationNanos());
        }
      } else {
        s_logger.warn("Job {} completed on node {} but we've already completed or aborted from another node", getJobSpec().getJobId(), result.getComputeNodeId());
      }
    }

    @Override
    public void jobFailed(final JobInvoker jobInvoker, final String computeNodeId, final Exception exception) {
      cancelTimeout();
      final JobResultReceiver resultReceiver = _resultReceiver.getAndSet(null);
      if (resultReceiver != null) {
        s_logger.debug("Job {} failed, {}", getJobSpec().getJobId(), (exception != null) ? exception.getMessage() : "no exception passed");
        _rescheduled++;
        if (_rescheduled >= getMaxJobAttempts()) {
          _resultReceiver.set(resultReceiver);
          jobAbort(exception, "internal node error");
        } else {
          s_logger.info("Retrying job {} (attempt {})", getJobSpec().getJobId(), _rescheduled);
          if (_excludeJobInvoker == null) {
            _excludeJobInvoker = new HashSet<JobInvoker>();
          }
          _excludeJobInvoker.add(jobInvoker);
          _resultReceiver.set(resultReceiver);
          dispatchJobImpl(this);
        }
        if (getStatisticsGatherer() != null) {
          getStatisticsGatherer().jobFailed(computeNodeId, getDurationNanos());
        }
      } else {
        s_logger.warn("Job {} failed but we've already completed or aborted from another node", getJobSpec().getJobId());
      }
    }

    private void jobAbort(Exception exception, final String alternativeError) {
      cancelTimeout();
      final JobResultReceiver resultReceiver = _resultReceiver.getAndSet(null);
      if (resultReceiver != null) {
        s_logger.warn("Failed job {} after {} attempts", getJobSpec().getJobId(), _rescheduled);
        if (exception == null) {
          s_logger.error("Failed job {} with {}", getJobSpec().getJobId(), alternativeError);
          exception = new OpenGammaRuntimeException(alternativeError);
          exception.fillInStackTrace();
        }
        final List<CalculationJobResultItem> failureItems = new ArrayList<CalculationJobResultItem>(getItems().size());
        for (CalculationJobItem item : getItems()) {
          failureItems.add(new CalculationJobResultItem(item, exception));
        }
        final CalculationJobResult jobResult = new CalculationJobResult(getJobSpec(), getDurationNanos(), failureItems, getJobFailureNodeId());
        resultReceiver.resultReceived(jobResult);
      } else {
        s_logger.warn("Job {} aborted but we've already completed or aborted from another node", getJobSpec().getJobId());
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

    private CalculationJobSpecification getJobSpec() {
      return _jobSpec;
    }

    private List<CalculationJobItem> getItems() {
      return _items;
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
  private StatisticsGatherer _statisticsGatherer;

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

  public void setStatisticsGatherer(final StatisticsGatherer statisticsGatherer) {
    _statisticsGatherer = statisticsGatherer;
  }

  public StatisticsGatherer getStatisticsGatherer() {
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
        if (jobInvoker.invoke(job.getJobSpec(), job.getItems(), job)) {
          s_logger.debug("Invoker {} accepted job {}", jobInvoker, job.getJobSpec().getJobId());
          // request a job timeout
          job.setTimeout(jobInvoker);
          // put invoker to the end of the list
          iterator.remove();
          getInvokers().add(jobInvoker);
          return true;
        } else {
          s_logger.debug("Invoker {} refused to execute job {}", jobInvoker, job.getJobSpec().getJobId());
          iterator.remove();
          jobInvoker.notifyWhenAvailable(this);
        }
      }
    }
    s_logger.debug("No invokers available for job {}", job.getJobSpec().getJobId());
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

  public void dispatchJob(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items, final JobResultReceiver resultReceiver) {
    ArgumentChecker.notNull(jobSpec, "jobSpec");
    ArgumentChecker.notNull(items, "items");
    ArgumentChecker.notNull(resultReceiver, "resultReceiver");
    s_logger.info("Dispatching job {}", jobSpec.getJobId());
    dispatchJobImpl(new DispatchJob(jobSpec, items, resultReceiver));
  }

}
