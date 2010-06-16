/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DModel;
import com.opengamma.math.interpolation.Interpolator1DModelFactory;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.StepInterpolator1D;

/**
 * 
 */
public class InterpolatedYieldAndDiscountCurveTest {
  private static final Interpolator1D<Interpolator1DModel> LINEAR = new LinearInterpolator1D();
  private static final Interpolator1D<Interpolator1DModel> STEP = new StepInterpolator1D();
  private static final Map<Double, Double> RATE_DATA = new HashMap<Double, Double>();
  private static final Map<Double, Double> DF_DATA = new HashMap<Double, Double>();
  private static final double SHIFT = 0.001;
  private static final double T = 1.5;
  private static final InterpolatedYieldAndDiscountCurve DISCOUNT_CURVE;
  private static final InterpolatedYieldAndDiscountCurve YIELD_CURVE;
  private static final Interpolator1DModel DISCOUNT_MODEL;
  private static final Interpolator1DModel YIELD_MODEL;
  private static final Double EPS = 1e-15;

  static {
    DF_DATA.put(1., Math.exp(-0.03));
    RATE_DATA.put(1., 0.03);
    DF_DATA.put(2., Math.exp(-0.08));
    RATE_DATA.put(2., 0.04);
    DF_DATA.put(3., Math.exp(-0.15));
    RATE_DATA.put(3., 0.05);
    DISCOUNT_CURVE = new InterpolatedDiscountCurve(DF_DATA, LINEAR);
    YIELD_CURVE = new InterpolatedYieldCurve(RATE_DATA, LINEAR);
    DISCOUNT_MODEL = Interpolator1DModelFactory.fromMap(DF_DATA);
    YIELD_MODEL = Interpolator1DModelFactory.fromMap(RATE_DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullMap() {
    new InterpolatedDiscountCurve(null, LINEAR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithInsufficientData() {
    new InterpolatedDiscountCurve(Collections.<Double, Double>singletonMap(3., 2.), LINEAR);
  }

  @Test(expected = AssertionError.class)
  public void testConstructorWithNullInterpolator() {
    final Map<Double, Double> map = new HashMap<Double, Double>();
    map.put(1., 1.);
    map.put(2., 2.);
    new InterpolatedDiscountCurve(map, (Interpolator1D<Interpolator1DModel>) null);
  }

  @Test(expected = AssertionError.class)
  public void testConstructorWithNullTime() {
    final Map<Double, Double> map = new HashMap<Double, Double>();
    map.put(null, 1.);
    map.put(2., 2.);
    new InterpolatedDiscountCurve(map, LINEAR);
  }

  @Test(expected = AssertionError.class)
  public void testConstructorWithNullData() {
    final Map<Double, Double> map = new HashMap<Double, Double>();
    map.put(1., null);
    map.put(2., 2.);
    new InterpolatedDiscountCurve(map, LINEAR);
  }

  @Test(expected = AssertionError.class)
  public void testConstructorWithNegativeTime1() {
    final Map<Double, Double> map = new HashMap<Double, Double>();
    map.put(-1., 1.);
    map.put(2., 2.);
    new InterpolatedDiscountCurve(map, LINEAR);
  }

  @Test(expected = AssertionError.class)
  public void testConstructorWithNegativeTime2() {
    final Map<Double, Interpolator1D<? extends Interpolator1DModel>> map = new HashMap<Double, Interpolator1D<? extends Interpolator1DModel>>();
    map.put(-1., LINEAR);
    map.put(2., LINEAR);
    new InterpolatedDiscountCurve(DF_DATA, map);
  }

  @Test(expected = AssertionError.class)
  public void testConstructorWithNullInterpolatorInMap() {
    final Map<Double, Interpolator1D<? extends Interpolator1DModel>> map = new HashMap<Double, Interpolator1D<? extends Interpolator1DModel>>();
    map.put(1., LINEAR);
    map.put(2., null);
    new InterpolatedDiscountCurve(DF_DATA, map);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullInterpolatorMap() {
    new InterpolatedDiscountCurve(DF_DATA, (Map<Double, Interpolator1D<? extends Interpolator1DModel>>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithEmptyInterpolatorMap() {
    new InterpolatedDiscountCurve(DF_DATA, Collections.<Double, Interpolator1D<? extends Interpolator1DModel>>emptyMap());
  }

  @Test(expected = AssertionError.class)
  public void testConstructorWithNullInInterpolatorMap() {
    final Map<Double, Interpolator1D<? extends Interpolator1DModel>> map = new HashMap<Double, Interpolator1D<? extends Interpolator1DModel>>();
    map.put(3., LINEAR);
    map.put(6., null);
    new InterpolatedDiscountCurve(DF_DATA, map);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetInterestRateWithNull() {
    DISCOUNT_CURVE.getInterestRate(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetDiscountFactorWithNull() {
    DISCOUNT_CURVE.getDiscountFactor(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetInterestRateWithNegativeTime() {
    DISCOUNT_CURVE.getInterestRate(-2.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetDiscountFactorWithNegativeTime() {
    DISCOUNT_CURVE.getDiscountFactor(-2.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetParallelShiftWithNull() {
    DISCOUNT_CURVE.withParallelShift(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetSingleShiftWithNullTime() {
    DISCOUNT_CURVE.withSingleShift(null, SHIFT);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetSingleShiftWithNegativeTime() {
    DISCOUNT_CURVE.withSingleShift(-T, SHIFT);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetSingleShiftWithNullShift() {
    DISCOUNT_CURVE.withSingleShift(T, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetMultipleShiftWithNull() {
    DISCOUNT_CURVE.withMultipleShifts(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetMultipleShiftWithNegativeTime() {
    DISCOUNT_CURVE.withMultipleShifts(Collections.<Double, Double>singletonMap(-T, SHIFT));
  }

  @Test
  public void testGetters() {
    assertEquals(DISCOUNT_CURVE.getMaturities(), DF_DATA.keySet());
    assertEquals(YIELD_CURVE.getMaturities(), RATE_DATA.keySet());
    assertEquals(DISCOUNT_CURVE.getInterpolators(), Collections.singletonMap(Double.POSITIVE_INFINITY, LINEAR));
    assertEquals(YIELD_CURVE.getInterpolators(), Collections.singletonMap(Double.POSITIVE_INFINITY, LINEAR));
    assertEquals(DISCOUNT_CURVE.getModels(), Collections.singletonMap(Double.POSITIVE_INFINITY, DISCOUNT_MODEL));
    assertEquals(YIELD_CURVE.getModels(), Collections.singletonMap(Double.POSITIVE_INFINITY, YIELD_MODEL));
  }

  @Test
  public void test() {
    double df = DISCOUNT_CURVE.getDiscountFactor(T);
    assertEquals(LINEAR.interpolate(DISCOUNT_MODEL, T), df, EPS);
    assertEquals(-Math.log(LINEAR.interpolate(DISCOUNT_MODEL, T)) / T, DISCOUNT_CURVE.getInterestRate(T), EPS);
    df = YIELD_CURVE.getDiscountFactor(T);
    assertEquals(Math.exp(-LINEAR.interpolate(YIELD_MODEL, T) * T), df, EPS);
    assertEquals(LINEAR.interpolate(YIELD_MODEL, T), YIELD_CURVE.getInterestRate(T), EPS);
  }

  @Test
  public void testParallelShift() {
    final YieldAndDiscountCurve discount = DISCOUNT_CURVE.withParallelShift(SHIFT);
    final YieldAndDiscountCurve yield = YIELD_CURVE.withParallelShift(SHIFT);
    final Map<Double, Double> shiftedDFData = new TreeMap<Double, Double>();
    final Map<Double, Double> shiftedRateData = new TreeMap<Double, Double>();
    for (final Map.Entry<Double, Double> entry : DF_DATA.entrySet()) {
      shiftedDFData.put(entry.getKey(), entry.getValue() + SHIFT);
    }
    for (final Map.Entry<Double, Double> entry : RATE_DATA.entrySet()) {
      shiftedRateData.put(entry.getKey(), entry.getValue() + SHIFT);
    }
    assertEquals(discount, new InterpolatedDiscountCurve(shiftedDFData, LINEAR));
    assertEquals(yield, new InterpolatedYieldCurve(shiftedRateData, LINEAR));
  }

  @Test
  public void testSingleShift() {
    final double t = 2.;
    YieldAndDiscountCurve curve = DISCOUNT_CURVE.withSingleShift(t, SHIFT);
    Map<Double, Double> shiftedData = new TreeMap<Double, Double>(DF_DATA);
    shiftedData.put(t, DF_DATA.get(t) + SHIFT);
    assertEquals(curve, new InterpolatedDiscountCurve(shiftedData, LINEAR));
    curve = YIELD_CURVE.withSingleShift(t, SHIFT);
    shiftedData = new TreeMap<Double, Double>(RATE_DATA);
    shiftedData.put(t, RATE_DATA.get(t) + SHIFT);
    assertEquals(curve, new InterpolatedYieldCurve(shiftedData, LINEAR));
  }

  @Test
  public void testMultipleShift() {
    final double t1 = 2.;
    final double t2 = 3.;
    final Map<Double, Double> shifts = new HashMap<Double, Double>();
    shifts.put(t1, SHIFT);
    shifts.put(t2, -SHIFT);
    YieldAndDiscountCurve curve = DISCOUNT_CURVE.withMultipleShifts(shifts);
    Map<Double, Double> shiftedData = new TreeMap<Double, Double>(DF_DATA);
    shiftedData.put(t1, DF_DATA.get(t1) + SHIFT);
    shiftedData.put(t2, DF_DATA.get(t2) - SHIFT);
    assertEquals(curve, new InterpolatedDiscountCurve(shiftedData, LINEAR));
    curve = YIELD_CURVE.withMultipleShifts(shifts);
    shiftedData = new TreeMap<Double, Double>(RATE_DATA);
    shiftedData.put(t1, RATE_DATA.get(t1) + SHIFT);
    shiftedData.put(t2, RATE_DATA.get(t2) - SHIFT);
    assertEquals(curve, new InterpolatedYieldCurve(shiftedData, LINEAR));
  }

  @Test
  public void testTwoLinearInterpolators() {
    final Map<Double, Interpolator1D<? extends Interpolator1DModel>> map = new HashMap<Double, Interpolator1D<? extends Interpolator1DModel>>();
    map.put(2.1, LINEAR);
    map.put(10., LINEAR);
    YieldAndDiscountCurve curve1 = new InterpolatedDiscountCurve(DF_DATA, LINEAR);
    YieldAndDiscountCurve curve2 = new InterpolatedDiscountCurve(DF_DATA, map);
    assertEquals(curve1.getDiscountFactor(1.5), curve2.getDiscountFactor(1.5), EPS);
    assertEquals(curve1.getDiscountFactor(2.5), curve2.getDiscountFactor(2.5), EPS);
    curve1 = new InterpolatedYieldCurve(RATE_DATA, LINEAR);
    curve2 = new InterpolatedYieldCurve(RATE_DATA, map);
    assertEquals(curve1.getInterestRate(1.5), curve2.getInterestRate(1.5), EPS);
    assertEquals(curve1.getInterestRate(2.5), curve2.getInterestRate(2.5), EPS);
  }

  @Test
  public void testMultipleInterpolators() {
    final Map<Double, Interpolator1D<? extends Interpolator1DModel>> map = new HashMap<Double, Interpolator1D<? extends Interpolator1DModel>>();
    map.put(2.1, LINEAR);
    map.put(10., STEP);
    YieldAndDiscountCurve curve = new InterpolatedDiscountCurve(DF_DATA, map);
    assertEquals(curve.getDiscountFactor(1.5), LINEAR.interpolate(DISCOUNT_MODEL, 1.5), EPS);
    assertEquals(curve.getDiscountFactor(2.5), STEP.interpolate(DISCOUNT_MODEL, 2.5), EPS);
    curve = new InterpolatedYieldCurve(RATE_DATA, map);
    assertEquals(curve.getInterestRate(1.5), LINEAR.interpolate(YIELD_MODEL, 1.5), EPS);
    assertEquals(curve.getInterestRate(2.5), STEP.interpolate(YIELD_MODEL, 2.5), EPS);
  }

  @Test
  public void testEqualsAndHashCode() {
    YieldAndDiscountCurve curve = new InterpolatedDiscountCurve(DF_DATA, LINEAR);
    assertEquals(curve, DISCOUNT_CURVE);
    assertEquals(curve.hashCode(), DISCOUNT_CURVE.hashCode());
    curve = new InterpolatedDiscountCurve(DF_DATA, STEP);
    assertFalse(curve.equals(DISCOUNT_CURVE));
    curve = new InterpolatedDiscountCurve(RATE_DATA, LINEAR);
    assertFalse(curve.equals(DISCOUNT_CURVE));
    assertFalse(DISCOUNT_CURVE.equals(YIELD_CURVE));
    curve = new InterpolatedYieldCurve(RATE_DATA, LINEAR);
    assertEquals(curve, YIELD_CURVE);
    assertEquals(curve.hashCode(), YIELD_CURVE.hashCode());
    curve = new InterpolatedYieldCurve(RATE_DATA, STEP);
    assertFalse(curve.equals(YIELD_CURVE));
    curve = new InterpolatedYieldCurve(DF_DATA, LINEAR);
    assertFalse(curve.equals(YIELD_CURVE));
  }
}
