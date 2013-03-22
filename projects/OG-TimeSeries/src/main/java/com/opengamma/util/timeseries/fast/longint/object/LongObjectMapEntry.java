/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint.object;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.util.Objects;

/**
 * Map entry class.
 */
final class LongObjectMapEntry<V> implements Long2ObjectMap.Entry<V> {

  private long _key;
  private V _value;

  LongObjectMapEntry(long key, V value) {
    _key = key;
    _value = value;
  }

  //-------------------------------------------------------------------------
  @Override
  public Long getKey() {
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
  public long getLongKey() {
    return _key;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LongObjectMapEntry) {
      LongObjectMapEntry<?> other = (LongObjectMapEntry<?>) obj;
      return _key == other._key && Objects.equals(_value, other._value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return ((int) (_key ^ (_key >>> 32))) ^ Objects.hashCode(_value);
  }

}
