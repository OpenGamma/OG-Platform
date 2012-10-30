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
 * The DenseMatrix class provides access to the typically understood notion of a matrix, i.e. A Fully populated array.
 * TODO: decide at what level we check for things like null pointers
 */

public class DenseMatrix implements MatrixPrimitive {
  private double[] _data;
  private int _rows;
  private int _cols;
  private int[] _rowPtr;

  /**
   * Constructors
   */
  // construct from array
  /**
   * Constructs from an array of arrays representation
   * @param aMatrix is an n columns x m rows matrix stored as a row major array of arrays
   */
  public DenseMatrix(double[][] aMatrix) {
    // test if ragged
    if (MatrixPrimitiveUtils.isRagged(aMatrix)) {
      throw new NotImplementedException("Construction from ragged array not implemented");
    }

    _rows = aMatrix.length;
    // test if square
    if (MatrixPrimitiveUtils.isSquare(aMatrix)) {
      _cols = _rows;
    } else {
      _cols = aMatrix[0].length;
    }

    //malloc
    _data = new double[MatrixPrimitiveUtils.getNumberOfElementsInArray(aMatrix)];
    _rowPtr = new int[aMatrix.length];

    int ptr = 0;
    // flatten the matrix
    for (int i = 0; i < aMatrix.length; i++) {
      _rowPtr[i] = ptr;
      for (int j = 0; j < aMatrix[i].length; j++) {
        _data[ptr] = aMatrix[i][j];
        ptr++;
      }
    }
  }

  // construct from DoubleMatrix2D
  public DenseMatrix(DoubleMatrix2D aMatrix) {
    this(aMatrix.toArray());
  }

  /**
   * A blank dense matrix; contains nothing, nothing is set.
   */
  public DenseMatrix() {
  }

  // copy construction for row unwound data
  public DenseMatrix copyOnContructFromRowVector(double[] rowData, int rows, int columns) {
    Validate.notNull(rowData, "Null pointer passed to copy on constructor");
    Validate.isTrue(rows > 0);
    Validate.isTrue(columns > 0);
    final int len = rows * columns;
    Validate.isTrue(len == rowData.length);
    _data = new double[len];
    _rows = rows;
    _cols = columns;
    _rowPtr = new int[rows];
    for (int i = 0; i < rows; i++) {
      _rowPtr[i] = i * columns;
    }
    System.arraycopy(rowData, 0, _data, 0, len);
    return this;
  }

  // no copy construction for row unwound data
  public DenseMatrix noCopyOnContructFromRowVector(double[] rowData, int rows, int columns) {
    Validate.notNull(rowData, "Null pointer passed to NO copy on constructor");
    Validate.isTrue(rows > 0);
    Validate.isTrue(columns > 0);
    final int len = rows * columns;
    Validate.isTrue(len == rowData.length);
    _data = rowData;
    _rows = rows;
    _cols = columns;
    _rowPtr = new int[rows];    
    for (int i = 0; i < rows; i++) {
      _rowPtr[i] = i * columns;
    }    
    return this;
  }

  /**
   * Methods
   */

  /**
   * Gets the number of elements in the matrix (full population assumed).
   * @return the number of elements in the matrix
   */
  @Override
  public int getNumberOfElements() {
    return _data.length;
  }

  /**
   * @param indices The index of the entry within the matrix to be returned.
   * If a single index is given, it assumes ind2sub behaviour (index = i*rows+j) and returns that index
   * If a pair of indices are given, it assumes standard lookup behaviour and returns the index at the given matrix "coordinate".
   * @return the entry at index specified
   *    */
  @Override
  public Double getEntry(int... indices) {
    if (indices.length > 2) {
      throw new IndexOutOfBoundsException("Trying to access a 2D array representation with tuple>2 is forbidden!");
    } else if (indices.length == 2) {
      return _data[_rowPtr[indices[0]] + indices[1]];
    } else {
      return _data[indices[0]];
    }
  }

  @Override
  public double[] getFullRow(int index) {
    double[] tmp = new double[_cols];
    for (int i = 0; i < _cols; i++) {
      tmp[i] = _data[_rowPtr[index] + i];
    }
    return tmp;
  }

  @Override
  public double[] getFullColumn(int index) {
    double[] tmp = new double[_rows];
    for (int i = 0; i < _rows; i++) {
      tmp[i] = _data[index + i * _cols];
    }
    return tmp;
  }

  @Override
  public double[] getRowElements(int index) {
    return this.getFullRow(index);
  }

  @Override
  public double[] getColumnElements(int index) {
    return this.getFullColumn(index);
  }

  @Override
  public int getNumberOfNonZeroElements() {
    return MatrixPrimitiveUtils.numberOfNonZeroElementsInVector(_data);
  }

  /**
   * Gets the data
   * @return _data the DenseMatrix data in it's native storage format
   */
  public double[] getData() {
    return _data;
  }

  /**
   * Gets the number of rows
   * @return _rows the number of rows
   */
  public int getNumberOfRows() {
    return _rows;
  }

  /**
   * Gets the number of columns
   * @return _cols the number of columns
   */
  public int getNumberOfColumns() {
    return _cols;
  }

  /**
   * The to...'s
   */

  /**
   * @return tmp an array of arrays row major representation of the full matrix
   */
  @Override
  public double[][] toArray() {
    double[][] tmp = new double[_rows][_cols];
    for (int i = 0; i < _rows; i++) {
      for (int j = 0; j < _cols; j++) {
        tmp[i][j] = _data[_rowPtr[i] + j];
      }
    }
    return tmp;
  }

  @Override
  public String toString() {
    return "DenseMatrix:" +
        "\ndata = " + Arrays.toString(_data) +
        "\nrows = " + _rows +
        "\ncols = " + _cols +
        "\nrowPtr = " + Arrays.toString(_rowPtr);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _cols;
    result = prime * result + Arrays.hashCode(_data);
    result = prime * result + Arrays.hashCode(_rowPtr);
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
    DenseMatrix other = (DenseMatrix) obj;
    if (_cols != other._cols) {
      return false;
    }
    if (_rows != other._rows) {
      return false;
    }
    if (!Arrays.equals(_data, other._data)) {
      return false;
    }
    return true;
  }

}
