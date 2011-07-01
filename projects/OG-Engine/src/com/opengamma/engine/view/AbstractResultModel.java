/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.tuple.Pair;

/**
 * A simple implementation of the calculation result model.
 * 
 * <T> the type of the name
 */
/*package*/ abstract class AbstractResultModel<T> implements Serializable {

  private final Map<T, Map<Pair<String, ValueProperties>, ComputedValue>> _valuesByName = new HashMap<T, Map<Pair<String, ValueProperties>, ComputedValue>>();

  protected Collection<T> getKeys() {
    return Collections.unmodifiableSet(_valuesByName.keySet());
  }

  protected Map<Pair<String, ValueProperties>, ComputedValue> getValuesByName(final T name) {
    Map<Pair<String, ValueProperties>, ComputedValue> values = _valuesByName.get(name);
    if (values != null) {
      return Collections.unmodifiableMap(values);
    } else {
      return null;
    }
  }

  protected Collection<ComputedValue> getAllValues(final T name) {
    Map<Pair<String, ValueProperties>, ComputedValue> values = _valuesByName.get(name);
    if (values != null) {
      return Collections.unmodifiableCollection(values.values());
    } else {
      return null;
    }
  }

  protected void addValue(final T key, final ComputedValue value) {
    Map<Pair<String, ValueProperties>, ComputedValue> valuesByName = _valuesByName.get(key);
    if (valuesByName == null) {
      valuesByName = new HashMap<Pair<String, ValueProperties>, ComputedValue>();
      _valuesByName.put(key, valuesByName);
    }
    if (value != null) {
      valuesByName.put(Pair.of(value.getSpecification().getValueName(), value.getSpecification().getProperties()), value);
    }
  }

}
