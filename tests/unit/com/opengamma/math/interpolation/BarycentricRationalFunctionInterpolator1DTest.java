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
public class BarycentricRationalFunctionInterpolator1DTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Function1D<Double, Double> F = new PolynomialFunction1D(new Double[] { RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble(),
      RANDOM.nextDouble() });
  private static final Interpolator1D INTERPOLATOR = new BarycentricRationalFunctionInterpolator1D(5);
  private static final double EPS = 1;

  @Test
  public void testInputs() {
    try {
      INTERPOLATOR.interpolate((Map<Double, Double>)null, 2.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      INTERPOLATOR.interpolate(Collections.<Double, Double> emptyMap(), 0.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final Map<Double, Double> map = new HashMap<Double, Double>();
    map.put(1., 2.);
    map.put(3., 4.);
    try {
      INTERPOLATOR.interpolate(map, 1.5);
      fail();
    } catch (final InterpolationException e) {
      // Expected
    }
  }

  @Test
  public void test() {
    final Map<Double, Double> data = new HashMap<Double, Double>();
    double x;
    for (int i = 0; i < 20; i++) {
      x = Double.valueOf(i) / 20.;
      data.put(x, F.evaluate(x));
    }
    x = 0.9;
    assertEquals(F.evaluate(x), INTERPOLATOR.interpolate(data, x).getResult(), EPS);
  }

}
