/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Invokes jobs on one or more local calculation node implementations.
 */
public class LocalNodeJobInvoker extends AbstractCalculationNodeInvocationContainer implements JobInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(LocalNodeJobInvoker.class);

  private final AtomicReference<JobInvokerRegister> _notifyWhenAvailable = new AtomicReference<JobInvokerRegister>();

  private final Set<Capability> _capabilities = new HashSet<Capability>();

  public LocalNodeJobInvoker() {
  }

  public LocalNodeJobInvoker(final AbstractCalculationNode node) {
    addNode(node);
  }

  public LocalNodeJobInvoker(final Collection<AbstractCalculationNode> nodes) {
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
    final ExecutionReceiver executionReceiver = new ExecutionReceiver() {

      @Override
      public void executionComplete(CalculationJobResult result) {
        receiver.jobCompleted(result);
      }

      @Override
      public void executionFailed(AbstractCalculationNode node, Exception exception) {
        s_logger.warn("Exception thrown by job execution", exception);
        receiver.jobFailed(LocalNodeJobInvoker.this, node.getNodeId(), exception);
      }

    };
    getExecutorService().execute(new Runnable() {

      private void addTail(final Collection<CalculationJob> tails) {
        if (tails != null) {
          for (CalculationJob tail : tails) {
            addJob(tail, executionReceiver, null);
            addTail(tail.getTail());
          }
        }
      }

      @Override
      public void run() {
        addTail(job.getTail());
        addJob(job, executionReceiver, node);
      }
      
    });
    return true;
  }

  @Override
  protected void onJobExecutionComplete() {
    onNodeChange();
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
