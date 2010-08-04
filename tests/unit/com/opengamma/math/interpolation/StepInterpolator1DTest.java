/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import java.util.TreeMap;

import org.junit.Test;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.temp.InterpolationResult;

/**
 * 
 */
public class StepInterpolator1DTest {
  private static final Interpolator1D<Interpolator1DDataBundle, InterpolationResult> INTERPOLATOR = new StepInterpolator1D();
  private static final Interpolator1DDataBundle DATA;
  private static final double EPS = 1e-13;

  static {
    final TreeMap<Double, Double> map = new TreeMap<Double, Double>();
    map.put(1., 4.5);
    map.put(2., 4.3);
    map.put(3., 6.7);
    DATA = INTERPOLATOR.getDataBundle(map);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate((Interpolator1DDataBundle) null, 2.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(DATA, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowValue() {
    INTERPOLATOR.interpolate(DATA, -3.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighValue() {
    INTERPOLATOR.interpolate(DATA, 15.);
  }

  @Test
  public void test() {
    double value = 1;
    assertEquals(INTERPOLATOR.interpolate(DATA, value).getResult(), 4.5, EPS);
    value = 1.1;
    assertEquals(INTERPOLATOR.interpolate(DATA, value).getResult(), 4.5, EPS);
    value = 2 - EPS * 10;
    assertEquals(INTERPOLATOR.interpolate(DATA, value).getResult(), 4.5, EPS);
    value = 2 + EPS / 10;
    assertEquals(INTERPOLATOR.interpolate(DATA, value).getResult(), 4.3, EPS);
    value = 2;
    assertEquals(INTERPOLATOR.interpolate(DATA, value).getResult(), 4.3, EPS);
    value = 3;
    assertEquals(INTERPOLATOR.interpolate(DATA, value).getResult(), 6.7, EPS);
  }
}
