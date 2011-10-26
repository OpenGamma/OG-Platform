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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Set of functional-like utilities
 */
public class Functional {

  /**
   * Returns part of the provided map which values are contained by provided set of values
   * @param map the map
   * @param values the set of values
   * @param <K> type of map keys
   * @param <V> type of map values
   * @return submap of the original map
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
   * Creates reversed map of type Map<V, Collection<K>> from map of type Map<K, V>
   * @param map the underlying map
   * @param <K> type of map keys
   * @param <V> type of map values
   * @return the reversed map
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
   * Returns sorted list of elements from unsorted collection.
   * @param c unsorted collection
   * @param <T> type if elements in unsorted collection (must implement Comparable interface)
   * @return list sorted using internal entries' {@link Comparable#compareTo(Object)} compareTo} method.
   */
  public static <T extends Comparable> List<T> sort(Collection<T> c) {
    List<T> list = new ArrayList<T>(c);
    Collections.sort(list);
    return list;
  }

}
