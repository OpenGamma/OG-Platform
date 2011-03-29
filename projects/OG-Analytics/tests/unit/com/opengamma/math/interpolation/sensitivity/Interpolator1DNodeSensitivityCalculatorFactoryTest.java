/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import static com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculatorFactory.getSensitivityCalculator;
import com.opengamma.math.interpolation.Interpolator1DFactory;

/**
 * 
 */
public class Interpolator1DNodeSensitivityCalculatorFactoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadName() {
    getSensitivityCalculator("a", false);
  }

  @Test
  public void test() {
    assertEquals(getSensitivityCalculator(Interpolator1DFactory.LINEAR, false).getClass(), LinearInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(getSensitivityCalculator(Interpolator1DFactory.DOUBLE_QUADRATIC, false).getClass(), DoubleQuadraticInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(getSensitivityCalculator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, false).getClass(), NaturalCubicSplineInterpolator1DNodeSensitivityCalculator.class);
    assertEquals(getSensitivityCalculator(Interpolator1DFactory.FLAT_EXTRAPOLATOR, false).getClass(), FlatExtrapolator1DNodeSensitivityCalculator.class);
    assertEquals(getSensitivityCalculator(Interpolator1DFactory.EXPONENTIAL, false).getClass(), FiniteDifferenceInterpolator1DNodeSensitivityCalculator.class);
  }
}
