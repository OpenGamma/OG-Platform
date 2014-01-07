/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import java.util.TreeMap;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RationalFunctionInterpolator1DTest {
  //TODO this test doesn't test answers properly - look at EPS
  private static final Function1D<Double, Double> F = new RealPolynomialFunction1D(new double[] {-0.87, 3.4, 1., -5.});
  private static final Interpolator1D INTERPOLATOR = new RationalFunctionInterpolator1D(3, 1e-12);
  private static final Interpolator1DDataBundle MODEL;
  private static final double EPS = 1;

  static {
    final TreeMap<Double, Double> data = new TreeMap<>();
    double x;
    for (int i = 0; i < 10; i++) {
      x = Double.valueOf(i) / 10.;
      data.put(x, F.evaluate(x));
    }
    MODEL = INTERPOLATOR.getDataBundle(data);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataBundle() {
    INTERPOLATOR.interpolate(null, 2.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(MODEL, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientData() {
    new RationalFunctionInterpolator1D(10, 1e-12).interpolate(MODEL, 0.23);
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
    assertEquals(F.evaluate(0.467), INTERPOLATOR.interpolate(MODEL, 0.467), EPS);
  }
}
