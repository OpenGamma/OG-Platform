/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * A standard immutable triple implementation consisting of three elements.
 * <p>
 * This implementation refers to the elements as 'first', 'second' and 'third'.
 * <p>
 * Although the implementation is immutable, there is no restriction on the objects
 * that may be stored. If mutable objects are stored in the triple, then the triple
 * itself effectively becomes mutable.
 * <p>
 * This class is immutable and thread-safe if the stored objects are immutable.
 *
 * @param <A> the first element type
 * @param <B> the second element type
 * @param <C> the third element type
 */
public final class Triple<A, B, C> implements Comparable<Triple<A, B, C>>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /** The first element. */
  private final A _first;
  /** The second element. */
  private final B _second;
  /** The third element. */
  private final C _third;

  /**
   * Factory method creating a triple inferring the types.
   * 
   * @param <A> the first element type
   * @param <B> the second element type
   * @param <C> the third element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @param third  the third element, may be null
   * @return a triple formed from the three parameters, not null
   */
  public static <A, B, C> Triple<A, B, C> of(A first, B second, C third) {
    return new Triple<A, B, C>(first, second, third);
  }

  /**
   * Constructs a triple.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @param third  the third element, may be null
   */
  public Triple(A first, B second, C third) {
    _first = first;
    _second = second;
    _third = third;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first element from this pair.
   * 
   * @return the first element, may be null
   */
  public A getFirst() {
    return _first;
  }

  /**
   * Gets the second element from this pair.
   * 
   * @return the second element, may be null
   */
  public B getSecond() {
    return _second;
  }

  /**
   * Gets the third element from this pair.
   * 
   * @return the third element, may be null
   */
  public C getThird() {
    return _third;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the elements from this triple as a list.
   * <p>
   * This method supports auto-casting as they is no way in generics to provide
   * a more specific type.
   * 
   * @param <T> an auto-cast list type
   * @return the elements as a list, not null
   */
  @SuppressWarnings("unchecked")
  public <T> List<T> toList() {
    ArrayList<Object> list = new ArrayList<Object>();
    list.add(getFirst());
    list.add(getSecond());
    list.add(getThird());
    return (List<T>) list;
  }

  /**
   * Gets the first and second elements from this triple as a pair.
   * 
   * @return the first and second elements, not null
   */
  public Pair<A, B> toFirstPair() {
    return Pair.of(getFirst(), getSecond());
  }

  /**
   * Gets the first and second elements from this triple as a pair.
   * 
   * @return the second and third elements, not null
   */
  public Pair<B, C> toSecondPair() {
    return Pair.of(getSecond(), getThird());
  }

  //-------------------------------------------------------------------------
  /**
   * Compares the pair based on the first element followed by the second element.
   * 
   * @param other  the other pair, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(Triple<A, B, C> other) {
    return new CompareToBuilder().append(_first, other._first)
        .append(_second, other._second).append(_third, other._third).toComparison();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Triple<?, ?, ?>) {
      Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
      return ObjectUtils.equals(getFirst(), other.getFirst()) &&
          ObjectUtils.equals(getSecond(), other.getSecond()) &&
          ObjectUtils.equals(getThird(), other.getThird());
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getFirst() == null) ? 0 : getFirst().hashCode());
    result = prime * result + ((getSecond() == null) ? 0 : getSecond().hashCode());
    result = prime * result + ((getThird() == null) ? 0 : getThird().hashCode());
    return result;
  }

  @Override
  public String toString() {
    return new StringBuilder()
        .append("[")
        .append(getFirst())
        .append(", ")
        .append(getSecond())
        .append(", ")
        .append(getThird())
        .append("]").toString();
  }

}
