/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.util.Collection;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * The shared cache through which various elements in view recalculation will
 * store and retrieve values.
 */
public interface ViewComputationCache {

  Object getValue(ValueSpecification specification);

  Object getValue(ValueSpecification specification, CacheSelectFilter filter);

  Collection<Pair<ValueSpecification, Object>> getValues(Collection<ValueSpecification> specifications);

  Collection<Pair<ValueSpecification, Object>> getValues(Collection<ValueSpecification> specifications, CacheSelectFilter filter);

  void putSharedValue(ComputedValue value);

  void putPrivateValue(ComputedValue value);

  void putValue(ComputedValue value, CacheSelectFilter filter);

  void putSharedValues(Collection<ComputedValue> values);

  void putPrivateValues(Collection<ComputedValue> values);

  void putValues(Collection<ComputedValue> values, CacheSelectFilter filter);

}
