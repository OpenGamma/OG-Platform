/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import java.io.Serializable;

/**
 * An immutable pair consisting of two {@code Object} elements.
 * <p>
 * Although the implementation is immutable, there is no restriction on the objects
 * that may be stored. If mutable objects are stored in the pair, then the pair
 * itself effectively becomes mutable.
 * <p>
 * This class is immutable and thread-safe if the stored objects are immutable.
 *
 * @param <A> the type of the first side of the pair
 * @param <B> the type of the second side of the pair
 */
public final class ObjectsPair<A, B> extends Pair<A, B> implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /** The first element. */
  public final A first; // CSIGNORE
  /** The second element. */
  public final B second; // CSIGNORE

  /**
   * Creates a pair inferring the types.
   * 
   * @param <A> the first element type
   * @param <B> the second element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static <A, B> ObjectsPair<A, B> of(A first, B second) {
    return new ObjectsPair<>(first, second);
  }

  /**
   * Constructs a pair.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   */
  public ObjectsPair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public A getFirst() {
    return first;
  }

  @Override
  public B getSecond() {
    return second;
  }

}
