/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.BLAS;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import com.opengamma.math.matrix.CompressedSparseRowFormatMatrix;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.FullMatrix;
import com.opengamma.math.matrix.MatrixPrimitiveInterface;

/**
 * Provides the BLAS level 2 behaviour for the OG matrix library.
 * Massive amounts of overloading goes on, beware and only use if confident.
  * METHODS: DGEMV
 */
public class BLAS2 {
  /**
  * DGEMV  performs one of the following matrix vector operations
  *
  *  y := alpha*A*x + beta*y OR y := alpha*A^T*x + beta*y,
  *
  *  where alpha and beta are scalars, x and y are vectors and A is an
  *  m by n matrix. The ^T indicates transposition.
  *
  *  For speed, the method is overloaded such that simplified calls can be
  *  made when different parts of the DGEMV operation are not needed.
  *
  */

  /**
   * orientation: Enumeration based for the orientation of matrix A in the scheme
   * y := alpha*A*x + beta*y
   */
  public enum orientation {
    /** orientation is "normal" */
    normal,
    /** orientation is "transposed" */
    transposed
  };

  /**
   * Ensures that the inputs to DGEMV routines are sane when DGMEV is function(Matrix,Vector).
   * @param aMatrix is the matrix to be tested (A)
   * @param aVector is the vector to be tested (x)
   */
  public static void dgemvInputSanityChecker(MatrixPrimitiveInterface aMatrix, double[] aVector) {
    assertNotNull(aMatrix); // check not null
    assertNotNull(aVector); // check not null
    assertEquals(aMatrix.getNumberOfColumns(), aVector.length); // check commutable
  }

  /**
   * Ensures that the inputs to DGEMV routines are sane when DGMEV is function(Matrix,Vector,Vector).
   * @param aMatrix is the matrix to be tested (A)
   * @param aVector is the vector to be tested (x)
   * @param addToVector is the vector to be tested (y)
   */
  public static void dgemvInputSanityChecker(MatrixPrimitiveInterface aMatrix, double[] aVector, double[] addToVector) {
    assertNotNull(aMatrix); // check not null
    assertNotNull(aVector); // check not null
    assertNotNull(addToVector); // check not null
    assertEquals(aMatrix.getNumberOfColumns(), aVector.length); // check commutable
    assertEquals(aMatrix.getNumberOfRows(), addToVector.length); // check commutable
  }

  /**
   * Ensures that the inputs to DGEMV routines for transposed matrices are sane when DGEMV is function(Matrix,Vector)
   * @param aMatrix is the matrix to be tested (A)
   * @param aVector is the vector to be tested (x)
   */
  public static void dgemvInputSanityCheckerTransposed(MatrixPrimitiveInterface aMatrix, double[] aVector) {
    assertNotNull(aMatrix); // check not null
    assertNotNull(aVector); // check not null
    assertEquals(aMatrix.getNumberOfRows(), aVector.length); // check commutable
  }

  /**
   * Ensures that the inputs to DGEMV routines for transposed matrices are sane when DGEMV is function(Matrix,Vector,Vector)
   * @param aMatrix is the matrix to be tested (A)
   * @param aVector is the vector to be tested (x)
   * @param addToVector is the vector to be tested (y)
   */
  public static void dgemvInputSanityCheckerTransposed(MatrixPrimitiveInterface aMatrix, double[] aVector, double[] addToVector) {
    assertNotNull(aMatrix); // check not null
    assertNotNull(aVector); // check not null
    assertNotNull(addToVector); // check not null
    assertEquals(aMatrix.getNumberOfRows(), aVector.length); // check commutable
    assertEquals(aMatrix.getNumberOfColumns(), addToVector.length); // check commutable
  }

  /**
   * Ensures that the inputs to DGEMV routines are sane.
   * @param aMatrix is the matrix to be tested (A)
   * @param aVector is the vector to be tested (x)
   * @param aYVector is the vector to be tested for back assignment (y)
   */
  public static void dgemvInputSanityChecker(double[] aYVector, MatrixPrimitiveInterface aMatrix, double[] aVector) {
    assertNotNull(aMatrix); // check not null
    assertNotNull(aVector); // check not null
    assertNotNull(aYVector); // check not null
    assertEquals(aMatrix.getNumberOfColumns(), aVector.length); // check commutable
    assertEquals(aMatrix.getNumberOfRows(), aYVector.length); // check commutable on back assignment
  }


  /* Stateless manipulators on the FullMatrix type */

  /* GROUP1:: A*x OR A^T*x */
  /**
   * DGEMV simplified: returns:=A*x OR returns:=A^T*x depending on the enum orientation.
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, double[] aVector, BLAS2.orientation o) {
    double[] tmp = null;
    switch (o) {
      case normal:
        tmp = dgemv(aMatrix, aVector);
        break;
      case transposed:
        tmp = dgemvTransposed(aMatrix, aVector);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x OR returns:=A^T*x depending on the enum orientation.
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, BLAS2.orientation o) {
    double[] tmp = dgemv(aMatrix, aVector.getData(), o);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, double[] aVector) {
    dgemvInputSanityChecker(aMatrix, aVector);
    final int rows = aMatrix.getNumberOfRows();
    final int cols = aMatrix.getNumberOfColumns();
    double[] tmp = new double[rows];
    double[] ptrA = aMatrix.getData();
    int idx, ptr;
    final int extra = cols - cols % 8;
    final int ub = ((cols / 8) * 8) - 1;
    double acc;
    for (int i = 0; i < rows; i++) {
      idx = i * cols;
      acc = 0;
      for (int j = 0; j < ub; j += 8) {
        ptr = idx + j;
        tmp[i] += ptrA[ptr] * aVector[j]
            + ptrA[ptr + 1] * aVector[j + 1]
            + ptrA[ptr + 2] * aVector[j + 2]
            + ptrA[ptr + 3] * aVector[j + 3];
        acc += ptrA[ptr + 4] * aVector[j + 4]
            + ptrA[ptr + 5] * aVector[j + 5]
            + ptrA[ptr + 6] * aVector[j + 6]
            + ptrA[ptr + 7] * aVector[j + 7];
      }
      tmp[i] += acc;
      for (int j = extra; j < cols; j++) {
        tmp[i] += ptrA[idx + j] * aVector[j];
      }
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector) {
    double[] tmp = dgemv(aMatrix, aVector.getData());
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(FullMatrix aMatrix, double[] aVector) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector);
    final int rows = aMatrix.getNumberOfRows();
    final int cols = aMatrix.getNumberOfColumns();
    double[] ptrA = aMatrix.getData();
    double[] tmp = new double[cols];
    int idx0, idx1, idx2, idx3, idx4, idx5, idx6, idx7;
    final int extra = cols - cols % 8;
    final int ub = ((cols / 8) * 8) - 1;
    final int extraRows = rows - rows % 8;
    final int ubRows = ((rows / 8) * 8) - 1;
    int ip1, ip2, ip3, ip4, ip5, ip6, ip7;
    for (int j = 0; j < ubRows; j += 8) {
      idx0 = (j + 0) * cols;
      idx1 = (j + 1) * cols;
      idx2 = (j + 2) * cols;
      idx3 = (j + 3) * cols;
      idx4 = (j + 4) * cols;
      idx5 = (j + 5) * cols;
      idx6 = (j + 6) * cols;
      idx7 = (j + 7) * cols;

      final double rhs0 = aVector[j];
      final double rhs1 = aVector[j + 1];
      final double rhs2 = aVector[j + 2];
      final double rhs3 = aVector[j + 3];
      final double rhs4 = aVector[j + 4];
      final double rhs5 = aVector[j + 5];
      final double rhs6 = aVector[j + 6];
      final double rhs7 = aVector[j + 7];

      for (int i = 0; i < ub; i += 8) {
        ip1 = i + 1;
        ip2 = i + 2;
        ip3 = i + 3;
        ip4 = i + 4;
        ip5 = i + 5;
        ip6 = i + 6;
        ip7 = i + 7;
        tmp[i] += ptrA[idx0 + i] * rhs0 + ptrA[idx1 + i] * rhs1 + ptrA[idx2 + i] * rhs2 + ptrA[idx3 + i] * rhs3 + ptrA[idx4 + i] * rhs4 + ptrA[idx5 + i] * rhs5 + ptrA[idx6 + i] * rhs6 +
            ptrA[idx7 + i] * rhs7;
        tmp[i + 1] += ptrA[idx0 + ip1] * rhs0 + ptrA[idx1 + ip1] * rhs1 + ptrA[idx2 + ip1] * rhs2 + ptrA[idx3 + ip1] * rhs3 + ptrA[idx4 + ip1] * rhs4 + ptrA[idx5 + ip1] * rhs5 + ptrA[idx6 + ip1] *
            rhs6 + ptrA[idx7 + ip1] * rhs7;
        tmp[i + 2] += ptrA[idx0 + ip2] * rhs0 + ptrA[idx1 + ip2] * rhs1 + ptrA[idx2 + ip2] * rhs2 + ptrA[idx3 + ip2] * rhs3 + ptrA[idx4 + ip2] * rhs4 + ptrA[idx5 + ip2] * rhs5 + ptrA[idx6 + ip2] *
            rhs6 + ptrA[idx7 + ip2] * rhs7;
        tmp[i + 3] += ptrA[idx0 + ip3] * rhs0 + ptrA[idx1 + ip3] * rhs1 + ptrA[idx2 + ip3] * rhs2 + ptrA[idx3 + ip3] * rhs3 + ptrA[idx4 + ip3] * rhs4 + ptrA[idx5 + ip3] * rhs5 + ptrA[idx6 + ip3] *
            rhs6 + ptrA[idx7 + ip3] * rhs7;
        tmp[i + 4] += ptrA[idx0 + ip4] * rhs0 + ptrA[idx1 + ip4] * rhs1 + ptrA[idx2 + ip4] * rhs2 + ptrA[idx3 + ip4] * rhs3 + ptrA[idx4 + ip4] * rhs4 + ptrA[idx5 + ip4] * rhs5 + ptrA[idx6 + ip4] *
            rhs6 + ptrA[idx7 + ip4] * rhs7;
        tmp[i + 5] += ptrA[idx0 + ip5] * rhs0 + ptrA[idx1 + ip5] * rhs1 + ptrA[idx2 + ip5] * rhs2 + ptrA[idx3 + ip5] * rhs3 + ptrA[idx4 + ip5] * rhs4 + ptrA[idx5 + ip5] * rhs5 + ptrA[idx6 + ip5] *
            rhs6 + ptrA[idx7 + ip5] * rhs7;
        tmp[i + 6] += ptrA[idx0 + ip6] * rhs0 + ptrA[idx1 + ip6] * rhs1 + ptrA[idx2 + ip6] * rhs2 + ptrA[idx3 + ip6] * rhs3 + ptrA[idx4 + ip6] * rhs4 + ptrA[idx5 + ip6] * rhs5 + ptrA[idx6 + ip6] *
            rhs6 + ptrA[idx7 + ip6] * rhs7;
        tmp[i + 7] += ptrA[idx0 + ip7] * rhs0 + ptrA[idx1 + ip7] * rhs1 + ptrA[idx2 + ip7] * rhs2 + ptrA[idx3 + ip7] * rhs3 + ptrA[idx4 + ip7] * rhs4 + ptrA[idx5 + ip7] * rhs5 + ptrA[idx6 + ip7] *
            rhs6 + ptrA[idx7 + ip7] * rhs7;
      }
      for (int i = extra; i < cols; i++) {
        tmp[i] += ptrA[idx0 + i] * rhs0 + ptrA[idx1 + i] * rhs1 + ptrA[idx2 + i] * rhs2 + ptrA[idx3 + i] * rhs3 + ptrA[idx4 + i] * rhs4 + ptrA[idx5 + i] * rhs5 + ptrA[idx6 + i] * rhs6 +
            ptrA[idx7 + i] * rhs7;
      }
    }
    for (int j = extraRows; j < rows; j++) {
      idx0 = j * cols;
      final double rhs = aVector[j];
      for (int i = 0; i < ub; i += 8) {
        ip1 = i + 1;
        ip2 = i + 2;
        ip3 = i + 3;
        ip4 = i + 4;
        ip5 = i + 5;
        ip6 = i + 6;
        ip7 = i + 7;
        tmp[i] += ptrA[idx0 + i] * rhs;
        tmp[i + 1] += ptrA[idx0 + ip1] * rhs;
        tmp[i + 2] += ptrA[idx0 + ip2] * rhs;
        tmp[i + 3] += ptrA[idx0 + ip3] * rhs;
        tmp[i + 4] += ptrA[idx0 + ip4] * rhs;
        tmp[i + 5] += ptrA[idx0 + ip5] * rhs;
        tmp[i + 6] += ptrA[idx0 + ip6] * rhs;
        tmp[i + 7] += ptrA[idx0 + ip7] * rhs;
      }
      for (int i = extra; i < cols; i++) {
        tmp[i] += ptrA[idx0 + i] * rhs;
      }
    }

    return tmp;

  }

  /**
   * DGEMV simplified: returns:=A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(FullMatrix aMatrix, DoubleMatrix1D aVector) {
    double[] tmp = dgemvTransposed(aMatrix, aVector.getData());
    return tmp;
  }

  /* GROUP2:: alpha*A*x OR alpha*A^T*x */
  /**
   * DGEMV simplified: returns:=alpha*A*x OR returns:=alpha*A^T*x depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double[] aVector, BLAS2.orientation o) {
    double[] tmp = null;
    switch (o) {
      case normal:
        tmp = dgemv(alpha, aMatrix, aVector);
        break;
      case transposed:
        tmp = dgemvTransposed(alpha, aMatrix, aVector);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x OR returns:=alpha*A^T*x depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, BLAS2.orientation o) {
    double[] tmp = dgemv(alpha, aMatrix, aVector.getData(), o);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double[] aVector) {
    dgemvInputSanityChecker(aMatrix, aVector);
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = dgemv(aMatrix, aVector);
    for (int i = 0; i < rows; i++) {
      tmp[i] *= alpha; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector) {
    double[] tmp = dgemv(alpha, aMatrix, aVector.getData());
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @return tmp a double[] vector
   * TODO: Replace vector scalings with BLAS1 calls.
   */
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, double[] aVector) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector);
    final int cols = aMatrix.getNumberOfColumns();
    double[] tmp = new double[cols];
    tmp = dgemvTransposed(aMatrix, aVector);
    for (int i = 0; i < cols; i++) {
      tmp[i] *= alpha;
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector) {
    double[] tmp = dgemvTransposed(alpha, aMatrix, aVector.getData());
    return tmp;
  }

  /* GROUP3:: A*x + y OR A^T*x + y */
  /**
   * DGEMV simplified: returns:=A*x+y OR returns:=A^T*x+y depending on the enum orientation.
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, double[] aVector, double[] y, BLAS2.orientation o) {
    double[] tmp = null;
    switch (o) {
      case normal:
        tmp = dgemv(aMatrix, aVector, y);
        break;
      case transposed:
        tmp = dgemvTransposed(aMatrix, aVector, y);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x+y OR returns:=A^T*x+y depending on the enum orientation.
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, double[] y, BLAS2.orientation o) {
    double[] tmp = dgemv(aMatrix, aVector.getData(), y, o);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x+y OR returns:=A^T*x+y depending on the enum orientation.
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, double[] aVector, DoubleMatrix1D y, BLAS2.orientation o) {
    double[] tmp = dgemv(aMatrix, aVector, y.getData(), o);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x+y OR returns:=A^T*x+y depending on the enum orientation.
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y, BLAS2.orientation o) {
    double[] tmp = dgemv(aMatrix, aVector.getData(), y.getData(), o);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x + y
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, double[] aVector, double[] y) {
    dgemvInputSanityChecker(aMatrix, aVector, y);
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = dgemv(aMatrix, aVector);
    for (int i = 0; i < rows; i++) {
      tmp[i] += y[i]; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x + y
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, double[] y) {
    double[] tmp = dgemv(aMatrix, aVector.getData(), y);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x + y
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, double[] aVector, DoubleMatrix1D y) {
    double[] tmp = dgemv(aMatrix, aVector, y.getData());
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x + y
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y) {
    double[] tmp = dgemv(aMatrix, aVector.getData(), y.getData());
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x + y
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(FullMatrix aMatrix, double[] aVector, double[] y) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
    final int cols = aMatrix.getNumberOfColumns();
    double[] tmp = new double[cols];
    tmp = dgemvTransposed(aMatrix, aVector);
    for (int i = 0; i < cols; i++) {
      tmp[i] += y[i];
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x + y
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(FullMatrix aMatrix, DoubleMatrix1D aVector, double[] y) {
    double[] tmp = dgemvTransposed(aMatrix, aVector.getData(), y);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x + y
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(FullMatrix aMatrix, double[] aVector, DoubleMatrix1D y) {
    double[] tmp = dgemvTransposed(aMatrix, aVector, y.getData());
    return tmp;
  }


  /**
   * DGEMV simplified: returns:=A^T*x + y
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(FullMatrix aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y) {
    double[] tmp = dgemvTransposed(aMatrix, aVector.getData(), y.getData());
    return tmp;
  }

  /* GROUP4:: alpha*A*x + y OR alpha*A^T*x + y */
  /**
   * DGEMV simplified: returns:= alpha*A*x + y OR returns:=alpha*A^T*x + y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double[] aVector, double[] y, BLAS2.orientation o) {
    double[] tmp = null;
    switch (o) {
      case normal:
        tmp = dgemv(alpha, aMatrix, aVector, y);
        break;
      case transposed:
        tmp = dgemvTransposed(alpha, aMatrix, aVector, y);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:= alpha*A*x + y OR returns:=alpha*A^T*x + y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double[] y, BLAS2.orientation o) {
    double[] tmp = dgemv(alpha, aMatrix, aVector.getData(), y, o);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:= alpha*A*x + y OR returns:=alpha*A^T*x + y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double[] aVector, DoubleMatrix1D y, BLAS2.orientation o) {
    double[] tmp = dgemv(alpha, aMatrix, aVector, y.getData(), o);
    return tmp;
  }


  /**
   * DGEMV simplified: returns:= alpha*A*x + y OR returns:=alpha*A^T*x + y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y, BLAS2.orientation o) {
    double[] tmp = dgemv(alpha, aMatrix, aVector.getData(), y.getData(), o);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x + y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double[] aVector, double[] y) {
    dgemvInputSanityChecker(aMatrix, aVector, y);
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = dgemv(aMatrix, aVector);
    for (int i = 0; i < rows; i++) {
      tmp[i] = alpha * tmp[i] + y[i]; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x + y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double[] y) {
    double[] tmp = dgemv(alpha, aMatrix, aVector.getData(), y);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x + y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double[] aVector, DoubleMatrix1D y) {
    double[] tmp = dgemv(alpha, aMatrix, aVector, y.getData());
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x + y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y) {
    double[] tmp = dgemv(alpha, aMatrix, aVector.getData(), y.getData());
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x + y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, double[] aVector, double[] y) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
    final int cols = aMatrix.getNumberOfColumns();
    double[] tmp = new double[cols];
    tmp = dgemvTransposed(aMatrix, aVector);
    for (int i = 0; i < cols; i++) {
      tmp[i] = alpha * tmp[i] + y[i];
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x + y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double[] y) {
    double[] tmp = dgemvTransposed(alpha, aMatrix, aVector.getData(), y);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x + y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, double[] aVector, DoubleMatrix1D y) {
    double[] tmp = dgemvTransposed(alpha, aMatrix, aVector, y.getData());
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x + y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y) {
    double[] tmp = dgemvTransposed(alpha, aMatrix, aVector.getData(), y.getData());
    return tmp;
  }

  /* GROUP5:: A*x + beta*y OR A^T*x + beta*y */
  /**
   * DGEMV simplified: returns:= A*x + beta*y OR returns:=A^T*x + beta*y  depending on the enum orientation.
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, double[] aVector, double beta, double[] y, BLAS2.orientation o) {
    double[] tmp = null;
    switch (o) {
      case normal:
        tmp = dgemv(aMatrix, aVector, beta, y);
        break;
      case transposed:
        tmp = dgemvTransposed(aMatrix, aVector, beta, y);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:= A*x + beta*y OR returns:=A^T*x + beta*y  depending on the enum orientation.
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, double[] y, BLAS2.orientation o) {
    double[] tmp = dgemv(aMatrix, aVector.getData(), beta, y, o);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:= A*x + beta*y OR returns:=A^T*x + beta*y  depending on the enum orientation.
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, double[] aVector, double beta, DoubleMatrix1D y, BLAS2.orientation o) {
    double[] tmp = dgemv(aMatrix, aVector, beta, y.getData(), o);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:= A*x + beta*y OR returns:=A^T*x + beta*y  depending on the enum orientation.
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y, BLAS2.orientation o) {
    double[] tmp = dgemv(aMatrix, aVector.getData(), beta, y.getData(), o);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x + beta*y
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, double[] aVector, double beta, double[] y) {
    dgemvInputSanityChecker(aMatrix, aVector, y);
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = dgemv(aMatrix, aVector);
    for (int i = 0; i < rows; i++) {
      tmp[i] += beta * y[i]; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x + beta*y
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, double[] y) {
    double[] tmp = dgemv(aMatrix, aVector.getData(), beta, y);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x + beta*y
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, double[] aVector, double beta, DoubleMatrix1D y) {
    double[] tmp = dgemv(aMatrix, aVector, beta, y.getData());
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x + beta*y
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y) {
    double[] tmp = dgemv(aMatrix, aVector.getData(), beta, y.getData());
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(FullMatrix aMatrix, double[] aVector, double beta, double[] y) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
    final int cols = aMatrix.getNumberOfColumns();
    double[] tmp = new double[cols];
    tmp = dgemvTransposed(aMatrix, aVector);
    for (int i = 0; i < cols; i++) {
      tmp[i] += beta * y[i];
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, double[] y) {
    double[] tmp = dgemvTransposed(aMatrix, aVector.getData(), beta, y);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @return tmp a double vector
   */
  public static double[] dgemvTransposed(FullMatrix aMatrix, double[] aVector, double beta, DoubleMatrix1D y) {
    double[] tmp = dgemvTransposed(aMatrix, aVector, beta, y.getData());
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y) {
    double[] tmp = dgemvTransposed(aMatrix, aVector.getData(), beta, y.getData());
    return tmp;
  }

  /* GROUP6:: alpha*A*x + beta*y OR alpha*A^T*x + beta*y */
  /**
   * DGEMV FULL: returns:= alpha*A*x + beta*y OR returns:=alpha*A^T*x + beta*y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double[] aVector, double beta, double[] y, BLAS2.orientation o) {
    double[] tmp = null;
    switch (o) {
      case normal:
        tmp = dgemv(alpha, aMatrix, aVector, beta, y);
        break;
      case transposed:
        tmp = dgemvTransposed(alpha, aMatrix, aVector, beta, y);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV FULL: returns:= alpha*A*x + beta*y OR returns:=alpha*A^T*x + beta*y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, double[] y, BLAS2.orientation o) {
    double[] tmp = dgemv(alpha, aMatrix, aVector.getData(), beta, y, o);
    return tmp;
  }

  /**
   * DGEMV FULL: returns:= alpha*A*x + beta*y OR returns:=alpha*A^T*x + beta*y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double[] aVector, double beta, DoubleMatrix1D y, BLAS2.orientation o) {
    double[] tmp = dgemv(alpha, aMatrix, aVector, beta, y.getData(), o);
    return tmp;
  }

  /**
   * DGEMV FULL: returns:= alpha*A*x + beta*y OR returns:=alpha*A^T*x + beta*y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y, BLAS2.orientation o) {
    double[] tmp = dgemv(alpha, aMatrix, aVector.getData(), beta, y.getData(), o);
    return tmp;
  }

  /**
   * DGEMV FULL: returns:=alpha*A*x + beta*y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double[] aVector, double beta, double[] y) {
    dgemvInputSanityChecker(aMatrix, aVector, y);
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = dgemv(aMatrix, aVector);
    for (int i = 0; i < rows; i++) {
      tmp[i] = alpha * tmp[i] + beta * y[i]; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   * DGEMV FULL: returns:=alpha*A*x + beta*y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, double[] y) {
    double[] tmp = dgemv(alpha, aMatrix, aVector.getData(), beta, y);
    return tmp;
  }

  /**
   * DGEMV FULL: returns:=alpha*A*x + beta*y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double[] aVector, double beta, DoubleMatrix1D y) {
    double[] tmp = dgemv(alpha, aMatrix, aVector, beta, y.getData());
    return tmp;
  }

  /**
   * DGEMV FULL: returns:=alpha*A*x + beta*y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y) {
    double[] tmp = dgemv(alpha, aMatrix, aVector.getData(), beta, y.getData());
    return tmp;
  }

  /**
   * DGEMV FULL: returns:=alpha*A^T*x + beta*y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, double[] aVector, double beta, double[] y) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
    final int cols = aMatrix.getNumberOfColumns();
    double[] tmp = new double[cols];
    tmp = dgemvTransposed(aMatrix, aVector);
    for (int i = 0; i < cols; i++) {
      tmp[i] = alpha * tmp[i] + beta * y[i];
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, double[] y) {
    double[] tmp = dgemvTransposed(alpha, aMatrix, aVector.getData(), beta, y);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, double[] aVector, double beta, DoubleMatrix1D y) {
    double[] tmp = dgemvTransposed(alpha, aMatrix, aVector, beta, y.getData());
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y) {
    double[] tmp = dgemvTransposed(alpha, aMatrix, aVector.getData(), beta, y.getData());
    return tmp;
  }

  /* Statefull manipulators on the FullMatrix type */
  //** group 1:: y=A*x OR y=A^T*x
  /**
   * DGEMV simplified: y:=A*x OR y:=A^T*x depending on the enum orientation.
   * @param y a double[] vector that will be altered to contain A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   */
  public static void dgemvInPlace(double[] y, FullMatrix aMatrix, double[] aVector, BLAS2.orientation o) {
    switch (o) {
      case normal:
        dgemvInPlace(y, aMatrix, aVector);
        break;
      case transposed:
        dgemvInPlaceTransposed(y, aMatrix, aVector);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
  }

  /**
   * DGEMV simplified: y:=A*x OR y:=A^T*x depending on the enum orientation.
   * @param y a DoubleMatrix1D vector that will be altered to contain A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   */
  public static void dgemvInPlace(DoubleMatrix1D y, FullMatrix aMatrix, double[] aVector, BLAS2.orientation o) {
    dgemvInPlace(y.getData(), aMatrix, aVector, o);
  }

  /**
   * DGEMV simplified: y:=A*x OR y:=A^T*x depending on the enum orientation.
   * @param y a double[] vector that will be altered to contain A*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   */
  public static void dgemvInPlace(double[] y, FullMatrix aMatrix, DoubleMatrix1D aVector, BLAS2.orientation o) {
    dgemvInPlace(y, aMatrix, aVector.getData(), o);
  }

  /**
   * DGEMV simplified: y:=A*x OR y:=A^T*x depending on the enum orientation.
   * @param y a DoubleMatrix1D vector that will be altered to contain A*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   */
  public static void dgemvInPlace(DoubleMatrix1D y, FullMatrix aMatrix, DoubleMatrix1D aVector, BLAS2.orientation o) {
    dgemvInPlace(y.getData(), aMatrix, aVector.getData(), o);
  }

  /**
   * DGEMV simplified: y:=A*x
   * @param y a double[] vector that will be altered to contain A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   */
  public static void dgemvInPlace(double[] y, FullMatrix aMatrix, double[] aVector) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    final int rows = aMatrix.getNumberOfRows();
    final int cols = aMatrix.getNumberOfColumns();
    assert (y.length == rows);
    double[] ptrA = aMatrix.getData();
    int idx, ptr;
    final int extra = cols - cols % 8;
    final int ub = ((cols / 8) * 8) - 1;
    double acc;
    for (int i = 0; i < rows; i++) {
      idx = i * cols;
      acc = 0;
      y[i] = 0;
      for (int j = 0; j < ub; j += 8) {
        ptr = idx + j;
        y[i] += ptrA[ptr] * aVector[j]
            + ptrA[ptr + 1] * aVector[j + 1]
            + ptrA[ptr + 2] * aVector[j + 2]
            + ptrA[ptr + 3] * aVector[j + 3];
        acc += ptrA[ptr + 4] * aVector[j + 4]
            + ptrA[ptr + 5] * aVector[j + 5]
            + ptrA[ptr + 6] * aVector[j + 6]
            + ptrA[ptr + 7] * aVector[j + 7];
      }
      y[i] += acc;
      for (int j = extra; j < cols; j++) {
        y[i] += ptrA[idx + j] * aVector[j];
      }
    }

  }

  /**
   * DGEMV simplified: y:=A*x
   * @param y a double vector that will be altered to contain A*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   */
  public static void dgemvInPlace(double[] y, FullMatrix aMatrix, DoubleMatrix1D aVector) {
    dgemvInPlace(y, aMatrix, aVector.getData());
  }

  /**
   * DGEMV simplified: y:=A*x
   * @param y a DoubleMatrix1D that will be altered to contain A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   */
  public static void dgemvInPlace(DoubleMatrix1D y, FullMatrix aMatrix, double[] aVector) {
    dgemvInPlace(y.getData(), aMatrix, aVector);
  }

  /**
   * DGEMV simplified: y:=A*x
   * @param y a DoubleMatrix1D that will be altered to contain A*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   */
  public static void dgemvInPlace(DoubleMatrix1D y, FullMatrix aMatrix, DoubleMatrix1D aVector) {
    dgemvInPlace(y.getData(), aMatrix, aVector.getData());
  }

  /**
   * DGEMV simplified: y:=A^T*x
   * @param y a double[] vector that will be altered to contain A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   */
  public static void dgemvInPlaceTransposed(double[] y, FullMatrix aMatrix, double[] aVector) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
    final int rows = aMatrix.getNumberOfRows();
    final int cols = aMatrix.getNumberOfColumns();
    double[] ptrA = aMatrix.getData();
    for (int i = 0; i < cols; i++) {
      y[i] = 0;
      for (int j = 0; j < rows; j++) {
        y[i] += ptrA[i + j * cols] * aVector[j];
      }
    }
  }

  /**
   * DGEMV simplified: y:=A^T*x
   * @param y a double vector that will be altered to contain A*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   */
  public static void dgemvInPlaceTransposed(double[] y, FullMatrix aMatrix, DoubleMatrix1D aVector) {
    dgemvInPlaceTransposed(y, aMatrix, aVector.getData());
  }

  /**
   * DGEMV simplified: y:=A^T*x
   * @param y a DoubleMatrix1D that will be altered to contain A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   */
  public static void dgemvInPlaceTransposed(DoubleMatrix1D y, FullMatrix aMatrix, double[] aVector) {
    dgemvInPlaceTransposed(y.getData(), aMatrix, aVector);
  }

  /**
   * DGEMV simplified: y:=A^T*x
   * @param y a DoubleMatrix1D that will be altered to contain A*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   */
  public static void dgemvInPlaceTransposed(DoubleMatrix1D y, FullMatrix aMatrix, DoubleMatrix1D aVector) {
    dgemvInPlaceTransposed(y.getData(), aMatrix, aVector.getData());
  }

  //** group 2:: y=alpha*A*x OR y=alpha*A^T*x
  //** group 3:: y=A*x+y OR y=A^T*x+y
  //** group 4:: y=alpha*A*x+y OR y=alpha*A^T*x+y
  //** group 5:: y=A*x+beta*y OR y=A^T*x+beta*y
  //** group 6:: y=alpha*A*x+beta*y OR y=alpha*A^T*x+beta*y

  /**
   * DGEMV FULL/simplified: y:=alpha*A*x + beta*y OR y:=alpha*A*x
   * NOTE: This method uses a short cut if alpha is set to 1 OR beta is set to 0 or 1.
   * If alpha is set to 0 the method continues anyway but is insanely wasteful (use a BLAS1 call instead!).
   * The following decision tree is used:
   * if (alpha == 1) then:
   *   if (beta == 0) then:
   *     y:=A*x
   *   elseif (beta == 1) then:
   *     y:=A*x + y
   *   else
   *     y:=A*x + beta*y
   *   end
   * else // alpha!=1
   *   if (beta == 0) then:
   *     y:=alpha*A*x
   *   elseif (beta == 1) then:
   *     y:=alpha*A*x + y
   *   else
   *     y:=alpha*A*x + beta*y
   *   end
   * end
   *
   * NOTE: The reason for the method not mirroring the stateless method is that the signatures are
   * identical for a number of the methods due to writebacks to vector "y"
   * @param y a double[] vector that will be altered to contain alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param beta a double indicating the scaling of y
   * @param aVector a double[] vector
   */
  public static void dgemvInPlace(double[] y, double alpha, FullMatrix aMatrix, double beta, double[] aVector) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    final int rows = aMatrix.getNumberOfRows();
    final int cols = aMatrix.getNumberOfColumns();
    double[] ptrA = aMatrix.getData();
    if (Double.doubleToLongBits(alpha) == 1) {
      if (Double.doubleToLongBits(beta) == 0) {
        dgemvInPlace(y, aMatrix, aVector);
      } else if (Double.doubleToLongBits(beta) == 0) {
        axplusy(y, ptrA, aVector, rows, cols);
      } else {
        axplusbetay(y, ptrA, beta, aVector, rows, cols);
      }
    } else {
      if (Double.doubleToLongBits(beta) == 0) {
        alphaAx(y, alpha, ptrA, aVector, rows, cols);
      } else {
        alphaAxplusbetay(y, alpha, ptrA, beta, aVector, rows, cols);
      }
    }
  }

  /**
   * DGEMV FULL/simplified: y:=alpha*A*x + beta*y OR y:=alpha*A*x
   * NOTE: This method uses a short cut if alpha is set to 1 OR beta is set to 0 or 1.
   * If alpha is set to 0 the method continues anyway but is insanely wasteful (use a BLAS1 call instead!).
   * The following decision tree is used:
   * if (alpha == 1) then:
   *   if (beta == 0) then:
   *     y:=A*x
   *   elseif (beta == 1) then:
   *     y:=A*x + y
   *   else
   *     y:=A*x + beta*y
   *   end
   * else // alpha!=1
   *   if (beta == 0) then:
   *     y:=alpha*A*x
   *   elseif (beta == 1) then:
   *     y:=alpha*A*x + y
   *   else
   *     y:=alpha*A*x + beta*y
   *   end
   * end
   *
   * NOTE: The reason for the method not mirroring the stateless method is that the signatures are
   * identical for a number of the methods due to writebacks to vector "y"
   * @param y a double[] vector that will be altered to contain alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param beta a double indicating the scaling of y
   * @param aVector a DoubleMatrix1D vector
   */
  public static void dgemvInPlace(double[] y, double alpha, FullMatrix aMatrix, double beta, DoubleMatrix1D aVector) {
    dgemvInPlace(y, alpha, aMatrix, beta, aVector.getData());
  }

  /**
   * DGEMV FULL/simplified: y:=alpha*A*x + beta*y OR y:=alpha*A*x
   * NOTE: This method uses a short cut if alpha is set to 1 OR beta is set to 0 or 1.
   * If alpha is set to 0 the method continues anyway but is insanely wasteful (use a BLAS1 call instead!).
   * The following decision tree is used:
   * if (alpha == 1) then:
   *   if (beta == 0) then:
   *     y:=A*x
   *   elseif (beta == 1) then:
   *     y:=A*x + y
   *   else
   *     y:=A*x + beta*y
   *   end
   * else // alpha!=1
   *   if (beta == 0) then:
   *     y:=alpha*A*x
   *   elseif (beta == 1) then:
   *     y:=alpha*A*x + y
   *   else
   *     y:=alpha*A*x + beta*y
   *   end
   * end
   *
   * NOTE: The reason for the method not mirroring the stateless method is that the signatures are
   * identical for a number of the methods due to writebacks to vector "y"
   * @param y a DoubleMatrix1D vector that will be altered to contain alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param beta a double indicating the scaling of y
   * @param aVector a double[] vector
   */
  public static void dgemvInPlace(DoubleMatrix1D y, double alpha, FullMatrix aMatrix, double beta, double[] aVector) {
    dgemvInPlace(y.getData(), alpha, aMatrix, beta, aVector);
  }

  /**
   * DGEMV FULL/simplified: y:=alpha*A*x + beta*y OR y:=alpha*A*x
   * NOTE: This method uses a short cut if alpha is set to 1 OR beta is set to 0 or 1.
   * If alpha is set to 0 the method continues anyway but is insanely wasteful (use a BLAS1 call instead!).
   * The following decision tree is used:
   * if (alpha == 1) then:
   *   if (beta == 0) then:
   *     y:=A*x
   *   elseif (beta == 1) then:
   *     y:=A*x + y
   *   else
   *     y:=A*x + beta*y
   *   end
   * else // alpha!=1
   *   if (beta == 0) then:
   *     y:=alpha*A*x
   *   elseif (beta == 1) then:
   *     y:=alpha*A*x + y
   *   else
   *     y:=alpha*A*x + beta*y
   *   end
   * end
   *
   * NOTE: The reason for the method not mirroring the stateless method is that the signatures are
   * identical for a number of the methods due to writebacks to vector "y"
   * @param y a DoubleMatrix1D vector that will be altered to contain alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param beta a double indicating the scaling of y
   * @param aVector a DoubleMatrix1D vector
   */
  public static void dgemvInPlace(DoubleMatrix1D y, double alpha, FullMatrix aMatrix, double beta, DoubleMatrix1D aVector) {
    dgemvInPlace(y.getData(), alpha, aMatrix, beta, aVector.getData());
  }

  /* HELPER FUNCTIONS */
  /** 4 helper functions for STATEFULL FULL/simplified alpha ?* A*x ?+ beta*y */
  // alpha*A*x
  private static void alphaAx(double[] y, double alpha, double[] ptrA, double[] aVector, int rows, int cols) {
    double alphaTmp;
    int idx, ptr;
    final int extra = cols - cols % 8;
    final int ub = ((cols / 8) * 8) - 1;
    double acc = 0;
    for (int i = 0; i < rows; i++) {
      y[i] = 0;
      idx = i * cols;
      alphaTmp = 0;
      acc = 0;
      for (int j = 0; j < ub; j += 8) {
        ptr = idx + j;
        alphaTmp += ptrA[ptr] * aVector[j]
                 + ptrA[ptr + 1] * aVector[j + 1]
                 + ptrA[ptr + 2] * aVector[j + 2]
                 + ptrA[ptr + 3] * aVector[j + 3];
        acc += ptrA[ptr + 4] * aVector[j + 4]
               + ptrA[ptr + 5] * aVector[j + 5]
               + ptrA[ptr + 6] * aVector[j + 6]
               + ptrA[ptr + 7] * aVector[j + 7];
      }
      y[i] = (alphaTmp + acc) * alpha;
      for (int j = extra; j < cols; j++) {
        y[i] += alpha * ptrA[idx + j] * aVector[j];
      }
    }
  }

  // A*x + beta*y
  private static void axplusbetay(double[] y, double[] ptrA, double beta, double[] aVector, int rows, int cols) {
    double alphaTmp;
    int idx, ptr, extra, ub;
    extra = cols - cols % 4;
    ub = ((cols / 4) * 4) - 1;
    for (int i = 0; i < rows; i++) {
      idx = i * cols;
      alphaTmp = 0;
      for (int j = 0; j < ub; j += 4) {
        ptr = idx + j;
        alphaTmp += ptrA[ptr] * aVector[j]
                 + ptrA[ptr + 1] * aVector[j + 1]
                 + ptrA[ptr + 2] * aVector[j + 2]
                 + ptrA[ptr + 3] * aVector[j + 3];
      }
      for (int j = extra; j < cols; j++) {
        alphaTmp += ptrA[idx + j] * aVector[j];
      }
      y[i] = alphaTmp + beta * y[i];
    }
  }

  // A*x + y
  private static void axplusy(double[] y, double[] ptrA, double[] aVector, int rows, int cols) {
    double alphaTmp;
    int idx, ptr, extra, ub;
    extra = cols - cols % 4;
    ub = ((cols / 4) * 4) - 1;
    for (int i = 0; i < rows; i++) {
      idx = i * cols;
      alphaTmp = 0;
      for (int j = 0; j < ub; j += 4) {
        ptr = idx + j;
        alphaTmp += ptrA[ptr] * aVector[j]
                 + ptrA[ptr + 1] * aVector[j + 1]
                 + ptrA[ptr + 2] * aVector[j + 2]
                 + ptrA[ptr + 3] * aVector[j + 3];
      }
      for (int j = extra; j < cols; j++) {
        alphaTmp += ptrA[idx + j] * aVector[j];
      }
      y[i] += alphaTmp;
    }
  }

  // alpha*A*x + beta*y
  private static void alphaAxplusbetay(double[] y, double alpha, double[] ptrA, double beta, double[] aVector, int rows, int cols) {
    double alphaTmp;
    int idx, ptr, extra, ub;
    extra = cols - cols % 4;
    ub = ((cols / 4) * 4) - 1;
    for (int i = 0; i < rows; i++) {
      idx = i * cols;
      y[i] = beta * y[i];
      alphaTmp = 0.;
      for (int j = 0; j < ub; j += 4) {
        ptr = idx + j;
        alphaTmp += ptrA[ptr] * aVector[j]
                 + ptrA[ptr + 1] * aVector[j + 1]
                 + ptrA[ptr + 2] * aVector[j + 2]
                 + ptrA[ptr + 3] * aVector[j + 3];
      }
      y[i] += alphaTmp * alpha;
      for (int j = extra; j < cols; j++) {
        y[i] += alpha * ptrA[idx + j] * aVector[j];
      }
    }
  }


  /**
   * DGEMV FULL/simplified: y:=alpha*A*x + beta*y OR y:=alpha*A*x
   * NOTE: This method uses a short cut if alpha is set to 1 OR beta is set to 0 or 1.
   * If alpha is set to 0 the method continues anyway but is insanely wasteful (use a BLAS1 call instead!).
   * The following decision tree is used:
   * if (alpha == 1) then:
   *   if (beta == 0) then:
   *     y:=A*x
   *   elseif (beta == 1) then:
   *     y:=A*x + y
   *   else
   *     y:=A*x + beta*y
   *   end
   * else // alpha!=1
   *   if (beta == 0) then:
   *     y:=alpha*A*x
   *   elseif (beta == 1) then:
   *     y:=alpha*A*x + y
   *   else
   *     y:=alpha*A*x + beta*y
   *   end
   * end
   *
   * NOTE: The reason for the method not mirroring the stateless method is that the signatures are
   * identical for a number of the methods due to writebacks to vector "y"
   * @param y a double[] vector that will be altered to contain alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param beta a double indicating the scaling of y
   * @param aVector a double[] vector
   */
  public static void dgemvInPlaceTransposed(double[] y, double alpha, FullMatrix aMatrix, double beta, double[] aVector) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
    final int rows = aMatrix.getNumberOfRows();
    final int cols = aMatrix.getNumberOfColumns();
    double[] ptrA = aMatrix.getData();
    if (Double.doubleToLongBits(alpha) == 1) {
      if (Double.doubleToLongBits(beta) == 0) {
        dgemvInPlaceTransposed(y, aMatrix, aVector);
      } else if (Double.doubleToLongBits(beta) == 0) {
        aTranposedxplusy(y, ptrA, aVector, rows, cols);
      } else {
        aTranposedxplusbetay(y, ptrA, beta, aVector, rows, cols);
      }
    } else {
      if (Double.doubleToLongBits(beta) == 0) {
        alphaATranposedx(y, alpha, ptrA, aVector, rows, cols);
      } else {
        alphaATranposedxplusbetay(y, alpha, ptrA, beta, aVector, rows, cols);
      }
    }
  }

  /**
   * DGEMV FULL/simplified: y:=alpha*A*x + beta*y OR y:=alpha*A*x
   * NOTE: This method uses a short cut if alpha is set to 1 OR beta is set to 0 or 1.
   * If alpha is set to 0 the method continues anyway but is insanely wasteful (use a BLAS1 call instead!).
   * The following decision tree is used:
   * if (alpha == 1) then:
   *   if (beta == 0) then:
   *     y:=A*x
   *   elseif (beta == 1) then:
   *     y:=A*x + y
   *   else
   *     y:=A*x + beta*y
   *   end
   * else // alpha!=1
   *   if (beta == 0) then:
   *     y:=alpha*A*x
   *   elseif (beta == 1) then:
   *     y:=alpha*A*x + y
   *   else
   *     y:=alpha*A*x + beta*y
   *   end
   * end
   *
   * NOTE: The reason for the method not mirroring the stateless method is that the signatures are
   * identical for a number of the methods due to writebacks to vector "y"
   * @param y a double[] vector that will be altered to contain alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param beta a double indicating the scaling of y
   * @param aVector a DoubleMatrix1D vector
   */
  public static void dgemvInPlaceTransposed(double[] y, double alpha, FullMatrix aMatrix, double beta, DoubleMatrix1D aVector) {
    dgemvInPlaceTransposed(y, alpha, aMatrix, beta, aVector.getData());
  }

  /**
   * DGEMV FULL/simplified: y:=alpha*A*x + beta*y OR y:=alpha*A*x
   * NOTE: This method uses a short cut if alpha is set to 1 OR beta is set to 0 or 1.
   * If alpha is set to 0 the method continues anyway but is insanely wasteful (use a BLAS1 call instead!).
   * The following decision tree is used:
   * if (alpha == 1) then:
   *   if (beta == 0) then:
   *     y:=A*x
   *   elseif (beta == 1) then:
   *     y:=A*x + y
   *   else
   *     y:=A*x + beta*y
   *   end
   * else // alpha!=1
   *   if (beta == 0) then:
   *     y:=alpha*A*x
   *   elseif (beta == 1) then:
   *     y:=alpha*A*x + y
   *   else
   *     y:=alpha*A*x + beta*y
   *   end
   * end
   *
   * NOTE: The reason for the method not mirroring the stateless method is that the signatures are
   * identical for a number of the methods due to writebacks to vector "y"
   * @param y a DoubleMatrix1D vector that will be altered to contain alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param beta a double indicating the scaling of y
   * @param aVector a double[] vector
   */
  public static void dgemvInPlaceTransposed(DoubleMatrix1D y, double alpha, FullMatrix aMatrix, double beta, double[] aVector) {
    dgemvInPlaceTransposed(y.getData(), alpha, aMatrix, beta, aVector);
  }

  /**
   * DGEMV FULL/simplified: y:=alpha*A*x + beta*y OR y:=alpha*A*x
   * NOTE: This method uses a short cut if alpha is set to 1 OR beta is set to 0 or 1.
   * If alpha is set to 0 the method continues anyway but is insanely wasteful (use a BLAS1 call instead!).
   * The following decision tree is used:
   * if (alpha == 1) then:
   *   if (beta == 0) then:
   *     y:=A*x
   *   elseif (beta == 1) then:
   *     y:=A*x + y
   *   else
   *     y:=A*x + beta*y
   *   end
   * else // alpha!=1
   *   if (beta == 0) then:
   *     y:=alpha*A*x
   *   elseif (beta == 1) then:
   *     y:=alpha*A*x + y
   *   else
   *     y:=alpha*A*x + beta*y
   *   end
   * end
   *
   * NOTE: The reason for the method not mirroring the stateless method is that the signatures are
   * identical for a number of the methods due to writebacks to vector "y"
   * @param y a DoubleMatrix1D vector that will be altered to contain alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param beta a double indicating the scaling of y
   * @param aVector a DoubleMatrix1D vector
   */
  public static void dgemvInPlaceTransposed(DoubleMatrix1D y, double alpha, FullMatrix aMatrix, double beta, DoubleMatrix1D aVector) {
    dgemvInPlaceTransposed(y.getData(), alpha, aMatrix, beta, aVector.getData());
  }

  /** 4 helper functions for STATEFULL FULL/simplified ****TRANSPOSED**** alpha*A^T*x ?+ beta*y */
  private static void alphaATranposedx(double[] y, double alpha, double[] ptrA, double[] aVector, int rows, int cols) {
    double alphaTmp;
    for (int i = 0; i < cols; i++) {
      alphaTmp = 0;
      for (int j = 0; j < rows; j++) {
        alphaTmp += ptrA[i + j * cols] * aVector[j];
      }
      y[i] = alphaTmp * alpha;
    }
  }

  private static void aTranposedxplusbetay(double[] y, double[] ptrA, double beta, double[] aVector, int rows, int cols) {
    double alphaTmp;
    for (int i = 0; i < cols; i++) {
      alphaTmp = 0;
      for (int j = 0; j < rows; j++) {
        alphaTmp += ptrA[i + j * cols] * aVector[j];
      }
      y[i] = alphaTmp + beta * y[i];
    }
  }

  private static void aTranposedxplusy(double[] y, double[] ptrA, double[] aVector, int rows, int cols) {
    double alphaTmp;
    for (int i = 0; i < cols; i++) {
      alphaTmp = 0;
      for (int j = 0; j < rows; j++) {
        alphaTmp += ptrA[i + j * cols] * aVector[j];
      }
      y[i] += alphaTmp;
    }
  }

  private static void alphaATranposedxplusbetay(double[] y, double alpha, double[] ptrA, double beta, double[] aVector, int rows, int cols) {
    double alphaTmp;
    for (int i = 0; i < cols; i++) {
      alphaTmp = 0;
      for (int j = 0; j < rows; j++) {
        alphaTmp += ptrA[i + j * cols] * aVector[j];
      }
      y[i] = alphaTmp * alpha + beta * y[i];
    }
  }

  /*  -------------------------------------------------------------------------------------------------------------------------------------------------- */

  /**
   * Sparse matrix types.
   *
   * Sparse DGEMV is notoriously horrible in terms of optimisation.
   * Depending on the storage format, there are usually mixtures of stride 1 accesses in some data and essentially random lookups in others.
   *
   *  TODO: To attempt to mitigate some of the cache miss horror this causes (without going down the route of ELLPACK or Z-tree compression or similar)
   * the routines attempt to do extra fetches where possible to try and improve cache performance. Protocode exists for this, but causes major headaches,
   * need to fix and implement cleanly.
   *
   *   TODO: Sort out a more cunning method of performing these sorts of operations. The "optimised" versions I attempted only achieve ~10%
   *   speed up over the naive version, which is not acceptable!
   *
   */

  /* GROUP1:: A*x OR A^T*x */
  /**
   * DGEMV simplified: returns:=A*x OR returns:=A^T*x depending on the enum orientation.
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, double[] aVector, orientation o) {
    double[] tmp = null;
    switch (o) {
      case normal:
        tmp = dgemv(aMatrix, aVector);
        break;
      case transposed:
        tmp = dgemvTransposed(aMatrix, aVector);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector, orientation o) {
    return dgemv(aMatrix, aVector.getData(), o);
  }

  /**
   * DGEMV simplified: returns:=A*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, double[] aVector) {
    dgemvInputSanityChecker(aMatrix, aVector);
    final int[] rowPtr = aMatrix.getRowPtr();
    final int[] colIdx = aMatrix.getColumnIndex();
    final double[] values = aMatrix.getNonZeroElements();
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = new double[rows];
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        tmp[i] += values[ptr] * aVector[colIdx[ptr]];
        ptr++;
      }
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector) {
    return dgemv(aMatrix, aVector.getData());
  }

  /**
   * DGEMV simplified: returns:=A^T*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(CompressedSparseRowFormatMatrix aMatrix, double[] aVector) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector);
    final int[] rowPtr = aMatrix.getRowPtr();
    final double[] values = aMatrix.getNonZeroElements();
    final int[] colIdx = aMatrix.getColumnIndex();
    final int rows = aMatrix.getNumberOfRows();
    final int cols = aMatrix.getNumberOfColumns();
    double[] tmp = new double[cols];
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        tmp[colIdx[ptr]] += values[ptr] * aVector[i];
        ptr++;
      }
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector) {
    return dgemvTransposed(aMatrix, aVector.getData());
  }

  /* GROUP2:: alpha*A*x OR alpha*A^T*x */
  /**
   * DGEMV simplified: returns:=alpha*A*x OR returns:=alpha*A^T*x depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, CompressedSparseRowFormatMatrix aMatrix, double[] aVector, orientation o) {
    double[] tmp = null;
    switch (o) {
      case normal:
        tmp = dgemv(alpha, aMatrix, aVector);
        break;
      case transposed:
        tmp = dgemvTransposed(alpha, aMatrix, aVector);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x OR returns:=alpha*A^T*x depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector, orientation o) {
    return dgemv(alpha, aMatrix, aVector.getData(), o);
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, CompressedSparseRowFormatMatrix aMatrix, double[] aVector) {
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = dgemv(aMatrix, aVector);
    for (int i = 0; i < rows; i++) {
      tmp[i] *= alpha;
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector) {
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = dgemv(aMatrix, aVector.getData());
    for (int i = 0; i < rows; i++) {
      tmp[i] *= alpha;
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(double alpha, CompressedSparseRowFormatMatrix aMatrix, double[] aVector) {
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = dgemvTransposed(aMatrix, aVector);
    for (int i = 0; i < rows; i++) {
      tmp[i] *= alpha;
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(double alpha, CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector) {
    return dgemvTransposed(alpha, aMatrix, aVector.getData());
  }

  /* GROUP3:: A*x + y OR A^T*x + y */
  /**
   * DGEMV simplified: returns:=A*x + y OR returns:=A^T*x + y depending on the enum orientation.
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, double[] aVector, double[] y, orientation o) {
    double[] tmp = null;
    switch (o) {
      case normal:
        tmp = dgemv(aMatrix, aVector, y);
        break;
      case transposed:
        tmp = dgemvTransposed(aMatrix, aVector, y);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x + y OR returns:=A^T*x + y depending on the enum orientation.
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector, double[] y, orientation o) {
    return dgemv(aMatrix, aVector.getData(), y, o);
  }

  /**
   * DGEMV simplified: returns:=A*x + y OR returns:=A^T*x + y depending on the enum orientation.
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, double[] aVector, DoubleMatrix1D y, orientation o) {
    return dgemv(aMatrix, aVector, y.getData(), o);
  }

  /**
   * DGEMV simplified: returns:=A*x + y OR returns:=A^T*x + y depending on the enum orientation.
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return tmp a double[] vector
   */
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y, orientation o) {
    return dgemv(aMatrix, aVector.getData(), y.getData(), o);
  }

  /**
   * DGEMV simplified: returns:=A*x + y
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, double[] aVector, double[] y) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = dgemv(aMatrix, aVector);
    for (int i = 0; i < rows; i++) {
      tmp[i] += y[i];
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x + y
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector, double[] y) {
    return dgemv(aMatrix, aVector.getData(), y);
  }

  /**
   * DGEMV simplified: returns:=A^T*x + y
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(CompressedSparseRowFormatMatrix aMatrix, double[] aVector, double[] y) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
    final int cols = aMatrix.getNumberOfColumns();
    double[] tmp = new double[cols];
    tmp = dgemvTransposed(aMatrix, aVector);
    for (int i = 0; i < cols; i++) {
      tmp[i] += y[i];
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x + y
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector, double[] y) {
    double[] tmp = dgemvTransposed(aMatrix, aVector.getData(), y);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x + y
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y) {
    double[] tmp = dgemvTransposed(aMatrix, aVector.getData(), y.getData());
    return tmp;
  }

  /* GROUP4:: alpha*A*x + y or alpha*A^T*x + y */
  /**
   * DGEMV simplified: returns:=alpha*A*x + y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, CompressedSparseRowFormatMatrix aMatrix, double[] aVector, double[] y) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = dgemv(aMatrix, aVector);
    for (int i = 0; i < rows; i++) {
      tmp[i] = alpha * tmp[i] + y[i];
    }
    return tmp;
  }

  /* GROUP5:: A*x + beta*y or A^T*x + beta*y */

  /* GROUP6:: alpha*A*x + beta*y or alpha*A^T*x + beta*y */

  /**
   * DGEMV FULL: returns:=alpha*A*x + beta*y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, CompressedSparseRowFormatMatrix aMatrix, double[] aVector, double beta, double[] y) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    final int[] rowPtr = aMatrix.getRowPtr();
    final int[] colIdx = aMatrix.getColumnIndex();
    final double[] values = aMatrix.getNonZeroElements();
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = new double[rows];
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        tmp[i] += values[ptr] * aVector[colIdx[ptr]];
        ptr++;
      }

      tmp[i] = alpha * tmp[i] + beta * y[i];
    }
    return tmp;
  }

  /* Statefull manipulators on the CompressedSparseRowFormatMatrix type */

  /**
   * DGEMV simplified: y:=A*x
   * @param y a double vector that will be altered to contain A*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   */
  public static void dgemvInPlace(double[] y, CompressedSparseRowFormatMatrix aMatrix, double[] aVector) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    final int[] rowPtr = aMatrix.getRowPtr();
    final int[] colIdx = aMatrix.getColumnIndex();
    final double[] values = aMatrix.getNonZeroElements();
    final int rows = aMatrix.getNumberOfRows();
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      y[i] = 0;
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        y[i] += values[ptr] * aVector[colIdx[ptr]];
        ptr++;
      }
    }
  }

  /**
   * DGEMV simplified: y:=alpha*A*x + beta*y OR y:=alpha*A*x
   * NOTE: This method uses a short cut if beta is set to 0
   * @param y a double vector that will be altered to contain alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param beta a double indicating the scaling of y
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   */
  public static void dgemvInPlace(double[] y, double alpha, CompressedSparseRowFormatMatrix aMatrix, double[] aVector, double beta) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    if (beta == 0) {
      csralphaAx(y, alpha, aMatrix, aVector);
    } else {
      csralphaAxplusbetay(y, alpha, aMatrix, beta, aVector);
    }
  }

  /** 2 helper functions for alpha*A*x ?+ beta*y */
  private static void csralphaAx(double[] y, double alpha, CompressedSparseRowFormatMatrix aMatrix, double[] aVector) {
    final int[] rowPtr = aMatrix.getRowPtr();
    final int[] colIdx = aMatrix.getColumnIndex();
    final double[] values = aMatrix.getNonZeroElements();
    final int rows = aMatrix.getNumberOfRows();
    int ptr = 0;
    double alphaTmp;
    for (int i = 0; i < rows; i++) {
      alphaTmp = 0;
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        alphaTmp += values[ptr] * aVector[colIdx[ptr]];
        ptr++;
      }
      y[i] = alpha * alphaTmp;
    }
  }

  private static void csralphaAxplusbetay(double[] y, double alpha, CompressedSparseRowFormatMatrix aMatrix, double beta, double[] aVector) {
    final int[] rowPtr = aMatrix.getRowPtr();
    final int[] colIdx = aMatrix.getColumnIndex();
    final double[] values = aMatrix.getNonZeroElements();
    final int rows = aMatrix.getNumberOfRows();
    int ptr = 0;
    double alphaTmp;
    for (int i = 0; i < rows; i++) {
      alphaTmp = 0;
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        alphaTmp += values[ptr] * aVector[colIdx[ptr]];
        ptr++;
      }
      y[i] = alpha * alphaTmp + beta * y[i];
    }
  }

} // class end
