/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class NewtonVectorRootFinderTest {

  private static final double EPS = 1e-8;
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> LINEAR = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      final double[] data = x.getDataAsPrimitiveArray();
      if (data.length != 2)
        throw new IllegalArgumentException("This test is for 2-d vector only");
      final double[] res = new double[2];
      res[0] = data[0] + data[1];
      res[1] = 2 * data[0] - data[1] - 3.0;
      return new DoubleMatrix1D(res);
    }
  };

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> FUNCTION2D = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      final double[] data = x.getDataAsPrimitiveArray();
      if (data.length != 2)
        throw new IllegalArgumentException("This test is for 2-d vector only");
      final double[] res = new double[2];
      res[0] = data[1] * Math.exp(data[0]) - Math.E;
      res[1] = data[0] * data[0] + data[1] * data[1] - 2.0;
      return new DoubleMatrix1D(res);
    }
  };

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> FUNCTION3D = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      final double[] data = x.getDataAsPrimitiveArray();
      if (data.length != 3)
        throw new IllegalArgumentException("This test is for 3-d vector only");
      final double[] res = new double[3];
      res[0] = Math.exp(data[0] + data[1]) + data[2] - Math.E + 1.0;
      res[1] = data[2] * Math.exp(data[0] - data[1]) + Math.E;
      res[2] = data[0] * data[0] + data[1] * data[1] + data[2] * data[2] - 2.0;
      return new DoubleMatrix1D(res);
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
      if (swapRates != null)
        return;
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
      final double[] yield = x.getDataAsPrimitiveArray();
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

    assertEquals(1.0, x1.getDataAsPrimitiveArray()[0], EPS);
    assertEquals(-1.0, x1.getDataAsPrimitiveArray()[1], EPS);
  }

  @Test
  public void testFunction2D() {

    final DoubleMatrix1D x0 = new DoubleMatrix1D(new double[] { -1.0, 1.0 });
    final VectorRootFinder rootFinder = new NewtonVectorRootFinder(EPS);
    final DoubleMatrix1D x1 = rootFinder.getRoot(FUNCTION2D, x0);

    assertEquals(1.0, x1.getDataAsPrimitiveArray()[0], EPS);
    assertEquals(1.0, x1.getDataAsPrimitiveArray()[1], EPS);
  }

  @Test
  public void testFunction3D() {

    final DoubleMatrix1D x0 = new DoubleMatrix1D(new double[] { 0.8, 0.2, -0.7 });
    final VectorRootFinder rootFinder = new NewtonVectorRootFinder(EPS);
    final DoubleMatrix1D x1 = rootFinder.getRoot(FUNCTION3D, x0);

    assertEquals(1.0, x1.getDataAsPrimitiveArray()[0], EPS);
    assertEquals(0.0, x1.getDataAsPrimitiveArray()[1], EPS);
    assertEquals(-1.0, x1.getDataAsPrimitiveArray()[2], EPS);
  }

  @Test
  public void testYieldCurveBootstrap() {

    final int n = timeGrid.length;
    final double[] flatCurve = new double[n];
    for (int i = 0; i < n; i++)
      flatCurve[i] = 0.05;

    final DoubleMatrix1D x0 = new DoubleMatrix1D(flatCurve);
    final VectorRootFinder rootFinder = new NewtonVectorRootFinder(EPS);
    final DoubleMatrix1D x1 = rootFinder.getRoot(SWAP_RATES, x0);
    for (int i = 0; i < n; i++)
      assertEquals(-Math.log(DUMMY_YEILD_CURVE.evaluate(timeGrid[i])) / timeGrid[i], x1.getDataAsPrimitiveArray()[i],
          EPS);

  }

}
