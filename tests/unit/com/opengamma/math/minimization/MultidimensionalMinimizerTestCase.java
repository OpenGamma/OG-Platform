/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.opengamma.math.function.FunctionND;

public class MultidimensionalMinimizerTestCase {
  private static final int DIMENSION = 2;
  private static final double EPS = 1e-6;
  private static final FunctionND<Double, Double> F_2D = new FunctionND<Double, Double>(DIMENSION) {

    @Override
    public Double evaluateFunction(final Double[] x) {
      return (x[0] + 3.4) * (x[0] + 3.4) + (x[1] - 1) * (x[1] - 1);
    }

  };

  public void testInputs(final MultidimensionalMinimizer minimizer) {
    try {
      minimizer.minimize(null, new double[] {2., 3.});
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      minimizer.minimize(F_2D, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      minimizer.minimize(F_2D, new double[] {2., 3., 4});
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  public void test(final MultidimensionalMinimizer minimizer) {
    final double[] r = minimizer.minimize(F_2D, new double[] {10., 10.});
    assertEquals(r[0], -3.4, EPS);
    assertEquals(r[1], 1, EPS);
  }
}
