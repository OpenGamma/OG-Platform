/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.jmx;

import javax.management.InstanceAlreadyExistsException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * JMX exposure of the sequence partitioning worker factory.
 */
public final class StaticSequencePartitioningViewProcessWorkerFactory implements StaticSequencePartitioningViewProcessWorkerFactoryMBean {

  private final com.opengamma.engine.view.worker.StaticSequencePartitioningViewProcessWorkerFactory _factory;

  private StaticSequencePartitioningViewProcessWorkerFactory(com.opengamma.engine.view.worker.StaticSequencePartitioningViewProcessWorkerFactory factory) {
    _factory = factory;
  }

  private com.opengamma.engine.view.worker.StaticSequencePartitioningViewProcessWorkerFactory getFactory() {
    return _factory;
  }

  protected void registerMBean(final MBeanServer server) throws JMException {
    final ObjectName name = new ObjectName("com.opengamma:type=ViewProcessWorkerFactory,name=StaticSequencePartitioningViewProcessWorkerFactory");
    try {
      server.registerMBean(this, name);
    } catch (InstanceAlreadyExistsException e) {
      server.unregisterMBean(name);
      server.registerMBean(this, name);
    }
  }

  public static void registerMBeans(final com.opengamma.engine.view.worker.StaticSequencePartitioningViewProcessWorkerFactory factory, final MBeanServer server) throws JMException {
    new StaticSequencePartitioningViewProcessWorkerFactory(factory).registerMBean(server);
  }

  // StaticSequencePartitioningViewProcessWorkerFactoryBean

  @Override
  public void setNumConcurrentWorkersPerProcess(int saturation) {
    getFactory().setSaturation(saturation);
  }

  @Override
  public int getNumConcurrentWorkersPerProcess() {
    return getFactory().getSaturation();
  }

  @Override
  public void setMinimumCyclesPerWorker(int cycles) {
    getFactory().setMinimumCycles(cycles);
  }

  @Override
  public int getMinimumCyclesPerWorker() {
    return getFactory().getMinimumCycles();
  }

  @Override
  public void setMaximumCyclesPerWorker(int cycles) {
    getFactory().setMaximumCycles(cycles);
  }

  @Override
  public int getMaximumCyclesPerWorker() {
    return getFactory().getMaximumCycles();
  }

}
