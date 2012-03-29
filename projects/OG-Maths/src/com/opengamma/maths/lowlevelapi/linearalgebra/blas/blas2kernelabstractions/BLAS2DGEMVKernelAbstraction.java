/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.blas.blas2kernelabstractions;

import com.opengamma.analytics.math.matrix.Matrix;

/**
 * BLAS2*KernelAbstraction classes are used to kinda emulate function pointer passing.
 * We want a unified set of BLAS templates with Matrix type, machine and performance specific kernels.
 * This class helps the class hierarchy mangle achieve this for DGEMV.
 *
 * If you want to add a new BLAS2 DGEMV kernel set for a matrix type of your choice then just implement this
 * and update the corresponding hashmap in BLAS2.
 * @param <T> a kind of matrix
 *
 * Function names starting with "dm_" are "direct mathematics" functions, their names are deliberately written with
 * underscores in to split out the mathematical operations they perform for ease of reading.
 *
 */
public abstract class BLAS2DGEMVKernelAbstraction<T extends Matrix<Double>> {
  //public abstract  class  BLAS2DGEMVKernelAbstraction {
  /* Stateless manipulators */
  //  {

  /* GROUP1:: A*x OR A^T*x */
  /**
   * Stateless DGEMV:: returns A*x
   */
  public abstract double[] dm_stateless_A_times_x(T A, double[] x); //CSIGNORE

  /**
   * Stateless DGEMV:: returns A^T*x
   */
  public abstract double[] dm_stateless_AT_times_x(T A, double[] x); //CSIGNORE

  /* GROUP2:: alpha*A*x OR alpha*A^T*x */
  /**
   * Stateless DGEMV:: returns alpha*A*x
   */
  public abstract double[] dm_stateless_alpha_times_A_times_x(double alpha, T A, double[] x); //CSIGNORE

  /**
   * Stateless DGEMV:: returns alpha*A^T*x
   */
  public abstract double[] dm_stateless_alpha_times_AT_times_x(double alpha, T A, double[] x); //CSIGNORE

  /* GROUP3:: A*x + y OR A^T*x + y */
  /**
   * Stateless DGEMV:: returns alpha*A*x
   */
  public abstract double[] dm_stateless_A_times_x_plus_y(T A, double[] x, double[] y); //CSIGNORE

  /**
   * Stateless DGEMV:: returns alpha*A^T*x
   */
  public abstract double[] dm_stateless_AT_times_x_plus_y(T A, double[] x, double[] y); //CSIGNORE

  /* GROUP4:: alpha*A*x + y OR alpha*A^T*x + y */
  /**
   * Stateless DGEMV:: returns alpha*A*x + y
   */
  public abstract double[] dm_stateless_alpha_times_A_times_x_plus_y(double alpha, T A, double[] x, double[] y); //CSIGNORE

  /**
   * Stateless DGEMV:: returns alpha*A^T*x + y
   */
  public abstract double[] dm_stateless_alpha_times_AT_times_x_plus_y(double alpha, T A, double[] x, double[] y); //CSIGNORE

  /* GROUP5:: A*x + beta*y OR A^T*x + beta*y */
  /**
   * Stateless DGEMV:: returns A*x + beta*y
   */
  public abstract double[] dm_stateless_A_times_x_plus_beta_times_y(T A, double[] x, double beta, double[] y); //CSIGNORE

  /**
   * Stateless DGEMV:: returns A^T*x + beta*y
   */
  public abstract double[] dm_stateless_AT_times_x_plus_beta_times_y(T A, double[] x, double beta, double[] y); //CSIGNORE

  /* GROUP6:: alpha*A*x + beta*y OR alpha*A^T*x + beta*y */
  /**
   * Stateless DGEMV:: returns alpha*A*x + beta*y
   */
  public abstract double[] dm_stateless_alpha_times_A_times_x_plus_beta_times_y(double alpha, T A, double[] x, double beta, double[] y); //CSIGNORE

  /**
   * Stateless DGEMV:: returns alpha*A^T*x + beta*y
   */
  public abstract double[] dm_stateless_alpha_times_AT_times_x_plus_beta_times_y(double alpha, T A, double[] x, double beta, double[] y); //CSIGNORE

  //  }

  /* Inplace manipulators */
  //{
  /* GROUP1:: A*x OR A^T*x */
  /**
   * In place DGEMV:: performs y:= A*x
   */
  public abstract void dm_inplace_A_times_x(double[] y, T A, double[] x); //CSIGNORE

  /**
   * In place DGEMV:: performs y:= A^T*x
   */
  public abstract void dm_inplace_AT_times_x(double[] y, T A, double[] x); //CSIGNORE

  /* GROUP2:: alpha*A*x OR alpha*A^T*x */
  /**
   * In place DGEMV:: performs y:= alpha*A*x
   */
  public abstract void dm_inplace_alpha_times_A_times_x(double[] y, double alpha, T A, double[] x); //CSIGNORE

  /**
   * In place DGEMV:: performs y:= alpha*A^T*x
   */
  public abstract void dm_inplace_alpha_times_AT_times_x(double[] y, double alpha, T A, double[] x); //CSIGNORE

  /* GROUP3:: A*x + y OR A^T*x + y */
  /**
   * In place DGEMV:: performs y:= A*x + y
   */
  public abstract void dm_inplace_A_times_x_plus_y(double[] y, T A, double[] x); //CSIGNORE

  /**
   * In place DGEMV:: performs y:= A^T*x + y
   */
  public abstract void dm_inplace_AT_times_x_plus_y(double[] y, T A, double[] x); //CSIGNORE

  /* GROUP4:: alpha*A*x + y OR alpha*A^T*x + y */
  /**
   * In place DGEMV:: performs y:= alpha*A*x + y
   */
  public abstract void dm_inplace_alpha_times_A_times_x_plus_y(double[] y, double alpha, T A, double[] x); //CSIGNORE

  /**
   * In place DGEMV:: performs y:= alpha*A^T*x + y
   */
  public abstract void dm_inplace_alpha_times_AT_times_x_plus_y(double[] y, double alpha, T A, double[] x); //CSIGNORE

  /* GROUP5:: A*x + beta*y OR A^T*x + beta*y */
  /**
   * In place DGEMV:: performs y:= A^T*x + beta*y
   */
  public abstract void dm_inplace_A_times_x_plus_beta_times_y(double[] y, T A, double[] x, double beta); //CSIGNORE

  /**
   * In place DGEMV:: performs y:= A^T*x + beta*y
   */
  public abstract void dm_inplace_AT_times_x_plus_beta_times_y(double[] y, T A, double[] x, double beta); //CSIGNORE

  /* GROUP6:: alpha*A*x + beta*y OR alpha*A^T*x + beta*y */
  /**
   * In place DGEMV:: performs y:= alpha*A^T*x + beta*y
   */
  public abstract void dm_inplace_alpha_times_A_times_x_plus_beta_times_y(double[] y, double alpha, T A, double[] x, double beta); //CSIGNORE
  /**
   * In place DGEMV:: performs y:= alpha*A^T*x + beta*y
   */
  public abstract void dm_inplace_alpha_times_AT_times_x_plus_beta_times_y(double[] y, double alpha, T A, double[] x, double beta); //CSIGNORE
  //}

}
