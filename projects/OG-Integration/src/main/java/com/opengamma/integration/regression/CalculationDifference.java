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
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.util.ClassMap;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 * TODO immutable bean?
 */
public final class CalculationDifference {

  // TODO static method to populate this from the outside
  private static final Map<Class<?>, EqualsHandler<?>> s_handlers = new ClassMap<>();

  static {
    s_handlers.put(Double.class, new DoubleHandler());
    s_handlers.put(double[].class, new PrimitiveDoubleArrayHandler());
    s_handlers.put(Double[].class, new DoubleArrayHandler());
    s_handlers.put(Object[].class, new ObjectArrayHandler());
    s_handlers.put(List.class, new ListHandler());
    s_handlers.put(YieldCurve.class, new YieldCurveHandler());
    s_handlers.put(DoubleLabelledMatrix1D.class, new DoubleLabelledMatrix1DHandler());
    s_handlers.put(MultipleCurrencyAmount.class, new MultipleCurrencyAmountHandler());
    // TODO generic joda bean handler
  }

  private final int _equalResultCount;
  private final String _viewDefinitionName;
  private final String _snapshotName;
  private final Map<CalculationResultKey, CalculatedValue> _onlyBase;
  private final Map<CalculationResultKey, CalculatedValue> _onlyTest;
  private final Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> _different;
  private final Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> _differentProperties;

  private CalculationDifference(int equalResultCount,
                                String viewDefinitionName,
                                String snapshotName,
                                Map<CalculationResultKey, CalculatedValue> onlyBase,
                                Map<CalculationResultKey, CalculatedValue> onlyTest,
                                Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> different,
                                Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> differentProperties) {
    _equalResultCount = equalResultCount;
    _viewDefinitionName = viewDefinitionName;
    _snapshotName = snapshotName;
    _onlyBase = onlyBase;
    _onlyTest = onlyTest;
    _different = different;
    _differentProperties = differentProperties;
  }

  public int getEqualResultCount() {
    return _equalResultCount;
  }

  public Map<CalculationResultKey, CalculatedValue> getOnlyBase() {
    return _onlyBase;
  }

  public Map<CalculationResultKey, CalculatedValue> getOnlyTest() {
    return _onlyTest;
  }

  public Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> getDifferent() {
    return _different;
  }

  public Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> getDifferentProperties() {
    return _differentProperties;
  }

  public String getViewDefinitionName() {
    return _viewDefinitionName;
  }

  public String getSnapshotName() {
    return _snapshotName;
  }

  // TODO different deltas for different columns?
  public static CalculationDifference between(CalculationResults results1, CalculationResults results2, double delta) {
    Set<CalculationResultKey> only1Keys = Sets.difference(results1.getValues().keySet(), results2.getValues().keySet());
    Set<CalculationResultKey> only2Keys = Sets.difference(results2.getValues().keySet(), results1.getValues().keySet());
    Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> diffs = Maps.newHashMap();
    Map<CalculationResultKey, Pair<CalculatedValue, CalculatedValue>> differentProps = Maps.newHashMap();
    Set<CalculationResultKey> bothKeys = Sets.intersection(results1.getValues().keySet(), results2.getValues().keySet());
    int equalResultCount = 0;
    for (CalculationResultKey key : bothKeys) {
      CalculatedValue value1 = results1.getValues().get(key);
      CalculatedValue value2 = results2.getValues().get(key);
      if (!equals(value1.getValue(), value2.getValue(), delta)) {
        diffs.put(key, Pair.of(value1, value2));
      } else {
        if (!value1.getSpecificationProperties().equals(value2.getSpecificationProperties())) {
          differentProps.put(key, Pair.of(value1, value2));
        } else {
          equalResultCount++;
        }
      }
    }
    Map<CalculationResultKey, CalculatedValue> only1 = getValues(only1Keys, results1.getValues());
    Map<CalculationResultKey, CalculatedValue> only2 = getValues(only2Keys, results2.getValues());
    String viewDefName = results1.getViewDefinitionName();
    String snapshotName = results1.getSnapshotName();
    return new CalculationDifference(equalResultCount, viewDefName, snapshotName, only1, only2, diffs, differentProps);
  }

  private static boolean equals(Object value1, Object value2, double delta) {
    if (value1 == null && value2 == null) {
      return true;
    }
    if (value1 == null || value2 == null) {
      return false;
    }
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

  private static Map<CalculationResultKey, CalculatedValue> getValues(Set<CalculationResultKey> keys,
                                                                      Map<CalculationResultKey, CalculatedValue> map) {
    Map<CalculationResultKey, CalculatedValue> retMap = Maps.newTreeMap();
    for (CalculationResultKey key : keys) {
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

  private static final class YieldCurveHandler implements EqualsHandler<YieldCurve> {

    @Override
    public boolean equals(YieldCurve value1, YieldCurve value2, double delta) {
      return CalculationDifference.equals(value1.getCurve(), value2.getCurve(), delta);
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

  private static final class MultipleCurrencyAmountHandler implements EqualsHandler<MultipleCurrencyAmount> {

    @Override
    public boolean equals(MultipleCurrencyAmount value1, MultipleCurrencyAmount value2, double delta) {
      for (CurrencyAmount currencyAmount : value1) {
        double amount1 = currencyAmount.getAmount();
        double amount2;
        try {
          amount2 = value2.getAmount(currencyAmount.getCurrency());
        } catch (IllegalArgumentException e) {
          return false;
        }
        if (!CalculationDifference.equals(amount1, amount2, delta)) {
          return false;
        }
      }
      return true;
    }
  }

  private static final class ObjectArrayHandler implements EqualsHandler<Object[]> {

    @Override
    public boolean equals(Object[] value1, Object[] value2, double delta) {
      if (value1.length != value2.length) {
        return false;
      }
      for (int i = 0; i < value1.length; i++) {
        Object item1 = value1[i];
        Object item2 = value2[i];
        if (!CalculationDifference.equals(item1, item2, delta)) {
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

  private static class DoubleLabelledMatrix1DHandler implements EqualsHandler<DoubleLabelledMatrix1D> {

    @Override
    public boolean equals(DoubleLabelledMatrix1D value1, DoubleLabelledMatrix1D value2, double delta) {
      if (value1.equals(value2)) {
        return true;
      }
      if (!CalculationDifference.equals(value1.getKeys(), value2.getKeys(), delta)) {
        return false;
      }
      if (!CalculationDifference.equals(value1.getValues(), value2.getValues(), delta)) {
        return false;
      }
      if (!CalculationDifference.equals(value1.getLabels(), value2.getLabels(), delta)) {
        return false;
      }
      return true;
    }
  }
}


