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

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.PolynomialFunction1D;

/**
 * 
 */
public class BarycentricRationalFunctionInterpolator1DTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Function1D<Double, Double> F =
      new PolynomialFunction1D(new double[] {RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble()});
  private static final Interpolator1D<Interpolator1DDataBundle, InterpolationResult> INTERPOLATOR = new BarycentricRationalFunctionInterpolator1D(5);
  private static final double EPS = 1;

  @Test(expected = IllegalArgumentException.class)
  public void testNullDataBundle() {
    INTERPOLATOR.interpolate(null, 2.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(Interpolator1DDataBundleFactory.fromMap(Collections.<Double, Double>emptyMap()), null);
  }

  @Test(expected = InterpolationException.class)
  public void testInsufficentData() {
    INTERPOLATOR.interpolate(Interpolator1DDataBundleFactory.fromArrays(new double[] {1, 2}, new double[] {3, 4}), 1.5);
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
    assertEquals(F.evaluate(x), INTERPOLATOR.interpolate(Interpolator1DDataBundleFactory.fromMap(data), x).getResult(), EPS);
  }

}
