/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class DoubleMatrix1DTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNullPrimitiveArray() {
    new DoubleMatrix1D((double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullObjectArray() {
    new DoubleMatrix1D((Double[]) null);
  }

  @Test
  public void testEmptyArray() {
    final DoubleMatrix1D d = new DoubleMatrix1D(new double[0]);
    assertTrue(Arrays.equals(new double[0], d.getData()));
  }

  @Test
  public void testArrays() {
    final int n = 10;
    double[] x = new double[n];
    for (int i = 0; i < n; i++) {
      x[i] = i;
    }
    DoubleMatrix1D d = new DoubleMatrix1D(x);
    assertEquals(d.getNumberOfElements(), n);
    final double[] y = d.getData();
    for (int i = 0; i < n; i++) {
      assertEquals(x[i], y[i], 1e-15);
    }
    for (int i = 0; i < n; i++) {
      y[i] = Double.valueOf(i);
    }
    d = new DoubleMatrix1D(y);
    x = d.getData();
    for (int i = 0; i < n; i++) {
      assertEquals(x[i], y[i], 1e-15);
    }
  }

  @Test
  public void testDotProduct() {
    final DoubleMatrix1D A = new DoubleMatrix1D(new double[] { -1, 2, 3 });
    final DoubleMatrix1D B = new DoubleMatrix1D(new double[] { 2, -2, 1 });
    double res = DoubleMatrix1D.dotProduct(A, B);
    assertEquals(-3.0, res, 1e-15);
    res = A.dotProduct(A);
    assertEquals(14.0, res, 1e-15);
  }
}
