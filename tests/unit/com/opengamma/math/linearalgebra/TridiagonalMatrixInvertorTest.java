/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import static com.opengamma.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class TridiagonalMatrixInvertorTest {
  private static final TridiagonalMatrixInvertor INVERTOR = new TridiagonalMatrixInvertor();
  private static final double[] A = new double[] {1.0, 2.4, -0.4, -0.8, 1.5, 7.8, -5.0};
  private static final double[] B = new double[] {1.56, 0.33, 0.42, -0.23, 0.276, 4.76};
  private static final double[] C = new double[] {0.56, 0.63, -0.42, -0.23, 0.76, 1.76};
  private static final TridiagonalMatrix MATRIX = new TridiagonalMatrix(A, B, C);
  private static final double EPS = 1e-15;
  DoubleMatrix2D tri;

  public TridiagonalMatrixInvertorTest() {
    int n = A.length;
    int i;
    double[][] data = new double[n][n];
    for (i = 0; i < n; i++) {
      data[i][i] = A[i];
    }
    for (i = 1; i < n; i++) {
      data[i - 1][i] = B[i - 1];
    }
    for (i = 1; i < n; i++) {
      data[i][i - 1] = C[i - 1];
    }

    tri = new DoubleMatrix2D(data);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArray() {
    INVERTOR.evaluate((TridiagonalMatrix) null);
  }

  @Test
  public void TestInvertIdentity() {
    int n = 10;
    double[] a = new double[n];
    double[] b = new double[n - 1];
    double[] c = new double[n - 1];
    int i, j;

    for (i = 0; i < n; i++) {
      a[i] = 1.0;
    }
    DoubleMatrix2D res = INVERTOR.evaluate(new TridiagonalMatrix(a, b, c));
    for (i = 0; i < n; i++) {
      for (j = 0; j < i; j++) {
        assertEquals((i == j ? 1.0 : 0.0), res.getEntry(i, j), EPS);
      }
    }

  }

  @Test
  public void TestInvert() {
    DoubleMatrix2D res = INVERTOR.evaluate(MATRIX);
    DoubleMatrix2D idet = (DoubleMatrix2D) OG_ALGEBRA.multiply(tri, res);

    int n = idet.getNumberOfRows();
    int i, j;
    for (i = 0; i < n; i++) {
      for (j = 0; j < i; j++) {
        assertEquals((i == j ? 1.0 : 0.0), idet.getEntry(i, j), EPS);
      }
    }

  }

}
