/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.BLAS;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.math.matrix.CompressedSparseRowFormatMatrix;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.FullMatrix;

/**
 * Tests the BLAS2 library
 */
public class BLAS2Test {

  // dense
  double [][] A ={{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14,15},{16,17,18,19,20},{21,22,23,24,25}};
  double [] x = {1,2,3,4,5};
  double [] y = {10,20,30,40,50};

  // dense m > n
  double [][] oddA ={{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,16},{17,18,19,20}};
  double [] oddx = {1,2,3,4};
  double [] oddy = {10,20,30,40,50};

  // single element data
  double [][] singleA = {{1}};
  double [] singlex = {2};
  double [] singley = {4};

  // sparse
  double[][]sparseA = {{1,2,0,0},{3,0,4,0},{0,5,6,0},{0,0,7,0}};
  double[][]largeSparseA = {{1,2,0,0,0,0,0},{3,0,4,0,0,0,0},{0,5,6,0,0,0,0},{0,0,7,0,0,0,0},{0,0,0,8,0,9,10},{0,11,0,0,12,0,13},{0,0,0,0,14,0,0}};
  double[] largeX = {1,2,3,4,5,6,7};

  // the scalings
  double alpha = 7.0;
  double beta = -3.0;

  // casts to objects for tests
  FullMatrix aMatrix = new FullMatrix(A);
  FullMatrix oddaMatrix = new FullMatrix(oddA);
  FullMatrix singleaMatrix = new FullMatrix(singleA);
  CompressedSparseRowFormatMatrix csraMatrix = new CompressedSparseRowFormatMatrix(sparseA);
  CompressedSparseRowFormatMatrix csrLargeaMatrix = new CompressedSparseRowFormatMatrix(largeSparseA);

  // Answers to Full matrix DGEMV's
  double [] A_times_x = {55,130,205,280,355};
  double [] alpha_times_A_times_x = {385,910,1435,1960,2485};
  double [] A_times_x_plus_y = {65,150,235,320,405};
  double [] alpha_times_A_times_x_plus_y = {395, 930, 1465, 2000, 2535};
  double [] A_times_x_plus_beta_times_y = {25,70,115,160,205};
  double [] alpha_times_A_times_x_plus_beta_times_y = {355, 850, 1345, 1840, 2335};
  double [] alpha_times_oddA_times_oddx_plus_beta_times_oddy = {180, 430, 680, 930, 1180};
  double [] alpha_times_singleA_times_singlex_plus_beta_times_singley = {2};

/**
 * Test input catchers
 */

  /* Normal 2 inputs */
@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherMatrix() {
  FullMatrix NullMat = null;
  BLAS2.dgemvInputSanityChecker(NullMat, x);
}

@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherVector() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityChecker(oddaMatrix, aVector);
}

@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherSizeWillNotCommute() {
  BLAS2.dgemvInputSanityChecker(oddaMatrix,x);
}

/* Normal 3 inputs */
@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherMatrix3inputs() {
  FullMatrix NullMat = null;
  BLAS2.dgemvInputSanityChecker(NullMat, oddx, oddy);
}

@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherVectorBadVectorGood() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityChecker(oddaMatrix, aVector, oddx);
}

@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherVectorGoodVectorBad() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityChecker(oddaMatrix, oddx, aVector);
}

@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherSizeWillNotCommuteWithIntermediteVector() {
  BLAS2.dgemvInputSanityChecker(oddaMatrix,singlex,oddy);
}

@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherSizeWillNotCommuteWithReturnVector() {
  BLAS2.dgemvInputSanityChecker(oddaMatrix,oddx,singley);
}

/* Transpose 2 inputs */
@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherMatrix() {
  FullMatrix NullMat = null;
  BLAS2.dgemvInputSanityCheckerTranspose(NullMat, x);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherVector() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityCheckerTranspose(oddaMatrix, aVector);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherSizeWillNotCommute() {
  BLAS2.dgemvInputSanityCheckerTranspose(oddaMatrix,singlex);
}

/* Transpose 3 inputs */
@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherMatrix3inputs() {
  FullMatrix NullMat = null;
  BLAS2.dgemvInputSanityCheckerTranspose(NullMat, oddx, oddy);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherVectorBadVectorGood() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityCheckerTranspose(oddaMatrix, aVector, oddx);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherVectorGoodVectorBad() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityCheckerTranspose(oddaMatrix, oddx, aVector);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherSizeWillNotCommuteWithIntermediteVector() {
  BLAS2.dgemvInputSanityCheckerTranspose(oddaMatrix,singlex,oddy);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherSizeWillNotCommuteWithReturnVector() {
  BLAS2.dgemvInputSanityCheckerTranspose(oddaMatrix,oddx,singley);
}


/**
 * DGEMV on FULL Matrices
 */
// stateless manipulators

//** group 1:: A*x OR A^T*x
@Test
public void testDGEMV_ans_eq_A_times_x() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,x),A_times_x));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x)),A_times_x));
}

@Test
public void testDGEMV_ans_eq_AT_times_x() {
  double[] tmp = {215,230,245,260,275};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix,x),tmp));
}

@Test
public void testDGEMV_ans_eq_AT_times_D1D_x() {
  double[] tmp = {215,230,245,260,275};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix, new DoubleMatrix1D(x)),tmp));
}

@Test
public void testDGEMV_ans_eq_A_times_x_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,x,BLAS2.orientation.normal),A_times_x));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),BLAS2.orientation.normal),A_times_x));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_interfaced_transposed() {
  double[] tmp = {215,230,245,260,275};
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),BLAS2.orientation.transposed),tmp));
}

//** group 2:: alpha*A*x OR alpha*A^T*x
@Test
public void testDGEMV_ans_eq_alpha_times_A_times_x() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,x),alpha_times_A_times_x));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_D1D_x() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x)),alpha_times_A_times_x));
}

@Test
public void testDGEMV_ans_eq_alpha_times_AT_times_x() {
  double[] tmp = {1505,1610,1715,1820,1925};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix,x),tmp));
}

@Test
public void testDGEMV_ans_eq_alpha_times_AT_times_D1D_x() {
  double[] tmp = {1505,1610,1715,1820,1925};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix,new DoubleMatrix1D(x)),tmp));
}

@Test
public void testDGEMV_ans_eq_alpha_A_times_x_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,x,BLAS2.orientation.normal),alpha_times_A_times_x));
}

@Test
public void testDGEMV_ans_eq_alpha_A_times_D1D_x_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x),BLAS2.orientation.normal),alpha_times_A_times_x));
}

@Test
public void testDGEMV_ans_eq_alpha_A_times_x_interfaced_transposed() {
  double[] tmp = {1505,1610,1715,1820,1925};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,x,BLAS2.orientation.transposed),tmp));
}

@Test
public void testDGEMV_ans_eq_alpha_A_times_D1D_x_interfaced_transposed() {
  double[] tmp = {1505,1610,1715,1820,1925};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x),BLAS2.orientation.transposed),tmp));
}

//** group 3:: A*x+y OR A^T*x+y
@Test
public void testDGEMV_ans_eq_A_times_x_plus_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,x,y),A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_plus_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),y),A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_plus_D1D_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),new DoubleMatrix1D(y)),A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_AT_times_x_plus_y() {
  double[] tmp = {225,250,275,300,325};
  BLAS2.dgemvTransposed(aMatrix,x,y);
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix,x,y),tmp));
}

@Test
public void testDGEMV_ans_eq_AT_times_D1D_x_plus_y() {
  double[] tmp = {225,250,275,300,325};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix,new DoubleMatrix1D(x),y),tmp));
}

@Test
public void testDGEMV_ans_eq_AT_times_D1D_x_plus_D1D_y() {
  double[] tmp = {225,250,275,300,325};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix,new DoubleMatrix1D(x),new DoubleMatrix1D(y)),tmp));
}

@Test
public void testDGEMV_ans_eq_A_times_x_plus_y_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,x,y,BLAS2.orientation.normal),A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_plus_y_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),y,BLAS2.orientation.normal),A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_plus_D1D_y_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),new DoubleMatrix1D(y),BLAS2.orientation.normal),A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_A_times_x_plus_y_interfaced_transposed() {
  double[] tmp = {225,250,275,300,325};
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,x,y,BLAS2.orientation.transposed),tmp));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_plus_y_interfaced_transposed() {
  double[] tmp = {225,250,275,300,325};
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),y,BLAS2.orientation.transposed),tmp));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_plus_D1D_y_interfaced_transposed() {
  double[] tmp = {225,250,275,300,325};
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),new DoubleMatrix1D(y),BLAS2.orientation.transposed),tmp));
}

//** group 4:: alpha*A*x+y OR alpha*A^T*x+y
@Test
public void testDGEMV_ans_eq_alpha_times_A_times_x_plus_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,x,y),alpha_times_A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_D1D_x_plus_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x),y),alpha_times_A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_D1D_x_plus_D1D_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x),new DoubleMatrix1D(y)),alpha_times_A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_alpha_times_AT_times_x_plus_y() {
  double[] tmp = {1515,1630,1745,1860,1975};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix,x,y),tmp));
}

@Test
public void testDGEMV_ans_eq_alpha_times_AT_times_D1D_x_plus_y() {
  double[] tmp = {1515,1630,1745,1860,1975};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix,new DoubleMatrix1D(x),y),tmp));
}

@Test
public void testDGEMV_ans_eq_alpha_times_AT_times_D1D_x_plus_D1D_y() {
  double[] tmp = {1515,1630,1745,1860,1975};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix,new DoubleMatrix1D(x),new DoubleMatrix1D(y)),tmp));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_x_plus_y_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,x,y,BLAS2.orientation.normal),alpha_times_A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_D1D_x_plus_y_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x),y,BLAS2.orientation.normal),alpha_times_A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_D1D_x_plus_D1D_y_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x), new DoubleMatrix1D(y),BLAS2.orientation.normal),alpha_times_A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_x_plus_y_interfaced_transposed() {
  double[] tmp = {1515,1630,1745,1860,1975};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,x,y,BLAS2.orientation.transposed),tmp));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_D1D_x_plus_y_interfaced_transposed() {
  double[] tmp = {1515,1630,1745,1860,1975};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x),y,BLAS2.orientation.transposed),tmp));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_D1D_x_plus_D1D_y_interfaced_transposed() {
  double[] tmp = {1515,1630,1745,1860,1975};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x), new DoubleMatrix1D(y),BLAS2.orientation.transposed),tmp));
}

//** group 5:: A*x+beta*y OR A^T*x+beta*y
@Test
public void testDGEMV_ans_eq_A_times_x_plus_beta_times_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,x,beta,y),A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_plus_beta_times_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),beta,y),A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_plus_beta_times_D1D_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),beta,new DoubleMatrix1D(y)),A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_AT_times_x_plus_beta_times_y() {
  double [] tmp={185,170,155,140,125};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix,x,beta,y),tmp));
}

@Test
public void testDGEMV_ans_eq_AT_times_D1D_x_plus_beta_times_y() {
  double [] tmp={185,170,155,140,125};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix,new DoubleMatrix1D(x),beta,y),tmp));
}

@Test
public void testDGEMV_ans_eq_AT_times_D1D_x_plus_beta_times_D1D_y() {
  double [] tmp={185,170,155,140,125};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix,new DoubleMatrix1D(x),beta,new DoubleMatrix1D(y)),tmp));
}

@Test
public void testDGEMV_ans_eq_A_times_x_plus_beta_times_y_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,x,beta,y,BLAS2.orientation.normal),A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_plus_beta_times_y_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),beta,y,BLAS2.orientation.normal),A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_plus_beta_times_D1D_y_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),beta,new DoubleMatrix1D(y),BLAS2.orientation.normal),A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_A_times_x_plus_beta_times_y_interfaced_transposed() {
  double [] tmp={185,170,155,140,125};
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,x,beta,y,BLAS2.orientation.transposed),tmp));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_plus_beta_times_y_interfaced_transposed() {
  double [] tmp={185,170,155,140,125};
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),beta,y,BLAS2.orientation.transposed),tmp));
}

@Test
public void testDGEMV_ans_eq_A_times_D1D_x_plus_beta_times_D1D_y_interfaced_transposed() {
  double [] tmp={185,170,155,140,125};
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,new DoubleMatrix1D(x),beta,new DoubleMatrix1D(y),BLAS2.orientation.transposed),tmp));
}

//** group 6:: alpha*A*x+beta*y OR alpha*A^T*x+beta*y
/* test some different sizes first */
@Test
public void testDGEMV_ans_eq_alpha_times_oddA_times_oddx_plus_beta_times_oddy() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,oddaMatrix,oddx,beta,oddy),alpha_times_oddA_times_oddx_plus_beta_times_oddy));
}

public void testDGEMV_ans_eq_alpha_times_singleA_times_singlex_plus_beta_times_singley() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,singleaMatrix,singlex,beta,singley),alpha_times_singleA_times_singlex_plus_beta_times_singley));
}

/* normal tests */
@Test
public void testDGEMV_ans_eq_alpha_times_A_times_x_plus_beta_times_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,x,beta,y),alpha_times_A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_D1D_x_plus_beta_times_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x),beta,y),alpha_times_A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_D1D_x_plus_beta_times_D1D_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x),beta,new DoubleMatrix1D(y)),alpha_times_A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_alpha_times_AT_times_x_plus_beta_times_y() {
  double [] tmp={1475,1550,1625,1700,1775};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix,x,beta,y),tmp));
}

@Test
public void testDGEMV_ans_eq_alpha_times_AT_times_D1D_x_plus_beta_times_y() {
  double [] tmp={1475,1550,1625,1700,1775};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix,new DoubleMatrix1D(x),beta,y),tmp));
}

@Test
public void testDGEMV_ans_eq_alpha_times_AT_times_D1D_x_plus_beta_times_D1D_y() {
  double [] tmp={1475,1550,1625,1700,1775};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix,new DoubleMatrix1D(x),beta,new DoubleMatrix1D(y)),tmp));
}

@Test
public void testDGEMV_ans_eq_alpha_A_times_x_plus_beta_times_y_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,x,beta,y,BLAS2.orientation.normal),alpha_times_A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_alpha_A_times_D1D_x_plus_beta_times_y_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x),beta,y,BLAS2.orientation.normal),alpha_times_A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_alpha_A_times_D1D_x_plus_beta_times_D1D_y_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x),beta,new DoubleMatrix1D(y),BLAS2.orientation.normal),alpha_times_A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_alpha_A_times_x_plus_beta_times_y_interfaced_transposed() {
  double [] tmp={1475,1550,1625,1700,1775};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,x,beta,y,BLAS2.orientation.transposed),tmp));
}

@Test
public void testDGEMV_ans_eq_alpha_A_times_D1D_x_plus_beta_times_y_interfaced_transposed() {
  double [] tmp={1475,1550,1625,1700,1775};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x),beta,y,BLAS2.orientation.transposed),tmp));
}

@Test
public void testDGEMV_ans_eq_alpha_A_times_D1D_x_plus_beta_times_D1D_y_interfaced_transposed() {
  double [] tmp={1475,1550,1625,1700,1775};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,new DoubleMatrix1D(x),beta,new DoubleMatrix1D(y),BLAS2.orientation.transposed),tmp));
}


// test the inplaces
@Test
public void testDGEMV_y_eq_A_times_x() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  BLAS2.dgemvInPlace(ycp,aMatrix,x);
  assertTrue(Arrays.equals(ycp,A_times_x));
}

@Test
public void testDGEMV_y_eq_A_times_D1D_x() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  BLAS2.dgemvInPlace(ycp,aMatrix,new DoubleMatrix1D(x));
  assertTrue(Arrays.equals(ycp,A_times_x));
}

@Test
public void testDGEMV_D1D_y_eq_A_times_x() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix,x);
  assertTrue(Arrays.equals(tmp.getData(),A_times_x));
}

@Test
public void testDGEMV_D1D_y_eq_A_times_D1D_x() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix,new DoubleMatrix1D(x));
  assertTrue(Arrays.equals(tmp.getData(),A_times_x));
}

@Test
public void testDGEMV_y_eq_alpha_times_A_times_x() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix,0,x);
  assertTrue(Arrays.equals(ycp,alpha_times_A_times_x));
}

@Test
public void testDGEMV_y_eq_alpha_times_A_times_D1D_x() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix,0,new DoubleMatrix1D(x));
  assertTrue(Arrays.equals(ycp,alpha_times_A_times_x));
}

@Test
public void testDGEMV_D1D_y_eq_alpha_times_A_times_x() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix,0,x);
  assertTrue(Arrays.equals(tmp.getData(),alpha_times_A_times_x));
}

@Test
public void testDGEMV_D1D_y_eq_alpha_times_A_times_D1D_x() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix,0,new DoubleMatrix1D(x));
  assertTrue(Arrays.equals(tmp.getData(),alpha_times_A_times_x));
}

@Test
public void testDGEMV_y_eq_alpha_times_A_times_x_plus_beta_times_y() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix,beta,x);
  assertTrue(Arrays.equals(ycp,alpha_times_A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_y_eq_alpha_times_A_times_D1D_x_plus_beta_times_y() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix,beta,new DoubleMatrix1D(x));
  assertTrue(Arrays.equals(ycp,alpha_times_A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_D1D_y_eq_alpha_times_A_times_x_plus_beta_times_D1D_y() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix,beta,x);
  assertTrue(Arrays.equals(tmp.getData(),alpha_times_A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_D1D_y_eq_alpha_times_A_times_D1D_x_plus_beta_times_D1D_y() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix,beta,new DoubleMatrix1D(x));
  assertTrue(Arrays.equals(tmp.getData(),alpha_times_A_times_x_plus_beta_times_y));
}


/**
 * DGEMV on CSR matrices
 */
//stateless manipulators
@Test
public void testCSRDGEMV_ans_eq_A_times_x_single() {
  double [] tmp = {5};
  double [][] m1 = {{5}};
  double [] v2 = {1};
  CompressedSparseRowFormatMatrix csr = new CompressedSparseRowFormatMatrix(m1);
  assertTrue(Arrays.equals(BLAS2.dgemv(csr,v2),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_A_times_x_small() {
  double [] tmp = {5,15,28,21};
  assertTrue(Arrays.equals(BLAS2.dgemv(csraMatrix,oddx),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_A_times_x_large() {
  double [] tmp = {5,15,28,21,156,173,70};
  assertTrue(Arrays.equals(BLAS2.dgemv(csrLargeaMatrix,largeX),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_alpha_times_A_times_x_small() {
  double [] tmp = {35,105,196,147};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,csraMatrix,oddx),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_A_times_x_plus_y() {
  double[] tmp = {15,35,58,61};
  double[] ytmp={10,20,30,40};
  assertTrue(Arrays.equals(BLAS2.dgemv(csraMatrix,oddx,ytmp),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_alpha_times_A_times_x_plus_y() {
  double[] tmp = {45,125,226,187};
  double[] ytmp={10,20,30,40};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,csraMatrix,oddx,ytmp),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_alpha_times_A_times_x_plus_beta_times_y() {
  double[] tmp = {5,45,106,27};
  double[] ytmp={10,20,30,40};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,csraMatrix,oddx,beta,ytmp),tmp));
}

//test the inplaces
@Test
public void testCSRDGEMV_y_eq_A_times_x() {
  double[] y={5,15,28,21};
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  BLAS2.dgemvInPlace(ycp,csraMatrix,oddx);
  assertTrue(Arrays.equals(ycp,y));
}

@Test
public void testCSRDGEMV_y_eq_alpha_times_A_times_x() {
  double[] y={35,105,196,147};
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  BLAS2.dgemvInPlace(ycp,alpha,csraMatrix,oddx,0);
  assertTrue(Arrays.equals(ycp,y));
}

@Test
public void testCSRDGEMV_y_eq_alpha_times_A_times_x_plus_beta_times_y() {
  double[] y={5,45,106,27};
  double[] ytmp={10,20,30,40};
  BLAS2.dgemvInPlace(ytmp,alpha,csraMatrix,oddx,beta);
  assertTrue(Arrays.equals(ytmp,y));
}

}
