/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Invokes jobs on one or more local calculation node implementations.
 */
public class LocalNodeJobInvoker extends AbstractCalculationNodeInvocationContainer<Queue<AbstractCalculationNode>> implements JobInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(LocalNodeJobInvoker.class);
  private static final int DEFAULT_PRIORITY = 10;

  private final AtomicReference<JobInvokerRegister> _notifyWhenAvailable = new AtomicReference<JobInvokerRegister>();
  private final ExecutorService _executorService;

  private int _nodePriority = DEFAULT_PRIORITY;

  public LocalNodeJobInvoker() {
    super(new ConcurrentLinkedQueue<AbstractCalculationNode>());
    _executorService = Executors.newCachedThreadPool();
  }

  public LocalNodeJobInvoker(final AbstractCalculationNode node) {
    this();
    addNode(node);
  }

  public LocalNodeJobInvoker(final Collection<AbstractCalculationNode> nodes) {
    this();
    getNodes().addAll(nodes);
  }

  @Override
  public void onNodeChange() {
    final JobInvokerRegister notify = _notifyWhenAvailable.getAndSet(null);
    if (notify != null) {
      notify.registerJobInvoker(this);
    }
  }

  public void setNodePriority(final int priority) {
    _nodePriority = priority;
  }

  public int getNodePriority() {
    return _nodePriority;
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  @Override
  public int canInvoke(CalculationJobSpecification jobSpec, List<CalculationJobItem> items) {
    return getNodePriority();
  }

  @Override
  public boolean invoke(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items, final JobInvocationReceiver receiver) {
    final AbstractCalculationNode node = getNodes().poll();
    if (node == null) {
      return false;
    }
    final CalculationJob job = new CalculationJob(jobSpec, items);
    final Runnable invokeTask = new Runnable() {
      @Override
      public void run() {
        CalculationJobResult result = null;
        try {
          result = node.executeJob(job);
        } catch (Exception e) {
          s_logger.warn("Exception thrown by job execution", e);
          receiver.jobFailed(e);
        }
        if (result != null) {
          receiver.jobCompleted(result);
        }
        addNode(node);
      }
    };
    getExecutorService().execute(invokeTask);
    return true;
  }

  @Override
  public void notifyWhenAvailable(JobInvokerRegister callback) {
    _notifyWhenAvailable.set(callback);
    if (!getNodes().isEmpty()) {
      callback = _notifyWhenAvailable.getAndSet(null);
      if (callback != null) {
        callback.registerJobInvoker(this);
      }
    }
  }

}
