/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static com.opengamma.math.minimization.MinimizationTestFunctions.COUPLED_ROSENBROCK;
import static com.opengamma.math.minimization.MinimizationTestFunctions.ROSENBROCK;
import static com.opengamma.math.minimization.MinimizationTestFunctions.UNCOUPLED_ROSENBROCK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

public class MultidimensionalMinimizerTestCase {
  private static final int DIMENSION = 2;
  private static final double EPS = 1e-6;
  private static final Function1D<DoubleMatrix1D, Double> F_2D = new Function1D<DoubleMatrix1D, Double>() {

    @Override
    public Double evaluate(DoubleMatrix1D x) {
      return (x.getEntry(0) + 3.4) * (x.getEntry(0) + 3.4) + (x.getEntry(1) - 1) * (x.getEntry(1) - 1);
    }

  };

  public void testInputs(final VectorMinimizer minimizer) {
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

  public void test(final VectorMinimizer minimizer, double tol) {
    DoubleMatrix1D r = minimizer.minimize(F_2D, new DoubleMatrix1D(new double[] {10., 10.}));
    assertEquals(r.getEntry(0), -3.4, tol);
    assertEquals(r.getEntry(1), 1, tol);
    r = (minimizer.minimize(ROSENBROCK, new DoubleMatrix1D(new double[] {10, -5})));
    assertEquals(r.getEntry(0), 1, tol);
    assertEquals(r.getEntry(1), 1, tol);
  }

  public void testSolvingRosenbrock(final VectorMinimizer minimizer, double tol) {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0});
    final DoubleMatrix1D solution = minimizer.minimize(ROSENBROCK, start);
    assertEquals(1.0, solution.getEntry(0), tol);
    assertEquals(1.0, solution.getEntry(1), tol);
  }

  public void testSolvingUncoupledRosenbrock(final VectorMinimizer minimizer, double tol) {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0, -1.0, 1.0, -1.0, 1.0});
    final DoubleMatrix1D solution = minimizer.minimize(UNCOUPLED_ROSENBROCK, start);
    for (int i = 0; i < solution.getNumberOfElements(); i++) {
      assertEquals(1.0, solution.getEntry(i), tol);
    }
  }

  public void testSolvingCoupledRosenbrock(final VectorMinimizer minimizer, double tol) {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 1.0});
    final DoubleMatrix1D solution = minimizer.minimize(COUPLED_ROSENBROCK, start);
    for (int i = 0; i < solution.getNumberOfElements(); i++) {
      assertEquals(1.0, solution.getEntry(i), tol);
    }
  }

}
