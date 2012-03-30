/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.blas;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.MatrixPrimitive;
import com.opengamma.maths.highlevelapi.datatypes.primitive.OGArrayType;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.CompressedSparseColumnFormatMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.CompressedSparseRowFormatMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.DenseMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.DenseSymmetricMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.PackedMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.SparseCoordinateFormatMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelabstractions.BLAS2DGEMVKernelAbstraction;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelimplementations.DGEMVForCOOMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelimplementations.DGEMVForCSCMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelimplementations.DGEMVForCSRMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelimplementations.DGEMVForDenseMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelimplementations.DGEMVForDenseSymmetricMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelimplementations.DGEMVForPackedMatrix;

/**
 * Provides the BLAS level 2 behaviour for the OG matrix library.
 * Massive amounts of overloading goes on, beware and only use if confident.
  * METHODS: DGEMV
 */
public class BLAS2 {
  /**
   * orientation: Enumeration based for the orientation of matrix A in the scheme
   * y := alpha*A*x + beta*y
   */
  public enum orientation {
    /** orientation is "normal" */
    normal,
    /** orientation is "transposed" */
    transposed
  }

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
   * DGEMV hashmapped function pointers
   */
  private static Map<Class<?>, BLAS2DGEMVKernelAbstraction<?>> s_dgemvFunctionPointers = new HashMap<Class<?>, BLAS2DGEMVKernelAbstraction<?>>();
  static {
    s_dgemvFunctionPointers.put(OGArrayType.class, DGEMVForDenseMatrix.getInstance()); // this is the wrapper for the high level API

    s_dgemvFunctionPointers.put(DenseMatrix.class, DGEMVForDenseMatrix.getInstance());
    s_dgemvFunctionPointers.put(CompressedSparseRowFormatMatrix.class, DGEMVForCSRMatrix.getInstance());
    s_dgemvFunctionPointers.put(CompressedSparseColumnFormatMatrix.class, DGEMVForCSCMatrix.getInstance());
    s_dgemvFunctionPointers.put(SparseCoordinateFormatMatrix.class, DGEMVForCOOMatrix.getInstance());
    s_dgemvFunctionPointers.put(PackedMatrix.class, DGEMVForPackedMatrix.getInstance());
    s_dgemvFunctionPointers.put(DenseSymmetricMatrix.class, DGEMVForDenseSymmetricMatrix.getInstance());
  }

  /**
   * gets the hashmap of class->kernels
   * @return Map class->kernels
   */
  public Map<Class<?>, BLAS2DGEMVKernelAbstraction<?>> getHashMap() {
    return s_dgemvFunctionPointers;
  }

  /**
  * Ensures that the inputs to DGEMV routines are sane when DGMEV is function(Matrix,Vector).
  * @param aMatrix is the matrix to be tested (A)
  * @param aVector is the vector to be tested (x)
  */
  public static void dgemvInputSanityChecker(MatrixPrimitive aMatrix, double[] aVector) {
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
  public static void dgemvInputSanityChecker(MatrixPrimitive aMatrix, double[] aVector, double[] addToVector) {
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
  public static void dgemvInputSanityCheckerTransposed(MatrixPrimitive aMatrix, double[] aVector) {
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
  public static void dgemvInputSanityCheckerTransposed(MatrixPrimitive aMatrix, double[] aVector, double[] addToVector) {
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
  public static void dgemvInputSanityChecker(double[] aYVector, MatrixPrimitive aMatrix, double[] aVector) {
    assertNotNull(aMatrix); // check not null
    assertNotNull(aVector); // check not null
    assertNotNull(aYVector); // check not null
    assertEquals(aMatrix.getNumberOfColumns(), aVector.length); // check commutable
    assertEquals(aMatrix.getNumberOfRows(), aYVector.length); // check commutable on back assignment
  }

  /* Stateless manipulators on the Matrix implementing the MatrixPrimitive interface type */

  /* GROUP1:: A*x OR A^T*x */
  /**
   * DGEMV simplified: returns:=A*x OR returns:=A^T*x depending on the enum orientation.
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, double[] aVector, BLAS2.orientation o) {
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    double[] tmp = null;
    switch (o) {
      case normal:
        dgemvInputSanityChecker(aMatrix, aVector);
        tmp = use.dm_stateless_A_times_x(aMatrix, aVector);
        break;
      case transposed:
        dgemvInputSanityCheckerTransposed(aMatrix, aVector);
        tmp = use.dm_stateless_AT_times_x(aMatrix, aVector);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x OR returns:=A^T*x depending on the enum orientation.
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, DoubleMatrix1D aVector, BLAS2.orientation o) {
    return dgemv(aMatrix, aVector.getData(), o);
  }

  /**
   * DGEMV simplified: returns:=A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, double[] aVector) {
    dgemvInputSanityChecker(aMatrix, aVector);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    return use.dm_stateless_A_times_x(aMatrix, aVector);
  }

  /**
   * DGEMV simplified: returns:=A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, DoubleMatrix1D aVector) {
    return dgemv(aMatrix, aVector.getData());
  }

  /**
   * DGEMV simplified: returns:=A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @return a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(T aMatrix, double[] aVector) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    return use.dm_stateless_AT_times_x(aMatrix, aVector);
  }

  /**
   * DGEMV simplified: returns:=A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(T aMatrix, DoubleMatrix1D aVector) {
    return dgemvTransposed(aMatrix, aVector.getData());
  }

  /* GROUP2:: alpha*A*x OR alpha*A^T*x */
  /**
   * DGEMV simplified: returns:=alpha*A*x OR returns:=alpha*A^T*x depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, double[] aVector, BLAS2.orientation o) {
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    double[] tmp = null;
    switch (o) {
      case normal:
        dgemvInputSanityChecker(aMatrix, aVector);
        tmp = use.dm_stateless_alpha_times_A_times_x(alpha, aMatrix, aVector);
        break;
      case transposed:
        dgemvInputSanityCheckerTransposed(aMatrix, aVector);
        tmp = use.dm_stateless_alpha_times_AT_times_x(alpha, aMatrix, aVector);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x OR returns:=alpha*A^T*x depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, DoubleMatrix1D aVector, BLAS2.orientation o) {
    return dgemv(alpha, aMatrix, aVector.getData(), o);
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, double[] aVector) {
    dgemvInputSanityChecker(aMatrix, aVector);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    return use.dm_stateless_alpha_times_A_times_x(alpha, aMatrix, aVector);
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, DoubleMatrix1D aVector) {
    return dgemv(alpha, aMatrix, aVector.getData());
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @return double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(double alpha, T aMatrix, double[] aVector) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    return use.dm_stateless_alpha_times_AT_times_x(alpha, aMatrix, aVector);
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(double alpha, T aMatrix, DoubleMatrix1D aVector) {
    return dgemvTransposed(alpha, aMatrix, aVector.getData());
  }

  /* GROUP3:: A*x + y OR A^T*x + y */
  /**
   * DGEMV simplified: returns:=A*x+y OR returns:=A^T*x+y depending on the enum orientation.
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, double[] aVector, double[] y, BLAS2.orientation o) {
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    double[] tmp = null;
    switch (o) {
      case normal:
        dgemvInputSanityChecker(aMatrix, aVector, y);
        tmp = use.dm_stateless_A_times_x_plus_y(aMatrix, aVector, y);
        break;
      case transposed:
        dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
        tmp = use.dm_stateless_AT_times_x_plus_y(aMatrix, aVector, y);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:=A*x+y OR returns:=A^T*x+y depending on the enum orientation.
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, DoubleMatrix1D aVector, double[] y, BLAS2.orientation o) {
    return dgemv(aMatrix, aVector.getData(), y, o);
  }

  /**
   * DGEMV simplified: returns:=A*x+y OR returns:=A^T*x+y depending on the enum orientation.
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, double[] aVector, DoubleMatrix1D y, BLAS2.orientation o) {
    return dgemv(aMatrix, aVector, y.getData(), o);
  }

  /**
   * DGEMV simplified: returns:=A*x+y OR returns:=A^T*x+y depending on the enum orientation.
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y, BLAS2.orientation o) {
    return dgemv(aMatrix, aVector.getData(), y.getData(), o);
  }

  /**
   * DGEMV simplified: returns:=A*x + y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, double[] aVector, double[] y) {
    dgemvInputSanityChecker(aMatrix, aVector, y);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    return use.dm_stateless_A_times_x_plus_y(aMatrix, aVector, y);
  }

  /**
   * DGEMV simplified: returns:=A*x + y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, DoubleMatrix1D aVector, double[] y) {
    return dgemv(aMatrix, aVector.getData(), y);
  }

  /**
   * DGEMV simplified: returns:=A*x + y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, double[] aVector, DoubleMatrix1D y) {
    return dgemv(aMatrix, aVector, y.getData());
  }

  /**
   * DGEMV simplified: returns:=A*x + y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y) {
    return dgemv(aMatrix, aVector.getData(), y.getData());
  }

  /**
   * DGEMV simplified: returns:=A^T*x + y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(T aMatrix, double[] aVector, double[] y) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    return use.dm_stateless_AT_times_x_plus_y(aMatrix, aVector, y);
  }

  /**
   * DGEMV simplified: returns:=A^T*x + y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(T aMatrix, DoubleMatrix1D aVector, double[] y) {
    return dgemvTransposed(aMatrix, aVector.getData(), y);
  }

  /**
   * DGEMV simplified: returns:=A^T*x + y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(T aMatrix, double[] aVector, DoubleMatrix1D y) {
    return dgemvTransposed(aMatrix, aVector, y.getData());
  }

  /**
   * DGEMV simplified: returns:=A^T*x + y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(T aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y) {
    return dgemvTransposed(aMatrix, aVector.getData(), y.getData());
  }

  /* GROUP4:: alpha*A*x + y OR alpha*A^T*x + y */
  /**
   * DGEMV simplified: returns:= alpha*A*x + y OR returns:=alpha*A^T*x + y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, double[] aVector, double[] y, BLAS2.orientation o) {
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    double[] tmp = null;
    switch (o) {
      case normal:
        dgemvInputSanityChecker(aMatrix, aVector, y);
        tmp = use.dm_stateless_alpha_times_A_times_x_plus_y(alpha, aMatrix, aVector, y);
        break;
      case transposed:
        dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
        tmp = use.dm_stateless_alpha_times_AT_times_x_plus_y(alpha, aMatrix, aVector, y);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:= alpha*A*x + y OR returns:=alpha*A^T*x + y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, DoubleMatrix1D aVector, double[] y, BLAS2.orientation o) {
    return dgemv(alpha, aMatrix, aVector.getData(), y, o);
  }

  /**
   * DGEMV simplified: returns:= alpha*A*x + y OR returns:=alpha*A^T*x + y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, double[] aVector, DoubleMatrix1D y, BLAS2.orientation o) {
    return dgemv(alpha, aMatrix, aVector, y.getData(), o);
  }

  /**
   * DGEMV simplified: returns:= alpha*A*x + y OR returns:=alpha*A^T*x + y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y, BLAS2.orientation o) {
    return dgemv(alpha, aMatrix, aVector.getData(), y.getData(), o);
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x + y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, double[] aVector, double[] y) {
    dgemvInputSanityChecker(aMatrix, aVector, y);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    return use.dm_stateless_alpha_times_A_times_x_plus_y(alpha, aMatrix, aVector, y);
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x + y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, DoubleMatrix1D aVector, double[] y) {
    return dgemv(alpha, aMatrix, aVector.getData(), y);
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x + y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, double[] aVector, DoubleMatrix1D y) {
    return dgemv(alpha, aMatrix, aVector, y.getData());
  }

  /**
   * DGEMV simplified: returns:=alpha*A*x + y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y) {
    return dgemv(alpha, aMatrix, aVector.getData(), y.getData());
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x + y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(double alpha, T aMatrix, double[] aVector, double[] y) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    return use.dm_stateless_alpha_times_AT_times_x_plus_y(alpha, aMatrix, aVector, y);
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x + y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(double alpha, T aMatrix, DoubleMatrix1D aVector, double[] y) {
    return dgemvTransposed(alpha, aMatrix, aVector.getData(), y);
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x + y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(double alpha, T aMatrix, double[] aVector, DoubleMatrix1D y) {
    return dgemvTransposed(alpha, aMatrix, aVector, y.getData());
  }

  /**
   * DGEMV simplified: returns:=alpha*A^T*x + y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(double alpha, T aMatrix, DoubleMatrix1D aVector, DoubleMatrix1D y) {
    return dgemvTransposed(alpha, aMatrix, aVector.getData(), y.getData());
  }

  /* GROUP5:: A*x + beta*y OR A^T*x + beta*y */
  /**
   * DGEMV simplified: returns:= A*x + beta*y OR returns:=A^T*x + beta*y  depending on the enum orientation.
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, double[] aVector, double beta, double[] y, BLAS2.orientation o) {
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    double[] tmp = null;
    switch (o) {
      case normal:
        dgemvInputSanityChecker(aMatrix, aVector, y);
        tmp = use.dm_stateless_A_times_x_plus_beta_times_y(aMatrix, aVector, beta, y);
        break;
      case transposed:
        dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
        tmp = use.dm_stateless_AT_times_x_plus_beta_times_y(aMatrix, aVector, beta, y);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV simplified: returns:= A*x + beta*y OR returns:=A^T*x + beta*y  depending on the enum orientation.
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}   *
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, DoubleMatrix1D aVector, double beta, double[] y, BLAS2.orientation o) {
    return dgemv(aMatrix, aVector.getData(), beta, y, o);
  }

  /**
   * DGEMV simplified: returns:= A*x + beta*y OR returns:=A^T*x + beta*y  depending on the enum orientation.
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, double[] aVector, double beta, DoubleMatrix1D y, BLAS2.orientation o) {
    return dgemv(aMatrix, aVector, beta, y.getData(), o);
  }

  /**
   * DGEMV simplified: returns:= A*x + beta*y OR returns:=A^T*x + beta*y  depending on the enum orientation.
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y, BLAS2.orientation o) {
    return dgemv(aMatrix, aVector.getData(), beta, y.getData(), o);
  }

  /**
   * DGEMV simplified: returns:=A*x + beta*y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, double[] aVector, double beta, double[] y) {
    dgemvInputSanityChecker(aMatrix, aVector, y);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    return use.dm_stateless_A_times_x_plus_beta_times_y(aMatrix, aVector, beta, y);
  }

  /**
   * DGEMV simplified: returns:=A*x + beta*y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, DoubleMatrix1D aVector, double beta, double[] y) {
    return dgemv(aMatrix, aVector.getData(), beta, y);
  }

  /**
   * DGEMV simplified: returns:=A*x + beta*y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, double[] aVector, double beta, DoubleMatrix1D y) {
    return dgemv(aMatrix, aVector, beta, y.getData());
  }

  /**
   * DGEMV simplified: returns:=A*x + beta*y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(T aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y) {
    return dgemv(aMatrix, aVector.getData(), beta, y.getData());
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(T aMatrix, double[] aVector, double beta, double[] y) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    return use.dm_stateless_AT_times_x_plus_beta_times_y(aMatrix, aVector, beta, y);
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(T aMatrix, DoubleMatrix1D aVector, double beta, double[] y) {
    return dgemvTransposed(aMatrix, aVector.getData(), beta, y);
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(T aMatrix, double[] aVector, double beta, DoubleMatrix1D y) {
    return dgemvTransposed(aMatrix, aVector, beta, y.getData());
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(T aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y) {
    return dgemvTransposed(aMatrix, aVector.getData(), beta, y.getData());
  }

  /* GROUP6:: alpha*A*x + beta*y OR alpha*A^T*x + beta*y */
  /**
   * DGEMV FULL: returns:= alpha*A*x + beta*y OR returns:=alpha*A^T*x + beta*y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, double[] aVector, double beta, double[] y, BLAS2.orientation o) {
    double[] tmp = null;
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    switch (o) {
      case normal:
        tmp = use.dm_stateless_alpha_times_A_times_x_plus_beta_times_y(alpha, aMatrix, aVector, beta, y);
        break;
      case transposed:
        tmp = use.dm_stateless_alpha_times_AT_times_x_plus_beta_times_y(alpha, aMatrix, aVector, beta, y);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
    return tmp;
  }

  /**
   * DGEMV FULL: returns:= alpha*A*x + beta*y OR returns:=alpha*A^T*x + beta*y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, DoubleMatrix1D aVector, double beta, double[] y, BLAS2.orientation o) {
    return dgemv(alpha, aMatrix, aVector.getData(), beta, y, o);
  }

  /**
   * DGEMV FULL: returns:= alpha*A*x + beta*y OR returns:=alpha*A^T*x + beta*y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, double[] aVector, double beta, DoubleMatrix1D y, BLAS2.orientation o) {
    return dgemv(alpha, aMatrix, aVector, beta, y.getData(), o);
  }

  /**
   * DGEMV FULL: returns:= alpha*A*x + beta*y OR returns:=alpha*A^T*x + beta*y  depending on the enum orientation.
   * @param alpha a double indicating the scaling of A*x OR A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y, BLAS2.orientation o) {
    return dgemv(alpha, aMatrix, aVector.getData(), beta, y.getData(), o);
  }

  /**
   * DGEMV FULL: returns:=alpha*A*x + beta*y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, double[] aVector, double beta, double[] y) {
    dgemvInputSanityChecker(aMatrix, aVector, y);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    return use.dm_stateless_alpha_times_A_times_x_plus_beta_times_y(alpha, aMatrix, aVector, beta, y);
  }

  /**
   * DGEMV FULL: returns:=alpha*A*x + beta*y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, DoubleMatrix1D aVector, double beta, double[] y) {
    return dgemv(alpha, aMatrix, aVector.getData(), beta, y);
  }

  /**
   * DGEMV FULL: returns:=alpha*A*x + beta*y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, double[] aVector, double beta, DoubleMatrix1D y) {
    return dgemv(alpha, aMatrix, aVector, beta, y.getData());
  }

  /**
   * DGEMV FULL: returns:=alpha*A*x + beta*y
   * @param alpha a double indicating the scaling of A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemv(double alpha, T aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y) {
    return dgemv(alpha, aMatrix, aVector.getData(), beta, y.getData());
  }

  /**
   * DGEMV FULL: returns:=alpha*A^T*x + beta*y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(double alpha, T aMatrix, double[] aVector, double beta, double[] y) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    return use.dm_stateless_alpha_times_AT_times_x_plus_beta_times_y(alpha, aMatrix, aVector, beta, y);
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(double alpha, T aMatrix, DoubleMatrix1D aVector, double beta, double[] y) {
    return dgemvTransposed(alpha, aMatrix, aVector.getData(), beta, y);
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(double alpha, T aMatrix, double[] aVector, double beta, DoubleMatrix1D y) {
    return dgemvTransposed(alpha, aMatrix, aVector, beta, y.getData());
  }

  /**
   * DGEMV simplified: returns:=A^T*x + beta*y
   * @param alpha a double indicating the scaling of A^T*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param beta a double indicating the scaling of y
   * @param y a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   * @return tmp a double[] vector
   */
  public static <T extends MatrixPrimitive> double[] dgemvTransposed(double alpha, T aMatrix, DoubleMatrix1D aVector, double beta, DoubleMatrix1D y) {
    return dgemvTransposed(alpha, aMatrix, aVector.getData(), beta, y.getData());
  }

  /* Statefull manipulators on the Matrix implementing the MatrixPrimitive interface type */
  //** group 1:: y=A*x OR y=A^T*x
  /**
   * DGEMV simplified: y:=A*x OR y:=A^T*x depending on the enum orientation.
   * @param y a double[] vector that will be altered to contain A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> void dgemvInPlace(double[] y, T aMatrix, double[] aVector, BLAS2.orientation o) {
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    switch (o) {
      case normal:
        dgemvInputSanityChecker(aMatrix, aVector, y);
        use.dm_inplace_A_times_x(y, aMatrix, aVector);
        break;
      case transposed:
        dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
        use.dm_inplace_AT_times_x(y, aMatrix, aVector);
        break;
      default:
        throw new IllegalArgumentException("BLAS2.orientation should be enumerated to either normal or transpose.");
    }
  }

  /**
   * DGEMV simplified: y:=A*x OR y:=A^T*x depending on the enum orientation.
   * @param y a DoubleMatrix1D vector that will be altered to contain A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlace(DoubleMatrix1D y, T aMatrix, double[] aVector, BLAS2.orientation o) {
    dgemvInPlace(y.getData(), aMatrix, aVector, o);
  }

  /**
   * DGEMV simplified: y:=A*x OR y:=A^T*x depending on the enum orientation.
   * @param y a double[] vector that will be altered to contain A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlace(double[] y, T aMatrix, DoubleMatrix1D aVector, BLAS2.orientation o) {
    dgemvInPlace(y, aMatrix, aVector.getData(), o);
  }

  /**
   * DGEMV simplified: y:=A*x OR y:=A^T*x depending on the enum orientation.
   * @param y a DoubleMatrix1D vector that will be altered to contain A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param o orientation "normal" performs A*x, "transpose" performs A^T*x
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlace(DoubleMatrix1D y, T aMatrix, DoubleMatrix1D aVector, BLAS2.orientation o) {
    dgemvInPlace(y.getData(), aMatrix, aVector.getData(), o);
  }

  /**
   * DGEMV simplified: y:=A*x
   * @param y a double[] vector that will be altered to contain A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> void dgemvInPlace(double[] y, T aMatrix, double[] aVector) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    use.dm_inplace_A_times_x(y, aMatrix, aVector);
  }

  /**
   * DGEMV simplified: y:=A*x
   * @param y a double vector that will be altered to contain A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlace(double[] y, T aMatrix, DoubleMatrix1D aVector) {
    dgemvInPlace(y, aMatrix, aVector.getData());
  }

  /**
   * DGEMV simplified: y:=A*x
   * @param y a DoubleMatrix1D that will be altered to contain A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlace(DoubleMatrix1D y, T aMatrix, double[] aVector) {
    dgemvInPlace(y.getData(), aMatrix, aVector);
  }

  /**
   * DGEMV simplified: y:=A*x
   * @param y a DoubleMatrix1D that will be altered to contain A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlace(DoubleMatrix1D y, T aMatrix, DoubleMatrix1D aVector) {
    dgemvInPlace(y.getData(), aMatrix, aVector.getData());
  }

  /**
   * DGEMV simplified: y:=A^T*x
   * @param y a double[] vector that will be altered to contain A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> void dgemvInPlaceTransposed(double[] y, T aMatrix, double[] aVector) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    use.dm_inplace_AT_times_x(y, aMatrix, aVector);
  }

  /**
   * DGEMV simplified: y:=A^T*x
   * @param y a double vector that will be altered to contain A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlaceTransposed(double[] y, T aMatrix, DoubleMatrix1D aVector) {
    dgemvInPlaceTransposed(y, aMatrix, aVector.getData());
  }

  /**
   * DGEMV simplified: y:=A^T*x
   * @param y a DoubleMatrix1D that will be altered to contain A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlaceTransposed(DoubleMatrix1D y, T aMatrix, double[] aVector) {
    dgemvInPlaceTransposed(y.getData(), aMatrix, aVector);
  }

  /**
   * DGEMV simplified: y:=A^T*x
   * @param y a DoubleMatrix1D that will be altered to contain A*x
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param aVector a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlaceTransposed(DoubleMatrix1D y, T aMatrix, DoubleMatrix1D aVector) {
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
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param beta a double indicating the scaling of y
   * @param aVector a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> void dgemvInPlace(double[] y, double alpha, T aMatrix, double beta, double[] aVector) {
    dgemvInputSanityChecker(y, aMatrix, aVector);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    if (Double.doubleToLongBits(alpha) == Double.doubleToLongBits(1)) {
      if (Double.doubleToLongBits(beta) == Double.doubleToLongBits(0)) {
        use.dm_inplace_A_times_x(y, aMatrix, aVector);
      } else if (Double.doubleToLongBits(beta) == Double.doubleToLongBits(1)) {
        use.dm_inplace_A_times_x_plus_y(y, aMatrix, aVector);
      } else {
        use.dm_inplace_A_times_x_plus_beta_times_y(y, aMatrix, aVector, beta);
      }
    } else {
      if (Double.doubleToLongBits(beta) == Double.doubleToLongBits(0)) {
        use.dm_inplace_alpha_times_A_times_x(y, alpha, aMatrix, aVector);
      } else if (Double.doubleToLongBits(beta) == Double.doubleToLongBits(1)) {
        use.dm_inplace_alpha_times_A_times_x_plus_y(y, alpha, aMatrix, aVector);
      } else {
        use.dm_inplace_alpha_times_A_times_x_plus_beta_times_y(y, alpha, aMatrix, aVector, beta);
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
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param beta a double indicating the scaling of y
   * @param aVector a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlace(double[] y, double alpha, T aMatrix, double beta, DoubleMatrix1D aVector) {
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
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param beta a double indicating the scaling of y
   * @param aVector a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlace(DoubleMatrix1D y, double alpha, T aMatrix, double beta, double[] aVector) {
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
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param beta a double indicating the scaling of y
   * @param aVector a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlace(DoubleMatrix1D y, double alpha, T aMatrix, double beta, DoubleMatrix1D aVector) {
    dgemvInPlace(y.getData(), alpha, aMatrix, beta, aVector.getData());
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
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param beta a double indicating the scaling of y
   * @param aVector a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  @SuppressWarnings("unchecked")
  public static <T extends MatrixPrimitive> void dgemvInPlaceTransposed(double[] y, double alpha, T aMatrix, double beta, double[] aVector) {
    dgemvInputSanityCheckerTransposed(aMatrix, aVector, y);
    BLAS2DGEMVKernelAbstraction<T> use = (BLAS2DGEMVKernelAbstraction<T>) s_dgemvFunctionPointers.get(aMatrix.getClass());
    Validate.notNull(use, "BLAS2 DGEMV was called with an unknown Matrix type: " + aMatrix.getClass() + ". If this type is needed the implement a BLAS2DGEMVKernelAbstraction.");
    if (Double.doubleToLongBits(alpha) == Double.doubleToLongBits(1)) {
      if (Double.doubleToLongBits(beta) == Double.doubleToLongBits(0)) {
        use.dm_inplace_AT_times_x(y, aMatrix, aVector);
      } else if (Double.doubleToLongBits(beta) == Double.doubleToLongBits(1)) {
        use.dm_inplace_AT_times_x_plus_y(y, aMatrix, aVector);
      } else {
        use.dm_inplace_AT_times_x_plus_beta_times_y(y, aMatrix, aVector, beta);
      }
    } else {
      if (Double.doubleToLongBits(beta) == Double.doubleToLongBits(0)) {
        use.dm_inplace_alpha_times_AT_times_x(y, alpha, aMatrix, aVector);
      } else if (Double.doubleToLongBits(beta) == Double.doubleToLongBits(1)) {
        use.dm_inplace_alpha_times_AT_times_x_plus_y(y, alpha, aMatrix, aVector);
      } else {
        use.dm_inplace_alpha_times_AT_times_x_plus_beta_times_y(y, alpha, aMatrix, aVector, beta);
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
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param beta a double indicating the scaling of y
   * @param aVector a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlaceTransposed(double[] y, double alpha, T aMatrix, double beta, DoubleMatrix1D aVector) {
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
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param beta a double indicating the scaling of y
   * @param aVector a double[] vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlaceTransposed(DoubleMatrix1D y, double alpha, T aMatrix, double beta, double[] aVector) {
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
   * @param aMatrix a Matrix implementing the MatrixPrimitive interface
   * @param beta a double indicating the scaling of y
   * @param aVector a DoubleMatrix1D vector
   * @param <T> a matrix that implements {@link MatrixPrimitive}
   */
  public static <T extends MatrixPrimitive> void dgemvInPlaceTransposed(DoubleMatrix1D y, double alpha, T aMatrix, double beta, DoubleMatrix1D aVector) {
    dgemvInPlaceTransposed(y.getData(), alpha, aMatrix, beta, aVector.getData());
  }

} // class end
