/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.map;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Refinement of {@link HashMap2} that holds references.
 * 
 * @param <K1> key 1 type
 * @param <K2> key 2 type
 * @param <V> value type
 */
/* package */abstract class ReferenceHashMap2<K1, K2, V> extends HashMap2<K1, K2, V> {

  private final ReferenceQueue<V> _garbage = new ReferenceQueue<V>();

  /* package */final class ReferenceMap implements ConcurrentMap<K2, V> {

    private final K1 _key1;
    private final ConcurrentMap<K2, Reference<? extends V>> _underlying;

    private ReferenceMap(final K1 key1, final ConcurrentMap<K2, Reference<? extends V>> underlying) {
      _key1 = key1;
      _underlying = underlying;
    }

    @Override
    public int size() {
      return _underlying.size();
    }

    @Override
    public boolean isEmpty() {
      return _underlying.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(Object value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V get(Object key) {
      final Reference<? extends V> reference = _underlying.get(key);
      if (reference != null) {
        return reference.get();
      }
      return null;
    }

    @Override
    public V put(K2 key, V value) {
      final Reference<? extends V> reference = _underlying.put(key, createReference(this, key, value));
      if (reference != null) {
        return reference.get();
      }
      return null;
    }

    @Override
    public V remove(Object key) {
      final Reference<? extends V> reference = _underlying.remove(key);
      if (reference != null) {
        return reference.get();
      }
      return null;
    }

    @Override
    public void putAll(Map<? extends K2, ? extends V> m) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<K2> keySet() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Collection<V> values() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<java.util.Map.Entry<K2, V>> entrySet() {
      throw new UnsupportedOperationException();
    }

    @Override
    public V putIfAbsent(K2 key, V value) {
      final Reference<? extends V> newValue = createReference(this, key, value);
      final Reference<? extends V> reference = _underlying.putIfAbsent(key, newValue);
      if (reference != null) {
        final V result = reference.get();
        if (result == null) {
          if (_underlying.replace(key, reference, newValue)) {
            return null;
          }
        } else {
          return result;
        }
      }
      return null;
    }

    @Override
    public boolean remove(Object key, Object value) {
      return _underlying.remove(key, value);
    }

    @Override
    public boolean replace(K2 key, V oldValue, V newValue) {
      throw new UnsupportedOperationException();
    }

    @Override
    public V replace(K2 key, V value) {
      throw new UnsupportedOperationException();
    }

    /* package */void housekeep(final K2 key, final Reference<? extends V> value) {
      synchronized (this) {
        if (!remove(key, value) || !isEmpty()) {
          return;
        }
      }
      removeAllKey1(_key1);
    }

  }

  @Override
  protected ConcurrentMap<K2, V> newSubMap(final K1 key1) {
    return new ReferenceMap(key1, new ConcurrentHashMap<K2, Reference<? extends V>>());
  }

  @Override
  protected void housekeep() {
    Reference<? extends V> garbage = getGarbageQueue().poll();
    while (garbage != null) {
      housekeep(garbage);
      garbage = getGarbageQueue().poll();
    }
  }

  protected ReferenceQueue<V> getGarbageQueue() {
    return _garbage;
  }

  protected abstract Reference<? extends V> createReference(ReferenceMap map, K2 key, V value);

  protected abstract void housekeep(Reference<? extends V> ref);

}
