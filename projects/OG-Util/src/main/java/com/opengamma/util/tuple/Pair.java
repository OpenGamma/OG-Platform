/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.CompareToBuilder;

import com.opengamma.util.PublicAPI;

/**
 * An immutable pair consisting of two elements.
 * <p>
 * This implementation refers to the elements as 'first' and 'second'.
 * The class also implements the {@code Map.Entry} interface where the key is 'first'
 * and the value is 'second'.
 * <p>
 * Although the implementation is immutable, there is no restriction on the objects
 * that may be stored. If mutable objects are stored in the pair, then the pair itself
 * effectively becomes mutable.
 *
 * @param <A> the first element type
 * @param <B> the second element type
 */
@PublicAPI
public abstract class Pair<A, B> implements Map.Entry<A, B>, Comparable<Pair<A, B>>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a pair of {@code Object}s inferring the types.
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
   * Creates a pair of {@code Double}s.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static ObjectsPair<Double, Double> of(Double first, double second) {
    return new ObjectsPair<>(first, second);
  }

  /**
   * Creates a pair of {@code Double}s.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static ObjectsPair<Double, Double> of(double first, Double second) {
    return new ObjectsPair<>(first, second);
  }

  /**
   * Creates a pair of {@code double}s.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static DoublesPair of(double first, double second) {
    return new DoublesPair(first, second);
  }

  /**
   * Creates a pair of {@code int} to {@code double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static IntDoublePair of(int first, double second) {
    return new IntDoublePair(first, second);
  }

  /**
   * Creates a pair of {@code long} to {@code double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static LongDoublePair of(long first, double second) {
    return new LongDoublePair(first, second);
  }

  /**
   * Constructs a pair.
   */
  protected Pair() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first element from this pair.
   * <p>
   * When treated as a key-value pair, this is the key.
   * 
   * @return the first element, may be null
   */
  public abstract A getFirst();

  /**
   * Gets the second element from this pair.
   * <p>
   * When treated as a key-value pair, this is the value.
   * 
   * @return the second element, may be null
   */
  public abstract B getSecond();

  /**
   * Gets the key from this pair.
   * <p>
   * This method implements the {@code Map.Entry} interface returning the
   * first element as the key.
   * 
   * @return the first element as the key, may be null
   */
  @Override
  public A getKey() {
    return getFirst();
  }

  /**
   * Gets the value from this pair.
   * <p>
   * This method implements the {@code Map.Entry} interface returning the
   * second element as the value.
   * 
   * @return the second element as the value, may be null
   */
  @Override
  public B getValue() {
    return getSecond();
  }

  /**
   * Throws {@code UnsupportedOperationException} as this class is immutable.
   * 
   * @param value  the new value, may be null
   * @return never
   */
  @Override
  public B setValue(B value) {
    throw new UnsupportedOperationException("Pair is immutable");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the elements from this pair as a list.
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
    return (List<T>) list;
  }

  //-------------------------------------------------------------------------
  /**
   * Compares the pair based on the first element followed by the second element.
   * The types must be {@code Comparable}.
   * 
   * @param other  the other pair, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(Pair<A, B> other) {
    return new CompareToBuilder().append(getFirst(), other.getFirst())
        .append(getSecond(), other.getSecond()).toComparison();
  }

  @Override
  public boolean equals(Object obj) {
    // see Map.Entry API specification
    if (this == obj) {
      return true;
    }
    if (obj instanceof Map.Entry<?, ?>) {
      Map.Entry<?, ?> other = (Map.Entry<?, ?>) obj;
      return ObjectUtils.equals(getKey(), other.getKey()) &&
          ObjectUtils.equals(getValue(), other.getValue());
    }
    return false;
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    return (getKey() == null ? 0 : getKey().hashCode()) ^
        (getValue() == null ? 0 : getValue().hashCode());
  }

  @Override
  public String toString() {
    return new StringBuilder()
        .append("[")
        .append(getFirst())
        .append(", ")
        .append(getSecond())
        .append("]").toString();
  }

}
