/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public abstract class OrthogonalPolynomialGeneratingFunctionTestCase {
  private static final double EPS = 1e-3;

  protected abstract QuadratureWeightAndAbscissaFunction getFunction();

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    getFunction().generate(-1, 0., 1.);
  }

  public void testResults(final GaussianQuadratureFunction f, final double[] x, final double[] w) {
    final double[] x1 = f.getAbscissas();
    final double[] w1 = f.getWeights();
    for (int i = 0; i < x.length; i++) {
      assertEquals(x1[i], x[i], EPS);
      assertEquals(w1[i], w[i], EPS);
    }
  }
}
