/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * Converts or instantiates a matrix to Sparse Coordinate Format (COO). COO is a non optimal method of storing sparse matrix data and is generally used as
 * an intermediary storage format prior to conversion to @see {@link CompressedSparseRowFormatMatrix} or {@link CompressedSparseColumnFormatMatrix}.
 * Only the non-zero components of the matrix are stored (note: there is no tolerance for testing zero to machine precision, zero is solely tested bitwise).
 * The COO format requires three vectors:
 * (x,y): form a coordinate pairing to give the location of an element within the sparse matrix
 * values: contains the values at coordinate pairings (x,y)
 */

public class SparseCoordinateFormatMatrix extends SparseMatrixType {
  private double[] _values;
  private int[] _x;
  private int[] _y;
  private int _els;
  private int _rows;
  private int _cols;

  // constructors
  /**
   * Construct from DoubleMatrix2D type
   * @param aMatrix is a DoubleMatrix2D
   */
  public SparseCoordinateFormatMatrix(final DoubleMatrix2D aMatrix) {
    Validate.notNull(aMatrix);

    //get number of elements
    _els = aMatrix.getNumberOfElements();

    // tmp arrays, in case we get in a fully populated matrix, intelligent design upstream should ensure that this is overkill!
    double[] valuesTmp = new double[_els];
    int[] xTmp = new int[_els];
    int[] yTmp = new int[_els];

    // we need unwind the array aMatrix into coordinate form
    int ptr = 0;
    for (int i = 0; i < aMatrix.getNumberOfRows(); i++) {
      for (int j = 0; j < aMatrix.getNumberOfColumns(); j++) {
        if (Double.doubleToLongBits(aMatrix.getEntry(i, j)) != 0L) {
          xTmp[ptr] = j;
          yTmp[ptr] = i;
          valuesTmp[ptr] = aMatrix.getEntry(i, j);
          ptr++;
        }
      }
    }

    _values = Arrays.copyOfRange(valuesTmp, 0, ptr);
    _x = Arrays.copyOfRange(xTmp, 0, ptr);
    _y = Arrays.copyOfRange(yTmp, 0, ptr);
    _rows = aMatrix.getNumberOfRows();
    _cols = aMatrix.getNumberOfColumns();
  }

  /**
   * Construct from UpperHessenbergMatrix type
   * @param aMatrix is an UpperHessenbergMatrix
   */
  public SparseCoordinateFormatMatrix(final UpperHessenbergMatrix aMatrix) {
    // for now, convert to DoubleMatrix2D and use its constructor
    this(aMatrix.toFullMatrix());
  }

  /**
   * Construct from double[][] type
   *  @param aMatrix is an array of arrays describing a matrix
   */
  public SparseCoordinateFormatMatrix(final double[][] aMatrix) {
    // for now, convert to DoubleMatrix2D and use it's constructor
    this(new DoubleMatrix2D(aMatrix));
  }


// here we shall shove in constructors for just about all types (eventually).


// Methods
  /**
   * Returns the "coordinates" of the columns where there are nonzero entries
   * @return _x, the column coordinates
   */
  public int[] getColumnCoordinates() {
    return _x;
  }

  /**
   * Returns the "coordinates" of the rows where there are nonzero entries
   * @return _y, the row coordinates
   */
  public int[] getRowCoordinates() {
    return _y;
  }

  /**
   * Returns the nonzero entries
   * @return _values, the values
   */
  public double[] getNonZeroEntries() {
    return _values;
  }

  /**
   * Returns the number of rows corresponding to the full matrix representation
   * @return _rows, the number of rows in the matrix
   */
  public int getNumberOfRows() {
    return _rows;
  }

  /**
   * Returns the number of columns corresponding to the full matrix representation
   * @return _cols, the number of columns in the matrix
   */
  public int getNumberOfColumns() {
    return _cols;
  }

  /**
   * Converts COO to array of arrays
   * @return tmp, an array of arrays corresponding to the full matrix representation
   */
  @Override
  public double[][] toArray() {
    double[][] tmp = new double[_rows][_cols];
    for (int i = 0; i < _x.length; i++) {
      tmp[_y[i]][_x[i]] = _values[i];
    }
    return tmp;
  }

  /**
   * Converts COO to DoubleMatrix2D
   * @return tmp, a DoubleMatrix2D corresponding to the full matrix representation
   */
  public DoubleMatrix2D toFullMatrix() {
    return new DoubleMatrix2D(this.toArray());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getFullRow(int index) {
    double[] tmp = new double[_cols];
    // brute force
    for (int i = 0; i < _y.length; i++) {
      if (_y[i] == index) {
        tmp[_x[i]] = _values[i];
      }
    }
    return tmp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getFullColumn(int index) {
    double[] tmp = new double[_rows];
    // brute force
    for (int i = 0; i < _y.length; i++) {
      if (_x[i] == index) {
        tmp[_y[i]] = _values[i];
      }
    }
    return tmp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getRowElements(int index) {
    double[] tmp = new double[_cols]; //overkill
    int ptr = 0;
    // brute force
    for (int i = 0; i < _y.length; i++) {
      if (_y[i] == index) {
        tmp[ptr] = _values[i];
        ptr++;
      }
    }
    return  Arrays.copyOfRange(tmp, 0, ptr);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getColumnElements(int index) {
    double[] tmp = new double[_rows]; //overkill
    int ptr = 0;
    // brute force
    for (int i = 0; i < _y.length; i++) {
      if (_x[i] == index) {
        tmp[ptr] = _values[i];
        ptr++;
      }
    }
    return  Arrays.copyOfRange(tmp, 0, ptr);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNonZeroElements() {
    return _values.length;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfElements() {
    return _els;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double getEntry(int... indices) {
    Validate.notNull(_x);
    Validate.notNull(_y);
    Validate.notNull(_values);

    // no real fast way of doing this, brute force used
    for (int i = 0; i < _y.length; i++) {
      if (_y[i] == indices[0] && _x[i] == indices[1]) {
        return _values[i];
      }
    }
    return 0.0; // index pairing not found so value must be a zero entry
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "SparseCoordinateFormatMatrix:\n"
      + "x=" + Arrays.toString(_x) + "\n"
      + "y=" + Arrays.toString(_y) + "\n"
      + "data=" + Arrays.toString(_values) + "\n"
      + "number of nonzero elements=" + _els;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _els;
    result = prime * result + Arrays.hashCode(_values);
    result = prime * result + Arrays.hashCode(_x);
    result = prime * result + Arrays.hashCode(_y);
    return result;
  }

  /**
   * {@inheritDoc}
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
    SparseCoordinateFormatMatrix other = (SparseCoordinateFormatMatrix) obj;
    if (_els != other._els) {
      return false;
    }
    if (!Arrays.equals(_values, other._values)) {
      return false;
    }
    if (!Arrays.equals(_x, other._x)) {
      return false;
    }
    if (!Arrays.equals(_y, other._y)) {
      return false;
    }
    return true;
  }

}
