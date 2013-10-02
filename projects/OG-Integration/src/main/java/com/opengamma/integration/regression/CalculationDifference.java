/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.util.ClassMap;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public final class CalculationDifference {

  // TODO handlers for every structured data type so I can dive in and compare individual values?

  private CalculationDifference() {
  }

  // TODO static method to populate this from the outside
  private static final Map<Class<?>, EqualsHandler<?>> s_handlers = new ClassMap<>();

  static {
    s_handlers.put(Double.class, new DoubleHandler());
    s_handlers.put(double[].class, new PrimitiveDoubleArrayHandler());
    s_handlers.put(Double[].class, new DoubleArrayHandler());
    s_handlers.put(List.class, new ListHandler());
    s_handlers.put(YieldCurve.class, new YieldCurveHandler());
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

  private static boolean equals(Object value1, Object value2, double delta) {
    if (value1 == null && value2 == null) {
      return true;
    }
    if (value1 == null || value2 == null) {
      return false;
    }
    // TODO deal with subtyping?
    if (!value1.getClass().equals(value2.getClass())) {
      return false;
    }
    @SuppressWarnings("unchecked")
    EqualsHandler<Object> equalsHandler = (EqualsHandler<Object>) s_handlers.get(value1.getClass());
    if (equalsHandler != null) {
      return equalsHandler.equals(value1, value2, delta);
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

  /**
   *
   * @param <T>
   */
  public interface EqualsHandler<T> {

    boolean equals(T value1, T value2, double delta);
  }

  // TODO this is almost certainly inadequate, need handler for subtypes
  private static final class YieldCurveHandler implements EqualsHandler<YieldCurve> {

    @Override
    public boolean equals(YieldCurve value1, YieldCurve value2, double delta) {
      if (!CalculationDifference.equals(value1.getCurve().getXData(), value2.getCurve().getXData(), delta)) {
        return false;
      }
      return CalculationDifference.equals(value1.getCurve().getYData(), value2.getCurve().getYData(), delta);
    }
  }

  private static final class DoubleArrayHandler implements EqualsHandler<Double[]> {

    @Override
    public boolean equals(Double[] value1, Double[] value2, double delta) {
      if (value1.length != value2.length) {
        return false;
      }
      for (int i = 0; i < value1.length; i++) {
        double item1 = value1[i];
        double item2 = value2[i];
        if (Math.abs(item1 - item2) > delta) {
          return false;
        }
      }
      return true;
    }
  }

  private static final class PrimitiveDoubleArrayHandler implements EqualsHandler<double[]> {

    @Override
    public boolean equals(double[] value1, double[] value2, double delta) {
      if (value1.length != value2.length) {
        return false;
      }
      for (int i = 0; i < value1.length; i++) {
        double item1 = value1[i];
        double item2 = value2[i];
        if (Math.abs(item1 - item2) > delta) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   *
   */
  private static final class DoubleHandler implements EqualsHandler<Double> {

    @Override
    public boolean equals(Double value1, Double value2, double delta) {
      return Math.abs(value1 - value2) <= delta;
    }
  }

  private static final class ListHandler implements EqualsHandler<List<?>> {

    @Override
    public boolean equals(List<?> value1, List<?> value2, double delta) {
      if (value1.size() != value2.size()) {
        return false;
      }
      for (Iterator<?> it1 = value1.iterator(), it2 = value2.iterator(); it1.hasNext(); ) {
        Object item1 = it1.next();
        Object item2 = it2.next();
        if (!CalculationDifference.equals(item1, item2, delta)) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   *
   */
  public static final class Result {

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


