/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.datatypes.primitive;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.maths.lowlevelapi.datatypes.primitive.MatrixPrimitiveUtils;

/**
 * The OGIndex class provides access to the typically understood notion of a matrix, i.e. A Fully populated array.
 */
public class OGIndexType {
  private int[] _data;
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
  public OGIndexType(int[][] aMatrix) {
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
    _data = new int[MatrixPrimitiveUtils.getNumberOfElementsInArray(aMatrix)];
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

  /**
   * Methods
   */

  /**
   * Gets the number of elements in the matrix (full population assumed).
   * @return the number of elements in the matrix
   */
  public int getNumberOfElements() {
    return _data.length;
  }

  /**
   * @param indices The index of the entry within the matrix to be returned.
   * If a single index is given, it assumes ind2sub behaviour (index = i*rows+j) and returns that index
   * If a pair of indices are given, it assumes standard lookup behaviour and returns the index at the given matrix "coordinate".
   * @return the entry at index specified
   *    */
  public int getEntry(int... indices) {
    if (indices.length > 2) {
      throw new IndexOutOfBoundsException("Trying to access a 2D array representation with tuple>2 is forbidden!");
    } else if (indices.length == 2) {
      return _data[_rowPtr[indices[0]] + indices[1]];
    } else {
      return _data[indices[0]];
    }
  }

  public OGIndexType getFullRow(int index) {
    int[] tmp = new int[_cols];
    for (int i = 0; i < _cols; i++) {
      tmp[i] = _data[_rowPtr[index] + i];
    }
    int[][] tmp2 = {tmp };
    return new OGIndexType(tmp2);
  }

  public OGIndexType getFullColumn(int index) {
    int[] tmp = new int[_rows];
    for (int i = 0; i < _rows; i++) {
      tmp[i] = _data[index + i * _cols];
    }
    int[][] tmp2 = {tmp };
    return new OGIndexType(tmp2);
  }

  public OGIndexType getRowElements(int index) {
    return this.getFullRow(index);
  }

  public OGIndexType getColumnElements(int index) {
    return this.getFullColumn(index);
  }

  public int getNumberOfNonZeroElements() {
    return MatrixPrimitiveUtils.numberOfNonZeroElementsInVector(_data);
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
   * Gets the data
   * @return _data the OGIndex data in it's native storage format
   */
  public int[] getData() {
    return Arrays.copyOf(_data, _data.length);
  }

  /**
   * The to...'s
   */

  /**
   * @return tmp an array of arrays row major representation of the OGIndex matrix
   */
  public int[][] toArray() {
    int[][] tmp = new int[_rows][_cols];
    for (int i = 0; i < _rows; i++) {
      for (int j = 0; j < _cols; j++) {
        tmp[i][j] = _data[_rowPtr[i] + j];
      }
    }
    return tmp;
  }


  /**
   * ToString for pretty printing
   * @return A string representation of the matrix
   */
  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append("\n{\n");
    for (int i = 0; i < _rows; i++) {
      for (int j = 0; j < _cols; j++) {
        sb.append(String.format("%12d ", _data[i * _cols + j]));
      }
      sb.append("\n");
    }
    sb.append("}");
    return sb.toString();
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
    OGIndexType other = (OGIndexType) obj;
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
