/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;

import org.junit.Test;

/**
 * 
 */
public class ConstantYieldCurveTest {
  private static final double EPS = 1e-15;
  private static final double RATE = 0.05;
  private static final double T = 4;
  private static final double SHIFT = 0.001;
  private static final YieldAndDiscountCurve CURVE = new ConstantYieldCurve(RATE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullInput() {
    new ConstantYieldCurve(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInput() {
    new ConstantYieldCurve(-RATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetDiscountFactor() {
    CURVE.getDiscountFactor(-T);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetInterestRate() {
    CURVE.getInterestRate(-T);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetParallelShift() {
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
    CURVE.withMultipleShifts(Collections.<Double, Double>singletonMap(-T, RATE));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetMultipleShiftWithNullShift() {
    CURVE.withMultipleShifts(Collections.<Double, Double>singletonMap(T, null));
  }

  @Test
  public void test() {
    assertEquals(RATE, CURVE.getInterestRate(T), EPS);
    assertEquals(Math.exp(-RATE * T), CURVE.getDiscountFactor(T), EPS);
    YieldAndDiscountCurve curve = CURVE.withParallelShift(SHIFT);
    assertEquals(RATE + SHIFT, curve.getInterestRate(T), EPS);
    assertEquals(Math.exp(-T * (RATE + SHIFT)), curve.getDiscountFactor(T), EPS);
    curve = CURVE.withSingleShift(T, SHIFT);
    assertEquals(RATE + SHIFT, curve.getInterestRate(T), EPS);
    assertEquals(Math.exp(-T * (RATE + SHIFT)), curve.getDiscountFactor(T), EPS);
    curve = CURVE.withMultipleShifts(Collections.<Double, Double>singletonMap(T, SHIFT));
    assertEquals(RATE + SHIFT, curve.getInterestRate(T), EPS);
    assertEquals(Math.exp(-T * (RATE + SHIFT)), curve.getDiscountFactor(T), EPS);
  }

  @Test
  public void testHashCodeAndEquals() {
    YieldAndDiscountCurve curve = new ConstantYieldCurve(RATE);
    assertEquals(CURVE, curve);
    assertEquals(CURVE.hashCode(), curve.hashCode());
    curve = new ConstantYieldCurve(RATE + 0.01);
    assertFalse(CURVE.equals(curve));
    curve = CURVE.withMultipleShifts(Collections.<Double, Double>emptyMap());
    assertEquals(curve, CURVE);
    curve = CURVE.withMultipleShifts(Collections.singletonMap(1., 0.001));
    assertFalse(curve.equals(CURVE));
    curve = CURVE.withParallelShift(0.002);
    assertFalse(curve.equals(CURVE));
    curve = CURVE.withSingleShift(0.1, 0.01);
    assertFalse(curve.equals(CURVE));
  }
}
