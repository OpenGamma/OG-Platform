/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.Collection;

import com.opengamma.util.SingletonFactoryBean;

/**
 * Factory bean for creating a {@link LocalNodeJobInvoker} instance. The local invoker is created with the supplied set of nodes and then registered with the job dispatcher (if supplied).
 */
public class LocalNodeJobInvokerFactoryBean extends SingletonFactoryBean<LocalNodeJobInvoker> {

  // This is used so that the local nodes can be created after the computation job dispatcher, allowing items dependent on that
  // to be added to the execution context before it gets cloned by local node construction.

  private Collection<SimpleCalculationNode> _nodes;

  private JobDispatcher _jobDispatcher;

  public Collection<SimpleCalculationNode> getNodes() {
    return _nodes;
  }

  public void setNodes(final Collection<SimpleCalculationNode> nodes) {
    _nodes = nodes;
  }

  public JobDispatcher getJobDispatcher() {
    return _jobDispatcher;
  }

  public void setJobDispatcher(final JobDispatcher jobDispatcher) {
    _jobDispatcher = jobDispatcher;
  }

  @Override
  protected LocalNodeJobInvoker createObject() {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker();
    if (getNodes() != null) {
      invoker.addNodes(getNodes());
    }
    if (getJobDispatcher() != null) {
      getJobDispatcher().registerJobInvoker(invoker);
    }
    return invoker;
  }

}
