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

import com.opengamma.engine.view.View;

/**
 * Maintains ever increasing tallies.
 */
public class TotallingGraphStatisticsGathererProvider extends PerViewStatisticsGathererProvider<TotallingGraphStatisticsGathererProvider.Statistics> {

  /**
   * 
   */
  public static final class Statistics implements GraphExecutorStatisticsGatherer {

    private final String _viewName;
    private final ConcurrentMap<String, GraphExecutionStatistics> _statistics = new ConcurrentHashMap<String, GraphExecutionStatistics>();

    private Statistics(final String viewName) {
      _viewName = viewName;
    }

    protected GraphExecutionStatistics getOrCreateConfiguration(final String calcConfig) {
      GraphExecutionStatistics stats = _statistics.get(calcConfig);
      if (stats == null) {
        stats = new GraphExecutionStatistics(_viewName, calcConfig);
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
        if (entry.getValue().getLastProcessedTime().isBefore(dropBefore)) {
          iterator.remove();
        }
      }
      return _statistics.isEmpty();
    }
    
    public String getViewName() {
      return _viewName;
    }

  }

  @Override
  protected Statistics createStatisticsGatherer(final View view) {
    return new Statistics(view.getName());
  }

  @Override
  protected boolean dropStatisticsBefore(Statistics gatherer, Instant dropBefore) {
    return gatherer.dropStatisticsBefore(dropBefore);
  }

}
