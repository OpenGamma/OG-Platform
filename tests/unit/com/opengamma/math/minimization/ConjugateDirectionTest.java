/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static com.opengamma.math.UtilFunctions.square;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class ConjugateDirectionTest {

  private static Minimizer1D LINE_MINIMIZER = new BrentMinimizer1D();
  private static MinimizerND MINIMISER = new ConjugateDirection(LINE_MINIMIZER);

  private static final Function1D<DoubleMatrix1D, Double> ROSENBROCK = new Function1D<DoubleMatrix1D, Double>() {

    @Override
    public Double evaluate(DoubleMatrix1D x) {
      return square(1 - x.getEntry(0)) + 100 * square(x.getEntry(1) - square(x.getEntry(0)));
    }
  };

  private static final Function1D<DoubleMatrix1D, Double> UNCOUPLED_ROSENBROCK = new Function1D<DoubleMatrix1D, Double>() {

    @Override
    public Double evaluate(DoubleMatrix1D x) {
      int n = x.getNumberOfElements();
      if (n % 2 != 0) {
        throw new IllegalArgumentException("vector length must be even");
      }
      double sum = 0;
      for (int i = 0; i < n / 2; i++) {
        sum += square(1 - x.getEntry(2 * i)) + 100 * square(x.getEntry(2 * i + 1) - square(x.getEntry(2 * i)));
      }
      return sum;
    }
  };

  private static final Function1D<DoubleMatrix1D, Double> COUPLED_ROSENBROCK = new Function1D<DoubleMatrix1D, Double>() {

    @Override
    public Double evaluate(DoubleMatrix1D x) {
      int n = x.getNumberOfElements();

      double sum = 0;
      for (int i = 0; i < n - 1; i++) {
        sum += square(1 - x.getEntry(i)) + 100 * square(x.getEntry(i + 1) - square(x.getEntry(i)));
      }
      return sum;
    }
  };

  @Test
  public void TestSolvingRosenbrock() {
    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0});
    DoubleMatrix1D solution = MINIMISER.minimize(ROSENBROCK, start);
    assertEquals(1.0, solution.getEntry(0), 1e-8);
    assertEquals(1.0, solution.getEntry(1), 1e-8);
  }

  @Test
  public void TestSolvingUncoupledRosenbrock() {
    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0, -1.0, 1.0, -1.0, 1.0});
    DoubleMatrix1D solution = MINIMISER.minimize(UNCOUPLED_ROSENBROCK, start);
    for (int i = 0; i < solution.getNumberOfElements(); i++) {
      assertEquals(1.0, solution.getEntry(i), 1e-8);
    }
  }

  @Test
  public void TestSolvingCoupledRosenbrock() {
    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 1.0});
    DoubleMatrix1D solution = MINIMISER.minimize(COUPLED_ROSENBROCK, start);
    for (int i = 0; i < solution.getNumberOfElements(); i++) {
      assertEquals(1.0, solution.getEntry(i), 1e-8);
    }
  }

}
