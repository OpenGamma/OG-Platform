/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelimplementations;

import java.util.Arrays;

import com.opengamma.maths.lowlevelapi.datatypes.primitive.DenseMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelabstractions.BLAS2DGEMVKernelAbstraction;

/**
 * Does DGEMV like operations on the {@link DenseMatrix} type
 */
public final class DGEMVForDenseMatrix extends BLAS2DGEMVKernelAbstraction<DenseMatrix> {
  private static DGEMVForDenseMatrix s_instance = new DGEMVForDenseMatrix();

  public static DGEMVForDenseMatrix getInstance() {
    return s_instance;
  }

  private DGEMVForDenseMatrix() {
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public double[] dm_stateless_A_times_x(DenseMatrix aMatrix, double[] aVector) { //CSIGNORE
    final int rows = aMatrix.getNumberOfRows();
    final int cols = aMatrix.getNumberOfColumns();
    final double[] ptrA = aMatrix.getData();
    double[] tmp = new double[rows];
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
   *{@inheritDoc}
   */
  @Override
  public double[] dm_stateless_AT_times_x(DenseMatrix aMatrix, double[] aVector) { //CSIGNORE
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

  /* TODO: Replace vector scalings with BLAS1 calls.*/
  /**
   *{@inheritDoc}
   */
  @Override
  public double[] dm_stateless_alpha_times_A_times_x(double alpha, DenseMatrix aMatrix, double[] aVector) { //CSIGNORE
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(aMatrix, aVector);
    for (int i = 0; i < rows; i++) {
      tmp[i] *= alpha; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public double[] dm_stateless_alpha_times_AT_times_x(double alpha, DenseMatrix aMatrix, double[] aVector) { //CSIGNORE
    final int cols = aMatrix.getNumberOfColumns();
    double[] tmp = dm_stateless_AT_times_x(aMatrix, aVector);
    for (int i = 0; i < cols; i++) {
      tmp[i] *= alpha; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public double[] dm_stateless_A_times_x_plus_y(DenseMatrix aMatrix, double[] aVector, double[] y) { //CSIGNORE
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(aMatrix, aVector);
    for (int i = 0; i < rows; i++) {
      tmp[i] += y[i]; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public double[] dm_stateless_AT_times_x_plus_y(DenseMatrix aMatrix, double[] aVector, double[] y) { //CSIGNORE
    final int cols = aMatrix.getNumberOfColumns();
    double[] tmp = dm_stateless_AT_times_x(aMatrix, aVector);
    for (int i = 0; i < cols; i++) {
      tmp[i] += y[i]; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public double[] dm_stateless_alpha_times_A_times_x_plus_y(double alpha, DenseMatrix A, double[] x, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] = alpha * tmp[i] + y[i]; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public double[] dm_stateless_alpha_times_AT_times_x_plus_y(double alpha, DenseMatrix A, double[] x, double[] y) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    double[] tmp = dm_stateless_AT_times_x(A, x);
    for (int i = 0; i < cols; i++) {
      tmp[i] = alpha * tmp[i] + y[i]; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public double[] dm_stateless_A_times_x_plus_beta_times_y(DenseMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] += beta * y[i]; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public double[] dm_stateless_AT_times_x_plus_beta_times_y(DenseMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    double[] tmp = dm_stateless_AT_times_x(A, x);
    for (int i = 0; i < cols; i++) {
      tmp[i] += beta * y[i]; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public double[] dm_stateless_alpha_times_A_times_x_plus_beta_times_y(double alpha, DenseMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] = alpha * tmp[i] + beta * y[i]; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public double[] dm_stateless_alpha_times_AT_times_x_plus_beta_times_y(double alpha, DenseMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    double[] tmp = dm_stateless_AT_times_x(A, x);
    for (int i = 0; i < cols; i++) {
      tmp[i] = alpha * tmp[i] + beta * y[i];
    }
    return tmp;
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public void dm_inplace_A_times_x(double[] y, DenseMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
    assert (y.length == rows);
    double[] ptrA = A.getData();
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
        y[i] += ptrA[ptr] * x[j]
            + ptrA[ptr + 1] * x[j + 1]
            + ptrA[ptr + 2] * x[j + 2]
            + ptrA[ptr + 3] * x[j + 3];
        acc += ptrA[ptr + 4] * x[j + 4]
            + ptrA[ptr + 5] * x[j + 5]
            + ptrA[ptr + 6] * x[j + 6]
            + ptrA[ptr + 7] * x[j + 7];
      }
      y[i] += acc;
      for (int j = extra; j < cols; j++) {
        y[i] += ptrA[idx + j] * x[j];
      }
    }
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public void dm_inplace_AT_times_x(double[] y, DenseMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
    double[] ptrA = A.getData();
    int idx0, idx1, idx2, idx3, idx4, idx5, idx6, idx7;
    final int extra = cols - cols % 8;
    final int ub = ((cols / 8) * 8) - 1;
    final int extraRows = rows - rows % 8;
    final int ubRows = ((rows / 8) * 8) - 1;
    int ip1, ip2, ip3, ip4, ip5, ip6, ip7;
    Arrays.fill(y, 0);
    for (int j = 0; j < ubRows; j += 8) {
      idx0 = (j + 0) * cols;
      idx1 = (j + 1) * cols;
      idx2 = (j + 2) * cols;
      idx3 = (j + 3) * cols;
      idx4 = (j + 4) * cols;
      idx5 = (j + 5) * cols;
      idx6 = (j + 6) * cols;
      idx7 = (j + 7) * cols;
      final double rhs0 = x[j];
      final double rhs1 = x[j + 1];
      final double rhs2 = x[j + 2];
      final double rhs3 = x[j + 3];
      final double rhs4 = x[j + 4];
      final double rhs5 = x[j + 5];
      final double rhs6 = x[j + 6];
      final double rhs7 = x[j + 7];

      for (int i = 0; i < ub; i += 8) {
        ip1 = i + 1;
        ip2 = i + 2;
        ip3 = i + 3;
        ip4 = i + 4;
        ip5 = i + 5;
        ip6 = i + 6;
        ip7 = i + 7;
        y[i] += ptrA[idx0 + i] * rhs0 + ptrA[idx1 + i] * rhs1 + ptrA[idx2 + i] * rhs2 + ptrA[idx3 + i] * rhs3 + ptrA[idx4 + i] * rhs4 + ptrA[idx5 + i] * rhs5 + ptrA[idx6 + i] * rhs6 +
            ptrA[idx7 + i] * rhs7;
        y[i + 1] += ptrA[idx0 + ip1] * rhs0 + ptrA[idx1 + ip1] * rhs1 + ptrA[idx2 + ip1] * rhs2 + ptrA[idx3 + ip1] * rhs3 + ptrA[idx4 + ip1] * rhs4 + ptrA[idx5 + ip1] * rhs5 + ptrA[idx6 + ip1] *
            rhs6 + ptrA[idx7 + ip1] * rhs7;
        y[i + 2] += ptrA[idx0 + ip2] * rhs0 + ptrA[idx1 + ip2] * rhs1 + ptrA[idx2 + ip2] * rhs2 + ptrA[idx3 + ip2] * rhs3 + ptrA[idx4 + ip2] * rhs4 + ptrA[idx5 + ip2] * rhs5 + ptrA[idx6 + ip2] *
            rhs6 + ptrA[idx7 + ip2] * rhs7;
        y[i + 3] += ptrA[idx0 + ip3] * rhs0 + ptrA[idx1 + ip3] * rhs1 + ptrA[idx2 + ip3] * rhs2 + ptrA[idx3 + ip3] * rhs3 + ptrA[idx4 + ip3] * rhs4 + ptrA[idx5 + ip3] * rhs5 + ptrA[idx6 + ip3] *
            rhs6 + ptrA[idx7 + ip3] * rhs7;
        y[i + 4] += ptrA[idx0 + ip4] * rhs0 + ptrA[idx1 + ip4] * rhs1 + ptrA[idx2 + ip4] * rhs2 + ptrA[idx3 + ip4] * rhs3 + ptrA[idx4 + ip4] * rhs4 + ptrA[idx5 + ip4] * rhs5 + ptrA[idx6 + ip4] *
            rhs6 + ptrA[idx7 + ip4] * rhs7;
        y[i + 5] += ptrA[idx0 + ip5] * rhs0 + ptrA[idx1 + ip5] * rhs1 + ptrA[idx2 + ip5] * rhs2 + ptrA[idx3 + ip5] * rhs3 + ptrA[idx4 + ip5] * rhs4 + ptrA[idx5 + ip5] * rhs5 + ptrA[idx6 + ip5] *
            rhs6 + ptrA[idx7 + ip5] * rhs7;
        y[i + 6] += ptrA[idx0 + ip6] * rhs0 + ptrA[idx1 + ip6] * rhs1 + ptrA[idx2 + ip6] * rhs2 + ptrA[idx3 + ip6] * rhs3 + ptrA[idx4 + ip6] * rhs4 + ptrA[idx5 + ip6] * rhs5 + ptrA[idx6 + ip6] *
            rhs6 + ptrA[idx7 + ip6] * rhs7;
        y[i + 7] += ptrA[idx0 + ip7] * rhs0 + ptrA[idx1 + ip7] * rhs1 + ptrA[idx2 + ip7] * rhs2 + ptrA[idx3 + ip7] * rhs3 + ptrA[idx4 + ip7] * rhs4 + ptrA[idx5 + ip7] * rhs5 + ptrA[idx6 + ip7] *
            rhs6 + ptrA[idx7 + ip7] * rhs7;
      }
      for (int i = extra; i < cols; i++) {
        y[i] += ptrA[idx0 + i] * rhs0 + ptrA[idx1 + i] * rhs1 + ptrA[idx2 + i] * rhs2 + ptrA[idx3 + i] * rhs3 + ptrA[idx4 + i] * rhs4 + ptrA[idx5 + i] * rhs5 + ptrA[idx6 + i] * rhs6 +
            ptrA[idx7 + i] * rhs7;
      }
    }
    for (int j = extraRows; j < rows; j++) {
      idx0 = j * cols;
      final double rhs = x[j];
      for (int i = 0; i < ub; i += 8) {
        ip1 = i + 1;
        ip2 = i + 2;
        ip3 = i + 3;
        ip4 = i + 4;
        ip5 = i + 5;
        ip6 = i + 6;
        ip7 = i + 7;
        y[i] += ptrA[idx0 + i] * rhs;
        y[i + 1] += ptrA[idx0 + ip1] * rhs;
        y[i + 2] += ptrA[idx0 + ip2] * rhs;
        y[i + 3] += ptrA[idx0 + ip3] * rhs;
        y[i + 4] += ptrA[idx0 + ip4] * rhs;
        y[i + 5] += ptrA[idx0 + ip5] * rhs;
        y[i + 6] += ptrA[idx0 + ip6] * rhs;
        y[i + 7] += ptrA[idx0 + ip7] * rhs;
      }
      for (int i = extra; i < cols; i++) {
        y[i] += ptrA[idx0 + i] * rhs;
      }
    }

  }

  /**
   *{@inheritDoc}
   */
  @Override
  public void dm_inplace_alpha_times_A_times_x(double[] y, double alpha, DenseMatrix A, double[] x) { //CSIGNORE
    final double[] ptrA = A.getData();
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
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
        alphaTmp += ptrA[ptr] * x[j]
                 + ptrA[ptr + 1] * x[j + 1]
                 + ptrA[ptr + 2] * x[j + 2]
                 + ptrA[ptr + 3] * x[j + 3];
        acc += ptrA[ptr + 4] * x[j + 4]
               + ptrA[ptr + 5] * x[j + 5]
               + ptrA[ptr + 6] * x[j + 6]
               + ptrA[ptr + 7] * x[j + 7];
      }
      y[i] = (alphaTmp + acc) * alpha;
      for (int j = extra; j < cols; j++) {
        y[i] += alpha * ptrA[idx + j] * x[j];
      }
    }
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public void dm_inplace_alpha_times_AT_times_x(double[] y, double alpha, DenseMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
    double[] ptrA = A.getData();
    int idx0, idx1, idx2, idx3, idx4, idx5, idx6, idx7;
    final int extra = cols - cols % 8;
    final int ub = ((cols / 8) * 8) - 1;
    final int extraRows = rows - rows % 8;
    final int ubRows = ((rows / 8) * 8) - 1;
    int ip1, ip2, ip3, ip4, ip5, ip6, ip7;
    Arrays.fill(y, 0);
    for (int j = 0; j < ubRows; j += 8) {
      idx0 = (j + 0) * cols;
      idx1 = (j + 1) * cols;
      idx2 = (j + 2) * cols;
      idx3 = (j + 3) * cols;
      idx4 = (j + 4) * cols;
      idx5 = (j + 5) * cols;
      idx6 = (j + 6) * cols;
      idx7 = (j + 7) * cols;
      final double rhs0 = x[j];
      final double rhs1 = x[j + 1];
      final double rhs2 = x[j + 2];
      final double rhs3 = x[j + 3];
      final double rhs4 = x[j + 4];
      final double rhs5 = x[j + 5];
      final double rhs6 = x[j + 6];
      final double rhs7 = x[j + 7];

      for (int i = 0; i < ub; i += 8) {
        ip1 = i + 1;
        ip2 = i + 2;
        ip3 = i + 3;
        ip4 = i + 4;
        ip5 = i + 5;
        ip6 = i + 6;
        ip7 = i + 7;
        y[i] += ptrA[idx0 + i] * rhs0 + ptrA[idx1 + i] * rhs1 + ptrA[idx2 + i] * rhs2 + ptrA[idx3 + i] * rhs3 + ptrA[idx4 + i] * rhs4 + ptrA[idx5 + i] * rhs5 + ptrA[idx6 + i] * rhs6 +
            ptrA[idx7 + i] * rhs7;
        y[i + 1] += ptrA[idx0 + ip1] * rhs0 + ptrA[idx1 + ip1] * rhs1 + ptrA[idx2 + ip1] * rhs2 + ptrA[idx3 + ip1] * rhs3 + ptrA[idx4 + ip1] * rhs4 + ptrA[idx5 + ip1] * rhs5 + ptrA[idx6 + ip1] *
            rhs6 + ptrA[idx7 + ip1] * rhs7;
        y[i + 2] += ptrA[idx0 + ip2] * rhs0 + ptrA[idx1 + ip2] * rhs1 + ptrA[idx2 + ip2] * rhs2 + ptrA[idx3 + ip2] * rhs3 + ptrA[idx4 + ip2] * rhs4 + ptrA[idx5 + ip2] * rhs5 + ptrA[idx6 + ip2] *
            rhs6 + ptrA[idx7 + ip2] * rhs7;
        y[i + 3] += ptrA[idx0 + ip3] * rhs0 + ptrA[idx1 + ip3] * rhs1 + ptrA[idx2 + ip3] * rhs2 + ptrA[idx3 + ip3] * rhs3 + ptrA[idx4 + ip3] * rhs4 + ptrA[idx5 + ip3] * rhs5 + ptrA[idx6 + ip3] *
            rhs6 + ptrA[idx7 + ip3] * rhs7;
        y[i + 4] += ptrA[idx0 + ip4] * rhs0 + ptrA[idx1 + ip4] * rhs1 + ptrA[idx2 + ip4] * rhs2 + ptrA[idx3 + ip4] * rhs3 + ptrA[idx4 + ip4] * rhs4 + ptrA[idx5 + ip4] * rhs5 + ptrA[idx6 + ip4] *
            rhs6 + ptrA[idx7 + ip4] * rhs7;
        y[i + 5] += ptrA[idx0 + ip5] * rhs0 + ptrA[idx1 + ip5] * rhs1 + ptrA[idx2 + ip5] * rhs2 + ptrA[idx3 + ip5] * rhs3 + ptrA[idx4 + ip5] * rhs4 + ptrA[idx5 + ip5] * rhs5 + ptrA[idx6 + ip5] *
            rhs6 + ptrA[idx7 + ip5] * rhs7;
        y[i + 6] += ptrA[idx0 + ip6] * rhs0 + ptrA[idx1 + ip6] * rhs1 + ptrA[idx2 + ip6] * rhs2 + ptrA[idx3 + ip6] * rhs3 + ptrA[idx4 + ip6] * rhs4 + ptrA[idx5 + ip6] * rhs5 + ptrA[idx6 + ip6] *
            rhs6 + ptrA[idx7 + ip6] * rhs7;
        y[i + 7] += ptrA[idx0 + ip7] * rhs0 + ptrA[idx1 + ip7] * rhs1 + ptrA[idx2 + ip7] * rhs2 + ptrA[idx3 + ip7] * rhs3 + ptrA[idx4 + ip7] * rhs4 + ptrA[idx5 + ip7] * rhs5 + ptrA[idx6 + ip7] *
            rhs6 + ptrA[idx7 + ip7] * rhs7;
      }
      for (int i = extra; i < cols; i++) {
        y[i] += ptrA[idx0 + i] * rhs0 + ptrA[idx1 + i] * rhs1 + ptrA[idx2 + i] * rhs2 + ptrA[idx3 + i] * rhs3 + ptrA[idx4 + i] * rhs4 + ptrA[idx5 + i] * rhs5 + ptrA[idx6 + i] * rhs6 +
            ptrA[idx7 + i] * rhs7;
      }
    }
    for (int j = extraRows; j < rows; j++) {
      idx0 = j * cols;
      final double rhs = x[j];
      for (int i = 0; i < ub; i += 8) {
        ip1 = i + 1;
        ip2 = i + 2;
        ip3 = i + 3;
        ip4 = i + 4;
        ip5 = i + 5;
        ip6 = i + 6;
        ip7 = i + 7;
        y[i] += ptrA[idx0 + i] * rhs;
        y[i + 1] += ptrA[idx0 + ip1] * rhs;
        y[i + 2] += ptrA[idx0 + ip2] * rhs;
        y[i + 3] += ptrA[idx0 + ip3] * rhs;
        y[i + 4] += ptrA[idx0 + ip4] * rhs;
        y[i + 5] += ptrA[idx0 + ip5] * rhs;
        y[i + 6] += ptrA[idx0 + ip6] * rhs;
        y[i + 7] += ptrA[idx0 + ip7] * rhs;
      }
      for (int i = extra; i < cols; i++) {
        y[i] += ptrA[idx0 + i] * rhs;
      }
    }
    for (int i = 0; i < cols; i++) {
      y[i] *= alpha;
    }
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public void dm_inplace_A_times_x_plus_y(double[] y, DenseMatrix A, double[] x) { //CSIGNORE
    final double[] ptrA = A.getData();
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
    double alphaTmp, acc;
    int idx, ptr, extra, ub;
    extra = cols - cols % 8;
    ub = ((cols / 8) * 8) - 1;
    for (int i = 0; i < rows; i++) {
      idx = i * cols;
      alphaTmp = 0;
      acc = 0;
      for (int j = 0; j < ub; j += 8) {
        ptr = idx + j;
        alphaTmp += ptrA[ptr] * x[j]
                 + ptrA[ptr + 1] * x[j + 1]
                 + ptrA[ptr + 2] * x[j + 2]
                 + ptrA[ptr + 3] * x[j + 3];
        acc += ptrA[ptr + 4] * x[j + 4]
            + ptrA[ptr + 5] * x[j + 5]
            + ptrA[ptr + 6] * x[j + 6]
            + ptrA[ptr + 7] * x[j + 7];
      }
      alphaTmp += acc;
      for (int j = extra; j < cols; j++) {
        alphaTmp += ptrA[idx + j] * x[j];
      }
      y[i] = alphaTmp + y[i];
    }
  }

  /**
  *{@inheritDoc}
  */
  @Override
  public void dm_inplace_AT_times_x_plus_y(double[] y, DenseMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
    double[] ptrA = A.getData();
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
      final double rhs0 = x[j];
      final double rhs1 = x[j + 1];
      final double rhs2 = x[j + 2];
      final double rhs3 = x[j + 3];
      final double rhs4 = x[j + 4];
      final double rhs5 = x[j + 5];
      final double rhs6 = x[j + 6];
      final double rhs7 = x[j + 7];

      for (int i = 0; i < ub; i += 8) {
        ip1 = i + 1;
        ip2 = i + 2;
        ip3 = i + 3;
        ip4 = i + 4;
        ip5 = i + 5;
        ip6 = i + 6;
        ip7 = i + 7;
        y[i] += ptrA[idx0 + i] * rhs0 + ptrA[idx1 + i] * rhs1 + ptrA[idx2 + i] * rhs2 + ptrA[idx3 + i] * rhs3 + ptrA[idx4 + i] * rhs4 + ptrA[idx5 + i] * rhs5 + ptrA[idx6 + i] * rhs6 +
            ptrA[idx7 + i] * rhs7;
        y[i + 1] += ptrA[idx0 + ip1] * rhs0 + ptrA[idx1 + ip1] * rhs1 + ptrA[idx2 + ip1] * rhs2 + ptrA[idx3 + ip1] * rhs3 + ptrA[idx4 + ip1] * rhs4 + ptrA[idx5 + ip1] * rhs5 + ptrA[idx6 + ip1] *
            rhs6 + ptrA[idx7 + ip1] * rhs7;
        y[i + 2] += ptrA[idx0 + ip2] * rhs0 + ptrA[idx1 + ip2] * rhs1 + ptrA[idx2 + ip2] * rhs2 + ptrA[idx3 + ip2] * rhs3 + ptrA[idx4 + ip2] * rhs4 + ptrA[idx5 + ip2] * rhs5 + ptrA[idx6 + ip2] *
            rhs6 + ptrA[idx7 + ip2] * rhs7;
        y[i + 3] += ptrA[idx0 + ip3] * rhs0 + ptrA[idx1 + ip3] * rhs1 + ptrA[idx2 + ip3] * rhs2 + ptrA[idx3 + ip3] * rhs3 + ptrA[idx4 + ip3] * rhs4 + ptrA[idx5 + ip3] * rhs5 + ptrA[idx6 + ip3] *
            rhs6 + ptrA[idx7 + ip3] * rhs7;
        y[i + 4] += ptrA[idx0 + ip4] * rhs0 + ptrA[idx1 + ip4] * rhs1 + ptrA[idx2 + ip4] * rhs2 + ptrA[idx3 + ip4] * rhs3 + ptrA[idx4 + ip4] * rhs4 + ptrA[idx5 + ip4] * rhs5 + ptrA[idx6 + ip4] *
            rhs6 + ptrA[idx7 + ip4] * rhs7;
        y[i + 5] += ptrA[idx0 + ip5] * rhs0 + ptrA[idx1 + ip5] * rhs1 + ptrA[idx2 + ip5] * rhs2 + ptrA[idx3 + ip5] * rhs3 + ptrA[idx4 + ip5] * rhs4 + ptrA[idx5 + ip5] * rhs5 + ptrA[idx6 + ip5] *
            rhs6 + ptrA[idx7 + ip5] * rhs7;
        y[i + 6] += ptrA[idx0 + ip6] * rhs0 + ptrA[idx1 + ip6] * rhs1 + ptrA[idx2 + ip6] * rhs2 + ptrA[idx3 + ip6] * rhs3 + ptrA[idx4 + ip6] * rhs4 + ptrA[idx5 + ip6] * rhs5 + ptrA[idx6 + ip6] *
            rhs6 + ptrA[idx7 + ip6] * rhs7;
        y[i + 7] += ptrA[idx0 + ip7] * rhs0 + ptrA[idx1 + ip7] * rhs1 + ptrA[idx2 + ip7] * rhs2 + ptrA[idx3 + ip7] * rhs3 + ptrA[idx4 + ip7] * rhs4 + ptrA[idx5 + ip7] * rhs5 + ptrA[idx6 + ip7] *
            rhs6 + ptrA[idx7 + ip7] * rhs7;
      }
      for (int i = extra; i < cols; i++) {
        y[i] += ptrA[idx0 + i] * rhs0 + ptrA[idx1 + i] * rhs1 + ptrA[idx2 + i] * rhs2 + ptrA[idx3 + i] * rhs3 + ptrA[idx4 + i] * rhs4 + ptrA[idx5 + i] * rhs5 + ptrA[idx6 + i] * rhs6 +
            ptrA[idx7 + i] * rhs7;
      }
    }
    for (int j = extraRows; j < rows; j++) {
      idx0 = j * cols;
      final double rhs = x[j];
      for (int i = 0; i < ub; i += 8) {
        ip1 = i + 1;
        ip2 = i + 2;
        ip3 = i + 3;
        ip4 = i + 4;
        ip5 = i + 5;
        ip6 = i + 6;
        ip7 = i + 7;
        y[i] += ptrA[idx0 + i] * rhs;
        y[i + 1] += ptrA[idx0 + ip1] * rhs;
        y[i + 2] += ptrA[idx0 + ip2] * rhs;
        y[i + 3] += ptrA[idx0 + ip3] * rhs;
        y[i + 4] += ptrA[idx0 + ip4] * rhs;
        y[i + 5] += ptrA[idx0 + ip5] * rhs;
        y[i + 6] += ptrA[idx0 + ip6] * rhs;
        y[i + 7] += ptrA[idx0 + ip7] * rhs;
      }
      for (int i = extra; i < cols; i++) {
        y[i] += ptrA[idx0 + i] * rhs;
      }
    }
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public void dm_inplace_alpha_times_A_times_x_plus_y(double[] y, double alpha, DenseMatrix A, double[] x) { //CSIGNORE
    final double[] ptrA = A.getData();
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
    double alphaTmp;
    int idx, ptr, extra, ub;
    extra = cols - cols % 4;
    ub = ((cols / 4) * 4) - 1;
    for (int i = 0; i < rows; i++) {
      idx = i * cols;
      alphaTmp = 0.;
      for (int j = 0; j < ub; j += 4) {
        ptr = idx + j;
        alphaTmp += ptrA[ptr] * x[j]
                 + ptrA[ptr + 1] * x[j + 1]
                 + ptrA[ptr + 2] * x[j + 2]
                 + ptrA[ptr + 3] * x[j + 3];
      }
      y[i] += alpha * alphaTmp;
      for (int j = extra; j < cols; j++) {
        y[i] += alpha * ptrA[idx + j] * x[j];
      }
    }
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public void dm_inplace_alpha_times_AT_times_x_plus_y(double[] y, double alpha, DenseMatrix A, double[] x) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    final int rows = A.getNumberOfRows();
    double[] ptrA = A.getData();
    double alphaTmp;
    for (int i = 0; i < cols; i++) {
      alphaTmp = 0;
      for (int j = 0; j < rows; j++) {
        alphaTmp += ptrA[i + j * cols] * x[j];
      }
      y[i] += alpha * alphaTmp;
    }
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public void dm_inplace_A_times_x_plus_beta_times_y(double[] y, DenseMatrix A, double[] x, double beta) { //CSIGNORE
    final double[] ptrA = A.getData();
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
    double alphaTmp;
    int idx, ptr, extra, ub;
    extra = cols - cols % 4;
    ub = ((cols / 4) * 4) - 1;
    for (int i = 0; i < rows; i++) {
      idx = i * cols;
      alphaTmp = 0;
      y[i] = beta * y[i];
      for (int j = 0; j < ub; j += 4) {
        ptr = idx + j;
        alphaTmp += ptrA[ptr] * x[j]
                 + ptrA[ptr + 1] * x[j + 1]
                 + ptrA[ptr + 2] * x[j + 2]
                 + ptrA[ptr + 3] * x[j + 3];
      }
      for (int j = extra; j < cols; j++) {
        alphaTmp += ptrA[idx + j] * x[j];
      }
      y[i] += alphaTmp;
    }
  }

  /**
   *{@inheritDoc}
   */
  @Override
 public void dm_inplace_AT_times_x_plus_beta_times_y(double[] y, DenseMatrix A, double[] x, double beta) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    for (int i = 0; i < cols; i++) {
      y[i] = beta * y[i];
    }
    dm_inplace_AT_times_x_plus_y(y, A, x);
  }

  /**
   *{@inheritDoc}
   */
  @Override
  public void dm_inplace_alpha_times_A_times_x_plus_beta_times_y(double[] y, double alpha, DenseMatrix A, double[] x, double beta) { //CSIGNORE
    final double[] ptrA = A.getData();
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
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
        alphaTmp += ptrA[ptr] * x[j]
                 + ptrA[ptr + 1] * x[j + 1]
                 + ptrA[ptr + 2] * x[j + 2]
                 + ptrA[ptr + 3] * x[j + 3];
      }
      y[i] += alpha * alphaTmp;
      for (int j = extra; j < cols; j++) {
        y[i] += alpha * ptrA[idx + j] * x[j];
      }
    }
  }

  /** TODO: Decide if function chaining and therefore JIT friendly methods are better than hardcoded optimised stuff that's less likely to be JITed */
  /**
   *{@inheritDoc}
   */
  @Override
  public void dm_inplace_alpha_times_AT_times_x_plus_beta_times_y(double[] y, double alpha, DenseMatrix A, double[] x, double beta) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    for (int i = 0; i < cols; i++) {
      y[i] = beta * y[i];
    }
    dm_inplace_alpha_times_AT_times_x_plus_y(y, alpha, A, x);
  }

}
