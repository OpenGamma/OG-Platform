/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.integer.object;

import java.util.Objects;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

/**
 * Map entry class.
 */
final class IntObjectMapEntry<V> implements Int2ObjectMap.Entry<V> {

  private int _key;
  private V _value;

  IntObjectMapEntry(int key, V value) {
    _key = key;
    _value = value;
  }

  //-------------------------------------------------------------------------
  @Override
  public Integer getKey() {
    return _key;
  }

  @Override
  public V getValue() {
    return _value;
  }

  @Override
  public V setValue(V value) {
    V old = _value;
    _value = value;
    return old;
  }

  @Override
  public int getIntKey() {
    return _key;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IntObjectMapEntry) {
      IntObjectMapEntry<?> other = (IntObjectMapEntry<?>) obj;
      return _key == other._key && Objects.equals(_value, other._value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _key ^ Objects.hashCode(_value);
  }

}
