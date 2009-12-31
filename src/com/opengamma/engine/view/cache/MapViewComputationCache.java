/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.NewComputedValue;
import com.opengamma.engine.value.ValueSpecification;

/**
 * An implementation of {@link ViewComputationCache} backed by a {@link ConcurrentHashMap}.
 *
 * @author kirk
 */
public class MapViewComputationCache implements ViewComputationCache {
  private static final Logger s_logger = LoggerFactory.getLogger(MapViewComputationCache.class);
  
  private final ConcurrentMap<ValueSpecification, NewComputedValue> _values =
    new ConcurrentHashMap<ValueSpecification, NewComputedValue>();

  @Override
  public NewComputedValue getValue(ValueSpecification specification) {
    if(specification == null) {
      return null;
    }
    return _values.get(specification);
  }

  @Override
  public void putValue(NewComputedValue value) {
    if(value == null) {
      throw new NullPointerException("Must provide a value to store.");
    }
    if(value.getSpecification() == null) {
      throw new NullPointerException("Value provided must have a specification.");
    }
    _values.put(value.getSpecification(), value);
  }
  
  public MapViewComputationCache clone() {
    MapViewComputationCache mapViewComputationCache = new MapViewComputationCache();
    mapViewComputationCache._values.putAll(_values);
    // I'm assuming here that we don't need to deep copy all of the AnalyticValues and AnalyticValueDefinitions.
    return mapViewComputationCache;
  }
  
  // for debugging.
  public void dump() {
    for(Map.Entry<ValueSpecification, NewComputedValue> entry : _values.entrySet()) {
      s_logger.info("{} => {}", entry.getKey(), entry.getValue());
    }
  }

}
