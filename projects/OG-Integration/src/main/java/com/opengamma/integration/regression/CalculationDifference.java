/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public final class CalculationDifference {

  // TODO handlers for every structured data type so I can dive in and compare individual values?

  private CalculationDifference() {
  }

  // TODO different deltas for different columns?
  // TODO return type needs 3 fields
  // in 1 but not 2 - map<key, value>
  // in 2 but not 1 - map<key, value>
  // in both but different value - map<key, pair<value, value>>
  public static Result compare(CalculationResults results1, CalculationResults results2, double delta) {
    Set<CalculationResultKey> only1Keys = Sets.difference(results1.getValues().keySet(), results2.getValues().keySet());
    Set<CalculationResultKey> only2Keys = Sets.difference(results2.getValues().keySet(), results1.getValues().keySet());
    Map<CalculationResultKey, Pair<Object, Object>> diffs = Maps.newHashMap();
    Set<CalculationResultKey> bothKeys = Sets.intersection(results1.getValues().keySet(), results2.getValues().keySet());
    for (CalculationResultKey key : bothKeys) {
      Object value1 = results1.getValues().get(key);
      Object value2 = results2.getValues().get(key);
      if (!equals(value1, value2, delta)) {
        diffs.put(key, Pair.of(value1, value2));
      }
    }
    Map<CalculationResultKey, Object> only1 = getValues(only1Keys, results1.getValues());
    Map<CalculationResultKey, Object> only2 = getValues(only2Keys, results2.getValues());
    return new Result(only1, only2, diffs);
  }

  // TODO type-specific comparison for complex types
  private static boolean equals(Object value1, Object value2, double delta) {
    if ((value1 instanceof Double) && (value2 instanceof Double)) {
      return Math.abs(((Double) value1) - ((Double) value2)) < delta;
    } else {
      return Objects.equals(value1, value2);
    }
  }

  private static <K, V> Map<K, V> getValues(Set<K> keys, Map<K, V> map) {
    Map<K, V> retMap = Maps.newHashMap();
    for (K key : keys) {
      if (map.containsKey(key)) {
        retMap.put(key, map.get(key));
      }
    }
    return retMap;
  }

  public static class Result {

    private final Map<CalculationResultKey, Object> _only1;
    private final Map<CalculationResultKey, Object> _only2;
    private final Map<CalculationResultKey, Pair<Object, Object>> _different;

    private Result(Map<CalculationResultKey, Object> only1,
                   Map<CalculationResultKey, Object> only2,
                   Map<CalculationResultKey, Pair<Object, Object>> different) {
      _only1 = only1;
      _only2 = only2;
      _different = different;
    }

    public Map<CalculationResultKey, Object> getOnly1() {
      return _only1;
    }

    public Map<CalculationResultKey, Object> getOnly2() {
      return _only2;
    }

    public Map<CalculationResultKey, Pair<Object, Object>> getDifferent() {
      return _different;
    }
  }
}


