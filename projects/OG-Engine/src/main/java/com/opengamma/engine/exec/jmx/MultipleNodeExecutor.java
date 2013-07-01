/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.jmx;

import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.exec.MultipleNodeExecutorFactory;

/**
 * MultipleNodeExecutorMBean implementation.
 */
public final class MultipleNodeExecutor implements MultipleNodeExecutorMBean {

  private static final Logger s_logger = LoggerFactory.getLogger(MultipleNodeExecutor.class);

  private final MultipleNodeExecutorFactory _underlying;

  private MultipleNodeExecutor(final MultipleNodeExecutorFactory underlying) {
    _underlying = underlying;
  }

  private MultipleNodeExecutorFactory getUnderlying() {
    return _underlying;
  }

  public static void registerMBeans(final MultipleNodeExecutorFactory executor, final MBeanServer server) throws JMException {
    final ObjectName name = new ObjectName("com.opengamma:type=MultipleNodeExecutor,name=" + executor.toString());
    final MultipleNodeExecutor instance = new MultipleNodeExecutor(executor);
    try {
      server.registerMBean(instance, name);
    } catch (InstanceAlreadyExistsException e) {
      s_logger.warn("JMX MBean {} already exists - replacing", name);
      server.unregisterMBean(name);
      server.registerMBean(instance, name);
    }
  }

  @Override
  public int getMaximumConcurrency() {
    return getUnderlying().getMaximumConcurrency();
  }

  @Override
  public long getMaximumJobCost() {
    return getUnderlying().getMaximumJobCost();
  }

  @Override
  public int getMaximumJobItems() {
    return getUnderlying().getMaximumJobItems();
  }

  @Override
  public long getMinimumJobCost() {
    return getUnderlying().getMinimumJobCost();
  }

  @Override
  public int getMinimumJobItems() {
    return getUnderlying().getMinimumJobItems();
  }

  @Override
  public void setMaximumConcurrency(int maximumConcurrency) {
    getUnderlying().setMaximumConcurrency(maximumConcurrency);
    getUnderlying().invalidateCache();
  }

  @Override
  public void setMaximumJobCost(long maximumJobCost) {
    getUnderlying().setMaximumJobCost(maximumJobCost);
    getUnderlying().invalidateCache();
  }

  @Override
  public void setMaximumJobItems(int maximumJobItems) {
    getUnderlying().setMaximumJobItems(maximumJobItems);
    getUnderlying().invalidateCache();
  }

  @Override
  public void setMinimumJobCost(long minimumJobCost) {
    getUnderlying().setMinimumJobCost(minimumJobCost);
    getUnderlying().invalidateCache();
  }

  @Override
  public void setMinimumJobItems(int minimumJobItems) {
    getUnderlying().setMinimumJobItems(minimumJobItems);
    getUnderlying().invalidateCache();
  }

}
