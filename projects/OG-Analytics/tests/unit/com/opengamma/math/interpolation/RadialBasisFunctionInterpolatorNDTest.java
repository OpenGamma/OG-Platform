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
public class RadialBasisFunctionInterpolatorNDTest extends InterpolatorNDTestCase {

  private static final InterpolatorND<RadialBasisFunctionInterpolatorDataBundle> INTERPOLATOR = new RadialBasisFunctionInterpolatorND();

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
  public void testGaussianRadialBasisFunction() {

    double r0 = 1;

    RadialBasisFunctionInterpolatorDataBundle dataBundle = new RadialBasisFunctionInterpolatorDataBundle(COS_EXP_DATA, new GaussianRadialBasisFunction(r0), false);
    testCosExp(INTERPOLATOR, dataBundle, 5e-2);

    r0 = 5;
    // why does normalised fit much better for flat surfaces?
    dataBundle = new RadialBasisFunctionInterpolatorDataBundle(FLAT_DATA, new GaussianRadialBasisFunction(r0), true);
    // printFlat(INTERPOLATOR, dataBundle);
    testFlat(INTERPOLATOR, dataBundle, 1e-6);

  }

  @Test
  public void testMultiquadraticRadialBasisFunction() {

    double r0 = 1;
    RadialBasisFunctionInterpolatorDataBundle dataBundle = new RadialBasisFunctionInterpolatorDataBundle(COS_EXP_DATA, new MultiquadraticRadialBasisFunction(r0), false);
    testCosExp(INTERPOLATOR, dataBundle, 1e-2);
  }

  @Test
  public void testInverseMultiquadraticRadialBasisFunction() {

    double r0 = 2;
    RadialBasisFunctionInterpolatorDataBundle dataBundle = new RadialBasisFunctionInterpolatorDataBundle(COS_EXP_DATA, new InverseMultiquadraticRadialBasisFunction(r0), true);
    testCosExp(INTERPOLATOR, dataBundle, 1e-2);
  }

  @Test
  public void testThinPlateSplineRadialBasisFunction() {

    double r0 = 1.0;
    RadialBasisFunctionInterpolatorDataBundle dataBundle = new RadialBasisFunctionInterpolatorDataBundle(COS_EXP_DATA, new ThinPlateSplineRadialBasisFunction(r0), true);
    testCosExp(INTERPOLATOR, dataBundle, 5e-2);
  }

}
