/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;

/**
 * 
 */
public class DoubleQuadraticInterpolator1DTest {

  private static final Interpolator1D<Interpolator1DDoubleQuadraticDataBundle> INTERPOLATOR = new DoubleQuadraticInterpolator1D();
  private static final double[] X_DATA = new double[] {0, 0.4, 1.0, 1.8, 2.8, 5};
  private static final double[] Y_DATA = new double[] {3., 4., 3.1, 2., 7., 2.};

  private static final double[] X_TEST = new double[] {0, 0.3, 1.0, 2.0, 4.5, 5.0};
  private static final double[] Y_TEST = new double[] {3.0, 3.87, 3.1, 2.619393939, 5.068181818, 2.0};

  private static final Interpolator1DDoubleQuadraticDataBundle DATA = INTERPOLATOR.getDataBundle(X_DATA, Y_DATA);

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate(null, 2.3);
  }

  @Test(expected = IllegalArgumentException.class)
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

}
