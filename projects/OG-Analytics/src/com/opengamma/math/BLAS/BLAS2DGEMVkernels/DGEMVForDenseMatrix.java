/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.BLAS.BLAS2DGEMVkernels;

import com.opengamma.math.BLAS.BLAS2KernelAbstraction;

/**
 *
 */
public final class DGEMVForDenseMatrix extends BLAS2KernelAbstraction {
  private static DGEMVForDenseMatrix s_instance = new DGEMVForDenseMatrix();

  public static DGEMVForDenseMatrix getInstance() {
    return s_instance;
  }

  private DGEMVForDenseMatrix() {}
}
