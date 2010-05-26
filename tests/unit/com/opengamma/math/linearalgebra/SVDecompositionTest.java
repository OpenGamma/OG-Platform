/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.math.matrix.ColtMatrixAlgebra;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class SVDecompositionTest {

  private static final MatrixAlgebra ALGEBRA = new ColtMatrixAlgebra();
  private static final double EPS = 1e-10;

  private static final DoubleMatrix2D A = new DoubleMatrix2D(new double[][] { new double[] { 1, 2, 3 },
      new double[] { -3.4, -1, 4 }, new double[] { 1, 6, 1 } });
  private static final SVDecomposition SVD = new SVDecompositionCommons();

  @Test(expected = IllegalArgumentException.class)
  public void testNullObjectMatrix() {
    SVD.evaluate((DoubleMatrix2D) null);
  }

  @Test
  public void testRecoverOrginal() {

    final SVDecompositionResult svd_result = SVD.evaluate(A);
   
    final DoubleMatrix2D u = svd_result.getU();
    final double[] sv = svd_result.getSingularValues();
    final DoubleMatrix2D w = makeDiagonal(sv);
    final DoubleMatrix2D vt = svd_result.getVT();

    final DoubleMatrix2D a = (DoubleMatrix2D) ALGEBRA.multiply(ALGEBRA.multiply(u, w), vt);
    checkEquals(A, a);
  }

  @Test
  public void testInvert() {

    final DecompositionResult result = SVD.evaluate(A);
    assertTrue(result instanceof SVDecompositionResult);
    final SVDecompositionResult svd_result = (SVDecompositionResult) result;

    final DoubleMatrix2D ut = svd_result.getUT();
    final DoubleMatrix2D v = svd_result.getV();

    final double[] sv = svd_result.getSingularValues();
    final int n = sv.length;
    final double[] svinv = new double[n];
    for (int i = 0; i < n; i++) {
      if (sv[i] == 0.0)
        svinv[i] = 0.0;
      else
        svinv[i] = 1.0 / sv[i];
    }
    final DoubleMatrix2D winv = makeDiagonal(svinv);

    final DoubleMatrix2D ainv = (DoubleMatrix2D) ALGEBRA.multiply(ALGEBRA.multiply(v, winv), ut);

    final DoubleMatrix2D identity = (DoubleMatrix2D) ALGEBRA.multiply(A, ainv);
    checkIndentity(identity);

  }

  private DoubleMatrix2D makeDiagonal(final double[] x) {
    final int n = x.length;
    if (n == 0)
      return null;
    final double[][] data = new double[n][n];
    for (int i = 0; i < n; i++)
      data[i][i] = x[i];

    return new DoubleMatrix2D(data);

  }

  private void checkEquals(final DoubleMatrix2D x, final DoubleMatrix2D y) {
    if (x == null) {
      assertTrue(false);
      return;
    }
    final int n = x.getNumberOfRows();
    final int m = x.getNumberOfColumns();
    if (n != y.getNumberOfRows() || m != y.getNumberOfColumns()) {
      assertTrue(false);
      return;
    }
    for (int i = 0; i < n; i++)
      for (int j = 0; j < m; j++) {
        assertEquals(x.getEntry(i, j), y.getEntry(i, j), EPS);
      }
  }

  private void checkIndentity(final DoubleMatrix2D x) {
    if (x == null) {
      assertTrue(false);
      return;
    }

    final int n = x.getNumberOfRows();
    if (x.getNumberOfColumns() != n) {
      assertTrue(false);
      return;
    }
    for (int i = 0; i < n; i++)
      for (int j = 0; j < n; j++) {
        if (i == j)
          assertEquals(1.0, x.getEntry(i, i), EPS);
        else
          assertEquals(0.0, x.getEntry(i, j), EPS);

      }

  }
}
