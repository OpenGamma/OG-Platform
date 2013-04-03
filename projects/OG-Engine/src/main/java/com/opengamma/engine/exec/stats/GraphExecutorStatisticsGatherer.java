/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.exec.stats;

import com.opengamma.engine.calcnode.JobDispatcher;
import com.opengamma.engine.exec.DependencyGraphExecutor;

/**
 * Receives information about graph execution from a {@link DependencyGraphExecutor}.
 */
public interface GraphExecutorStatisticsGatherer {

  /**
   * Reports a graph successfully processed to a {@link JobDispatcher}.
   * 
   * @param calcConfig Calculation configuration name.
   * @param totalJobs Number of jobs to be dispatched - not all might have gone yet.
   * @param meanJobSize Mean size of the jobs.
   * @param meanJobCycleCost Mean computational cost of the jobs, or {@code NaN} if not available
   * @param meanJobIOCost Mean shared cache input/output cost of the jobs, or {@code NaN} if not available
   */
  void graphProcessed(String calcConfig, int totalJobs, double meanJobSize, double meanJobCycleCost, double meanJobIOCost);
  
  /**
   * Reports a graph successfully executed by a {@link JobDispatcher}.
   * 
   * @param calcConfig Calculation configuration name.
   * @param nodeCount Total number of nodes in the graph.
   * @param executionTime Total reported execution time, in nanoseconds.
   * @param duration Time from first starting graph processing to completion.
   */
  void graphExecuted(String calcConfig, int nodeCount, long executionTime, long duration);

}
