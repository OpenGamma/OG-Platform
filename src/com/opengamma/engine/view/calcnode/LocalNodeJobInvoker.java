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

/**
 * Invokes jobs on one or more local calculation node implementations.
 */
public class LocalNodeJobInvoker implements JobInvoker {

  private static final int DEFAULT_PRIORITY = 10;

  private final Queue<AbstractCalculationNode> _nodes = new ConcurrentLinkedQueue<AbstractCalculationNode>();
  private final Queue<JobInvokerRegister> _notifyWhenAvailable = new ConcurrentLinkedQueue<JobInvokerRegister>();
  private final ExecutorService _executorService;

  private int _nodePriority = DEFAULT_PRIORITY;

  public LocalNodeJobInvoker() {
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

  public void addNode(final AbstractCalculationNode node) {
    getNodes().add(node);
    if (_notifyWhenAvailable.isEmpty()) {
      synchronized (this) {
        if (!_notifyWhenAvailable.isEmpty()) {
          notifyAvailable();
        }
      }
    } else {
      notifyAvailable();
    }
  }

  public void setNodePriority(final int priority) {
    _nodePriority = priority;
  }

  public int getNodePriority() {
    return _nodePriority;
  }

  protected Queue<AbstractCalculationNode> getNodes() {
    return _nodes;
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  @Override
  public int canInvoke(CalculationJobSpecification jobSpec, List<CalculationJobItem> items) {
    return getNodePriority();
  }

  @Override
  public boolean invoke(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items, final JobResultReceiver receiver) {
    final AbstractCalculationNode node = getNodes().poll();
    if (node == null) {
      return false;
    }
    final CalculationJob job = new CalculationJob(jobSpec, items);
    final Runnable invokeTask = new Runnable() {
      @Override
      public void run() {
        receiver.resultReceived(node.executeJob(job));
        addNode(node);
      }
    };
    getExecutorService().execute(invokeTask);
    return true;
  }

  private void notifyAvailable() {
    JobInvokerRegister callback = _notifyWhenAvailable.poll();
    while (callback != null) {
      callback.registerJobInvoker(this);
      callback = _notifyWhenAvailable.poll();
    }
  }

  @Override
  public void notifyWhenAvailable(final JobInvokerRegister callback) {
    _notifyWhenAvailable.add(callback);
    if (getNodes().isEmpty()) {
      synchronized (this) {
        if (!getNodes().isEmpty()) {
          notifyAvailable();
        }
      }
    } else {
      notifyAvailable();
    }
  }

}
