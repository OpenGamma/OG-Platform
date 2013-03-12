/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.calcnode.stats;

import com.opengamma.engine.calcnode.JobDispatcher;

/**
 * Receives information about job execution from the {@link JobDispatcher}.
 */
public interface CalculationNodeStatisticsGatherer {

  /**
   * Reports a job successfully completed.
   * 
   * @param nodeId  the node the job completed on
   * @param jobItems  the number of items in the job
   * @param executionNanos  the time reported by the node, in nanoseconds
   * @param durationNanos  the time from first scheduling to completion, in nanoseconds
   */
  void jobCompleted(String nodeId, int jobItems, long executionNanos, long durationNanos);

  /**
   * Reports a job failure.
   * 
   * @param nodeId  the node the job failed on
   * @param durationNanos  the time from scheduling to failure, in nanoseconds
   */
  void jobFailed(String nodeId, long durationNanos);

}
