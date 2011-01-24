/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.RealPolynomialFunction1D;
import com.opengamma.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class BarycentricRationalFunctionInterpolator1DTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Function1D<Double, Double> F = new RealPolynomialFunction1D(new double[] {RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble(),
      RANDOM.nextDouble()});
  private static final Interpolator1D<Interpolator1DDataBundle> INTERPOLATOR = new BarycentricRationalFunctionInterpolator1D(5);
  private static final double EPS = 1;

  @Test(expected = IllegalArgumentException.class)
  public void testNullDataBundle() {
    INTERPOLATOR.interpolate(null, 2.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(new double[0], new double[0]), null);
  }

  @Test(expected = MathException.class)
  public void testInsufficentData() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(new double[] {1, 2}, new double[] {3, 4}), 1.5);
  }

  @Test
  public void testDataBundleType1() {
    assertEquals(INTERPOLATOR.getDataBundle(new double[] {1, 2, 3}, new double[] {1, 2, 3}).getClass(), ArrayInterpolator1DDataBundle.class);
  }

  @Test
  public void testDataBundleType2() {
    assertEquals(INTERPOLATOR.getDataBundleFromSortedArrays(new double[] {1, 2, 3}, new double[] {1, 2, 3}).getClass(), ArrayInterpolator1DDataBundle.class);
  }

  @Test
  public void test() {
    final int n = 20;
    final double[] x = new double[n];
    final double[] y = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = Double.valueOf(i) / n;
      y[i] = F.evaluate(x[i]);
    }
    final double value = 0.9;
    assertEquals(F.evaluate(value), INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(x, y), value), EPS);
  }

}
