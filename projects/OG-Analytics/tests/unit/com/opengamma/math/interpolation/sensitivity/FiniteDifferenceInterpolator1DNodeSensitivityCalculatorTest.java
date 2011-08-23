/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.math.interpolation.StepInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class FiniteDifferenceInterpolator1DNodeSensitivityCalculatorTest {
  private static final StepInterpolator1D STEP_INTERPOLATOR = new StepInterpolator1D();
  private static final FiniteDifferenceInterpolator1DNodeSensitivityCalculator<Interpolator1DDataBundle> CALCULATOR = new FiniteDifferenceInterpolator1DNodeSensitivityCalculator<Interpolator1DDataBundle>(
      STEP_INTERPOLATOR);
  private static final Interpolator1DDataBundle DATA;

  static {
    final int n = 10;
    final double[] x = new double[n];
    final double[] y = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = i;
      y[i] = i;
    }
    DATA = STEP_INTERPOLATOR.getDataBundleFromSortedArrays(x, y);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator() {
    new FiniteDifferenceInterpolator1DNodeSensitivityCalculator<Interpolator1DDataBundle>(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CALCULATOR.calculate(null, 1.2);
  }

  @Test
  public void test() {
    final double[] result = CALCULATOR.calculate(DATA, 3.4);
    for (int i = 0; i < result.length; i++) {
      assertEquals(result[i], i == 3 ? 1 : 0, 1e-9);
    }
  }
}
