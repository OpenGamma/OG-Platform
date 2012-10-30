/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Converts or instantiates a matrix to Packed Matrix format. This class is reserved for expert use only.
 */
public class PackedMatrix implements MatrixPrimitive {
  private double[] _data; // the data
  private int _rows; // number of rows
  private int _cols; // number of columns
  private int _els; // number of elements
  private int[] _colCount; // cumulative sum of unwound columns (incremented per row)
  private int[] _rowPtr; // pointer to where the data should start on a given row

  /**
   * Enumerator for where zeros are allowed within the packing.
   */
  public enum allowZerosOn {

    /**
     * Allow zeros to be packed on the left side of the matrix. For example
     * pack({0 0 1 2 3 4 5 0}) -> {0 0 1 2 3 4 5}
     */
    leftSide,

    /**
     * Allow zeros to be packed on the right side of the matrix. For example
     * pack({0 0 1 2 3 4 5 0}) -> {1 2 3 4 5 0}
     */
    rightSide,

    /**
     * Allow zeros to be packed on both sides of the matrix. For example
     * pack({0 0 1 2 3 4 5 0}) -> {0 0 1 2 3 4 5 0}
     */
    bothSides,

    /**
     * Zeros are forbidden at either sides of the data
     * pack({0 0 1 2 3 4 5 0}) -> {1 2 3 4 5}
     */
    none
  }

  /**
   * Constructors
   */

  /**
   * Constructs from an array of arrays representation.
   * @param aMatrix is an n columns x m rows matrix stored as a row major array of arrays.
   */
  public PackedMatrix(double[][] aMatrix) {
    this(aMatrix, allowZerosOn.none, aMatrix.length, aMatrix[0].length);
  }

  /**
   * Constructs from a DoubleMatrix2D representation
   * @param aMatrix is a DoubleMatrix2D
   */
  public PackedMatrix(DoubleMatrix2D aMatrix) {
    this(aMatrix.toArray(), allowZerosOn.none, aMatrix.getNumberOfRows(), aMatrix.getNumberOfColumns());
  }

  /**
   * Constructs from an array of arrays representation.
   * @param aMatrix is an n columns x m rows matrix stored as a row major array of arrays.
   * @param allowZeros is set to allow the packing of zeros in the packed data structure.
   */
  public PackedMatrix(double[][] aMatrix, allowZerosOn allowZeros) {
    this(aMatrix, allowZeros, aMatrix.length, aMatrix[0].length);
  }

  /**
   * Constructs from array of arrays *but* allows zeros to be packed into the data structure.
   * This is particularly useful for banded matrices in which
   * allowing some zero padding is beneficial in terms of making access patterns more simple.
   * @param aMatrix is an n columns x m rows matrix stored as a row major array of arrays.
   * @param zeroPattern is enumerated based on {@link allowZerosOn} to allow the packing of zeros in the packed data structure.
   * @param rows is the number of rows in the matrix that is to be represented
   * @param cols is the number of columns in the matrix that is to be represented
   */
  public PackedMatrix(double[][] aMatrix, allowZerosOn zeroPattern, int rows, int cols) {
    Validate.notNull(aMatrix);
    // test if ragged
    if (MatrixPrimitiveUtils.isRagged(aMatrix)) {
      throw new NotImplementedException("Construction from ragged array not implemented");
    }

    _rows = rows;
    _cols = cols;
    _els = _rows * _cols;

    double[] tmp = new double[_els];
    _rowPtr = new int[_rows];
    _colCount = new int[_rows + 1];

    boolean isSet;
    double val;
    int count = 0;
    _colCount[0] = 0;

    switch (zeroPattern) {
      case bothSides: {
        // make flat!
        for (int i = 0; i < _rows; i++) {
          _rowPtr[i] = 0;
          for (int j = 0; j < _cols; j++) { //for each col
            tmp[count] = aMatrix[i][j]; // assign to tmp
            count++;
          }
          _colCount[i + 1] += count;
        }
        break;
      }
      case rightSide: {
        for (int i = 0; i < _rows; i++) {
          isSet = false; // init each starting point as not being set and look for it.
          for (int j = 0; j < cols; j++) { //for each col
            val = aMatrix[i][j]; // get the value
            if (Double.doubleToLongBits(val) != 0L || isSet) { // test if not zero and whether we have found the start of the data yet
              tmp[count] = val; // assign to tmp
              count++;
              if (!isSet) { // if we haven't already set the starting point in the row
                _rowPtr[i] = j; // assign this element as the starting point
                isSet = true; // and ensure we don't come back here for this row
              }
            }
          }
          _colCount[i + 1] += count;
        }
        break;
      }
      case leftSide: {
        for (int i = 0; i < _rows; i++) {
          isSet = false; // init each starting point as not being set and look for it.

          // search backwards and find the end point
          int end = -1;
          for (int j = _cols - 1; j >= 0; j--) {
            val = aMatrix[i][j];
            if (Double.doubleToLongBits(val) != 0L) { // test if not zero
              end = j;
              break;
            }
          }

          // flatten
          for (int j = 0; j < end + 1; j++) { //for each col
            val = aMatrix[i][j]; // get the value
            tmp[count] = val; // assign to tmp
            count++;
            if (!isSet) { // if we haven't already set the starting point in the row
              _rowPtr[i] = j; // assign this element as the starting point
              isSet = true; // and ensure we don't come back here for this row
            }
          }
          _colCount[i + 1] += count;
        }

        break;
      }
      case none: {
        // make flat!
        for (int i = 0; i < _rows; i++) {
          isSet = false; // init each starting point as not being set and look for it.
          // search backwards and find the end point
          int end = 0;
          for (int j = _cols - 1; j >= 0; j--) {
            val = aMatrix[i][j];
            if (Double.doubleToLongBits(val) != 0L) { // test if not zero
              end = j;
              break;
            }
          }
          // flatten
          for (int j = 0; j < end + 1; j++) { //for each col
            val = aMatrix[i][j]; // get the value
            if (Double.doubleToLongBits(val) != 0L || isSet) { // test if not zero
              tmp[count] = val; // assign to tmp
              count++;
              if (!isSet) { // if we haven't already set the starting point in the row
                _rowPtr[i] = j; // assign this element as the starting point
                isSet = true; // and ensure we don't come back here for this row
              }
            }
          }
          _colCount[i + 1] += count;
        }
        break;
      }

    }
    _data = Arrays.copyOfRange(tmp, 0, count);
  } // method end

  // The old way, turns out our needs are more complicated!
  //    if (!allowZeros) {
  //      // make flat!
  //      for (int i = 0; i < _rows; i++) {
  //        isSet = false; // init each starting point as not being set and look for it.
  //
  //        // search backwards and find the end point
  //        int end = 0;
  //        for (int j = _cols - 1; j >= 0; j--) {
  //          val = aMatrix[i][j];
  //          if (Double.doubleToLongBits(val) != 0L) { // test if not zero
  //            end = j;
  //            break;
  //          }
  //        }
  //
  //        // flatten
  //        for (int j = 0; j < end + 1; j++) { //for each col
  //          val = aMatrix[i][j]; // get the value
  //          if (Double.doubleToLongBits(val) != 0L) { // test if not zero
  //            tmp[count] = val; // assign to tmp
  //            count++;
  //            if (!isSet) { // if we haven't already set the starting point in the row
  //              _rowPtr[i] = j; // assign this element as the starting point
  //              isSet = true; // and ensure we don't come back here for this row
  //            }
  //          }
  //        }
  //        _colCount[i + 1] += count;
  //      }
  //    } else {
  //      /** ALLOWING ZEROS TO BE PACKED INTO THE ARRAY, BEHAVIOUR IS SLIGHTLY DIFFERENT! */
  //      // make flat!
  //      for (int i = 0; i < aMatrix.length; i++) {
  //        isSet = false; // init each starting point as not being set and look for it.
  //        // Don't do this in case a zero pattern accidentally occurs in the middle of a populated row.
  //        //        // test to ensure data is contiguous
  //        //        if (!MatrixPrimitiveUtils.arrayHasContiguousRowEntries(aMatrix[i])) {
  //        //          throw new IllegalArgumentException("Matrix given does not contain contiguous nonzero entries, error was thrown due to bad data on row " + i);
  //        //        }
  //
  //        // flatten
  //        for (int j = 0; j < aMatrix[i].length; j++) { // for each col
  //          val = aMatrix[i][j]; // get the value
  //          tmp[count] = val; // assign to tmp
  //          count++;
  //          if (!isSet) { // if we haven't already set the starting point in the row
  //            _rowPtr[i] = j; // assign this element as the starting point
  //            isSet = true; // and ensure we don't come back here for this row
  //          }
  //        }
  //        _colCount[i + 1] += count;
  //      }
  //    }
  //    _data = Arrays.copyOfRange(tmp, 0, count);
  //  }

  /**
   * Returns the cumulative column count data used which can be used for indexing purposes.
   * @return _colCount, the cumulative column count.
   */
  public int[] getColCount() {
    return _colCount;
  }

  /**
   * Returns the index of where on each row the data starts.
   * @return _rowPtr, the index on each row where the data starts.
   */
  public int[] getRowPointer() {
    return _rowPtr;
  }

  /**
   * Returns the raw data in its packed form.
   * @return _data, the raw data in its packed form.
   */
  public double[] getData() {
    return _data;
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
    if (indices.length > 2) {
      throw new IllegalArgumentException("Too many indices given for matrix dimension");
    }
    for (int i = 0; i < indices.length; i++) {
      if (indices[i] < 0) {
        throw new IllegalArgumentException("Negative index lookup requested");
      }
    }
    int rownum;
    int colnum;
    if (indices.length == 1) {
      rownum = indices[0] / _cols; // row number to look up
      colnum = indices[0] - rownum * _cols; // column to look up
    } else {
      rownum = indices[0];
      colnum = indices[1];
    }

    if (colnum >= _rowPtr[rownum] && colnum < ((_colCount[rownum + 1] - _colCount[rownum]) + _rowPtr[rownum])) {
      return _data[_colCount[rownum] + (colnum - _rowPtr[rownum])];
    } else {
      return 0.0;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getFullRow(int index) {
    double[] tmp = new double[_cols];
    for (int i = 0; i < _cols; i++) {
      tmp[i] = getEntry(index, i);
    }
    return tmp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getFullColumn(int index) {
    double[] tmp = new double[_rows];
    for (int i = 0; i < _rows; i++) {
      tmp[i] = getEntry(i, index);
    }
    return tmp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getRowElements(int index) {
    int count = (_colCount[index + 1] - _colCount[index]);
    double[] tmp = new double[count];
    for (int i = 0; i < count; i++) {
      tmp[i] = _data[_colCount[index] + i];
    }
    return tmp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getColumnElements(int index) {
    double[] tmp = new double[_rows];
    int idx = 0;
    for (int i = 0; i < _rows; i++) {
      if (index >= _rowPtr[i] && index < ((_colCount[i + 1] - _colCount[i]) + _rowPtr[i])) {
        tmp[idx] = _data[_colCount[i] + (index - _rowPtr[i])];
        idx++;
      }

    }
    return Arrays.copyOfRange(tmp, 0, idx);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNonZeroElements() {
    return _data.length;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfRows() {
    return _rows;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfColumns() {
    return _cols;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[][] toArray() {
    double[][] tmp = new double[_rows][_cols];
    for (int i = 0; i < _rows; i++) {
      for (int j = 0; j < _cols; j++) {
        if (j >= _rowPtr[i] && j < ((_colCount[i + 1] - _colCount[i]) + _rowPtr[i])) {
          tmp[i][j] = _data[_colCount[i] + (j - _rowPtr[i])];
        } else {
          tmp[i][j] = 0.0;
        }
      }
    }
    return tmp;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "PackedMatrix:\ndata = " + Arrays.toString(_data)
        + "\nrows = " + _rows + "\ncols= " + _cols + "\nels= " + _els + "n_colCount= " + Arrays.toString(_colCount) + "\n rowPtr= "
        + Arrays.toString(_rowPtr);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_colCount);
    result = prime * result + _cols;
    result = prime * result + Arrays.hashCode(_data);
    result = prime * result + _els;
    result = prime * result + Arrays.hashCode(_rowPtr);
    result = prime * result + _rows;
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
    PackedMatrix other = (PackedMatrix) obj;
    if (_cols != other._cols) {
      return false;
    }
    if (_rows != other._rows) {
      return false;
    }
    if (!Arrays.equals(_colCount, other._colCount)) {
      return false;
    }
    if (!Arrays.equals(_rowPtr, other._rowPtr)) {
      return false;
    }
    if (!Arrays.equals(_data, other._data)) {
      return false;
    }
    return true;
  }

}
