/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import java.util.TreeMap;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class StepInterpolator1DTest {
  private static final Interpolator1D INTERPOLATOR = new StepInterpolator1D();
  private static final Interpolator1DDataBundle DATA;
  private static final double EPS = 1e-13;

  static {
    final TreeMap<Double, Double> map = new TreeMap<>();
    map.put(1., 4.5);
    map.put(2., 4.3);
    map.put(3., 6.7);
    DATA = INTERPOLATOR.getDataBundle(map);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate((Interpolator1DDataBundle) null, 2.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(DATA, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowValue() {
    INTERPOLATOR.interpolate(DATA, -3.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighValue() {
    INTERPOLATOR.interpolate(DATA, 15.);
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
    double value = 1;
    assertEquals(INTERPOLATOR.interpolate(DATA, value), 4.5, EPS);
    value = 1.1;
    assertEquals(INTERPOLATOR.interpolate(DATA, value), 4.5, EPS);
    value = 2 - EPS * 10;
    assertEquals(INTERPOLATOR.interpolate(DATA, value), 4.5, EPS);
    value = 2 + EPS / 10;
    assertEquals(INTERPOLATOR.interpolate(DATA, value), 4.3, EPS);
    value = 2;
    assertEquals(INTERPOLATOR.interpolate(DATA, value), 4.3, EPS);
    value = 3;
    assertEquals(INTERPOLATOR.interpolate(DATA, value), 6.7, EPS);
  }

  @Test(enabled = false)
  void printTest() {
    System.out.println("StepInterpolator1DTest");
    final CombinedInterpolatorExtrapolator interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.STEP, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    for (int i = 0; i < 101; i++) {
      final double x = 0.5 + i * 3.0 / 100;
      System.out.println(x + "\t" + interpolator.interpolate(DATA, x));
    }
  }

}
