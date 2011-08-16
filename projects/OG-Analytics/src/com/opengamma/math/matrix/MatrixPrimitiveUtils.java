/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.apache.commons.lang.NotImplementedException;

/**
 *
 */
public class MatrixPrimitiveUtils {

  /**
   * Tests if an array is ragged
   * @param aMatrix an array of arrays
   * @return true if array is ragged, false if array is not!
   */
  public static boolean isRagged(double[][] aMatrix) {
    int rows = aMatrix.length;
    int cols = aMatrix[0].length;
    for (int i = 0; i < rows; i++) {
      if (aMatrix[i].length != cols) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests if an array is square
   * @param aMatrix an array of arrays
   * @return true if array is "square" (length of all rows == number of rows), false if array is not!
   */
  public static boolean isSquare(double[][] aMatrix) {
    int rows = aMatrix.length;
    for (int i = 0; i < rows; i++) {
      if (aMatrix[i].length != rows) {
        return false;
      }
    }
    return true;
  }

  /**
   * Computes the number of elements in an array of arrays
   * @param aMatrix the array of arrays who's elements are going to be counted
   * @return els the number of elements in the array of arrays
   */
  public static int getNumberOfElementsInArray(double[][] aMatrix) {
    int els = 0;
    for (int i = 0; i < aMatrix.length; i++) {
      els += aMatrix[i].length;
    }
    return els;
  }

  /**
   * Counts number of *true* nonzero elements in a vector
   * @param aVector which is the vector (array of doubles) being tested
   * @return tmp the number of nonzero elements in the vector
   */
  public static int numberOfNonZeroElementsInVector(double[] aVector) {
    int tmp = 0;
    for (int i = 0; i < aVector.length; i++) {
      if (Double.doubleToLongBits(aVector[i]) != 0L) {
        tmp++;
      }
    }
    return tmp;
  }

  /**
   * Counts the number of *true* nonzero elements in an array of arrays
   * @param aMatrix which is the array of arrays being tested
   * @return tmp the number of nonzero elements
   */
  public static int numberOfNonZeroElementsInMatrix(double[][] aMatrix) {
    int tmp = 0;
    for (int i = 0; i < aMatrix.length; i++) {
      tmp += numberOfNonZeroElementsInVector(aMatrix[i]);
    }
    return tmp;
  }

  /**
   * Test a vector to see if there is a block of contiguous nonzero values present.
   * @param aVector the vector to be tested
   * @return true if a contiguous block exists, false otherwise. If a vector is entirely composed of zeros it returns true as the data is contiguous but not present!
   */
  public static boolean arrayHasContiguousRowEntries(double[] aVector) {
    int nnz = numberOfNonZeroElementsInVector(aVector);
    if (nnz == 0) {
      return true;
    }
    if (nnz == 1) {
      return true;
    }
    int dataStartsAt = 0;
    for (int i = 0; i < aVector.length; i++) {
      if (Double.doubleToLongBits(aVector[i]) != 0L) {
        dataStartsAt = i;
        break;
      }
    }
    int dataEndsAt = aVector.length - 1;
    for (int i = aVector.length - 1; i > 0; i--) {
      if (Double.doubleToLongBits(aVector[i]) != 0L) {
        dataEndsAt = i;
        break;
      }
    }
    if (dataEndsAt - dataStartsAt + 1 == nnz) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Mangles a full matrix into something sane for packing as a symmetric matrix.
   * (basically knocks out the lower triangle).
   * @param aMatrix which is the array of arrays to be manipulated
   * @return a double array of arrays with the lower triangle set to zero
   */
  public static double[][] removeLowerTriangle(double[][] aMatrix) throws  IllegalArgumentException, NotImplementedException {
    if (isRagged(aMatrix)) {
      throw new NotImplementedException("Construction from ragged array is not implemented");
    }
    if (!isSquare(aMatrix)) {
      throw new IllegalArgumentException("Matrix is not square so removing lower triangle isn't clear cut enough to be implemented");
    }
    int rows = aMatrix.length;
    int cols = aMatrix[0].length;

    double[][] tmp = new double[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = i; j < cols; j++) {
        tmp[i][j] = aMatrix[i][j];
      }
    }
    return tmp;
  }

/**
 * Boolean on whether a matrix is upper triangular
 * @param aMatrix an array of arrays representation of the matrix to be tested.
 * @return boolean, true if matrix is Upper Triangular, false if matrix is not.
 * @throws IllegalArgumentException
 */
  public static boolean isUpperTriangular(double[][] aMatrix) throws  IllegalArgumentException {
    if (!isSquare(aMatrix)) {
      throw new IllegalArgumentException("Matrix is not square so removing lower triangle isn't clear cut enough to be implemented");
    }
    int rows = aMatrix.length;

    for (int i = 1; i < rows; i++) {
      for (int j = 0; j < i; j++) {
        if (Double.doubleToLongBits(aMatrix[i][j]) != 0) {
          return false;
        }
      }
    }
    return true;
  }

/**
 * Checks if a matrix is upper triangular and returns the matrix if true, throws and exception other wise.
 * @param aMatrix an array of arrays representation of the matrix to be tested.
 * @return the matrix referred to in the argument if the check passes
 */
  public static double[][] checkIsUpperTriangular(double[][] aMatrix) {
    if (MatrixPrimitiveUtils.isUpperTriangular(aMatrix)) {
      return aMatrix;
    } else {
      throw new IllegalArgumentException("Upper triangular matrix called on data that isn't upper triangular!");
    }
  }

}
