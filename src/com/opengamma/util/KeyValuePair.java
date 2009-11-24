package com.opengamma.util;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
/**
 * This is supposed to hold a key/value pair; it can be used as a concrete version of {@link Map.Entry}.
 * @author jim
 * @param <K> the key
 * @param <V> the value
 */
public class KeyValuePair<K, V> implements Entry<K, V> {
  private final K _key;
  private V _value;
  
  public KeyValuePair(K key, V value) {
    _key = key;
    _value = value;
  }
  
  @Override
  public K getKey() {
    return _key;
  }

  @Override
  public V getValue() {
    return _value;
  }

  @Override
  public V setValue(V value) {
    return _value = value;
  }
  
  @Override
  public boolean equals(Object o) {
    if(this == o) {
      return true;
    }
    if(o == null) {
      return false;
    }
    if(!(o instanceof KeyValuePair<?,?>)) {
      return false;
    }
    KeyValuePair<?,?> other = (KeyValuePair<?,?>) o;
    if(!ObjectUtils.equals(getKey(), other.getKey())) {
      return false;
    }
    if(!ObjectUtils.equals(getValue(), other.getValue())) {
      return false;
    }
    return true;
  }
  
  @Override
  public int hashCode() {
    return getKey().hashCode() ^ getValue().hashCode();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("KeyValuePair[");
    sb.append(getKey());
    sb.append(", ");
    sb.append(getValue());
    sb.append("]");
    return sb.toString();
  }
}
