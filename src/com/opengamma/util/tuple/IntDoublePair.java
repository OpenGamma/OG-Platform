/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

/**
 * An immutable pair consisting of an {@code int} and {@code double}.
 * <p>
 * The class provides direct access to the primitive types and implements
 * the relevant fastutil interface.
 */
public class IntDoublePair extends Pair<Integer, Double> implements Int2DoubleMap.Entry {

  /** The first element. */
  private final int _first;
  /** The second element. */
  private final double _second;

  /**
   * Constructor.
   * @param first  the first element
   * @param second  the second element
   */
  public IntDoublePair(final int first, final double second) {
    _first = first;
    _second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Integer getFirst() {
    return _first;
  }

  @Override
  public Double getSecond() {
    return _second;
  }

  /**
   * Gets the first element as a primitive {@code int}.
   * @return the primitive
   */
  public int getFirstInt() {
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
  public int getIntKey() {
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
    if (obj instanceof IntDoublePair) {
      final IntDoublePair other = (IntDoublePair) obj;
      return this.getFirstInt() == other.getFirstInt() && this.getSecondDouble() == other.getSecondDouble();
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    final long d = Double.doubleToLongBits(getSecondDouble());
    return getFirstInt() ^ ((int) (d ^ (d >>> 32)));
  }

}
