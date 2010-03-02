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

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.PolynomialFunction1D;

/**
 * 
 * @author emcleod
 */
public class NaturalCubicSplineInterpolator1DTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Double[] COEFF = new Double[] { -0.4, 0.05, 0.2, 1. };
  private static final Interpolator1D INTERPOLATOR = new NaturalCubicSplineInterpolator1D();
  private static final Function1D<Double, Double> CUBIC = new PolynomialFunction1D(COEFF);
  private static final double EPS = 1e-1;

  @Test
  public void testInputs() {
    try {
      INTERPOLATOR.interpolate(null, 3.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      INTERPOLATOR.interpolate(Collections.<Double, Double> emptyMap(), 3.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final Map<Double, Double> map = new HashMap<Double, Double>();
    map.put(1., 2.);
    map.put(2., 4.);
    try {
      INTERPOLATOR.interpolate(map, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void test() {
    final Map<Double, Double> data = new HashMap<Double, Double>();
    double x;
    for (int i = 0; i < 12; i++) {
      x = Double.valueOf(i) / 10.;
      data.put(x, CUBIC.evaluate(x));
    }
    for (int i = 0; i < 100; i++) {
      x = RANDOM.nextDouble();
      assertEquals(CUBIC.evaluate(x), INTERPOLATOR.interpolate(data, x).getResult(), EPS);
    }
  }
}
