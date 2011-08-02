/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

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
}
