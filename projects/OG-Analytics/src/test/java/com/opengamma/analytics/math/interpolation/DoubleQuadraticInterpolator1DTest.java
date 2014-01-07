/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DoubleQuadraticInterpolator1DTest {
  private static final Interpolator1D INTERPOLATOR = new DoubleQuadraticInterpolator1D();

  private static final double[] X_DATA = new double[] {0, 0.4, 1.0, 1.8, 2.8, 5 };
  private static final double[] Y_DATA = new double[] {3., 4., 3.1, 2., 7., 2. };

  private static final double[] X_TEST = new double[] {0, 0.3, 1.0, 2.0, 4.5, 5.0 };
  private static final double[] Y_TEST = new double[] {3.0, 3.87, 3.1, 2.619393939, 5.068181818, 2.0 };
  private static final Interpolator1DDataBundle DATA = INTERPOLATOR.getDataBundle(X_DATA, Y_DATA);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate(null, 2.3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(DATA, null);
  }

  @Test
  public void testDataBundleType1() {
    assertEquals(INTERPOLATOR.getDataBundle(X_DATA, Y_DATA).getClass(), Interpolator1DDoubleQuadraticDataBundle.class);
  }

  @Test
  public void testDataBundleType2() {
    assertEquals(INTERPOLATOR.getDataBundleFromSortedArrays(X_DATA, Y_DATA).getClass(), Interpolator1DDoubleQuadraticDataBundle.class);
  }

  @Test
  public void test() {
    for (int i = 0; i < X_TEST.length; i++) {
      assertEquals(INTERPOLATOR.interpolate(DATA, X_TEST[i]), Y_TEST[i], 1e-8);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void samexNodesTest() {
    final double[] xData = new double[] {0.4, 0.7, 0.9, 0.9, 1.3, 1.8 };
    final double[] yData = new double[] {0.4, 0.5, 0.3, 0.8, 0.7, 1.0 };
    final Interpolator1DDataBundle data = INTERPOLATOR.getDataBundle(xData, yData);
    double y = INTERPOLATOR.interpolate(data, 1.0);
    assertTrue("y: " + y, !Double.isNaN(y));
  }

  @Test
  public void testSingleData() {
    final double x = 1.4;
    final double y = 0.34;
    Interpolator1DDataBundle dataBundle = INTERPOLATOR.getDataBundleFromSortedArrays(new double[] {x }, new double[] {y });
    double value = INTERPOLATOR.interpolate(dataBundle, x);
    assertEquals(y, value, 0.0);
  }

  @Test
  public void testTwoData() {
    final double[] x = new double[] {1.4, 1.8 };
    final double[] y = new double[] {0.34, 0.56 };
    Interpolator1DDataBundle dataBundle = INTERPOLATOR.getDataBundleFromSortedArrays(x, y);
    double value = INTERPOLATOR.interpolate(dataBundle, 1.6);
    assertEquals((y[0] + y[1]) / 2, value, 0.0);
  }

  @Test
  public void testSingleDerivativeData() {
    final double x = 1.4;
    final double y = 0.34;
    Interpolator1DDataBundle dataBundle = INTERPOLATOR.getDataBundleFromSortedArrays(new double[] {x }, new double[] {y });
    double value = INTERPOLATOR.firstDerivative(dataBundle, x);
    assertEquals(0., value, 0.0);
  }

  @Test
  public void testTwoDerivativeData() {
    final double[] x = new double[] {1.4, 1.8 };
    final double[] y = new double[] {0.34, 0.56 };
    Interpolator1DDataBundle dataBundle = INTERPOLATOR.getDataBundleFromSortedArrays(x, y);
    double value = INTERPOLATOR.firstDerivative(dataBundle, 1.5);
    assertEquals((y[1] - y[0]) / (x[1] - x[0]), value, 0.0);
  }

  @Test
  public void derivativeFiniteDifferenceTest() {
    final double[] xData = new double[] {0.4, 0.7, 0.9, 0.95, 1.3, 1.8 };
    final double[] yData = new double[] {0.4, 0.5, 0.3, 0.8, 0.7, 1.0 };
    final double eps = 1.e-6;
    final double[] xKeys = new double[] {0.5, 0.77, 0.92, 1.13, 1.5 };
    final int nKeys = xKeys.length;

    final Interpolator1DDataBundle data = INTERPOLATOR.getDataBundle(xData, yData);
    for (int i = 0; i < nKeys; ++i) {
      double y = INTERPOLATOR.firstDerivative(data, xKeys[i]);
      assertEquals(0.5 * (INTERPOLATOR.interpolate(data, xKeys[i] + eps) - INTERPOLATOR.interpolate(data, xKeys[i] - eps)) / eps, y, eps);
    }
  }
}
