/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.exclusion;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;
import com.opengamma.engine.function.FunctionDefinition;

/**
 * Partial implementation of {@link FunctionExclusionGroup.Provider} that caches the lookups.
 * 
 * @param <K> an arbitrary key to refer to the groups
 */
public abstract class AbstractFunctionExclusionGroups<K> implements FunctionExclusionGroups {

  /**
   * Placeholder for null as the concurrent map implementation doesn't let us use null keys or values.
   */
  private static final FunctionExclusionGroup NULL = new FunctionExclusionGroup();

  private final ConcurrentMap<K, FunctionExclusionGroup> _groupsByKey = new ConcurrentHashMap<K, FunctionExclusionGroup>();
  private final ConcurrentMap<FunctionDefinition, FunctionExclusionGroup> _groupsByFunction = new MapMaker().weakKeys().makeMap();

  /**
   * Returns a key that identifies the exclusion group, if any.
   * 
   * @param function the function to test
   * @return the key or null if the function is not part of a group
   */
  protected abstract K getKey(FunctionDefinition function);

  @Override
  public FunctionExclusionGroup getExclusionGroup(final FunctionDefinition function) {
    FunctionExclusionGroup group = _groupsByFunction.get(function);
    if (group == null) {
      final K key = getKey(function);
      if (key == null) {
        _groupsByFunction.putIfAbsent(function, NULL);
        return null;
      } else {
        group = _groupsByKey.get(key);
        if (group == null) {
          group = new FunctionExclusionGroup();
          final FunctionExclusionGroup existing = _groupsByKey.putIfAbsent(key, group);
          if (existing == null) {
            _groupsByFunction.putIfAbsent(function, group);
          } else {
            group = existing;
          }
        }
        return group;
      }
    } else {
      if (group != NULL) {
        return group;
      } else {
        return null;
      }
    }
  }

}
