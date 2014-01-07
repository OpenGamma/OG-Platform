/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import static com.opengamma.analytics.math.minimization.MinimizationTestFunctions.COUPLED_ROSENBROCK;
import static com.opengamma.analytics.math.minimization.MinimizationTestFunctions.COUPLED_ROSENBROCK_GRAD;
import static com.opengamma.analytics.math.minimization.MinimizationTestFunctions.ROSENBROCK;
import static com.opengamma.analytics.math.minimization.MinimizationTestFunctions.ROSENBROCK_GRAD;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.test.TestGroup;

/**
 * Abstract test.
 */
@Test(groups = TestGroup.UNIT)
public abstract class MultidimensionalMinimizerWithGradientTestCase {

  protected void assertSolvingRosenbrock(final MinimizerWithGradient<Function1D<DoubleMatrix1D, Double>, Function1D<DoubleMatrix1D, DoubleMatrix1D>, DoubleMatrix1D> minimzer, final double tol) {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0});
    final DoubleMatrix1D solution = minimzer.minimize(ROSENBROCK, ROSENBROCK_GRAD, start);
    assertEquals(1.0, solution.getEntry(0), tol);
    assertEquals(1.0, solution.getEntry(1), tol);
  }

  protected void assertSolvingRosenbrockWithoutGradient(final MinimizerWithGradient<Function1D<DoubleMatrix1D, Double>, Function1D<DoubleMatrix1D, DoubleMatrix1D>, DoubleMatrix1D> minimzer,
      final double tol) {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0});
    final DoubleMatrix1D solution = minimzer.minimize(ROSENBROCK, start);
    assertEquals(1.0, solution.getEntry(0), tol);
    assertEquals(1.0, solution.getEntry(1), tol);
  }

  protected void assertSolvingCoupledRosenbrock(final MinimizerWithGradient<Function1D<DoubleMatrix1D, Double>, Function1D<DoubleMatrix1D, DoubleMatrix1D>, DoubleMatrix1D> minimzer, final double tol) {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 1.0});
    final DoubleMatrix1D solution = minimzer.minimize(COUPLED_ROSENBROCK, COUPLED_ROSENBROCK_GRAD, start);
    for (int i = 0; i < solution.getNumberOfElements(); i++) {
      assertEquals(1.0, solution.getEntry(i), tol);
    }
  }

  protected void assertSolvingCoupledRosenbrockWithoutGradient(final MinimizerWithGradient<Function1D<DoubleMatrix1D, Double>, Function1D<DoubleMatrix1D, DoubleMatrix1D>, DoubleMatrix1D> minimzer,
      final double tol) {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 1.0});
    final DoubleMatrix1D solution = minimzer.minimize(COUPLED_ROSENBROCK, start);
    for (int i = 0; i < solution.getNumberOfElements(); i++) {
      assertEquals(1.0, solution.getEntry(i), tol);
    }
  }
}
