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

import com.opengamma.id.UniqueIdentifier;

/**
 * Partial implementation of a {@link GraphExecutorStatisticsGathererProvider} that delivers a per-view
 * {@link GraphExecutorStatisticsGatherer} instance.
 * 
 * @param <T>
 */
public abstract class PerViewStatisticsGathererProvider<T extends GraphExecutorStatisticsGatherer> implements GraphExecutorStatisticsGathererProvider {

  private final ConcurrentMap<UniqueIdentifier, T> _statisticsGatherers = new ConcurrentHashMap<UniqueIdentifier, T>();

  @Override
  public T getStatisticsGatherer(final UniqueIdentifier viewId) {
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

  protected abstract T createStatisticsGatherer(UniqueIdentifier viewId);

  public List<T> getViewStatistics() {
    return new ArrayList<T>(_statisticsGatherers.values());
  }

  public void dropStatisticsBefore(final InstantProvider instantProvider) {
    final Instant dropBefore = Instant.of(instantProvider);
    final Iterator<Map.Entry<UniqueIdentifier, T>> iterator = _statisticsGatherers.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<UniqueIdentifier, T> entry = iterator.next();
      if (dropStatisticsBefore(entry.getValue(), dropBefore)) {
        iterator.remove();
      }
    }
  }

  protected abstract boolean dropStatisticsBefore(final T gatherer, final Instant dropBefore);

}
