/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.map;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;

/**
 * Implementation of {@link Map2} backed by a standard {@link ConcurrentHashMap}.
 * 
 * @param <K1> key 1 type
 * @param <K2> key 2 type
 * @param <V> value type
 */
public class HashMap2<K1, K2, V> implements Map2<K1, K2, V> {

  /**
   * Strategy pattern for dealing with the K1 key.
   */
  public abstract static class KeyStrategy {

    protected abstract <K1, K2, V> ConcurrentMap<K1, ConcurrentMap<K2, V>> createBaseMap();

    protected abstract <K> K getKey(final Object reference);

    protected abstract <K> Object createReference(final K key);

  }

  /**
   * Use to hold key1 with strong references.
   */
  public static final KeyStrategy STRONG_KEYS = new KeyStrategy() {

    @Override
    protected <K1, K2, V> ConcurrentMap<K1, ConcurrentMap<K2, V>> createBaseMap() {
      return new ConcurrentHashMap<K1, ConcurrentMap<K2, V>>();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <K> K getKey(final Object reference) {
      return (K) reference;
    }

    @Override
    protected <K> Object createReference(final K key) {
      return key;
    }

  };

  /**
   * Use to hold key1 with weak references.
   */
  public static final KeyStrategy WEAK_KEYS = new KeyStrategy() {

    @Override
    protected <K1, K2, V> ConcurrentMap<K1, ConcurrentMap<K2, V>> createBaseMap() {
      return new MapMaker().weakKeys().makeMap();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <K> K getKey(final Object reference) {
      return ((Reference<K>) reference).get();
    }

    @Override
    protected <K> Object createReference(final K key) {
      return new WeakReference<K>(key);
    }

  };

  private final KeyStrategy _key1Strategy;
  private final ConcurrentMap<K1, ConcurrentMap<K2, V>> _values;

  public HashMap2(final KeyStrategy key1Strategy) {
    _key1Strategy = key1Strategy;
    _values = key1Strategy.createBaseMap();
  }

  protected ConcurrentMap<K2, V> newSubMap(final K1 key1) {
    return new MapMaker().makeMap();
  }

  protected Object createKey1Reference(final K1 key1) {
    return _key1Strategy.createReference(key1);
  }

  protected K1 getKey1(final Object opaqueReference) {
    return _key1Strategy.getKey(opaqueReference);
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
  public void retainAllKey1(final Collection<K1> key1) {
    _values.keySet().retainAll(key1);
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

  @Override
  public void clear() {
    _values.clear();
    housekeep();
  }

}
