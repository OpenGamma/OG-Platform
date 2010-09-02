/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Invokes jobs on one or more local calculation node implementations.
 */
public class LocalNodeJobInvoker extends AbstractCalculationNodeInvocationContainer<Queue<AbstractCalculationNode>> implements JobInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(LocalNodeJobInvoker.class);

  private final AtomicReference<JobInvokerRegister> _notifyWhenAvailable = new AtomicReference<JobInvokerRegister>();
  private final ExecutorService _executorService;

  private final Set<Capability> _capabilities = new HashSet<Capability>();

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

  public void addCapability(final Capability capability) {
    ArgumentChecker.notNull(capability, "capability");
    getCapabilities().add(capability);
  }

  public void setCapabilities(final Collection<Capability> capabilities) {
    ArgumentChecker.notNull(capabilities, "capabilities");
    getCapabilities().clear();
    getCapabilities().addAll(capabilities);
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  @Override
  public Collection<Capability> getCapabilities() {
    return _capabilities;
  }

  @Override
  public boolean invoke(final CalculationJob job, final JobInvocationReceiver receiver) {
    final AbstractCalculationNode node = getNodes().poll();
    if (node == null) {
      return false;
    }
    final Runnable invokeTask = new Runnable() {
      @Override
      public void run() {
        CalculationJobResult result = null;
        try {
          result = node.executeJob(job);
        } catch (Exception e) {
          s_logger.warn("Exception thrown by job execution", e);
          receiver.jobFailed(LocalNodeJobInvoker.this, node.getNodeId(), e);
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

  @Override
  public String toString() {
    return "local";
  }

}
