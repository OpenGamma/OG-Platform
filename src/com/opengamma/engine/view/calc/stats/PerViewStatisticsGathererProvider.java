/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.calc.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.engine.view.View;

/**
 * Partial implementation of a {@link GraphExecutorStatisticsGathererProvider} that delivers a per-view
 * {@link GraphExecutorStatisticsGatherer} instance.
 */
public abstract class PerViewStatisticsGathererProvider implements GraphExecutorStatisticsGathererProvider {

  private final ConcurrentMap<String, GraphExecutorStatisticsGatherer> _statisticsGatherers = new ConcurrentHashMap<String, GraphExecutorStatisticsGatherer>();

  @Override
  public GraphExecutorStatisticsGatherer getStatisticsGatherer(final View view) {
    GraphExecutorStatisticsGatherer stats = _statisticsGatherers.get(view.getName());
    if (stats == null) {
      stats = createStatisticsGatherer(view);
      final GraphExecutorStatisticsGatherer newStats = _statisticsGatherers.putIfAbsent(view.getName(), stats);
      if (newStats != null) {
        stats = newStats;
      }
    }
    return stats;
  }

  protected abstract GraphExecutorStatisticsGatherer createStatisticsGatherer(View view);

  public List<GraphExecutorStatisticsGatherer> getViewStatistics() {
    return new ArrayList<GraphExecutorStatisticsGatherer>(_statisticsGatherers.values());
  }

}
