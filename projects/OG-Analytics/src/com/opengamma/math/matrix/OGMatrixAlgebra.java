/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.apache.commons.lang.NotImplementedException;

/**
 * An absolutely minimal implementation of matrix algebra - only various multiplications covered. For more advanced stuff (e.g. calculating the inverse) use {@link ColtMatrixAlgebra} or
 * {@link CommonsMatrixAlgebra}
 */
public class OGMatrixAlgebra extends MatrixAlgebra {

  @Override
  public double getCondition(final Matrix<?> m) {
    throw new NotImplementedException();
  }

  @Override
  public double getDeterminant(final Matrix<?> m) {
    throw new NotImplementedException();
  }

  @Override
  public double getInnerProduct(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix1D) {
      final double[] a = ((DoubleMatrix1D) m1).getData();
      final double[] b = ((DoubleMatrix1D) m2).getData();
      final int l = a.length;
      if (b.length != l) {
        throw new IllegalArgumentException("Matrix size mismatch");
      }
      double sum = 0.0;
      for (int i = 0; i < l; i++) {
        sum += a[i] * b[i];
      }
      return sum;
    }
    throw new IllegalArgumentException("Can only find inner product of DoubleMatrix1D; have " + m1.getClass() + " and " + m2.getClass());
  }

  @Override
  public DoubleMatrix2D getInverse(final Matrix<?> m) {
    throw new NotImplementedException();
  }

  @Override
  public double getNorm1(final Matrix<?> m) {
    throw new NotImplementedException();
  }

  @Override
  public double getNorm2(final Matrix<?> m) {
    if (m instanceof DoubleMatrix1D) {
      final double[] a = ((DoubleMatrix1D) m).getData();
      final int l = a.length;
      double sum = 0.0;
      for (int i = 0; i < l; i++) {
        sum += a[i] * a[i];
      }
      return Math.sqrt(sum);
    } else if (m instanceof DoubleMatrix2D) {
      throw new NotImplementedException();
    }
    throw new IllegalArgumentException("Can only find  Norm2 of a DoubleMatrix1D; have " + m.getClass());
  }

  @Override
  public double getNormInfinity(final Matrix<?> m) {
    throw new NotImplementedException();
  }

  @Override
  public DoubleMatrix2D getOuterProduct(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix1D) {
      final double[] a = ((DoubleMatrix1D) m1).getData();
      final double[] b = ((DoubleMatrix1D) m2).getData();
      final int m = a.length;
      final int n = b.length;
      final double[][] res = new double[m][n];
      int i, j;
      for (i = 0; i < m; i++) {
        for (j = 0; j < n; j++) {
          res[i][j] = a[i] * b[j];
        }
      }
      return new DoubleMatrix2D(res);
    }
    throw new IllegalArgumentException("Can only find outer product of DoubleMatrix1D; have " + m1.getClass() + " and " + m2.getClass());
  }

  @Override
  public DoubleMatrix2D getPower(final Matrix<?> m, final int p) {
    throw new NotImplementedException();
  }

  @Override
  public double getTrace(final Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      final double[][] data = ((DoubleMatrix2D) m).getData();
      final int rows = data.length;
      if (rows != data[0].length) {
        throw new IllegalArgumentException("Matrix not square");
      }
      double sum = 0.0;
      for (int i = 0; i < rows; i++) {
        sum += data[i][i];
      }
      return sum;
    }
    throw new IllegalArgumentException("Can only take transpose of DoubleMatrix2D; Have " + m.getClass());
  }

  @Override
  public DoubleMatrix2D getTranspose(final Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      final double[][] data = ((DoubleMatrix2D) m).getData();
      final int rows = data.length;
      final int cols = data[0].length;
      final double[][] res = new double[cols][rows];
      int i, j;
      for (i = 0; i < rows; i++) {
        for (j = 0; j < cols; j++) {
          res[i][j] = data[j][i];
        }
      }
      return new DoubleMatrix2D(res);
    }
    throw new IllegalArgumentException("Can only take transpose of DoubleMatrix2D; Have " + m.getClass());
  }

  @Override
  public Matrix<?> multiply(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix2D && m2 instanceof DoubleMatrix2D) {
      return multiply((DoubleMatrix2D) m1, (DoubleMatrix2D) m2);
    } else if (m1 instanceof DoubleMatrix2D && m2 instanceof DoubleMatrix1D) {
      return multiply((DoubleMatrix2D) m1, (DoubleMatrix1D) m2);
    } else if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix2D) {
      return multiply((DoubleMatrix1D) m1, (DoubleMatrix2D) m2);
    }
    throw new IllegalArgumentException("Can only multiply two DoubleMatrix2D; a DoubleMatrix2D and a DoubleMatrix1D; or a DoubleMatrix1D and a DoubleMatrix2D. Have " + m1.getClass() + " and "
        + m2.getClass());
  }

  @Override
  public DoubleMatrix2D getPower(final Matrix<?> m, final double p) {
    throw new NotImplementedException();
  }

  private DoubleMatrix2D multiply(final DoubleMatrix2D m1, final DoubleMatrix2D m2) {
    final double[][] a = m1.getData();
    final double[][] b = m2.getData();
    final int p = b.length;
    if (a[0].length != p) {
      throw new IllegalArgumentException("Matrix size mismatch");
    }
    final int m = a.length;
    final int n = b[0].length;
    double sum;
    final double[][] res = new double[m][n];
    int i, j, k;
    for (i = 0; i < m; i++) {
      for (j = 0; j < n; j++) {
        sum = 0.0;
        for (k = 0; k < p; k++) {
          sum += a[i][k] * b[k][j];
        }
        res[i][j] = sum;
      }
    }
    return new DoubleMatrix2D(res);
  }

  /**
   * A matrix M times a column vector v, i.e M*v
   * @param matrix A matrix
   * @param vector A column vector
   * @return A column vector
   */
  private DoubleMatrix1D multiply(final DoubleMatrix2D matrix, final DoubleMatrix1D vector) {
    final double[][] a = matrix.getData();
    final double[] b = vector.getData();
    final int n = b.length;
    if (a[0].length != n) {
      throw new IllegalArgumentException("Matrix/vector size mismatch");
    }
    final int m = a.length;
    final double[] res = new double[m];
    int i, j;
    double sum;
    for (i = 0; i < m; i++) {
      sum = 0.0;
      for (j = 0; j < n; j++) {
        sum += a[i][j] * b[j];
      }
      res[i] = sum;
    }
    return new DoubleMatrix1D(res);
  }

  /**
   * A row vector times a matrix i.e v<sup>T</sup>*M
   * @param vector A row vector
   * @param matrix A matrix
   * @return A row vector
   */
  private DoubleMatrix1D multiply(final DoubleMatrix1D vector, final DoubleMatrix2D matrix) {
    final double[] a = vector.getData();
    final double[][] b = matrix.getData();
    final int n = a.length;
    if (b.length != n) {
      throw new IllegalArgumentException("Matrix/vector size mismatch");
    }
    final int m = b[0].length;
    final double[] res = new double[m];
    int i, j;
    double sum;
    for (i = 0; i < m; i++) {
      sum = 0.0;
      for (j = 0; j < n; j++) {
        sum += a[j] * b[j][i];
      }
      res[i] = sum;
    }
    return new DoubleMatrix1D(res);
  }
}
