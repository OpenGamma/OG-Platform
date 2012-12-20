/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelimplementations;

import java.util.Arrays;

import com.opengamma.maths.lowlevelapi.datatypes.primitive.PackedMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.BLAS1;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelabstractions.BLAS2DGEMVKernelAbstraction;

/**
 * Does DGEMV like operations on the {@link PackedMatrix} type
 */
public final class DGEMVForPackedMatrix extends BLAS2DGEMVKernelAbstraction<PackedMatrix> {

  private static DGEMVForPackedMatrix s_instance = new DGEMVForPackedMatrix();

  public static DGEMVForPackedMatrix getInstance() {
    return s_instance;
  }

  private DGEMVForPackedMatrix() {
  }

  @Override
  public double[] dm_stateless_A_times_x(PackedMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = new double[rows];
    int[] colCount = A.getColCount();
    int[] rowPtr = A.getRowPointer();
    double[] data = A.getData();
    int ptr = 0;
    int rowstart;
    int lim;
    for (int i = 0; i < rows; i++) {
      rowstart = rowPtr[i];
      lim = colCount[i + 1] - colCount[i];
      for (int j = 0; j < lim; j++) {
        tmp[i] += data[ptr] * x[rowstart + j];
        ptr++;
      }
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_AT_times_x(PackedMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    final int cols = A.getNumberOfColumns();
    double[] tmp = new double[cols];
    int[] colCount = A.getColCount();
    int[] rowPtr = A.getRowPointer();
    double[] data = A.getData();
    int ptr = 0;
    int rowstart;
    int lim;
    for (int i = 0; i < rows; i++) {
      rowstart = rowPtr[i];
      lim = colCount[i + 1] - colCount[i];
      for (int j = 0; j < lim; j++) {
        tmp[rowstart + j] += data[ptr] * x[i];
        ptr++;
      }
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x(double alpha, PackedMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = new double[rows];
    int[] colCount = A.getColCount();
    int[] rowPtr = A.getRowPointer();
    double[] data = A.getData();
    int ptr = 0;
    int rowstart;
    int lim;
    for (int i = 0; i < rows; i++) {
      rowstart = rowPtr[i];
      lim = colCount[i + 1] - colCount[i];
      for (int j = 0; j < lim; j++) {
        tmp[i] += data[ptr] * x[rowstart + j];
        ptr++;
      }
      tmp[i] *= alpha; // considering its in cache avoid BLAS1 on outerloop
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x(double alpha, PackedMatrix A, double[] x) { //CSIGNORE
    double[] tmp = dm_stateless_AT_times_x(A, x);
    BLAS1.dscalInplace(alpha, tmp);
    return tmp;
  }

  @Override
  public double[] dm_stateless_A_times_x_plus_y(PackedMatrix A, double[] x, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] += y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_AT_times_x_plus_y(PackedMatrix A, double[] x, double[] y) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    double[] tmp = dm_stateless_AT_times_x(A, x);
    for (int i = 0; i < cols; i++) {
      tmp[i] += y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x_plus_y(double alpha, PackedMatrix A, double[] x, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] = alpha * tmp[i] + y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x_plus_y(double alpha, PackedMatrix A, double[] x, double[] y) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    double[] tmp = new double[cols];
    tmp = dm_stateless_AT_times_x(A, x);
    for (int i = 0; i < cols; i++) {
      tmp[i] = alpha * tmp[i] + y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_A_times_x_plus_beta_times_y(PackedMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] += beta * y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_AT_times_x_plus_beta_times_y(PackedMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    double[] tmp = dm_stateless_AT_times_x(A, x);
    for (int i = 0; i < cols; i++) {
      tmp[i] += beta * y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x_plus_beta_times_y(double alpha, PackedMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] = alpha * tmp[i] + beta * y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x_plus_beta_times_y(double alpha, PackedMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    double[] tmp = dm_stateless_AT_times_x(A, x);
    for (int i = 0; i < cols; i++) {
      tmp[i] = alpha * tmp[i] + beta * y[i];
    }
    return tmp;
  }

  @Override
  public void dm_inplace_A_times_x(double[] y, PackedMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    int[] colCount = A.getColCount();
    int[] rowPtr = A.getRowPointer();
    double[] data = A.getData();
    int ptr = 0;
    int rowstart;
    int lim;
    Arrays.fill(y, 0);
    for (int i = 0; i < rows; i++) {
      rowstart = rowPtr[i];
      lim = colCount[i + 1] - colCount[i];
      for (int j = 0; j < lim; j++) {
        y[i] += data[ptr] * x[rowstart + j];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_AT_times_x(double[] y, PackedMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    int[] colCount = A.getColCount();
    int[] rowPtr = A.getRowPointer();
    double[] data = A.getData();
    int ptr = 0;
    int rowstart;
    int lim;
    Arrays.fill(y, 0);
    for (int i = 0; i < rows; i++) {
      rowstart = rowPtr[i];
      lim = colCount[i + 1] - colCount[i];
      for (int j = 0; j < lim; j++) {
        y[rowstart + j] += data[ptr] * x[i];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_alpha_times_A_times_x(double[] y, double alpha, PackedMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    int[] colCount = A.getColCount();
    int[] rowPtr = A.getRowPointer();
    double[] data = A.getData();
    int ptr = 0;
    int rowstart;
    int lim;
    Arrays.fill(y, 0);
    for (int i = 0; i < rows; i++) {
      rowstart = rowPtr[i];
      lim = colCount[i + 1] - colCount[i];
      for (int j = 0; j < lim; j++) {
        y[i] += data[ptr] * x[rowstart + j];
        ptr++;
      }
      y[i] *= alpha;
    }
  }

  @Override
  public void dm_inplace_alpha_times_AT_times_x(double[] y, double alpha, PackedMatrix A, double[] x) { //CSIGNORE
    dm_inplace_AT_times_x(y, A, x);
    BLAS1.dscalInplace(alpha, y);
  }

  @Override
  public void dm_inplace_A_times_x_plus_y(double[] y, PackedMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    int[] colCount = A.getColCount();
    int[] rowPtr = A.getRowPointer();
    double[] data = A.getData();
    int ptr = 0;
    int rowstart;
    int lim;
    for (int i = 0; i < rows; i++) {
      rowstart = rowPtr[i];
      lim = colCount[i + 1] - colCount[i];
      for (int j = 0; j < lim; j++) {
        y[i] += data[ptr] * x[rowstart + j];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_AT_times_x_plus_y(double[] y, PackedMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    int[] colCount = A.getColCount();
    int[] rowPtr = A.getRowPointer();
    double[] data = A.getData();
    int ptr = 0;
    int rowstart;
    int lim;
    for (int i = 0; i < rows; i++) {
      rowstart = rowPtr[i];
      lim = colCount[i + 1] - colCount[i];
      for (int j = 0; j < lim; j++) {
        y[rowstart + j] += data[ptr] * x[i];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_alpha_times_A_times_x_plus_y(double[] y, double alpha, PackedMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    int[] colCount = A.getColCount();
    int[] rowPtr = A.getRowPointer();
    double[] data = A.getData();
    int ptr = 0;
    int rowstart;
    int lim;
    double acc = 0;
    for (int i = 0; i < rows; i++) {
      rowstart = rowPtr[i];
      lim = colCount[i + 1] - colCount[i];
      acc = 0;
      for (int j = 0; j < lim; j++) {
        acc += data[ptr] * x[rowstart + j];
        ptr++;
      }
      y[i] += alpha * acc;
    }
  }

  @Override
  public void dm_inplace_alpha_times_AT_times_x_plus_y(double[] y, double alpha, PackedMatrix A, double[] x) { //CSIGNORE
    double[] tmp = dm_stateless_AT_times_x(A, x);
    final int cols = A.getNumberOfColumns();
    for (int i = 0; i < cols; i++) {
      y[i] = y[i] + alpha * tmp[i];
    }
  }

  @Override
  public void dm_inplace_A_times_x_plus_beta_times_y(double[] y, PackedMatrix A, double[] x, double beta) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    int[] colCount = A.getColCount();
    int[] rowPtr = A.getRowPointer();
    double[] data = A.getData();
    int ptr = 0;
    int rowstart;
    int lim;
    for (int i = 0; i < rows; i++) {
      rowstart = rowPtr[i];
      lim = colCount[i + 1] - colCount[i];
      y[i] *= beta;
      for (int j = 0; j < lim; j++) {
        y[i] += data[ptr] * x[rowstart + j];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_AT_times_x_plus_beta_times_y(double[] y, PackedMatrix A, double[] x, double beta) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    int[] colCount = A.getColCount();
    int[] rowPtr = A.getRowPointer();
    double[] data = A.getData();
    int ptr = 0;
    int rowstart;
    int lim;
    BLAS1.dscalInplace(beta, y);
    for (int i = 0; i < rows; i++) {
      rowstart = rowPtr[i];
      lim = colCount[i + 1] - colCount[i];
      for (int j = 0; j < lim; j++) {
        y[rowstart + j] += data[ptr] * x[i];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_alpha_times_A_times_x_plus_beta_times_y(double[] y, double alpha, PackedMatrix A, double[] x, double beta) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    int[] colCount = A.getColCount();
    int[] rowPtr = A.getRowPointer();
    double[] data = A.getData();
    int ptr = 0;
    int rowstart;
    int lim;
    double acc = 0;
    for (int i = 0; i < rows; i++) {
      rowstart = rowPtr[i];
      lim = colCount[i + 1] - colCount[i];
      acc = 0;
      for (int j = 0; j < lim; j++) {
        acc += data[ptr] * x[rowstart + j];
        ptr++;
      }
      y[i] = y[i] * beta + acc * alpha;
    }
  }

  @Override
  public void dm_inplace_alpha_times_AT_times_x_plus_beta_times_y(double[] y, double alpha, PackedMatrix A, double[] x, double beta) { //CSIGNORE
    double[] tmp = dm_stateless_AT_times_x(A, x);
    final int cols = A.getNumberOfColumns();
    for (int i = 0; i < cols; i++) {
      y[i] = beta * y[i] + alpha * tmp[i];
    }
  }
};
