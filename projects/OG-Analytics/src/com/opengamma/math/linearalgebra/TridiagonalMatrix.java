/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
// TODO should be an instance of DoubleMatrix2D?
public class TridiagonalMatrix {
  private final double[] _a;
  private final double[] _b;
  private final double[] _c;
  private final DoubleMatrix2D _matrix;

  public TridiagonalMatrix(final double[] a, final double[] b, final double[] c) {
    Validate.notNull(a, "a");
    Validate.notNull(b, "b");
    Validate.notNull(c, "c");
    final int n = a.length;
    if (b.length != n - 1) {
      throw new IllegalArgumentException("Length of subdiagonal b is incorrect");
    }
    if (c.length != n - 1) {
      throw new IllegalArgumentException("Length of subdiagonal c is incorrect");
    }
    _a = a;
    _b = b;
    _c = c;
    int i;
    final double[][] data = new double[n][n];
    for (i = 0; i < n; i++) {
      data[i][i] = _a[i];
    }
    for (i = 1; i < n; i++) {
      data[i - 1][i] = _b[i - 1];
    }
    for (i = 1; i < n; i++) {
      data[i][i - 1] = _c[i - 1];
    }
    _matrix = new DoubleMatrix2D(data);
  }

  public double[] getDiagonal() {
    return _a;
  }

  public double[] getUpperSubDiagonal() {
    return _b;
  }

  public double[] getLowerSubDiagonal() {
    return _c;
  }

  public DoubleMatrix2D toDoubleMatrix2D() {
    return _matrix;
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
