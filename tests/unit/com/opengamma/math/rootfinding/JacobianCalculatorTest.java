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
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class JacobianCalculatorTest {
  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> F = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      final double x0 = x.getEntry(0);
      final double x1 = x.getEntry(1);
      final double x2 = x.getEntry(2);
      final double x3 = x.getEntry(3);
      final double y0 = x0;
      final double y1 = 3 * x1 - 4 * x2 * x2 + Math.pow(x3, 5);
      final double y2 = Math.cos(x0) - Math.sin(x1);
      final double y3 = 1 + x0 * x0 + x2 * x2;
      return new DoubleMatrix1D(new double[] {y0, y1, y2, y3});
    }

  };
  private static final JacobianCalculator CALCULATOR = new JacobianCalculator(F);
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> JACOBIAN = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

    @Override
    public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
      final double x0 = x.getEntry(0);
      final double x1 = x.getEntry(1);
      final double x2 = x.getEntry(2);
      final double x3 = x.getEntry(3);
      return new DoubleMatrix2D(new double[][] {new double[] {1, 0, 0, 0}, new double[] {0, 3, -8 * x2, 5 * Math.pow(x3, 4)}, new double[] {-Math.sin(x0), -Math.cos(x1), 0, 0},
          new double[] {2 * x0, 0, 2 * x2, 0}});
    }

  };

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    new JacobianCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMatrix() {
    CALCULATOR.evaluate((DoubleMatrix1D) null);
  }

  @Test
  public void test() {
    final DoubleMatrix1D v = new DoubleMatrix1D(new double[] {0.1, 0.2, 0.3, 0.4});
    assertMatrixEquals(CALCULATOR.evaluate(v), JACOBIAN.evaluate(v));
  }

  private void assertMatrixEquals(final DoubleMatrix2D m1, final DoubleMatrix2D m2) {
    final int m = m1.getNumberOfRows();
    final int n = m1.getNumberOfColumns();
    assertEquals(m, m2.getNumberOfRows());
    assertEquals(n, m2.getNumberOfColumns());
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        assertEquals(m1.getEntry(i, j), m2.getEntry(i, j), 1e-6);
      }
    }

  }
}
