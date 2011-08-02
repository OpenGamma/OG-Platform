/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * Converts, or instantiates, a matrix to Compressed Sparse Row format (CSR). CSR is a near optimal method of storing sparse matrix data.
 * Only the non-zero components of the matrix are stored (note: there is no tolerance for testing zero to machine precision, zero is solely tested bitwise).
 * The CSR format requires three vectors:
 *  colIdx - contains column indexes of elements
 *  rowPtr - contains the row pointer information used to reassemble the colIdx references in the correct place within the matrix
 *  values - contains the nonzero values of the sparse matrix in row-major order
 *
 *  For an example call and output see the unit test.
 *
 *  A "normal" path to CSR form is via the {@link SparseCoordinateFormatMatrix} this formatting path is taken to allow access to the
 *  Compressed Sparse Column (CSC) format with equal ease.
 *  The final format chosen (CSR or CSC) should reflect the cache optimal access pattern of the calling function data.
 */

public class CompressedSparseRowFormatMatrix extends SparseMatrixType {

  private double[] _values;
  private int[] _colIdx;
  private int[] _rowPtr;
  private int _els;
  private int _rows;
  private int _cols;

  /* constructors */

  /**
   * Construct from DoubleMatrix2D type
   * @param m is a DoubleMatrix2D
   */
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

    // return correct 0 to correct length of the vector buffers
    _values = Arrays.copyOfRange(dataTmp, 0, ptr);
    _colIdx = Arrays.copyOfRange(colIndTmp, 0, ptr);
    _rowPtr = Arrays.copyOfRange(rowPtrTmp, 0, i + 1); // yes, the +1 is correct, it allows the computation of the number of elements in the final row!
    _rows = m.getNumberOfRows();
    _cols = m.getNumberOfColumns();
  }

  /**
   * Construct from SparseCoordinateFormatMatrix type
   * @param m is a SparseCoordinateFormatMatrix
   */
  public CompressedSparseRowFormatMatrix(SparseCoordinateFormatMatrix m) {
    Validate.notNull(m);
    _els = m.getNumberOfElements();
    int[] rowPtrTmp = new int[_els];
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
    _values = Arrays.copyOfRange(m.getNonZeroEntries(), 0, ptr);
    _colIdx = Arrays.copyOfRange(m.getColumnCoordinates(), 0, ptr);
    _rowPtr = Arrays.copyOfRange(rowPtrTmp, 0, i + 1); // yes, the +1 is correct!
    _rows = m.getNumberOfRows();
    _cols = m.getNumberOfColumns();
  }

  /**
   * Construct from a (double) array of arrays
   * @param m is a (double) array of arrays
   */
  public CompressedSparseRowFormatMatrix(double[][] m) {
    this(new DoubleMatrix2D(m));
  }

  /* methods */

  /**
   *  Gets the column indexes that can be looked up by the row pointer
   *  @return _colIdx, the column indexes
   */
  public int[] getColumnIndex() {
    return _colIdx;
  }

  /**
   * Gets the row pointer (not actually a pointer, but would be in C)
   * @return _rowPtr, the row pointer
   */
  public int[] getRowPtr() {
    return _rowPtr;
  }

  /**
   * Gets the non-zero values in the matrix, i.e. the values that are worth storing
   * @return _values, the non-zero values
   */
  public double[] getNonZeroValues() {
    return _values;
  }

  /**
   * Gets the number of rows in the matrix (is not equal to count(unique(rowPtr)) as matrix could be singular/have row of zeros))
   * @return _rows, the number of rows corresponding to a full matrix representation of the compressed matrix
   */
  public int getNumberOfRows() {
    return _rows;
  }

  /**
   * Gets the number of columns in the matrix (is not equal to count(unique(colIdx)) as matrix could be singular/have column of zeros))
   * @return _cols, the number of columns corresponding to a full matrix representation of the compressed matrix
   */
  public int getNumberOfColumns() {
    return _cols;
  }

 /**
  * Gets the number of non-zero elements in the matrix
  * @return _values.length, the number of non-zero elements in the matrix
  */
  public int getNumberOfNonzeroElements() {
    return _values.length;
  }

/**
 * Converts matrix to a Full Matrix representation (undoes the sparse compression)
 * @return tmp, a DoubleMatrix2D
 */
  public DoubleMatrix2D toFullMatrix() {
    return new DoubleMatrix2D(this.toArray());
  }

  /**
   * Converts matrix to a Full Matrix representation (undoes the sparse compression)
   * @return tmp, an array of arrays
   */
  @Override
  public double[][] toArray() {
    double[][] tmp = new double[_rows][_cols];
    for (int ir = 0; ir < _rows; ir++) {
        //translate an index and see if it exists, if it doesn't then return 0
      for (int i = _rowPtr[ir]; i <= _rowPtr[ir + 1] - 1; i++) { // loops through elements of correct row
        tmp[ir][_colIdx[i]] = _values[i];
      }
    }
    return tmp;
  }

  @Override
  public int getNumberOfElements() {
    return _els;
  }

  @Override
  public Double getEntry(int... indices) {
    //translate an index and see if it exists, if it doesn't then return 0
    for (int i = _rowPtr[indices[0]]; i <= _rowPtr[indices[0] + 1] - 1; i++) { // loops through elements of correct row
      // looks for col index
      if (_colIdx[i] == indices[1]) {
        return _values[i];
      }
    }
    return 0.0;
  }

  @Override
  public String toString() {
    return "\nvalues=" + Arrays.toString(_values) +
        "\ncolInd=" + Arrays.toString(_colIdx) +
        "\nrowPtr=" + Arrays.toString(_rowPtr) +
        "\nels=" + _els;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_colIdx);
    result = prime * result + _cols;
    result = prime * result + _els;
    result = prime * result + Arrays.hashCode(_rowPtr);
    result = prime * result + _rows;
    result = prime * result + Arrays.hashCode(_values);
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
    CompressedSparseRowFormatMatrix other = (CompressedSparseRowFormatMatrix) obj;
    if (_cols != other._cols) {
      return false;
    }
    if (_rows != other._rows) {
      return false;
    }
    if (!Arrays.equals(_colIdx, other._colIdx)) {
      return false;
    }
    if (!Arrays.equals(_rowPtr, other._rowPtr)) {
      return false;
    }
    if (!Arrays.equals(_values, other._values)) {
      return false;
    }
    return true;
  }

  @Override
  public double[] getFullRow(int index) {
    return null;
  }

  @Override
  public double[] getFullColumn(int index) {
    return null;
  }

  @Override
  public double[] getRowElements(int index) {
    return null;
  }

  @Override
  public double[] getColumnElements(int index) {
    return null;
  }

  @Override
  public int getNumberOfNonZeroElements() {
    return 0;
  }



}

