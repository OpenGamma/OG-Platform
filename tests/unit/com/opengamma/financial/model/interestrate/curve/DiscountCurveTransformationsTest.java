/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * 
 * @author emcleod
 */
public class DiscountCurveTransformationsTest {
  private static final InterpolatedDiscountCurve CURVE;
  private static final Map<Double, Double> DATA;
  private static final Interpolator1D INTERPOLATOR = new LinearInterpolator1D();

  static {
    DATA = new TreeMap<Double, Double>();
    DATA.put(1., 0.04);
    DATA.put(2., 0.06);
    DATA.put(3., 0.05);
    DATA.put(4., 0.02);
    CURVE = new InterpolatedDiscountCurve(DATA, INTERPOLATOR);
  }

  @Test
  public void testParallelShift() {
    final DiscountCurve noShift = DiscountCurveTransformation.getParallelShiftedCurve(CURVE, 0);
    assertEquals(noShift, CURVE);
    final double shift = 0.005;
    final Map<Double, Double> shiftedData = new TreeMap<Double, Double>(DATA);
    for (final Map.Entry<Double, Double> entry : shiftedData.entrySet()) {
      shiftedData.put(entry.getKey(), entry.getValue() + shift);
    }
    final DiscountCurve shifted = DiscountCurveTransformation.getParallelShiftedCurve(CURVE, shift);
    assertEquals(shifted, new InterpolatedDiscountCurve(shiftedData, INTERPOLATOR));
  }

  @Test
  public void testSingleShiftedDataPointCurve() {
    final double shift = 0.004;
    final int index = 2;
    try {
      DiscountCurveTransformation.getSingleShiftedDataPointCurve(CURVE, -1, shift);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DiscountCurveTransformation.getSingleShiftedDataPointCurve(CURVE, 10, shift);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final DiscountCurve noShift = DiscountCurveTransformation.getSingleShiftedDataPointCurve(CURVE, index, 0);
    assertEquals(noShift, CURVE);
    final TreeMap<Double, Double> shiftedData = new TreeMap<Double, Double>(DATA);
    int i = 0;
    for (final Map.Entry<Double, Double> entry : shiftedData.entrySet()) {
      if (i++ == index) {
        shiftedData.put(entry.getKey(), entry.getValue() + shift);
        break;
      }
    }
    final DiscountCurve shifted = DiscountCurveTransformation.getSingleShiftedDataPointCurve(CURVE, 2, shift);
    assertEquals(shifted, new InterpolatedDiscountCurve(shiftedData, INTERPOLATOR));
  }

  @Test
  public void testSingleShiftedPointCurve() {
    final double shift = 0.004;
    try {
      DiscountCurveTransformation.getSingleShiftedPointCurve(CURVE, -1, shift);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DiscountCurveTransformation.getSingleShiftedPointCurve(CURVE, 10, shift);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final double time = 1.5;
    final DiscountCurve noShift = DiscountCurveTransformation.getSingleShiftedPointCurve(CURVE, time, 0);
    assertEquals(noShift, CURVE);
    final TreeMap<Double, Double> shiftedData = new TreeMap<Double, Double>(DATA);
    shiftedData.put(time, CURVE.getInterestRate(time) + shift);
    final DiscountCurve shifted = DiscountCurveTransformation.getSingleShiftedPointCurve(CURVE, time, shift);
    assertEquals(shifted, new InterpolatedDiscountCurve(shiftedData, INTERPOLATOR));
  }

  @Test
  public void testMultipleShiftedDataPointCurve() {
    assertEquals(CURVE, DiscountCurveTransformation.getMultipleShiftedDataPointCurve(CURVE, null));
    assertEquals(CURVE, DiscountCurveTransformation.getMultipleShiftedDataPointCurve(CURVE, new HashMap<Integer, Double>()));
    final Map<Integer, Double> shifts = new HashMap<Integer, Double>();
    final double shift = 0.003;
    shifts.put(1, shift);
    final TreeMap<Double, Double> shiftedData = new TreeMap<Double, Double>(DATA);
    int i = 0;
    for (final Map.Entry<Double, Double> entry : shiftedData.entrySet()) {
      if (i == 1) {
        shiftedData.put(entry.getKey(), entry.getValue() + shift);
      } else if (i == 2) {
        shiftedData.put(entry.getKey(), entry.getValue() - shift);
      }
      i++;
    }
    assertEquals(DiscountCurveTransformation.getMultipleShiftedDataPointCurve(CURVE, shifts), DiscountCurveTransformation.getSingleShiftedDataPointCurve(CURVE, 1, shift));
    shifts.put(2, -shift);
    final DiscountCurve shifted = DiscountCurveTransformation.getMultipleShiftedDataPointCurve(CURVE, shifts);
    assertEquals(shifted, new InterpolatedDiscountCurve(shiftedData, INTERPOLATOR));
  }

  @Test
  public void testMultipleShiftedPointCurve() {
    assertEquals(CURVE, DiscountCurveTransformation.getMultipleShiftedPointCurve(CURVE, null));
    assertEquals(CURVE, DiscountCurveTransformation.getMultipleShiftedPointCurve(CURVE, new HashMap<Double, Double>()));
    final Map<Double, Double> shifts = new HashMap<Double, Double>();
    final double shift = 0.003;
    shifts.put(1.5, shift);
    final TreeMap<Double, Double> shiftedData = new TreeMap<Double, Double>(DATA);
    shiftedData.put(1.5, CURVE.getInterestRate(1.5) + shift);
    shiftedData.put(2.5, CURVE.getInterestRate(2.5) - shift);
    assertEquals(DiscountCurveTransformation.getMultipleShiftedPointCurve(CURVE, shifts), DiscountCurveTransformation.getSingleShiftedPointCurve(CURVE, 1.5, shift));
    shifts.put(2.5, -shift);
    final DiscountCurve shifted = DiscountCurveTransformation.getMultipleShiftedPointCurve(CURVE, shifts);
    assertEquals(shifted, new InterpolatedDiscountCurve(shiftedData, INTERPOLATOR));
  }
}
