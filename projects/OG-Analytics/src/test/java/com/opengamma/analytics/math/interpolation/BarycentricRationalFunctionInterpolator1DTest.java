/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BarycentricRationalFunctionInterpolator1DTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final Function1D<Double, Double> F = new RealPolynomialFunction1D(new double[] {RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble(),
      RANDOM.nextDouble()});
  private static final Interpolator1D INTERPOLATOR = new BarycentricRationalFunctionInterpolator1D(5, 1e-12);
  private static final double EPS = 1;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataBundle() {
    INTERPOLATOR.interpolate(null, 2.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(new double[0], new double[0]), null);
  }

  @Test(expectedExceptions = MathException.class)
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
