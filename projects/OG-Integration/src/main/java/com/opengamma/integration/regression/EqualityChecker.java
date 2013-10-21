/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.Maps;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.util.serialization.InvokedSerializedForm;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.util.ClassMap;
import com.opengamma.util.fudgemsg.WriteReplaceHelper;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Checks whether values are close enough to equality to satisfy the regression test.
 */
public final class EqualityChecker {

  // TODO static method to populate these from the outside
  private static final Map<Class<?>, TypeHandler<?>> s_handlers = new ClassMap<>();
  private static final Map<MetaProperty<?>, Comparator<?>> s_propertyComparators = Maps.newHashMap();
  private static final ObjectArrayHandler s_objectArrayHandler = new ObjectArrayHandler();

  static {
    s_handlers.put(Double.class, new DoubleHandler());
    s_handlers.put(double[].class, new PrimitiveDoubleArrayHandler());
    s_handlers.put(Double[].class, new DoubleArrayHandler());
    s_handlers.put(Object[].class, s_objectArrayHandler);
    s_handlers.put(List.class, new ListHandler());
    s_handlers.put(YieldCurve.class, new YieldCurveHandler());
    s_handlers.put(LabelledMatrix1D.class, new LabelledMatrix1DHandler());
    s_handlers.put(MultipleCurrencyAmount.class, new MultipleCurrencyAmountHandler());
    s_handlers.put(Bean.class, new BeanHandler());
    s_handlers.put(InvokedSerializedForm.class, new InvokedSerializedFormHandler());
    s_handlers.put(VolatilitySurfaceData.class, new VolatilitySurfaceDataHandler());
    s_handlers.put(Map.class, new MapHandler());

    s_propertyComparators.put(Curve.meta().name(), new AlwaysEqualComparator());
  }

  private EqualityChecker() {
  }

  /**
   * Checks whether two values are close enough to equality to satisfy the regression test.
   * @param o1 A value
   * @param o2 Another value
   * @param delta The maximum allowable difference between two double values
   * @return true If the values are close enough to be considered equal
   */
  public static boolean equals(Object o1, Object o2, double delta) {
    if (o1 == null && o2 == null) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    if (!o1.getClass().equals(o2.getClass())) {
      return false;
    }
    // for normal classes this method returns the object itself. for instances of anonymous inner classes produced
    // by a factory method it returns an instance of InvokedSerializedForm which encodes the factory method and
    // arguments needed to recreate the object. comparing anonymous classes is obviously fraught with difficulties,
    // but comparing the serialized form will work
    Object value1 = WriteReplaceHelper.writeReplace(o1);
    Object value2 = WriteReplaceHelper.writeReplace(o2);
    @SuppressWarnings("unchecked")
    TypeHandler<Object> handler = (TypeHandler<Object>) s_handlers.get(value1.getClass());
    if (handler != null) {
      return handler.equals(value1, value2, delta);
    } else {
      // ClassMap doesn't handle subtyping and arrays, this uses the Object[] handler for non-primitive arrays
      if (value1.getClass().isArray() && Object[].class.isAssignableFrom(value1.getClass())) {
        return s_objectArrayHandler.equals((Object[]) value1, (Object[]) value2, delta);
      } else {
        return Objects.equals(value1, value2);
      }
    }
  }

  /**
   * Handles equality checking for a specific type.
   * @param <T> The type
   */
  public interface TypeHandler<T> {

    /**
     * Returns true if the values are close enough to equality to satisfy the regression test.
     * @param value1 A value
     * @param value2 Another value
     * @param delta The maximum allowable difference between two double values
     * @return true If the values are close enough to be considered equal
     */
    boolean equals(T value1, T value2, double delta);
  }

  private static final class YieldCurveHandler implements TypeHandler<YieldCurve> {

    @Override
    public boolean equals(YieldCurve value1, YieldCurve value2, double delta) {
      return EqualityChecker.equals(value1.getCurve(), value2.getCurve(), delta);
    }
  }

  private static final class DoubleArrayHandler implements TypeHandler<Double[]> {

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

  private static final class MultipleCurrencyAmountHandler implements TypeHandler<MultipleCurrencyAmount> {

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
        if (!EqualityChecker.equals(amount1, amount2, delta)) {
          return false;
        }
      }
      return true;
    }
  }

  private static final class ObjectArrayHandler implements TypeHandler<Object[]> {

    @Override
    public boolean equals(Object[] value1, Object[] value2, double delta) {
      if (value1.length != value2.length) {
        return false;
      }
      for (int i = 0; i < value1.length; i++) {
        Object item1 = value1[i];
        Object item2 = value2[i];
        if (!EqualityChecker.equals(item1, item2, delta)) {
          return false;
        }
      }
      return true;
    }
  }

  private static final class PrimitiveDoubleArrayHandler implements TypeHandler<double[]> {

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

  private static final class DoubleHandler implements TypeHandler<Double> {

    @Override
    public boolean equals(Double value1, Double value2, double delta) {
      return Math.abs(value1 - value2) <= delta;
    }
  }

  private static final class ListHandler implements TypeHandler<List<?>> {

    @Override
    public boolean equals(List<?> value1, List<?> value2, double delta) {
      if (value1.size() != value2.size()) {
        return false;
      }
      for (Iterator<?> it1 = value1.iterator(), it2 = value2.iterator(); it1.hasNext(); ) {
        Object item1 = it1.next();
        Object item2 = it2.next();
        if (!EqualityChecker.equals(item1, item2, delta)) {
          return false;
        }
      }
      return true;
    }
  }

  private static class BeanHandler implements TypeHandler<Bean> {

    @Override
    public boolean equals(Bean bean1, Bean bean2, double delta) {
      for (MetaProperty<?> property : bean1.metaBean().metaPropertyIterable()) {
        Object value1 = property.get(bean1);
        Object value2 = property.get(bean2);
        @SuppressWarnings("unchecked")
        Comparator<Object> comparator = (Comparator<Object>) s_propertyComparators.get(property);
        if (comparator == null) {
          if (!EqualityChecker.equals(value1, value2, delta)) {
            return false;
          }
        } else {
          if (value1 == null && value2 == null) {
            continue;
          }
          if (value1 == null || value2 == null) {
            return false;
          }
          if (!value1.getClass().equals(value2.getClass())) {
            return false;
          }
          if (comparator.compare(value1, value2) != 0) {
            return false;
          }
        }
      }
      return true;
    }
  }

  private static class InvokedSerializedFormHandler implements TypeHandler<InvokedSerializedForm> {

    @Override
    public boolean equals(InvokedSerializedForm value1, InvokedSerializedForm value2, double delta) {
      if (!Objects.equals(value1.getOuterClass(), value2.getOuterClass())) {
        return false;
      }
      if (!value1.getMethod().equals(value2.getMethod())) {
        return false;
      }
      if (!EqualityChecker.equals(value1.getOuterInstance(), value2.getOuterInstance(), delta)) {
        return false;
      }
      if (!EqualityChecker.equals(value1.getParameters(), value2.getParameters(), delta)) {
        return false;
      }
      return true;
    }
  }

  private static class VolatilitySurfaceDataHandler implements TypeHandler<VolatilitySurfaceData<?, ?>> {

    @Override
    public boolean equals(VolatilitySurfaceData<?, ?> value1, VolatilitySurfaceData<?, ?> value2, double delta) {
      if (!Objects.equals(value1.getDefinitionName(), value2.getDefinitionName())) {
        return false;
      }
      if (!Objects.equals(value1.getSpecificationName(), value2.getSpecificationName())) {
        return false;
      }
      if (!Objects.equals(value1.getTarget(), value2.getTarget())) {
        return false;
      }
      if (!Objects.equals(value1.getXLabel(), value2.getXLabel())) {
        return false;
      }
      if (!Objects.equals(value1.getYLabel(), value2.getYLabel())) {
        return false;
      }
      if (!EqualityChecker.equals(value1.asMap(), value2.asMap(), delta)) {
        return false;
      }
      return true;
    }
  }

  private static class MapHandler implements TypeHandler<Map<?, ?>> {

    @Override
    public boolean equals(Map<?, ?> map1, Map<?, ?> map2, double delta) {
      if (!map1.keySet().equals(map2.keySet())) {
        return false;
      }
      for (Map.Entry<?, ?> entry : map1.entrySet()) {
        Object value1 = entry.getValue();
        Object value2 = map2.get(entry.getKey());
        if (!EqualityChecker.equals(value1, value2, delta)) {
          return false;
        }
      }
      return true;
    }
  }

  private static class LabelledMatrix1DHandler implements TypeHandler<LabelledMatrix1D<?, ?>> {

    @Override
    public boolean equals(LabelledMatrix1D<?, ?> value1, LabelledMatrix1D<?, ?> value2, double delta) {
      if (!Objects.equals(value1.getLabelsTitle(), value2.getLabelsTitle())) {
        return false;
      }
      if (!Objects.equals(value1.getValuesTitle(), value2.getValuesTitle())) {
        return false;
      }
      if (!EqualityChecker.equals(value1.getKeys(), value2.getKeys(), delta)) {
        return false;
      }
      if (!EqualityChecker.equals(value1.getLabels(), value2.getLabels(), delta)) {
        return false;
      }
      if (!EqualityChecker.equals(value1.getValues(), value2.getValues(), delta)) {
        return false;
      }
      if (!EqualityChecker.equals(value1.getDefaultTolerance(), value2.getDefaultTolerance(), delta)) {
        return false;
      }
      return true;
    }
  }

  private static class AlwaysEqualComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
      return 0;
    }
  }
}
