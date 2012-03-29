/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions;

import com.opengamma.maths.commonapi.exceptions.MathsExceptionGeneric;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGArrayType;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGIndexType;
import com.opengamma.maths.lowlevelapi.functions.utilities.Min;
import com.opengamma.maths.lowlevelapi.functions.utilities.Reverse;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.BLAS2;

/**
 * This wraps all the functions for the high level API such that they are just exposed as is.
 * Everything is done statelessly.
 * This function set is designed so that people can't blow their feet off with thread mess.
 * Everything takes and returns OGArrays, this is for safety.
 */
public class OGMatrixManipulationFunctions {

  public static OGArrayType multiply(OGArrayType thisArray, OGArrayType thatArray) {
    catchNull(thisArray);
    catchNull(thatArray);

    // check they commute.
    final int colsArray1 = thisArray.getNumberOfColumns();
    final int colsArray2 = thatArray.getNumberOfColumns();
    final int rowsArray1 = thisArray.getNumberOfRows();
    final int rowsArray2 = thatArray.getNumberOfRows();

    if (colsArray1 != rowsArray2) {
      throw new MathsExceptionGeneric("Arguments do not conform: thisArray is " + rowsArray1 + "x" + colsArray1 + ", thatArray is " + rowsArray2 + "x" + colsArray2 + ".");
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
      throw new MathsExceptionGeneric("BLAS3 DGEMM not implemented yet");
    }
    return new OGArrayType(answer);
  }

  // non linear algebra ops
  /**
   * Does abs()
   * @param thisArray is the array from which the absolute values are to be calculated
   * @return the absolute values of thisArray
   */
  public static OGArrayType abs(OGArrayType thisArray) {
    catchNull(thisArray);
    double[][] answer = allocNewBasedOnSizeOf(thisArray);
    for (int i = 0; i < answer.length; i++) {
      answer[i] = com.opengamma.maths.lowlevelapi.functions.utilities.Abs.stateless(thisArray.getFullRow(i));
    }
    return new OGArrayType(answer);
  }

  /**
   * Does abs()
   * @param thisArray is the array from which the absolute values are to be calculated
   * @return the absolute values of thisArray
   */
  public static OGIndexType abs(OGIndexType thisArray) {
    catchNull(thisArray);
    int[][] answer = allocNewBasedOnSizeOf(thisArray);
    for (int i = 0; i < answer.length; i++) {
      answer[i] = com.opengamma.maths.lowlevelapi.functions.utilities.Abs.stateless(thisArray.getFullRow(i).getData());
    }
    return new OGIndexType(answer);
  }

  /**
   * Does unique()
   * @param thisArray is the array from which the unique values are to be calculated
   * @return the negated values of thisArray
   */
  public static OGArrayType unique(OGArrayType thisArray) {
    catchNull(thisArray);
    double[] answer = com.opengamma.maths.lowlevelapi.functions.utilities.Unique.bitwise(thisArray.getData());
    return new OGArrayType(singlePointerToDoublePointer(answer));
  }

  /**
   * Does unique()
   * @param thisArray is the array from which the unique values are to be calculated
   * @return the negated values of thisArray
   */
  public static OGIndexType unique(OGIndexType thisArray) {
    catchNull(thisArray);
    int[] answer = com.opengamma.maths.lowlevelapi.functions.utilities.Unique.bitwise(thisArray.getData());
    return new OGIndexType(singlePointerToDoublePointer(answer));
  }

  /**
   * Transpose a matrix, this is the mathematical transpose of a matrix.
   * @param thisArray the array to transpose
   * @return the transpose of the matrix.
   */
  public static OGArrayType transpose(OGArrayType thisArray) {
    catchNull(thisArray);
    final int rows = thisArray.getNumberOfRows();
    final int cols = thisArray.getNumberOfColumns();
    double[][] tmp = new double[cols][rows];
    double[] data = thisArray.getData();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        tmp[j][i] = data[i * cols + j];
      }
    }
    return new OGArrayType(tmp);
  }

  /**
   * Transpose a matrix, this is the mathematical transpose of a matrix.
   * @param thisArray the array to transpose
   * @return the transpose of the matrix.
   */
  public static OGIndexType transpose(OGIndexType thisArray) {
    catchNull(thisArray);
    final int rows = thisArray.getNumberOfRows();
    final int cols = thisArray.getNumberOfColumns();
    int[][] tmp = new int[cols][rows];
    int[] data = thisArray.getData();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        tmp[j][i] = data[i * cols + j];
      }
    }
    return new OGIndexType(tmp);
  }

  // array manipulation
  /**
   * Does reshape(). The number of elements must be preserved. i.e. a 6x4 matrix can be converted to a 8x3 but not a 8x4.
   * The reshape is performed as a row wise unwind of the original matrix followed by a wrap to the new matrix size.
   * @param thisArray is the array from which the unique values are to be calculated
   * @param newRows the number of rows in the new matrix
   * @param newCols the number of columns in the new matrix
   * @return the negated values of thisArray
   */
  public static OGArrayType reshape(OGArrayType thisArray, int newRows, int newCols) {
    catchNull(thisArray);
    int rows = thisArray.getNumberOfRows();
    int cols = thisArray.getNumberOfColumns();

    if (thisArray.getNumberOfElements() != newRows * newCols) {
      throw new MathsExceptionGeneric("Cannot reshape array, number of elements are not conformant.\n  thisArray is " + rows + "x" + cols + ". Requested reshape is: " + newRows + " x " + newCols);
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
    return new OGArrayType(newData);
  }

  /**
   * Does reshape(). The number of elements must be preserved. i.e. a 6x4 matrix can be converted to a 8x3 but not a 8x4.
   * The reshape is performed as a row wise unwind of the original matrix followed by a wrap to the new matrix size.
   * @param thisArray is the array from which the unique values are to be calculated
   * @param newRows the number of rows in the new matrix
   * @param newCols the number of columns in the new matrix
   * @return the negated values of thisArray
   */
  public static OGIndexType reshape(OGIndexType thisArray, int newRows, int newCols) {
    catchNull(thisArray);
    int rows = thisArray.getNumberOfRows();
    int cols = thisArray.getNumberOfColumns();

    if (thisArray.getNumberOfElements() != newRows * newCols) {
      throw new MathsExceptionGeneric("Cannot reshape array, number of elements are not conformant.\n  thisArray is " + rows + "x" + cols + ". Requested reshape is: " + newRows + " x " + newCols);
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
    return new OGIndexType(newData);
  }

  /**
   * Flips left-to-right (i.e. horizontally) an array
   * @param thisArray the array to be flipped
   * @return a horizontally flipped version of thisArray
   */
  public static OGArrayType fliplr(OGArrayType thisArray) {
    catchNull(thisArray);
    int rows = thisArray.getNumberOfRows();
    // flip each row.
    double[][] flipped = new double[rows][];
    for (int i = 0; i < rows; i++) {
      flipped[i] = Reverse.stateless(thisArray.getFullRow(i));
    }
    return new OGArrayType(flipped);
  }

  /**
   * Flips left-to-right (i.e. horizontally) an array
   * @param thisArray the array to be flipped
   * @return a horizontally flipped version of thisArray
   */
  public static OGIndexType fliplr(OGIndexType thisArray) {
    catchNull(thisArray);
    int rows = thisArray.getNumberOfRows();
    // flip each row.
    int[][] flipped = new int[rows][];
    for (int i = 0; i < rows; i++) {
      flipped[i] = Reverse.stateless(thisArray.getFullRow(i).getData());
    }
    return new OGIndexType(flipped);
  }

  /**
   * Flips top-to-bottom (i.e. vertically) an array
   * @param thisArray the array to be flipped
   * @return a vertically flipped version of thisArray
   */
  public static OGArrayType flipud(OGArrayType thisArray) {
    catchNull(thisArray);
    int rows = thisArray.getNumberOfRows();
    // move columns by row at a time.
    double[][] flipped = new double[rows][];
    for (int i = 0; i < rows; i++) {
      flipped[i] = thisArray.getFullRow(rows - 1 - i);
    }
    return new OGArrayType(flipped);
  }

  /**
   * Flips top-to-bottom (i.e. vertically) an array
   * @param thisArray the array to be flipped
   * @return a vertically flipped version of thisArray
   */
  public static OGIndexType flipud(OGIndexType thisArray) {
    catchNull(thisArray);
    int rows = thisArray.getNumberOfRows();
    // move columns by row at a time.
    int[][] flipped = new int[rows][];
    for (int i = 0; i < rows; i++) {
      flipped[i] = thisArray.getFullRow(rows - 1 - i).getData();
    }
    return new OGIndexType(flipped);
  }

  /**
   * Horizontally concatenate arrays
   * @param theseArrays a number of OGArrays to concatenate horizontally (to the right) in the order specified in vargin.
   * @return the horizontal concatenation of the arrays specified in vargin.
   */
  public static OGArrayType horzcat(OGArrayType... theseArrays) {
    catchNull(theseArrays[0]);

    final int baseRows = theseArrays[0].getNumberOfRows();
    int colCount = theseArrays[0].getNumberOfColumns();
    // check the arrays commute.
    for (int i = 1; i < theseArrays.length; i++) {
      catchNull(theseArrays[i]);
      if (theseArrays[i].getNumberOfRows() != baseRows) {
        throw new MathsExceptionGeneric("Dimension mismatch for horizontal concatenation. Number of rows in first argument is " + baseRows + " whereas arguement " + i + "has " +
            theseArrays[i].getNumberOfRows() + "rows");
      }
      colCount += theseArrays[0].getNumberOfColumns();
    }
    // malloc
    double[][] tmp = new double[baseRows][colCount];
    int localCols;
    int cumCols = 0;
    for (int i = 0; i < theseArrays.length; i++) {
      localCols = theseArrays[i].getNumberOfColumns();
      for (int j = 0; j < baseRows; j++) {
        System.arraycopy(theseArrays[i].getFullRow(j), 0, tmp[j], cumCols, localCols);
      }
      cumCols += localCols;
    }

    return new OGArrayType(tmp);
  }

  /**
   * Horizontally concatenate arrays
   * @param theseArrays a number of OGIndexs to concatenate horizontally (to the right) in the order specified in vargin.
   * @return the horizontal concatenation of the arrays specified in vargin.
   */
  public static OGIndexType horzcat(OGIndexType... theseArrays) {
    catchNull(theseArrays[0]);

    final int baseRows = theseArrays[0].getNumberOfRows();
    int colCount = theseArrays[0].getNumberOfColumns();
    // check the arrays commute.
    for (int i = 1; i < theseArrays.length; i++) {
      catchNull(theseArrays[i]);
      if (theseArrays[i].getNumberOfRows() != baseRows) {
        throw new MathsExceptionGeneric("Dimension mismatch for horizontal concatenation. Number of rows in first argument is " + baseRows + " whereas arguement " + i + "has " +
            theseArrays[i].getNumberOfRows() + "rows");
      }
      colCount += theseArrays[0].getNumberOfColumns();
    }
    // malloc
    int[][] tmp = new int[baseRows][colCount];
    int localCols;
    int cumCols = 0;
    for (int i = 0; i < theseArrays.length; i++) {
      localCols = theseArrays[i].getNumberOfColumns();
      for (int j = 0; j < baseRows; j++) {
        System.arraycopy(theseArrays[i].getFullRow(j).getData(), 0, tmp[j], cumCols, localCols);
      }
      cumCols += localCols;
    }

    return new OGIndexType(tmp);
  }

  /**
   * Vertically concatenate arrays
   * @param theseArrays a number of OGArrays to concatenate vertically (to the bottom) in the order specified in vargin.
   * @return the vertical concatenation of the arrays specified in vargin.
   */
  public static OGArrayType vertcat(OGArrayType... theseArrays) {
    catchNull(theseArrays[0]);

    final int baseCols = theseArrays[0].getNumberOfColumns();
    int rowCount = theseArrays[0].getNumberOfRows();
    // check the arrays commute.
    for (int i = 1; i < theseArrays.length; i++) {
      catchNull(theseArrays[i]);
      if (theseArrays[i].getNumberOfColumns() != baseCols) {
        throw new MathsExceptionGeneric("Dimension mismatch for horizontal concatenation. Number of rows in first argument is " + baseCols + " whereas arguement " + i + "has " +
            theseArrays[i].getNumberOfRows() + "rows");
      }
      rowCount += theseArrays[0].getNumberOfRows();
    }
    // malloc
    double[][] tmp = new double[rowCount][baseCols];
    int ptr = 0;
    for (int i = 0; i < theseArrays.length; i++) {
      final int lim = theseArrays[i].getNumberOfRows();
      for (int j = 0; j < lim; j++) {
        System.arraycopy(theseArrays[i].getFullRow(j), 0, tmp[ptr], 0, baseCols);
        ptr++;
      }
    }
    return new OGArrayType(tmp);
  }

  /**
   * Vertically concatenate arrays
   * @param theseArrays a number of OGIndexs to concatenate vertically (to the bottom) in the order specified in vargin.
   * @return the vertical concatenation of the arrays specified in vargin.
   */
  public static OGIndexType vertcat(OGIndexType... theseArrays) {
    catchNull(theseArrays[0]);

    final int baseCols = theseArrays[0].getNumberOfColumns();
    int rowCount = theseArrays[0].getNumberOfRows();
    // check the arrays commute.
    for (int i = 1; i < theseArrays.length; i++) {
      catchNull(theseArrays[i]);
      if (theseArrays[i].getNumberOfColumns() != baseCols) {
        throw new MathsExceptionGeneric("Dimension mismatch for horizontal concatenation. Number of rows in first argument is " + baseCols + " whereas arguement " + i + "has " +
            theseArrays[i].getNumberOfRows() + "rows");
      }
      rowCount += theseArrays[0].getNumberOfRows();
    }
    // malloc
    int[][] tmp = new int[rowCount][baseCols];
    int ptr = 0;
    for (int i = 0; i < theseArrays.length; i++) {
      final int lim = theseArrays[i].getNumberOfRows();
      for (int j = 0; j < lim; j++) {
        System.arraycopy(theseArrays[i].getFullRow(j).getData(), 0, tmp[ptr], 0, baseCols);
        ptr++;
      }
    }
    return new OGIndexType(tmp);
  }

  /**
   * Repmat, creates a new matrix by repeatedly copying a matrix tiling it "n" times in the horizontal and "m" in the vertical
   * @param thisArray the array to be tiled
   * @param m the number of tiles in the vertical direction
   * @param n the number of tiles in the horizontal direction
   * @return tmp a new matrix made from m x n tiles of thisArray
   */
  public static OGArrayType repmat(OGArrayType thisArray, int m, int n) {
    catchNull(thisArray);
    if (n < 1 | m < 1) {
      throw new MathsExceptionGeneric("The number of tiles in the m and n directions must be greater than 1 (" + m + " x " + n + "given).");
    }
    int rows = thisArray.getNumberOfRows();
    int cols = thisArray.getNumberOfColumns();
    double[][] tmp = new double[m * rows][n * cols];
    double[] localRow;
    int offsetRows;
    for (int i = 0; i < m; i++) {
      offsetRows = i * rows;
      for (int k = 0; k < rows; k++) {
        localRow = thisArray.getFullRow(k);
        for (int j = 0; j < n; j++) {
          System.arraycopy(localRow, 0, tmp[offsetRows + k], j * cols, cols);
        }
      }
    }
    return new OGArrayType(tmp);
  }

  /**
   * Repmat, creates a new matrix by repeatedly copying a matrix tiling it "n" times in the horizontal and "m" in the vertical
   * @param thisArray the array to be tiled
   * @param m the number of tiles in the vertical direction
   * @param n the number of tiles in the horizontal direction
   * @return tmp a new matrix made from m x n tiles of thisArray
   */
  public static OGIndexType repmat(OGIndexType thisArray, int m, int n) {
    catchNull(thisArray);
    if (n < 1 | m < 1) {
      throw new MathsExceptionGeneric("The number of tiles in the m and n directions must be greater than 1 (" + m + " x " + n + "given).");
    }
    int rows = thisArray.getNumberOfRows();
    int cols = thisArray.getNumberOfColumns();
    int[][] tmp = new int[m * rows][n * cols];
    int[] localRow;
    int offsetRows;
    for (int i = 0; i < m; i++) {
      offsetRows = i * rows;
      for (int k = 0; k < rows; k++) {
        localRow = thisArray.getFullRow(k).getData();
        for (int j = 0; j < n; j++) {
          System.arraycopy(localRow, 0, tmp[offsetRows + k], j * cols, cols);
        }
      }
    }
    return new OGIndexType(tmp);
  }

  /**
   * Diag performs operations on the matrix diagonals.
   * @param thisArray the array from which the diagonal will be extracted or a diagonal matrix can be created from 
   * @return a diag or a diagonal matrix, TODO: Fix with better explanation, add in DIAG matrix type 
   */
  public static OGArrayType diag(OGArrayType thisArray) {
    catchNull(thisArray);
    final int rows = thisArray.getNumberOfRows();
    final int cols = thisArray.getNumberOfColumns();
    double[][] tmp = null;
    if (rows == 1 || cols == 1) { // we are creating a diagonal matrix
      final int dim = rows == 1 ? cols : rows;
      tmp = new double[dim][dim];
      double[] dptr = thisArray.getData();
      for (int i = 0; i < dim; i++) {
        tmp[i][i] = dptr[i];
      }
    } else { // we are extracting the diagonal from a matrix
      final int dim = rows < cols ? rows : cols;
      tmp = new double[1][dim];
      double[] dptr = thisArray.getData();
      for (int i = 0; i < dim; i++) {
        tmp[0][i] = dptr[i * cols + i];
      }
    }
    return new OGArrayType(tmp);
  }

  /**
   * Diag performs operations on the matrix diagonals.
   * @param thisArray the array from which the diagonal will be extracted or a diagonal matrix can be created from 
   * @return a diag or a diagonal matrix, TODO: Fix with better explanation, add in DIAG matrix type 
   */
  public static OGIndexType diag(OGIndexType thisArray) {
    catchNull(thisArray);
    final int rows = thisArray.getNumberOfRows();
    final int cols = thisArray.getNumberOfColumns();
    int[][] tmp = null;
    int[] dptr = thisArray.getData();
    if (rows == 1 || cols == 1) { // we are creating a diagonal matrix
      final int dim = rows == 1 ? cols : rows;
      tmp = new int[dim][dim];
      for (int i = 0; i < dim; i++) {
        tmp[i][i] = dptr[i];
      }
    } else { // we are extracting the diagonal from a matrix
      final int dim = rows < cols ? rows : cols;
      tmp = new int[1][dim];
      for (int i = 0; i < dim; i++) {
        tmp[0][i] = dptr[i * cols + i];
      }
    }
    return new OGIndexType(tmp);
  }

  /**
   * Diag performs operations on the matrix diagonals.
   * @param thisArray the array from which the diagonal will be extracted or a diagonal matrix can be created from 
   * @param k the offset from the diagonal to be used
   * @return a diag or a diagonal matrix, TODO: Fix with better explanation, add in DIAG matrix type 
   */
  public static OGArrayType diag(OGArrayType thisArray, int k) {
    catchNull(thisArray);
    final int rows = thisArray.getNumberOfRows();
    final int cols = thisArray.getNumberOfColumns();

    double[] dptr = thisArray.getData();
    // do the calc
    double[][] tmp = null;
    if (rows == 1 || cols == 1) { // we are creating a kth diagonal matrix
      final int dim = rows == 1 ? cols + Math.abs(k) : rows + Math.abs(k);
      tmp = new double[dim][dim];
      if (k > 0) { // k in upper triangle
        for (int i = 0; i < dim - k; i++) {
          tmp[i][i + k] = dptr[i];
        }
      } else { // k on diag or in lower triangle
        for (int i = 0; i < dim + k; i++) {
          tmp[i - k][i] = dptr[i];
        }
      }
    } else { // we are extracting the kth diagonal from a matrix
      if (k > 0 && k > cols - 1) {
        throw new MathsExceptionGeneric("The + " + k + "^th diagonal was requested but the matrix only has " + cols + " columns.");
      }
      if (k < 0 && -k > rows - 1) {
        throw new MathsExceptionGeneric("The + " + k + "^th diagonal was requested but the matrix only has " + rows + " rows.");
      }
      // shortcut for 0 k.
      if (k == 0) {
        return diag(thisArray);
      }
      if (k > 0) {
        int mlen = Min.value(new int[] {cols - k, rows });
        tmp = new double[1][mlen];
        for (int i = 0; i < mlen; i++) {
          tmp[0][i] = dptr[i * cols + i + k];
        }
      } else {
        int pk = -k;
        int mlen = Min.value(new int[] {rows - pk, cols });
        tmp = new double[1][mlen];
        for (int i = 0; i < mlen; i++) {
          tmp[0][i] = dptr[pk * cols + i * cols + i];
        }
      }

    }
    return new OGArrayType(tmp);
  }

  /**
   * Diag performs operations on the matrix diagonals.
   * @param thisArray the array from which the diagonal will be extracted or a diagonal matrix can be created from 
   * @param k the offset from the diagonal to be used
   * @return a diag or a diagonal matrix, TODO: Fix with better explanation, add in DIAG matrix type 
   */
  public static OGIndexType diag(OGIndexType thisArray, int k) {
    catchNull(thisArray);
    final int rows = thisArray.getNumberOfRows();
    final int cols = thisArray.getNumberOfColumns();

    int[] dptr = thisArray.getData();
    // do the calc
    int[][] tmp = null;
    if (rows == 1 || cols == 1) { // we are creating a kth diagonal matrix
      final int dim = rows == 1 ? cols + Math.abs(k) : rows + Math.abs(k);
      tmp = new int[dim][dim];
      if (k > 0) { // k in upper triangle
        for (int i = 0; i < dim - k; i++) {
          tmp[i][i + k] = dptr[i];
        }
      } else { // k on diag or in lower triangle
        for (int i = 0; i < dim + k; i++) {
          tmp[i - k][i] = dptr[i];
        }
      }
    } else { // we are extracting the kth diagonal from a matrix
      if (k > 0 && k > cols - 1) {
        throw new MathsExceptionGeneric("The + " + k + "^th diagonal was requested but the matrix only has " + cols + " columns.");
      }
      if (k < 0 && -k > rows - 1) {
        throw new MathsExceptionGeneric("The + " + k + "^th diagonal was requested but the matrix only has " + rows + " rows.");
      }
      // shortcut for 0 k.
      if (k == 0) {
        return diag(thisArray);
      }
      if (k > 0) {
        int mlen = Min.value(new int[] {cols - k, rows });
        tmp = new int[1][mlen];
        for (int i = 0; i < mlen; i++) {
          tmp[0][i] = dptr[i * cols + i + k];
        }
      } else {
        int pk = -k;
        int mlen = Min.value(new int[] {rows - pk, cols });
        tmp = new int[1][mlen];
        for (int i = 0; i < mlen; i++) {
          tmp[0][i] = dptr[pk * cols + i * cols + i];
        }
      }

    }
    return new OGIndexType(tmp);
  }

  // printin
  /**
   * @deprecated
   * debug routine
   * @param thisInt an int to print
   */
  @Deprecated
  public static void print(int thisInt) {
    System.out.format("Integer = %12d%n", thisInt);
  }

  // printin
  /**
   * @deprecated
   * debug routine
   * @param thisArray an array to print
   */
  @Deprecated
  public static void print(OGArrayType thisArray) {
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

  // printin
  /**
   * @deprecated
   * debug routine
   * @param thisArray an array to print
   */
  @Deprecated
  public static void print(OGIndexType thisArray) {
    final int rows = thisArray.getNumberOfRows();
    final int cols = thisArray.getNumberOfColumns();
    final int[] data = thisArray.getData();
    System.out.println("{");
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        System.out.format("%12d ", data[i * cols + j]);
      }
      System.out.println("");
    }
    System.out.println("}");
  }

  // helpers
  // allocs an array that is the same dimension as thisArray
  private static double[][] allocNewBasedOnSizeOf(OGArrayType thisArray) {
    return new double[thisArray.getNumberOfRows()][thisArray.getNumberOfColumns()];
  }

  // allocs an array that is the same size in memory as thisArray
  private static double[] allocFlatNewBasedOnSizeOf(OGArrayType thisArray) {
    return new double[thisArray.getNumberOfRows() * thisArray.getNumberOfColumns()];
  }

  //catches nulls else throws pain
  private static void catchNull(OGArrayType thisArray) {
    if (thisArray == null) {
      throw new MathsExceptionGeneric("OGArray passed to function that points to NULL");
    }
  }

  // allocs an array that is the same dimension as thisArray
  private static int[][] allocNewBasedOnSizeOf(OGIndexType thisArray) {
    return new int[thisArray.getNumberOfRows()][thisArray.getNumberOfColumns()];
  }

  // allocs an array that is the same size in memory as thisArray
  private static int[] allocFlatNewBasedOnSizeOf(OGIndexType thisArray) {
    return new int[thisArray.getNumberOfRows() * thisArray.getNumberOfColumns()];
  }

  //catches nulls else throws pain
  private static void catchNull(OGIndexType thisArray) {
    if (thisArray == null) {
      throw new MathsExceptionGeneric("OGIndex passed to function that points to NULL");
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
