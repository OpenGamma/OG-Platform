/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.map;

import java.util.Map;

import com.opengamma.util.tuple.Triple;

/**
 * Variant of {@link Map} that allows three keys to be used, equivalent to a map that has a composite key of three elements.
 * 
 * @param <K1> key 1 type
 * @param <K2> key 2 type
 * @param <K3> key 3 type
 * @param <V> value type
 */
public interface Map3<K1, K2, K3, V> extends Map<Triple<K1, K2, K3>, V> {

  /**
   * Returns the element referenced by the given keys.
   * 
   * @param key1 the first key
   * @param key2 the second key
   * @param key3 the third key
   * @return the value or null if the key-triple is not in the map
   */
  V get(K1 key1, K2 key2, K3 key3);

  /**
   * Stores a new element in the map.
   * 
   * @param key1 the first key
   * @param key2 the second key
   * @param key3 the third key
   * @param value the value to store
   * @return the previously stored value
   */
  V put(K1 key1, K2 key2, K3 key3, V value);

  /**
   * Stores a new element in the map if there is not already one for that key triple.
   * 
   * @param key1 the first key
   * @param key2 the second key
   * @param key3 the third key
   * @param value the value to store
   * @return the previously stored value
   */
  V putIfAbsent(K1 key1, K2 key2, K3 key3, V value);

  /**
   * Removes an element from the map.
   * 
   * @param key1 the first key
   * @param key2 the second key
   * @param key3 the third key
   * @return the value stored, or null if the key-triple is not in the map
   */
  V remove(K1 key1, K2 key2, K3 key3);

  /**
   * Tests if a key-triple is present in the map.
   * 
   * @param key1 the first key
   * @param key2 the second key
   * @param key3 the third key
   * @return true if the key-triple is present, false otherwise
   */
  boolean containsKey(K1 key1, K2 key2, K3 key3);

}
