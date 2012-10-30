/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelimplementations;

import java.util.Arrays;

import com.opengamma.maths.lowlevelapi.datatypes.primitive.SparseCoordinateFormatMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.BLAS1;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelabstractions.BLAS2DGEMVKernelAbstraction;

/**
 *
 */
public final class DGEMVForCOOMatrix extends BLAS2DGEMVKernelAbstraction<SparseCoordinateFormatMatrix> {
  private static DGEMVForCOOMatrix s_instance = new DGEMVForCOOMatrix();

  public static DGEMVForCOOMatrix getInstance() {
    return s_instance;
  }

  private DGEMVForCOOMatrix() {
  }

  @Override
  public double[] dm_stateless_A_times_x(SparseCoordinateFormatMatrix A, double[] x) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    final int[] rco = A.getRowCoordinates();
    final int[] cco = A.getColumnCoordinates();
    final double[] data = A.getNonZeroEntries();
    double[] tmp = new double[rows];
    final int len = rco.length;
    // fingers crossed the data is laid out row wise else this'll thrash cache nicely
    for (int i = 0; i < len; i++) {
      tmp[rco[i]] += data[i] * x[cco[i]];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_AT_times_x(SparseCoordinateFormatMatrix A, double[] x) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    final int[] rco = A.getRowCoordinates();
    final int[] cco = A.getColumnCoordinates();
    final double[] data = A.getNonZeroEntries();
    double[] tmp = new double[cols];
    final int len = rco.length;
    for (int i = 0; i < len; i++) {
      tmp[cco[i]] += data[i] * x[rco[i]];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x(double alpha, SparseCoordinateFormatMatrix A, double[] x) { //CSIGNORE
    double[] tmp = dm_stateless_A_times_x(A, x);
    BLAS1.dscalInplace(alpha, tmp);
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x(double alpha, SparseCoordinateFormatMatrix A, double[] x) { //CSIGNORE
    double[] tmp = dm_stateless_AT_times_x(A, x);
    BLAS1.dscalInplace(alpha, tmp);
    return tmp;
  }

  @Override
  public double[] dm_stateless_A_times_x_plus_y(SparseCoordinateFormatMatrix A, double[] x, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    final int[] rco = A.getRowCoordinates();
    final int[] cco = A.getColumnCoordinates();
    final double[] data = A.getNonZeroEntries();
    double[] tmp = new double[rows];
    System.arraycopy(y, 0, tmp, 0, rows);
    final int len = rco.length;
    for (int i = 0; i < len; i++) {
      tmp[rco[i]] += data[i] * x[cco[i]];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_AT_times_x_plus_y(SparseCoordinateFormatMatrix A, double[] x, double[] y) { //CSIGNORE
    final int cols = A.getNumberOfColumns();
    final int[] rco = A.getRowCoordinates();
    final int[] cco = A.getColumnCoordinates();
    final double[] data = A.getNonZeroEntries();
    double[] tmp = new double[cols];
    System.arraycopy(y, 0, tmp, 0, cols);
    final int len = rco.length;
    for (int i = 0; i < len; i++) {
      tmp[cco[i]] += data[i] * x[rco[i]];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x_plus_y(double alpha, SparseCoordinateFormatMatrix A, double[] x, double[] y) { //CSIGNORE
    double[] tmp = dm_stateless_A_times_x(A, x);
    return BLAS1.daxpy(alpha, tmp, y);
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x_plus_y(double alpha, SparseCoordinateFormatMatrix A, double[] x, double[] y) { //CSIGNORE
    double[] tmp = dm_stateless_AT_times_x(A, x);
    return BLAS1.daxpy(alpha, tmp, y);
  }

  @Override
  public double[] dm_stateless_A_times_x_plus_beta_times_y(SparseCoordinateFormatMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    double[] tmp = dm_stateless_A_times_x(A, x);
    return BLAS1.daxpy(beta, y, tmp);
  }

  @Override
  public double[] dm_stateless_AT_times_x_plus_beta_times_y(SparseCoordinateFormatMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    double[] tmp = dm_stateless_AT_times_x(A, x);
    return BLAS1.daxpy(beta, y, tmp);
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x_plus_beta_times_y(double alpha, SparseCoordinateFormatMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    double[] tmp = dm_stateless_A_times_x(A, x);
    final int rows = A.getNumberOfRows();
    for (int i = 0; i < rows; i++) {
      tmp[i] = tmp[i] * alpha + beta * y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x_plus_beta_times_y(double alpha, SparseCoordinateFormatMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    double[] tmp = dm_stateless_AT_times_x(A, x);
    final int cols = A.getNumberOfColumns();
    for (int i = 0; i < cols; i++) {
      tmp[i] = tmp[i] * alpha + beta * y[i];
    }
    return tmp;
  }

  @Override
  public void dm_inplace_A_times_x(double[] y, SparseCoordinateFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rco = A.getRowCoordinates();
    final int[] cco = A.getColumnCoordinates();
    final double[] data = A.getNonZeroEntries();
    final int len = rco.length;
    Arrays.fill(y, 0d);
    for (int i = 0; i < len; i++) {
      y[rco[i]] += data[i] * x[cco[i]];
    }
  }

  @Override
  public void dm_inplace_AT_times_x(double[] y, SparseCoordinateFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rco = A.getRowCoordinates();
    final int[] cco = A.getColumnCoordinates();
    final double[] data = A.getNonZeroEntries();
    final int len = rco.length;
    Arrays.fill(y, 0d);
    for (int i = 0; i < len; i++) {
      y[cco[i]] += data[i] * x[rco[i]];
    }
  }

  @Override
  public void dm_inplace_alpha_times_A_times_x(double[] y, double alpha, SparseCoordinateFormatMatrix A, double[] x) { //CSIGNORE
    dm_inplace_A_times_x(y, A, x);
    BLAS1.dscalInplace(alpha, y);
  }

  @Override
  public void dm_inplace_alpha_times_AT_times_x(double[] y, double alpha, SparseCoordinateFormatMatrix A, double[] x) { //CSIGNORE
    dm_inplace_AT_times_x(y, A, x);
    BLAS1.dscalInplace(alpha, y);
  }

  @Override
  public void dm_inplace_A_times_x_plus_y(double[] y, SparseCoordinateFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rco = A.getRowCoordinates();
    final int[] cco = A.getColumnCoordinates();
    final double[] data = A.getNonZeroEntries();
    final int len = rco.length;
    for (int i = 0; i < len; i++) {
      y[rco[i]] += data[i] * x[cco[i]];
    }
  }

  @Override
  public void dm_inplace_AT_times_x_plus_y(double[] y, SparseCoordinateFormatMatrix A, double[] x) { //CSIGNORE
    final int[] rco = A.getRowCoordinates();
    final int[] cco = A.getColumnCoordinates();
    final double[] data = A.getNonZeroEntries();
    final int len = rco.length;
    for (int i = 0; i < len; i++) {
      y[cco[i]] += data[i] * x[rco[i]];
    }
  }

  @Override
  public void dm_inplace_alpha_times_A_times_x_plus_y(double[] y, double alpha, SparseCoordinateFormatMatrix A, double[] x) { //CSIGNORE
    double[] tmp = dm_stateless_A_times_x(A, x);
    BLAS1.daxpyInplace(alpha, tmp, y);
  }

  @Override
  public void dm_inplace_alpha_times_AT_times_x_plus_y(double[] y, double alpha, SparseCoordinateFormatMatrix A, double[] x) { //CSIGNORE
    double[] tmp = dm_stateless_AT_times_x(A, x);
    BLAS1.daxpyInplace(alpha, tmp, y);
  }

  @Override
  public void dm_inplace_A_times_x_plus_beta_times_y(double[] y, SparseCoordinateFormatMatrix A, double[] x, double beta) { //CSIGNORE
    BLAS1.dscalInplace(beta, y);
    dm_inplace_A_times_x_plus_y(y, A, x);
  }

  @Override
  public void dm_inplace_AT_times_x_plus_beta_times_y(double[] y, SparseCoordinateFormatMatrix A, double[] x, double beta) { //CSIGNORE
    BLAS1.dscalInplace(beta, y);
    dm_inplace_AT_times_x_plus_y(y, A, x);
  }

  @Override
  public void dm_inplace_alpha_times_A_times_x_plus_beta_times_y(double[] y, double alpha, SparseCoordinateFormatMatrix A, double[] x, double beta) { //CSIGNORE
    double[] tmp = dm_stateless_A_times_x(A, x);
    final int rows = A.getNumberOfRows();
    for (int i = 0; i < rows; i++) {
      y[i] = tmp[i] * alpha + beta * y[i];
    }
  }

  @Override
  public void dm_inplace_alpha_times_AT_times_x_plus_beta_times_y(double[] y, double alpha, SparseCoordinateFormatMatrix A, double[] x, double beta) { //CSIGNORE
    double[] tmp = dm_stateless_AT_times_x(A, x);
    final int cols = A.getNumberOfColumns();
    for (int i = 0; i < cols; i++) {
      y[i] = tmp[i] * alpha + beta * y[i];
    }
  }

}
