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
 * @author emcleod
 * 
 */
public class StepInterpolator1DTest {
  private static final Interpolator1D INTERPOLATOR = new StepInterpolator1D();
  private static final Map<Double, Double> DATA = new HashMap<Double, Double>();
  private static final double EPS = 1e-13;

  static {
    DATA.put(1., 4.5);
    DATA.put(2., 4.3);
    DATA.put(3., 6.7);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate(null, 2.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(DATA, null);
  }

  @Test
  public void test() {
    double value = -1;
    test(INTERPOLATOR.interpolate(DATA, value), 4.5);
    value = 10;
    test(INTERPOLATOR.interpolate(DATA, value), 6.7);
    value = 1;
    test(INTERPOLATOR.interpolate(DATA, value), 4.5);
    value = 1.1;
    test(INTERPOLATOR.interpolate(DATA, value), 4.5);
    value = 2 - EPS * 10;
    test(INTERPOLATOR.interpolate(DATA, value), 4.5);
    value = 2 - EPS / 10;
    test(INTERPOLATOR.interpolate(DATA, value), 4.3);
    value = 2;
    test(INTERPOLATOR.interpolate(DATA, value), 4.3);
    value = 3;
    test(INTERPOLATOR.interpolate(DATA, value), 6.7);
  }

  private void test(final InterpolationResult<Double> result, final double expected) {
    assertEquals(result.getResult(), expected, EPS);
  }
}
