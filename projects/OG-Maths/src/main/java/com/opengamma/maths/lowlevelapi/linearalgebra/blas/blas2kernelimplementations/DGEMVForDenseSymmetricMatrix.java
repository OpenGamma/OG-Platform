/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelimplementations;

import java.util.Arrays;

import com.opengamma.maths.lowlevelapi.datatypes.primitive.DenseSymmetricMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.BLAS1;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelabstractions.BLAS2DGEMVKernelAbstraction;

/**
 * Does DGEMV like operations on the {@link DenseSymmetricMatrix} type
 */
public final class DGEMVForDenseSymmetricMatrix extends BLAS2DGEMVKernelAbstraction<DenseSymmetricMatrix> {

  private static DGEMVForDenseSymmetricMatrix s_instance = new DGEMVForDenseSymmetricMatrix();

  public static DGEMVForDenseSymmetricMatrix getInstance() {
    return s_instance;
  }

  private DGEMVForDenseSymmetricMatrix() {
  }

  @Override
  public double[] dm_stateless_A_times_x(DenseSymmetricMatrix A, double[] x) { //CSIGNORE
    final int n = A.getNumberOfRows();
    double[] tmp = new double[n];
    double[] data = A.getData();
    int ptr = 0;
    for (int i = 0; i < n; i++) {
      tmp[i] += data[ptr] * x[i];
      ptr++;
      for (int j = i + 1; j < n; j++) {
        tmp[i] += data[ptr] * x[j];
        tmp[j] += data[ptr] * x[i];
        ptr++;
      }
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_AT_times_x(DenseSymmetricMatrix A, double[] x) { //CSIGNORE
    return dm_stateless_A_times_x(A, x);
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x(double alpha, DenseSymmetricMatrix A, double[] x) { //CSIGNORE
    return BLAS1.dscal(alpha, dm_stateless_A_times_x(A, x));
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x(double alpha, DenseSymmetricMatrix A, double[] x) { //CSIGNORE
    return dm_stateless_alpha_times_A_times_x(alpha, A, x);
  }

  @Override
  public double[] dm_stateless_A_times_x_plus_y(DenseSymmetricMatrix A, double[] x, double[] y) { //CSIGNORE
    final int n = A.getNumberOfRows();
    double[] tmp = new double[n];
    double[] data = A.getData();
    int ptr = 0;
    for (int i = 0; i < n; i++) {
      tmp[i] += data[ptr] * x[i];
      ptr++;
      for (int j = i + 1; j < n; j++) {
        tmp[i] += data[ptr] * x[j];
        tmp[j] += data[ptr] * x[i];
        ptr++;
      }
      tmp[i] += y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_AT_times_x_plus_y(DenseSymmetricMatrix A, double[] x, double[] y) { //CSIGNORE
    return dm_stateless_A_times_x_plus_y(A, x, y);
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x_plus_y(double alpha, DenseSymmetricMatrix A, double[] x, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] = alpha * tmp[i] + y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x_plus_y(double alpha, DenseSymmetricMatrix A, double[] x, double[] y) { //CSIGNORE
    return dm_stateless_alpha_times_A_times_x_plus_y(alpha, A, x, y);
  }

  @Override
  public double[] dm_stateless_A_times_x_plus_beta_times_y(DenseSymmetricMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] += beta * y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_AT_times_x_plus_beta_times_y(DenseSymmetricMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    return dm_stateless_A_times_x_plus_beta_times_y(A, x, beta, y);
  }

  @Override
  public double[] dm_stateless_alpha_times_A_times_x_plus_beta_times_y(double alpha, DenseSymmetricMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    final int rows = A.getNumberOfRows();
    double[] tmp = dm_stateless_A_times_x(A, x);
    for (int i = 0; i < rows; i++) {
      tmp[i] = alpha * tmp[i] + beta * y[i];
    }
    return tmp;
  }

  @Override
  public double[] dm_stateless_alpha_times_AT_times_x_plus_beta_times_y(double alpha, DenseSymmetricMatrix A, double[] x, double beta, double[] y) { //CSIGNORE
    return dm_stateless_alpha_times_A_times_x_plus_beta_times_y(alpha, A, x, beta, y);
  }

  @Override
  public void dm_inplace_A_times_x(double[] y, DenseSymmetricMatrix A, double[] x) { //CSIGNORE
    final int n = A.getNumberOfRows();
    double[] data = A.getData();
    int ptr = 0;
    Arrays.fill(y, 0);
    for (int i = 0; i < n; i++) {
      y[i] += data[ptr] * x[i];
      ptr++;
      for (int j = i + 1; j < n; j++) {
        y[i] += data[ptr] * x[j];
        y[j] += data[ptr] * x[i];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_AT_times_x(double[] y, DenseSymmetricMatrix A, double[] x) { //CSIGNORE
    dm_inplace_A_times_x(y, A, x);
  }

  @Override
  public void dm_inplace_alpha_times_A_times_x(double[] y, double alpha, DenseSymmetricMatrix A, double[] x) { //CSIGNORE
    final int n = A.getNumberOfRows();
    double[] data = A.getData();
    int ptr = 0;
    Arrays.fill(y, 0);
    for (int i = 0; i < n; i++) {
      y[i] += data[ptr] * x[i];
      ptr++;
      for (int j = i + 1; j < n; j++) {
        y[i] += data[ptr] * x[j];
        y[j] += data[ptr] * x[i];
        ptr++;
      }
    }
    BLAS1.dscalInplace(alpha, y);
  }

  @Override
  public void dm_inplace_alpha_times_AT_times_x(double[] y, double alpha, DenseSymmetricMatrix A, double[] x) { //CSIGNORE
    dm_inplace_alpha_times_A_times_x(y, alpha, A, x);
  }

  @Override
  public void dm_inplace_A_times_x_plus_y(double[] y, DenseSymmetricMatrix A, double[] x) { //CSIGNORE
    final int n = A.getNumberOfRows();
    double[] data = A.getData();
    int ptr = 0;
    for (int i = 0; i < n; i++) {
      y[i] += data[ptr] * x[i];
      ptr++;
      for (int j = i + 1; j < n; j++) {
        y[i] += data[ptr] * x[j];
        y[j] += data[ptr] * x[i];
        ptr++;
      }
    }
  }

  @Override
  public void dm_inplace_AT_times_x_plus_y(double[] y, DenseSymmetricMatrix A, double[] x) { //CSIGNORE
    dm_inplace_A_times_x_plus_y(y, A, x);
  }

  @Override
  public void dm_inplace_alpha_times_A_times_x_plus_y(double[] y, double alpha, DenseSymmetricMatrix A, double[] x) { //CSIGNORE
    double[] tmp = dm_stateless_A_times_x(A, x);
    final int n = A.getNumberOfRows();
    for (int i = 0; i < n; i++) {
      y[i] += tmp[i] * alpha;
    }
  }

  @Override
  public void dm_inplace_alpha_times_AT_times_x_plus_y(double[] y, double alpha, DenseSymmetricMatrix A, double[] x) { //CSIGNORE
    dm_inplace_alpha_times_A_times_x_plus_y(y, alpha, A, x);
  }

  @Override
  public void dm_inplace_A_times_x_plus_beta_times_y(double[] y, DenseSymmetricMatrix A, double[] x, double beta) { //CSIGNORE
    double[] tmp = dm_stateless_A_times_x(A, x);
    final int n = A.getNumberOfRows();
    for (int i = 0; i < n; i++) {
      y[i] = y[i] * beta + tmp[i];
    }
  }

  @Override
  public void dm_inplace_AT_times_x_plus_beta_times_y(double[] y, DenseSymmetricMatrix A, double[] x, double beta) { //CSIGNORE
    dm_inplace_A_times_x_plus_beta_times_y(y, A, x, beta);
  }

  @Override
  public void dm_inplace_alpha_times_A_times_x_plus_beta_times_y(double[] y, double alpha, DenseSymmetricMatrix A, double[] x, double beta) { //CSIGNORE
    double[] tmp = dm_stateless_AT_times_x(A, x);
    final int n = A.getNumberOfColumns();
    for (int i = 0; i < n; i++) {
      y[i] = beta * y[i] + alpha * tmp[i];
    }
  }

  @Override
  public void dm_inplace_alpha_times_AT_times_x_plus_beta_times_y(double[] y, double alpha, DenseSymmetricMatrix A, double[] x, double beta) { //CSIGNORE
    dm_inplace_alpha_times_A_times_x_plus_beta_times_y(y, alpha, A, x, beta);
  }

} // class end
