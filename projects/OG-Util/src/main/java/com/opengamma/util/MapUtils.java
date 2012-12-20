/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.Map;

/**
 * Utility methods for working with maps.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class MapUtils {

  /**
   * Restricted constructor.
   */
  private MapUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Puts the value into the map if the key is not present.
   * <p>
   * This is most useful in building up a map of maps, or similar structure.
   * 
   * @param <K> the map key type
   * @param <V> the map value type
   * @param map  the map  to populate, not null
   * @param key  the key
   * @param value  the value
   * @return the contents of the map for the key
   */
  public static <K, V> V putIfAbsentGet(Map<K, V> map, K key, V value) {
    V existing = map.get(key);
    if (existing != null) {
      return existing;
    }
    map.put(key, value);
    return value;
  }

}
