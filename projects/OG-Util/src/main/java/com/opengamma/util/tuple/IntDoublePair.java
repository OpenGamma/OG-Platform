/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * <p>
 * This class is immutable and thread-safe.
 */
public class IntDoublePair extends Pair<Integer, Double> implements Int2DoubleMap.Entry {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /** The first element. */
  public final int first; // CSIGNORE
  /** The second element. */
  public final double second; // CSIGNORE

  /**
   * Creates a pair from the specified values.
   * 
   * @param first  the first element
   * @param second  the second element
   * @return a pair formed from the two parameters, not null
   */
  public static IntDoublePair of(final int first, final double second) {
    return new IntDoublePair(first, second);
  }

  /**
   * Constructor.
   * @param first  the first element
   * @param second  the second element
   */
  public IntDoublePair(final int first, final double second) {
    this.first = first;
    this.second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Integer getFirst() {
    return first;
  }

  @Override
  public Double getSecond() {
    return second;
  }

  /**
   * Gets the first element as a primitive {@code int}.
   * 
   * @return the primitive
   */
  public int getFirstInt() {
    return first;
  }

  /**
   * Gets the second element as a primitive {@code double}.
   * 
   * @return the primitive
   */
  public double getSecondDouble() {
    return second;
  }

  //-------------------------------------------------------------------------
  @Override
  public int getIntKey() {
    return first;
  }

  @Override
  public double getDoubleValue() {
    return second;
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
      return this.first == other.first && this.second == other.second;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    final long d = Double.doubleToLongBits(second);
    return first ^ ((int) (d ^ (d >>> 32)));
  }

}
