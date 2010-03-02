/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

/**
 * @author jim
 * 
 */
public class LongDoublePair implements Long2DoubleMap.Entry {

  private final long _longValue;
  private double _doubleValue;

  public LongDoublePair(final long longValue, final double doubleValue) {
    _longValue = longValue;
    _doubleValue = doubleValue;
  }

  @Override
  public double getDoubleValue() {
    return _doubleValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see it.unimi.dsi.fastutil.longs.Long2DoubleMap.Entry#getLongKey()
   */
  @Override
  public long getLongKey() {
    return _longValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see it.unimi.dsi.fastutil.longs.Long2DoubleMap.Entry#setValue(double)
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
  public Long getKey() {
    return _longValue;
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
