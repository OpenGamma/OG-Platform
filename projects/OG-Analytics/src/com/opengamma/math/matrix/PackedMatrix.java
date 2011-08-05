/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;

/**
 *
 */
public class PackedMatrix implements MatrixPrimitiveInterface  {
  private double[] _data;
  private int _rows;
  private int _cols;
  private int _els;
  private int[] _colCount;
  private int[] _rowPtr;

/**
 * Constructors
 */

  /**
   * Constructs from an array of arrays representation.
   * @param aMatrix is an n columns x m rows matrix stored as a row major array of arrays.
   */
  public PackedMatrix(double[][] aMatrix) {
    this(aMatrix, false, aMatrix.length, aMatrix[0].length);
  }

  /**
   * Constructs from an array of arrays representation.
   * @param aMatrix is an n columns x m rows matrix stored as a row major array of arrays.
   * @param allowZeros is set to allow the packing of zeros in the packed data structure.
   */
  public PackedMatrix(double[][] aMatrix, boolean allowZeros) {
    this(aMatrix, allowZeros, aMatrix.length, aMatrix[0].length);
  }

  /**
   * Constructs from array of arrays *but* allows zeros to be packed into the data structure. This is particularly useful for banded matrices in which
   * allowing some zero padding is beneficial in terms of making access patterns more simple.
   * @param aMatrix is an n columns x m rows matrix stored as a row major array of arrays.
   * @param allowZeros is set to allow the packing of zeros in the packed data structure.
   * @param rows is the number of rows in the matrix that is to be represented
   * @param cols is the number of columns in the matrix that is to be represented
   */
  public PackedMatrix(double[][] aMatrix, boolean allowZeros, int rows, int cols) {
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

    if (!allowZeros) {
      // make flat!
      for (int i = 0; i < _rows; i++) {
        isSet = false; // init each starting point as not being set and look for it.
        // test to ensure data is contiguous
        if (!MatrixPrimitiveUtils.arrayHasContiguousRowEntries(aMatrix[i])) {
          throw new IllegalArgumentException("Matrix given does not contain contiguous nonzero entries, error was thrown due to bad data on row " + i);
        }
        // flatten
        for (int j = 0; j < _cols; j++) { //for each col
          val = aMatrix[i][j]; // get the value

          if (Double.doubleToLongBits(val) != 0L) { // test if not zero
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
    } else { /** ALLOWING ZEROS TO BE PACKED INTO THE ARRAY, BEHAVIOUR IS SLIGHTLY DIFFERENT! */
      // make flat!
      for (int i = 0; i <  aMatrix.length; i++) {
        isSet = false; // init each starting point as not being set and look for it.
        // test to ensure data is contiguous
        if (!MatrixPrimitiveUtils.arrayHasContiguousRowEntries(aMatrix[i])) {
          throw new IllegalArgumentException("Matrix given does not contain contiguous nonzero entries, error was thrown due to bad data on row " + i);
        }
        // flatten
        for (int j = 0; j < aMatrix[i].length; j++) { // for each col
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
    }
    _data = Arrays.copyOfRange(tmp, 0, count);
  }


  @Override
  public int getNumberOfElements() {
    return _data.length;
  }

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

  @Override
  public double[] getFullRow(int index) {
    double[] tmp = new double[_cols];
    for (int i = 0; i < _cols; i++) {
      tmp[i] = getEntry(index, i);
    }
    return tmp;
  }

  @Override
  public double[] getFullColumn(int index) {
    double[] tmp = new double[_rows];
    for (int i = 0; i < _rows; i++) {
      tmp[i] = getEntry(i, index);
    }
    return tmp;
  }

  @Override
  public double[] getRowElements(int index) {
    int count = (_colCount[index + 1] - _colCount[index]);
    double[] tmp = new double[count];
    for (int i = 0; i < count; i++) {
      tmp[i] = _data[_colCount[index] + i];
    }
    return tmp;
  }

  @Override
  public double[] getColumnElements(int index) {
    return null;
  }

  @Override
  public int getNumberOfNonZeroElements() {
    return _data.length;
  }

  @Override
  public double[][] toArray() {
    return null;
  }

}
