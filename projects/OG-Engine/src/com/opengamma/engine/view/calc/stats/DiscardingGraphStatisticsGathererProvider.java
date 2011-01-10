/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.calc.stats;

import com.opengamma.engine.view.View;

/**
 * Discards any statistics.
 */
public class DiscardingGraphStatisticsGathererProvider implements GraphExecutorStatisticsGathererProvider {

  /**
   * Instance of a statistics gatherer that doesn't do anything.
   */
  public static final GraphExecutorStatisticsGatherer GATHERER_INSTANCE = new GraphExecutorStatisticsGatherer() {

    @Override
    public void graphExecuted(String calcConfig, int nodeCount, long executionTime, long duration) {
      // No action
    }

    @Override
    public void graphProcessed(String calcConfig, int totalJobs, double meanJobSize, double meanJobCycleCost, double meanJobIOCost) {
      // No action
    }

  };

  public GraphExecutorStatisticsGatherer getStatisticsGatherer(final View view) {
    return GATHERER_INSTANCE;
  }

}
