/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * 
 */
public class Interpolator1DTest {
  private static final Interpolator1D DUMMY = new Interpolator1D() {
    @Override
    public InterpolationResult<Double> interpolate(Interpolator1DModel model, Double value) {
      return new InterpolationResult<Double>(0., 0.);
    }

  };
  private static final Map<Double, Double> DATA = new HashMap<Double, Double>();
  private static final double EPS = 1e-15;
  static {
    DATA.put(2., 0.);
    DATA.put(1., 0.);
    DATA.put(3., 0.);
    DATA.put(6.7, 0.);
    DATA.put(3.5, 0.);
  }

  @Test
  public void testDataInit() {
    try {
      DUMMY.initData(null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      DUMMY.initData(Collections.<Double, Double> singletonMap(1., 2.));
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final double[] x = new double[] { 1., 2., 3., 3.5, 6.7 };
    final Interpolator1DModel model = DUMMY.initData(DATA);
    assertArrayEquals(model.getKeys(), x);
  }
  
  private void assertArrayEquals(final double[] x, final double[] y) {
    assertEquals(x.length, y.length);
    for (int i = 0; i < x.length; i++) {
      assertEquals(x[i], y[i], EPS);
    }
  }
}
