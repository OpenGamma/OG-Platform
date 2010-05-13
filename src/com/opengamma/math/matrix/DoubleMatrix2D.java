/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

public class DoubleMatrix2D implements Matrix<Double[][]> {
  private final Double[][] _data;
  private final double[][] _primitives;
  private final int _rows;
  private final int _columns;
  private final int _elements;

  public DoubleMatrix2D(final double[][] primitives) {
    if (primitives == null)
      throw new IllegalArgumentException("Cannot initialize matrix with null data");
    if (primitives.length == 0) {
      _data = new Double[0][0];
      _primitives = new double[0][0];
      _elements = 0;
      _rows = 0;
      _columns = 0;
    } else {
      _rows = primitives.length;
      _columns = primitives[0].length;
      _data = new Double[_rows][_columns];
      _primitives = new double[_rows][_columns];
      for (int i = 0; i < _rows; i++) {
        if (primitives[i].length != _columns)
          throw new IllegalArgumentException("Number of columns in row " + i + " did not match that in first row");
        for (int j = 0; j < _columns; j++) {
          _data[i][j] = primitives[i][j];
          _primitives[i][j] = primitives[i][j];
        }
      }
      _elements = _rows * _columns;
    }
  }

  public DoubleMatrix2D(final Double[][] data) {
    if (data == null)
      throw new IllegalArgumentException("Cannot initialize matrix with null data");
    if (data.length == 0) {
      _primitives = new double[0][0];
      _data = new Double[0][0];
      _elements = 0;
      _rows = 0;
      _columns = 0;
    } else {
      _rows = data.length;
      _columns = data[0].length;
      _primitives = new double[_rows][_columns];
      _data = new Double[_rows][_columns];
      for (int i = 0; i < _rows; i++) {
        if (data[i].length != _columns)
          throw new IllegalArgumentException("Number of columns in row " + i + " did not match that in first row");
        for (int j = 0; j < _columns; j++) {
          _primitives[i][j] = data[i][j];
          _data[i][j] = data[i][j];
        }
      }
      _elements = _rows * _columns;
    }
  }

  public double getElement(final int rowIndex, final int columnIndex) {
    return _primitives[rowIndex][columnIndex];

  }

  public double[][] getDataAsPrimitiveArray() {
    return _primitives;
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

  public DoubleMatrix2D multiply(final DoubleMatrix2D a, final DoubleMatrix2D b) {
    if (a == null || b == null)
      throw new IllegalArgumentException("Passed in a null");
    final int p = b._rows;
    if (a._columns != p)
      throw new IllegalArgumentException("Matrix size mismatch");
    final int m = a._rows;
    final int n = b._columns;
    double sum;
    final double[][] res = new double[m][n];
    int i, j, k;
    for (i = 0; i < m; i++) {
      for (j = 0; j < n; j++) {
        sum = 0.0;
        for (k = 0; k < p; k++)
          sum += a._primitives[i][k] * b._primitives[k][j];
        res[i][j] = sum;
      }
    }
    return new DoubleMatrix2D(res);
  }

  public DoubleMatrix2D getTranspose() {

    final double[][] primitives = new double[_columns][_rows];
    for (int i = 0; i < _rows; i++)
      for (int j = 0; j < _columns; j++) {
        primitives[i][j] = _primitives[j][i];
      }

    return new DoubleMatrix2D(primitives);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _data.hashCode();
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final DoubleMatrix2D other = (DoubleMatrix2D) obj;
    if (_data == null) {
      if (other._data != null)
        return false;
    } else if (!_data.equals(other._data))
      return false;
    return true;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    for (final Double[] d : _data) {
      sb.append("(");
      for (int i = 0; i < d.length - 1; i++) {
        sb.append(d[i] + "\t");
      }
      sb.append(d[d.length - 1] + ")\n");
    }
    return sb.toString();
  }
}
