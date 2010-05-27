/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.apache.commons.lang.NotImplementedException;

/**
 * An absolutely minimal implementation of matrix algebra - only various multiplications covered. For more advanced stuff (e.g. {@link getInverse}) use {@link ColtMatrixAlgebra} or 
 * {@link CommonsMatrixAlgebra} 
 */
public class OGMatrixAlgebra extends MatrixAlgebra {

  /* (non-Javadoc)
   * @see com.opengamma.math.matrix.MatrixAlgebra#getCondition(com.opengamma.math.matrix.Matrix)
   */
  @Override
  public double getCondition(Matrix<?> m) {
    throw new NotImplementedException();
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.matrix.MatrixAlgebra#getDeterminant(com.opengamma.math.matrix.Matrix)
   */
  @Override
  public double getDeterminant(Matrix<?> m) {
    throw new NotImplementedException();
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.matrix.MatrixAlgebra#getInnerProduct(com.opengamma.math.matrix.Matrix, com.opengamma.math.matrix.Matrix)
   */
  @Override
  public double getInnerProduct(Matrix<?> m1, Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix1D) {
      double[] a = ((DoubleMatrix1D) m1).getData();
      double[] b = ((DoubleMatrix1D) m2).getData();
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
    throw new IllegalArgumentException("Can only find inner product of DoubleMatrix1D; have " + m1.getClass() + " and "
        + m2.getClass());
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.matrix.MatrixAlgebra#getInverse(com.opengamma.math.matrix.Matrix)
   */
  @Override
  public DoubleMatrix2D getInverse(Matrix<?> m) {
    throw new NotImplementedException();
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.matrix.MatrixAlgebra#getNorm1(com.opengamma.math.matrix.Matrix)
   */
  @Override
  public double getNorm1(Matrix<?> m) {
    throw new NotImplementedException();
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.matrix.MatrixAlgebra#getNorm2(com.opengamma.math.matrix.Matrix)
   */
  @Override
  public double getNorm2(Matrix<?> m) {
    if (m instanceof DoubleMatrix1D) {
      double[] a = ((DoubleMatrix1D) m).getData();
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

  /* (non-Javadoc)
   * @see com.opengamma.math.matrix.MatrixAlgebra#getNormInfinity(com.opengamma.math.matrix.Matrix)
   */
  @Override
  public double getNormInfinity(Matrix<?> m) {
    throw new NotImplementedException();
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.matrix.MatrixAlgebra#getOuterProduct(com.opengamma.math.matrix.Matrix, com.opengamma.math.matrix.Matrix)
   */
  @Override
  public DoubleMatrix2D getOuterProduct(Matrix<?> m1, Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix1D) {
      double[] a = ((DoubleMatrix1D) m1).getData();
      double[] b = ((DoubleMatrix1D) m2).getData();
      int m = a.length;
      int n = b.length;
      double[][] res = new double[m][n];
      int i, j;
      for (i = 0; i < m; i++) {
        for (j = 0; j < n; j++) {
          res[i][j] = a[i] * b[j];
        }
      }
      return new DoubleMatrix2D(res);
    }
    throw new IllegalArgumentException("Can only find outer product of DoubleMatrix1D; have " + m1.getClass() + " and "
        + m2.getClass());
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.matrix.MatrixAlgebra#getPower(com.opengamma.math.matrix.Matrix, int)
   */
  @Override
  public DoubleMatrix2D getPower(Matrix<?> m, int p) {
    throw new NotImplementedException();
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.matrix.MatrixAlgebra#getTrace(com.opengamma.math.matrix.Matrix)
   */
  @Override
  public double getTrace(Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      double[][] data = ((DoubleMatrix2D) m).getData();
      int rows = data.length;
      if (rows != data[0].length) {
        throw new IllegalArgumentException("Matrix not square");
      }
      double sum = 0.0;
      for (int i = 0; i < rows; i++) {
        sum += data[i][i];
      }
      return sum;
    }
    throw new IllegalArgumentException("Can only take transpose of DoubleMatrix2D ÊHave " + m.getClass());
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.matrix.MatrixAlgebra#getTranspose(com.opengamma.math.matrix.Matrix)
   */
  @Override
  public DoubleMatrix2D getTranspose(Matrix<?> m) {
    if (m instanceof DoubleMatrix2D) {
      double[][] data = ((DoubleMatrix2D) m).getData();
      int rows = data.length;
      int cols = data[0].length;
      final double[][] res = new double[cols][rows];
      int i, j;
      for (i = 0; i < rows; i++) {
        for (j = 0; j < cols; j++) {
          res[i][j] = data[j][i];
        }
      }
      return new DoubleMatrix2D(res);
    }
    throw new IllegalArgumentException("Can only take transpose of DoubleMatrix2D ÊHave " + m.getClass());
  }

  public DoubleMatrix2D multiply(DoubleMatrix2D m1, DoubleMatrix2D m2) {
    double[][] a = m1.getData();
    double[][] b = m2.getData();
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
  public DoubleMatrix1D multiply(DoubleMatrix2D matrix, DoubleMatrix1D vector) {
    double[][] a = matrix.getData();
    double[] b = vector.getData();
    int n = b.length;
    if (a[0].length != n) {
      throw new IllegalArgumentException("Matrix/vector size mismatch");
    }
    int m = a.length;
    double[] res = new double[m];
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
  public DoubleMatrix1D multiply(DoubleMatrix1D vector, DoubleMatrix2D matrix) {
    double[] a = vector.getData();
    double[][] b = matrix.getData();
    int n = a.length;
    if (b.length != n) {
      throw new IllegalArgumentException("Matrix/vector size mismatch");
    }
    int m = b[0].length;
    double[] res = new double[m];
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

  /* (non-Javadoc)
   * @see com.opengamma.math.matrix.MatrixAlgebra#multiply(com.opengamma.math.matrix.Matrix, com.opengamma.math.matrix.Matrix)
   */
  @Override
  public Matrix<?> multiply(Matrix<?> m1, Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix2D && m2 instanceof DoubleMatrix2D) {
      return multiply((DoubleMatrix2D) m1, (DoubleMatrix2D) m2);
    } else if (m1 instanceof DoubleMatrix2D && m2 instanceof DoubleMatrix1D) {
      return multiply((DoubleMatrix2D) m1, (DoubleMatrix1D) m2);
    } else if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix2D) {
      return multiply((DoubleMatrix1D) m1, (DoubleMatrix2D) m2);
    }
    throw new IllegalArgumentException(
        "Can only multiply two DoubleMatrix2D; a DoubleMatrix2D and a DoubleMatrix1D; or a DoubleMatrix1D and a DoubleMatrix2D.ÊHave "
            + m1.getClass() + " and " + m2.getClass());
  }
}
