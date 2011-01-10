/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 
 */
public class OrthogonalPolynomialGeneratingFunctionTestCase {
  private static final double EPS = 1e-3;

  protected void testInputs(final GeneratingFunction<Double, GaussianQuadratureFunction> f, final Double[] params) {
    try {
      f.generate(-1, params);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      f.generate(3, (Double[]) null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      f.generate(3, new Double[0]);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  protected void testInputsFixedLimits(final GeneratingFunction<Double, GaussianQuadratureFunction> f, final Double[] params) {
    try {
      f.generate(-1, params);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  protected void testResults(final GaussianQuadratureFunction f, final double[] x, final double[] w) {
    final double[] x1 = f.getAbscissas();
    final double[] w1 = f.getWeights();
    for (int i = 0; i < x.length; i++) {
      assertEquals(x1[i], x[i], EPS);
      assertEquals(w1[i], w[i], EPS);
    }
  }
}
