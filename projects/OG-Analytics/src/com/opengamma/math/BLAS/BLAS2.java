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
   * Enumeration based for the orientation of matrix A in the scheme
   * y := alpha*A*x + beta*y
   */
  public enum orientation { normal, transposed };

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
  public static void dgemvInputSanityCheckerTranspose(MatrixPrimitiveInterface aMatrix, double[] aVector) {
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
  public static void dgemvInputSanityCheckerTranspose(MatrixPrimitiveInterface aMatrix, double[] aVector, double[] addToVector) {
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
  public static double[] dgemv(FullMatrix aMatrix, double [] aVector, BLAS2.orientation o)
  {
    double [] tmp = null;
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
    double[] tmp = dgemv(aMatrix, aVector.toArray(), o);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, double [] aVector) {
    dgemvInputSanityChecker(aMatrix, aVector);
    final int rows = aMatrix.getNumberOfRows();
    final int cols = aMatrix.getNumberOfColumns();
    double[] tmp = new double[rows];
    double[] ptrA = aMatrix.getData();
    int idx, ptr, extra, ub;
    extra = cols - cols % 4;
    ub = ((cols / 4) * 4) - 1;
    for (int i = 0; i < rows; i++) {
      idx = i * cols;
      for (int j = 0; j < ub; j += 4) {
        ptr = idx + j;
        tmp[i] = ptrA[ptr] * aVector[j]
          + ptrA[ptr + 1] * aVector[j + 1]
          + ptrA[ptr + 2] * aVector[j + 2]
          + ptrA[ptr + 3] * aVector[j + 3]
          + tmp[i];
      }
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
    double[] tmp = dgemv(aMatrix, aVector.toArray());
    return tmp;
  }


  /**
   * DGEMV simplified: returns:=A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @return tmp a double[] vector
   * TODO: work out which unwinds/optimisations perform best for transposed matrices
   */
  public static double[] dgemvTransposed(FullMatrix aMatrix, double [] aVector) {
    dgemvInputSanityCheckerTranspose(aMatrix, aVector);
    final int rows = aMatrix.getNumberOfRows();
    final int cols = aMatrix.getNumberOfColumns();
    double[] tmp = new double[cols];
    double[] ptrA = aMatrix.getData();
    for (int i = 0; i < cols; i++) {
      for (int j = 0; j < rows; j++) {
        tmp[i] += ptrA[i + j * cols] * aVector[j];
      }
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A^T*x
   * @param aMatrix a FullMatrix
   * @param aVector a DoubleMatrix1D vector
   * @return tmp a double[] vector
   * TODO: work out which unwinds/optimisations perform best for transposed matrices
   */
  public static double[] dgemvTransposed(FullMatrix aMatrix, DoubleMatrix1D aVector) {
    double[] tmp = dgemvTransposed(aMatrix, aVector.toArray());
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
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double [] aVector, BLAS2.orientation o) {
    double [] tmp = null;
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
    double[] tmp = dgemv(alpha, aMatrix, aVector.toArray(), o);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double [] aVector) {
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
    double[] tmp = dgemv(alpha, aMatrix, aVector.toArray());
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
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, double [] aVector) {
    dgemvInputSanityCheckerTranspose(aMatrix, aVector);
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
    double[] tmp = dgemvTransposed(alpha, aMatrix, aVector.toArray());
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
  public static double[] dgemv(FullMatrix aMatrix, double [] aVector, double [] y, BLAS2.orientation o)
  {
    double [] tmp = null;
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
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, double [] y, BLAS2.orientation o)
  {
    double[] tmp = dgemv(aMatrix, aVector.toArray(), y , o);
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
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y, BLAS2.orientation o)
  {
    double[] tmp = dgemv(aMatrix, aVector.toArray(), y.toArray(), o);
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x + y
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(FullMatrix aMatrix, double [] aVector, double [] y) {
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
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, double [] y) {
    double[] tmp = dgemv(aMatrix, aVector.toArray(), y);
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
    double[] tmp = dgemv(aMatrix, aVector.toArray(), y.toArray());
    return tmp;
  }


  /**
   * DGEMV simplified: returns:=A^T*x + y
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(FullMatrix aMatrix, double [] aVector, double [] y) {
    dgemvInputSanityCheckerTranspose(aMatrix, aVector, y);
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
  public static double[] dgemvTransposed(FullMatrix aMatrix, DoubleMatrix1D aVector, double [] y) {
    double[] tmp = dgemvTransposed(aMatrix, aVector.toArray(), y);
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
    double[] tmp = dgemvTransposed(aMatrix, aVector.toArray(), y.toArray());
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
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double [] aVector, double [] y, BLAS2.orientation o)
  {
    double [] tmp = null;
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
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double [] y, BLAS2.orientation o)
  {
    double[] tmp = dgemv(alpha, aMatrix, aVector.toArray(), y, o);
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
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y, BLAS2.orientation o)
  {
    double[] tmp = dgemv(alpha, aMatrix, aVector.toArray(), y.toArray(), o);
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
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double [] aVector, double [] y) {
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
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double [] y) {
    double[] tmp = dgemv(alpha, aMatrix, aVector.toArray(), y);
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
    double[] tmp = dgemv(alpha, aMatrix, aVector.toArray(), y.toArray());
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
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, double [] aVector, double [] y) {
    dgemvInputSanityCheckerTranspose(aMatrix, aVector, y);
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
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double [] y) {
    double[] tmp = dgemvTransposed(alpha, aMatrix, aVector.toArray(), y);
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
    double[] tmp = dgemvTransposed(alpha, aMatrix, aVector.toArray(), y.toArray());
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
  public static double[] dgemv(FullMatrix aMatrix, double [] aVector, double beta, double [] y, BLAS2.orientation o)
  {
    double [] tmp = null;
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
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, double [] y, BLAS2.orientation o)
  {
    double[] tmp = dgemv(aMatrix, aVector.toArray(), beta, y, o);
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
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y, BLAS2.orientation o)
  {
    double[] tmp = dgemv(aMatrix, aVector.toArray(), beta, y.toArray(), o);
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
  public static double[] dgemv(FullMatrix aMatrix, double [] aVector, double beta, double [] y) {
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
  public static double[] dgemv(FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, double [] y) {
    double[] tmp = dgemv(aMatrix, aVector.toArray(), beta, y);
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
    double[] tmp = dgemv(aMatrix, aVector.toArray(), beta, y.toArray());
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
  public static double[] dgemvTransposed(FullMatrix aMatrix, double [] aVector, double beta, double [] y) {
    dgemvInputSanityCheckerTranspose(aMatrix, aVector, y);
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
  public static double[] dgemvTransposed(FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, double [] y) {
    double[] tmp = dgemvTransposed(aMatrix, aVector.toArray(), beta, y);
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
    double[] tmp = dgemvTransposed(aMatrix, aVector.toArray(), beta, y.toArray());
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
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double [] aVector, double beta, double [] y, BLAS2.orientation o)
  {
    double [] tmp = null;
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
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, double [] y, BLAS2.orientation o)
  {
    double [] tmp = dgemv(alpha, aMatrix, aVector.toArray(), beta, y, o);
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
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y, BLAS2.orientation o)
  {
    double [] tmp = dgemv(alpha, aMatrix, aVector.toArray(), beta, y.toArray(), o);
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
  public static double[] dgemv(double alpha, FullMatrix aMatrix, double [] aVector, double beta, double [] y) {
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
  public static double[] dgemv(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, double [] y) {
    double [] tmp = dgemv(alpha, aMatrix, aVector.toArray(), beta, y);
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
    double [] tmp = dgemv(alpha, aMatrix, aVector.toArray(), beta, y.toArray());
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
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, double [] aVector, double beta, double [] y) {
    dgemvInputSanityCheckerTranspose(aMatrix, aVector, y);
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
  public static double[] dgemvTransposed(double alpha, FullMatrix aMatrix, DoubleMatrix1D aVector, double beta, double [] y) {
    double[] tmp = dgemvTransposed(alpha, aMatrix, aVector.toArray(), beta, y);
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
    double[] tmp = dgemvTransposed(alpha, aMatrix, aVector.toArray(), beta, y.toArray());
    return tmp;
  }



/* Statefull manipulators on the FullMatrix type */
  /**
   * DGEMV simplified: y:=A*x
   * @param y a double[] vector that will be altered to contain A*x
   * @param aMatrix a FullMatrix
   * @param aVector a double[] vector
   */
  public static void dgemvInPlace(double[] y, FullMatrix aMatrix, double [] aVector) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    final int rows = aMatrix.getNumberOfRows();
    final int cols = aMatrix.getNumberOfColumns();
    assert (y.length == rows);
    double[] ptrA = aMatrix.getData();
    int idx, ptr, extra, ub;
    extra = cols - cols % 4;
    ub = ((cols / 4) * 4) - 1;
    for (int i = 0; i < rows; i++) {
      y[i] = 0;
      idx = i * cols;
      for (int j = 0; j < ub; j += 4) {
        ptr = idx + j;
        y[i] = ptrA[ptr] * aVector[j]
          + ptrA[ptr + 1] * aVector[j + 1]
          + ptrA[ptr + 2] * aVector[j + 2]
          + ptrA[ptr + 3] * aVector[j + 3]
          + y[i];
      }
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
    dgemvInPlace(y, aMatrix, aVector.toArray());
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
    dgemvInPlace(y.getData(), aMatrix, aVector.toArray());
  }

  /**
   * DGEMV FULL/simplified: y:=alpha*A*x + beta*y OR y:=alpha*A*x
   * NOTE: This method uses a short cut if beta is set to 0
   * NOTE: The reason for the method not mirroring the stateless method is that the signatures are
   * identical for a number of the methods due to writebacks to vector "y"
   * @param y a double[] vector that will be altered to contain alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param beta a double indicating the scaling of y
   * @param aVector a double[] vector
   */
  public static void dgemvInPlace(double[] y, double alpha, FullMatrix aMatrix,  double beta, double [] aVector) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    final int rows = aMatrix.getNumberOfRows();
    final int cols = aMatrix.getNumberOfColumns();
    double[] ptrA = aMatrix.getData();
    if (beta == 0) {
      alphaAx(y, alpha, ptrA, aVector, rows, cols);
    } else {
      alphaAxplusbetay(y, alpha, ptrA, beta, aVector, rows, cols);
    }
  }

  /**
   * DGEMV FULL/simplified: y:=alpha*A*x + beta*y OR y:=alpha*A*x
   * NOTE: This method uses a short cut if beta is set to 0
   * NOTE: The reason for the method not mirroring the stateless method is that the signatures are
   * identical for a number of the methods due to writebacks to vector "y"
   * @param y a double[] vector that will be altered to contain alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param beta a double indicating the scaling of y
   * @param aVector a double[] vector
   */
  public static void dgemvInPlace(double[] y, double alpha, FullMatrix aMatrix,  double beta, DoubleMatrix1D aVector) {
    dgemvInPlace(y, alpha, aMatrix, beta, aVector.toArray());
  }

  /**
   * DGEMV FULL/simplified: y:=alpha*A*x + beta*y OR y:=alpha*A*x
   * NOTE: This method uses a short cut if beta is set to 0
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
   * NOTE: This method uses a short cut if beta is set to 0
   * NOTE: The reason for the method not mirroring the stateless method is that the signatures are
   * identical for a number of the methods due to writebacks to vector "y"
   * @param y a DoubleMatrix1D vector that will be altered to contain alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a FullMatrix
   * @param beta a double indicating the scaling of y
   * @param aVector a DoubleMatrix1D vector
   */
  public static void dgemvInPlace(DoubleMatrix1D y, double alpha, FullMatrix aMatrix, double beta, DoubleMatrix1D aVector) {
    dgemvInPlace(y.getData(), alpha, aMatrix, beta, aVector.toArray());
  }


/* HELPER FUNCTIONS */
  /** 2 helper functions for STATEFULL FULL/simplified alpha*A*x ?+ beta*y */
  private static void alphaAx(double[] y, double alpha, double[] ptrA, double [] aVector, int rows, int cols) {
    double alphaTmp;
    int idx, ptr, extra, ub;
    extra = cols - cols % 4;
    ub = ((cols / 4) * 4) - 1;
    for (int i = 0; i < rows; i++) {
      y[i] = 0;
      idx = i * cols;
      alphaTmp = 0;
      for (int j = 0; j < ub; j += 4) {
        ptr = idx + j;
        alphaTmp += ptrA[ptr]     * aVector[j]
                 + ptrA[ptr + 1] * aVector[j + 1]
                 + ptrA[ptr + 2] * aVector[j + 2]
                 + ptrA[ptr + 3] * aVector[j + 3];
      }
      y[i] = alphaTmp * alpha;
      for (int j = extra; j < cols; j++) {
        y[i] += alpha * ptrA[idx + j] * aVector[j];
      }
    }
  }

  private static void alphaAxplusbetay(double[] y, double alpha, double[] ptrA, double beta, double [] aVector, int rows, int cols) {
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
        alphaTmp += ptrA[ptr]     * aVector[j]
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
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, double [] aVector, orientation o)
  {
    double [] tmp = null;
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
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, double [] aVector) {
    dgemvInputSanityChecker(aMatrix, aVector);
    final int [] rowPtr = aMatrix.getRowPtr();
    final int [] colIdx = aMatrix.getColumnIndex();
    final double [] values = aMatrix.getNonZeroElements();
    final int rows = aMatrix.getNumberOfRows();
    double [] tmp = new double[rows];
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
  public static double[] dgemvTransposed(CompressedSparseRowFormatMatrix aMatrix, double [] aVector) {
    dgemvInputSanityCheckerTranspose(aMatrix, aVector);
    final int [] rowPtr = aMatrix.getRowPtr();
    final double [] values = aMatrix.getNonZeroElements();
    final int [] colIdx = aMatrix.getColumnIndex();
    final int rows = aMatrix.getNumberOfRows();
    final int cols = aMatrix.getNumberOfColumns();
    double [] tmp = new double[cols];
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
  public static double[] dgemv(double alpha, CompressedSparseRowFormatMatrix aMatrix, double [] aVector, orientation o)
  {
    double [] tmp = null;
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
  public static double[] dgemv(double alpha, CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector, orientation o)
  {
    return dgemv(alpha, aMatrix, aVector.getData(), o);
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(double alpha, CompressedSparseRowFormatMatrix aMatrix, double [] aVector) {
    final int rows = aMatrix.getNumberOfRows();
    double [] tmp = dgemv(aMatrix, aVector);
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
    double [] tmp = dgemv(aMatrix, aVector.getData());
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
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, double [] aVector, double[] y, orientation o)
  {
    double [] tmp = null;
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
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector, double[] y, orientation o)
  {
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
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, double[] aVector, DoubleMatrix1D y, orientation o)
  {
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
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y, orientation o)
  {
    return dgemv(aMatrix, aVector.getData(), y.getData(), o);
  }


  /**
   * DGEMV simplified: returns:=A*x + y
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, double [] aVector, double [] y) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    final int rows = aMatrix.getNumberOfRows();
    double [] tmp = dgemv(aMatrix, aVector);
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
  public static double[] dgemv(CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector, double [] y) {
    return dgemv(aMatrix, aVector.getData(), y);
  }


  /**
   * DGEMV simplified: returns:=A^T*x + y
   * @param aMatrix a CompressedSparseRowFormatMatrix
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dgemvTransposed(CompressedSparseRowFormatMatrix aMatrix, double [] aVector, double [] y) {
    dgemvInputSanityCheckerTranspose(aMatrix, aVector, y);
    final int cols = aMatrix.getNumberOfColumns();
    double [] tmp = new double[cols];
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
  public static double[] dgemvTransposed(CompressedSparseRowFormatMatrix aMatrix, DoubleMatrix1D aVector, double [] y) {
    double[] tmp = dgemvTransposed(aMatrix, aVector.toArray(), y);
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
    double[] tmp = dgemvTransposed(aMatrix, aVector.toArray(), y.toArray());
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
  public static double[] dgemv(double alpha, CompressedSparseRowFormatMatrix aMatrix, double [] aVector, double [] y) {
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
  public static double[] dgemv(double alpha, CompressedSparseRowFormatMatrix  aMatrix, double [] aVector, double beta, double [] y) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    final int [] rowPtr = aMatrix.getRowPtr();
    final int [] colIdx = aMatrix.getColumnIndex();
    final double [] values = aMatrix.getNonZeroElements();
    final int rows = aMatrix.getNumberOfRows();
    double [] tmp = new double[rows];
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
  public static void dgemvInPlace(double[] y, CompressedSparseRowFormatMatrix aMatrix, double [] aVector) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    final int [] rowPtr = aMatrix.getRowPtr();
    final int [] colIdx = aMatrix.getColumnIndex();
    final double [] values = aMatrix.getNonZeroElements();
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
  public static void dgemvInPlace(double[] y, double alpha, CompressedSparseRowFormatMatrix aMatrix, double [] aVector, double beta) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    if (beta == 0) {
      csralphaAx(y, alpha, aMatrix, aVector);
    } else {
      csralphaAxplusbetay(y, alpha, aMatrix, beta, aVector);
    }
  }

  /** 2 helper functions for alpha*A*x ?+ beta*y */
  private static void csralphaAx(double[] y, double alpha, CompressedSparseRowFormatMatrix aMatrix, double [] aVector) {
    final int [] rowPtr = aMatrix.getRowPtr();
    final int [] colIdx = aMatrix.getColumnIndex();
    final double [] values = aMatrix.getNonZeroElements();
    final int rows = aMatrix.getNumberOfRows();
    int ptr = 0;
    double alphaTmp;
    for (int i = 0; i < rows; i++) {
      alphaTmp = 0;
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        alphaTmp  += values[ptr] * aVector[colIdx[ptr]];
        ptr++;
      }
      y[i] = alpha * alphaTmp;
    }
  }

  private static void csralphaAxplusbetay(double[] y, double alpha, CompressedSparseRowFormatMatrix aMatrix, double beta, double [] aVector) {
    final int [] rowPtr = aMatrix.getRowPtr();
    final int [] colIdx = aMatrix.getColumnIndex();
    final double [] values = aMatrix.getNonZeroElements();
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
