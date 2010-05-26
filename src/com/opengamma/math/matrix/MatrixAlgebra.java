/**
 * Copyright (C); 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.apache.commons.lang.NotImplementedException;

/**
 * @author emcleod
 * 
 */
public abstract class MatrixAlgebra {

  public Matrix<?> add(final Matrix<?> m1, final Matrix<?> m2) {
    if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix1D) {
      final double[] x1 = ((DoubleMatrix1D) m1).getData();
      final double[] x2 = ((DoubleMatrix1D) m2).getData();
      final int n = x1.length;
      if (n != x2.length)
        throw new IllegalArgumentException("Can only add matrices of the same shape");
      final double[] sum = new double[n];
      for (int i = 0; i < n; i++) {
        sum[i] = x1[i] + x2[i];
      }
      return new DoubleMatrix1D(sum);
    } else if (m1 instanceof DoubleMatrix2D && m2 instanceof DoubleMatrix2D) {
      final double[][] x1 = ((DoubleMatrix2D) m1).getData();
      final double[][] x2 = ((DoubleMatrix2D) m2).getData();
      final int n = x1.length;
      final int m = x1[0].length;
      if (n != x2.length)
        throw new IllegalArgumentException("Can only add matrices of the same shape");
      final double[][] sum = new double[n][x1[0].length];
      for (int i = 0; i < n; i++) {
        if (x2[i].length != m)
          throw new IllegalArgumentException("Can only add matrices of the same shape");
        for (int j = 0; j < m; j++) {
          sum[i][j] = x1[i][j] + x2[i][j];
        }
      }
      return new DoubleMatrix2D(sum);
    }
    throw new NotImplementedException();
  }

  public Matrix<?> divide(final Matrix<?> m1, final Matrix<?> m2) {
    if (!(m1 instanceof DoubleMatrix2D))
      throw new IllegalArgumentException("Can only divide a 2D matrix");
    if (!(m2 instanceof DoubleMatrix2D))
      throw new IllegalArgumentException("Can only perform division with a 2D matrix");
    return multiply(m1, getInverse(m2));
  }

  public abstract Matrix<?> multiply(final Matrix<?> m1, final Matrix<?> m2);

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
    if (m1 instanceof DoubleMatrix1D && m2 instanceof DoubleMatrix1D) {
      final double[] x1 = ((DoubleMatrix1D) m1).getData();
      final double[] x2 = ((DoubleMatrix1D) m2).getData();
      final int n = x1.length;
      if (n != x2.length)
        throw new IllegalArgumentException("Can only subtract matrices of the same shape");
      final double[] subtract = new double[n];
      for (int i = 0; i < n; i++) {
        subtract[i] = x1[i] - x2[i];
      }
      return new DoubleMatrix1D(subtract);
    } else if (m1 instanceof DoubleMatrix2D && m2 instanceof DoubleMatrix2D) {
      final double[][] x1 = ((DoubleMatrix2D) m1).getData();
      final double[][] x2 = ((DoubleMatrix2D) m2).getData();
      final int n = x1.length;
      final int m = x1[0].length;
      if (n != x2.length)
        throw new IllegalArgumentException("Can only subtract matrices of the same shape");
      final double[][] subtract = new double[n][x1[0].length];
      for (int i = 0; i < n; i++) {
        if (x2[i].length != m)
          throw new IllegalArgumentException("Can only subtract matrices of the same shape");
        for (int j = 0; j < m; j++) {
          subtract[i][j] = x1[i][j] - x2[i][j];
        }
      }
      return new DoubleMatrix2D(subtract);
    }
    throw new NotImplementedException();
  }

  public abstract double getCondition(final Matrix<?> m);

  public abstract double getDeterminant(final Matrix<?> m);

  public abstract DoubleMatrix2D getInverse(final Matrix<?> m);

  public abstract double getInnerProduct(final Matrix<?> m1, final Matrix<?> m2);

  public abstract DoubleMatrix2D getOuterProduct(final Matrix<?> m1, final Matrix<?> m2);

  public abstract double getNorm1(final Matrix<?> m);

  public abstract double getNorm2(final Matrix<?> m);

  public abstract double getNormInfinity(final Matrix<?> m);

  public abstract DoubleMatrix2D getPower(final Matrix<?> m, final int p);

  public abstract double getTrace(final Matrix<?> m);

  public abstract DoubleMatrix2D getTranspose(final Matrix<?> m);
}
