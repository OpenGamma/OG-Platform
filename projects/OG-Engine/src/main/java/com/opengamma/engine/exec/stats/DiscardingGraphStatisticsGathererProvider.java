/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.exec.stats;

import com.opengamma.id.UniqueId;

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

  public GraphExecutorStatisticsGatherer getStatisticsGatherer(final UniqueId viewProcessId) {
    return GATHERER_INSTANCE;
  }

}
