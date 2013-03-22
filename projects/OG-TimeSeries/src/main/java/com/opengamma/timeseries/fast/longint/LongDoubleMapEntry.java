/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.longint;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

/**
 * Map entry class.
 */
final class LongDoubleMapEntry implements Long2DoubleMap.Entry {

  private long _key;
  private double _value;

  LongDoubleMapEntry(long key, double value) {
    _key = key;
    _value = value;
  }

  //-------------------------------------------------------------------------
  @Override
  public Long getKey() {
    return _key;
  }

  @Override
  public Double getValue() {
    return _value;
  }

  @Override
  public Double setValue(Double value) {
    return setValue(value.doubleValue());
  }

  @Override
  public long getLongKey() {
    return _key;
  }

  @Override
  public double setValue(double value) {
    double old = _value;
    _value = value;
    return old;
  }

  @Override
  public double getDoubleValue() {
    return _value;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LongDoubleMapEntry) {
      LongDoubleMapEntry other = (LongDoubleMapEntry) obj;
      return _key == other._key &&
          Double.doubleToLongBits(_value) == Double.doubleToLongBits(other._value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    long bits = Double.doubleToLongBits(_value);
    return ((int) (_key ^ (_key >>> 32))) ^ ((int) (bits ^ (bits >>> 32)));
  }

}
