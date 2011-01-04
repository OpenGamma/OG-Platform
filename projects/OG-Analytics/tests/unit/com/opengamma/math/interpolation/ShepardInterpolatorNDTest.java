/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.junit.Test;

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
    RadialBasisFunctionInterpolatorDataBundle dataBundle = new RadialBasisFunctionInterpolatorDataBundle(FLAT_DATA, new GaussianRadialBasisFunction(), false);
    INTERPOLATOR.interpolate(dataBundle, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongDimension() {
    RadialBasisFunctionInterpolatorDataBundle dataBundle = new RadialBasisFunctionInterpolatorDataBundle(FLAT_DATA, new GaussianRadialBasisFunction(), false);
    INTERPOLATOR.interpolate(dataBundle, new double[] {1, 2});
  }

  @Test
  public void testInterpolation() {

    InterpolatorNDDataBundle dataBundle = new InterpolatorNDDataBundle(COS_EXP_DATA);
    testCosExp(INTERPOLATOR, dataBundle, 1e-1); // fairly awful interpolator

    dataBundle = new InterpolatorNDDataBundle(FLAT_DATA);
    testFlat(INTERPOLATOR, dataBundle, 1e-12);

  }
}
