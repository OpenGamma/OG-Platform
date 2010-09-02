/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.calcnode.stats;

/**
 * Receives information about job execution from the {@link JobDispatcher}.
 */
public interface StatisticsGatherer {

  /**
   * Reports a job successfully completed.
   * 
   * @param nodeId Node the job completed on
   * @param executionTime Time reported by the node, in nanoseconds
   * @param duration Time from first scheduling to completion
   */
  void jobCompleted(String nodeId, long executionTime, long duration);

  /**
   * Reports a job failure.
   * 
   * @param nodeId Node the job failed on
   * @param duration Time from scheduling to failure, in nanoseconds
   */
  void jobFailed(String nodeId, long duration);

}
