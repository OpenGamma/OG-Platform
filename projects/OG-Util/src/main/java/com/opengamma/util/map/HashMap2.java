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

import com.opengamma.util.tuple.Pair;

/**
 * Implementation of {@link Map2} backed by a standard {@link ConcurrentHashMap}.
 *
 * @param <K1> key 1 type
 * @param <K2> key 2 type
 * @param <V> value type
 */
public class HashMap2<K1, K2, V> implements Map2<K1, K2, V> {

  private final ConcurrentMap<K1, ConcurrentMap<K2, V>> _values = new ConcurrentHashMap<K1, ConcurrentMap<K2, V>>();

  protected ConcurrentMap<K2, V> newSubMap(final K1 key1) {
    return new ConcurrentHashMap<K2, V>();
  }

  protected ConcurrentMap<K2, V> createSubMap(final K1 key1, final K2 key2, final V value) {
    final ConcurrentMap<K2, V> map = newSubMap(key1);
    map.put(key2, value);
    return map;
  }

  protected void housekeep() {
  }

  // Map2

  @Override
  public V get(final K1 key1, final K2 key2) {
    final ConcurrentMap<K2, V> values = _values.get(key1);
    if (values != null) {
      return values.get(key2);
    } else {
      return null;
    }
  }

  @Override
  public V put(final K1 key1, final K2 key2, final V value) {
    V result;
    do {
      ConcurrentMap<K2, V> values = _values.get(key1);
      if (values == null) {
        values = createSubMap(key1, key2, value);
        final ConcurrentMap<K2, V> existing = _values.putIfAbsent(key1, values);
        if (existing == null) {
          result = null;
          break;
        }
        values = existing;
      }
      synchronized (values) {
        if (!values.isEmpty()) {
          result = values.put(key2, value);
          break;
        }
      }
    } while (true);
    housekeep();
    return result;
  }

  @Override
  public boolean containsKey(final K1 key1, final K2 key2) {
    final ConcurrentMap<K2, V> values = _values.get(key1);
    if (values != null) {
      return values.containsKey(key2);
    } else {
      return false;
    }
  }

  @Override
  public V remove(final K1 key1, final K2 key2) {
    housekeep();
    do {
      final ConcurrentMap<K2, V> values = _values.get(key1);
      if (values == null) {
        return null;
      }
      synchronized (values) {
        if (!values.isEmpty()) {
          final V result = values.remove(key2);
          if (values.isEmpty()) {
            _values.remove(key1, values);
          }
          return result;
        }
      }
    } while (true);
  }

  protected void removeAllKey1NoHousekeep(final K1 key1) {
    _values.remove(key1);
  }

  @Override
  public void removeAllKey1(final K1 key1) {
    removeAllKey1NoHousekeep(key1);
    housekeep();
  }

  @Override
  public V putIfAbsent(final K1 key1, final K2 key2, final V value) {
    V result;
    do {
      ConcurrentMap<K2, V> values = _values.get(key1);
      if (values == null) {
        values = createSubMap(key1, key2, value);
        final ConcurrentMap<K2, V> existing = _values.putIfAbsent(key1, values);
        if (existing == null) {
          result = null;
          break;
        }
        values = existing;
      }
      synchronized (values) {
        if (!values.isEmpty()) {
          result = values.putIfAbsent(key2, value);
          break;
        }
      }
    } while (true);
    housekeep();
    return result;
  }

  // Map

  @SuppressWarnings("unchecked")
  @Override
  public V get(final Object key) {
    if (key instanceof Pair) {
      return get(((Pair<K1, K2>) key).getFirst(), ((Pair<K1, K2>) key).getSecond());
    } else {
      return null;
    }
  }

  @Override
  public V put(final Pair<K1, K2> key, final V value) {
    final V result = put(key.getFirst(), key.getSecond(), value);
    housekeep();
    return result;
  }

  @Override
  public int size() {
    int size = 0;
    for (final ConcurrentMap<K2, V> map : _values.values()) {
      size += map.size();
    }
    return size;
  }

  @Override
  public boolean isEmpty() {
    return _values.isEmpty();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean containsKey(final Object key) {
    if (key instanceof Pair) {
      return containsKey(((Pair<K1, K2>) key).getFirst(), ((Pair<K1, K2>) key).getSecond());
    } else {
      return false;
    }
  }

  @Override
  public boolean containsValue(final Object value) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public V remove(final Object key) {
    if (key instanceof Pair) {
      final V result = remove(((Pair<K1, K2>) key).getFirst(), ((Pair<K1, K2>) key).getSecond());
      housekeep();
      return result;
    } else {
      return null;
    }
  }

  @Override
  public void putAll(final Map<? extends Pair<K1, K2>, ? extends V> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    _values.clear();
    housekeep();
  }

  @Override
  public Set<Pair<K1, K2>> keySet() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<V> values() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Map.Entry<Pair<K1, K2>, V>> entrySet() {
    throw new UnsupportedOperationException();
  }

}
