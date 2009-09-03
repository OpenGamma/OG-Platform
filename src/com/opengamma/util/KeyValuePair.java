package com.opengamma.util;

import java.util.Map.Entry;
/**
 * @author jim
 * This is supposed to hold a key/value pair - it can be used as a concrete version of Map.Entry<K, V>
 * @param <K> the key
 * @param <V> the value
 */
public class KeyValuePair<K, V> implements Entry<K, V> {
  private K _key;
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
  
  public boolean equals(Object o) {
    if (o instanceof KeyValuePair) {
      KeyValuePair<?, ?> kvp = (KeyValuePair<?, ?>)o;
      return CompareUtils.equalsWithNull(getKey(), kvp.getKey()) &&
             CompareUtils.equalsWithNull(getValue(), kvp.getValue());
    } else {
      return false;
    }
  }
  
  public int hashCode() {
    return getKey().hashCode() ^ getValue().hashCode();
  }
}
