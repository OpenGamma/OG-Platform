/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.MathException;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * Direct inversion of a tridiagonal matrix using the formula from
 * "R. Usmani, Inversion of a tridiagonal Jacobi matrix, Linear Algebra Appl. 212/213 (1994) 413-414."
 */
public class TridiagonalMatrixInvertor {

  /**
   * An N by N matrix is in tridiagonal form if the main diagonal and the diagonals immediately above and below the main diagonal have non-zero entries, while every other entry is zero 
   * @param a the main diagonal of length N
   * @param b the sub-diagonal above the main diagonal of length N-1 
   * @param c the sub-diagonal below the main diagonal of length N-1 
   * @return the inverse of the tridiagonal matrix 
   */
  public static DoubleMatrix2D getInverse(final double[] a, final double[] b, final double[] c) {
    if (a == null || b == null || c == null) {
      throw new IllegalArgumentException("some of the diagonals are null");
    }

    int n = a.length;
    if (b.length != n - 1 || c.length != n - 1) {
      throw new IllegalArgumentException("length of subdiagonals is wrong");
    }

    int i, j, k;
    double[] theta = new double[n + 1];
    double[] phi = new double[n];

    theta[0] = 1.0;
    theta[1] = a[0];
    for (i = 2; i <= n; i++) {
      theta[i] = a[i - 1] * theta[i - 1] - b[i - 2] * c[i - 2] * theta[i - 2];
    }

    if (theta[n] == 0.0) {
      throw new MathException("Zero determinate. Cannot invert the matrix");
    }

    phi[n - 1] = 1.0;
    phi[n - 2] = a[n - 1];
    for (i = n - 3; i >= 0; i--) {
      phi[i] = a[i + 1] * phi[i + 1] - b[i + 1] * c[i + 1] * phi[i + 2];
    }

    double product;
    double[][] res = new double[n][n];
    int sign = 1;
    for (j = 0; j < n; j++) {
      for (i = 0; i <= j; i++) {
        product = 1.0;
        for (k = i; k < j; k++) {
          product *= b[k];
        }
        res[i][j] = sign * product * theta[i] * phi[j] / theta[n];
        sign *= -1;
      }
      for (i = j + 1; i < n; i++) {
        product = 1.0;
        for (k = j; k < i; k++) {
          product *= c[k];
        }
        res[i][j] = sign * product * theta[j] * phi[i] / theta[n];
        sign *= -1;
      }
      sign *= -1;
    }

    return new DoubleMatrix2D(res);
  }

}
