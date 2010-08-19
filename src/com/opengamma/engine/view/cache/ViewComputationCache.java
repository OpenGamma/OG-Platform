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

  Collection<Pair<ValueSpecification, Object>> getValues(Collection<ValueSpecification> specifications);

  void putValue(ComputedValue value);

  void putValues(Collection<ComputedValue> values);

}
