/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.jmx;

/**
 * JMX MBean interface for the runtime tunable factory parameters.
 */
public interface StaticSequencePartitioningViewProcessWorkerFactoryMBean {

  void setNumConcurrentWorkersPerProcess(int saturation);

  int getNumConcurrentWorkersPerProcess();

  void setMinimumCyclesPerWorker(int cycles);

  int getMinimumCyclesPerWorker();

  void setMaximumCyclesPerWorker(int cycles);

  int getMaximumCyclesPerWorker();

}
