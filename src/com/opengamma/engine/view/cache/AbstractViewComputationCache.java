/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.ArrayList;
import java.util.Collection;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * Partial implementation of {@link ViewComputationCache}. A real implementation should
 * handle the multiple value operations more efficiently whenever possible.
 */
public abstract class AbstractViewComputationCache implements ViewComputationCache {

  @Override
  public Collection<Pair<ValueSpecification, Object>> getValues(final Collection<ValueSpecification> specifications) {
    return getValues(this, specifications);
  }

  @Override
  public void putValues(Collection<ComputedValue> values) {
    putValues(this, values);
  }

  public static Collection<Pair<ValueSpecification, Object>> getValues(final ViewComputationCache cache, final Collection<ValueSpecification> specifications) {
    final Collection<Pair<ValueSpecification, Object>> values = new ArrayList<Pair<ValueSpecification, Object>>(specifications.size());
    for (ValueSpecification specification : specifications) {
      values.add(Pair.of(specification, cache.getValue(specification)));
    }
    return values;
  }

  public static void putValues(final ViewComputationCache cache, final Collection<ComputedValue> values) {
    for (ComputedValue value : values) {
      cache.putValue(value);
    }
  }

}
