/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.opengamma.util.tuple.Pair;


/**
 * An implementation of {@link ViewComputationCacheSource} that generates
 * {@link MapViewComputationCache}.
 *
 * @author kirk
 */
public class MapViewComputationCacheSource
implements ViewComputationCacheSource {
  private final Map<String, Map<Pair<String,Long>, MapViewComputationCache>> _currentCaches =
    new HashMap<String, Map<Pair<String,Long>, MapViewComputationCache>>();

  @Override
  public synchronized ViewComputationCache getCache(String viewName, String calcConfigName, long timestamp) {
    Map<Pair<String,Long>, MapViewComputationCache> viewCaches = _currentCaches.get(viewName);
    if(viewCaches == null) {
      viewCaches = new HashMap<Pair<String,Long>, MapViewComputationCache>();
      _currentCaches.put(viewName, viewCaches);
    }
    Pair<String,Long> cacheKey = new Pair<String,Long>(calcConfigName,timestamp);
    MapViewComputationCache cache = viewCaches.get(cacheKey);
    if(cache == null) {
      cache = new MapViewComputationCache();
      viewCaches.put(cacheKey, cache);
    }
    return cache;
  }
  
  public synchronized ViewComputationCache cloneCache(String viewName, String calcConfigName, long timestamp) {
    MapViewComputationCache cache = (MapViewComputationCache) getCache(viewName, calcConfigName, timestamp); 
    return cache.clone();
  }

  @Override
  public synchronized void releaseCaches(String viewName, long timestamp) {
    Map<Pair<String,Long>, MapViewComputationCache> viewCaches = _currentCaches.get(viewName);
    if(viewCaches != null) {
      Iterator<Map.Entry<Pair<String,Long>, MapViewComputationCache>> entryIter = viewCaches.entrySet().iterator();
      while(entryIter.hasNext()) {
        Map.Entry<Pair<String,Long>, MapViewComputationCache> entry = entryIter.next();
        if(entry.getKey().getSecond() == timestamp) {
          entryIter.remove();
        }
      }
    }
  }

}
