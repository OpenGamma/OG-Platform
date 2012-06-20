/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.map;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.util.tuple.Triple;

/**
 * Crude implementation of {@link Map3}.
 * 
 * @param <K1> key 1 type
 * @param <K2> key 2 type
 * @param <K3> key 3 type
 * @param <V> value type
 */
public class HashMap3<K1, K2, K3, V> implements Map3<K1, K2, K3, V> {

  private final ConcurrentMap<Triple<K1, K2, K3>, V> _data = new ConcurrentHashMap<Triple<K1, K2, K3>, V>();

  @Override
  public int size() {
    return _data.size();
  }

  @Override
  public boolean isEmpty() {
    return _data.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return _data.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return _data.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return _data.get(key);
  }

  @Override
  public V put(Triple<K1, K2, K3> key, V value) {
    return _data.put(key, value);
  }

  @Override
  public V remove(Object key) {
    return _data.remove(key);
  }

  @Override
  public void putAll(Map<? extends Triple<K1, K2, K3>, ? extends V> m) {
    _data.putAll(m);
  }

  @Override
  public void clear() {
    _data.clear();
  }

  @Override
  public Set<Triple<K1, K2, K3>> keySet() {
    return _data.keySet();
  }

  @Override
  public Collection<V> values() {
    return _data.values();
  }

  @Override
  public Set<java.util.Map.Entry<Triple<K1, K2, K3>, V>> entrySet() {
    return _data.entrySet();
  }

  @Override
  public V get(K1 key1, K2 key2, K3 key3) {
    return _data.get(Triple.of(key1, key2, key3));
  }

  @Override
  public V put(K1 key1, K2 key2, K3 key3, V value) {
    return _data.put(Triple.of(key1, key2, key3), value);
  }

  @Override
  public V putIfAbsent(K1 key1, K2 key2, K3 key3, V value) {
    return _data.putIfAbsent(Triple.of(key1, key2, key3), value);
  }

  @Override
  public V remove(K1 key1, K2 key2, K3 key3) {
    return _data.remove(Triple.of(key1, key2, key3));
  }

  @Override
  public boolean containsKey(K1 key1, K2 key2, K3 key3) {
    return _data.containsKey(Triple.of(key1, key2, key3));
  }

}
