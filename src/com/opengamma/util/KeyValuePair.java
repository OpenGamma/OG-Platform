/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;

/**
 * A concrete implementation of {@link Map.Entry} that acts as a key-value pair.
 * <p>
 * This implementation is mutable and supports null values.
 * 
 * @author jim
 * @param <K> the key
 * @param <V> the value
 */
public final class KeyValuePair<K, V> implements Entry<K, V> {

  /** The key. */
  private final K _key;
  /** The value. */
  private V _value;

  /**
   * Factory method creating a key-value pair inferring the types.
   * @param key  the key, may be null
   * @param value  the value, may be null
   */
  public static <K, V> KeyValuePair<K, V> of(K key, V value) {
    return new KeyValuePair<K, V>(key, value);
  }

  /**
   * Constructs a key-value pair.
   * @param key  the key, may be null
   * @param value  the value, may be null
   */
  public KeyValuePair(K key, V value) {
    _key = key;
    _value = value;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the key from this pair.
   * @return the key, may be null
   */
  @Override
  public K getKey() {
    return _key;
  }

  /**
   * Gets the value from this pair.
   * @return the value, may be null
   */
  @Override
  public V getValue() {
    return _value;
  }

  /**
   * Sets the value for this pair.
   * <p>
   * This class implements {@link Map.Entry} but it is not linked to a map.
   * Any change will only update this instance.
   * @param value  the value to update to, may be null
   * @return the old value, may be null
   */
  @Override
  public V setValue(V value) {
    V old = _value;
    _value = value;
    return old;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    // see Map.Entry API specification
    if (this == obj) {
      return true;
    }
    if (obj instanceof KeyValuePair<?,?>) {
      KeyValuePair<?,?> other = (KeyValuePair<?,?>) obj;
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
      .append("KeyValuePair[")
      .append(getKey())
      .append(", ")
      .append(getValue())
      .append("]").toString();
  }

}
