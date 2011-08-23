/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import it.unimi.dsi.fastutil.doubles.Double2DoubleMap;

import com.opengamma.util.ArgumentChecker;

/**
 * An immutable pair consisting of two {@code double} elements.
 * <p>
 * The class provides direct access to the primitive types and implements
 * the relevant fastutil interface.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class DoublesPair extends Pair<Double, Double> implements Double2DoubleMap.Entry {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /** The first element. */
  public final double first; // CSIGNORE
  /** The second element. */
  public final double second; // CSIGNORE

  /**
   * Checks the specified pair is not null.
   * <p>
   * This method exists to catch instances of {@code DoublesPair} being passed to
   * {@link #of(Pair)} in an optimal way.
   * 
   * @param pair  the pair to convert, not null
   * @return the input pair, not null
   */
  public static DoublesPair of(final DoublesPair pair) {
    ArgumentChecker.notNull(pair, "pair");
    return pair;
  }

  /**
   * Creates a pair from the specified {@code Double} values.
   * 
   * @param pair  the pair to convert, not null
   * @return a pair formed by extracting values from the pair, not null
   */
  public static DoublesPair of(final Pair<Double, Double> pair) {
    if (pair instanceof DoublesPair) {
      return (DoublesPair) pair;
    }
    ArgumentChecker.notNull(pair, "pair");
    ArgumentChecker.notNull(pair.getFirst(), "pair.first");
    ArgumentChecker.notNull(pair.getSecond(), "pair.second");
    return new DoublesPair(pair.getFirst(), pair.getSecond());
  }

  /**
   * Creates a pair from the specified {@code Number} values.
   * <p>
   * This uses {@link Number#doubleValue()}.
   * 
   * @param pair  the pair to convert, not null
   * @return a pair formed by extracting values from the pair, not null
   */
  public static DoublesPair ofNumbers(final Pair<? extends Number, ? extends Number> pair) {
    if (pair instanceof DoublesPair) {
      return (DoublesPair) pair;
    }
    ArgumentChecker.notNull(pair, "pair");
    ArgumentChecker.notNull(pair.getFirst(), "pair.first");
    ArgumentChecker.notNull(pair.getSecond(), "pair.second");
    return new DoublesPair(pair.getFirst().doubleValue(), pair.getSecond().doubleValue());
  }

  /**
   * Creates a pair from the specified values.
   * 
   * @param first  the first element
   * @param second  the second element
   * @return a pair formed from the two parameters, not null
   */
  public static DoublesPair of(final double first, final double second) {
    return new DoublesPair(first, second);
  }

  /**
   * Constructs a pair.
   * 
   * @param first  the first element
   * @param second  the second element
   */
  public DoublesPair(final double first, final double second) {
    this.first = first;
    this.second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Double getFirst() {
    return first;
  }

  @Override
  public Double getSecond() {
    return second;
  }

  /**
   * Gets the first element as a primitive {@code double}.
   * 
   * @return the primitive
   */
  public double getFirstDouble() {
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
  public double getDoubleKey() {
    return first;
  }

  @Override
  public double getDoubleValue() {
    return second;
  }

  @Override
  public double setValue(double newValue) {
    throw new UnsupportedOperationException("Immutable");
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof DoublesPair) {
      final DoublesPair other = (DoublesPair) obj;
      return this.first == other.first && this.second == other.second;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    final long f = Double.doubleToLongBits(first);
    final long s = Double.doubleToLongBits(second);
    return ((int) (f ^ (f >>> 32))) ^ ((int) (s ^ (s >>> 32)));
  }

}
