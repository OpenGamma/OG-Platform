/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.engine.value.AnalyticValueDefinition;
import com.opengamma.engine.value.ComputedValue;

/**
 * An implementation of {@link ViewComputationCache} backed by a {@link ConcurrentHashMap}.
 *
 * @author kirk
 */
public class MapViewComputationCache implements ViewComputationCache {
  private final ConcurrentMap<AnalyticValueDefinition<?>, ComputedValue<?>> _values =
    new ConcurrentHashMap<AnalyticValueDefinition<?>, ComputedValue<?>>();

  @SuppressWarnings("unchecked")
  @Override
  public <T> ComputedValue<T> getValue(AnalyticValueDefinition<T> definition) {
    if(definition == null) {
      return null;
    }
    return (ComputedValue<T>) _values.get(definition);
  }

  @Override
  public <T> void putValue(ComputedValue<T> value) {
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
    for (Entry<AnalyticValueDefinition<?>, ComputedValue<?>> entry : _values.entrySet()) {
      System.err.println(entry.getKey()+" => "+entry.getValue());
    }
  }

}
