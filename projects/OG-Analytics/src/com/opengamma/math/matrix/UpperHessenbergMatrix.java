/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Upper Hessenberg matrix is an implementation of the Matrix interface.
 * It holds data and properties needed to define an Upper Hessenberg matrix
 * and implements methods to access particular parts of the matrix useful to
 * more general mathematics.
 *
 * An Upper Hessenberg matrix is a square matrix with entries on the upper triangle
 * directly below the diagonal. The matrix generally looks like:
 *
 *  |* * * *     * * *|
 *  |* * * *     * * *|
 *  |o * * * ... * * *|
 *  |o o * *     * * *|
 *  |o o o *     * * *|
 *  |o o o o.    * * *|
 *  |   .      . * * *|
 *  |   .  o ... o * *|
 *
 * where '*' denotes entry and 'o' denotes zero.
 *
 * To reduce arithmetic intensity, where possible, the matrix is stored as a ragged
 * array of arrays, the entries of which are solely the nonzero parts of the matrix.
 *
 */

public class UpperHessenbergMatrix implements Matrix<Double> {
  private static final Logger s_log = LoggerFactory.getLogger(UpperHessenbergMatrix.class);
  private final double[][] _data;
  private final int _dimension;
  private final int _rows;
  private final int _columns;
  private final int _elements;
  private final boolean _debug = false; // set to true to turn on the logging code for debug

  /**
   * Constructor
   */

  /**
   * Sets up an empty Upper Hessenberg matrix
   * @param dimension Dimension of matrix
   */
  public UpperHessenbergMatrix(final int dimension) {
    Validate.isTrue(dimension > 0, "dimension cannot be negative or zero");
    _rows = dimension;
    _columns = dimension;
    _dimension = dimension;
    _data = new double[_rows][];
    _data[0] = new double [dimension];
    for (int i = 1; i < dimension; i++) {
      _data[i] = new double [dimension + 1 - i];
    }
    _elements = _dimension * (_dimension + 1) / 2 + (_dimension - 1);

    // print matrix if debug
    if (_debug) {
      for (int i = 0; i < _rows; i++) {
        for (int j = 0; j < _data[i].length; j++) {
          s_log.info("i=" + i + " j=" + j + " data= " + _data[i][j]);
        }
      }
    }

  }

  /**
   * @param indata The data, not null. The data is expected in row major form, method args are raggedity invariant
   * @throws IllegalArgumentException If the matrix is not square.
   */
  public UpperHessenbergMatrix(final double[][] indata) {
    Validate.notNull(indata);
    if (indata.length == 0) {
      _data = new double[0][0];
      _dimension = 0;
      _elements = 0;
      _rows = 0;
      _columns = 0;
    } else {

      _dimension = indata.length;
      _rows = _dimension;
      _columns = _dimension;
      _elements = _dimension * (_dimension + 1) / 2 + (_dimension - 1);

      if (_debug) {
        s_log.info("rows = " + _rows + " _columns =" + _columns + " _dimension= " + _dimension);
      }

      /**
       * Ragged array, number of should vary from _columns to 2 with row.
       */
      _data = new double[_rows][];
      _data[0] = new double [_rows];
      for (int i = 1; i < _rows; i++) {
        _data[i] = new double [_rows - i + 1];
        if (_debug) {
          s_log.info("_data[" + i + "].length=" + _data[i].length);
        }
      }

      /**
       * Unwind the data appropriately
       */
      int sqc = 0;

      // see if indata is square
      for (int i = 0; i < _rows; i++) {
        if (indata[i].length == _rows) {
          sqc++;
        }
      }

      // Ensure the number of elements in the first row of the input data is the same as that in the first row of the Hessenberg formation
      Validate.isTrue(indata[0].length == _dimension);
      for (int j = 0; j < _data[0].length; j++) {
        _data[0][j] = indata[0][j];
      }

      if (sqc == _rows) {
        // try to unwind the data assuming it is a "square" array
        for (int i = 1; i < _rows; i++) {
          for (int j = 0; j < _data[i].length; j++) {
            _data[i][j] = indata[i][i - 1 + j];
          }
        }
      } else {
        // try to unwind the data assuming it is a "ragged" array
        for (int i = 1; i < _rows; i++) {
          Validate.isTrue(indata[i].length >= _data[i].length);
          for (int j = 0; j < _data[i].length; j++) {
            _data[i][j] = indata[i][j];
          }
        }
      }

      // print matrix if debug
      if (_debug) {
        for (int i = 0; i < _rows; i++) {
          for (int j = 0; j < _data[i].length; j++) {
            s_log.info("i=" + i + " j=" + j + " data= " + _data[i][j]);
          }
        }
      }
    }
  }


  /** Methods: **/

  /**
   * toFullMatrix() converts an Upper Hessenberg matrix to a full (DoubleMatrix2D) matrix.
   * @return a DoubleMatrix2D representation of an Upper Hessenberg matrix.
   */
  public DoubleMatrix2D toFullMatrix() {
    DoubleMatrix2D matrix2d;
    double[][] tmp = new double[_dimension][_dimension];
    //copy first row
    for (int j = 0; j < _dimension; j++) {
      tmp[0][j] = _data[0][j];
    }

    //unwind data into a double matrix
    for (int i = 1; i < _dimension; i++) {
      for (int j = 0; j < _data[i].length; j++) {
        tmp[i][j + i - 1] = _data[i][j];
      }
    }
    //create new double matrix based on the unwound data
    matrix2d = new DoubleMatrix2D(tmp);
    return matrix2d;
  }

  /**
   * toArray() returns and array of arrays representation of the Upper Hessenberg matrix
   * @return double[][] an double array of double arrays representing the Upper Hessenberg matrix.
   * A new copy of the data is generated such that the original object data is safe.
   */
  public double[][] toArray() {
    final DoubleMatrix2D tmp = toFullMatrix();
    return tmp.getData();
  }

  /**
   * getRowVector(index) returns the row "index" of the Upper Hessenberg matrix,
   * data which is by definition zero is not returned.
   *  @param index The index of the row to be returned
   *  @return DoubleMatrix1D the row specified by index
   **/
  public DoubleMatrix1D getRowVector(final int index) {
    Validate.notNull(_data);
    Validate.isTrue(index < _dimension);
    Validate.isTrue(index >= 0);
    final int len = _data[index].length;
    double[] tmp = new double[len];
    for (int i = 0; i < len; i++) {
      tmp[i] = _data[index][i];
    }
    return new DoubleMatrix1D(tmp);
  }


  /**
   * getFullRowVector(index) returns the row "index" of the Upper Hessenberg matrix,
   * data which is by definition zero *is* returned as part of the vector.
   *  @param index The index of the row to be returned
   *  @return DoubleMatrix1D the row specified by index
   **/
  public DoubleMatrix1D getFullRowVector(final int index) {
    Validate.notNull(_data);
    Validate.isTrue(index < _dimension);
    Validate.isTrue(index >= 0);
    double[] tmp = new double[_dimension];
    for (int i = 0; i < _data[index].length; i++) {
      tmp[i + _dimension - _data[index].length ] = _data[index][i];
    }
    return new DoubleMatrix1D(tmp);
  }


  /**
   * getColumnVector(index) returns the column "index" of the Upper Hessenberg matrix,
   * data which is by definition zero is not returned.
   *  @param index The index of the column to be returned
   *  @return DoubleMatrix1D the column specified by index
   **/
  public DoubleMatrix1D getColumnVector(final int index) {
    Validate.isTrue(index < _dimension);
    Validate.isTrue(index >= 0);
    int len = Math.min(index + 2, _dimension);
    double[] tmp = new double[len];
    tmp[0] = _data[0][index];
    for (int i = 1; i < index + 1; i++) {
      s_log.warn("adding _data = " + _data[i][len - i + 1]);
      tmp[i] = _data[i][len - i + 1];
    }
    return new DoubleMatrix1D(tmp);
  }


  /**
   * getDiag()returns a DoubleMatrix1D of the diagonal entries in the Upper Hessenberg matrix
   * @return DoubleMatrix1D the diagonal entries of the Upper Hessenberg Matrix
   */
  public DoubleMatrix1D getDiag() {
    final double[] tmp = new double[_dimension];
    for (int i = 0; i < _dimension; i++) {
      tmp[i] = _data[i][i];
    }
    return new DoubleMatrix1D(tmp);
  }

  @Override
  public Double getEntry(final int... index) {
    return _data[index[0]][index[1]];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfElements() {
    return _elements;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _columns;
    result = prime * result + Arrays.hashCode(_data);
    result = prime * result + _dimension;
    result = prime * result + _elements;
    result = prime * result + _rows;
    return result;
  }

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
    UpperHessenbergMatrix other = (UpperHessenbergMatrix) obj;
    if (_columns != other._columns) {
      return false;
    }
    if (!Arrays.equals(_data, other._data)) {
      return false;
    }
    if (_dimension != other._dimension) {
      return false;
    }
    if (_elements != other._elements) {
      return false;
    }
    if (_rows != other._rows) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    String tmp = new String("\nUpperHessenbergMatrix:\n" +
      "_dimension=" + _dimension + "\n_rows=" + _rows + "\n_columns=" +
      _columns + "\n_elements=" + _elements);
    tmp += "\n_data=";
    for (int i = 0; i < _rows; i++) {
      tmp += "\n";
      for (int j = 0; j < _data[i].length; j++) {
        tmp += String.format("%12.8f ", _data[i][j]);
      }
    }
    tmp += "\n";
    return tmp;
  }



}
