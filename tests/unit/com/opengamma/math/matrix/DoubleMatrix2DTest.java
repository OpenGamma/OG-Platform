/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author emcleod
 * 
 */
public class DoubleMatrix2DTest {
  private static final DoubleMatrix2D A = new DoubleMatrix2D(new double[][] { { 1., 2., 3. }, { -1., 1., 0. },
      { -2., 1., -2. } });
  private static final DoubleMatrix2D B = new DoubleMatrix2D(new double[][] { { 1, 1 }, { 2, -2 }, { 3, 1 } });
  private static final DoubleMatrix2D C = new DoubleMatrix2D(new double[][] { { 14, 0 }, { 1, -3 }, { -6, -6 } });
  private static final DoubleMatrix1D D = new DoubleMatrix1D(new double[] { 1, 1, 1 });

  @Test(expected = IllegalArgumentException.class)
  public void testNullPrimitiveArray() {
    new DoubleMatrix2D((double[][]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullObjectArray() {
    new DoubleMatrix2D((Double[][]) null);
  }

  @Test
  public void testEmptyArray() {
    final DoubleMatrix2D d = new DoubleMatrix2D(new double[0][0]);
    final double[][] primitive = d.getData();
    assertEquals(primitive.length, 0);
    assertEquals(d.getNumberOfColumns(), 0);
    assertEquals(d.getNumberOfRows(), 0);
    assertEquals(d.getNumberOfElements(), 0);
  }

  @Test
  public void testArrays() {
    final int n = 10;
    final int m = 30;
    double[][] x = new double[m][n];
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        x[i][j] = i * j;
      }
    }
    DoubleMatrix2D d = new DoubleMatrix2D(x);
    assertEquals(d.getNumberOfRows(), m);
    assertEquals(d.getNumberOfColumns(), n);
    assertEquals(d.getNumberOfElements(), m * n);
    final double[][] y = d.getData();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        assertEquals(x[i][j], y[i][j], 1e-15);
      }
    }
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        y[i][j] = Double.valueOf(i * j);
      }
    }
    d = new DoubleMatrix2D(y);
    x = d.getData();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        assertEquals(x[i][j], y[i][j], 1e-15);
      }
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMatrixSizeMismatch() {
    DoubleMatrix2D.multiply(B, A);
  }

  @Test
  public void testMultiply() {
    DoubleMatrix2D c = DoubleMatrix2D.multiply(A, B);
    final int rows = c.getNumberOfRows();
    final int cols = c.getNumberOfColumns();
    int i, j;
    for (i = 0; i < rows; i++)
      for (j = 0; j < cols; j++)
        assertEquals(c.getEntry(i, j), C.getEntry(i, j), 1e-15);

    c = A.multiply(B);
    for (i = 0; i < rows; i++)
      for (j = 0; j < cols; j++)
        assertEquals(c.getEntry(i, j), C.getEntry(i, j), 1e-15);

    final DoubleMatrix1D d = A.multiply(D);
    assertEquals(6, d.getEntry(0), 1e-15);
    assertEquals(0, d.getEntry(1), 1e-15);
    assertEquals(-3, d.getEntry(2), 1e-15);
  }
}
