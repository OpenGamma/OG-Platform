/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class MontonicIncreasingInterpolator1DTest {
  private static final Interpolator1D INTERPOLATOR = new MonotonicIncreasingInterpolator1D();

  //  private static final double[] X_DATA = new double[] {0.4, 0.9, 1.0, 1.8, 2.8, 5 };
  //  private static final double[] Y_DATA = new double[] {0.8, 4., 4.1, 5.6, 7., 8.1 };
  private static final double[] X_DATA = new double[] {0.4, 0.9, 1.5, 1.8, 2.8, 5 };
  private static final double[] Y_DATA = new double[] {0.48, 1.278, 2.429, 3.027202095
    , 4.74222234, 5.716337815 };
  private static final Interpolator1DDataBundle DATA = INTERPOLATOR.getDataBundle(X_DATA, Y_DATA);

  @Test
  public void lowTest() {
    double x = 0.05;
    double expected = x * Y_DATA[0] / X_DATA[0];
    double res = INTERPOLATOR.interpolate(DATA, x);
    assertEquals(expected, res, 1e-10);
  }

  @Test
  public void test() {
    final int n = X_DATA.length;
    for (int i = 0; i < n; i++) {
      double res = INTERPOLATOR.interpolate(DATA, X_DATA[i]);
      assertEquals(Y_DATA[i], res, 1e-10);
    }
  }

  @Test(enabled = false)
  public void TestPrint() {
    for (int i = 0; i < 101; i++) {
      double x = 10. * i / 100.;
      System.out.println(x + "\t" + INTERPOLATOR.interpolate(DATA, x));
    }

  }

}
