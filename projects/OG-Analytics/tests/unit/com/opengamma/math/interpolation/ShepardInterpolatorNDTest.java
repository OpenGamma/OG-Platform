/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.junit.Test;

import com.opengamma.math.interpolation.data.InterpolatorNDDataBundle;

/**
 * 
 */
public class ShepardInterpolatorNDTest extends InterpolatorNDTestCase {
  private static final InterpolatorND<InterpolatorNDDataBundle> INTERPOLATOR = new ShepardInterpolatorND(3.0);

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate(null, new double[] {1, 2});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPoint() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(FLAT_DATA), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongDimension() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(FLAT_DATA), new double[] {1, 2});
  }

  @Test
  public void testInterpolation() {
    // testCosExp(INTERPOLATOR, 1e-1); // fairly awful interpolator
    testFlat(INTERPOLATOR, 1e-12);

  }
}
