/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import com.opengamma.analytics.math.matrix.Matrix;

/**
 *
 */
public interface MatrixPrimitive extends Matrix<Double> {

/**
 * Gets a full row of the matrix.
 * @param index the index of the row required.
 * @return a double[] vector containing the entries from row "index" were the matrix considered in it's full representation.
 */
  double[] getFullRow(int index);

  /**
   * Gets a full column of the matrix.
   * @param index the index of the column required.
   * @return a double[] vector containing the entries from row "index" were the matrix considered in it's full representation.
   */
  double[] getFullColumn(int index);

  /**
   * Gets a the nonzero entries on a specified row of the matrix.
   * @param index the index of the row required.
   * @return a double[] vector containing the nonzero entries from row "index".
   */
  double[] getRowElements(int index);

  /**
   * Gets a the nonzero entries on a specified column of the matrix.
   * @param index the index of the column required.
   * @return a double[] vector containing the nonzero entries from column "index".
   */
  double[] getColumnElements(int index);

  /**
   * Gets a the nonzero entries on a specified column of the matrix.
   * @return the number of non zero elements in the matrix
   */
  int getNumberOfNonZeroElements();

  /**
   * Converts the matrix to an array of arrays representation of the matrix, the returned matrix is "full".
   *  i.e. if the matrix is not fully populated, zero padding is added as needed to create a full matrix.
   *  @return an array of arrays representation of the Matrix (populated and padded to ensure the array is "full".
   */
  double[][] toArray();

  /**
   * Gets the number of rows in the matrix.
   * @return the number of rows.
   */
  int getNumberOfRows();

  /**
   * Gets the number of rows in the matrix.
   * @return the number of columns.
   */
  int getNumberOfColumns();

}
