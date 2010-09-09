/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.calcnode.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;
import javax.time.InstantProvider;

/**
 * Maintains ever increasing tallies of the reported metrics. 
 */
public class TotallingNodeStatisticsGatherer implements CalculationNodeStatisticsGatherer {

  private final ConcurrentMap<String, CalculationNodeStatistics> _nodeStatistics = new ConcurrentHashMap<String, CalculationNodeStatistics>();

  protected CalculationNodeStatistics getOrCreateNodeStatistics(final String nodeId) {
    CalculationNodeStatistics stats = _nodeStatistics.get(nodeId);
    if (stats == null) {
      stats = new CalculationNodeStatistics(nodeId);
      final CalculationNodeStatistics newStats = _nodeStatistics.putIfAbsent(nodeId, stats);
      if (newStats != null) {
        stats = newStats;
      }
    }
    return stats;
  }

  @Override
  public void jobCompleted(String nodeId, int jobItems, int jobCycleCost, long executionTime, long duration) {
    getOrCreateNodeStatistics(nodeId).recordSuccessfulJob(jobItems, jobCycleCost, executionTime, duration);
  }

  @Override
  public void jobFailed(String nodeId, long duration) {
    getOrCreateNodeStatistics(nodeId).recordUnsuccessfulJob(duration);
  }

  public List<CalculationNodeStatistics> getNodeStatistics() {
    return new ArrayList<CalculationNodeStatistics>(_nodeStatistics.values());
  }

  public void dropStatisticsBefore(final InstantProvider instantProvider) {
    final Instant dropBefore = Instant.of(instantProvider);
    final Iterator<Map.Entry<String, CalculationNodeStatistics>> nodeStatisticsIterator = _nodeStatistics.entrySet().iterator();
    while (nodeStatisticsIterator.hasNext()) {
      final Map.Entry<String, CalculationNodeStatistics> nodeStatistics = nodeStatisticsIterator.next();
      if (nodeStatistics.getValue().getLastJobTime().isBefore(dropBefore)) {
        nodeStatisticsIterator.remove();
      }
    }
  }

}
