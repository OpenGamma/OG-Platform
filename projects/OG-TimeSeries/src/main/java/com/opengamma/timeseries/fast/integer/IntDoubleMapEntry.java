/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.integer;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

/**
 * Map entry class.
 */
final class IntDoubleMapEntry implements Int2DoubleMap.Entry {

  private int _key;
  private double _value;

  IntDoubleMapEntry(int key, double value) {
    _key = key;
    _value = value;
  }

  //-------------------------------------------------------------------------
  @Override
  public Integer getKey() {
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
  public int getIntKey() {
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
    if (obj instanceof IntDoubleMapEntry) {
      IntDoubleMapEntry other = (IntDoubleMapEntry) obj;
      return _key == other._key &&
          Double.doubleToLongBits(_value) == Double.doubleToLongBits(other._value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    long bits = Double.doubleToLongBits(_value);
    return _key ^ ((int) (bits ^ (bits >>> 32)));
  }

}
