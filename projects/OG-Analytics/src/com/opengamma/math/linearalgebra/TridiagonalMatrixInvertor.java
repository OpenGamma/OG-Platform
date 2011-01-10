/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import org.apache.commons.lang.Validate;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * Direct inversion of a tridiagonal matrix using the formula from
 * "R. Usmani, Inversion of a tridiagonal Jacobi matrix, Linear Algebra Appl. 212/213 (1994) 413-414."
 */
public class TridiagonalMatrixInvertor extends Function1D<TridiagonalMatrix, DoubleMatrix2D> {

  @Override
  public DoubleMatrix2D evaluate(TridiagonalMatrix x) {
    Validate.notNull(x);
    double[] a = x.getDiagonal();
    double[] b = x.getUpperSubDiagonal();
    double[] c = x.getLowerSubDiagonal();
    int n = a.length;
    int i, j, k;
    double[] theta = new double[n + 1];
    double[] phi = new double[n];

    theta[0] = 1.0;
    theta[1] = a[0];
    for (i = 2; i <= n; i++) {
      theta[i] = a[i - 1] * theta[i - 1] - b[i - 2] * c[i - 2] * theta[i - 2];
    }

    if (theta[n] == 0.0) {
      throw new MathException("Zero determinant. Cannot invert the matrix");
    }

    phi[n - 1] = 1.0;
    phi[n - 2] = a[n - 1];
    for (i = n - 3; i >= 0; i--) {
      phi[i] = a[i + 1] * phi[i + 1] - b[i + 1] * c[i + 1] * phi[i + 2];
    }

    double product;
    double[][] res = new double[n][n];

    for (j = 0; j < n; j++) {
      for (i = 0; i <= j; i++) {
        product = 1.0;
        for (k = i; k < j; k++) {
          product *= b[k];
        }
        res[i][j] = ((i + j) % 2 == 0 ? 1 : -1) * product * theta[i] * phi[j] / theta[n];
      }
      for (i = j + 1; i < n; i++) {
        product = 1.0;
        for (k = j; k < i; k++) {
          product *= c[k];
        }
        res[i][j] = ((i + j) % 2 == 0 ? 1 : -1) * product * theta[j] * phi[i] / theta[n];
      }
    }

    return new DoubleMatrix2D(res);
  }

}
