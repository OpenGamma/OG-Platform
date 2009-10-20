/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class RadialBasisFunctionInterpolatorNDTest extends InterpolatorNDTest {
  private static final Function1D<Double, Double> UNIFORM_WEIGHT_FUNCTION = new MultiquadraticRadialBasisFunction();
  private static final double EPS = 1e-1;

  @Test
  public void testInputs() {
    try {
      new RadialBasisFunctionInterpolatorND(null, false);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final InterpolatorND interpolator = new RadialBasisFunctionInterpolatorND(UNIFORM_WEIGHT_FUNCTION, false);
    try {
      interpolator.interpolate(FLAT_DATA, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      interpolator.interpolate(FLAT_DATA, Arrays.asList(2., 3., 4.));
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void test() {
    InterpolatorND interpolator = new RadialBasisFunctionInterpolatorND(UNIFORM_WEIGHT_FUNCTION, false);
    final List<Double> value = Arrays.asList(0.2, 0.4);
    assertEquals(interpolator.interpolate(FLAT_DATA, value).getResult(), VALUE, EPS);
    interpolator = new RadialBasisFunctionInterpolatorND(UNIFORM_WEIGHT_FUNCTION, true);
    assertEquals(interpolator.interpolate(FLAT_DATA, value).getResult(), VALUE, EPS);
    testResult(new MultiquadraticRadialBasisFunction(0.5), true);
    testResult(new MultiquadraticRadialBasisFunction(0.5), false);
    testResult(new InverseMultiquadraticRadialBasisFunction(0.5), true);
    testResult(new InverseMultiquadraticRadialBasisFunction(0.5), false);
    testResult(new GaussianRadialBasisFunction(0.5), true);
    testResult(new GaussianRadialBasisFunction(0.5), false);
    testResult(new ThinPlateSplineRadialBasisFunction(0.5), true);
    testResult(new ThinPlateSplineRadialBasisFunction(0.5), false);
  }

  private void testResult(final Function1D<Double, Double> basisFunction, final boolean useNormalized) {
    final Double[] a = new Double[] { 0.13, 0.4, 0.3 };
    final InterpolatorND interpolator = new RadialBasisFunctionInterpolatorND(basisFunction, useNormalized);
    assertEquals(interpolator.interpolate(DATA1, Arrays.asList(a)).getResult(), F1.evaluate(a), EPS);
  }
}
