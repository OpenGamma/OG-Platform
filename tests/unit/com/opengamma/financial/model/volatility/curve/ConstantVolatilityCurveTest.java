/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.curve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * 
 */
public class ConstantVolatilityCurveTest {
  private static final double SIGMA = 0.2;
  private static final double SHIFT = 0.1;
  private static final ConstantVolatilityCurve CURVE = new ConstantVolatilityCurve(SIGMA);

  @Test(expected = IllegalArgumentException.class)
  public void testNegative() {
    new ConstantVolatilityCurve(-SIGMA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTime() {
    CURVE.getVolatility(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeTime() {
    CURVE.getVolatility(-1.);
  }

  @Test
  public void testGetters() {
    assertEquals(CURVE.getVolatility(1.), SIGMA, 0);
    assertEquals(CURVE.getXData(), Collections.emptySet());
  }

  @Test
  public void testHashCodeAndEquals() {
    ConstantVolatilityCurve other = new ConstantVolatilityCurve(SIGMA);
    assertEquals(other, CURVE);
    assertEquals(other.hashCode(), CURVE.hashCode());
    other = new ConstantVolatilityCurve(SIGMA + SHIFT);
    assertFalse(other.equals(CURVE));
  }

  @Test
  public void testBuilders() {
    ConstantVolatilityCurve shifted = new ConstantVolatilityCurve(SIGMA + SHIFT);
    assertEquals(CURVE.withParallelShift(SHIFT), shifted);
    assertEquals(CURVE.withSingleShift(0.1, SHIFT), shifted);
    Map<Double, Double> shifts = new HashMap<Double, Double>();
    shifts.put(0.1, SHIFT);
    assertEquals(CURVE.withMultipleShifts(shifts), shifted);
    shifts.put(0.2, 2 * SHIFT);
    assertEquals(CURVE.withMultipleShifts(shifts), shifted);
  }

}
