/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * Manages a set of JobInvokers and dispatches jobs to them for execution.
 */
public class JobDispatcher implements JobInvokerRegister {

  private static final Logger s_logger = LoggerFactory.getLogger(JobDispatcher.class);

  private final Queue<Triple<CalculationJobSpecification, List<CalculationJobItem>, JobResultReceiver>> _pending = new LinkedList<Triple<CalculationJobSpecification, List<CalculationJobItem>, JobResultReceiver>>();

  private static class JobInvokerNode {
    private JobInvokerNode _next;
    private JobInvoker _invoker;

    public JobInvokerNode(final JobInvoker invoker, final JobInvokerNode next) {
      _next = next;
      _invoker = invoker;
    }
  }

  private JobInvokerNode _invokers;

  public JobDispatcher() {
  }

  public JobDispatcher(final JobInvoker invoker) {
    registerJobInvoker(invoker);
  }

  public JobDispatcher(final Collection<JobInvoker> invokers) {
    addInvokers(invokers);
  }

  public synchronized void addInvokers(final Collection<JobInvoker> invokers) {
    for (JobInvoker invoker : invokers) {
      _invokers = new JobInvokerNode(invoker, _invokers);
    }
  }

  protected Queue<Triple<CalculationJobSpecification, List<CalculationJobItem>, JobResultReceiver>> getPending() {
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
    final Iterator<Triple<CalculationJobSpecification, List<CalculationJobItem>, JobResultReceiver>> iterator = getPending().iterator();
    while (iterator.hasNext()) {
      final Triple<CalculationJobSpecification, List<CalculationJobItem>, JobResultReceiver> job = iterator.next();
      if (invoke(job.getFirst(), job.getSecond(), job.getThird())) {
        iterator.remove();
      } else if (_invokers == null) {
        s_logger.debug("Aborting retry pending operation - no invokers in chain");
        break;
      }
    }
  }

  private boolean invoke(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items, final JobResultReceiver resultReceiver) {
    do {
      int bestScore = 0;
      JobInvokerNode bestInvokerPrev = null;
      JobInvokerNode prevInvokerNode = null;
      JobInvokerNode invokerNode = _invokers;
      while (invokerNode != null) {
        final int score = invokerNode._invoker.canInvoke(jobSpec, items);
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
        if (invokerNode._invoker.invoke(jobSpec, items, resultReceiver)) {
          s_logger.debug("Invoker {} accepted job {}", invokerNode._invoker, jobSpec.getJobId ());
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
          s_logger.debug("Invoker {} refused to execute job {}", invokerNode._invoker, jobSpec.getJobId ());
          invokerNode._invoker.notifyWhenAvailable(this);
          // Repeat in case there are other, lower priority, invokers
          
          // TODO This is inefficient in the case of there being lots of invokers. We should have kept the scores, sort them and process in correct order 
        }
      } else {
        s_logger.debug("No invokers can accept job {}", jobSpec.getJobId ());
        return false;
      }
    } while (true);
  }
  
  // TODO The above selection logic and "canInvoke" mechanism for scoring is inefficient for large numbers of invokers. Change to something more sensible when doing ENG-42 properly

  public synchronized void dispatchJob(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items, final JobResultReceiver resultReceiver) {
    ArgumentChecker.notNull(jobSpec, "jobSpec");
    ArgumentChecker.notNull(items, "items");
    ArgumentChecker.notNull(resultReceiver, "resultReceiver");
    s_logger.debug("Begin dispatch job");
    if (!invoke(jobSpec, items, resultReceiver)) {
      s_logger.debug("Adding job to pending set");
      getPending().add(Triple.of(jobSpec, items, resultReceiver));
      if (_invokers != null) {
        retryPending();
      }
    }
  }

}
