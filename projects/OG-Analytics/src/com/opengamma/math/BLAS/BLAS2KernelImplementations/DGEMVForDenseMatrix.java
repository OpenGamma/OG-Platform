/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.BLAS.BLAS2KernelImplementations;

import com.opengamma.math.BLAS.BLAS2KernelAbstractions.BLAS2DGEMVKernelAbstraction;
import com.opengamma.math.matrix.DenseMatrix;

/**
 * Does DGEMV like operations on the DenseMatrix type
 */
public final class DGEMVForDenseMatrix extends BLAS2DGEMVKernelAbstraction<DenseMatrix> {
  private static DGEMVForDenseMatrix s_instance = new DGEMVForDenseMatrix();

  public static DGEMVForDenseMatrix getInstance() {
    return s_instance;
  }

  private DGEMVForDenseMatrix() {
  }

  @Override
  public double[] dm_stateless_A_times_x(DenseMatrix aMatrix, double[] aVector) {
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

  @Override
  public double[] dm_stateless_AT_times_x(DenseMatrix aMatrix, double[] aVector) {
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
  @Override
  public double[] dm_stateless_alpha_times_A_times_x(double alpha, DenseMatrix aMatrix, double[] aVector) {
    final int rows = aMatrix.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(aMatrix, aVector);
    for (int i = 0; i < rows; i++) {
      tmp[i] *= alpha; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  /* TODO: Replace vector scalings with BLAS1 calls.*/
  @Override
  public double[] dm_stateless_alpha_times_AT_times_x(double alpha, DenseMatrix aMatrix, double[] aVector) {
    final int cols = aMatrix.getNumberOfColumns();
    double[] tmp = dm_stateless_AT_times_x(aMatrix, aVector);
    for (int i = 0; i < cols; i++) {
      tmp[i] *= alpha; // slight cache thrash but should help force A*x to be JITed
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_A_times_x_plus_y() {
    return null;
  }

  @Override
  public double[] dm_stateless_AT_times_x_plus_y() {
    return null;
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x_plus_y() {
    return null;
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x_plus_y() {
    return null;
  }

  @Override
  public double[] dm_stateless_A_times_x_plus_beta_times_y() {
    return null;
  }

  @Override
  public double[] dm_stateless_AT_times_x_plus_beta_times_y() {
    return null;
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x_plus_beta_times_y() {
    return null;
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x_plus_beta_times_y() {
    return null;
  }

  @Override
  public double[] dm_inplace_A_times_x() {
    return null;
  }

  @Override
  public double[] dm_inplace_AT_times_x() {
    return null;
  }

  @Override
  public double[] dm_inplace_alpha_times_A_times_x() {
    return null;
  }

  @Override
  public double[] dm_inplace_alpha_times_AT_times_x() {
    return null;
  }

  @Override
  public double[] dm_inplace_A_times_x_plus_y() {
    return null;
  }

  @Override
  public double[] dm_inplace_AT_times_x_plus_y() {
    return null;
  }

  @Override
  public double[] dm_inplace_alpha_times_A_times_x_plus_y() {
    return null;
  }

  @Override
  public double[] dm_inplace_alpha_times_AT_times_x_plus_y() {
    return null;
  }

  @Override
  public double[] dm_inplace_A_times_x_plus_beta_times_y() {
    return null;
  }

  @Override
  public double[] dm_inplace_AT_times_x_plus_beta_times_y() {
    return null;
  }

  @Override
  public double[] dm_inplace_alpha_times_A_times_x_plus_beta_times_y() {
    return null;
  }

  @Override
  public double[] dm_inplace_alpha_times_AT_times_x_plus_beta_times_y() {
    return null;
  }

  }
