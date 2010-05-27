/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static com.opengamma.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.linearalgebra.SVDecompositionCommons;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class NewtonVectorRootFinderTest {

  private static final double EPS = 1e-8;
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> LINEAR = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      final double[] data = x.getData();
      if (data.length != 2) {
        throw new IllegalArgumentException("This test is for 2-d vector only");
      }
      final double[] res = new double[2];
      res[0] = data[0] + data[1];
      res[1] = 2 * data[0] - data[1] - 3.0;
      return new DoubleMatrix1D(res);
    }
  };

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> FUNCTION2D = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      final double[] data = x.getData();
      if (data.length != 2) {
        throw new IllegalArgumentException("This test is for 2-d vector only");
      }
      final double[] res = new double[2];
      res[0] = data[1] * Math.exp(data[0]) - Math.E;
      res[1] = data[0] * data[0] + data[1] * data[1] - 2.0;
      return new DoubleMatrix1D(res);
    }
  };

  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> JACOBIAN2D = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

    @Override
    public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
      if (x.getNumberOfElements() != 2) {
        throw new IllegalArgumentException("This test is for 2-d vector only");
      }
      final double[][] res = new double[2][2];
      double temp = Math.exp(x.getEntry(0));

      res[0][0] = x.getEntry(1) * temp;
      res[0][1] = temp;
      for (int i = 0; i < 2; i++) {
        res[1][i] = 2 * x.getEntry(i);
      }

      return new DoubleMatrix2D(res);
    }

  };

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> FUNCTION3D = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      if (x.getNumberOfElements() != 3) {
        throw new IllegalArgumentException("This test is for 3-d vector only");
      }
      final double[] res = new double[3];
      res[0] = Math.exp(x.getEntry(0) + x.getEntry(1)) + x.getEntry(2) - Math.E + 1.0;
      res[1] = x.getEntry(2) * Math.exp(x.getEntry(0) - x.getEntry(1)) + Math.E;
      res[2] = OG_ALGEBRA.getInnerProduct(x, x) - 2.0;
      return new DoubleMatrix1D(res);
    }
  };

  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> JACOBIAN3D = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

    @Override
    public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
      if (x.getNumberOfElements() != 3) {
        throw new IllegalArgumentException("This test is for 3-d vector only");
      }
      final double[][] res = new double[3][3];
      double temp1 = Math.exp(x.getEntry(0) + x.getEntry(1));
      double temp2 = Math.exp(x.getEntry(0) - x.getEntry(1));
      res[0][0] = res[0][1] = temp1;
      res[0][2] = 1.0;
      res[1][0] = x.getEntry(2) * temp2;
      res[1][1] = -x.getEntry(2) * temp2;
      res[1][2] = temp2;
      for (int i = 0; i < 3; i++) {
        res[2][i] = 2 * x.getEntry(i);
      }

      return new DoubleMatrix2D(res);
    }

  };

  static final double[] timeGrid = new double[] { 0.5, 1.0, 1.5, 2.0, 3.0 };
  final static Function1D<Double, Double> DUMMY_YEILD_CURVE = new Function1D<Double, Double>() {

    private static final double a = -0.03;
    private static final double b = 0.02;
    private static final double c = 0.5;
    private static final double d = 0.05;

    @Override
    public Double evaluate(final Double x) {
      return Math.exp(-x * ((a + b * x) * Math.exp(c * x) + d));
    }
  };

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> SWAP_RATES = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    private final int n = timeGrid.length;
    private double[] swapRates = null;

    private void calSwapRates() {
      if (swapRates != null) {
        return;
      }
      swapRates = new double[n];
      double acc = 0.0;
      double pi;
      for (int i = 0; i < n; i++) {
        pi = DUMMY_YEILD_CURVE.evaluate(timeGrid[i]);
        acc += (timeGrid[i] - (i == 0 ? 0.0 : timeGrid[i - 1])) * pi;
        swapRates[i] = (1.0 - pi) / acc;
      }
    }

    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      calSwapRates();
      final double[] yield = x.getData();
      final double[] diff = new double[n];
      double pi;
      double acc = 0.0;
      for (int i = 0; i < n; i++) {
        pi = Math.exp(-yield[i] * timeGrid[i]);
        acc += (timeGrid[i] - (i == 0 ? 0.0 : timeGrid[i - 1])) * pi;
        diff[i] = (1.0 - pi) / acc - swapRates[i];
      }

      return new DoubleMatrix1D(diff);
    }

  };

  @Test
  public void testLinear() {

    final DoubleMatrix1D x0 = new DoubleMatrix1D(new double[] { 0.0, 0.0 });
    final VectorRootFinder rootFinder = new NewtonVectorRootFinder();
    final DoubleMatrix1D x1 = rootFinder.getRoot(LINEAR, x0);

    assertEquals(1.0, x1.getData()[0], EPS);
    assertEquals(-1.0, x1.getData()[1], EPS);
  }

  /**
   * Note: at the root (1,1) the Jacobian is singular which leads to very slow convergence and is why
   * we switch to using SVD rather than the default LU
   */
  @Test
  public void testFunction2D() {
    final DoubleMatrix1D x0 = new DoubleMatrix1D(new double[] { -0.0, 0.0 });
    final VectorRootFinder rootFinder = new NewtonVectorRootFinder(EPS);
    assertTrue(rootFinder instanceof NewtonVectorRootFinder);
    final NewtonVectorRootFinder newtonRootFinder = (NewtonVectorRootFinder) rootFinder;
    newtonRootFinder.setDecompositionMethod(new SVDecompositionCommons());
    final DoubleMatrix1D x1 = rootFinder.getRoot(FUNCTION2D, JACOBIAN2D, x0);

    //R White - I'm sure these used to work without the 10*EPS
    assertEquals(1.0, x1.getEntry(0), 10 * EPS);
    assertEquals(1.0, x1.getEntry(1), 10 * EPS);
  }

  @Test
  public void testFunction3D() {

    final DoubleMatrix1D x0 = new DoubleMatrix1D(new double[] { 0.8, 0.2, -0.7 });
    final VectorRootFinder rootFinder = new NewtonVectorRootFinder(EPS);
    final DoubleMatrix1D x1 = rootFinder.getRoot(FUNCTION3D, x0);

    assertEquals(1.0, x1.getData()[0], EPS);
    assertEquals(0.0, x1.getData()[1], EPS);
    assertEquals(-1.0, x1.getData()[2], EPS);
  }

  @Test
  public void testJacobian3D() {

    final DoubleMatrix1D x0 = new DoubleMatrix1D(new double[] { -1.0, 0.2, -0.7 });
    final VectorRootFinder rootFinder = new NewtonVectorRootFinder(EPS);
    final DoubleMatrix1D x1 = rootFinder.getRoot(FUNCTION3D, JACOBIAN3D, x0);

    assertEquals(1.0, x1.getData()[0], EPS);
    assertEquals(0.0, x1.getData()[1], EPS);
    assertEquals(-1.0, x1.getData()[2], EPS);
  }

  @Test
  public void testYieldCurveBootstrap() {

    final int n = timeGrid.length;
    final double[] flatCurve = new double[n];
    for (int i = 0; i < n; i++) {
      flatCurve[i] = 0.05;
    }

    final DoubleMatrix1D x0 = new DoubleMatrix1D(flatCurve);
    final VectorRootFinder rootFinder = new NewtonVectorRootFinder(EPS);
    final DoubleMatrix1D x1 = rootFinder.getRoot(SWAP_RATES, x0);
    for (int i = 0; i < n; i++) {
      assertEquals(-Math.log(DUMMY_YEILD_CURVE.evaluate(timeGrid[i])) / timeGrid[i], x1.getData()[i], EPS);
    }

  }

}
