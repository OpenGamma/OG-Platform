/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelimplementations;

import java.util.Arrays;

import com.opengamma.maths.lowlevelapi.datatypes.primitive.CompressedSparseRowFormatMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.BLAS1;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelabstractions.BLAS2DGEMVKernelAbstraction;

/**
 * Does DGEMV like operations on the {@link CompressedSparseRowFormatMatrix} type
 */
public final class DGEMVForCSRMatrix extends BLAS2DGEMVKernelAbstraction<CompressedSparseRowFormatMatrix> {
  private static DGEMVForCSRMatrix s_instance = new DGEMVForCSRMatrix();

  public static DGEMVForCSRMatrix getInstance() {
    return s_instance;
  }

  private DGEMVForCSRMatrix() {
  }

  @Override
  public double[] dm_stateless_A_times_x(CompressedSparseRowFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final int[] colIdx = A.getColumnIndex();
    final double[] values = A.getNonZeroElements();
    final int rows = A.getNumberOfRows();
    double[] tmp = new double[rows];
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        tmp[i] += values[ptr] * x[colIdx[ptr]];
        ptr++;
      }
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_AT_times_x(CompressedSparseRowFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final double[] values = A.getNonZeroElements();
    final int[] colIdx = A.getColumnIndex();
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
    double[] tmp = new double[cols];
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        tmp[colIdx[ptr]] += values[ptr] * x[i];
        ptr++;
      }
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x(double alpha, CompressedSparseRowFormatMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] *= alpha;
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x(double alpha, CompressedSparseRowFormatMatrix A, double[] x) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    double[] tmp = dm_stateless_AT_times_x(A, x);
    for (int i = 0; i < cols; i++) {
      tmp[i] *= alpha;
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_A_times_x_plus_y(CompressedSparseRowFormatMatrix A, double[] x, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] += y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_AT_times_x_plus_y(CompressedSparseRowFormatMatrix A, double[] x, double[] y) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    double[] tmp = new double[cols];
    tmp = dm_stateless_AT_times_x(A, x);
    for (int i = 0; i < cols; i++) {
      tmp[i] += y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x_plus_y(double alpha, CompressedSparseRowFormatMatrix A, double[] x, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] = alpha * tmp[i] + y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x_plus_y(double alpha, CompressedSparseRowFormatMatrix A, double[] x, double[] y) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    double[] tmp = new double[cols];
    tmp = dm_stateless_AT_times_x(A, x);
    for (int i = 0; i < cols; i++) {
      tmp[i] = alpha * tmp[i] + y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_A_times_x_plus_beta_times_y(CompressedSparseRowFormatMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] += beta * y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_AT_times_x_plus_beta_times_y(CompressedSparseRowFormatMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    double[] tmp = dm_stateless_AT_times_x(A, x);
    for (int i = 0; i < cols; i++) {
      tmp[i] += beta * y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x_plus_beta_times_y(double alpha, CompressedSparseRowFormatMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] = alpha * tmp[i] + beta * y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x_plus_beta_times_y(double alpha, CompressedSparseRowFormatMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    double[] tmp = dm_stateless_AT_times_x(A, x);
    for (int i = 0; i < cols; i++) {
      tmp[i] = alpha * tmp[i] + beta * y[i];
    }
    return tmp;
  }

  @Override
  public void dm_inplace_A_times_x(double[] y, CompressedSparseRowFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final int[] colIdx = A.getColumnIndex();
    final double[] values = A.getNonZeroElements();
    final int rows = A.getNumberOfRows();
    Arrays.fill(y, 0);
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        y[i] += values[ptr] * x[colIdx[ptr]];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_AT_times_x(double[] y, CompressedSparseRowFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final double[] values = A.getNonZeroElements();
    final int[] colIdx = A.getColumnIndex();
    final int rows = A.getNumberOfRows();
    Arrays.fill(y, 0);
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        y[colIdx[ptr]] += values[ptr] * x[i];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_alpha_times_A_times_x(double[] y, double alpha, CompressedSparseRowFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final int[] colIdx = A.getColumnIndex();
    final double[] values = A.getNonZeroElements();
    final int rows = A.getNumberOfRows();
    Arrays.fill(y, 0);
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        y[i] += values[ptr] * x[colIdx[ptr]];
        ptr++;
      }
      y[i] *= alpha;
    }
  }

  @Override
  public void dm_inplace_alpha_times_AT_times_x(double[] y, double alpha, CompressedSparseRowFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final double[] values = A.getNonZeroElements();
    final int[] colIdx = A.getColumnIndex();
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
    Arrays.fill(y, 0);
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        y[colIdx[ptr]] += values[ptr] * x[i];
        ptr++;
      }
    }
    for (int i = 0; i < cols; i++) {
      y[i] *= alpha;
    }
  }

  @Override
  public void dm_inplace_A_times_x_plus_y(double[] y, CompressedSparseRowFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final int[] colIdx = A.getColumnIndex();
    final double[] values = A.getNonZeroElements();
    final int rows = A.getNumberOfRows();
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        y[i] += values[ptr] * x[colIdx[ptr]];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_AT_times_x_plus_y(double[] y, CompressedSparseRowFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final double[] values = A.getNonZeroElements();
    final int[] colIdx = A.getColumnIndex();
    final int rows = A.getNumberOfRows();
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        y[colIdx[ptr]] += values[ptr] * x[i];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_alpha_times_A_times_x_plus_y(double[] y, double alpha, CompressedSparseRowFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final int[] colIdx = A.getColumnIndex();
    final double[] values = A.getNonZeroElements();
    final int rows = A.getNumberOfRows();
    double acc;
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      acc = 0;
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        acc += values[ptr] * x[colIdx[ptr]];
        ptr++;
      }
      y[i] += acc * alpha;
    }
  }

  @Override
  public void dm_inplace_alpha_times_AT_times_x_plus_y(double[] y, double alpha, CompressedSparseRowFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final double[] values = A.getNonZeroElements();
    final int[] colIdx = A.getColumnIndex();
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
    double[] tmp = new double[cols];
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        tmp[colIdx[ptr]] += values[ptr] * x[i];
        ptr++;
      }
    }
    for (int i = 0; i < cols; i++) {
      y[i] += tmp[i] * alpha;
    }
  }

  @Override
  public void dm_inplace_A_times_x_plus_beta_times_y(double[] y, CompressedSparseRowFormatMatrix A, double[] x, double beta) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final int[] colIdx = A.getColumnIndex();
    final double[] values = A.getNonZeroElements();
    final int rows = A.getNumberOfRows();
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      y[i] *= beta;
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        y[i] += values[ptr] * x[colIdx[ptr]];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_AT_times_x_plus_beta_times_y(double[] y, CompressedSparseRowFormatMatrix A, double[] x, double beta) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final double[] values = A.getNonZeroElements();
    final int[] colIdx = A.getColumnIndex();
    final int rows = A.getNumberOfRows();
    BLAS1.dscalInplace(beta, y);
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        y[colIdx[ptr]] += values[ptr] * x[i];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_alpha_times_A_times_x_plus_beta_times_y(double[] y, double alpha, CompressedSparseRowFormatMatrix A, double[] x, double beta) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final int[] colIdx = A.getColumnIndex();
    final double[] values = A.getNonZeroElements();
    final int rows = A.getNumberOfRows();
    int ptr = 0;
    double acc;
    for (int i = 0; i < rows; i++) {
      y[i] *= beta;
      acc = 0;
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        acc += values[ptr] * x[colIdx[ptr]];
        ptr++;
      }
      y[i] += alpha * acc;
    }
  }

  @Override
  public void dm_inplace_alpha_times_AT_times_x_plus_beta_times_y(double[] y, double alpha, CompressedSparseRowFormatMatrix A, double[] x, double beta) { //CSIGNORE
    final int[] rowPtr = A.getRowPtr();
    final double[] values = A.getNonZeroElements();
    final int[] colIdx = A.getColumnIndex();
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
    double[] tmp = new double[cols];
    int ptr = 0;
    for (int i = 0; i < rows; i++) {
      for (int j = rowPtr[i]; j < rowPtr[i + 1]; j++) {
        tmp[colIdx[ptr]] += values[ptr] * x[i];
        ptr++;
      }
    }
    for (int i = 0; i < cols; i++) {
      y[i] = beta * y[i] + tmp[i] * alpha;
    }
  }

}
