/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

/**
 * A mutable pair implementation consisting of an {@code int} and a {@code double}.
 * <p>
 * This implementation refers to the elements as 'key' and 'value'.
 * The class also implements the {@link Map.Entry} and fastutil interfaces.
 *
 * @author jim
 */
public class IntDoublePair implements Int2DoubleMap.Entry {

  /** The {@code int} element. */
  private final int _intValue;
  /** The {@code double} element. */
  private double _doubleValue;

  /**
   * Constructor.
   * @param intValue  the int element
   * @param doubleValue  the double element
   */
  public IntDoublePair(final int intValue, final double doubleValue) {
    _intValue = intValue;
    _doubleValue = doubleValue;
  }

  //-------------------------------------------------------------------------
  @Override
  public int getIntKey() {
    return _intValue;
  }

  @Override
  public double getDoubleValue() {
    return _doubleValue;
  }

  @Override
  public double setValue(final double value) {
    final double old = _doubleValue;
    _doubleValue = value;
    return old;
  }

  //-------------------------------------------------------------------------
  @Override
  public Integer getKey() {
    return _intValue;
  }

  @Override
  public Double getValue() {
    return _doubleValue;
  }

  @Override
  public Double setValue(final Double value) {
    return setValue(value);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof IntDoublePair) {
      final IntDoublePair other = (IntDoublePair) obj;
      return this.getIntKey() == other.getIntKey() && this.getDoubleValue() == other.getDoubleValue();
    }
    return false;
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    final long d = Double.doubleToLongBits(getDoubleValue());
    return getIntKey() ^ ((int) (d ^ (d >>> 32)));
  }

  @Override
  public String toString() {
    return "[" + getIntKey() + ", " + getDoubleValue() + "]";
  }

}
