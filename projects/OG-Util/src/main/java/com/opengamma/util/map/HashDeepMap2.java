/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.map;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;

/**
 * Implementation of {@link com.opengamma.util.map.Map2} backed by a standard {@link java.util.concurrent.ConcurrentHashMap}.
 *
 * @param <K1> key 1 type
 * @param <K2> key 2 type
 * @param <V> value type
 */
public class HashDeepMap2<K1, K2, V> implements DeepMap2<K1, K2, V> {

  private final ConcurrentMap<K1, Map<K2, V>> _data = mapMaker().makeMap();


  protected MapMaker mapMaker() {
    return new MapMaker();
  }

  private final ConcurrentMap<K1, K1> _keys = new ConcurrentHashMap<K1, K1>();

  private K1 borrowKey(final K1 key) {
    K1 old = _keys.putIfAbsent(key, key);
    return (old != null ? old : key);
  }

  private void returnKey(final K1 key) {
    _keys.remove(key);
  }

  @Override
  public V get(final K1 key1, final K2 key2) {
    K1 k = borrowKey(key1);
    synchronized (k) {
      try {
        final Map<K2, V> map = _data.get(key1);
        if (map != null) {
          return map.get(key2);
        }
        return null;
      } finally {
        returnKey(k);
      }
    }
  }

  @Override
  public Map<K2, V> get(final K1 key) {
    return Collections.unmodifiableMap(_data.get(key));
  }

  @Override
  public V put(final K1 key1, final K2 key2, final V value) {
    K1 k = borrowKey(key1);
    synchronized (k) {
      try {
        Map<K2, V> map = _data.get(key1);
        if (map == null) {
          map = mapMaker().makeMap();
          _data.put(key1, map);
        }
        return map.put(key2, value);
      } finally {
        returnKey(k);
      }
    }
  }


  @Override
  public boolean containsKey(final K1 key1, final K2 key2) {
    K1 k = borrowKey(key1);
    synchronized (k) {
      try {
        Map<K2, V> map = _data.get(key1);
        if (map == null) {
          return false;
        } else {
          return map.containsKey(key2);
        }
      } finally {
        returnKey(k);
      }
    }
  }


  @Override
  public V remove(final K1 key1, final K2 key2) {
    K1 k = borrowKey(key1);
    synchronized (k) {
      try {
        Map<K2, V> map = _data.get(key1);
        if (map == null) {
          return null;
        } else {
          return map.remove(key2);
        }
      } finally {
        returnKey(k);
      }
    }
  }

  @Override
  public Map<K2, V> remove(K1 key) {
    K1 k = borrowKey(key);
    synchronized (k) {
      try {
        Map<K2, V> map = _data.remove(key);
        if (map == null) {
          return null;
        } else {
          return map;
        }
      } finally {
        returnKey(k);
      }
    }
  }
}
