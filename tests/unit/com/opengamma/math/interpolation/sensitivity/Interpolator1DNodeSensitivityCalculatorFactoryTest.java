/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import static com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculatorFactory.getSensitivityCalculator;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.interpolation.Interpolator1DFactory;

/**
 * 
 */
public class Interpolator1DNodeSensitivityCalculatorFactoryTest {

  @Test(expected = IllegalArgumentException.class)
  public void testBadName() {
    getSensitivityCalculator("a");
  }

  @Test
  public void test() {
    assertEquals(getSensitivityCalculator(Interpolator1DFactory.LINEAR).getClass(), LinearInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(getSensitivityCalculator(Interpolator1DFactory.DOUBLE_QUADRATIC).getClass(), DoubleQuadraticInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(getSensitivityCalculator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE).getClass(), NaturalCubicSplineInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(getSensitivityCalculator(Interpolator1DFactory.FLAT_EXTRAPOLATOR).getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
    assertEquals(getSensitivityCalculator(Interpolator1DFactory.EXPONENTIAL).getClass(), FiniteDifferenceInterpolator1DNodeSensitivityCalculator.class);
  }
}
