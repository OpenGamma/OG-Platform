/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import static org.junit.Assert.assertEquals;

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
 * @author emcleod
 */
public class InterpolatedDiscountCurveTest {
  private static final Interpolator1D<Interpolator1DModel> LINEAR = new LinearInterpolator1D();
  private static final Interpolator1D<Interpolator1DModel> STEP = new StepInterpolator1D();
  private static final Map<Double, Double> DATA = new HashMap<Double, Double>();
  private static final Map<Double, Double> DF_DATA = new HashMap<Double, Double>();
  private static final double SHIFT = 0.001;
  private static final double T = 1.5;
  private static final DiscountCurve CURVE;
  private static final Double EPS = 1e-15;

  static {
    DATA.put(1., 0.03);
    DF_DATA.put(1., Math.exp(-0.03));
    DATA.put(2., 0.04);
    DF_DATA.put(2., Math.exp(-0.08));
    DATA.put(3., 0.05);
    DF_DATA.put(3., Math.exp(-0.15));
    CURVE = new InterpolatedDiscountCurve(DATA, LINEAR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullMap() {
    new InterpolatedDiscountCurve(null, LINEAR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithInsufficientData() {
    new InterpolatedDiscountCurve(Collections.<Double, Double>singletonMap(3., 2.), LINEAR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullInterpolator() {
    final Map<Double, Double> map = new HashMap<Double, Double>();
    map.put(1., 1.);
    map.put(2., 2.);
    new InterpolatedDiscountCurve(map, (Interpolator1D<Interpolator1DModel>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNegativeTime() {
    final Map<Double, Double> map = new HashMap<Double, Double>();
    map.put(-1., 1.);
    map.put(2., 2.);
    new InterpolatedDiscountCurve(map, LINEAR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullInterpolatorMap() {
    new InterpolatedDiscountCurve(DATA, (Map<Double, Interpolator1D<Interpolator1DModel>>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithEmptyInterpolatorMap() {
    new InterpolatedDiscountCurve(DATA, Collections.<Double, Interpolator1D<Interpolator1DModel>>emptyMap());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullInInterpolatorMap() {
    final Map<Double, Interpolator1D<Interpolator1DModel>> map = new HashMap<Double, Interpolator1D<Interpolator1DModel>>();
    map.put(3., LINEAR);
    map.put(6., null);
    new InterpolatedDiscountCurve(DATA, map);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetInterestRateWithNull() {
    CURVE.getInterestRate(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetDiscountFactorWithNull() {
    CURVE.getDiscountFactor(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetInterestRateWithNegativeTime() {
    CURVE.getInterestRate(-2.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetDiscountFactorWithNegativeTime() {
    CURVE.getDiscountFactor(-2.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetParallelShiftWithNull() {
    CURVE.withParallelShift(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetSingleShiftWithNullTime() {
    CURVE.withSingleShift(null, SHIFT);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetSingleShiftWithNegativeTime() {
    CURVE.withSingleShift(-T, SHIFT);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetSingleShiftWithNullShift() {
    CURVE.withSingleShift(T, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetMultipleShiftWithNull() {
    CURVE.withMultipleShifts(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetMultipleShiftWithNegativeTime() {
    CURVE.withMultipleShifts(Collections.<Double, Double>singletonMap(-T, SHIFT));
  }

  @Test
  public void test() {
    final Double df = CURVE.getDiscountFactor(T);
    assertEquals(LINEAR.interpolate(Interpolator1DModelFactory.fromMap(DF_DATA), T).getResult(), df, EPS);
    assertEquals(-Math.log(LINEAR.interpolate(Interpolator1DModelFactory.fromMap(DF_DATA), T).getResult()) / T, CURVE.getInterestRate(T), EPS);
  }

  @Test
  public void testParallelShift() {
    final DiscountCurve curve = CURVE.withParallelShift(SHIFT);
    final Map<Double, Double> shiftedData = new TreeMap<Double, Double>();
    for (final Map.Entry<Double, Double> entry : DATA.entrySet()) {
      shiftedData.put(entry.getKey(), entry.getValue() + SHIFT);
    }
    assertEquals(curve, new InterpolatedDiscountCurve(shiftedData, LINEAR));
  }

  @Test
  public void testSingleShift() {
    final double t = 2.;
    final DiscountCurve curve = CURVE.withSingleShift(t, SHIFT);
    final Map<Double, Double> shiftedData = new TreeMap<Double, Double>(DATA);
    shiftedData.put(t, DATA.get(t) + SHIFT);
    assertEquals(curve, new InterpolatedDiscountCurve(shiftedData, LINEAR));
  }

  @Test
  public void testMultipleShift() {
    final double t1 = 2.;
    final double t2 = 3.;
    final Map<Double, Double> shifts = new HashMap<Double, Double>();
    shifts.put(t1, SHIFT);
    shifts.put(t2, -SHIFT);
    final DiscountCurve curve = CURVE.withMultipleShifts(shifts);
    final Map<Double, Double> shiftedData = new TreeMap<Double, Double>(DATA);
    shiftedData.put(t1, DATA.get(t1) + SHIFT);
    shiftedData.put(t2, DATA.get(t2) - SHIFT);
    assertEquals(curve, new InterpolatedDiscountCurve(shiftedData, LINEAR));
  }

  @Test
  public void testTwoLinearInterpolators() {
    final Map<Double, Interpolator1D<Interpolator1DModel>> map = new HashMap<Double, Interpolator1D<Interpolator1DModel>>();
    map.put(2.1, LINEAR);
    map.put(10., LINEAR);
    final DiscountCurve curve1 = new InterpolatedDiscountCurve(DATA, LINEAR);
    final DiscountCurve curve2 = new InterpolatedDiscountCurve(DATA, map);
    assertEquals(curve1.getDiscountFactor(1.5), curve2.getDiscountFactor(1.5), EPS);
    assertEquals(curve1.getDiscountFactor(2.5), curve2.getDiscountFactor(2.5), EPS);
  }

  @Test
  public void testMultipleInterpolators() {
    final Map<Double, Interpolator1D<Interpolator1DModel>> map = new HashMap<Double, Interpolator1D<Interpolator1DModel>>();
    map.put(2.1, LINEAR);
    map.put(10., STEP);
    final DiscountCurve curve = new InterpolatedDiscountCurve(DATA, map);
    assertEquals(curve.getInterestRate(1.5), -Math.log(LINEAR.interpolate(Interpolator1DModelFactory.fromMap(DF_DATA), 1.5).getResult()) / 1.5, EPS);
    assertEquals(curve.getInterestRate(2.5), -Math.log(STEP.interpolate(Interpolator1DModelFactory.fromMap(DF_DATA), 2.5).getResult()) / 2.5, EPS);
  }
}
