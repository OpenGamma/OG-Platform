/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.exec.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;

/**
 * Partial implementation of a {@link GraphExecutorStatisticsGathererProvider} that delivers a per-view
 * {@link GraphExecutorStatisticsGatherer} instance.
 * 
 * @param <T>
 */
public abstract class PerViewStatisticsGathererProvider<T extends GraphExecutorStatisticsGatherer> implements GraphExecutorStatisticsGathererProvider {

  private final ConcurrentMap<UniqueId, T> _statisticsGatherers = new ConcurrentHashMap<UniqueId, T>();

  @Override
  public T getStatisticsGatherer(final UniqueId viewId) {
    T stats = _statisticsGatherers.get(viewId);
    if (stats == null) {
      stats = createStatisticsGatherer(viewId);
      final T newStats = _statisticsGatherers.putIfAbsent(viewId, stats);
      if (newStats != null) {
        stats = newStats;
      }
    }
    return stats;
  }

  protected abstract T createStatisticsGatherer(UniqueId viewId);

  public List<T> getViewStatistics() {
    return new ArrayList<T>(_statisticsGatherers.values());
  }

  public void dropStatisticsBefore(final Instant dropBefore) {
    final Iterator<Map.Entry<UniqueId, T>> iterator = _statisticsGatherers.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<UniqueId, T> entry = iterator.next();
      if (dropStatisticsBefore(entry.getValue(), dropBefore)) {
        iterator.remove();
      }
    }
  }

  protected abstract boolean dropStatisticsBefore(final T gatherer, final Instant dropBefore);

}
