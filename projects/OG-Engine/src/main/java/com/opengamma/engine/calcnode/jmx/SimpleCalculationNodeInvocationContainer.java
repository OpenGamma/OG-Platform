/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.jmx;

import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.calcnode.SimpleCalculationNode;
import com.opengamma.engine.calcnode.SimpleCalculationNodeFactory;

/**
 * JMX exposure of a node container.
 * 
 * @param <T> the container type
 */
public class SimpleCalculationNodeInvocationContainer<T extends com.opengamma.engine.calcnode.SimpleCalculationNodeInvocationContainer> implements SimpleCalculationNodeInvocationContainerMBean {

  private static final Logger s_logger = LoggerFactory.getLogger(CalculationNodesMBean.class);

  private final T _container;
  private final SimpleCalculationNodeFactory _nodeFactory;

  protected SimpleCalculationNodeInvocationContainer(final T container, final SimpleCalculationNodeFactory nodeFactory) {
    _container = container;
    _nodeFactory = nodeFactory;
  }

  protected T getContainer() {
    return _container;
  }

  protected SimpleCalculationNodeFactory getNodeFactory() {
    return _nodeFactory;
  }

  protected void registerMBean(final MBeanServer server) throws JMException {
    final ObjectName name = new ObjectName("com.opengamma:type=CalculationNodes,name=" + getContainer().toString());
    try {
      server.registerMBean(this, name);
    } catch (InstanceAlreadyExistsException e) {
      s_logger.warn("JMX MBean {} already exists - replacing", name);
      server.unregisterMBean(name);
      server.registerMBean(this, name);
    }
  }

  public static void registerMBeans(final com.opengamma.engine.calcnode.SimpleCalculationNodeInvocationContainer container, final SimpleCalculationNodeFactory nodeFactory,
      final MBeanServer server) throws JMException {
    new SimpleCalculationNodeInvocationContainer<com.opengamma.engine.calcnode.SimpleCalculationNodeInvocationContainer>(container, nodeFactory).registerMBean(server);
  }

  @Override
  public int getTotalNodeCount() {
    return getContainer().getTotalNodeCount();
  }

  @Override
  public int getAvailableNodeCount() {
    return getContainer().getAvailableNodeCount();
  }

  @Override
  public int getTotalJobCount() {
    return getContainer().getTotalJobCount();
  }

  @Override
  public int getRunnableJobCount() {
    return getContainer().getRunnableJobCount();
  }

  @Override
  public int getPartialJobCount() {
    return getContainer().getPartialJobCount();
  }

  @Override
  public String removeNode() {
    for (int i = 10; i > 0; i--) {
      final SimpleCalculationNode node = getContainer().removeNode();
      if (node != null) {
        return "Removed " + node.getNodeId() + " from container";
      }
      if (getTotalNodeCount() == 0) {
        return "Total node count is zero";
      }
      if (i > 1) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          throw new OpenGammaRuntimeException("interrupted", e);
        }
      }
    }
    return "No available nodes to remove";
  }

  @Override
  public String addNode() {
    final SimpleCalculationNode node = getNodeFactory().createNode();
    getContainer().addNode(node);
    return "Added " + node.getNodeId() + " to container";
  }

}
