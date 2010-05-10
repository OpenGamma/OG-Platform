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
    final double[][] primitive = d.getDataAsPrimitiveArray();
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
    final double[][] y = d.getDataAsPrimitiveArray();
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
    x = d.getDataAsPrimitiveArray();
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        assertEquals(x[i][j], y[i][j], 1e-15);
      }
    }
  }
}
