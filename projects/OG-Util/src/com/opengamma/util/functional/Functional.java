/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.functional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Set of functional-like utilities.
 */
public class Functional {

  /**
   * Returns part of the provided map which values are contained by provided set of values.
   * 
   * @param map  the map, not null
   * @param values  the set of values, not null
   * @param <K> type of map keys
   * @param <V> type of map values
   * @return submap of the original map, not null
   */
  public static <K, V> Map<K, V> submapByValueSet(Map<K, V> map, Set<V> values) {
    Map<K, V> submap = new HashMap<K, V>();
    for (K key : map.keySet()) {
      V value = map.get(key);
      if (values.contains(value)) {
        submap.put(key, value);
      }
    }
    return submap;
  }

  /**
   * Returns part of the provided map which keys are contained by provided set of keys.
   * 
   * @param map  the map, not null
   * @param keys  the set of keys, not null
   * @param <K> type of map keys
   * @param <V> type of map values
   * @return submap of the original map, not null
   */
  public static <K, V> Map<K, V> submapByKeySet(Map<K, V> map, Set<K> keys) {
    Map<K, V> submap = new HashMap<K, V>();
    for (K key : keys) {
      if (map.containsKey(key)) {
        submap.put(key, map.get(key));
      }
    }
    return submap;
  }


  /**
   * Creates reversed map of type Map<V, Collection<K>> from map of type Map<K, V>.
   * 
   * @param map  the underlying map, not null
   * @param <K> type of map keys
   * @param <V> type of map values
   * @return the reversed map, not null
   */
  public static <K, V> Map<V, Collection<K>> reverseMap(Map<K, V> map) {
    Map<V, Collection<K>> reversed = new HashMap<V, Collection<K>>();
    for (K key : map.keySet()) {
      V value = map.get(key);
      Collection<K> keys = reversed.get(value);
      if (keys == null) {
        keys = new ArrayList<K>();
        reversed.put(value, keys);
      }
      keys.add(key);
    }
    return reversed;
  }

  /**
   * Merges source map into target one by mutating it (overwriting entries if
   * the target map already contains the same keys).
   * 
   * @param target  the target map, not null
   * @param source  the source map, not null
   * @param <K> type of map keys
   * @param <V> type of map values
   * @return the merged map, not null
   */
  public static <K, V> Map<K, V> merge(Map<K, V> target, Map<K, V> source) {
    for (K key : source.keySet()) {
      target.put(key, source.get(key));
    }
    return target;
  }

  /**
   * Returns sorted list of elements from unsorted collection.
   * 
   * @param coll  unsorted collection
   * @param <T> type if elements in unsorted collection (must implement Comparable interface)
   * @return list sorted using internal entries' {@link Comparable#compareTo(Object)} compareTo} method.
   */
  public static <T extends Comparable<? super T>> List<T> sort(Collection<T> coll) {
    List<T> list = new ArrayList<T>(coll);
    Collections.sort(list);
    return list;
  }

  public static <T> T head(Iterable<? extends T> iterable) {
    final Iterator<? extends T> iter = iterable.iterator();
    if (iter.hasNext()) {
      return iter.next();
    } else {
      return null;
    }
  }

  public static <T> Iterable<? extends T> tail(Iterable<? extends T> i) {
    final Iterator<? extends T> iter = i.iterator();
    if (iter.hasNext()) {
      iter.next(); // loose the head
      return new Iterable<T>() {
        @Override
        public Iterator<T> iterator() {
          return new Iterator<T>() {
            @Override
            public boolean hasNext() {
              return iter.hasNext();
            }

            @Override
            public T next() {
              return iter.next();
            }

            @Override
            public void remove() {
              //iter.remove();
              throw new UnsupportedOperationException("don't mutate");
            }
          };
        }
      };
    } else {
      throw new UnsupportedOperationException("can't get tail of the empty Iterable");
    }
  }

  public static <T> boolean empty(Iterable<? extends T> i) {
    final Iterator<? extends T> iter = i.iterator();
    return !iter.hasNext();
  }

  public static final Iterable EMPTY_ITERABLE = new Iterable() {
    @Override
    public Iterator iterator() {
      return new Iterator() {
        @Override
        public boolean hasNext() {
          return false;
        }

        @Override
        public Object next() {
          return null;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException("don't mutate");
        }
      };
    }
  };

  public static <T, S> T reduce(T acc, Iterable<? extends S> c, Function2<T, S, T> reducer) {
    T result = acc;
    final Iterator<? extends S> iter = c.iterator();
    if (iter.hasNext()) {
      result = reducer.execute(result, iter.next());
      while (iter.hasNext()) {
        result = reducer.execute(result, iter.next());
      }
    }
    return result;
  }

  public static <T> T reduce(Iterable<? extends T> c, Function2<T, T, T> reducer) {
    T head = head(c);
    if (head != null) {
      return reduce(head, tail(c), reducer);
    } else {
      return null;
    }
  }

  public static <T> Collection<T> filter(Iterable<? extends T> c, final Function1<T, Boolean> predicate) {
    return reduce(new LinkedList<T>(), c, new Function2<LinkedList<T>, T, LinkedList<T>>() {
      @Override
      public LinkedList<T> execute(LinkedList<T> acc, T e) {
        if (predicate.execute(e)){
          acc.add(e);
        }
        return acc;
      }
    });
  }

  public static <T, S> Collection<T> map(Iterable<? extends S> c, Function1<S, T> mapper) {
    return map(new LinkedList<T>(), c, mapper);
  }

  public static <T, S> Collection<T> map(Collection<T> into, Iterable<? extends S> c, Function1<S, T> mapper) {
    for (S arg : c) {
      into.add(mapper.execute(arg));
    }
    return into;
  }
  
  public static <T, S> Collection<T> flatMap(Iterable<? extends S> c, Function1<S, Collection<T>> mapper) {
    return flatMap(new LinkedList<T>(), c, mapper);
  }
  
  public static <T, S, X extends Collection<T>> X flatMap(X into, Iterable<? extends S> c, Function1<S, Collection<T>> mapper) {
    for (S arg : c) {
      into.addAll(mapper.execute(arg));
    }
    return into;
  }

  public abstract static class Reduce<T, S> extends Function3<T, Iterable<? extends S>, Function2<T, S, T>, T> {

    public abstract T reduce(T acc, S v);

    @Override
    public T execute(T acc, Iterable<? extends S> c, Function2<T, S, T> reducer) {
      return Functional.reduce(acc, c, reducer);
    }

    public T execute(T acc, Iterable<? extends S> c) {
      return execute(acc, c, new Function2<T, S, T>() {
        @Override
        public T execute(T t, S s) {
          return reduce(t, s);
        }
      });
    }
  }

  public abstract static class ReduceSame<S> extends Function2<Iterable<? extends S>, Function2<S, S, S>, S> {

    public abstract S reduce(S acc, S v);

    @Override
    public S execute(Iterable<? extends S> c, Function2<S, S, S> reducer) {
      return Functional.reduce(c, reducer);
    }

    public S execute(Iterable<? extends S> c) {
      return execute(c, new Function2<S, S, S>() {
        @Override
        public S execute(S a, S b) {
          return reduce(a, b);
        }
      });
    }
  }

}
