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
import org.joda.beans.ImmutableBean;

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
public abstract class Pair<A, B>
    implements ImmutableBean, Map.Entry<A, B>, Comparable<Pair<A, B>>, Serializable {
  // this ImmutableBean is not auto-generated

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
   * @deprecated Use {@link Pairs#of(Object, Object)} or {@link ObjectsPair#of(Object, Object)}
   */
  @Deprecated
  public static <A, B> ObjectsPair<A, B> of(A first, B second) {
    return ObjectsPair.of(first, second);
  }

  /**
   * Creates a pair of {@code Double}s.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   * @deprecated Use {@link Pairs#of(Double, double)} or {@link ObjectsPair#of(Object, Object)}
   */
  @Deprecated
  public static ObjectsPair<Double, Double> of(Double first, double second) {
    return ObjectsPair.of(first, (Double) second);
  }

  /**
   * Creates a pair of {@code Double}s.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   * @deprecated Use {@link Pairs#of(double, Double)} or {@link ObjectsPair#of(Object, Object)}
   */
  @Deprecated
  public static ObjectsPair<Double, Double> of(double first, Double second) {
    return ObjectsPair.of((Double) first, second);
  }

  /**
   * Creates a pair of {@code double}s.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   * @deprecated Use {@link Pairs#of(double, double)} or {@link DoublesPair#of(double, double)}
   */
  @Deprecated
  public static DoublesPair of(double first, double second) {
    return DoublesPair.of(first, second);
  }

  /**
   * Creates a pair of {@code int} to {@code double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   * @deprecated Use {@link Pairs#of(int, double)} or {@link IntDoublePair#of(long, double)}
   */
  @Deprecated
  public static IntDoublePair of(int first, double second) {
    return IntDoublePair.of(first, second);
  }

  /**
   * Creates a pair of {@code long} to {@code double}.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   * @deprecated Use {@link Pairs#of(long, double)} or {@link LongDoublePair#of(long, double)}
   */
  @Deprecated
  public static LongDoublePair of(long first, double second) {
    return LongDoublePair.of(first, second);
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

  /**
   * Clones this pair, returning an independent copy.
   * <p>
   * Pair subclasses must be immutable, so {@code this} is returned.
   * 
   * @return the clone, not null
   */
  @Override
  public Pair<A, B> clone() {
    return this;
  }

  //-------------------------------------------------------------------------
  /**
   * Compares the pair based on the first element followed by the second element.
   * <p>
   * A Pair <i>(x<sub>1</sub>, y<sub>1</sub>)</i> is less than another pair
   * <i>(x<sub>2</sub>, y<sub>2</sub>)</i> if one of these is true:<br />
   * <i>x<sub>1</sub> &lt; x<sub>2</sub></i><br>
   * <i>x<sub>1</sub> = x<sub>2</sub></i> and <i>y<sub>1</sub> &lt; y<sub>2</sub></i>
   * <p>
   * The element types must be {@code Comparable}.
   * 
   * @param other  the other pair, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(Pair<A, B> other) {
    return new CompareToBuilder()
        .append(getFirst(), other.getFirst())
        .append(getSecond(), other.getSecond())
        .toComparison();
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
