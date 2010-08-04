/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.StepInterpolator1D;

/**
 * 
 */
public class FiniteDifferenceInterpolator1DNodeSensitivityCalculatorTest {
  private static final StepInterpolator1D STEP_INTERPOLATOR = new StepInterpolator1D();
  private static final FiniteDifferenceInterpolator1DNodeSensitivityCalculator<Interpolator1DDataBundle> CALCULATOR = new FiniteDifferenceInterpolator1DNodeSensitivityCalculator<Interpolator1DDataBundle>();
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

  @Test(expected = IllegalArgumentException.class)
  public void testNullInterpolator() {
    CALCULATOR.calculate(null, DATA, 3.4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    CALCULATOR.calculate(STEP_INTERPOLATOR, null, 1.2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    CALCULATOR.calculate(STEP_INTERPOLATOR, DATA, null);
  }

  @Test
  public void test() {
    final double[] result = CALCULATOR.calculate(STEP_INTERPOLATOR, DATA, 3.4);
    for (int i = 0; i < result.length; i++) {
      assertEquals(result[i], i == 3 ? 1 : 0, 1e-9);
    }
  }
}
