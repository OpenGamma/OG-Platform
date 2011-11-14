/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.calc.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.id.UniqueId;

/**
 * Maintains ever increasing tallies.
 */
public class TotallingGraphStatisticsGathererProvider extends PerViewStatisticsGathererProvider<TotallingGraphStatisticsGathererProvider.Statistics> {

  /**
   * 
   */
  public static final class Statistics implements GraphExecutorStatisticsGatherer {

    private final UniqueId _viewProcessId;
    private final ConcurrentMap<String, GraphExecutionStatistics> _statistics = new ConcurrentHashMap<String, GraphExecutionStatistics>();

    private Statistics(final UniqueId viewProcessId) {
      _viewProcessId = viewProcessId;
    }

    protected GraphExecutionStatistics getOrCreateConfiguration(final String calcConfig) {
      GraphExecutionStatistics stats = _statistics.get(calcConfig);
      if (stats == null) {
        stats = new GraphExecutionStatistics(_viewProcessId, calcConfig);
        final GraphExecutionStatistics newStats = _statistics.putIfAbsent(calcConfig, stats);
        if (newStats != null) {
          stats = newStats;
        }
      }
      return stats;
    }

    @Override
    public void graphExecuted(String calcConfig, int nodeCount, long executionTime, long duration) {
      getOrCreateConfiguration(calcConfig).recordExecution(nodeCount, executionTime, duration);
    }

    @Override
    public void graphProcessed(String calcConfig, int totalJobs, double meanJobSize, double meanJobCycleCost, double meanJobIOCost) {
      getOrCreateConfiguration(calcConfig).recordProcessing(totalJobs, meanJobSize, meanJobCycleCost, meanJobIOCost);
    }

    public List<GraphExecutionStatistics> getExecutionStatistics() {
      return new ArrayList<GraphExecutionStatistics>(_statistics.values());
    }

    public boolean dropStatisticsBefore(final InstantProvider instantProvider) {
      final Instant dropBefore = Instant.of(instantProvider);
      final Iterator<Map.Entry<String, GraphExecutionStatistics>> iterator = _statistics.entrySet().iterator();
      while (iterator.hasNext()) {
        final Map.Entry<String, GraphExecutionStatistics> entry = iterator.next();
        if (entry.getValue().getLastProcessedTime() != null && entry.getValue().getLastProcessedTime().isBefore(dropBefore)) {
          iterator.remove();
        }
      }
      return _statistics.isEmpty();
    }
    
    public UniqueId getViewProcessId() {
      return _viewProcessId;
    }

  }

  @Override
  protected Statistics createStatisticsGatherer(final UniqueId viewProcessId) {
    return new Statistics(viewProcessId);
  }

  @Override
  protected boolean dropStatisticsBefore(Statistics gatherer, Instant dropBefore) {
    return gatherer.dropStatisticsBefore(dropBefore);
  }

}
