/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import static com.opengamma.math.minimization.MinimizationTestFunctions.COUPLED_ROSENBROCK;
import static com.opengamma.math.minimization.MinimizationTestFunctions.COUPLED_ROSENBROCK_GRAD;
import static com.opengamma.math.minimization.MinimizationTestFunctions.ROSENBROCK;
import static com.opengamma.math.minimization.MinimizationTestFunctions.ROSENBROCK_GRAD;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class MultidimensionalMinimizerWithGradiantTestCase {

  @Test
  public void testSolvingRosenbrock(VectorMinimizerWithGradient minimzer, double tol) {
    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0});
    DoubleMatrix1D solution = minimzer.minimize(ROSENBROCK, ROSENBROCK_GRAD, start);
    assertEquals(1.0, solution.getEntry(0), tol);
    assertEquals(1.0, solution.getEntry(1), tol);
  }

  @Test
  public void testSolvingRosenbrockWithoutGradient(VectorMinimizerWithGradient minimzer, double tol) {
    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0});
    DoubleMatrix1D solution = minimzer.minimize(ROSENBROCK, start);
    assertEquals(1.0, solution.getEntry(0), tol);
    assertEquals(1.0, solution.getEntry(1), tol);
  }

  @Test
  public void testSolvingCoupledRosenbrock(VectorMinimizerWithGradient minimzer, double tol) {
    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 1.0});
    DoubleMatrix1D solution = minimzer.minimize(COUPLED_ROSENBROCK, COUPLED_ROSENBROCK_GRAD, start);
    for (int i = 0; i < solution.getNumberOfElements(); i++) {
      assertEquals(1.0, solution.getEntry(i), tol);
    }
  }

  @Test
  public void testSolvingCoupledRosenbrockWithoutGradient(VectorMinimizerWithGradient minimzer, double tol) {
    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 1.0});
    DoubleMatrix1D solution = minimzer.minimize(COUPLED_ROSENBROCK, start);
    for (int i = 0; i < solution.getNumberOfElements(); i++) {
      assertEquals(1.0, solution.getEntry(i), tol);
    }
  }
}
