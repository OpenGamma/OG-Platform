/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.map;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.MapMaker;
import com.opengamma.lambdava.tuple.Pair;

/**
 * Implementation of {@link Map2} backed by a standard {@link ConcurrentHashMap}.
 * 
 * @param <K1> key 1 type
 * @param <K2> key 2 type
 * @param <V> value type
 */
public class HashMap2<K1, K2, V> implements Map2<K1, K2, V> {

  // TODO: need a better implementation of this

  private static final class Key {

    private Object _key1;
    private Object _key2;
    private Key _next;

    private Key(final Object key1, final Object key2) {
      _key1 = key1;
      _key2 = key2;
    }

    @Override
    public int hashCode() {
      return _key1.hashCode() ^ _key2.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
      final Key k = (Key) o;
      return _key1.equals(k._key1) && _key2.equals(k._key2);
    }

  }

  private final ConcurrentMap<Key, V> _data = mapMaker().makeMap();
  private final AtomicReference<Key> _keys = new AtomicReference<Key>();

  protected MapMaker mapMaker() {
    return new MapMaker();
  }

  private Key borrowKey(final K1 key1, final K2 key2) {
    Key key = _keys.get();
    if (key == null) {
      return new Key(key1, key2);
    } else {
      do {
        synchronized (key) {
          if (_keys.compareAndSet(key, key._next)) {
            key._key1 = key1;
            key._key2 = key2;
            return key;
          }
        }
        key = _keys.get();
      } while (key != null);
      return new Key(key1, key2);
    }
  }

  private void returnKey(final Key key) {
    synchronized (key) {
      key._next = _keys.get();
      while (!_keys.compareAndSet(key._next, key)) {
        key._next = _keys.get();
      }
    }
  }

  @Override
  public V get(final K1 key1, final K2 key2) {
    final Key key = borrowKey(key1, key2);
    final V result = _data.get(key);
    returnKey(key);
    return result;
  }

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
  public V put(final K1 key1, final K2 key2, final V value) {
    return _data.put(new Key(key1, key2), value);
  }

  @Override
  public V put(final Pair<K1, K2> key, final V value) {
    return put(key.getFirst(), key.getSecond(), value);
  }

  @Override
  public int size() {
    return _data.size();
  }

  @Override
  public boolean isEmpty() {
    return _data.isEmpty();
  }

  @Override
  public boolean containsKey(final K1 key1, final K2 key2) {
    final Key key = borrowKey(key1, key2);
    final boolean result = _data.containsKey(key);
    returnKey(key);
    return result;
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
    return _data.containsValue(value);
  }

  @Override
  public V remove(final K1 key1, final K2 key2) {
    final Key key = borrowKey(key1, key2);
    final V result = _data.remove(key);
    returnKey(key);
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public V remove(final Object key) {
    if (key instanceof Pair) {
      return remove(((Pair<K1, K2>) key).getFirst(), ((Pair<K1, K2>) key).getSecond());
    } else {
      return null;
    }
  }

  @Override
  public void putAll(final Map<? extends Pair<K1, K2>, ? extends V> m) {
    for (Map.Entry<? extends Pair<K1, K2>, ? extends V> entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void clear() {
    _data.clear();
  }

  @Override
  public Set<Pair<K1, K2>> keySet() {
    return new AbstractSet<Pair<K1, K2>>() {
      private final Iterator<Key> _it = _data.keySet().iterator();
      @Override
      public Iterator<Pair<K1, K2>> iterator() {
        return new AbstractIterator<Pair<K1, K2>>() {
          @SuppressWarnings("unchecked")
          @Override
          protected Pair<K1, K2> computeNext() {
            if (_it.hasNext()) {
              Key key = _it.next();
              return Pair.of((K1) key._key1, (K2) key._key2);
            }
            return endOfData();
          }
        };
      }
      @Override
      public int size() {
        return _data.size();
      }
    };
  }

  @Override
  public Collection<V> values() {
    return _data.values();
  }

  @Override
  public Set<Map.Entry<Pair<K1, K2>, V>> entrySet() {
    return new Set<Map.Entry<Pair<K1, K2>, V>>() {

      @Override
      public int size() {
        return _data.size();
      }

      @Override
      public boolean isEmpty() {
        return _data.isEmpty();
      }

      @Override
      public boolean contains(Object o) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Iterator<java.util.Map.Entry<Pair<K1, K2>, V>> iterator() {
        final Iterator<Map.Entry<Key, V>> itr = _data.entrySet().iterator();
        return new Iterator<Map.Entry<Pair<K1, K2>, V>>() {

          @Override
          public boolean hasNext() {
            return itr.hasNext();
          }

          @Override
          public java.util.Map.Entry<Pair<K1, K2>, V> next() {
            final Map.Entry<Key, V> entry = itr.next();
            return new Map.Entry<Pair<K1, K2>, V>() {

              @SuppressWarnings("unchecked")
              @Override
              public Pair<K1, K2> getKey() {
                return (Pair<K1, K2>) Pair.of(entry.getKey()._key1, entry.getKey()._key2);
              }

              @Override
              public V getValue() {
                return entry.getValue();
              }

              @Override
              public V setValue(final V value) {
                throw new UnsupportedOperationException();
              }

            };
          }

          @Override
          public void remove() {
          }

        };
      }

      @Override
      public Object[] toArray() {
        throw new UnsupportedOperationException();
      }

      @Override
      public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean add(java.util.Map.Entry<Pair<K1, K2>, V> e) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(Object o) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(Collection<? extends java.util.Map.Entry<Pair<K1, K2>, V>> c) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void clear() {
        _data.clear();
      }

    };
  }

  @Override
  public V putIfAbsent(final K1 key1, final K2 key2, final V value) {
    final Key key = borrowKey(key1, key2);
    final V result = _data.putIfAbsent(key, value);
    if (result != null) {
      returnKey(key);
    }
    return result;
  }

}
