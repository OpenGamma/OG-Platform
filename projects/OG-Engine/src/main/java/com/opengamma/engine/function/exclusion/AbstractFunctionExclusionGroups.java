/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.exclusion;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.opengamma.engine.function.FunctionDefinition;

/**
 * Partial implementation of {@link FunctionExclusionGroups} that caches the lookups.
 */
public abstract class AbstractFunctionExclusionGroups implements FunctionExclusionGroups {

  /**
   * Placeholder for null as the concurrent map implementation doesn't let us use null keys or values.
   */
  private static final FunctionExclusionGroup NULL = new FunctionExclusionGroup();

  private final ConcurrentMap<Object, FunctionExclusionGroup> _groupsByKey = new ConcurrentHashMap<Object, FunctionExclusionGroup>();
  private final ConcurrentMap<FunctionDefinition, FunctionExclusionGroup> _groupsByFunction = new MapMaker().weakKeys().makeMap();

  /**
   * Returns a key that identifies the exclusion group, if any.
   * 
   * @param function the function to test
   * @return the key or null if the function is not part of a group
   */
  protected abstract Object getKey(FunctionDefinition function);

  protected FunctionExclusionGroup createExclusionGroup(final Object key, final String displayName) {
    return new FunctionExclusionGroup(key, displayName);
  }

  protected String createDisplayName(final Object key) {
    return key.toString();
  }

  protected FunctionExclusionGroup createExclusionGroup(final Object key) {
    return createExclusionGroup(key, createDisplayName(key));
  }

  protected Object getKey(final FunctionExclusionGroup group) {
    return group.getKey();
  }

  protected String getDisplayName(final FunctionExclusionGroup group) {
    return group.getDisplayName();
  }

  @Override
  public FunctionExclusionGroup getExclusionGroup(final FunctionDefinition function) {
    FunctionExclusionGroup group = _groupsByFunction.get(function);
    if (group == null) {
      final Object key = getKey(function);
      if (key == null) {
        _groupsByFunction.putIfAbsent(function, NULL);
        return null;
      } else {
        group = _groupsByKey.get(key);
        if (group == null) {
          group = createExclusionGroup(key);
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

  @Override
  public boolean isExcluded(final FunctionExclusionGroup group, final Collection<FunctionExclusionGroup> existing) {
    return existing.contains(group);
  }

  @Override
  public Collection<FunctionExclusionGroup> withExclusion(final Collection<FunctionExclusionGroup> existing, final FunctionExclusionGroup newGroup) {
    final Set<FunctionExclusionGroup> result = Sets.newHashSetWithExpectedSize(existing.size() + 1);
    result.addAll(existing);
    result.add(newGroup);
    return result;
  }

}
