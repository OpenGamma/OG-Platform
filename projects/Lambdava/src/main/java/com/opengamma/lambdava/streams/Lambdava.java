/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.lambdava.functions.Function0;

/**
 * Lambdava λ-flavoured java.
 *
 * @param <S> the type of the iterable
 */
public abstract class Lambdava<S> implements StreamI<S> {

  public static <S> Functional<S> functional(Iterable<S> c) {
    if (c instanceof Functional)
      return (Functional<S>) c;
    else
      return FunctionalStream.functional(c);
  }

  public static <S> Lambdava<S> functional(S[] c) {
    //TODO implement array based streams and refactor this method to use it.
    return FunctionalStream.functional(Arrays.asList(c));
  }

  abstract protected Iterable<S> underlying();

  public List<S> asList() {
    return iterable2collection(underlying());
  }

  public Set<S> asSet() {
    return new HashSet<S>(iterable2collection(underlying()));
  }

  @Override
  public StreamI<S> asStream() {
    return Stream.from(underlying());
  }

  static <T> ArrayList<T> iterable2collection(Iterable<T> iterable) {
    ArrayList<T> collection;
    if (iterable instanceof Collection) {
      collection = new ArrayList<T>((Collection<T>) iterable);
    } else {
      collection = new ArrayList<T>();
      for (T s : iterable) {
        collection.add(s);
      }
    }
    return collection;
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
  
  
//  public static <T> StreamI<T> lazySeq(final Function0<StreamI<T>> body){
//    return new AbstractStream<T>(){
//      private StreamI<T> _realized;
//      
//      synchronized private StreamI<T> realize(){
//        if(_realized == null){
//          _realized = body.execute();
//        }
//        return _realized;
//      }
//      
//      @Override
//      public int count() {
//        return realize().count();
//      }
//
//      @Override
//      public boolean isEmpty() {
//        return realize().isEmpty();
//      }
//
//      @Override
//      public T head() {
//        return realize().head();
//      }
//
//      @Override
//      public StreamI<T> rest() {
//        return realize().rest();
//      }
//
//      @Override
//      public String toString() {
//        return "lazySeq";
//      }
//    };
//  }

  public static Functional<Integer> range(int start, int untill) {
    if (start >= untill)
      return functional(Stream.<Integer>empty());
    else
      return functional(Stream.of(start).append(range(start + 1, untill)));
  }

  @Override
  public String toString() {
    return "λ( " + underlying().toString() + " )";
  }
}


