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
public class CompressedSparseRowFormatMatrix implements Matrix<Double> {

  private double[] _values;
  private int[] _colIdx;
  private int[] _rowPtr;
  private int _els;

// constructors
  public CompressedSparseRowFormatMatrix(DoubleMatrix2D m) {
    Validate.notNull(m);

    //get number of elements
    _els = m.getNumberOfElements();

    // tmp arrays, in case we get in a fully populated matrix, intelligent design upstream should ensure that this is overkill!
    double[] dataTmp = new double[_els];
    int[] colIndTmp = new int[_els];
    int[] rowPtrTmp = new int[_els];

    // we need unwind the array m into coordinate form
    int ptr = 0;
    int i;
    for (i = 0; i < m.getNumberOfRows(); i++) {
      rowPtrTmp[i] = ptr;
      for (int j = 0; j < m.getNumberOfColumns(); j++) {
        if (Double.doubleToLongBits(m.getEntry(i, j)) != 0L) {
          colIndTmp[ptr] = j;
          dataTmp[ptr] = m.getEntry(i, j);
          ptr++;
        }
      }
    }

    rowPtrTmp[i] = ptr;

    _values = Arrays.copyOfRange(dataTmp, 0, ptr);
    _colIdx = Arrays.copyOfRange(colIndTmp, 0, ptr);
    _rowPtr = Arrays.copyOfRange(rowPtrTmp, 0, i + 1); // yes, the +1 is correct!
  }

//constructors
  public CompressedSparseRowFormatMatrix(SparseCoordinateFormatMatrix m) {
    Validate.notNull(m);

    //get number of elements
    _els = m.getNumberOfElements();

    // tmp arrays, in case we get in a fully populated matrix, intelligent design upstream should ensure that this is overkill!
    int[] rowPtrTmp = new int[_els];
    double[] dataTmp = null;
    int[] colIdxTmp = null;

    // we need unwind the array m into coordinate form
    int ptr = 0;
    int i;
    for (i = 0; i < m.getNumberOfRows(); i++) {
      rowPtrTmp[i] = ptr;
      for (int j = 0; j < m.getNumberOfColumns(); j++) {
        if (Double.doubleToLongBits(m.getEntry(i, j)) != 0L) {
          ptr++;
        }
      }
    }

    rowPtrTmp[i] = ptr;

    _values = Arrays.copyOfRange(dataTmp, 0, ptr);
    _colIdx = Arrays.copyOfRange(colIdxTmp, 0, ptr);
    _rowPtr = Arrays.copyOfRange(rowPtrTmp, 0, i + 1); // yes, the +1 is correct!
  }


// methods
  public int[] getColumnIndex() {
    return _colIdx;
  }

  public int[] getRowPtr() {
    return _rowPtr;
  }

  public double[] getNonZeroValues() {
    return _values;
  }

  @Override
  public int getNumberOfElements() {
    return 0;
  }

  @Override
  public Double getEntry(int... indices) {
    return null;
  }


  @Override
  public String toString() {
    return "\nvalues=" + Arrays.toString(_values) +
      "\ncolInd=" + Arrays.toString(_colIdx) +
      "\nrowPtr=" + Arrays.toString(_rowPtr) +
      "\nels=" + _els;
  }

}
