/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.apache.commons.lang.Validate;

/**
 * A minimal implementation of a 2D matrix of doubles 
 * 
 */
public class DoubleMatrix2D implements Matrix<Double> {
  private final double[][] _data;
  private final int _rows;
  private final int _columns;
  private final int _elements;
  /**
   * Empty 2D matrix
   */
  public static final DoubleMatrix2D EMPTY_MATRIX = new DoubleMatrix2D(new double[0][0]);

  //REVIEW could do with a constructor that does NOT copy the data 
  public DoubleMatrix2D(final double[][] data) {
    Validate.notNull(data);
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

  public DoubleMatrix2D(final Double[][] data) {
    Validate.notNull(data);
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _columns;
    result = prime * result + _rows;
    int count = 0;
    for (int i = 0; i < _rows; i++) {
      for (int j = 0; j < _columns; j++) {
        result = prime * result + Double.valueOf(_data[i][j]).hashCode();
        if (count == 10) {
          break;
        }
        count++;
      }
      if (count == 10) {
        break;
      }
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DoubleMatrix2D other = (DoubleMatrix2D) obj;
    if (_columns != other._columns) {
      return false;
    }
    if (_rows != other._rows) {
      return false;
    }
    for (int i = 0; i < _rows; i++) {
      for (int j = 0; j < _columns; j++) {
        if (Double.doubleToLongBits(_data[i][j]) != Double.doubleToLongBits(other._data[i][j])) {
          return false;
        }
      }
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
