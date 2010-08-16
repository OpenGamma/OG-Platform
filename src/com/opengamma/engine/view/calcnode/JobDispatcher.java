/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
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
    private final JobResultReceiver _resultReceiver;
    private final long _jobCreationTime;
    private int _rescheduled;

    private DispatchJob(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items, final JobResultReceiver resultReceiver) {
      _jobSpec = jobSpec;
      _items = items;
      _resultReceiver = resultReceiver;
      _jobCreationTime = System.currentTimeMillis();
    }

    private long getDurationNanos() {
      return (System.currentTimeMillis() - _jobCreationTime) * 1000000L;
    }

    @Override
    public void jobCompleted(CalculationJobResult result) {
      // REVIEW 2010-08-16 We only need the node ID and the list of result items; we already have everything else we need for the job result
      assert _jobSpec.equals(result.getSpecification());
      s_logger.debug("Job {} completed", _jobSpec.getJobId());
      _resultReceiver.resultReceived(result);
      s_logger.debug("Non-executing job time overhead = {}ms", ((double) getDurationNanos() - (double) result.getDuration()) / 1000000d);
    }

    @Override
    public void jobFailed(Exception exception) {
      s_logger.debug("Job {} failed", _jobSpec.getJobId());
      _rescheduled++;
      if (_rescheduled >= getMaxJobAttempts()) {
        s_logger.warn("Failed job {} after {} attempts", _jobSpec.getJobId(), _rescheduled);
        if (exception == null) {
          exception = new OpenGammaRuntimeException("No underlying transport exception");
          exception.fillInStackTrace();
        }
        final List<CalculationJobResultItem> failureItems = new ArrayList<CalculationJobResultItem>(getItems().size());
        for (CalculationJobItem item : getItems()) {
          failureItems.add(new CalculationJobResultItem(item, exception));
        }
        final CalculationJobResult jobResult = new CalculationJobResult(_jobSpec, getDurationNanos(), failureItems, getJobFailureNodeId());
        _resultReceiver.resultReceived(jobResult);
      } else {
        s_logger.info("Retrying job {} (attempt {})", _jobSpec.getJobId(), _rescheduled);
        dispatchJobImpl(this);
      }
    }

    private CalculationJobSpecification getJobSpec() {
      return _jobSpec;
    }

    private List<CalculationJobItem> getItems() {
      return _items;
    }

    // [ENG-42] As part of the capability rewrite, query the job for its requirements so it can flag up an invoker it doesn't want to run on again

  }

  private final Queue<DispatchJob> _pending = new LinkedList<DispatchJob>();

  private static class JobInvokerNode {
    private JobInvokerNode _next;
    private JobInvoker _invoker;

    public JobInvokerNode(final JobInvoker invoker, final JobInvokerNode next) {
      _next = next;
      _invoker = invoker;
    }
  }

  private JobInvokerNode _invokers;
  private int _maxJobAttempts = DEFAULT_MAX_JOB_ATTEMPTS;
  private String _jobFailureNodeId = DEFAULT_JOB_FAILURE_NODE_ID;

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

  public synchronized void addInvokers(final Collection<JobInvoker> invokers) {
    for (JobInvoker invoker : invokers) {
      _invokers = new JobInvokerNode(invoker, _invokers);
    }
  }

  protected Queue<DispatchJob> getPending() {
    return _pending;
  }

  @Override
  public synchronized void registerJobInvoker(final JobInvoker invoker) {
    ArgumentChecker.notNull(invoker, "invoker");
    s_logger.debug("Registering job invoker {}", invoker);
    _invokers = new JobInvokerNode(invoker, _invokers);
    if (!getPending().isEmpty()) {
      retryPending();
    }
  }

  private void retryPending() {
    s_logger.debug("Retrying pending operations");
    final Iterator<DispatchJob> iterator = getPending().iterator();
    while (iterator.hasNext()) {
      final DispatchJob job = iterator.next();
      if (invoke(job)) {
        iterator.remove();
      } else if (_invokers == null) {
        s_logger.debug("Aborting retry pending operation - no invokers in chain");
        break;
      }
    }
  }

  private boolean invoke(final DispatchJob job) {
    do {
      int bestScore = 0;
      JobInvokerNode bestInvokerPrev = null;
      JobInvokerNode prevInvokerNode = null;
      JobInvokerNode invokerNode = _invokers;
      while (invokerNode != null) {
        final int score = invokerNode._invoker.canInvoke(job.getJobSpec(), job.getItems());
        if (score > bestScore) {
          bestScore = score;
          bestInvokerPrev = prevInvokerNode;
        }
        prevInvokerNode = invokerNode;
        invokerNode = invokerNode._next;
      }
      if (bestScore > 0) {
        // Remove the node from its current position in the list
        if (bestInvokerPrev != null) {
          invokerNode = bestInvokerPrev._next;
          bestInvokerPrev._next = invokerNode._next;
        } else {
          invokerNode = _invokers;
          _invokers = invokerNode._next;
        }
        if (invokerNode._invoker.invoke(job.getJobSpec(), job.getItems(), job)) {
          s_logger.debug("Invoker {} accepted job {}", invokerNode._invoker, job.getJobSpec().getJobId());
          if (prevInvokerNode == invokerNode) {
            // invoker node was already at the end of the list
            if (bestInvokerPrev != null) {
              bestInvokerPrev._next = invokerNode;
            } else {
              _invokers = invokerNode;
            }
          } else {
            // return invoker node to the end of the list
            prevInvokerNode._next = invokerNode;
          }
          invokerNode._next = null;
          return true;
        } else {
          s_logger.debug("Invoker {} refused to execute job {}", invokerNode._invoker, job.getJobSpec().getJobId());
          invokerNode._invoker.notifyWhenAvailable(this);
          // Repeat in case there are other, lower priority, invokers

          // TODO This is inefficient in the case of there being lots of invokers. We should have kept the scores, sort them and process in correct order
        }
      } else {
        s_logger.debug("No invokers can accept job {}", job.getJobSpec().getJobId());
        return false;
      }
    } while (true);
  }

  // TODO The above selection logic and "canInvoke" mechanism for scoring is inefficient for large numbers of invokers. Change to something more sensible when doing ENG-42 properly

  private void dispatchJobImpl(final DispatchJob job) {
    if (!invoke(job)) {
      s_logger.debug("Adding job to pending set");
      getPending().add(job);
      if (_invokers != null) {
        retryPending();
      }
    }
  }

  public synchronized void dispatchJob(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items, final JobResultReceiver resultReceiver) {
    ArgumentChecker.notNull(jobSpec, "jobSpec");
    ArgumentChecker.notNull(items, "items");
    ArgumentChecker.notNull(resultReceiver, "resultReceiver");
    s_logger.info("Dispatching job {}", jobSpec.getJobId());
    dispatchJobImpl(new DispatchJob(jobSpec, items, resultReceiver));
  }

}
