/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.calcnode.stats;

/**
 * Discards the statistics
 */
public final class DiscardingStatisticsGatherer implements CalculationNodeStatisticsGatherer {
  
  @Override
  public void jobCompleted(String nodeId, int jobItems, int jobCycleCost, long executionTime, long duration) {
    // No action
  }

  @Override
  public void jobFailed(String nodeId, long duration) {
    // No action
  }

}
