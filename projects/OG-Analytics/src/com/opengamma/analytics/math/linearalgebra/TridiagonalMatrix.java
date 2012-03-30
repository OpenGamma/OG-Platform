/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Class representing a tridiagonal matrix:
 * $$
 * \begin{align*}
 * \begin{pmatrix}
 * a_1     & b_1     & 0       & \cdots  & 0       & 0       & 0        \\
 * c_1     & a_2     & b_2     & \cdots  & 0       & 0       & 0        \\
 * 0       &         & \ddots  &         & \vdots  & \vdots  & \vdots   \\
 * 0       & 0       & 0       &         & c_{n-2} & a_{n-1} & b_{n-1}  \\
 * 0       & 0       & 0       & \cdots  & 0       & c_{n-1} & a_n     
 * \end{pmatrix}
 * \end{align*}
 * $$
 */
public class TridiagonalMatrix {
  private final double[] _a;
  private final double[] _b;
  private final double[] _c;
  private DoubleMatrix2D _matrix;

  /**
   * @param a An array containing the diagonal values of the matrix, not null
   * @param b An array containing the upper sub-diagonal values of the matrix, not null. Its length must be one less than the length of the diagonal array
   * @param c An array containing the lower sub-diagonal values of the matrix, not null. Its length must be one less than the length of the diagonal array
   */
  public TridiagonalMatrix(final double[] a, final double[] b, final double[] c) {
    Validate.notNull(a, "a");
    Validate.notNull(b, "b");
    Validate.notNull(c, "c");
    final int n = a.length;
    Validate.isTrue(b.length == n - 1, "Length of subdiagonal b is incorrect");
    Validate.isTrue(c.length == n - 1, "Length of subdiagonal c is incorrect");
    _a = a;
    _b = b;
    _c = c;
  }

  /**
   * @return An array of the values of the diagonal
   */
  public double[] getDiagonal() {
    return _a;
  }

  /**
   * @return An array of the values of the upper sub-diagonal
   */
  public double[] getUpperSubDiagonal() {
    return _b;
  }

  /**
   * @return An array of the values of the lower sub-diagonal
   */
  public double[] getLowerSubDiagonal() {
    return _c;
  }

  /**
   * @return Returns the tridiagonal matrix as a {@link com.opengamma.analytics.math.matrix.DoubleMatrix2D}
   */
  public DoubleMatrix2D toDoubleMatrix2D() {
    if (_matrix == null) {
      calMatrix();
    }
    return _matrix;
  }

  private void calMatrix() {
    int n = _a.length;
    final double[][] data = new double[n][n];
    for (int i = 0; i < n; i++) {
      data[i][i] = _a[i];
    }
    for (int i = 1; i < n; i++) {
      data[i - 1][i] = _b[i - 1];
    }
    for (int i = 1; i < n; i++) {
      data[i][i - 1] = _c[i - 1];
    }
    _matrix = new DoubleMatrix2D(data);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_a);
    result = prime * result + Arrays.hashCode(_b);
    result = prime * result + Arrays.hashCode(_c);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TridiagonalMatrix other = (TridiagonalMatrix) obj;
    if (!Arrays.equals(_a, other._a)) {
      return false;
    }
    if (!Arrays.equals(_b, other._b)) {
      return false;
    }
    if (!Arrays.equals(_c, other._c)) {
      return false;
    }
    return true;
  }

}
