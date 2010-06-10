/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * 
 */
public class ExponentialInterpolator1DTest {
  private static final Interpolator1D<Interpolator1DModel> INTERPOLATOR = new ExponentialInterpolator1D();
  private static final double EPS = 1e-4;

  @Test
  public void test() {
    final Map<Double, Double> data = new HashMap<Double, Double>();
    final double t1 = 3;
    final double t2 = 4;
    final double df1 = 0.8325;
    final double df2 = 0.7572;
    data.put(t1, df1);
    data.put(t2, df2);
    try {
      INTERPOLATOR.interpolate(Interpolator1DModelFactory.fromMap(data), null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    assertEquals(0.7957, INTERPOLATOR.interpolate(Interpolator1DModelFactory.fromMap(data), 3.5).getResult(), EPS);
  }
}
