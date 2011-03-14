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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;

/**
 * A simple implementation of the calculation result model.
 */
/* package */abstract class AbstractResultModel<T> implements Serializable {

  private final Map<T, Map<String, ComputedValue>> _valuesByName = new HashMap<T, Map<String, ComputedValue>>();
  private final Map<T, Set<ComputedValue>> _allValues = new HashMap<T, Set<ComputedValue>>();

  protected Collection<T> getKeys() {
    return Collections.unmodifiableSet(_allValues.keySet());
  }

  protected Map<String, ComputedValue> getValuesByName(final T target) {
    Map<String, ComputedValue> values = _valuesByName.get(target);
    if (values != null) {
      return Collections.unmodifiableMap(values);
    } else {
      return null;
    }
  }

  protected Set<ComputedValue> getAllValues(final T target) {
    Set<ComputedValue> values = _allValues.get(target);
    if (values != null) {
      return Collections.unmodifiableSet(values);
    } else {
      return null;
    }
  }

  protected void addValue(final T key, final ComputedValue value) {
    Map<String, ComputedValue> valuesByName = _valuesByName.get(key);
    Set<ComputedValue> allValues = _allValues.get(key);
    if (valuesByName == null) {
      valuesByName = new HashMap<String, ComputedValue>();
      _valuesByName.put(key, valuesByName);
      allValues = new HashSet<ComputedValue>();
      _allValues.put(key, allValues);
    }
    if (value != null) {
      valuesByName.put(value.getSpecification().getValueName(), value);
      allValues.add(value);
    }
  }

}
