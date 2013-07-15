/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.jmx;


/**
 * JMX MBean interface for the runtime tunable factory parameters.
 */
public interface MultipleNodeExecutorMBean {
  
  void setMinimumJobItems(int minimumJobItems);
  int getMinimumJobItems();
  void setMaximumJobItems(int maximumJobItems);
  int getMaximumJobItems();
  void setMinimumJobCost(long minimumJobCost);
  long getMinimumJobCost();
  void setMaximumJobCost(long maximumJobCost);
  long getMaximumJobCost();
  void setMaximumConcurrency(int maximumConcurrency);
  int getMaximumConcurrency();

}
