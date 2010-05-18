/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

/**
 * An immutable pair consisting of an {@code long} and {@code double}.
 * <p>
 * The class provides direct access to the primitive types and implements
 * the relevant fastutil interface.
 */
public class LongDoublePair extends Pair<Long, Double> implements Long2DoubleMap.Entry {

  /** The first element. */
  private final long _first;
  /** The second element. */
  private final double _second;

  /**
   * Constructor.
   * @param first  the first element
   * @param second  the second element
   */
  public LongDoublePair(final long first, final double second) {
    _first = first;
    _second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Long getFirst() {
    return _first;
  }

  @Override
  public Double getSecond() {
    return _second;
  }

  /**
   * Gets the first element as a primitive {@code long}.
   * @return the primitive
   */
  public long getFirstLong() {
    return _first;
  }

  /**
   * Gets the second element as a primitive {@code double}.
   * @return the primitive
   */
  public double getSecondDouble() {
    return _second;
  }

  //-------------------------------------------------------------------------
  @Override
  public long getLongKey() {
    return _first;
  }

  @Override
  public double getDoubleValue() {
    return _second;
  }

  @Override
  public double setValue(final double value) {
    throw new UnsupportedOperationException("Immutable");
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof LongDoublePair) {
      final LongDoublePair other = (LongDoublePair) obj;
      return this.getFirstLong() == other.getFirstLong() && this.getSecondDouble() == other.getSecondDouble();
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    final long f = getFirstLong();
    final long s = Double.doubleToLongBits(getSecondDouble());
    return ((int) (f ^ (f >>> 32))) ^ ((int) (s ^ (s >>> 32)));
  }

}
