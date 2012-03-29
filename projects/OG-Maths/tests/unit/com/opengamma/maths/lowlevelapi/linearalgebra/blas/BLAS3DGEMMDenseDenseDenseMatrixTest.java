/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.blas;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.datatypes.primitive.DenseMatrix;

/**
 * tests the BLAS3 DGEMM kernel on A=dense, B=dense, C=dense matrices.
 */
public class BLAS3DGEMMDenseDenseDenseMatrixTest {

  double[][] aData5x5={{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14,15},{16,17,18,19,20},{21,22,23,24,25}};
  double[][] bData5x5={{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14,15},{16,17,18,19,20},{21,22,23,24,25}};
  DenseMatrix aMatrix5by5 = new DenseMatrix(aData5x5);
  DenseMatrix bMatrix5by5 = new DenseMatrix(bData5x5);

  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5() {

  }


}
