/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;

/**
 * Various utility classes for matrices.
 */
public final class DoubleMatrixUtils {

  private DoubleMatrixUtils() {
    //Cannot instantiate
  }

  public static DoubleMatrix2D getTranspose(final DoubleMatrix2D matrix) {
    final int rows = matrix.getNumberOfRows();
    final int columns = matrix.getNumberOfColumns();
    final double[][] primitives = new double[columns][rows];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        primitives[i][j] = matrix.getEntry(j, i);
      }
    }
    return new DoubleMatrix2D(primitives);
  }

  public static DoubleMatrix2D getIdentityMatrix2D(final int dimension) {
    ArgumentChecker.notNegative(dimension, "dimension");
    if (dimension == 0) {
      return DoubleMatrix2D.EMPTY_MATRIX;
    }
    if (dimension == 1) {
      return new DoubleMatrix2D(new double[][] {new double[] {1}});
    }
    final double[][] data = new double[dimension][dimension];
    for (int i = 0; i < dimension; i++) {
      data[i][i] = 1;
    }
    return new DoubleMatrix2D(data);
  }

  public static DoubleMatrix2D getTwoDimensionalDiagonalMatrix(final DoubleMatrix1D vector) {
    Validate.notNull(vector);
    final int n = vector.getNumberOfElements();
    if (n == 0) {
      return DoubleMatrix2D.EMPTY_MATRIX;
    }
    final double[][] data = new double[n][n];
    for (int i = 0; i < n; i++) {
      data[i][i] = vector.getEntry(i);
    }
    return new DoubleMatrix2D(data);
  }

  public static DoubleMatrix2D getTwoDimensionalDiagonalMatrix(final double[] vector) {
    Validate.notNull(vector);
    return getTwoDimensionalDiagonalMatrix(new DoubleMatrix1D(vector));
  }
}
