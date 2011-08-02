/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 *
 */
public class SparseCoordinateFormatMatrix implements Matrix<Double> {
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
          xTmp[ptr] = i;
          yTmp[ptr] = j;
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
    // for now, convert to DoubleMatrix2D and use it's constructor
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
  public int[] getColumnCoordinates() {
    return _x;
  }

  public int[] getRowCoordinates() {
    return _y;
  }

  public double[] getNonZeroEntries() {
    return _values;
  }

  public int getNumberOfRows() {
    return _rows;
  }

  public int getNumberOfColumns() {
    return _cols;
  }

  @Override
  public int getNumberOfElements() {
    return _els;
  }

  @Override
  public Double getEntry(int... indices) {
    Validate.notNull(_x);
    Validate.notNull(_y);
    Validate.notNull(_values);

    // no real fast way of doing this, brute force used
    for (int i = 0; i < _x.length; i++) {
      if (_x[i] == indices[0] && _y[i] == indices[1]) {
        return _values[i];
      }
    }
    return 0D; // index pairing not found so value must be a zero entry
  }

  @Override
  public String toString() {
    return "SparseCoordinateFormatMatrix:\n"
      + "x=" + Arrays.toString(_x) + "\n"
      + "y=" + Arrays.toString(_y) + "\n"
      + "data=" + Arrays.toString(_values) + "\n"
      + "number of nonzero elements=" + _els;
  }

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
