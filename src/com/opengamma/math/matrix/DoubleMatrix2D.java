/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import java.util.Arrays;

/**
 * A minimal implementation of a matrix of doubles class 
 */
public class DoubleMatrix2D implements Matrix<Double> {
  private final double[][] _data;
  private final int _rows;
  private final int _columns;
  private final int _elements;

  public DoubleMatrix2D(final double[][] primitives) {
    if (primitives == null) {
      throw new IllegalArgumentException("Cannot initialize matrix with null data");
    }
    if (primitives.length == 0) {
      _data = new double[0][0];
      _elements = 0;
      _rows = 0;
      _columns = 0;
    } else {
      _rows = primitives.length;
      _columns = primitives[0].length;
      _data = new double[_rows][_columns];
      for (int i = 0; i < _rows; i++) {
        if (primitives[i].length != _columns) {
          throw new IllegalArgumentException("Number of columns in row " + i + " did not match that in first row");
        }
        for (int j = 0; j < _columns; j++) {
          _data[i][j] = primitives[i][j];
        }
      }
      _elements = _rows * _columns;
    }
  }

  public DoubleMatrix2D(final Double[][] data) {
    if (data == null) {
      throw new IllegalArgumentException("Cannot initialize matrix with null data");
    }
    if (data.length == 0) {
      _data = new double[0][0];
      _elements = 0;
      _rows = 0;
      _columns = 0;
    } else {
      _rows = data.length;
      _columns = data[0].length;
      _data = new double[_rows][_columns];
      for (int i = 0; i < _rows; i++) {
        if (data[i].length != _columns) {
          throw new IllegalArgumentException("Number of columns in row " + i + " did not match that in first row");
        }
        for (int j = 0; j < _columns; j++) {
          _data[i][j] = data[i][j];
        }
      }
      _elements = _rows * _columns;
    }
  }

  @Override
  public Double getEntry(final int... index) {
    return _data[index[0]][index[1]];

  }

  public double[][] getData() {
    return _data;
  }

  public int getNumberOfElements() {
    return _elements;
  }

  public int getNumberOfRows() {
    return _rows;
  }

  public int getNumberOfColumns() {
    return _columns;
  }

  public DoubleMatrix2D multiply(final DoubleMatrix2D other) {
    return DoubleMatrix2D.multiply(this, other);
  }

  /**
   * Multiply two matrices together, i.e. c = a*b
   * @param a first matrix
   * @param b second matrix
   * @return new matrix c
   */
  public static DoubleMatrix2D multiply(final DoubleMatrix2D a, final DoubleMatrix2D b) {
    if (a == null || b == null) {
      throw new IllegalArgumentException("Passed in a null");
    }
    final int p = b._rows;
    if (a._columns != p) {
      throw new IllegalArgumentException("Matrix size mismatch");
    }
    final int m = a._rows;
    final int n = b._columns;
    double sum;
    final double[][] res = new double[m][n];
    int i, j, k;
    for (i = 0; i < m; i++) {
      for (j = 0; j < n; j++) {
        sum = 0.0;
        for (k = 0; k < p; k++) {
          sum += a._data[i][k] * b._data[k][j];
        }
        res[i][j] = sum;
      }
    }
    return new DoubleMatrix2D(res);
  }

  /**
   * Multiply this matrix by a vector 
   * @param vector
   * @return a new vector 
   */
  public DoubleMatrix1D multiply(final DoubleMatrix1D vector) {
    return multiply(this, vector);
  }

  /**
   * Multiply a matrix by a vector, i.e c = a*b
   * @param a matrix 
   * @param b vector
   * @return A new vector c
   */
  public static DoubleMatrix1D multiply(final DoubleMatrix2D a, final DoubleMatrix1D b) {
    if (a == null || b == null) {
      throw new IllegalArgumentException("Passed in a null");
    }
    final int p = b.getNumberOfElements();
    if (a._columns != p) {
      throw new IllegalArgumentException("Matrix/Vector size mismatch");
    }
    final int m = a._rows;
    double sum;
    final double[] res = new double[m];
    int i, j;
    for (i = 0; i < m; i++) {
      sum = 0.0;
      for (j = 0; j < p; j++) {
        sum += a._data[i][j] * b.getEntry(j);
        res[i] = sum;
      }
    }
    return new DoubleMatrix1D(res);
  }

  /**
   * Gets the matrix transpose
   * @return a new matrix 
   */
  public DoubleMatrix2D getTranspose() {

    final double[][] primitives = new double[_columns][_rows];
    for (int i = 0; i < _rows; i++) {
      for (int j = 0; j < _columns; j++) {
        primitives[i][j] = _data[j][i];
      }
    }

    return new DoubleMatrix2D(primitives);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _columns;
    result = prime * result + _elements;
    result = prime * result + Arrays.hashCode(_data);
    result = prime * result + _rows;
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    DoubleMatrix2D other = (DoubleMatrix2D) obj;
    if (_columns != other._columns) {
      return false;
    }
    if (_elements != other._elements) {
      return false;
    }
    if (!Arrays.equals(_data, other._data)) {
      return false;
    }
    if (_rows != other._rows) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    for (final double[] d : _data) {
      sb.append("(");
      for (int i = 0; i < d.length - 1; i++) {
        sb.append(d[i] + "\t");
      }
      sb.append(d[d.length - 1] + ")\n");
    }
    return sb.toString();
  }
}
