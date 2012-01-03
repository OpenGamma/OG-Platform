/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.highlevelapi;

import com.opengamma.math.BLAS.BLAS2;

/**
 * This wraps all the functions for the high level API such that they are just exposed as is.
 * Everything is done statelessly.
 * This function set is designed so that people can't blow their feet off with thread mess.
 * Everything takes and returns OGArrays, this is for safety.
 */
public class OGFunctions {

  public static OGArray multiply(OGArray thisArray, OGArray thatArray) {
    catchNull(thisArray);
    catchNull(thatArray);

    // check they commute.
    final int colsArray1 = thisArray.getNumberOfColumns();
    final int colsArray2 = thatArray.getNumberOfColumns();
    final int rowsArray1 = thisArray.getNumberOfRows();
    final int rowsArray2 = thatArray.getNumberOfRows();

    if (colsArray1 != rowsArray2) {
      throw new MathsException("Arguments do not conform: thisArray is " + rowsArray1 + "x" + colsArray1 + ", thatArray is " + rowsArray2 + "x" + colsArray2 + ".");
    }

    double[][] answer = null;
    if (colsArray2 == 1) { // A*x
      final int rows = thisArray.getNumberOfRows();
      answer = new double[rows][1];
      double[] tmp = BLAS2.dgemv(thisArray, thatArray.getData());
      for (int i = 0; i < rows; i++) {
        answer[i][0] = tmp[i];
      }
    } else if (rowsArray1 == 1) { // x'*A
      final int cols = thatArray.getNumberOfColumns();
      answer = new double[1][cols];
      double[] tmp = BLAS2.dgemvTransposed(thatArray, thisArray.getData());
      for (int i = 0; i < cols; i++) {
        answer[0][i] = tmp[i];
      }
    } else { // BLAS3.DGEMM
      throw new MathsException("BLAS3 DGEMM not implemented yet");
    }
    return new OGArray(answer);
  }

  // non linear algebra ops
  /**
   * Does abs()
   * @param thisArray is the array from which the absolute values are to be calculated
   * @return the absolute values of thisArray
   */
  public static OGArray abs(OGArray thisArray) {
    catchNull(thisArray);
    double[][] answer = allocNewBasedOnSizeOf(thisArray);
    for (int i = 0; i < answer.length; i++) {
      answer[i] = com.opengamma.math.utilities.Abs.stateless(thisArray.getFullRow(i));
    }
    return new OGArray(answer);
  }

  /**
   * Does abs()
   * @param thisArray is the array from which the absolute values are to be calculated
   * @return the absolute values of thisArray
   */
  public static OGIndex abs(OGIndex thisArray) {
    catchNull(thisArray);
    int[][] answer = allocNewBasedOnSizeOf(thisArray);
    for (int i = 0; i < answer.length; i++) {
      answer[i] = com.opengamma.math.utilities.Abs.stateless(thisArray.getFullRow(i).getData());
    }
    return new OGIndex(answer);
  }

  /**
   * Does unique()
   * @param thisArray is the array from which the unique values are to be calculated
   * @return the negated values of thisArray
   */
  public static OGArray unique(OGArray thisArray) {
    catchNull(thisArray);
    double[] answer = com.opengamma.math.utilities.Unique.bitwise(thisArray.getData());
    return new OGArray(singlePointerToDoublePointer(answer));
  }

  /**
   * Does unique()
   * @param thisArray is the array from which the unique values are to be calculated
   * @return the negated values of thisArray
   */
  public static OGIndex unique(OGIndex thisArray) {
    catchNull(thisArray);
    int[] answer = com.opengamma.math.utilities.Unique.bitwise(thisArray.getData());
    return new OGIndex(singlePointerToDoublePointer(answer));
  }

  /**
   * Does reshape(). The number of elements must be preserved. i.e. a 6x4 matrix can be converted to a 8x3 but not a 8x4.
   * The reshape is performed as a row wise unwind of the original matrix followed by a wrap to the new matrix size.
   * @param thisArray is the array from which the unique values are to be calculated
   * @param newRows the number of rows in the new matrix
   * @param newCols the number of columns in the new matrix
   * @return the negated values of thisArray
   */
  public static OGArray reshape(OGArray thisArray, int newRows, int newCols) {
    catchNull(thisArray);
    int rows = thisArray.getNumberOfRows();
    int cols = thisArray.getNumberOfColumns();

    if (thisArray.getNumberOfElements() != newRows * newCols) {
      throw new MathsException("Cannot reshape array, number of elements are not conformant.\n  thisArray is " + rows + "x" + cols + ". Requested reshape is: " + newRows + " x " + newCols);
    }
    double[] data = thisArray.getData();
    int ptr = 0;
    double[][] newData = new double[newRows][newCols];
    for (int i = 0; i < newRows; i++) {
      for (int j = 0; j < newCols; j++) {
        newData[i][j] = data[ptr];
        ptr++;
      }
    }
    return new OGArray(newData);
  }

  /**
   * Does reshape(). The number of elements must be preserved. i.e. a 6x4 matrix can be converted to a 8x3 but not a 8x4.
   * The reshape is performed as a row wise unwind of the original matrix followed by a wrap to the new matrix size.
   * @param thisArray is the array from which the unique values are to be calculated
   * @param newRows the number of rows in the new matrix
   * @param newCols the number of columns in the new matrix
   * @return the negated values of thisArray
   */
  public static OGIndex reshape(OGIndex thisArray, int newRows, int newCols) {
    catchNull(thisArray);
    int rows = thisArray.getNumberOfRows();
    int cols = thisArray.getNumberOfColumns();

    if (thisArray.getNumberOfElements() != newRows * newCols) {
      throw new MathsException("Cannot reshape array, number of elements are not conformant.\n  thisArray is " + rows + "x" + cols + ". Requested reshape is: " + newRows + " x " + newCols);
    }
    int[] data = thisArray.getData();
    int ptr = 0;
    int[][] newData = new int[newRows][newCols];
    for (int i = 0; i < newRows; i++) {
      for (int j = 0; j < newCols; j++) {
        newData[i][j] = data[ptr];
        ptr++;
      }
    }
    return new OGIndex(newData);
  }

  // printin

  public static void print(int thisInt) {
    System.out.format("Integer = %12d%n", thisInt);
  }

  public static void print(OGArray thisArray) {
    final int rows = thisArray.getNumberOfRows();
    final int cols = thisArray.getNumberOfColumns();
    final double[] data = thisArray.getData();
    System.out.println("{");
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        System.out.format("%12.8f ", data[i * cols + j]);
      }
      System.out.println("");
    }
    System.out.println("}");
  }

  // helpers
  // allocs an array that is the same dimension as thisArray
  private static double[][] allocNewBasedOnSizeOf(OGArray thisArray) {
    return new double[thisArray.getNumberOfRows()][thisArray.getNumberOfColumns()];
  }

  // allocs an array that is the same size in memory as thisArray
  private static double[] allocFlatNewBasedOnSizeOf(OGArray thisArray) {
    return new double[thisArray.getNumberOfRows() * thisArray.getNumberOfColumns()];
  }

  //catches nulls else throws pain
  private static void catchNull(OGArray thisArray) {
    if (thisArray == null) {
      throw new MathsException("OGArray passed to function that points to NULL");
    }
  }

  // allocs an array that is the same dimension as thisArray
  private static int[][] allocNewBasedOnSizeOf(OGIndex thisArray) {
    return new int[thisArray.getNumberOfRows()][thisArray.getNumberOfColumns()];
  }

  // allocs an array that is the same size in memory as thisArray
  private static int[] allocFlatNewBasedOnSizeOf(OGIndex thisArray) {
    return new int[thisArray.getNumberOfRows() * thisArray.getNumberOfColumns()];
  }

  //catches nulls else throws pain
  private static void catchNull(OGIndex thisArray) {
    if (thisArray == null) {
      throw new MathsException("OGIndex passed to function that points to NULL");
    }
  }

  // converts single pointers to double pointers so the single unified int[][] constructor can be used
  private static int[][] singlePointerToDoublePointer(int[] data) {
    int[][] tmp = new int[1][data.length];
    System.arraycopy(data, 0, tmp[0], 0, data.length);
    return tmp;
  }

  private static double[][] singlePointerToDoublePointer(double[] data) {
    double[][] tmp = new double[1][data.length];
    System.arraycopy(data, 0, tmp[0], 0, data.length);
    return tmp;
  }

}
