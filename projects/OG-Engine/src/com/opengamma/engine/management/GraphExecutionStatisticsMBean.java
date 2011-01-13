/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.management;


/**
 * A management bean for a GrapExecutionStatistics
 *
 */
public interface GraphExecutionStatisticsMBean {

  String getViewName();

  String getCalcConfigName();

  long getProcessedGraphs();

  long getExecutedGraphs();

  long getExecutedNodes();

  long getExecutionTime();

  long getActualTime();

  long getProcessedJobs();

  long getProcessedJobSize();

  long getProcessedJobCycleCost();

  long getProcessedJobDataCost();

  String getLastProcessedTime();

  String getLastExecutedTime();

  double getAverageGraphSize();

  double getAverageExecutionTime();

  double getAverageActualTime();

  double getAverageJobSize();

  double getAverageJobCycleCost();

  double getAverageJobDataCost();

  void reset();

}
