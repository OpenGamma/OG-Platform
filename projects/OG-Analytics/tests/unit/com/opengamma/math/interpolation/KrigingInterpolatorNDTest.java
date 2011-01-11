/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.junit.Test;

/**
 * 
 */
public class KrigingInterpolatorNDTest extends InterpolatorNDTestCase {

  private static final InterpolatorND<KrigingInterpolatorDataBundle> INTERPOLATOR = new KrigingInterpolatorND();

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate(null, new double[] {1, 2});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPoint() {
    KrigingInterpolatorDataBundle dataBundle = new KrigingInterpolatorDataBundle(FLAT_DATA, 1.5);
    INTERPOLATOR.interpolate(dataBundle, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongDimension() {
    KrigingInterpolatorDataBundle dataBundle = new KrigingInterpolatorDataBundle(FLAT_DATA, 1.5);
    INTERPOLATOR.interpolate(dataBundle, new double[] {1, 2});
  }

  @Test
  public void testInterpolation() {

    KrigingInterpolatorDataBundle dataBundle = new KrigingInterpolatorDataBundle(COS_EXP_DATA, 1.99);
    testCosExp(INTERPOLATOR, dataBundle, 2e-2);

    // Fails utterly for flat surface since the variogram function will be zero for all r
    dataBundle = new KrigingInterpolatorDataBundle(FLAT_DATA, 1.99);
    // printFlat(INTERPOLATOR, dataBundle);
    // testFlat(INTERPOLATOR, dataBundle, 1e-10);
  }

}
