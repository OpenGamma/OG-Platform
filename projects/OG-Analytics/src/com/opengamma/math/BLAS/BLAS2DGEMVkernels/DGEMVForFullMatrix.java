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
public final class DGEMVForFullMatrix extends BLAS2KernelAbstraction {
  private static DGEMVForFullMatrix s_instance = new DGEMVForFullMatrix();

  public static DGEMVForFullMatrix getInstance() {
    return s_instance;
  }

  private DGEMVForFullMatrix() {}
}
