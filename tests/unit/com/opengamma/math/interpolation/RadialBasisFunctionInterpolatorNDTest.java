/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class RadialBasisFunctionInterpolatorNDTest extends InterpolatorNDTest {
  private static final Function1D<Double, Double> UNIFORM_WEIGHT_FUNCTION = new MultiquadraticRadialBasisFunction();

  @Test
  public void testInputs() {
    try {
      new RadialBasisFunctionInterpolatorND(null, false);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final InterpolatorND interpolator = new RadialBasisFunctionInterpolatorND(UNIFORM_WEIGHT_FUNCTION, false);
    super.testData(interpolator);
    try {
      interpolator.interpolate(FLAT_DATA, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      interpolator.interpolate(FLAT_DATA, Arrays.asList(2., 3., 4., 5.));
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
