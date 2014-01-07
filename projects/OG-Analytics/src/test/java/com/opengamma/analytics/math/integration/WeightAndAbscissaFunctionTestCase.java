/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Abstract test.
 */
@Test(groups = TestGroup.UNIT)
public abstract class WeightAndAbscissaFunctionTestCase {
  private static final double EPS = 1e-3;

  protected abstract QuadratureWeightAndAbscissaFunction getFunction();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    getFunction().generate(-1);
  }

  protected void assertResults(final GaussianQuadratureData f, final double[] x, final double[] w) {
    final double[] x1 = f.getAbscissas();
    final double[] w1 = f.getWeights();
    for (int i = 0; i < x.length; i++) {
      assertEquals(x1[i], x[i], EPS);
      assertEquals(w1[i], w[i], EPS);
    }
  }
}
