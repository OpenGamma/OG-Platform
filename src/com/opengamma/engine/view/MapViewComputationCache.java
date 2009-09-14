/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

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
  private final ConcurrentMap<AnalyticValueDefinition, AnalyticValue> _values =
    new ConcurrentHashMap<AnalyticValueDefinition, AnalyticValue>();

  @Override
  public AnalyticValue getValue(AnalyticValueDefinition definition) {
    if(definition == null) {
      return null;
    }
    return _values.get(definition);
  }

  @Override
  public void putValue(AnalyticValue value) {
    if(value == null) {
      throw new NullPointerException("Must provide a value to store.");
    }
    if(value.getDefinition() == null) {
      throw new NullPointerException("Value provided must have a definition.");
    }
    _values.put(value.getDefinition(), value);
  }

}
