/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.calcnode.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.threeten.bp.Instant;

/**
 * Gatherer that maintains ever increasing totals of the reported metrics.
 * <p>
 * This is run centrally to record the success and failure of jobs.
 */
public class TotallingNodeStatisticsGatherer implements CalculationNodeStatisticsGatherer {

  /**
   * The statistics.
   */
  private final ConcurrentMap<String, CalculationNodeStatistics> _nodeStatistics = new ConcurrentHashMap<String, CalculationNodeStatistics>();

  @Override
  public void jobCompleted(String nodeId, int jobItems, long executionTime, long duration) {
    getOrCreateNodeStatistics(nodeId).recordSuccessfulJob(jobItems, executionTime, duration);
  }

  @Override
  public void jobFailed(String nodeId, long duration) {
    getOrCreateNodeStatistics(nodeId).recordUnsuccessfulJob(duration);
  }

  /**
   * Creates the statistics for a given node.
   * 
   * @param nodeId  the node id, not null
   * @return the statistics, not null
   */
  protected CalculationNodeStatistics getOrCreateNodeStatistics(final String nodeId) {
    CalculationNodeStatistics stats = _nodeStatistics.get(nodeId);
    if (stats == null) {
      _nodeStatistics.putIfAbsent(nodeId, new CalculationNodeStatistics(nodeId));
      stats = _nodeStatistics.get(nodeId);
    }
    return stats;
  }

  /**
   * Gets the node statistics as a list.
   * <p>
   * Each statistics element is live.
   * 
   * @return an independent list of the statistics, not null
   */
  public List<CalculationNodeStatistics> getNodeStatistics() {
    return new ArrayList<CalculationNodeStatistics>(_nodeStatistics.values());
  }

  /**
   * Cleanup the statistics deleting all information before a fixed instant.
   * 
   * @param dropBefore  the instant to delete before, not null
   */
  public void dropStatisticsBefore(final Instant dropBefore) {
    final Iterator<CalculationNodeStatistics> it = _nodeStatistics.values().iterator();
    while (it.hasNext()) {
      final CalculationNodeStatistics nodeStatistics = it.next();
      if (nodeStatistics.getLastJobTime().isBefore(dropBefore)) {
        it.remove();
      }
    }
  }

}
