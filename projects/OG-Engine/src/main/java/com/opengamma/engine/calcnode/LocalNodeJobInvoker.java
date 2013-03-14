/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Invokes jobs on one or more local calculation node implementations.
 */
public class LocalNodeJobInvoker extends SimpleCalculationNodeInvocationContainer implements JobInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(LocalNodeJobInvoker.class);

  private final AtomicReference<JobInvokerRegister> _notifyWhenAvailable = new AtomicReference<JobInvokerRegister>();
  private final CapabilitySet _capabilitySet = new CapabilitySet();

  private String _invokerId = "local";

  public LocalNodeJobInvoker() {
  }

  public LocalNodeJobInvoker(final SimpleCalculationNode node) {
    addNode(node);
    recalculateCapabilities();
  }

  public LocalNodeJobInvoker(final Collection<SimpleCalculationNode> nodes) {
    addNodes(nodes);
    recalculateCapabilities();
  }

  /**
   * Updates the capabilities. Call this if the set of nodes has changed.
   */
  public void recalculateCapabilities() {
    setCapability(PlatformCapabilities.NODE_COUNT, getNodes().size());
  }

  public void setCapability(final String identifier, final double parameter) {
    getCapabilitySet().setParameterCapability(identifier, parameter);
  }

  /**
   * For injecting capabilities from spring.
   * 
   * @param parameters capabilities
   */
  public void setCapabilities(final Map<String, Double> parameters) {
    for (Map.Entry<String, Double> parameter : parameters.entrySet()) {
      setCapability(parameter.getKey(), parameter.getValue());
    }
  }

  @Override
  public void onNodeChange() {
    final JobInvokerRegister notify = _notifyWhenAvailable.getAndSet(null);
    if (notify != null) {
      notify.registerJobInvoker(this);
    }
  }

  protected CapabilitySet getCapabilitySet() {
    return _capabilitySet;
  }

  @Override
  public Collection<Capability> getCapabilities() {
    return getCapabilitySet().getCapabilities();
  }

  private void addTail(final Collection<CalculationJob> tails, final ExecutionReceiver executionReceiver) {
    if (tails != null) {
      for (CalculationJob tail : tails) {
        addJob(tail, executionReceiver, null);
        addTail(tail.getTail(), executionReceiver);
      }
    }
  }

  @Override
  public boolean invoke(final CalculationJob job, final JobInvocationReceiver receiver) {
    final SimpleCalculationNode node = getNodes().poll();
    if (node == null) {
      return false;
    }
    final ExecutionReceiver executionReceiver = new ExecutionReceiver() {

      @Override
      public void executionComplete(CalculationJobResult result) {
        receiver.jobCompleted(result);
      }

      @Override
      public void executionFailed(SimpleCalculationNode node, Exception exception) {
        s_logger.warn("Exception thrown by job execution", exception);
        receiver.jobFailed(LocalNodeJobInvoker.this, node.getNodeId(), exception);
      }

    };
    addJob(job, executionReceiver, node);
    addTail(job.getTail(), executionReceiver);
    return true;
  }

  @Override
  public void cancel(final Collection<CalculationJobSpecification> jobs) {
    for (CalculationJobSpecification job : jobs) {
      cancel(job);
    }
  }

  @Override
  public boolean isAlive(final Collection<CalculationJobSpecification> jobs) {
    for (CalculationJobSpecification job : jobs) {
      if (!isAlive(job)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected void onJobExecutionComplete() {
    onNodeChange();
  }

  @Override
  public boolean notifyWhenAvailable(JobInvokerRegister callback) {
    _notifyWhenAvailable.set(callback);
    if (!getNodes().isEmpty()) {
      callback = _notifyWhenAvailable.getAndSet(null);
      if (callback != null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return getInvokerId();
  }

  @Override
  public String getInvokerId() {
    return _invokerId;
  }

  public void setInvokerId(final String invokerId) {
    ArgumentChecker.notNull(invokerId, "invokerId");
    _invokerId = invokerId;
  }

}
