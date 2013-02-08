/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.tuple;

/**
 * An immutable pair consisting of an {@code int} and {@code Object}.
 * <p>
 * The class provides direct access to the primitive types and implements
 * the relevant fastutil interface.
 * <p>
 * This class is immutable and thread-safe if the stored object is immutable.
 *
 * @param <T> the type of the second side of the pair
 */
public class IntObjectPair<T> extends Pair<Integer, T> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /** The first element. */
  public final int first; // CSIGNORE
  /** The second element. */
  public final T second; // CSIGNORE

  /**
   * Creates a pair inferring the types.
   *
   * @param <B> the second element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static <B> IntObjectPair<B> of(final int first, final B second) {
    return new IntObjectPair<B>(first, second);
  }

  /**
   * Constructor.
   *
   * @param first  the first element
   * @param second  the second element
   */
  public IntObjectPair(final int first, final T second) {
    this.first = first;
    this.second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Integer getFirst() {
    return first;
  }

  @Override
  public T getSecond() {
    return second;
  }

  public int getFirstInt() {
    return first;
  }

  public T getSecondObject() {
    return second;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof IntObjectPair) {
      final IntObjectPair<T> other = (IntObjectPair<T>) obj;
      return this.first == other.first && this.second == null ? other.second == null : this.second.equals(other.second);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    return first ^ second.hashCode();
  }

}
