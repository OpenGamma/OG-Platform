/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

public class MultidimensionalMinimizerTestCase {
  private static final double EPS = 1e-6;
  private static final Function1D<DoubleMatrix1D, Double> F_2D = new Function1D<DoubleMatrix1D, Double>() {

    @Override
    public Double evaluate(final DoubleMatrix1D x) {
      return (x.getEntry(0) + 3.4) * (x.getEntry(0) + 3.4) + (x.getEntry(1) - 1) * (x.getEntry(1) - 1);
    }

  };
  private static final Function1D<DoubleMatrix1D, Double> ROSENBROCK = new Function1D<DoubleMatrix1D, Double>() {

    @Override
    public Double evaluate(final DoubleMatrix1D coord) {
      final double x = coord.getEntry(0);
      final double y = coord.getEntry(1);
      return (1 - x) * (1 - x) + 100 * (y - x * x) * (y - x * x);
    }

  };

  public void testInputs(final SimplexMinimizer minimizer) {
    try {
      minimizer.minimize(null, new DoubleMatrix1D(new double[] {2., 3.}));
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
  }

  public void test(final SimplexMinimizer minimizer) {
    DoubleMatrix1D r = minimizer.minimize(F_2D, new DoubleMatrix1D(new double[] {10., 10.}));
    assertEquals(r.getEntry(0), -3.4, EPS);
    assertEquals(r.getEntry(1), 1, EPS);
    r = (minimizer.minimize(ROSENBROCK, new DoubleMatrix1D(new double[] {10, -5})));
    assertEquals(r.getEntry(0), 1, EPS);
    assertEquals(r.getEntry(1), 1, EPS);
  }
}
