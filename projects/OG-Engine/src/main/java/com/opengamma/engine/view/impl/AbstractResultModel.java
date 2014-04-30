/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A simple implementation of the calculation result model.
 * 
 * <T> the type of the name
 */
/*package*/ abstract class AbstractResultModel<T> implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private final Map<T, Map<Pair<String, ValueProperties>, ComputedValueResult>> _valuesByName = new HashMap<T, Map<Pair<String, ValueProperties>, ComputedValueResult>>();

  protected Collection<T> getKeys() {
    return Collections.unmodifiableSet(_valuesByName.keySet());
  }

  protected Map<Pair<String, ValueProperties>, ComputedValueResult> getValuesByName(final T name) {
    Map<Pair<String, ValueProperties>, ComputedValueResult> values = _valuesByName.get(name);
    if (values != null) {
      return Collections.unmodifiableMap(values);
    } else {
      return null;
    }
  }

  protected Collection<ComputedValueResult> getAllValues(final T name) {
    Map<Pair<String, ValueProperties>, ComputedValueResult> values = _valuesByName.get(name);
    if (values != null) {
      return Collections.unmodifiableCollection(values.values());
    } else {
      return null;
    }
  }

  protected void addValue(final T key, final ComputedValueResult value) {
    Map<Pair<String, ValueProperties>, ComputedValueResult> valuesByName = _valuesByName.get(key);
    if (valuesByName == null) {
      valuesByName = new HashMap<Pair<String, ValueProperties>, ComputedValueResult>();
      _valuesByName.put(key, valuesByName);
    }
    if (value != null) {
      valuesByName.put(Pairs.of(value.getSpecification().getValueName(), value.getSpecification().getProperties()), value);
    }
  }

}
