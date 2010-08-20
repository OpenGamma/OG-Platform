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
public class ConjugateGradientTest {

  private static double EPS = 1e-8;
  private static Minimizer1D LINE_MINIMIZER = new BrentMinimizer1D();
  private static MinimizerNDWithFirstDerivative MINIMISER = new ConjugateGradient(LINE_MINIMIZER, EPS, 500);

  private static final Function1D<DoubleMatrix1D, Double> ROSENBROCK = new Function1D<DoubleMatrix1D, Double>() {

    @Override
    public Double evaluate(DoubleMatrix1D x) {
      return square(1 - x.getEntry(0)) + 100 * square(x.getEntry(1) - square(x.getEntry(0)));
    }
  };

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> ROSENBROCK_GRAD = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
      double[] temp = new double[2];
      temp[0] = 2 * (x.getEntry(0) - 1) + 400 * x.getEntry(0) * (square(x.getEntry(0)) - x.getEntry(1));
      temp[1] = 200 * (x.getEntry(1) - square(x.getEntry(0)));
      return new DoubleMatrix1D(temp);
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

  private class FD_grad extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {
    private static final double EPS = 1e-6;
    private final Function1D<DoubleMatrix1D, Double> _f;

    public FD_grad(final Function1D<DoubleMatrix1D, Double> f) {
      _f = f;
    }

    @Override
    public DoubleMatrix1D evaluate(DoubleMatrix1D x) {

      int n = x.getNumberOfElements();
      double[] res = new double[n];
      double up, down;
      for (int i = 0; i < n; i++) {
        double[] temp = x.toArray();
        temp[i] += EPS;
        up = _f.evaluate(new DoubleMatrix1D(temp));
        temp[i] -= 2 * EPS;
        down = _f.evaluate(new DoubleMatrix1D(temp));
        res[i] = (up - down) / 2 / EPS;
      }
      return new DoubleMatrix1D(res);
    }

  }

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> COUPLED_ROSENBROCK_GRAD = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
      int n = x.getNumberOfElements();

      double[] res = new double[n];
      res[0] = 2 * (x.getEntry(0) - 1) + 400 * x.getEntry(0) * (square(x.getEntry(0)) - x.getEntry(1));
      res[n - 1] = 200 * (x.getEntry(n - 1) - square(x.getEntry(n - 2)));
      for (int i = 1; i < n - 1; i++) {
        res[i] = 2 * (x.getEntry(i) - 1) + 400 * x.getEntry(i) * (square(x.getEntry(i)) - x.getEntry(i + 1)) + 200 * (x.getEntry(i) - square(x.getEntry(i - 1)));
      }
      return new DoubleMatrix1D(res);
    }
  };

  @Test
  public void TestSolvingRosenbrock() {
    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0});
    DoubleMatrix1D solution = MINIMISER.minimize(ROSENBROCK, ROSENBROCK_GRAD, start);
    assertEquals(1.0, solution.getEntry(0), 1e-8);
    assertEquals(1.0, solution.getEntry(1), 1e-8);
  }

  @Test
  public void TestSolvingCoupledRosenbrock() {
    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 1.0});
    Function1D<DoubleMatrix1D, DoubleMatrix1D> fd = new FD_grad(ROSENBROCK);
    DoubleMatrix1D solution = MINIMISER.minimize(COUPLED_ROSENBROCK, COUPLED_ROSENBROCK_GRAD, start);
    for (int i = 0; i < solution.getNumberOfElements(); i++) {
      assertEquals(1.0, solution.getEntry(i), 10 * EPS);
    }
  }
}
