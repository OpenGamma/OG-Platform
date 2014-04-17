/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FiniteDifferenceInterpolator1DNodeSensitivityCalculatorTest {
  private static final StepInterpolator1D STEP_INTERPOLATOR = new StepInterpolator1D();
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
  public void testNullData() {
    STEP_INTERPOLATOR.getNodeSensitivitiesForValue(null, 1.2);
  }

  @Test
  public void test() {
    double[] result = STEP_INTERPOLATOR.getNodeSensitivitiesForValue(DATA, 3.4);
    for (int i = 0; i < result.length; i++) {
      assertEquals(result[i], i == 3 ? 1 : 0, 1e-9);
    }
    result = STEP_INTERPOLATOR.getNodeSensitivitiesForValue(DATA, 3.4, true);
    for (int i = 0; i < result.length; i++) {
      assertEquals(result[i], i == 3 ? 1 : 0, 1e-9);
    }
  }
}
