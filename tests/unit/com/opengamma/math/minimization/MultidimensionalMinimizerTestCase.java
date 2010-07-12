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
  private static final FunctionND<Double, Double> ROSENBROCK = new FunctionND<Double, Double>(2) {

    @Override
    protected Double evaluateFunction(final Double[] coord) {
      final double x = coord[0];
      final double y = coord[1];
      return (1 - x) * (1 - x) + 100 * (y - x * x) * (y - x * x);
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
    double[] r = minimizer.minimize(F_2D, new double[] {10., 10.});
    assertEquals(r[0], -3.4, EPS);
    assertEquals(r[1], 1, EPS);
    r = (minimizer.minimize(ROSENBROCK, new double[] {1000, -1020}));
    assertEquals(r[0], 1, EPS);
    assertEquals(r[1], 1, EPS);
  }
}
