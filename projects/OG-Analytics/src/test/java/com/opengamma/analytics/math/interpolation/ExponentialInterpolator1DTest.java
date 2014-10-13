/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.TreeMap;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the exponential interpolator.
 */
@Test(groups = TestGroup.UNIT)
public class ExponentialInterpolator1DTest {
  private static final Interpolator1D INTERPOLATOR = new ExponentialInterpolator1D();
  private static final double EPS = 1e-4;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataBundle() {
    INTERPOLATOR.interpolate(null, 2.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(Collections.<Double, Double> emptyMap()), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowValue() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundleFromSortedArrays(new double[] {1, 2}, new double[] {1, 2}), -4.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighValue() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundleFromSortedArrays(new double[] {1, 2}, new double[] {1, 2}), -4.);
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
    final TreeMap<Double, Double> data = new TreeMap<>();
    final double t1 = 3;
    final double t2 = 4;
    final double df1 = 0.8325;
    final double df2 = 0.7572;
    data.put(t1, df1);
    data.put(t2, df2);
    assertEquals(0.7957, INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(data), 3.5), EPS);
  }

  @Test(enabled = false)
  public void graph() {
    Interpolator1D interpolatorExponential = new ExponentialInterpolator1DDuplicate();
    double[] t = {1.0, 2.0};
    double[] v = {200.0, 250.0};
    Interpolator1DDataBundle data = interpolatorExponential.getDataBundleFromSortedArrays(t,v);
    int nbInt = 51;
    double[] values = new double[nbInt];
    for (int i=0; i<nbInt; i++) {
      values[i] = interpolatorExponential.interpolate(data, t[0]+i*(t[1]-t[0])/(nbInt-1));
    }
    @SuppressWarnings("unused")
    int a=0;
  }
  
}
