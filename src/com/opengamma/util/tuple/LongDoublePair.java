/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import java.util.Map;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

/**
 * A mutable pair implementation consisting of a {@code long} and a {@code double}.
 * <p>
 * This implementation refers to the elements as 'key' and 'value'.
 * The class also implements the {@link Map.Entry} and fastutil interfaces.
 *
 * @author jim
 */
public class LongDoublePair implements Long2DoubleMap.Entry {

  /** The {@code long} element. */
  private final long _longValue;
  /** The {@code double} element. */
  private double _doubleValue;

  /**
   * Constructor.
   * @param longValue  the long element
   * @param doubleValue  the double element
   */
  public LongDoublePair(final long longValue, final double doubleValue) {
    _longValue = longValue;
    _doubleValue = doubleValue;
  }

  //-------------------------------------------------------------------------
  @Override
  public long getLongKey() {
    return _longValue;
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
  public Long getKey() {
    return _longValue;
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
    if (obj instanceof LongDoublePair) {
      final LongDoublePair other = (LongDoublePair) obj;
      return this.getLongKey() == other.getLongKey() && this.getDoubleValue() == other.getDoubleValue();
    }
    return false;
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    final long l = getLongKey();
    final long d = Double.doubleToLongBits(getDoubleValue());
    return ((int) (l ^ (l >>> 32))) ^ ((int) (d ^ (d >>> 32)));
  }

  @Override
  public String toString() {
    return "[" + getLongKey() + ", " + getDoubleValue() + "]";
  }

}
