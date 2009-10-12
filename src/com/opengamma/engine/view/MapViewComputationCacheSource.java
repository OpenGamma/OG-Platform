/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link ViewComputationCacheSource} that generates
 * {@link MapViewComputationCache}.
 *
 * @author kirk
 */
public class MapViewComputationCacheSource
implements ViewComputationCacheSource {
  private final Map<String, Map<Long, MapViewComputationCache>> _currentCaches =
    new HashMap<String, Map<Long, MapViewComputationCache>>();

  @Override
  public synchronized ViewComputationCache getCache(String viewName, long timestamp) {
    Map<Long, MapViewComputationCache> viewCaches = _currentCaches.get(viewName);
    if(viewCaches == null) {
      viewCaches = new HashMap<Long, MapViewComputationCache>();
      _currentCaches.put(viewName, viewCaches);
    }
    MapViewComputationCache cache = viewCaches.get(timestamp);
    if(cache == null) {
      cache = new MapViewComputationCache();
      viewCaches.put(timestamp, cache);
    }
    return cache;
  }
  
  public synchronized ViewComputationCache cloneCache(String viewName, long timestamp) {
    MapViewComputationCache cache = (MapViewComputationCache) getCache(viewName, timestamp);
    return cache.clone();
  }

  @Override
  public synchronized void releaseCache(String viewName, long timestamp) {
    Map<Long, MapViewComputationCache> viewCaches = _currentCaches.get(viewName);
    if(viewCaches != null) {
      viewCaches.remove(timestamp);
    }
  }

}
