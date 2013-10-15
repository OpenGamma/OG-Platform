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

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.util.serialization.InvokedSerializedForm;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.util.ClassMap;
import com.opengamma.util.fudgemsg.WriteReplaceHelper;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Checks whether values are close enough to equality to satisfy the regression test.
 */
public final class EqualityChecker {

  // TODO static method to populate this from the outside
  private static final Map<Class<?>, EqualsHandler<?>> s_handlers = new ClassMap<>();

  static {
    s_handlers.put(Double.class, new EqualityChecker.DoubleHandler());
    s_handlers.put(double[].class, new EqualityChecker.PrimitiveDoubleArrayHandler());
    s_handlers.put(Double[].class, new EqualityChecker.DoubleArrayHandler());
    s_handlers.put(Object[].class, new EqualityChecker.ObjectArrayHandler());
    s_handlers.put(List.class, new EqualityChecker.ListHandler());
    s_handlers.put(YieldCurve.class, new EqualityChecker.YieldCurveHandler());
    s_handlers.put(DoubleLabelledMatrix1D.class, new EqualityChecker.DoubleLabelledMatrix1DHandler());
    s_handlers.put(MultipleCurrencyAmount.class, new EqualityChecker.MultipleCurrencyAmountHandler());
    s_handlers.put(Bean.class, new EqualityChecker.BeanEqualsHandler());
    s_handlers.put(InvokedSerializedForm.class, new InvokedSerializedFormEqualsHandler());
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
    // for normal classes this method returns the object itself. for instances of anonymous inner classes produced
    // by a factory method it returns an instance of InvokedSerializedForm which encodes the factory method and
    // arguments needed to recreate the object. comparing anonymous classes is obviously fraught with difficulties,
    // but comparing the serialized form will work
    Object value1 = WriteReplaceHelper.writeReplace(o1);
    Object value2 = WriteReplaceHelper.writeReplace(o2);
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

  /**
   * TODO this is a rubbish name
   * @param <T>
   */
  public interface EqualsHandler<T> {

    /**
     * Returns true if the values are close enough to equality to satisfy the regression test.
     * @param value1 A value
     * @param value2 Another value
     * @param delta The maximum allowable difference between two double values
     * @return true If the values are close enough to be considered equal
     */
    boolean equals(T value1, T value2, double delta);
  }

  static final class YieldCurveHandler implements EqualsHandler<YieldCurve> {

    @Override
    public boolean equals(YieldCurve value1, YieldCurve value2, double delta) {
      return EqualityChecker.equals(value1.getCurve(), value2.getCurve(), delta);
    }
  }

  static final class DoubleArrayHandler implements EqualsHandler<Double[]> {

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

  static final class MultipleCurrencyAmountHandler implements EqualsHandler<MultipleCurrencyAmount> {

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

  static final class ObjectArrayHandler implements EqualsHandler<Object[]> {

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

  static final class PrimitiveDoubleArrayHandler implements EqualsHandler<double[]> {

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

  static final class DoubleHandler implements EqualsHandler<Double> {

    @Override
    public boolean equals(Double value1, Double value2, double delta) {
      return Math.abs(value1 - value2) <= delta;
    }
  }

  static final class ListHandler implements EqualsHandler<List<?>> {

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

  static class DoubleLabelledMatrix1DHandler implements EqualsHandler<DoubleLabelledMatrix1D> {

    @Override
    public boolean equals(DoubleLabelledMatrix1D value1, DoubleLabelledMatrix1D value2, double delta) {
      if (value1.equals(value2)) {
        return true;
      }
      if (!EqualityChecker.equals(value1.getKeys(), value2.getKeys(), delta)) {
        return false;
      }
      if (!EqualityChecker.equals(value1.getValues(), value2.getValues(), delta)) {
        return false;
      }
      if (!EqualityChecker.equals(value1.getLabels(), value2.getLabels(), delta)) {
        return false;
      }
      return true;
    }
  }

  /* package */ static class BeanEqualsHandler implements EqualsHandler<Bean> {

    @Override
    public boolean equals(Bean bean1, Bean bean2, double delta) {
      for (MetaProperty<?> property : bean1.metaBean().metaPropertyIterable()) {
        Object value1 = property.get(bean1);
        Object value2 = property.get(bean2);
        if (!EqualityChecker.equals(value1, value2, delta)) {
          return false;
        }
      }
      return true;
    }
  }

  private static class InvokedSerializedFormEqualsHandler implements EqualsHandler<InvokedSerializedForm> {

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
}
