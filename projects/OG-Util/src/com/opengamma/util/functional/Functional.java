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

  public static <T> T first(Collection<T> collection) {
    Iterator<T> it = collection.iterator();
    if (it.hasNext()) {
      return it.next();
    } else {
      return null;
    }
  }

  /**
   * Creates an array of stings out of supplied objects
   *
   * @param objs objects out of which string array is created
   * @return an array of stings out of supplied objects 
   */
  public static String[] newStringArray(final Object... objs) {
    String[] strings = new String[objs.length];
    for (int i = 0; i < objs.length; i++) {
      strings[i] = objs[i] != null ? objs[i].toString() : null;
    }
    return strings;
  }

  /**
   * Creates an array of type V out of provided objects (of type V as well)
   * 
   * @param <V>  the object type
   * @param array objects out of which the array is created
   * @return an array of type V out of provided objects
   */
  public static <V> V[] newArray(final V... array) {
    return array;
  }

  /**
   * Returns part of the provided map which values are contained by provided set of values
   * @param map the map
   * @param values the set of values
   * @param <K> type of map keys
   * @param <V> type of map values
   * @return submap of the original map, not null
   */
  public static <K, V> Map<K, V> submapByValueSet(final Map<K, V> map, final Set<V> values) {
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
  public static <K, V> Map<K, V> submapByKeySet(final Map<K, V> map, final Set<K> keys) {
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
  public static <K, V> Map<V, Collection<K>> reverseMap(final Map<K, V> map) {
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
  public static <K, V> Map<K, V> merge(final Map<K, V> target, final Map<K, V> source) {
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
  @SuppressWarnings({"rawtypes", "unchecked" })
  public static <T extends Comparable> List<T> sort(final Collection<T> coll) {
    List<T> list = new ArrayList<T>(coll);
    Collections.sort(list);
    return list;
  }

  public static <T, S> T reduce(final T acc, final Iterator<? extends S> iter, final Function2<T, S, T> reducer) {
    T result = acc;    
    while (iter.hasNext()) {
      result = reducer.execute(result, iter.next());
    }
    return result;
  }

  public static <T, S> T reduce(final T acc, final Iterable<? extends S> c, final Function2<T, S, T> reducer) {
    final Iterator<? extends S> iter = c.iterator();
    return reduce(acc, iter, reducer);
  }

  public static <T> T reduce(final Iterable<? extends T> c, final Function2<T, T, T> reducer) {
    final Iterator<? extends T> iter = c.iterator();
    if (iter.hasNext()) {
      T acc = iter.next();
      return reduce(acc, iter, reducer);
    } else {
      return null;
    }
  }

  public static <T> boolean any(final Iterable<? extends T> c, final Function1<T, Boolean> predicate) {
    final Iterator<? extends T> iter = c.iterator();
    boolean any = false;
    while (!any && iter.hasNext()) {
      any = predicate.execute(iter.next());
    }
    return any;
  }
  
  public static <T> boolean all(final Iterable<? extends T> c, final Function1<T, Boolean> predicate) {
    final Iterator<? extends T> iter = c.iterator();
    boolean all = true;
    while (all && iter.hasNext()) {
      all = predicate.execute(iter.next());
    }
    return all;
  }

  public static <T> Function1<T, Boolean> complement(final Function1<T, Boolean> predicate) {
    return new Function1<T, Boolean>() {
      @Override
      public Boolean execute(T t) {
        return !predicate.execute(t);
      }
    };
  }
  
  public static <T> List<T> filter(Iterable<? extends T> c, final Function1<T, Boolean> predicate) {
    return reduce(new LinkedList<T>(), c, new Function2<LinkedList<T>, T, LinkedList<T>>() {
      @Override
      public LinkedList<T> execute(LinkedList<T> acc, T e) {
        if (predicate.execute(e)) {
          acc.add(e);
        }
        return acc;
      }
    });
  }

  public static <T, S> Collection<T> map(Iterable<? extends S> c, Function1<S, T> mapper) {
    return map(new LinkedList<T>(), c, mapper);
  }

  public static <T, S, X extends Collection<T>> X map(X into, Iterable<? extends S> c, Function1<S, T> mapper) {
    for (S arg : c) {
      into.add(mapper.execute(arg));
    }
    return into;
  }

  public static <S> void dorun(Iterable<? extends S> c, Function1<S, Void> executor) {
    for (S arg : c) {
      executor.execute(arg);
    }
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

  //-------------------------------------------------------------------------
  /**
   * Class for implementing a reducer.
   * 
   * @param <T>  the first type
   * @param <S>  the second type
   */
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

  /**
   * Class for implementing a reducer on a single type.
   * 
   * @param <S>  the second type
   */
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
