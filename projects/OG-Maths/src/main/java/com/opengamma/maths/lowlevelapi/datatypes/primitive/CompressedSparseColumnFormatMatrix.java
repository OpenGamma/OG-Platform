/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.maths.lowlevelapi.functions.utilities.Max;

/**
 * Converts, or instantiates, a matrix to Compressed Sparse Column format (CSC). CSC is a near optimal method of storing sparse matrix data.
 * Only the non-zero components of the matrix are stored (note: there is no tolerance for testing zero to machine precision, zero is solely tested bitwise).
 * The CSC format requires three vectors:
 *  rowIdx - contains row indexes of elements
 *  colPtr - contains the column pointer information used to reassemble the rowIdx references in the correct place within the matrix
 *  values - contains the nonzero values of the sparse matrix in column-major order
 *
 *  For an example call and output see the unit test.
 *
 *  A "normal" path to CSC form is via the {@link SparseCoordinateFormatMatrix} this formatting path is taken to allow access to the
 *  Compressed Sparse Row (CSR) format with equal ease.
 *  The final format chosen (CSR or CSC) should reflect the cache optimal access pattern of the calling function data.
 */
public class CompressedSparseColumnFormatMatrix extends SparseMatrixType  {

  private double[] _values;
  private int[] _colPtr;
  private int[] _rowIdx;
  private int _els;
  private int _rows;
  private int _cols;
  private int _maxEntriesInAColumn;

  /* constructors */

  /**
   * Construct from DoubleMatrix2D type
   * @param m is a DoubleMatrix2D
   */
  public CompressedSparseColumnFormatMatrix(DoubleMatrix2D m) {
    Validate.notNull(m);

    //get number of elements
    _els = m.getNumberOfElements();

    // tmp arrays, in case we get in a fully populated matrix, intelligent design upstream should ensure that this is overkill!
    double[] dataTmp = new double[_els];
    int[] colPtrTmp = new int[_els + 1];
    int[] rowIdxTmp = new int[_els];

    // we need unwind the array m into coordinate form
    int ptr = 0;
    int i;
    int localMaxEntrisInACol;
    _maxEntriesInAColumn = -1;
    for (i = 0; i < m.getNumberOfColumns(); i++) {
      colPtrTmp[i] = ptr;
      localMaxEntrisInACol = 0;
      for (int j = 0; j < m.getNumberOfRows(); j++) {
        if (Double.doubleToLongBits(m.getEntry(j, i)) != 0L) {
          rowIdxTmp[ptr] = j;
          dataTmp[ptr] = m.getEntry(j, i);
          ptr++;
          localMaxEntrisInACol++;
        }
      }
      if (localMaxEntrisInACol > _maxEntriesInAColumn) {
        _maxEntriesInAColumn = localMaxEntrisInACol;
      }
    }
    colPtrTmp[i] = ptr;

    // return correct 0 to correct length of the vector buffers
    _values = Arrays.copyOfRange(dataTmp, 0, ptr);
    _colPtr = Arrays.copyOfRange(colPtrTmp, 0, i + 1); // yes, the +1 is correct, it allows the computation of the number of elements in the final row!
    _rowIdx = Arrays.copyOfRange(rowIdxTmp, 0, ptr);
    _rows = m.getNumberOfRows();
    _cols = m.getNumberOfColumns();
  }

  /**
   * Construct from SparseCoordinateFormatMatrix type
   * @param m is a SparseCoordinateFormatMatrix
   */
  public CompressedSparseColumnFormatMatrix(SparseCoordinateFormatMatrix m) {
    Validate.notNull(m);
    _els = m.getNumberOfElements();
    int[] colPtrTmp = new int[_els];
    int[] rowIdxTmp = new int[_els];
    double[] valuesTmp = new double[_els];
    double val;
    int ptr = 0, i;
    int localMaxEntrisInACol;
    _maxEntriesInAColumn = -1;
    for (i = 0; i < m.getNumberOfColumns(); i++) {
      colPtrTmp[i] = ptr;
      localMaxEntrisInACol = 0;
      for (int j = 0; j < m.getNumberOfRows(); j++) {
        val = m.getEntry(j, i);
        if (Double.doubleToLongBits(val) != 0L) {
          valuesTmp[ptr] = val;
          rowIdxTmp[ptr] = j;
          ptr++;
          localMaxEntrisInACol++;
        }
      }
      if (localMaxEntrisInACol > _maxEntriesInAColumn) {
        _maxEntriesInAColumn = localMaxEntrisInACol;
      }
    }
    colPtrTmp[i] = ptr; // adds in last entry to close access fields

    _values = Arrays.copyOfRange(valuesTmp, 0, ptr);
    _rowIdx = Arrays.copyOfRange(rowIdxTmp, 0, ptr);
    _colPtr = Arrays.copyOfRange(colPtrTmp, 0, i + 1); // yes, the +1 is correct!
    _rows = m.getNumberOfRows();
    _cols = m.getNumberOfColumns();
  }

  /**
   * Construct from a (double) array of arrays
   * @param m is a (double) array of arrays
   */
  public CompressedSparseColumnFormatMatrix(double[][] m) {
    this(new DoubleMatrix2D(m));
  }
  
  
  /**
   * Construct from coordinate inputs
   * @param x x-coordinates of data points
   * @param y y-coordinates of data points
   * @param values value of data points
   */
  public CompressedSparseColumnFormatMatrix(int[] x, int[] y, double[] values) {
    this(new SparseCoordinateFormatMatrix(x, y, values, (Max.value(y) + 1), (Max.value(x) + 1)));
  }

  /**
   * Construct from coordinate inputs
   * @param x x-coordinates of data points
   * @param y y-coordinates of data points
   * @param values value of data points
   * @param m the number of rows the CSC matrix should have
   * @param n the number of columns the CSC matrix should have
   */
  public CompressedSparseColumnFormatMatrix(int[] x, int[] y, double[] values, int m, int n) {
    this(new SparseCoordinateFormatMatrix(x, y, values, m, n));
  }  

  /* methods */

  /**
   *  Gets the row indexes that can be looked up by the column pointer
   *  @return _rowIdx, the row indexes
   */
  public int[] getRowIndex() {
    return _rowIdx;
  }

  /**
   * Gets the column pointer (not actually a pointer, but would be in C)
   * @return _colPtr, the column pointer
   */
  public int[] getColumnPtr() {
    return _colPtr;
  }

  /**
   * Gets the non-zero values in the matrix, i.e. the values that are worth storing
   * @return _values, the non-zero values
   */
  public double[] getNonZeroElements() {
    return _values;
  }

  /**
   * Gets the number of rows in the matrix (is not equal to count(unique(rowPtr)) as matrix could be singular/have row of zeros))
   * @return _rows, the number of rows corresponding to a full matrix representation of the compressed matrix
   */
  @Override
  public int getNumberOfRows() {
    return _rows;
  }

  /**
   * Gets the number of columns in the matrix (is not equal to count(unique(colIdx)) as matrix could be singular/have column of zeros))
   * @return _cols, the number of columns corresponding to a full matrix representation of the compressed matrix
   */
  @Override
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
  public DoubleMatrix2D toDenseMatrix() {
    return new DoubleMatrix2D(this.toArray());
  }

  /**
   * Converts matrix to a Full Matrix representation (undoes the sparse compression)
   * @return tmp, an array of arrays
   */
  @Override
  public double[][] toArray() {
    double[][] tmp = new double[_rows][_cols];
    for (int ir = 0; ir < _cols; ir++) {
        //translate an index and see if it exists, if it doesn't then return 0
      for (int i = _colPtr[ir]; i <= _colPtr[ir + 1] - 1; i++) { // loops through elements of correct column
        tmp[_rowIdx[i]][ir] = _values[i];
      }
    }
    return tmp;
  }

  @Override
  public int getNumberOfElements() {
    return _els;
  }


  @Override
  public double[] getFullColumn(int index) {
    double[] tmp = new double[_cols];
    for (int i = _colPtr[index]; i <= _colPtr[index + 1] - 1; i++) { // loops through elements of correct column
      tmp[_rowIdx[i]] = _values[i];
    }
    return tmp;
  }

  @Override
  public double[] getFullRow(int index) { // getting rows in CSC form is generally bad
    double[] tmp = new double[_rows];
    for (int i = 0; i < _rows; i++) {
      tmp[i] = this.getEntry(index, i);
    }
    return tmp;
  }

  @Override
  public double[] getColumnElements(int index) {
    double[] tmp = new double[_cols];
    int ptr = 0;
    for (int i = _colPtr[index]; i <= _colPtr[index + 1] - 1; i++) { // loops through elements of correct column
      tmp[ptr] = _values[i];
      ptr++;
    }
    return Arrays.copyOfRange(tmp, 0, ptr);
  }

  @Override
  public double[] getRowElements(int index) { // getting rows in CSC form is generally bad
    double[] tmp = new double[_cols];
    double val;
    int ptr = 0;
    for (int i = 0; i < _cols; i++) {
      val = this.getEntry(index, i);
      if (Double.doubleToLongBits(val) != 0) {
        tmp[ptr] = val;
        ptr++;
      }

    }
    return Arrays.copyOfRange(tmp, 0, ptr);
  }

  @Override
  public int getNumberOfNonZeroElements() {
    return _values.length;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getMaxNonZerosInSignificantDirection() {
    return _maxEntriesInAColumn;
  }


  @Override
  public Double getEntry(int... indices) {
    //translate an index and see if it exists, if it doesn't then return 0
    for (int i = _colPtr[indices[1]]; i <= _colPtr[indices[1] + 1] - 1; i++) { // loops through elements of correct column
      // looks for col index
      if (_rowIdx[i] == indices[0]) {
        return _values[i];
      }
    }
    return 0.0;
  }

  @Override
  public String toString() {
    return "\nvalues=" + Arrays.toString(_values) +
        "\nrowInd=" + Arrays.toString(_rowIdx) +
        "\ncolPtr=" + Arrays.toString(_colPtr) +
        "\nels=" + _els;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_rowIdx);
    result = prime * result + _cols;
    result = prime * result + _els;
    result = prime * result + Arrays.hashCode(_colPtr);
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
    CompressedSparseColumnFormatMatrix other = (CompressedSparseColumnFormatMatrix) obj;
    if (_cols != other._cols) {
      return false;
    }
    if (_rows != other._rows) {
      return false;
    }
    if (!Arrays.equals(_colPtr, other._colPtr)) {
      return false;
    }
    if (!Arrays.equals(_rowIdx, other._rowIdx)) {
      return false;
    }
    if (!Arrays.equals(_values, other._values)) {
      return false;
    }
    return true;
  }

}


