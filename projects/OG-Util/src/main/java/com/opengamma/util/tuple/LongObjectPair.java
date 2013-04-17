/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.util.Objects;

/**
 * An immutable pair consisting of an {@code long} and {@code Object}.
 * <p>
 * The class provides direct access to the primitive types and implements
 * the relevant fastutil interface.
 * <p>
 * This class is immutable and thread-safe if the stored object is immutable.
 *
 * @param <T> the type of the second side of the pair
 */
public class LongObjectPair<T> extends Pair<Long, T> implements Long2ObjectMap.Entry<T> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /** The first element. */
  public final long first; // CSIGNORE
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
  public static <B> LongObjectPair<B> of(final long first, final B second) {
    return new LongObjectPair<>(first, second);
  }

  /**
   * Constructor.
   * 
   * @param first  the first element
   * @param second  the second element
   */
  public LongObjectPair(final long first, final T second) {
    this.first = first;
    this.second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Long getFirst() {
    return first;
  }

  @Override
  public T getSecond() {
    return second;
  }

  public long getFirstLong() {
    return first;
  }

  public T getSecondObject() {
    return second;
  }

  //-------------------------------------------------------------------------
  @Override
  public long getLongKey() {
    return first;
  }

  @Override
  public T setValue(final T value) {
    throw new UnsupportedOperationException("Immutable");
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof LongObjectPair) {
      final LongObjectPair<T> other = (LongObjectPair<T>) obj;
      return this.first == other.first && Objects.equals(this.second, other.second);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    return ((int) (first ^ (first >>> 32))) ^ second.hashCode();
  }

}
