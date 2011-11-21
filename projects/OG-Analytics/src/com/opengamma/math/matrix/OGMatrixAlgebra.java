/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

/**
 * An absolutely minimal implementation of matrix algebra - only various multiplications covered. For more advanced stuff (e.g. calculating the inverse) use {@link ColtMatrixAlgebra} or
 * {@link CommonsMatrixAlgebra}
 */
public class OGMatrixAlgebra extends MatrixAlgebra {

  /**
   * {@inheritDoc}
   * @throws NotImplementedException
   */
  @Override
  public double getCondition(final Matrix<?> m) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   * @throws NotImplementedException
   */
  @Override
  public double getDeterminant(final Matrix<?> m) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getInnerProduct(final Matrix<?> m1, final Matrix<?> m2) {
    Validate.notNull(m1, "m1");
    Validate.notNull(m2, "m2");
    if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix1D) {
      final double[] a = ((DoubleMatrix1D) m1).getData();
      final double[] b = ((DoubleMatrix1D) m2).getData();
      final int l = a.length;
      Validate.isTrue(l == b.length, "Matrix size mismacth");
      double sum = 0.0;
      for (int i = 0; i < l; i++) {
        sum += a[i] * b[i];
      }
      return sum;
    }
    throw new IllegalArgumentException("Can only find inner product of DoubleMatrix1D; have " + m1.getClass() + " and " + m2.getClass());
  }

  /**
   * {@inheritDoc}
   * @throws NotImplementedException
   */
  @Override
  public DoubleMatrix2D getInverse(final Matrix<?> m) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   * @throws NotImplementedException
   */
  @Override
  public double getNorm1(final Matrix<?> m) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   * This is only implemented for {@link DoubleMatrix1D}.
   * @throws IllegalArgumentException If the matrix is not a {@link DoubleMatrix1D}
   */
  @Override
  public double getNorm2(final Matrix<?> m) {
    Validate.notNull(m, "m");
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
    throw new IllegalArgumentException("Can only find norm2 of a DoubleMatrix1D; have " + m.getClass());
  }

  /**
   * {@inheritDoc}
   * @throws NotImplementedException
   */
  @Override
  public double getNormInfinity(final Matrix<?> m) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D getOuterProduct(final Matrix<?> m1, final Matrix<?> m2) {
    Validate.notNull(m1, "m1");
    Validate.notNull(m2, "m2");
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

  /**
   * {@inheritDoc}
   * @throws NotImplementedException
   */
  @Override
  public DoubleMatrix2D getPower(final Matrix<?> m, final int p) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getTrace(final Matrix<?> m) {
    Validate.notNull(m, "m");
    if (m instanceof DoubleMatrix2D) {
      final double[][] data = ((DoubleMatrix2D) m).getData();
      final int rows = data.length;
      Validate.isTrue(rows == data[0].length, "Matrix not square");
      double sum = 0.0;
      for (int i = 0; i < rows; i++) {
        sum += data[i][i];
      }
      return sum;
    }
    throw new IllegalArgumentException("Can only take the trace of DoubleMatrix2D; have " + m.getClass());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix2D getTranspose(final Matrix<?> m) {
    Validate.notNull(m, "m");
    if (m instanceof DoubleMatrix2D) {
      final double[][] data = ((DoubleMatrix2D) m).getData();
      final int rows = data.length;
      final int cols = data[0].length;
      final double[][] res = new double[cols][rows];
      int i, j;
      for (i = 0; i < cols; i++) {
        for (j = 0; j < rows; j++) {
          res[i][j] = data[j][i];
        }
      }
      return new DoubleMatrix2D(res);
    }
    throw new IllegalArgumentException("Can only take transpose of DoubleMatrix2D; have " + m.getClass());
  }

  /**
   * {@inheritDoc}
   * The following combinations of input matrices m1 and m2 are allowed:
   * <ul>
   * <li> m1 = 2-D matrix, m2 = 2-D matrix, returns {@latex.inline $\\mathbf{C} = \\mathbf{AB}$}
   * <li> m1 = 2-D matrix, m2 = 1-D matrix, returns {@latex.inline $\\mathbf{C} = \\mathbf{A}b$}
   * <li> m1 = 1-D matrix, m2 = 2-D matrix, returns {@latex.inline $\\mathbf{C} = a^T\\mathbf{B}$}
   * </ul>
   */
  @Override
  public Matrix<?> multiply(final Matrix<?> m1, final Matrix<?> m2) {
    Validate.notNull(m1, "m1");
    Validate.notNull(m2, "m2");
    if (m1 instanceof DoubleMatrix2D && m2 instanceof DoubleMatrix2D) {
      return multiply((DoubleMatrix2D) m1, (DoubleMatrix2D) m2);
    } else if (m1 instanceof DoubleMatrix2D && m2 instanceof DoubleMatrix1D) {
      return multiply((DoubleMatrix2D) m1, (DoubleMatrix1D) m2);
    } else if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix2D) {
      return multiply((DoubleMatrix1D) m1, (DoubleMatrix2D) m2);
    }
    throw new IllegalArgumentException("Can only multiply two DoubleMatrix2D; a DoubleMatrix2D and a DoubleMatrix1D; or a DoubleMatrix1D and a DoubleMatrix2D. have " + m1.getClass() + " and "
        + m2.getClass());
  }

  /**
   * {@inheritDoc}
   * @throws NotImplementedException
   */
  @Override
  public DoubleMatrix2D getPower(final Matrix<?> m, final double p) {
    throw new NotImplementedException();
  }

  private DoubleMatrix2D multiply(final DoubleMatrix2D m1, final DoubleMatrix2D m2) {
    final double[][] a = m1.getData();
    final double[][] b = m2.getData();
    final int p = b.length;
    Validate.isTrue(a[0].length == p, "Matrix size mismatch");
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

  private DoubleMatrix1D multiply(final DoubleMatrix2D matrix, final DoubleMatrix1D vector) {
    final double[][] a = matrix.getData();
    final double[] b = vector.getData();
    final int n = b.length;
    Validate.isTrue(a[0].length == n, "Matrix/vector size mismatch");
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

  private DoubleMatrix1D multiply(final DoubleMatrix1D vector, final DoubleMatrix2D matrix) {
    final double[] a = vector.getData();
    final double[][] b = matrix.getData();
    final int n = a.length;
    Validate.isTrue(b.length == n, "Matrix/vector size mismatch");
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
