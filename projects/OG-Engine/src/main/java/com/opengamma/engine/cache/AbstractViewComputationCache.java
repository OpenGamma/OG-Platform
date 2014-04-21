/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Partial implementation of {@link ViewComputationCache}. A real implementation should handle the multiple value operations more efficiently whenever possible.
 */
public abstract class AbstractViewComputationCache implements ViewComputationCache {

  @Override
  public Object getValue(final ValueSpecification specification, final CacheSelectHint filter) {
    return getValue(specification);
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications) {
    return getValues(this, specifications);
  }

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications, final CacheSelectHint filter) {
    return getValues(specifications);
  }

  public static Collection<Pair<ValueSpecification, Object>> getValues(final ViewComputationCache cache, final Collection<ValueSpecification> specifications) {
    final Collection<Pair<ValueSpecification, Object>> values = new ArrayList<Pair<ValueSpecification, Object>>(specifications.size());
    for (ValueSpecification specification : specifications) {
      values.add(Pairs.of(specification, cache.getValue(specification)));
    }
    return values;
  }

  @Override
  public void putValue(final ComputedValue value, final CacheSelectHint filter) {
    putValue(this, value, filter);
  }

  public static void putValue(final ViewComputationCache cache, final ComputedValue value, final CacheSelectHint filter) {
    if (filter.isPrivateValue(value.getSpecification())) {
      cache.putPrivateValue(value);
    } else {
      cache.putSharedValue(value);
    }
  }

  @Override
  public void putSharedValues(Collection<? extends ComputedValue> values) {
    putSharedValues(this, values);
  }

  public static void putSharedValues(final ViewComputationCache cache, final Collection<? extends ComputedValue> values) {
    for (ComputedValue value : values) {
      cache.putSharedValue(value);
    }
  }

  @Override
  public void putPrivateValues(Collection<? extends ComputedValue> values) {
    putPrivateValues(this, values);
  }

  public static void putPrivateValues(final ViewComputationCache cache, final Collection<? extends ComputedValue> values) {
    for (ComputedValue value : values) {
      cache.putPrivateValue(value);
    }
  }

  @Override
  public void putValues(final Collection<? extends ComputedValue> values, final CacheSelectHint filter) {
    putValuesBatched(this, values, filter);
  }

  /**
   * Implementation of {@link #putValues} that calls through to {@link #putSharedValues} and {@link #putPrivateValues}.
   * 
   * @param cache instance
   * @param values values to put
   * @param filter cache select filter
   */
  public static void putValuesBatched(final ViewComputationCache cache, final Collection<? extends ComputedValue> values, final CacheSelectHint filter) {
    List<ComputedValue> privateValues = null;
    List<ComputedValue> sharedValues = null;
    for (ComputedValue value : values) {
      if (filter.isPrivateValue(value.getSpecification())) {
        if (privateValues == null) {
          privateValues = new ArrayList<ComputedValue>(values.size());
        }
        privateValues.add(value);
      } else {
        if (sharedValues == null) {
          sharedValues = new ArrayList<ComputedValue>(values.size());
        }
        sharedValues.add(value);
      }
    }
    if (sharedValues != null) {
      if (sharedValues.size() == 1) {
        cache.putSharedValue(sharedValues.get(0));
      } else {
        cache.putSharedValues(sharedValues);
      }
    }
    if (privateValues != null) {
      if (privateValues.size() == 1) {
        cache.putPrivateValue(privateValues.get(0));
      } else {
        cache.putPrivateValues(privateValues);
      }
    }
  }

  /**
   * Implementation of {@link #putValues} that calls through to {@link #putValue}.
   * 
   * @param cache instance
   * @param values values to put
   * @param filter cache select filter
   */
  public static void putValuesDirect(final ViewComputationCache cache, final Collection<ComputedValue> values, final CacheSelectHint filter) {
    for (ComputedValue value : values) {
      cache.putValue(value, filter);
    }
  }

}
