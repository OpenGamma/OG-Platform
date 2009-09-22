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
import java.util.TreeMap;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class Interpolator1DTest {
  private static final Interpolator1D DUMMY = new Interpolator1D() {

    @Override
    public InterpolationResult<Double> interpolate(Map<Double, Double> data, Double value) {
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
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      DUMMY.initData(Collections.<Double, Double> singletonMap(1., 2.));
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    Double[] x = new Double[] { 1., 2., 3., 3.5, 6.7 };
    TreeMap<Double, Double> sorted = DUMMY.initData(DATA);
    assertArrayEquals(sorted.keySet().toArray(new Double[0]), x);
  }

  @Test
  public void testGetLowerBound() {
    TreeMap<Double, Double> sorted = DUMMY.initData(DATA);
    try {
      DUMMY.getLowerBoundKey(null, 0.);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      DUMMY.getLowerBoundKey(sorted, null);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      DUMMY.getLowerBoundKey(sorted, 0.);
      fail();
    } catch (InterpolationException e) {
      // Expected
    }
    try {
      DUMMY.getLowerBoundKey(sorted, 10.);
      fail();
    } catch (InterpolationException e) {
      // Expected
    }
    try {
      DUMMY.getLowerBoundKey(sorted, 6.7);
      fail();
    } catch (InterpolationException e) {
      // Expected
    }
    assertEquals(DUMMY.getLowerBoundKey(sorted, 3.2), 3., EPS);
    assertEquals(DUMMY.getLowerBoundIndex(sorted, 3.2), 2);
  }

  private void assertArrayEquals(Double[] x, Double[] y) {
    assertEquals(x.length, y.length);
    for (int i = 0; i < x.length; i++) {
      assertEquals(x[i], y[i]);
    }
  }
}
