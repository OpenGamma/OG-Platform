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
  double [][] A5by5 ={{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14,15},{16,17,18,19,20},{21,22,23,24,25}};
  double [][] A5by4 ={{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,16},{17,18,19,20}};
  double [][] A4by5 ={{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14,15},{16,17,18,19,20}};

  // single element data
  double [][] A1by1 = {{1}};
  double [] x1 = {2};
  double [] y1 = {10};

  // sparse
  double[][]sparseA4by4 = {{1,2,0,0},{3,0,4,0},{0,5,6,0},{0,0,7,0}};
  double[][]sparseA5by4 = {{1,2,0,0},{3,0,4,0},{0,5,6,0},{0,0,7,0},{8,0,0,9}};
  double[][]sparseA4by5 = {{1,2,0,0,0},{0,3,0,4,0},{0,5,6,0,0},{0,0,0,7,0}};
  double[][]sparseA7by7 = {{1,2,0,0,0,0,0},{3,0,4,0,0,0,0},{0,5,6,0,0,0,0},{0,0,7,0,0,0,0},{0,0,0,8,0,9,10},{0,11,0,0,12,0,13},{0,0,0,0,14,0,0}};
  double[] x7 = {1,2,3,4,5,6,7};

  // the scalings
  double alpha = 7.0;
  double beta = -3.0;

  // x and y vectors
  double [] x4 = {1,2,3,4};
  double [] x5 = {1,2,3,4,5};
  double [] y5 = {10,20,30,40,50};
  double [] y4 = {10,20,30,40};

  // casts to objects for tests
  FullMatrix aMatrix5by5 = new FullMatrix(A5by5);
  FullMatrix aMatrix5by4 = new FullMatrix(A5by4);
  FullMatrix aMatrix4by5 = new FullMatrix(A4by5);
  FullMatrix aMatrix1by1 = new FullMatrix(A1by1);
  CompressedSparseRowFormatMatrix csraMatrix4by4 = new CompressedSparseRowFormatMatrix(sparseA4by4);
  CompressedSparseRowFormatMatrix csrLargeaMatrix7by7 = new CompressedSparseRowFormatMatrix(sparseA7by7);

  // Answers to Full matrix DGEMV's
  //5x5's
  double[] tmp_A5x5_times_x5 = {55,130,205,280,355}; //A*x
  double[] tmp_A5x5T_times_x5 = {215,230,245,260,275}; //A^T*x
  double[] tmp_alpha_times_A5x5_times_x5={385,910,1435,1960,2485}; //alpha*A*x
  double[] tmp_alpha_times_A5x5T_times_x5={1505,1610,1715,1820,1925}; //alpha*A^T*x
  double[] tmp_A5x5_times_x5_plus_y5 = {65,150,235,320,405}; //A*x+y
  double[] tmp_A5x5T_times_x5_plus_y5 = {225,250,275,300,325}; //A^T*x+y
  double[] tmp_alpha_times_A5x5_times_x5_plus_y5 = {395,930,1465,2000,2535}; //alpha*A*x+y
  double[] tmp_alpha_times_A5x5T_times_x5_plus_y5 = {1515,1630,1745,1860,1975}; //alpha*A^T*x+y
  double[] tmp_A5x5_times_x5_plus_beta_times_y5 = {25,70,115,160,205}; //A*x+beta*y
  double[] tmp_A5x5T_times_x5_plus_beta_times_y5 = {185,170,155,140,125}; //A^T*x+beta*y
  double[] tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5={355,850,1345,1840,2335}; //alpha*A*x+beta*y
  double[] tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5={1475,1550,1625,1700,1775}; //alpha*A^T*x+beta*y

  //5x4's
  double[] tmp_A5x4_times_x4 = {30,70,110,150,190}; //A*x
  double[] tmp_A5x4T_times_x5 = {175,190,205,220}; //A^T*x
  double[] tmp_alpha_times_A5x4_times_x4={210,490,770,1050,1330}; //alpha*A*x
  double[] tmp_alpha_times_A5x4T_times_x5={1225,1330,1435,1540}; //alpha*A^T*x
  double[] tmp_A5x4_times_x4_plus_y5 = {40,90,140,190,240}; //A*x+y
  double[] tmp_A5x4T_times_x5_plus_y4 = {185,210,235,260}; //A^T*x+y
  double[] tmp_alpha_times_A5x4_times_x4_plus_y5 = {220,510,800,1090,1380}; //alpha*A*x+y
  double[] tmp_alpha_times_A5x4T_times_x5_plus_y4 = {1235,1350,1465,1580}; //alpha*A^T*x+y
  double[] tmp_A5x4_times_x4_plus_beta_times_y5 = {0,10,20,30,40}; //A*x+beta*y
  double[] tmp_A5x4T_times_x5_plus_beta_times_y4 = {145,130,115,100}; //A^T*x+beta*y
  double[] tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5={180,430,680,930,1180}; //alpha*A*x+beta*y
  double[] tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4={1195,1270,1345,1420}; //alpha*A^T*x+beta*y

  //4x5's
  double[] tmp_A4x5_times_x5 = {55,130,205,280}; //A*x
  double[] tmp_A4x5T_times_x4 = {110,120,130,140,150}; //A^T*x
  double[] tmp_alpha_times_A4x5_times_x5={385,910,1435,1960}; //alpha*A*x
  double[] tmp_alpha_times_A4x5T_times_x4={770,840,910,980,1050}; //alpha*A^T*x
  double[] tmp_A4x5_times_x5_plus_y4 = {65,150,235,320}; //A*x+y
  double[] tmp_A4x5T_times_x4_plus_y5 = {120,140,160,180,200}; //A^T*x+y
  double[] tmp_alpha_times_A4x5_times_x5_plus_y4 = {395,930,1465,2000}; //alpha*A*x+y
  double[] tmp_alpha_times_A4x5T_times_x4_plus_y5 = {780,860,940,1020,1100}; //alpha*A^T*x+y
  double[] tmp_A4x5_times_x5_plus_beta_times_y4 = {25,70,115,160}; //A*x+beta*y
  double[] tmp_A4x5T_times_x4_plus_beta_times_y5 = {80,60,40,20,0}; //A^T*x+beta*y
  double[] tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4={355,850,1345,1840}; //alpha*A*x+beta*y
  double[] tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5={740,780,820,860,900}; //alpha*A^T*x+beta*y

  //1x1's
  double[] tmp_A1x1_times_x1={2}; //A*x
  double[] tmp_A1x1T_times_x1={2}; //A^T*x
  double[] tmp_alpha_times_A1x1_times_x1={14}; //alpha*A*x
  double[] tmp_alpha_times_A1x1T_times_x1={14}; //alpha*A^T*x
  double[] tmp_A1x1_times_x1_plus_y1 = {12}; //A*x+y
  double[] tmp_A1x1T_times_x1_plus_y1 = {12}; //A^T*x+y
  double[] tmp_alpha_times_A1x1_times_x1_plus_y1 = {24}; //alpha*A*x+y
  double[] tmp_alpha_times_A1x1T_times_x1_plus_y1 = {24}; //alpha*A^T*x+y
  double[] tmp_A1x1_times_x1_plus_beta_times_y1 = {-28}; //A*x+beta*y
  double[] tmp_A1x1T_times_x1_plus_beta_times_y1 = {-28}; //A^T*x+beta*y
  double[] tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1={-16}; //alpha*A*x+beta*y
  double[] tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1={-16}; //alpha*A^T*x+beta*y

/**
 * Test input catchers
 */

  /* Normal 2 inputs */
@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherMatrix() {
  FullMatrix NullMat = null;
  BLAS2.dgemvInputSanityChecker(NullMat, x5);
}

@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherVector() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityChecker(aMatrix5by4, aVector);
}

@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherSizeWillNotCommute() {
  BLAS2.dgemvInputSanityChecker(aMatrix5by4,x5);
}

/* Normal 3 inputs */
@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherMatrix3inputs() {
  FullMatrix NullMat = null;
  BLAS2.dgemvInputSanityChecker(NullMat, x4, y5);
}

@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherVectorBadVectorGood() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityChecker(aMatrix5by4, aVector, x4);
}

@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherVectorGoodVectorBad() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityChecker(aMatrix5by4, x4, aVector);
}

@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherSizeWillNotCommuteWithIntermediteVector() {
  BLAS2.dgemvInputSanityChecker(aMatrix5by4,x1,y5);
}

@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherSizeWillNotCommuteWithReturnVector() {
  BLAS2.dgemvInputSanityChecker(aMatrix5by4,x4,y1);
}

/* Transpose 2 inputs */
@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherMatrix() {
  FullMatrix NullMat = null;
  BLAS2.dgemvInputSanityCheckerTranspose(NullMat, x5);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherVector() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityCheckerTranspose(aMatrix5by4, aVector);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherSizeWillNotCommute() {
  BLAS2.dgemvInputSanityCheckerTranspose(aMatrix5by4,x1);
}

/* Transpose 3 inputs */
@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherMatrix3inputs() {
  FullMatrix NullMat = null;
  BLAS2.dgemvInputSanityCheckerTranspose(NullMat, x4, y5);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherVectorBadVectorGood() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityCheckerTranspose(aMatrix5by4, aVector, x4);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherVectorGoodVectorBad() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityCheckerTranspose(aMatrix5by4, x4, aVector);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherSizeWillNotCommuteWithIntermediteVector() {
  BLAS2.dgemvInputSanityCheckerTranspose(aMatrix5by4,x1,y5);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherSizeWillNotCommuteWithReturnVector() {
  BLAS2.dgemvInputSanityCheckerTranspose(aMatrix5by4,x4,y1);
}


/**
 * DGEMV on FULL Matrices
 */
// stateless manipulators

//** group 1:: A*x OR A^T*x

// test 5x5
@Test
public void testDGEMV_ans_eq_A5x5_times_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5),tmp_A5x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5)),tmp_A5x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_A5x5T_times_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5,x5),tmp_A5x5T_times_x5));
}

@Test
public void testDGEMV_ans_eq_A5x5T_times_D1D_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, new DoubleMatrix1D(x5)),tmp_A5x5T_times_x5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_x5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,BLAS2.orientation.normal),tmp_A5x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),BLAS2.orientation.normal),tmp_A5x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_A5x5T_times_x5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,BLAS2.orientation.transposed),tmp_A5x5T_times_x5));
}

@Test
public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),BLAS2.orientation.transposed),tmp_A5x5T_times_x5));
}

// test 5x4
@Test
public void testDGEMV_ans_eq_A5x4_times_x4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x4),tmp_A5x4_times_x4));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x4)),tmp_A5x4_times_x4));
}

@Test
public void testDGEMV_ans_eq_A5x4T_times_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by4,x5),tmp_A5x4T_times_x5));
}

@Test
public void testDGEMV_ans_eq_A5x4T_times_D1D_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by4, new DoubleMatrix1D(x5)),tmp_A5x4T_times_x5));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_x4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x4,BLAS2.orientation.normal),tmp_A5x4_times_x4));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x4),BLAS2.orientation.normal),tmp_A5x4_times_x4));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_x4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x5,BLAS2.orientation.transposed),tmp_A5x4T_times_x5));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x5),BLAS2.orientation.transposed),tmp_A5x4T_times_x5));
}


//test 4x5
@Test
public void testDGEMV_ans_eq_A4x5_times_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x5),tmp_A4x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x5)),tmp_A4x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_A4x5T_times_x4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix4by5,x4),tmp_A4x5T_times_x4));
}

@Test
public void testDGEMV_ans_eq_A4x5T_times_D1D_x4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix4by5, new DoubleMatrix1D(x4)),tmp_A4x5T_times_x4));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_x5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x5,BLAS2.orientation.normal),tmp_A4x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x5),BLAS2.orientation.normal),tmp_A4x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_x5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x4,BLAS2.orientation.transposed),tmp_A4x5T_times_x4));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x4),BLAS2.orientation.transposed),tmp_A4x5T_times_x4));
}


//test 1x1
@Test
public void testDGEMV_ans_eq_A1x1_times_x1() {
assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1),tmp_A1x1_times_x1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1() {
assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1)),tmp_A1x1_times_x1));
}

@Test
public void testDGEMV_ans_eq_A1x1T_times_x1() {
assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1,x1),tmp_A1x1T_times_x1));
}

@Test
public void testDGEMV_ans_eq_A1x1T_times_D1D_x1() {
assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, new DoubleMatrix1D(x1)),tmp_A1x1T_times_x1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_x1_interfaced_normal() {
assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,BLAS2.orientation.normal),tmp_A1x1_times_x1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_interfaced_normal() {
assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),BLAS2.orientation.normal),tmp_A1x1_times_x1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_x1_interfaced_transposed() {
assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,BLAS2.orientation.transposed),tmp_A1x1T_times_x1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_interfaced_transposed() {
assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),BLAS2.orientation.transposed),tmp_A1x1T_times_x1));
}

//** group 2:: alpha*A*x OR alpha*A^T*x
//test 5x5
@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5),tmp_alpha_times_A5x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5)),tmp_alpha_times_A5x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by5,x5),tmp_alpha_times_A5x5T_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by5,new DoubleMatrix1D(x5)),tmp_alpha_times_A5x5T_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x5_times_x5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,BLAS2.orientation.normal),tmp_alpha_times_A5x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5),BLAS2.orientation.normal),tmp_alpha_times_A5x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x5_times_x5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,BLAS2.orientation.transposed),tmp_alpha_times_A5x5T_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5),BLAS2.orientation.transposed),tmp_alpha_times_A5x5T_times_x5));
}

//test 5x4
@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_x4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x4),tmp_alpha_times_A5x4_times_x4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_D1D_x4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x4)),tmp_alpha_times_A5x4_times_x4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4T_times_x4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by4,x5),tmp_alpha_times_A5x4T_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4T_times_D1D_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by4,new DoubleMatrix1D(x5)),tmp_alpha_times_A5x4T_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x4_times_x4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x4,BLAS2.orientation.normal),tmp_alpha_times_A5x4_times_x4));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x4_times_D1D_x4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x4),BLAS2.orientation.normal),tmp_alpha_times_A5x4_times_x4));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x4T_times_x5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x5,BLAS2.orientation.transposed),tmp_alpha_times_A5x4T_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x4T_times_D1D_x5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x5),BLAS2.orientation.transposed),tmp_alpha_times_A5x4T_times_x5));
}

//test 4x5
@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x5),tmp_alpha_times_A4x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_D1D_x5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x5)),tmp_alpha_times_A4x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5T_times_x4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix4by5,x4),tmp_alpha_times_A4x5T_times_x4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5T_times_D1D_x4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix4by5,new DoubleMatrix1D(x4)),tmp_alpha_times_A4x5T_times_x4));
}

@Test
public void testDGEMV_ans_eq_alpha_A4x5_times_x5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x5,BLAS2.orientation.normal),tmp_alpha_times_A4x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_A4x5_times_D1D_x5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x5),BLAS2.orientation.normal),tmp_alpha_times_A4x5_times_x5));
}

@Test
public void testDGEMV_ans_eq_alpha_A4x5T_times_x4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x4,BLAS2.orientation.transposed),tmp_alpha_times_A4x5T_times_x4));
}

@Test
public void testDGEMV_ans_eq_alpha_A4x5T_times_D1D_x4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x4),BLAS2.orientation.transposed),tmp_alpha_times_A4x5T_times_x4));
}

//test 1x1
@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1() {
assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1),tmp_alpha_times_A1x1_times_x1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1() {
assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1)),tmp_alpha_times_A1x1_times_x1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1() {
assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix1by1,x1),tmp_alpha_times_A1x1T_times_x1));
}

@Test
public void testDGEMV_ans_alpha_times_eq_A1x1T_times_D1D_x1() {
assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix1by1, new DoubleMatrix1D(x1)),tmp_alpha_times_A1x1T_times_x1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_interfaced_normal() {
assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,BLAS2.orientation.normal),tmp_alpha_times_A1x1_times_x1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_interfaced_normal() {
assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1),BLAS2.orientation.normal),tmp_alpha_times_A1x1_times_x1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_interfaced_transposed() {
assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,BLAS2.orientation.transposed),tmp_alpha_times_A1x1T_times_x1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_interfaced_transposed() {
assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1),BLAS2.orientation.transposed),tmp_alpha_times_A1x1T_times_x1));
}

//** group 3:: A*x+y OR A^T*x+y

//test 5x5
@Test
public void testDGEMV_ans_eq_A5x5_times_x5_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,y5),tmp_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),y5),tmp_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),new DoubleMatrix1D(y5)),tmp_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5T_times_x5_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5,x5,y5),tmp_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5,new DoubleMatrix1D(x5),y5),tmp_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5,new DoubleMatrix1D(x5),new DoubleMatrix1D(y5)),tmp_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_x5_plus_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,y5,BLAS2.orientation.normal),tmp_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),y5,BLAS2.orientation.normal),tmp_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_x5_plus_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,y5,BLAS2.orientation.transposed),tmp_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),y5,BLAS2.orientation.transposed),tmp_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_A5x5T_times_x5_plus_y5));
}

//test 5x4
@Test
public void testDGEMV_ans_eq_A5x4_times_x4_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x4,y5),tmp_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x4_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x4),y5),tmp_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x4_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x4),new DoubleMatrix1D(y5)),tmp_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x4T_times_x5_plus_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by4,x5,y4),tmp_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_A5x4T_times_D1D_x5_plus_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by4,new DoubleMatrix1D(x5),y4),tmp_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_A5x4T_times_D1D_x5_plus_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by4,new DoubleMatrix1D(x5),new DoubleMatrix1D(y4)),tmp_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_x4_plus_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x4,y5,BLAS2.orientation.normal),tmp_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x4_plus_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x4),y5,BLAS2.orientation.normal),tmp_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x4_plus_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x4),new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_x5_plus_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x5,y4,BLAS2.orientation.transposed),tmp_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x5_plus_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x5),y4,BLAS2.orientation.transposed),tmp_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x5_plus_D1D_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x5),new DoubleMatrix1D(y4),BLAS2.orientation.transposed),tmp_A5x4T_times_x5_plus_y4));
}

//test 4x5
@Test
public void testDGEMV_ans_eq_A4x5_times_x5_plus_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x5,y4),tmp_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x5_plus_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x5),y4),tmp_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x5_plus_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x5),new DoubleMatrix1D(y4)),tmp_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_A4x5T_times_x4_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix4by5,x4,y5),tmp_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A4x5T_times_D1D_x4_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix4by5,new DoubleMatrix1D(x4),y5),tmp_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A4x5T_times_D1D_x4_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix4by5,new DoubleMatrix1D(x4),new DoubleMatrix1D(y5)),tmp_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_x5_plus_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x5,y4,BLAS2.orientation.normal),tmp_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x5_plus_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x5),y4,BLAS2.orientation.normal),tmp_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x5_plus_D1D_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x5),new DoubleMatrix1D(y4),BLAS2.orientation.normal),tmp_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_x4_plus_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x4,y5,BLAS2.orientation.transposed),tmp_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x4_plus_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x4),y5,BLAS2.orientation.transposed),tmp_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x4_plus_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x4),new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_A4x5T_times_x4_plus_y5));
}

//test 1x1
@Test
public void testDGEMV_ans_eq_A1x1_times_x1_plus_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,y1),tmp_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),y1),tmp_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),new DoubleMatrix1D(y1)),tmp_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1T_times_x1_plus_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1,x1,y1),tmp_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1T_times_D1D_x1_plus_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1,new DoubleMatrix1D(x1),y1),tmp_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1T_times_D1D_x1_plus_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1,new DoubleMatrix1D(x1),new DoubleMatrix1D(y1)),tmp_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_x1_plus_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,y1,BLAS2.orientation.normal),tmp_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),y1,BLAS2.orientation.normal),tmp_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),new DoubleMatrix1D(y1),BLAS2.orientation.normal),tmp_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_x1_plus_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,y1,BLAS2.orientation.transposed),tmp_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),y1,BLAS2.orientation.transposed),tmp_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),new DoubleMatrix1D(y1),BLAS2.orientation.transposed),tmp_A1x1T_times_x1_plus_y1));
}

//** group 4:: alpha*A*x+y OR alpha*A^T*x+y
//5x5
@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,y5),tmp_alpha_times_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5),y5),tmp_alpha_times_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5),new DoubleMatrix1D(y5)),tmp_alpha_times_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by5,x5,y5),tmp_alpha_times_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by5,new DoubleMatrix1D(x5),y5),tmp_alpha_times_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by5,new DoubleMatrix1D(x5),new DoubleMatrix1D(y5)),tmp_alpha_times_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,y5,BLAS2.orientation.normal),tmp_alpha_times_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5),y5,BLAS2.orientation.normal),tmp_alpha_times_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5), new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_alpha_times_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,y5,BLAS2.orientation.transposed),tmp_alpha_times_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5),y5,BLAS2.orientation.transposed),tmp_alpha_times_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5), new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_alpha_times_A5x5T_times_x5_plus_y5));
}

//5x4
@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_x4_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x4,y5),tmp_alpha_times_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_D1D_x4_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x4),y5),tmp_alpha_times_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_D1D_x4_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x4),new DoubleMatrix1D(y5)),tmp_alpha_times_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4T_times_x5_plus_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by4,x5,y4),tmp_alpha_times_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4T_times_D1D_x5_plus_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by4,new DoubleMatrix1D(x5),y4),tmp_alpha_times_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4T_times_D1D_x5_plus_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by4,new DoubleMatrix1D(x5),new DoubleMatrix1D(y4)),tmp_alpha_times_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_x4_plus_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x4,y5,BLAS2.orientation.normal),tmp_alpha_times_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_D1D_x4_plus_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x4),y5,BLAS2.orientation.normal),tmp_alpha_times_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_D1D_x4_plus_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x4), new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_alpha_times_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_x5_plus_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x5,y4,BLAS2.orientation.transposed),tmp_alpha_times_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_D1D_x5_plus_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x5),y4,BLAS2.orientation.transposed),tmp_alpha_times_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_D1D_x5_plus_D1D_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x5), new DoubleMatrix1D(y4),BLAS2.orientation.transposed),tmp_alpha_times_A5x4T_times_x5_plus_y4));
}


//4x5
@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_x5_plus_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x5,y4),tmp_alpha_times_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_D1D_x5_plus_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x5),y4),tmp_alpha_times_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_D1D_x5_plus_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x5),new DoubleMatrix1D(y4)),tmp_alpha_times_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5T_times_x4_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix4by5,x4,y5),tmp_alpha_times_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5T_times_D1D_x4_plus_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix4by5,new DoubleMatrix1D(x4),y5),tmp_alpha_times_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5T_times_D1D_x4_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix4by5,new DoubleMatrix1D(x4),new DoubleMatrix1D(y5)),tmp_alpha_times_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_x5_plus_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x5,y4,BLAS2.orientation.normal),tmp_alpha_times_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_D1D_x5_plus_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x5),y4,BLAS2.orientation.normal),tmp_alpha_times_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_D1D_x5_plus_D1D_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x5), new DoubleMatrix1D(y4),BLAS2.orientation.normal),tmp_alpha_times_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_x4_plus_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x4,y5,BLAS2.orientation.transposed),tmp_alpha_times_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_D1D_x4_plus_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x4),y5,BLAS2.orientation.transposed),tmp_alpha_times_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_D1D_x4_plus_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x4), new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_alpha_times_A4x5T_times_x4_plus_y5));
}

//1x1
@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,y1),tmp_alpha_times_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1),y1),tmp_alpha_times_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1),new DoubleMatrix1D(y1)),tmp_alpha_times_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1_plus_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix1by1,x1,y1),tmp_alpha_times_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1T_times_D1D_x1_plus_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix1by1,new DoubleMatrix1D(x1),y1),tmp_alpha_times_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1T_times_D1D_x1_plus_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix1by1,new DoubleMatrix1D(x1),new DoubleMatrix1D(y1)),tmp_alpha_times_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,y1,BLAS2.orientation.normal),tmp_alpha_times_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1),y1,BLAS2.orientation.normal),tmp_alpha_times_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1), new DoubleMatrix1D(y1),BLAS2.orientation.normal),tmp_alpha_times_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,y1,BLAS2.orientation.transposed),tmp_alpha_times_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1),y1,BLAS2.orientation.transposed),tmp_alpha_times_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1), new DoubleMatrix1D(y1),BLAS2.orientation.transposed),tmp_alpha_times_A1x1T_times_x1_plus_y1));
}

//** group 5:: A*x+beta*y OR A^T*x+beta*y
//5x5
@Test
public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,beta,y5),tmp_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),beta,y5),tmp_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y5)),tmp_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5T_times_x5_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5,x5,beta,y5),tmp_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5,new DoubleMatrix1D(x5),beta,y5),tmp_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y5)),tmp_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,beta,y5,BLAS2.orientation.normal),tmp_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),beta,y5,BLAS2.orientation.normal),tmp_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,beta,y5,BLAS2.orientation.transposed),tmp_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),beta,y5,BLAS2.orientation.transposed),tmp_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_A5x5T_times_x5_plus_beta_times_y5));
}

//5x4
@Test
public void testDGEMV_ans_eq_A5x4_times_x4_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x4,beta,y5),tmp_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x4_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x4),beta,y5),tmp_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x4_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x4),beta,new DoubleMatrix1D(y5)),tmp_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x4T_times_x5_plus_beta_times_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by4,x5,beta,y4),tmp_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_A5x4T_times_D1D_x5_plus_beta_times_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by4,new DoubleMatrix1D(x5),beta,y4),tmp_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_A5x4T_times_D1D_x5_plus_beta_times_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by4,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y4)),tmp_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_x4_plus_beta_times_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x4,beta,y5,BLAS2.orientation.normal),tmp_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x4_plus_beta_times_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x4),beta,y5,BLAS2.orientation.normal),tmp_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x4_plus_beta_times_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x4),beta,new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_x5_plus_beta_times_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x5,beta,y4,BLAS2.orientation.transposed),tmp_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x5_plus_beta_times_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x5),beta,y4,BLAS2.orientation.transposed),tmp_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_A5x4_times_D1D_x5_plus_beta_times_D1D_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y4),BLAS2.orientation.transposed),tmp_A5x4T_times_x5_plus_beta_times_y4));
}

//4x5
@Test
public void testDGEMV_ans_eq_A4x5_times_x5_plus_beta_times_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x5,beta,y4),tmp_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x5_plus_beta_times_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x5),beta,y4),tmp_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x5_plus_beta_times_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y4)),tmp_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_A4x5T_times_x4_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix4by5,x4,beta,y5),tmp_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A4x5T_times_D1D_x4_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix4by5,new DoubleMatrix1D(x4),beta,y5),tmp_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A4x5T_times_D1D_x4_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix4by5,new DoubleMatrix1D(x4),beta,new DoubleMatrix1D(y5)),tmp_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_x5_plus_beta_times_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x5,beta,y4,BLAS2.orientation.normal),tmp_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x5_plus_beta_times_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x5),beta,y4,BLAS2.orientation.normal),tmp_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x5_plus_beta_times_D1D_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y4),BLAS2.orientation.normal),tmp_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_x4_plus_beta_times_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x4,beta,y5,BLAS2.orientation.transposed),tmp_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x4_plus_beta_times_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x4),beta,y5,BLAS2.orientation.transposed),tmp_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_A4x5_times_D1D_x4_plus_beta_times_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,new DoubleMatrix1D(x4),beta,new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_A4x5T_times_x4_plus_beta_times_y5));
}

//1x1
@Test
public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,beta,y1),tmp_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),beta,y1),tmp_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),beta,new DoubleMatrix1D(y1)),tmp_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1T_times_x1_plus_beta_times_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1,x1,beta,y1),tmp_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1T_times_D1D_x1_plus_beta_times_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1,new DoubleMatrix1D(x1),beta,y1),tmp_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1T_times_D1D_x1_plus_beta_times_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1,new DoubleMatrix1D(x1),beta,new DoubleMatrix1D(y1)),tmp_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,beta,y1,BLAS2.orientation.normal),tmp_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),beta,y1,BLAS2.orientation.normal),tmp_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),beta,new DoubleMatrix1D(y1),BLAS2.orientation.normal),tmp_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,beta,y1,BLAS2.orientation.transposed),tmp_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),beta,y1,BLAS2.orientation.transposed),tmp_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),beta,new DoubleMatrix1D(y1),BLAS2.orientation.transposed),tmp_A1x1T_times_x1_plus_beta_times_y1));
}


//** group 6:: alpha*A*x+beta*y OR alpha*A^T*x+beta*y

//5x5
@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,beta,y5),tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5),beta,y5),tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y5)),tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by5,x5,beta,y5),tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by5,new DoubleMatrix1D(x5),beta,y5),tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by5,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y5)),tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x5_times_x5_plus_beta_times_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,beta,y5,BLAS2.orientation.normal),tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_plus_beta_times_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5),beta,y5,BLAS2.orientation.normal),tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_plus_beta_times_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x5_times_x5_plus_beta_times_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,beta,y5,BLAS2.orientation.transposed),tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_plus_beta_times_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5),beta,y5,BLAS2.orientation.transposed),tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_plus_beta_times_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
}


//5x4
@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_x4_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x4,beta,y5),tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_D1D_x4_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x4),beta,y5),tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_D1D_x4_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x4),beta,new DoubleMatrix1D(y5)),tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4T_times_x5_plus_beta_times_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by4,x5,beta,y4),tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4T_times_D1D_x5_plus_beta_times_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by4,new DoubleMatrix1D(x5),beta,y4),tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4T_times_D1D_x5_plus_beta_times_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by4,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y4)),tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_x4_plus_beta_times_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x4,beta,y5,BLAS2.orientation.normal),tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_D1D_x4_plus_beta_times_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x4),beta,y5,BLAS2.orientation.normal),tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_D1D_x4_plus_beta_times_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x4),beta,new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_x5_plus_beta_times_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x5,beta,y4,BLAS2.orientation.transposed),tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_D1D_x5_plus_beta_times_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x5),beta,y4,BLAS2.orientation.transposed),tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A5x4_times_D1D_x5_plus_beta_times_D1D_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y4),BLAS2.orientation.transposed),tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4));
}

//4x5
@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_x5_plus_beta_times_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x5,beta,y4),tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_D1D_x5_plus_beta_times_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x5),beta,y4),tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_D1D_x5_plus_beta_times_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y4)),tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5T_times_x4_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix4by5,x4,beta,y5),tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5T_times_D1D_x4_plus_beta_times_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix4by5,new DoubleMatrix1D(x4),beta,y5),tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5T_times_D1D_x4_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix4by5,new DoubleMatrix1D(x4),beta,new DoubleMatrix1D(y5)),tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_x5_plus_beta_times_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x5,beta,y4,BLAS2.orientation.normal),tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_D1D_x5_plus_beta_times_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x5),beta,y4,BLAS2.orientation.normal),tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_D1D_x5_plus_beta_times_D1D_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x5),beta,new DoubleMatrix1D(y4),BLAS2.orientation.normal),tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_x4_plus_beta_times_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x4,beta,y5,BLAS2.orientation.transposed),tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_D1D_x4_plus_beta_times_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x4),beta,y5,BLAS2.orientation.transposed),tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A4x5_times_D1D_x4_plus_beta_times_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,new DoubleMatrix1D(x4),beta,new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5));
}


//1x1
@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,beta,y1),tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1),beta,y1),tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1),beta,new DoubleMatrix1D(y1)),tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1_plus_beta_times_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix1by1,x1,beta,y1),tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1T_times_D1D_x1_plus_beta_times_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix1by1,new DoubleMatrix1D(x1),beta,y1),tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1T_times_D1D_x1_plus_beta_times_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix1by1,new DoubleMatrix1D(x1),beta,new DoubleMatrix1D(y1)),tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,beta,y1,BLAS2.orientation.normal),tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1),beta,y1,BLAS2.orientation.normal),tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1),beta,new DoubleMatrix1D(y1),BLAS2.orientation.normal),tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,beta,y1,BLAS2.orientation.transposed),tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1),beta,y1,BLAS2.orientation.transposed),tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1),beta,new DoubleMatrix1D(y1),BLAS2.orientation.transposed),tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
}



// test the inplaces
double [] A_times_x={55,130,205,280,355};
@Test
public void testDGEMV_y_eq_A_times_x() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by5,x5);
  assertTrue(Arrays.equals(ycp,A_times_x));
}

@Test
public void testDGEMV_y_eq_A_times_D1D_x() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by5,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,A_times_x));
}

@Test
public void testDGEMV_D1D_y_eq_A_times_x() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix5by5,x5);
  assertTrue(Arrays.equals(tmp.getData(),A_times_x));
}

@Test
public void testDGEMV_D1D_y_eq_A_times_D1D_x() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix5by5,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),A_times_x));
}

double[] alpha_times_A_times_x = {385,910,1435,1960,2485};
@Test
public void testDGEMV_y_eq_alpha_times_A_times_x() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by5,0,x5);
  assertTrue(Arrays.equals(ycp,alpha_times_A_times_x));
}

@Test
public void testDGEMV_y_eq_alpha_times_A_times_D1D_x() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by5,0,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,alpha_times_A_times_x));
}

@Test
public void testDGEMV_D1D_y_eq_alpha_times_A_times_x() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by5,0,x5);
  assertTrue(Arrays.equals(tmp.getData(),alpha_times_A_times_x));
}

@Test
public void testDGEMV_D1D_y_eq_alpha_times_A_times_D1D_x() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by5,0,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),alpha_times_A_times_x));
}

@Test
public void testDGEMV_y_eq_alpha_times_A_times_x_plus_beta_times_y() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by5,beta,x5);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_y_eq_alpha_times_A_times_D1D_x_plus_beta_times_y() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by5,beta,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y_eq_alpha_times_A_times_x_plus_beta_times_D1D_y() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by5,beta,x5);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y_eq_alpha_times_A_times_D1D_x_plus_beta_times_D1D_y() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by5,beta,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}


/**
 * DGEMV on CSR matrices
 */
//stateless manipulators

/* GROUP1:: A*x OR A^T*x */
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
  assertTrue(Arrays.equals(BLAS2.dgemv(csraMatrix4by4,x4),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_A_times_x_large() {
  double [] tmp = {5,15,28,21,156,173,70};
  assertTrue(Arrays.equals(BLAS2.dgemv(csrLargeaMatrix7by7,x7),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_A_times_D1D_x_large() {
  double [] tmp = {5,15,28,21,156,173,70};
  assertTrue(Arrays.equals(BLAS2.dgemv(csrLargeaMatrix7by7,new DoubleMatrix1D(x7)),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_AT_times_x_large() {
  double [] tmp = {7,83,54,40,170,45,128};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(csrLargeaMatrix7by7,x7),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_AT_times_D1D_x_large() {
  double [] tmp = {7,83,54,40,170,45,128};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(csrLargeaMatrix7by7,new DoubleMatrix1D(x7)),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_A_times_x_interfaced_normal() {
  double [] tmp = {5,15,28,21,156,173,70};
  assertTrue(Arrays.equals(BLAS2.dgemv(csrLargeaMatrix7by7,x7,BLAS2.orientation.normal),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_A_times_D1D_x_interfaced_normal() {
  double [] tmp = {5,15,28,21,156,173,70};
  assertTrue(Arrays.equals(BLAS2.dgemv(csrLargeaMatrix7by7,new DoubleMatrix1D(x7),BLAS2.orientation.normal),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_A_times_x_interfaced_transposed() {
  double [] tmp = {7,83,54,40,170,45,128};
  assertTrue(Arrays.equals(BLAS2.dgemv(csrLargeaMatrix7by7,x7,BLAS2.orientation.transposed),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_A_times_D1D_x_interfaced_transposed() {
  double [] tmp = {7,83,54,40,170,45,128};
  assertTrue(Arrays.equals(BLAS2.dgemv(csrLargeaMatrix7by7,new DoubleMatrix1D(x7),BLAS2.orientation.transposed),tmp));
}

/* GROUP2:: alpha*A*x OR alpha*A^T*x */

@Test
public void testCSRDGEMV_ans_eq_alpha_times_A_times_x() {
  double [] tmp = {35,105,196,147,1092,1211,490};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,csrLargeaMatrix7by7,x7),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_alpha_times_A_times_D1D_x() {
  double [] tmp = {35,105,196,147,1092,1211,490};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,csrLargeaMatrix7by7,new DoubleMatrix1D(x7)),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_alpha_times_AT_times_x() {
  double [] tmp = {49,581,378,280,1190,315,896};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,csrLargeaMatrix7by7,x7),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_alpha_times_AT_times_D1D_x() {
  double [] tmp = {49,581,378,280,1190,315,896};
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,csrLargeaMatrix7by7,new DoubleMatrix1D(x7)),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_alpha_times_A_times_x_interfaced_normal() {
  double [] tmp = {35,105,196,147,1092,1211,490};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,csrLargeaMatrix7by7,x7,BLAS2.orientation.normal),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_alpha_times_A_times_D1D_x_interfaced_normal() {
  double [] tmp = {35,105,196,147,1092,1211,490};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,csrLargeaMatrix7by7,new DoubleMatrix1D(x7),BLAS2.orientation.normal),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_alpha_times_A_times_x_interfaced_transposed() {
  double [] tmp = {49,581,378,280,1190,315,896};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,csrLargeaMatrix7by7,x7,BLAS2.orientation.transposed),tmp));
}

@Test
public void testCSRDGEMV_ans_eq_alpha_times_A_times_D1D_x_interfaced_transposed() {
  double [] tmp = {49,581,378,280,1190,315,896};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,csrLargeaMatrix7by7,new DoubleMatrix1D(x7),BLAS2.orientation.transposed),tmp));
}

/* GROUP3:: A*x + y5 or A^T*x + y5 */
@Test
public void testCSRDGEMV_ans_eq_A_times_x_plus_y() {
  double[] tmp = {15,35,58,61};
  double[] ytmp={10,20,30,40};
  assertTrue(Arrays.equals(BLAS2.dgemv(csraMatrix4by4,x4,ytmp),tmp));
}

/* GROUP4:: alpha*A*x + y5 or alpha*A^T*x + y5 */
@Test
public void testCSRDGEMV_ans_eq_alpha_times_A_times_x_plus_y() {
  double[] tmp = {45,125,226,187};
  double[] ytmp={10,20,30,40};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,csraMatrix4by4,x4,ytmp),tmp));
}

/* GROUP5:: A*x + beta*y5 or A^T*x + beta*y5 */


/* GROUP6:: alpha*A*x + beta*y5 or alpha*A^T*x + beta*y5 */
@Test
public void testCSRDGEMV_ans_eq_alpha_times_A_times_x_plus_beta_times_y() {
  double[] tmp = {5,45,106,27};
  double[] ytmp={10,20,30,40};
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,csraMatrix4by4,x4,beta,ytmp),tmp));
}

//test the inplaces
@Test
public void testCSRDGEMV_y_eq_A_times_x() {
  double[] y5={5,15,28,21};
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,csraMatrix4by4,x4);
  assertTrue(Arrays.equals(ycp,y5));
}

@Test
public void testCSRDGEMV_y_eq_alpha_times_A_times_x() {
  double[] y5={35,105,196,147};
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,alpha,csraMatrix4by4,x4,0);
  assertTrue(Arrays.equals(ycp,y5));
}

@Test
public void testCSRDGEMV_y_eq_alpha_times_A_times_x_plus_beta_times_y() {
  double[] y5={5,45,106,27};
  double[] ytmp={10,20,30,40};
  BLAS2.dgemvInPlace(ytmp,alpha,csraMatrix4by4,x4,beta);
  assertTrue(Arrays.equals(ytmp,y5));
}

}
