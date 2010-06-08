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
 * @author emcleod
 */
public class Interpolator1DTest {
  private static final Interpolator1D DUMMY = new Interpolator1D() {

    @Override
    public InterpolationResult<Double> interpolate(final Map<Double, Double> data, final Double value) {
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
  
  // REVIEW kirk 2010-06-08 -- This needs to be moved to a test of Interpolator1DModel.

  @Test
  public void testGetLowerBound() {
    final Interpolator1DModel model = DUMMY.initData(DATA);
    try {
      model.getLowerBoundKey(null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      model.getLowerBoundKey(0.);
      fail();
    } catch (final InterpolationException e) {
      // Expected
    }
    try {
      model.getLowerBoundKey(10.);
      fail();
    } catch (final InterpolationException e) {
      // Expected
    }
    assertEquals(model.getLowerBoundKey(3.2), 3., EPS);
    assertEquals(model.getLowerBoundIndex(3.2), 2);
  }

  private void assertArrayEquals(final double[] x, final double[] y) {
    assertEquals(x.length, y.length);
    for (int i = 0; i < x.length; i++) {
      assertEquals(x[i], y[i], EPS);
    }
  }
}
