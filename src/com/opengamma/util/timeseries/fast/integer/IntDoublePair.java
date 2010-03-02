/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.integer;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

/**
 * @author jim
 * 
 */
public class IntDoublePair implements Int2DoubleMap.Entry {

  private final int _intValue;
  private double _doubleValue;

  public IntDoublePair(final int intValue, final double doubleValue) {
    _intValue = intValue;
    _doubleValue = doubleValue;
  }

  @Override
  public double getDoubleValue() {
    return _doubleValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see it.unimi.dsi.fastutil.ints.Int2DoubleMap.Entry#getIntKey()
   */
  @Override
  public int getIntKey() {
    return _intValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see it.unimi.dsi.fastutil.ints.Int2DoubleMap.Entry#setValue(double)
   */
  @Override
  public double setValue(final double value) {
    final double old = _doubleValue;
    _doubleValue = value;
    return old;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map.Entry#getKey()
   */
  @Override
  public Integer getKey() {
    return _intValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map.Entry#getValue()
   */
  @Override
  public Double getValue() {
    return _doubleValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map.Entry#setValue(java.lang.Object)
   */
  @Override
  public Double setValue(final Double value) {
    return setValue(value);
  }

}
