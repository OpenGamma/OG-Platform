/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * 
 */
public class ExponentialInterpolator1DTest {
  private static final Interpolator1D<Interpolator1DModel, InterpolationResult> INTERPOLATOR = new ExponentialInterpolator1D();
  private static final double EPS = 1e-4;

  @Test(expected = IllegalArgumentException.class)
  public void testNullModel() {
    INTERPOLATOR.interpolate(null, 2.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate(Interpolator1DModelFactory.fromMap(Collections.<Double, Double>emptyMap()), null);
  }

  @Test(expected = InterpolationException.class)
  public void testLowValue() {
    INTERPOLATOR.interpolate(Interpolator1DModelFactory.fromSortedArrays(new double[] {1, 2}, new double[] {1, 2}), -4.);
  }

  @Test(expected = InterpolationException.class)
  public void testHgihValue() {
    INTERPOLATOR.interpolate(Interpolator1DModelFactory.fromSortedArrays(new double[] {1, 2}, new double[] {1, 2}), -4.);
  }

  @Test
  public void test() {
    final Map<Double, Double> data = new HashMap<Double, Double>();
    final double t1 = 3;
    final double t2 = 4;
    final double df1 = 0.8325;
    final double df2 = 0.7572;
    data.put(t1, df1);
    data.put(t2, df2);
    assertEquals(0.7957, INTERPOLATOR.interpolate(Interpolator1DModelFactory.fromMap(data), 3.5).getResult(), EPS);
  }
}
