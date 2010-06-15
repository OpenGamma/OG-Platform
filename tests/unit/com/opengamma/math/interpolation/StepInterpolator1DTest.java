/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * 
 */
public class StepInterpolator1DTest {
  private static final Interpolator1D<Interpolator1DModel> INTERPOLATOR = new StepInterpolator1D();
  private static final Interpolator1DModel DATA;
  private static final double EPS = 1e-13;

  static {
    final Map<Double, Double> map = new HashMap<Double, Double>();
    map.put(1., 4.5);
    map.put(2., 4.3);
    map.put(3., 6.7);
    DATA = Interpolator1DModelFactory.fromMap(map);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate((Interpolator1DModel) null, 2.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(DATA, null);
  }

  @Test(expected = InterpolationException.class)
  public void testLowValue() {
    INTERPOLATOR.interpolate(DATA, -3.);
  }

  @Test(expected = InterpolationException.class)
  public void testHighValue() {
    INTERPOLATOR.interpolate(DATA, 15.);
  }

  @Test
  public void test() {
    double value = 1;
    assertEquals(INTERPOLATOR.interpolate(DATA, value), 4.5, EPS);
    value = 1.1;
    assertEquals(INTERPOLATOR.interpolate(DATA, value), 4.5, EPS);
    value = 2 - EPS * 10;
    assertEquals(INTERPOLATOR.interpolate(DATA, value), 4.5, EPS);
    value = 2 + EPS / 10;
    assertEquals(INTERPOLATOR.interpolate(DATA, value), 4.3, EPS);
    value = 2;
    assertEquals(INTERPOLATOR.interpolate(DATA, value), 4.3, EPS);
    value = 3;
    assertEquals(INTERPOLATOR.interpolate(DATA, value), 6.7, EPS);
  }
}
