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

/**
 * A simple implementation of the calculation result model.
 */
/* package */abstract class AbstractResultModel<T> implements Serializable {

  private final Map<T, Map<String, ComputedValue>> _values = new HashMap<T, Map<String, ComputedValue>>();

  protected Collection<T> getKeys() {
    return Collections.unmodifiableSet(_values.keySet());
  }

  public Map<String, ComputedValue> getValues(final T target) {
    Map<String, ComputedValue> values = _values.get(target);
    if (values != null) {
      return Collections.unmodifiableMap(values);
    } else {
      return null;
    }
  }

  protected void addValue(final T key, final ComputedValue value) {
    Map<String, ComputedValue> values = _values.get(key);
    if (values == null) {
      values = new HashMap<String, ComputedValue>();
      _values.put(key, values);
    }
    if (value != null) {
      values.put(value.getSpecification().getValueName(), value);
    }
  }

}
