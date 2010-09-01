/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.calc.stats;

import com.opengamma.engine.view.View;

/**
 * Discards any statistics.
 */
public class DiscardingStatisticsGathererProvider implements GraphExecutorStatisticsGathererProvider {

  private final GraphExecutorStatisticsGatherer _gatherer = new GraphExecutorStatisticsGatherer() {

    @Override
    public void graphExecuted(String calcConfig, int nodeCount, long executionTime, long duration) {
      // No action
    }

    @Override
    public void graphProcessed(String calcConfig, int totalJobs, int meanJobSize, int meanJobCycleCost) {
      // No action
    }

  };

  public GraphExecutorStatisticsGatherer getStatisticsGatherer(final View view) {
    return _gatherer;
  }

}
