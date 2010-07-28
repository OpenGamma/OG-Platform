/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class ConstantVolatilitySurfaceTest {
  private static final double SIGMA = 0.2;
  private static final DoublesPair XY = DoublesPair.of(3., 5.);
  private static final double SHIFT = 0.01;
  private static final VolatilitySurface SURFACE = new ConstantVolatilitySurface(SIGMA);
  private static final double EPS = 1e-15;

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNegativeVolatility() {
    new ConstantVolatilitySurface(-SIGMA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetVolatilityWithNullXY() {
    SURFACE.getVolatility(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetSingleShiftWithNullPair() {
    SURFACE.withSingleShift(null, 3.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleShiftsWithNull() {
    SURFACE.withMultipleShifts(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleShiftsWithNullShift() {
    SURFACE.withMultipleShifts(Collections.<DoublesPair, Double> singletonMap(DoublesPair.of(2., 2.), null));
  }

  @Test
  public void test() {
    assertEquals(SIGMA, SURFACE.getVolatility(XY), EPS);
  }

  @Test
  public void testParallelShift() {
    final VolatilitySurface surface = SURFACE.withParallelShift(SHIFT);
    assertEquals(SIGMA + SHIFT, surface.getVolatility(XY), EPS);
  }

  @Test
  public void testSingleShift() {
    final VolatilitySurface surface = SURFACE.withSingleShift(XY, SHIFT);
    assertEquals(SIGMA + SHIFT, surface.getVolatility(XY), EPS);
  }

  @Test
  public void testMultipleShift() {
    final VolatilitySurface surface = SURFACE.withMultipleShifts(Collections.<DoublesPair, Double> singletonMap(XY, SHIFT));
    assertEquals(SIGMA + SHIFT, surface.getVolatility(XY), EPS);
  }
}
