/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * The standard {@code Pair} class that every single Java project in the world
 * ends up reinventing.
 *
 * @author kirk
 */
public class Pair<K,V> implements Map.Entry<K, V>, Serializable, Comparable<Pair<K,V>> {
  private final K _first;
  private final V _second;

  public Pair(K first, V second) {
    _first = first;
    _second = second;
  }

  /**
   * @return the first
   */
  public K getFirst() {
    return _first;
  }

  /**
   * @return the second
   */
  public V getSecond() {
    return _second;
  }

  @Override
  public K getKey() {
    return _first;
  }

  @Override
  public V getValue() {
    return _second;
  }

  @Override
  public V setValue(V value) {
    throw new UnsupportedOperationException("Pair<> is immutable.");
  }

  @Override
  public int compareTo(Pair<K, V> o) {
    return new CompareToBuilder().append(_first, o._first).append(_second, o._second).toComparison();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if(!(obj instanceof Pair)) {
      return false;
    }
    Pair other = (Pair) obj;
    if(!ObjectUtils.equals(_first, other._first)) {
      return false;
    }
    if(!ObjectUtils.equals(_second, other._second)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_first == null) ? 0 : _first.hashCode());
    result = prime * result + ((_second == null) ? 0 : _second.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("first", _first).append("second", _second).toString();
  }
  
  public FudgeFieldContainer toFudgeMsg (final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage ();
    message.add (0, getClass ().getName ());
    context.objectToFudgeMsg (message, "first", null, getFirst ());
    context.objectToFudgeMsg (message, "second", null, getSecond ());
    return message;
  }
  
  public static Pair<?,?> fromFudgeMsg (final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    return new Pair<Object,Object> (context.fieldValueToObject (message.getByName ("first")), context.fieldValueToObject (message.getByName ("second")));
  }

}
