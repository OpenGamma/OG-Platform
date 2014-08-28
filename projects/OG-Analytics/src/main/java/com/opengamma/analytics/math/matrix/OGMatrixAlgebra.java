/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.matrix;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.linearalgebra.TridiagonalMatrix;
import com.opengamma.util.ArgumentChecker;

/**
 * An absolutely minimal implementation of matrix algebra - only various multiplications covered. For more advanced
 * stuff (e.g. calculating the inverse) use {@link ColtMatrixAlgebra} or {@link CommonsMatrixAlgebra}
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
    throw new IllegalArgumentException("Can only find inner product of DoubleMatrix1D; have " + m1.getClass() +
        " and " + m2.getClass());
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
   * {@inheritDoc} This is only implemented for {@link DoubleMatrix1D}.
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
    throw new IllegalArgumentException("Can only find outer product of DoubleMatrix1D; have " + m1.getClass() +
        " and " + m2.getClass());
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
    if (m instanceof IdentityMatrix) {
      return (IdentityMatrix) m;
    }
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
   * {@inheritDoc} The following combinations of input matrices m1 and m2 are allowed:
   * <ul>
   * <li>m1 = 2-D matrix, m2 = 2-D matrix, returns $\mathbf{C} = \mathbf{AB}$
   * <li>m1 = 2-D matrix, m2 = 1-D matrix, returns $\mathbf{C} = \mathbf{A}b$
   * <li>m1 = 1-D matrix, m2 = 2-D matrix, returns $\mathbf{C} = a^T\mathbf{B}$
   * </ul>
   */
  @Override
  public Matrix<?> multiply(final Matrix<?> m1, final Matrix<?> m2) {
    Validate.notNull(m1, "m1");
    Validate.notNull(m2, "m2");
    if (m1 instanceof IdentityMatrix) {
      if (m2 instanceof IdentityMatrix) {
        return multiply((IdentityMatrix) m1, (IdentityMatrix) m2);
      } else if (m2 instanceof DoubleMatrix1D) {
        return multiply((IdentityMatrix) m1, (DoubleMatrix1D) m2);
      } else if (m2 instanceof DoubleMatrix2D) {
        return multiply((IdentityMatrix) m1, (DoubleMatrix2D) m2);
      }
      throw new IllegalArgumentException("can only handle identity by DoubleMatrix2D or DoubleMatrix1D, have " +
          m1.getClass() + " and " + m2.getClass());
    }
    if (m2 instanceof IdentityMatrix) {
      if (m1 instanceof DoubleMatrix1D) {
        return multiply((DoubleMatrix1D) m1, (IdentityMatrix) m2);
      } else if (m1 instanceof DoubleMatrix2D) {
        return multiply((DoubleMatrix2D) m1, (IdentityMatrix) m2);
      }
      throw new IllegalArgumentException("can only handle identity by DoubleMatrix2D or DoubleMatrix1D, have " +
          m1.getClass() + " and " + m2.getClass());
    }
    if (m1 instanceof TridiagonalMatrix && m2 instanceof DoubleMatrix1D) {
      return multiply((TridiagonalMatrix) m1, (DoubleMatrix1D) m2);
    } else if (m1 instanceof DoubleMatrix1D && m2 instanceof TridiagonalMatrix) {
      return multiply((DoubleMatrix1D) m1, (TridiagonalMatrix) m2);
    } else if (m1 instanceof DoubleMatrix2D && m2 instanceof DoubleMatrix2D) {
      return multiply((DoubleMatrix2D) m1, (DoubleMatrix2D) m2);
    } else if (m1 instanceof DoubleMatrix2D && m2 instanceof DoubleMatrix1D) {
      return multiply((DoubleMatrix2D) m1, (DoubleMatrix1D) m2);
    } else if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix2D) {
      return multiply((DoubleMatrix1D) m1, (DoubleMatrix2D) m2);
    }
    throw new IllegalArgumentException(
        "Can only multiply two DoubleMatrix2D; a DoubleMatrix2D and a DoubleMatrix1D; or a DoubleMatrix1D and a DoubleMatrix2D. have " +
            m1.getClass() + " and " + m2.getClass());
  }

  /**
   * {@inheritDoc}
   * @throws NotImplementedException
   */
  @Override
  public DoubleMatrix2D getPower(final Matrix<?> m, final double p) {
    throw new NotImplementedException();
  }

  private DoubleMatrix2D multiply(final IdentityMatrix idet, final DoubleMatrix2D m) {
    ArgumentChecker.isTrue(idet.getSize() == m.getNumberOfRows(),
        "size of identity matrix ({}) does not match number or rows of m ({})", idet.getSize(), m.getNumberOfRows());
    return m;
  }

  private DoubleMatrix2D multiply(final DoubleMatrix2D m, final IdentityMatrix idet) {
    ArgumentChecker.isTrue(idet.getSize() == m.getNumberOfColumns(),
        "size of identity matrix ({}) does not match number or columns of m ({})", idet.getSize(),
        m.getNumberOfColumns());
    return m;
  }

  private IdentityMatrix multiply(final IdentityMatrix i1, final IdentityMatrix i2) {
    ArgumentChecker.isTrue(i1.getSize() == i2.getSize(),
        "size of identity matrix 1 ({}) does not match size of identity matrix 2 ({})", i1.getSize(), i2.getSize());
    return i1;
  }

  private DoubleMatrix2D multiply(final DoubleMatrix2D m1, final DoubleMatrix2D m2) {
    final double[][] a = m1.getData();
    final double[][] b = m2.getData();
    final int p = b.length;
    Validate.isTrue(
        a[0].length == p,
        "Matrix size mismatch. m1 is " + m1.getNumberOfRows() + " by " + m1.getNumberOfColumns() + ", but m2 is " +
            m2.getNumberOfRows() + " by " + m2.getNumberOfColumns());
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

  private DoubleMatrix1D multiply(final IdentityMatrix matrix, final DoubleMatrix1D vector) {
    ArgumentChecker.isTrue(matrix.getSize() == vector.getNumberOfElements(),
        "size of identity matrix ({}) does not match size of vector ({})", matrix.getSize(),
        vector.getNumberOfElements());
    return vector;
  }

  private DoubleMatrix1D multiply(final DoubleMatrix1D vector, final IdentityMatrix matrix) {
    ArgumentChecker.isTrue(matrix.getSize() == vector.getNumberOfElements(),
        "size of identity matrix ({}) does not match size of vector ({})", matrix.getSize(),
        vector.getNumberOfElements());
    return vector;
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

  private DoubleMatrix1D multiply(final TridiagonalMatrix matrix, final DoubleMatrix1D vector) {
    final double[] a = matrix.getLowerSubDiagonalData();
    final double[] b = matrix.getDiagonalData();
    final double[] c = matrix.getUpperSubDiagonalData();
    final double[] x = vector.getData();
    final int n = x.length;
    Validate.isTrue(b.length == n, "Matrix/vector size mismatch");
    final double[] res = new double[n];
    int i;
    res[0] = b[0] * x[0] + c[0] * x[1];
    res[n - 1] = b[n - 1] * x[n - 1] + a[n - 2] * x[n - 2];
    for (i = 1; i < n - 1; i++) {
      res[i] = a[i - 1] * x[i - 1] + b[i] * x[i] + c[i] * x[i + 1];
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

  private DoubleMatrix1D multiply(final DoubleMatrix1D vector, final TridiagonalMatrix matrix) {
    final double[] a = matrix.getLowerSubDiagonalData();
    final double[] b = matrix.getDiagonalData();
    final double[] c = matrix.getUpperSubDiagonalData();
    final double[] x = vector.getData();
    final int n = x.length;
    Validate.isTrue(b.length == n, "Matrix/vector size mismatch");
    final double[] res = new double[n];
    int i;
    res[0] = b[0] * x[0] + a[0] * x[1];
    res[n - 1] = b[n - 1] * x[n - 1] + c[n - 2] * x[n - 2];
    for (i = 1; i < n - 1; i++) {
      res[i] = a[i] * x[i + 1] + b[i] * x[i] + c[i - 1] * x[i - 1];
    }
    return new DoubleMatrix1D(res);
  }

}
