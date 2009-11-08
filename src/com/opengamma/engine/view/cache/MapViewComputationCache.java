/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;

/**
 * An implementation of {@link ViewComputationCache} backed by a {@link ConcurrentHashMap}.
 *
 * @author kirk
 */
public class MapViewComputationCache implements ViewComputationCache {
  private final ConcurrentMap<AnalyticValueDefinition<?>, AnalyticValue<?>> _values =
    new ConcurrentHashMap<AnalyticValueDefinition<?>, AnalyticValue<?>>();

  @SuppressWarnings("unchecked")
  @Override
  public <T> AnalyticValue<T> getValue(AnalyticValueDefinition<T> definition) {
    if(definition == null) {
      return null;
    }
    return (AnalyticValue<T>) _values.get(definition);
  }

  @Override
  public <T> void putValue(AnalyticValue<T> value) {
    if(value == null) {
      throw new NullPointerException("Must provide a value to store.");
    }
    if(value.getDefinition() == null) {
      throw new NullPointerException("Value provided must have a definition.");
    }
    _values.put(value.getDefinition(), value);
  }
  
  public MapViewComputationCache clone() {
    MapViewComputationCache mapViewComputationCache = new MapViewComputationCache();
    mapViewComputationCache._values.putAll(_values);
    // I'm assuming here that we don't need to deep copy all of the AnalyticValues and AnalyticValueDefinitions.
    return mapViewComputationCache;
  }
  
  // for debugging.
  public void dump() {
    for (Entry<AnalyticValueDefinition<?>, AnalyticValue<?>> entry : _values.entrySet()) {
      System.err.println(entry.getKey()+" => "+entry.getValue());
    }
  }

}
