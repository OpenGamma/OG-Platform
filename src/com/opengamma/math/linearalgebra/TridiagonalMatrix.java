/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * 
 */
//TODO extends DoubleMatrix2D
public class TridiagonalMatrix {
  private final double[] _a;
  private final double[] _b;
  private final double[] _c;

  public TridiagonalMatrix(double[] a, double[] b, double[] c) {
    Validate.notNull(a, "a");
    Validate.notNull(b, "b");
    Validate.notNull(c, "c");
    int n = a.length;
    if (b.length != n - 1) {
      throw new IllegalArgumentException("Length of subdiagonal b is incorrect");
    }
    if (c.length != n - 1) {
      throw new IllegalArgumentException("Length of subdiagonal c is incorrect");
    }
    _a = a;
    _b = b;
    _c = c;
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TridiagonalMatrix other = (TridiagonalMatrix) obj;
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
