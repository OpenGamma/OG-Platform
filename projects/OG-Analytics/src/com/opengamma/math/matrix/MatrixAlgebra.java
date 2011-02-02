/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.apache.commons.lang.NotImplementedException;

/**
 * Abstract class for Matrix Algebra. Basic stuff (add, subtract, scale) is implemented here, everything else should be overridden in concrete sub classes.
 * @see CommonsMatrixAlgebra
 * @see ColtMatrixAlgebra
 * @see OGMatrixAlgebra
 */
public abstract class MatrixAlgebra {

  public Matrix<?> add(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix1D) {
      if (m2 instanceof DoubleMatrix1D) {
        final double[] x1 = ((DoubleMatrix1D) m1).getData();
        final double[] x2 = ((DoubleMatrix1D) m2).getData();
        final int n = x1.length;
        if (n != x2.length) {
          throw new IllegalArgumentException("Can only add matrices of the same shape");
        }
        final double[] sum = new double[n];
        for (int i = 0; i < n; i++) {
          sum[i] = x1[i] + x2[i];
        }
        return new DoubleMatrix1D(sum);
      }
      throw new IllegalArgumentException("Tried to add a " + m1.getClass() + " and " + m2.getClass());
    } else if (m1 instanceof DoubleMatrix2D) {
      if (m2 instanceof DoubleMatrix2D) {

        final double[][] x1 = ((DoubleMatrix2D) m1).getData();
        final double[][] x2 = ((DoubleMatrix2D) m2).getData();
        final int n = x1.length;
        final int m = x1[0].length;
        if (n != x2.length) {
          throw new IllegalArgumentException("Can only add matrices of the same shape");
        }
        final double[][] sum = new double[n][x1[0].length];
        for (int i = 0; i < n; i++) {
          if (x2[i].length != m) {
            throw new IllegalArgumentException("Can only add matrices of the same shape");
          }
          for (int j = 0; j < m; j++) {
            sum[i][j] = x1[i][j] + x2[i][j];
          }
        }
        return new DoubleMatrix2D(sum);
      }
      throw new IllegalArgumentException("Tried to add a " + m1.getClass() + " and " + m2.getClass());
    }
    throw new NotImplementedException();
  }

  /**
   * Returns the division of two matrices C = A/B = A*B<sup>-1</sup> where B<sup>-1</sup> is the pseudo inverse of B , i.e. B*B<sup>-1</sup> = <b>1</b>
   * @param m1 Matrix A
   * @param m2 Matrix B
   * @return The matrix result 
   */
  public Matrix<?> divide(final Matrix<?> m1, final Matrix<?> m2) {
    if (!(m1 instanceof DoubleMatrix2D)) {
      throw new IllegalArgumentException("Can only divide a 2D matrix");
    }
    if (!(m2 instanceof DoubleMatrix2D)) {
      throw new IllegalArgumentException("Can only perform division with a 2D matrix");
    }
    return multiply(m1, getInverse(m2));
  }

  public Matrix<?> kroneckerProduct(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix2D && m2 instanceof DoubleMatrix2D) {
      final double[][] a = ((DoubleMatrix2D) m1).getData();
      final double[][] b = ((DoubleMatrix2D) m2).getData();
      int aRows = a.length;
      int aCols = a[0].length;
      int bRows = b.length;
      int bCols = b[0].length;
      int rRows = aRows * bRows;
      int rCols = aCols * bCols;
      double[][] res = new double[rRows][rCols];
      for (int i = 0; i < aRows; i++) {
        for (int j = 0; j < aCols; j++) {
          double t = a[i][j];
          if (t != 0.0) {
            for (int k = 0; k < bRows; k++) {
              for (int l = 0; l < bCols; l++) {
                res[i * bRows + k][j * bCols + l] = t * b[k][l];
              }
            }
          }
        }
      }
      return new DoubleMatrix2D(res);
    }
    throw new IllegalArgumentException("Can only multiply two DoubleMatrix2D. Have " + m1.getClass() + " and " + m2.getClass());
  }

  public abstract Matrix<?> multiply(final Matrix<?> m1, final Matrix<?> m2);

  /**
   * Scale a vector or matrix by a given amount, i.e. each element is multiplied by the scale 
   * @param m Some vector or matrix
   * @param scale 
   * @return the scaled vector or matrix 
   */
  public Matrix<?> scale(final Matrix<?> m, final double scale) {
    if (m instanceof DoubleMatrix1D) {
      final double[] x = ((DoubleMatrix1D) m).getData();
      final int n = x.length;
      final double[] scaled = new double[n];
      for (int i = 0; i < n; i++) {
        scaled[i] = x[i] * scale;
      }
      return new DoubleMatrix1D(scaled);
    } else if (m instanceof DoubleMatrix2D) {
      final double[][] x = ((DoubleMatrix2D) m).getData();
      final int n = x.length;
      final double[][] scaled = new double[n][x[0].length];
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < x[0].length; j++) {
          scaled[i][j] = x[i][j] * scale;
        }
      }
      return new DoubleMatrix2D(scaled);
    }
    throw new NotImplementedException();
  }

  public Matrix<?> subtract(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix1D) {
      if (m2 instanceof DoubleMatrix1D) {
        final double[] x1 = ((DoubleMatrix1D) m1).getData();
        final double[] x2 = ((DoubleMatrix1D) m2).getData();
        final int n = x1.length;
        if (n != x2.length) {
          throw new IllegalArgumentException("Can only subtract matrices of the same shape");
        }
        final double[] sum = new double[n];
        for (int i = 0; i < n; i++) {
          sum[i] = x1[i] - x2[i];
        }
        return new DoubleMatrix1D(sum);
      }
      throw new IllegalArgumentException("Tried to subtract a " + m1.getClass() + " and " + m2.getClass());
    } else if (m1 instanceof DoubleMatrix2D) {
      if (m2 instanceof DoubleMatrix2D) {

        final double[][] x1 = ((DoubleMatrix2D) m1).getData();
        final double[][] x2 = ((DoubleMatrix2D) m2).getData();
        final int n = x1.length;
        final int m = x1[0].length;
        if (n != x2.length) {
          throw new IllegalArgumentException("Can only subtract matrices of the same shape");
        }
        final double[][] sum = new double[n][x1[0].length];
        for (int i = 0; i < n; i++) {
          if (x2[i].length != m) {
            throw new IllegalArgumentException("Can only subtract matrices of the same shape");
          }
          for (int j = 0; j < m; j++) {
            sum[i][j] = x1[i][j] - x2[i][j];
          }
        }
        return new DoubleMatrix2D(sum);
      }
      throw new IllegalArgumentException("Tried to subtract a " + m1.getClass() + " and " + m2.getClass());
    }
    throw new NotImplementedException();
  }

  /**
   * Return the condition number of the matrix.
   * @param m A matrix 
   * @return condition number of the matrix
   */
  public abstract double getCondition(final Matrix<?> m);

  /**
   * Return the determinant of the matrix
   * @param m A matrix 
   * @return determinant of the matrix
   */
  public abstract double getDeterminant(final Matrix<?> m);

  /** Get the inverse (or pseudo-inverse) of the decomposed matrix.
   * @param m A matrix
   * @return inverse matrix
   */
  public abstract DoubleMatrix2D getInverse(final Matrix<?> m);

  /**
   * Compute the inner (or dot) product.
   * @param m1 vector
   * @param m2 vector
   * @return the scalar dot product between m1 & m2
   * @exception IllegalArgumentException vectors not the same size
   */
  public abstract double getInnerProduct(final Matrix<?> m1, final Matrix<?> m2);

  /**
   * Compute the outer product.
   * @param m1 vector
   * @param m2 vector
   * @return the matrix return of the outer product 
   * @exception IllegalArgumentException vectors not the same size
   */
  public abstract DoubleMatrix2D getOuterProduct(final Matrix<?> m1, final Matrix<?> m2);

  /**
   * For a vector returns the <a href = "http://mathworld.wolfram.com/L1-Norm.html"> L<sub>1</sub> norm</a> (also known as Taxicab norm or Manhattan norm), i.e. sum(abs(x<sub>i</sub>)).
   * <p>For a matrix returns the <a href="http://mathworld.wolfram.com/MaximumAbsoluteColumnSumNorm.html">
     * Maximum Absolute Column Sum Norm</a> of the matrix.</p>
     *
   * @param m vector or matrix
   * @return the norm
   */
  public abstract double getNorm1(final Matrix<?> m);

  /**
   * For a vector returns <a href="http://mathworld.wolfram.com/L2-Norm.html"> L2-Norm or Euclidean Norm</a>
   * <p>For a matrix returns the <a href="http://mathworld.wolfram.com/SpectralNorm.html"> spectral norm</a></p>
   * @param m vector or matrix
   * @return the norm
   */
  public abstract double getNorm2(final Matrix<?> m);

  /**
   * For a vector returns the <a href="http://mathworld.wolfram.com/L-Infinity-Norm.html"> L<sub>&infin;</sub> norm</a>.
   * The L<sub>&infin;</sub> norm is the max of the absolute values of elements.
   * <p>For a matrix returns the <a href="http://mathworld.wolfram.com/MaximumAbsoluteRowSumNorm.html"> Maximum Absolute Row Sum Norm</a></p>
   * @param m a vector or a matrix
   * @return the norm
   */
  public abstract double getNormInfinity(final Matrix<?> m);

  /**
   * Returns a matrix raised to some integer power, e.g. A<sup>3</sup> = A*A*A
   * @param m Some square Matrix
   * @param p An integer power
   * @return The matrix result 
   */
  public abstract DoubleMatrix2D getPower(final Matrix<?> m, final int p);

  /**
   * Returns a matrix raised to some power, e.g. A<sup>3</sup> = A*A*A
   * @param m Some square Matrix
   * @param p The power
   * @return The matrix result 
   */
  public abstract DoubleMatrix2D getPower(Matrix<?> m, double p);

  /**
   * Returns the trace (i.e. sum of diagonal elements) of a matrix
   * @param m Some square matrix
   * @return The trace 
   */
  public abstract double getTrace(final Matrix<?> m);

  /**
   * Returns the transpose of a matrix
   * @param m Some matrix
   * @return The transpose
   */
  public abstract DoubleMatrix2D getTranspose(final Matrix<?> m);
}
