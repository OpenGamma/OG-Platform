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
  private int _maxEntriesInARow; // used to decide whether 16bit ints can be used as index later

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
    int[] rowPtrTmp = new int[_els + 1];

    // we need unwind the array m into coordinate form
    int localmaxEntriesInARow;
    _maxEntriesInARow = -1; // set max entries in a row negative, so that maximiser will work
    int ptr = 0;
    int i;
    for (i = 0; i < m.getNumberOfRows(); i++) {
      rowPtrTmp[i] = ptr;
      localmaxEntriesInARow = 0;
      for (int j = 0; j < m.getNumberOfColumns(); j++) {
        if (Double.doubleToLongBits(m.getEntry(i, j)) != 0L) {
          localmaxEntriesInARow++;
          colIndTmp[ptr] = j;
          dataTmp[ptr] = m.getEntry(i, j);
          ptr++;
        }
      }
      if (localmaxEntriesInARow > _maxEntriesInARow) { // is the number of entries on this row the largest?
        _maxEntriesInARow = localmaxEntriesInARow;
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
    int[] rowPtrTmp = new int[_els > 1 ? _els : 2];
    int localmaxEntriesInARow;
    _maxEntriesInARow = -1; // set max entries in a row negative, so that maximiser will work
    int ptr = 0;
    int i;
    for (i = 0; i < m.getNumberOfRows(); i++) {
      rowPtrTmp[i] = ptr;
      localmaxEntriesInARow = 0;
      for (int j = 0; j < m.getNumberOfColumns(); j++) {
        if (Double.doubleToLongBits(m.getEntry(i, j)) != 0L) {
          localmaxEntriesInARow++;
          ptr++;
        }
      }
      if (localmaxEntriesInARow > _maxEntriesInARow) { // is the number of entries on this row the largest?
        _maxEntriesInARow = localmaxEntriesInARow;
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

  /**
   * Construct from coordinate inputs
   * @param x x-coordinates of data points
   * @param y y-coordinates of data points
   * @param values value of data points
   */
  public CompressedSparseRowFormatMatrix(int[] x, int[] y, double[] values) {
    this(new SparseCoordinateFormatMatrix(x, y, values, (Max.value(y) + 1), (Max.value(x) + 1)));
  }

  /**
   * Construct from coordinate inputs
   * @param x x-coordinates of data points
   * @param y y-coordinates of data points
   * @param values value of data points
   * @param m the number of rows the CSR matrix should have
   * @param n the number of columns the CSR matrix should have
   */
  public CompressedSparseRowFormatMatrix(int[] x, int[] y, double[] values, int m, int n) {
    this(new SparseCoordinateFormatMatrix(x, y, values, m, n));
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
  public double[] getNonZeroElements() {
    return _values;
  }

  /**
   * Gets the non-zero values in the matrix, i.e. the values that are worth storing.
   * Method is for unity with other matrix types and simply redirects to getNonZeroElements();
   * @return _values, the non-zero values
   */
  public double[] getData() {
    return getNonZeroElements();
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
   * {@inheritDoc}
   */
  @Override
  public int getMaxNonZerosInSignificantDirection() {
    return _maxEntriesInARow;
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
  public double[] getFullRow(int index) {
    double[] tmp = new double[_cols];
    for (int i = _rowPtr[index]; i <= _rowPtr[index + 1] - 1; i++) { // loops through elements of correct row
      tmp[_colIdx[i]] = _values[i];
    }
    return tmp;
  }

  @Override
  // column slicing CSR is a nightmare, implemented for completeness, but really not worth actually using unless desperate. Use COO or CSC instead.
  public double[] getFullColumn(int index) {
    double[] tmp = new double[_rows];
    for (int i = 0; i < _rows; i++) {
      tmp[i] = this.getEntry(i, index);
    }
    return tmp;
  }

  @Override
  public double[] getRowElements(int index) {
    double[] tmp = new double[_cols];
    int ptr = 0;
    for (int i = _rowPtr[index]; i <= _rowPtr[index + 1] - 1; i++) { // loops through elements of correct row
      tmp[ptr] = _values[i];
      ptr++;
    }
    return Arrays.copyOfRange(tmp, 0, ptr);
  }

  @Override
  // again, column slicing CSR is a bad idea and essentially requires multiple brute forces. Store the matrix differently if you are thinking about doing this a lot
  public double[] getColumnElements(int index) {
    double[] tmp = new double[_cols];
    double val;
    int ptr = 0;
    for (int i = 0; i < _cols; i++) {
      val = this.getEntry(i, index);
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

}
