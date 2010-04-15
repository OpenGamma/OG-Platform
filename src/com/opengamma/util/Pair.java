/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * A standard immutable pair implementation consisting of two elements.
 * <p>
 * This implementation refers to the elements as 'first' and 'second'.
 * The class also implements the {@link Map.Entry} interface where the key is 'first'
 * and the value is 'second'.
 * <p>
 * Although the implementation is immutable, there is no restriction on the objects
 * that may be stored. If mutable objects are stored in the pair, then the pair itself
 * effectively becomes mutable.
 *
 * @author kirk
 * @param <A> the first element type
 * @param <B> the second element type
 */
public final class Pair<A, B> implements Map.Entry<A, B>, Comparable<Pair<A,B>>, Serializable {

  /** The first element. */
  private final A _first;
  /** The second element. */
  private final B _second;

  /**
   * Factory method creating a pair inferring the types.
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   */
  public static <A, B> Pair<A, B> of(A first, B second) {
    return new Pair<A, B>(first, second);
  }

  /**
   * Constructs a pair.
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   */
  public Pair(A first, B second) {
    _first = first;
    _second = second;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first element from this pair.
   * When treated as a key-value pair, this is the key.
   * @return the first element, may be null
   */
  public A getFirst() {
    return _first;
  }

  /**
   * Gets the second element from this pair.
   * When treated as a key-value pair, this is the value.
   * @return the second element, may be null
   */
  public B getSecond() {
    return _second;
  }

  /**
   * Gets the key from this pair.
   * This method implements the {@link Map.Entry} interface returning the
   * first element as the key.
   * @return the first element as the key, may be null
   */
  @Override
  public A getKey() {
    return _first;
  }

  /**
   * Gets the value from this pair.
   * This method implements the {@link Map.Entry} interface returning the
   * second element as the value.
   * @return the second element as the value, may be null
   */
  @Override
  public B getValue() {
    return _second;
  }

  /**
   * Throws {@code UnsupportedOperationException} as this class is immutable.
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
   * This method supports auto-casting as they is no way in generics to provide
   * a more specific type.
   * @return the elements as a list, never null
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
   * The generic types must be {@link Comparable}.
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
    if (obj instanceof Pair<?,?>) {
      Pair<?,?> other = (Pair<?,?>) obj;
      return ObjectUtils.equals(getFirst(), other.getFirst()) &&
              ObjectUtils.equals(getSecond(), other.getSecond());
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
      .append("Pair[")
      .append(getFirst())
      .append(", ")
      .append(getSecond())
      .append("]").toString();
  }

  //-------------------------------------------------------------------------
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    message.add(0, getClass().getName());
    context.objectToFudgeMsg(message, "first", null, getFirst());
    context.objectToFudgeMsg(message, "second", null, getSecond());
    return message;
  }

  public static Pair<?,?> fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    Object first = context.fieldValueToObject(message.getByName("first"));
    Object second = context.fieldValueToObject(message.getByName("second"));
    return Pair.of(first, second);
  }

}
