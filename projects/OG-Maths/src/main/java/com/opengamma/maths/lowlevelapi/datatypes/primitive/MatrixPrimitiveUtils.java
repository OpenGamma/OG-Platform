/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import static org.testng.AssertJUnit.assertNotNull;

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
   * Tests if an array is ragged
   * @param aMatrix an array of arrays
   * @return true if array is ragged, false if array is not!
   */
  public static boolean isRagged(int[][] aMatrix) {
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
   * Tests if an array is square
   * @param aMatrix an array of arrays
   * @return true if array is "square" (length of all rows == number of rows), false if array is not!
   */
  public static boolean isSquare(int[][] aMatrix) {
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
   * Computes the number of elements in an array of arrays
   * @param aMatrix the array of arrays who's elements are going to be counted
   * @return els the number of elements in the array of arrays
   */
  public static int getNumberOfElementsInArray(int[][] aMatrix) {
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
   * Counts number of *true* nonzero elements in a vector
   * @param aVector which is the vector (array of doubles) being tested
   * @return tmp the number of nonzero elements in the vector
   */
  public static int numberOfNonZeroElementsInVector(int[] aVector) {
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
   * Counts the number of *true* nonzero elements in an array of arrays
   * @param aMatrix which is the array of arrays being tested
   * @return tmp the number of nonzero elements
   */
  public static int numberOfNonZeroElementsInMatrix(int[][] aMatrix) {
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
      throw new IllegalArgumentException("Matrix is not square so the notion of Upper Triangular isn't clear cut enough to be implemented");
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
 * Checks if a matrix is upper triangular and returns the matrix if true, throws and exception otherwise.
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


/**
 * Boolean on whether a matrix is lower triangular
 * @param aMatrix an array of arrays representation of the matrix to be tested.
 * @return boolean, true if matrix is Lower Triangular, false if matrix is not.
 * @throws IllegalArgumentException
 */
  public static boolean isLowerTriangular(double[][] aMatrix) throws  IllegalArgumentException {
    if (!isSquare(aMatrix)) {
      throw new IllegalArgumentException("Matrix is not square so the notion of Lower Triangular isn't clear cut enough to be implemented");
    }
    int rows = aMatrix.length;

    for (int i = 0; i < rows; i++) {
      for (int j = i + 1; j < rows; j++) {
        if (Double.doubleToLongBits(aMatrix[i][j]) != 0) {
          return false;
        }
      }
    }
    return true;
  }

/**
 * Checks if a matrix is lower triangular and returns the matrix if true, throws and exception otherwise.
 * @param aMatrix an array of arrays representation of the matrix to be tested.
 * @return the matrix referred to in the argument if the check passes
 */
  public static double[][] checkIsLowerTriangular(double[][] aMatrix) {
    if (MatrixPrimitiveUtils.isLowerTriangular(aMatrix)) {
      return aMatrix;
    } else {
      throw new IllegalArgumentException("Lower triangular matrix called on data that isn't lower triangular!");
    }
  }


/**
 * Boolean on whether a matrix is upper Hessenberg
 * @param aMatrix an array of arrays representation of the matrix to be tested.
 * @return boolean, true if matrix is upper Hessenberg, false if matrix is not.
 * @throws IllegalArgumentException
 */
  public static boolean isUpperHessenberg(double[][] aMatrix) throws  IllegalArgumentException {
    if (!isSquare(aMatrix)) {
      throw new IllegalArgumentException("Matrix is not square so the notion of Upper Hessenberg isn't clear cut enough to be implemented");
    }
    int rows = aMatrix.length;

    for (int i = 2; i < rows; i++) {
      for (int j = 0; j < i - 1; j++) {
        if (Double.doubleToLongBits(aMatrix[i][j]) != 0) {
          return false;
        }
      }
    }
    return true;
  }

/**
 * Checks if a matrix is upper triangular and returns the matrix if true, throws and exception otherwise.
 * @param aMatrix an array of arrays representation of the matrix to be tested.
 * @return the matrix referred to in the argument if the check passes
 */
  public static double[][] checkIsUpperHessenberg(double[][] aMatrix) {
    if (MatrixPrimitiveUtils.isUpperHessenberg(aMatrix)) {
      return aMatrix;
    } else {
      throw new IllegalArgumentException("Upper Hessenberg matrix called on data that isn't upper Hessenberg!");
    }
  }


/**
 * Boolean on whether a matrix is lower Hessenberg.
 * @param aMatrix an array of arrays representation of the matrix to be tested.
 * @return boolean, true if matrix is Lower Hessenberg, false if matrix is not.
 * @throws IllegalArgumentException
 */
  public static boolean isLowerHessenberg(double[][] aMatrix) throws  IllegalArgumentException {
    if (!isSquare(aMatrix)) {
      throw new IllegalArgumentException("Matrix is not square so the notion of Lower Hessenberg isn't clear cut enough to be implemented");
    }
    int rows = aMatrix.length;

    for (int i = 0; i < rows - 1; i++) {
      for (int j = i + 2; j < rows; j++) {
        if (Double.doubleToLongBits(aMatrix[i][j]) != 0) {
          return false;
        }
      }
    }
    return true;
  }

/**
 * Checks if a matrix is lower Hessenberg and returns the matrix if true, throws and exception otherwise.
 * @param aMatrix an array of arrays representation of the matrix to be tested.
 * @return the matrix referred to in the argument if the check passes
 */
  public static double[][] checkIsLowerHessenberg(double[][] aMatrix) {
    if (MatrixPrimitiveUtils.isLowerHessenberg(aMatrix)) {
      return aMatrix;
    } else {
      throw new IllegalArgumentException("Lower Hessenberg matrix called on data that isn't lower Hessenberg!");
    }
  }



/**
 * Checks if a matrix is tri-diagonal and returns the matrix if true, throws and exception otherwise.
 * @param aMatrix an array of arrays representation of the matrix to be tested.
 * @return the matrix referred to in the argument if the check passes
 */
  public static double[][] checkIsTriDiag(double[][] aMatrix) {
    if (MatrixPrimitiveUtils.isTriDiag(aMatrix)) {
      return aMatrix;
    } else {
      throw new IllegalArgumentException("TriDiag matrix called on data that isn't Tri-Diagonal!");
    }
  }

/**
 * Boolean on whether a matrix is tri-Diagonal.
 * @param aMatrix an array of arrays representation of the matrix to be tested.
 * @return boolean, true if matrix is tri-Diagonal, false if matrix is not.
 * @throws IllegalArgumentException
 */
  public static boolean isTriDiag(double[][] aMatrix) throws IllegalArgumentException {
    if (!isSquare(aMatrix)) {
      throw new IllegalArgumentException("Matrix is not square so the notion of Tri-Diagonal isn't clear cut enough to be implemented");
    }
    final int rows = aMatrix.length;

    // test first row
    for (int i = 2; i < rows; i++) {
      if (Double.doubleToLongBits(aMatrix[0][i]) != 0) {
        return false;
      }
    }

    // tests members of each row that should be empty to ensure they are!
    for (int i = 1; i < rows - 1; i++) {
      // first check the elements on the LHS of the tridiag entries
      for (int k = 0; k < i - 1; k++) {
        if (Double.doubleToLongBits(aMatrix[i][k]) != 0) {
          return false;
        }
      }
      // second check the elements on the RHS of the tridiag entries
      for (int k = i - 1 + 3; k < rows; k++) {
        if (Double.doubleToLongBits(aMatrix[i][k]) != 0) {
          return false;
        }
      }
    }

    // test last row
    for (int i = 0; i < (rows - 2); i++) {
      if (Double.doubleToLongBits(aMatrix[rows - 1][i]) != 0) {
        return false;
      }
    }

    return true;
  }


  /**
   * Checks if a matrix is N-diagonal and returns the matrix if true, throws and exception otherwise.
   * @param aMatrix an array of arrays representation of the matrix to be tested.
   * @param n the bandwidth
   * @return the matrix referred to in the argument if the check passes
   */
  public static double[][] checkIsNDiag(double[][] aMatrix, int n) {
    if (MatrixPrimitiveUtils.isNDiag(aMatrix, n)) {
      return aMatrix;
    } else {
      throw new IllegalArgumentException("N-Diag matrix called on data that isn't N-Diagonal!");
    }
  }

  /**
   * Boolean on whether a matrix is N-Diagonal.
   * @param aMatrix an array of arrays representation of the matrix to be tested.
   * @param n the bandwidth
   * @return boolean, true if matrix is N-Diagonal, false if matrix is not.
   * @throws IllegalArgumentException
   */
  public static boolean isNDiag(double[][] aMatrix, int n) throws IllegalArgumentException, NotImplementedException {
    assertNotNull(aMatrix);
    if (isEven(n) || n < 1) {
      throw new IllegalArgumentException("Matrix bandwidth must be odd (as in an odd number of bands) AND positive");
    }
    if (!isSquare(aMatrix)) {
      throw new IllegalArgumentException("Matrix is not square so the notion of N-Diagonal isn't clear cut enough to be implemented");
    }
    if (n > aMatrix.length || n == 0) {
      throw new IllegalArgumentException("Impossible bandwidth suggested: bandwidth = " + n);
    }

    final int rows = aMatrix.length;

    int nonStandardRows = (n + 1) / 2;
    // test first rows that are not N wide
    for (int i = 0; i < nonStandardRows - 1; i++) {
      for (int j = nonStandardRows + i; j < rows; j++) {
        if (Double.doubleToLongBits(aMatrix[i][j]) != 0) {
          return false;
        }
      }
    }

    // tests members of each row that should be empty to ensure they are!
    for (int i = nonStandardRows - 1; i < rows - nonStandardRows + 1; i++) {
      // first check the elements on the LHS of the Ndiag entries
      for (int k = 0; k < i - (nonStandardRows - 1); k++) {
        if (Double.doubleToLongBits(aMatrix[i][k]) != 0) {
          return false;
        }
      }
      // second check the elements on the RHS of the Ndiag entries
      for (int k = i + nonStandardRows; k < rows; k++) {
        if (Double.doubleToLongBits(aMatrix[i][k]) != 0) {
          return false;
        }
      }
    }

    // test last rows that are not N wide
    for (int i = rows - nonStandardRows; i < rows; i++) { // row ptr
      for (int j = 0; j < i - nonStandardRows + 1; j++) { // col idx
        if (Double.doubleToLongBits(aMatrix[i][j]) != 0) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Tests if an integer is odd
   * @param n the integer to be tested
   * @return true if odd, false if even
   */
  public static boolean isOdd(int n) {
    return ((n & 0x1) == 0x1 ? true : false);
  }

  /**
   * Tests if an integer is even
   * @param n the integer to be tested
   * @return true if even, false if odd
   */
  public static boolean isEven(int n) {
    return ((n & 0x1) != 0x1 ? true : false);
  }

} // class end

