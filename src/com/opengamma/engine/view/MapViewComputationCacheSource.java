/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An implementation of {@link ViewComputationCacheSource} that generates
 * {@link MapViewComputationCache}.
 *
 * @author kirk
 */
public class MapViewComputationCacheSource implements
    ViewComputationCacheSource {
  private final ConcurrentMap<Long, MapViewComputationCache> _currentCaches =
    new ConcurrentHashMap<Long, MapViewComputationCache>();

  @Override
  public ViewComputationCache getCache(long timestamp) {
    MapViewComputationCache freshCache = new MapViewComputationCache();
    MapViewComputationCache resultCache = _currentCaches.putIfAbsent(timestamp, freshCache);
    if(resultCache == null) {
      resultCache = freshCache;
    }
    return resultCache;
  }

  @Override
  public void releaseCache(long timestamp) {
    _currentCaches.remove(timestamp);
  }

}
