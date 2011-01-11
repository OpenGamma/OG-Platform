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
 * Partial implementation of a {@link GraphExecutorStatisticsGathererProvider} that delivers a per-view
 * {@link GraphExecutorStatisticsGatherer} instance.
 * 
 * @param <T>
 */
public abstract class PerViewStatisticsGathererProvider<T extends GraphExecutorStatisticsGatherer> implements GraphExecutorStatisticsGathererProvider {

  private final ConcurrentMap<String, T> _statisticsGatherers = new ConcurrentHashMap<String, T>();

  @Override
  public T getStatisticsGatherer(final View view) {
    T stats = _statisticsGatherers.get(view.getName());
    if (stats == null) {
      stats = createStatisticsGatherer(view);
      final T newStats = _statisticsGatherers.putIfAbsent(view.getName(), stats);
      if (newStats != null) {
        stats = newStats;
      }
    }
    return stats;
  }

  protected abstract T createStatisticsGatherer(View view);

  public List<T> getViewStatistics() {
    return new ArrayList<T>(_statisticsGatherers.values());
  }

  public void dropStatisticsBefore(final InstantProvider instantProvider) {
    final Instant dropBefore = Instant.of(instantProvider);
    final Iterator<Map.Entry<String, T>> iterator = _statisticsGatherers.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<String, T> entry = iterator.next();
      if (dropStatisticsBefore(entry.getValue(), dropBefore)) {
        iterator.remove();
      }
    }
  }

  protected abstract boolean dropStatisticsBefore(final T gatherer, final Instant dropBefore);

}
