/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.blas;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.SparseCoordinateFormatMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.BLAS2;

/**
 *
 */
public class BLAS2DGEMVCOOMatrixTest {

  // dense
  final double [][] A5by5 ={{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14,15},{16,17,18,19,20},{21,22,23,24,25}};
  final double [][] A5by4 ={{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,16},{17,18,19,20}};
  final double [][] A4by5 ={{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14,15},{16,17,18,19,20}};
  final double [][] A19by17 =
  {{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17},
   {18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34},
   {35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51},
   {52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68},
   {69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85},
   {86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102},
   {103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119},
   {120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136},
   {137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153},
   {154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170},
   {171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187},
   {188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204},
   {205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221},
   {222,223,224,225,226,227,228,229,230,231,232,233,234,235,236,237,238},
   {239,240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255},
   {256,257,258,259,260,261,262,263,264,265,266,267,268,269,270,271,272},
   {273,274,275,276,277,278,279,280,281,282,283,284,285,286,287,288,289},
   {290,291,292,293,294,295,296,297,298,299,300,301,302,303,304,305,306},
   {307,308,309,310,311,312,313,314,315,316,317,318,319,320,321,322,323}
  };

  // single element data
  final double [][] A1by1 = {{1}};
  final double [] x1 = {2};
  final double [] y1 = {10};

  // the scalings
  final double alpha = 7.0;
  final double beta = -3.0;

  // x and y vectors
  final double [] x4 = {1,2,3,4};
  final double [] x5 = {1,2,3,4,5};
  final double [] y5 = {10,20,30,40,50};
  final double [] y4 = {10,20,30,40};
  final double [] x17 = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
  final double [] x19 = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};
  final double [] y17 = {10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170};
  final double [] y19 = {10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170,180,190};

  // casts to objects for tests
  final SparseCoordinateFormatMatrix aMatrix5by5 = new SparseCoordinateFormatMatrix(A5by5);
  final SparseCoordinateFormatMatrix aMatrix5by4 = new SparseCoordinateFormatMatrix(A5by4);
  final SparseCoordinateFormatMatrix aMatrix4by5 = new SparseCoordinateFormatMatrix(A4by5);
  final SparseCoordinateFormatMatrix aMatrix1by1 = new SparseCoordinateFormatMatrix(A1by1);
  final SparseCoordinateFormatMatrix aMatrix19by17 = new SparseCoordinateFormatMatrix(A19by17);

  // Answers to Full matrix DGEMV's
  //5x5's
  final double[] tmp_A5x5_times_x5 = {55,130,205,280,355}; //A*x
  final double[] tmp_A5x5T_times_x5 = {215,230,245,260,275}; //A^T*x
  final double[] tmp_alpha_times_A5x5_times_x5={385,910,1435,1960,2485}; //alpha*A*x
  final double[] tmp_alpha_times_A5x5T_times_x5={1505,1610,1715,1820,1925}; //alpha*A^T*x
  final double[] tmp_A5x5_times_x5_plus_y5 = {65,150,235,320,405}; //A*x+y
  final double[] tmp_A5x5T_times_x5_plus_y5 = {225,250,275,300,325}; //A^T*x+y
  final double[] tmp_alpha_times_A5x5_times_x5_plus_y5 = {395,930,1465,2000,2535}; //alpha*A*x+y
  final double[] tmp_alpha_times_A5x5T_times_x5_plus_y5 = {1515,1630,1745,1860,1975}; //alpha*A^T*x+y
  final double[] tmp_A5x5_times_x5_plus_beta_times_y5 = {25,70,115,160,205}; //A*x+beta*y
  final double[] tmp_A5x5T_times_x5_plus_beta_times_y5 = {185,170,155,140,125}; //A^T*x+beta*y
  final double[] tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5={355,850,1345,1840,2335}; //alpha*A*x+beta*y
  final double[] tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5={1475,1550,1625,1700,1775}; //alpha*A^T*x+beta*y

  //5x4's
  final double[] tmp_A5x4_times_x4 = {30,70,110,150,190}; //A*x
  final double[] tmp_A5x4T_times_x5 = {175,190,205,220}; //A^T*x
  final double[] tmp_alpha_times_A5x4_times_x4={210,490,770,1050,1330}; //alpha*A*x
  final double[] tmp_alpha_times_A5x4T_times_x5={1225,1330,1435,1540}; //alpha*A^T*x
  final double[] tmp_A5x4_times_x4_plus_y5 = {40,90,140,190,240}; //A*x+y
  final double[] tmp_A5x4T_times_x5_plus_y4 = {185,210,235,260}; //A^T*x+y
  final double[] tmp_alpha_times_A5x4_times_x4_plus_y5 = {220,510,800,1090,1380}; //alpha*A*x+y
  final double[] tmp_alpha_times_A5x4T_times_x5_plus_y4 = {1235,1350,1465,1580}; //alpha*A^T*x+y
  final double[] tmp_A5x4_times_x4_plus_beta_times_y5 = {0,10,20,30,40}; //A*x+beta*y
  final double[] tmp_A5x4T_times_x5_plus_beta_times_y4 = {145,130,115,100}; //A^T*x+beta*y
  final double[] tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5={180,430,680,930,1180}; //alpha*A*x+beta*y
  final double[] tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4={1195,1270,1345,1420}; //alpha*A^T*x+beta*y

  //4x5's
  final double[] tmp_A4x5_times_x5 = {55,130,205,280}; //A*x
  final double[] tmp_A4x5T_times_x4 = {110,120,130,140,150}; //A^T*x
  final double[] tmp_alpha_times_A4x5_times_x5={385,910,1435,1960}; //alpha*A*x
  final double[] tmp_alpha_times_A4x5T_times_x4={770,840,910,980,1050}; //alpha*A^T*x
  final double[] tmp_A4x5_times_x5_plus_y4 = {65,150,235,320}; //A*x+y
  final double[] tmp_A4x5T_times_x4_plus_y5 = {120,140,160,180,200}; //A^T*x+y
  final double[] tmp_alpha_times_A4x5_times_x5_plus_y4 = {395,930,1465,2000}; //alpha*A*x+y
  final double[] tmp_alpha_times_A4x5T_times_x4_plus_y5 = {780,860,940,1020,1100}; //alpha*A^T*x+y
  final double[] tmp_A4x5_times_x5_plus_beta_times_y4 = {25,70,115,160}; //A*x+beta*y
  final double[] tmp_A4x5T_times_x4_plus_beta_times_y5 = {80,60,40,20,0}; //A^T*x+beta*y
  final double[] tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4={355,850,1345,1840}; //alpha*A*x+beta*y
  final double[] tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5={740,780,820,860,900}; //alpha*A^T*x+beta*y

  //1x1's
  final double[] tmp_A1x1_times_x1={2}; //A*x
  final double[] tmp_A1x1T_times_x1={2}; //A^T*x
  final double[] tmp_alpha_times_A1x1_times_x1={14}; //alpha*A*x
  final double[] tmp_alpha_times_A1x1T_times_x1={14}; //alpha*A^T*x
  final double[] tmp_A1x1_times_x1_plus_y1 = {12}; //A*x+y
  final double[] tmp_A1x1T_times_x1_plus_y1 = {12}; //A^T*x+y
  final double[] tmp_alpha_times_A1x1_times_x1_plus_y1 = {24}; //alpha*A*x+y
  final double[] tmp_alpha_times_A1x1T_times_x1_plus_y1 = {24}; //alpha*A^T*x+y
  final double[] tmp_A1x1_times_x1_plus_beta_times_y1 = {-28}; //A*x+beta*y
  final double[] tmp_A1x1T_times_x1_plus_beta_times_y1 = {-28}; //A^T*x+beta*y
  final double[] tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1={-16}; //alpha*A*x+beta*y
  final double[] tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1={-16}; //alpha*A^T*x+beta*y


  //19x17's
  final double[] tmp_A19x17_times_x17={1785,4386,6987,9588,12189,14790,17391,19992,22593,25194,27795,30396,32997,35598,38199,40800,43401,46002,48603}; //A*x
  final double[] tmp_A19x17T_times_x19={38950,39140,39330,39520,39710,39900,40090,40280,40470,40660,40850,41040,41230,41420,41610,41800,41990}; //A^T*x
  final double[] tmp_alpha_times_A19x17_times_x17={12495,30702,48909,67116,85323,103530,121737,139944,158151,176358,194565,212772,230979,249186,267393,285600,303807,322014,340221}; //alpha*A*x
  final double[] tmp_alpha_times_A19x17T_times_x19={272650,273980,275310,276640,277970,279300,280630,281960,283290,284620,285950,287280,288610,289940,291270,292600,293930}; //alpha*A^T*x
  final double[] tmp_A19x17_times_x17_plus_y19 = {1795,4406,7017,9628,12239,14850,17461,20072,22683,25294,27905,30516,33127,35738,38349,40960,43571,46182,48793}; //A*x+y
  final double[] tmp_A19x17T_times_x19_plus_y17 = {38960,39160,39360,39560,39760,39960,40160,40360,40560,40760,40960,41160,41360,41560,41760,41960,42160}; //A^T*x+y
  final double[] tmp_alpha_times_A19x17_times_x17_plus_y19 = {12505,30722,48939,67156,85373,103590,121807,140024,158241,176458,194675,212892,231109,249326,267543,285760,303977,322194,340411}; //alpha*A*x+y
  final double[] tmp_alpha_times_A19x17T_times_x19_plus_y17 = {272660,274000,275340,276680,278020,279360,280700,282040,283380,284720,286060,287400,288740,290080,291420,292760,294100}; //alpha*A^T*x+y
  final double[] tmp_A19x17_times_x17_plus_beta_times_y19 = {1755,4326,6897,9468,12039,14610,17181,19752,22323,24894,27465,30036,32607,35178,37749,40320,42891,45462,48033}; //A*x+beta*y
  final double[] tmp_A19x17T_times_x19_plus_beta_times_y17 = {38920,39080,39240,39400,39560,39720,39880,40040,40200,40360,40520,40680,40840,41000,41160,41320,41480}; //A^T*x+beta*y
  final double[] tmp_alpha_times_A19x17_times_x17_plus_beta_times_y19={12465,30642,48819,66996,85173,103350,121527,139704,157881,176058,194235,212412,230589,248766,266943,285120,303297,321474,339651}; //alpha*A*x+beta*y
  final double[] tmp_alpha_times_A19x17T_times_x19_plus_beta_times_y17={272620,273920,275220,276520,277820,279120,280420,281720,283020,284320,285620,286920,288220,289520,290820,292120,293420}; //alpha*A^T*x+beta*y

/**
 * Test input catchers
 */

  /* Normal 2 inputs */
@Test(expectedExceptions = AssertionError.class)
public void testInputCatcherMatrix() {
  SparseCoordinateFormatMatrix NullMat = null;
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
  SparseCoordinateFormatMatrix NullMat = null;
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
  SparseCoordinateFormatMatrix NullMat = null;
  BLAS2.dgemvInputSanityCheckerTransposed(NullMat, x5);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherVector() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by4, aVector);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherSizeWillNotCommute() {
  BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by4,x1);
}

/* Transpose 3 inputs */
@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherMatrix3inputs() {
  SparseCoordinateFormatMatrix NullMat = null;
  BLAS2.dgemvInputSanityCheckerTransposed(NullMat, x4, y5);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherVectorBadVectorGood() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by4, aVector, x4);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherVectorGoodVectorBad() {
  double[] aVector = null;
  BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by4, x4, aVector);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherSizeWillNotCommuteWithIntermediteVector() {
  BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by4,x1,y5);
}

@Test(expectedExceptions = AssertionError.class)
public void testTransposeInputCatcherSizeWillNotCommuteWithReturnVector() {
  BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by4,x4,y1);
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

//test 19x17, should trip loop unwindings
@Test
public void testDGEMV_ans_eq_A19x17_times_x17() {
assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x17),tmp_A19x17_times_x17));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x1() {
assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x17)),tmp_A19x17_times_x17));
}

@Test
public void testDGEMV_ans_eq_A19x17T_times_x1() {
assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by17,x19),tmp_A19x17T_times_x19));
}

@Test
public void testDGEMV_ans_eq_A19x17T_times_D1D_x1() {
assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by17, new DoubleMatrix1D(x19)),tmp_A19x17T_times_x19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_x1_interfaced_normal() {
assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x17,BLAS2.orientation.normal),tmp_A19x17_times_x17));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x1_interfaced_normal() {
assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x17),BLAS2.orientation.normal),tmp_A19x17_times_x17));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_x1_interfaced_transposed() {
assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x19,BLAS2.orientation.transposed),tmp_A19x17T_times_x19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x1_interfaced_transposed() {
assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x19),BLAS2.orientation.transposed),tmp_A19x17T_times_x19));
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


//test 19x17 should trip loop unwindings
@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x17() {
assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x17),tmp_alpha_times_A19x17_times_x17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x17() {
assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x17)),tmp_alpha_times_A19x17_times_x17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17T_times_x19() {
assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix19by17,x19),tmp_alpha_times_A19x17T_times_x19));
}

@Test
public void testDGEMV_ans_alpha_times_eq_A19x17T_times_D1D_x19() {
assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix19by17, new DoubleMatrix1D(x19)),tmp_alpha_times_A19x17T_times_x19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x17_interfaced_normal() {
assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x17,BLAS2.orientation.normal),tmp_alpha_times_A19x17_times_x17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x17_interfaced_normal() {
assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x17),BLAS2.orientation.normal),tmp_alpha_times_A19x17_times_x17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x19_interfaced_transposed() {
assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x19,BLAS2.orientation.transposed),tmp_alpha_times_A19x17T_times_x19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x19_interfaced_transposed() {
assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x19),BLAS2.orientation.transposed),tmp_alpha_times_A19x17T_times_x19));
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
public void testDGEMV_ans_eq_A5x5_times_x5_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,new DoubleMatrix1D(y5)),tmp_A5x5_times_x5_plus_y5));
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
public void testDGEMV_ans_eq_A5x5T_times_x5_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5,x5,new DoubleMatrix1D(y5)),tmp_A5x5T_times_x5_plus_y5));
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
public void testDGEMV_ans_eq_A5x5_times_x5_plus_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_A5x5_times_x5_plus_y5));
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
public void testDGEMV_ans_eq_A5x5_times_x5_plus_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_A5x5T_times_x5_plus_y5));
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
public void testDGEMV_ans_eq_A5x4_times_x4_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x4,new DoubleMatrix1D(y5)),tmp_A5x4_times_x4_plus_y5));
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
public void testDGEMV_ans_eq_A5x4T_times_x5_plus_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by4,x5,new DoubleMatrix1D(y4)),tmp_A5x4T_times_x5_plus_y4));
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
public void testDGEMV_ans_eq_A5x4_times_x4_plus_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x4,new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_A5x4_times_x4_plus_y5));
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
public void testDGEMV_ans_eq_A5x4_times_x5_plus_D1D_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x5,new DoubleMatrix1D(y4),BLAS2.orientation.transposed),tmp_A5x4T_times_x5_plus_y4));
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
public void testDGEMV_ans_eq_A4x5_times_x5_plus_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x5,new DoubleMatrix1D(y4)),tmp_A4x5_times_x5_plus_y4));
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
public void testDGEMV_ans_eq_A4x5T_times_x4_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix4by5,x4,new DoubleMatrix1D(y5)),tmp_A4x5T_times_x4_plus_y5));
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
public void testDGEMV_ans_eq_A4x5_times_x5_plus_D1D_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x5,new DoubleMatrix1D(y4),BLAS2.orientation.normal),tmp_A4x5_times_x5_plus_y4));
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
public void testDGEMV_ans_eq_A4x5_times_x4_plus_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x4,new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_A4x5T_times_x4_plus_y5));
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
public void testDGEMV_ans_eq_A1x1_times_x1_plus_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,new DoubleMatrix1D(y1)),tmp_A1x1_times_x1_plus_y1));
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
public void testDGEMV_ans_eq_A1x1T_times_x1_plus_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1,x1,new DoubleMatrix1D(y1)),tmp_A1x1T_times_x1_plus_y1));
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
public void testDGEMV_ans_eq_A1x1_times_x1_plus_D1D_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,new DoubleMatrix1D(y1),BLAS2.orientation.normal),tmp_A1x1_times_x1_plus_y1));
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
public void testDGEMV_ans_eq_A1x1_times_x1_plus_D1D_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,new DoubleMatrix1D(y1),BLAS2.orientation.transposed),tmp_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),new DoubleMatrix1D(y1),BLAS2.orientation.transposed),tmp_A1x1T_times_x1_plus_y1));
}


//test 19x17 should trip loop unwindings
@Test
public void testDGEMV_ans_eq_A19x17_times_x17_plus_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x17,y19),tmp_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x17_plus_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x17),y19),tmp_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_x17_plus_D1D_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x17,new DoubleMatrix1D(y19)),tmp_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x17_plus_D1D_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x17),new DoubleMatrix1D(y19)),tmp_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17T_times_x19_plus_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by17,x19,y17),tmp_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17T_times_D1D_x19_plus_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by17,new DoubleMatrix1D(x19),y17),tmp_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17T_times_x19_plus_D1D_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by17,x19,new DoubleMatrix1D(y17)),tmp_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17T_times_D1D_x19_plus_D1D_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by17,new DoubleMatrix1D(x19),new DoubleMatrix1D(y17)),tmp_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_x17_plus_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x17,y19,BLAS2.orientation.normal),tmp_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x17_plus_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x17),y19,BLAS2.orientation.normal),tmp_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_x17_plus_y19_D1D_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x17,new DoubleMatrix1D(y19),BLAS2.orientation.normal),tmp_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x17_plus_D1D_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x17),new DoubleMatrix1D(y19),BLAS2.orientation.normal),tmp_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_x19_plus_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x19,y17,BLAS2.orientation.transposed),tmp_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x19_plus_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x19),y17,BLAS2.orientation.transposed),tmp_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_x19_plus_y17_D1D_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x19,new DoubleMatrix1D(y17),BLAS2.orientation.transposed),tmp_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x19_plus_D1D_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x19),new DoubleMatrix1D(y17),BLAS2.orientation.transposed),tmp_A19x17T_times_x19_plus_y17));
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
public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,new DoubleMatrix1D(y5)),tmp_alpha_times_A5x5_times_x5_plus_y5));
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
public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by5,x5,new DoubleMatrix1D(y5)),tmp_alpha_times_A5x5T_times_x5_plus_y5));
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
public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5_D1D_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_alpha_times_A5x5_times_x5_plus_y5));
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
public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5_D1D_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_alpha_times_A5x5T_times_x5_plus_y5));
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
public void testDGEMV_ans_eq_alpha_times_A5x4_times_x4_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x4,new DoubleMatrix1D(y5)),tmp_alpha_times_A5x4_times_x4_plus_y5));
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
public void testDGEMV_ans_eq_alpha_times_A5x4T_times_x5_plus_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by4,x5,new DoubleMatrix1D(y4)),tmp_alpha_times_A5x4T_times_x5_plus_y4));
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
public void testDGEMV_ans_eq_alpha_times_A5x4_times_x4_plus_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x4,new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_alpha_times_A5x4_times_x4_plus_y5));
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
public void testDGEMV_ans_eq_alpha_times_A5x4_times_x5_plus_D1D_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x5,new DoubleMatrix1D(y4),BLAS2.orientation.transposed),tmp_alpha_times_A5x4T_times_x5_plus_y4));
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
public void testDGEMV_ans_eq_alpha_times_A4x5_times_x5_plus_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x5,new DoubleMatrix1D(y4)),tmp_alpha_times_A4x5_times_x5_plus_y4));
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
public void testDGEMV_ans_eq_alpha_times_A4x5T_times_x4_plus_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix4by5,x4,new DoubleMatrix1D(y5)),tmp_alpha_times_A4x5T_times_x4_plus_y5));
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
public void testDGEMV_ans_eq_alpha_times_A4x5_times_x5_plus_D1D_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x5,new DoubleMatrix1D(y4),BLAS2.orientation.normal),tmp_alpha_times_A4x5_times_x5_plus_y4));
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
public void testDGEMV_ans_eq_alpha_times_A4x5_times_x4_plus_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x4,new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_alpha_times_A4x5T_times_x4_plus_y5));
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
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,new DoubleMatrix1D(y1)),tmp_alpha_times_A1x1_times_x1_plus_y1));
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
public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1_plus_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix1by1,x1,new DoubleMatrix1D(y1)),tmp_alpha_times_A1x1T_times_x1_plus_y1));
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
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_D1D_y1_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,new DoubleMatrix1D(y1),BLAS2.orientation.normal),tmp_alpha_times_A1x1_times_x1_plus_y1));
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
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_D1D_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,new DoubleMatrix1D(y1),BLAS2.orientation.transposed),tmp_alpha_times_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1), new DoubleMatrix1D(y1),BLAS2.orientation.transposed),tmp_alpha_times_A1x1T_times_x1_plus_y1));
}

//19x17
@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x17_plus_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x17,y19),tmp_alpha_times_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x17_plus_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x17),y19),tmp_alpha_times_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x17_plus_D1D_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x17,new DoubleMatrix1D(y19)),tmp_alpha_times_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x17_plus_D1D_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x17),new DoubleMatrix1D(y19)),tmp_alpha_times_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17T_times_x19_plus_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix19by17,x19,y17),tmp_alpha_times_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17T_times_D1D_x19_plus_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix19by17,new DoubleMatrix1D(x19),y17),tmp_alpha_times_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17T_times_x19_plus_D1D_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix19by17,x19,new DoubleMatrix1D(y17)),tmp_alpha_times_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17T_times_D1D_x19_plus_D1D_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix19by17,new DoubleMatrix1D(x19),new DoubleMatrix1D(y17)),tmp_alpha_times_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x17_plus_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x17,y19,BLAS2.orientation.normal),tmp_alpha_times_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x17_plus_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x17),y19,BLAS2.orientation.normal),tmp_alpha_times_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x17_plus_D1D_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x17,new DoubleMatrix1D(y19),BLAS2.orientation.normal),tmp_alpha_times_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x17_plus_D1D_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x17), new DoubleMatrix1D(y19),BLAS2.orientation.normal),tmp_alpha_times_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x19_plus_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x19,y17,BLAS2.orientation.transposed),tmp_alpha_times_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x19_plus_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x19),y17,BLAS2.orientation.transposed),tmp_alpha_times_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x19_plus_y17_D1D_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x19,new DoubleMatrix1D(y17),BLAS2.orientation.transposed),tmp_alpha_times_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x19_plus_D1D_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x19), new DoubleMatrix1D(y17),BLAS2.orientation.transposed),tmp_alpha_times_A19x17T_times_x19_plus_y17));
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
public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,beta,new DoubleMatrix1D(y5)),tmp_A5x5_times_x5_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_A5x5T_times_x5_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5,x5,beta,new DoubleMatrix1D(y5)),tmp_A5x5T_times_x5_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,beta,new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_A5x5_times_x5_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_y5_D1D_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5,x5,beta,new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_A5x5T_times_x5_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_A5x4_times_x4_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x4,beta,new DoubleMatrix1D(y5)),tmp_A5x4_times_x4_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_A5x4T_times_x5_plus_beta_times_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by4,x5,beta,new DoubleMatrix1D(y4)),tmp_A5x4T_times_x5_plus_beta_times_y4));
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
public void testDGEMV_ans_eq_A5x4_times_x4_plus_beta_times_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x4,beta,new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_A5x4_times_x4_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_A5x4_times_x5_plus_beta_times_y4_D1D_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by4,x5,beta,new DoubleMatrix1D(y4),BLAS2.orientation.transposed),tmp_A5x4T_times_x5_plus_beta_times_y4));
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
public void testDGEMV_ans_eq_A4x5_times_x5_plus_beta_times_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x5,beta,new DoubleMatrix1D(y4)),tmp_A4x5_times_x5_plus_beta_times_y4));
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
public void testDGEMV_ans_eq_A4x5T_times_x4_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix4by5,x4,beta,new DoubleMatrix1D(y5)),tmp_A4x5T_times_x4_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_A4x5_times_x5_plus_beta_times_D1D_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x5,beta,new DoubleMatrix1D(y4),BLAS2.orientation.normal),tmp_A4x5_times_x5_plus_beta_times_y4));
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
public void testDGEMV_ans_eq_A4x5_times_x4_plus_beta_times_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix4by5,x4,beta,new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_A4x5T_times_x4_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,beta,new DoubleMatrix1D(y1)),tmp_A1x1_times_x1_plus_beta_times_y1));
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
public void testDGEMV_ans_eq_A1x1T_times_x1_plus_beta_times_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1,x1,beta,new DoubleMatrix1D(y1)),tmp_A1x1T_times_x1_plus_beta_times_y1));
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
public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_y1_D1D_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,beta,new DoubleMatrix1D(y1),BLAS2.orientation.normal),tmp_A1x1_times_x1_plus_beta_times_y1));
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
public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,x1,beta,new DoubleMatrix1D(y1),BLAS2.orientation.transposed),tmp_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1,new DoubleMatrix1D(x1),beta,new DoubleMatrix1D(y1),BLAS2.orientation.transposed),tmp_A1x1T_times_x1_plus_beta_times_y1));
}

//19x17
@Test
public void testDGEMV_ans_eq_A19x17_times_x17_plus_beta_times_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x17,beta,y19),tmp_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x17_plus_beta_times_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x17),beta,y19),tmp_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_x17_plus_beta_times_D1D_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x17,beta,new DoubleMatrix1D(y19)),tmp_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x17_plus_beta_times_D1D_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x17),beta,new DoubleMatrix1D(y19)),tmp_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17T_times_x19_plus_beta_times_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by17,x19,beta,y17),tmp_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17T_times_D1D_x19_plus_beta_times_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by17,new DoubleMatrix1D(x19),beta,y17),tmp_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17T_times_x19_plus_beta_times_D1D_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by17,x19,beta,new DoubleMatrix1D(y17)),tmp_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17T_times_D1D_x19_plus_beta_times_D1D_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by17,new DoubleMatrix1D(x19),beta,new DoubleMatrix1D(y17)),tmp_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_x17_plus_beta_times_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x17,beta,y19,BLAS2.orientation.normal),tmp_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x17_plus_beta_times_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x17),beta,y19,BLAS2.orientation.normal),tmp_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_x17_plus_beta_times_D1D_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x17,beta,new DoubleMatrix1D(y19),BLAS2.orientation.normal),tmp_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x17_plus_beta_times_D1D_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x17),beta,new DoubleMatrix1D(y19),BLAS2.orientation.normal),tmp_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_x19_plus_beta_times_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x19,beta,y17,BLAS2.orientation.transposed),tmp_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x19_plus_beta_times_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x19),beta,y17,BLAS2.orientation.transposed),tmp_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_x19_plus_beta_times_D1D_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,x19,beta,new DoubleMatrix1D(y17),BLAS2.orientation.transposed),tmp_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_A19x17_times_D1D_x19_plus_beta_times_D1D_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by17,new DoubleMatrix1D(x19),beta,new DoubleMatrix1D(y17),BLAS2.orientation.transposed),tmp_A19x17T_times_x19_plus_beta_times_y17));
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
public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,beta,new DoubleMatrix1D(y5)),tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by5,x5,beta,new DoubleMatrix1D(y5)),tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_alpha_A5x5_times_x5_plus_beta_times_D1D_y5_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,beta,new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_alpha_A5x5_times_x5_plus_beta_times_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by5,x5,beta,new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_alpha_times_A5x4_times_x4_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x4,beta,new DoubleMatrix1D(y5)),tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_alpha_times_A5x4T_times_x5_plus_beta_times_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix5by4,x5,beta,new DoubleMatrix1D(y4)),tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4));
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
public void testDGEMV_ans_eq_alpha_times_A5x4_times_x4_plus_beta_times_y5_D1D_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x4,beta,new DoubleMatrix1D(y5),BLAS2.orientation.normal),tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_alpha_times_A5x4_times_x5_plus_beta_times_D1D_y4_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix5by4,x5,beta,new DoubleMatrix1D(y4),BLAS2.orientation.transposed),tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4));
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
public void testDGEMV_ans_eq_alpha_times_A4x5_times_x5_plus_beta_times_D1D_y4() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x5,beta,new DoubleMatrix1D(y4)),tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4));
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
public void testDGEMV_ans_eq_alpha_times_A4x5T_times_x4_plus_beta_times_D1D_y5() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix4by5,x4,beta,new DoubleMatrix1D(y5)),tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_alpha_times_A4x5_times_x5_plus_beta_times_D1D_y4_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x5,beta,new DoubleMatrix1D(y4),BLAS2.orientation.normal),tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4));
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
public void testDGEMV_ans_eq_alpha_times_A4x5_times_x4_plus_beta_times_D1D_y5_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix4by5,x4,beta,new DoubleMatrix1D(y5),BLAS2.orientation.transposed),tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5));
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
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,beta,new DoubleMatrix1D(y1)),tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
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
public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1_plus_beta_times_D1D_y1() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix1by1,x1,beta,new DoubleMatrix1D(y1)),tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
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
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1_D1D_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,beta,new DoubleMatrix1D(y1),BLAS2.orientation.normal),tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
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
public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,x1,beta,new DoubleMatrix1D(y1),BLAS2.orientation.transposed),tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix1by1,new DoubleMatrix1D(x1),beta,new DoubleMatrix1D(y1),BLAS2.orientation.transposed),tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
}

//19x17
@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x17_plus_beta_times_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x17,beta,y19),tmp_alpha_times_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x17_plus_beta_times_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x17),beta,y19),tmp_alpha_times_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x17_plus_beta_times_D1D_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x17,beta,new DoubleMatrix1D(y19)),tmp_alpha_times_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x17_plus_beta_times_D1D_y19() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x17),beta,new DoubleMatrix1D(y19)),tmp_alpha_times_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17T_times_x19_plus_beta_times_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix19by17,x19,beta,y17),tmp_alpha_times_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17T_times_D1D_x19_plus_beta_times_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix19by17,new DoubleMatrix1D(x19),beta,y17),tmp_alpha_times_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17T_times_x19_plus_beta_times_D1D_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix19by17,x19,beta,new DoubleMatrix1D(y17)),tmp_alpha_times_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17T_times_D1D_x19_plus_beta_times_D1D_y17() {
  assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha,aMatrix19by17,new DoubleMatrix1D(x19),beta,new DoubleMatrix1D(y17)),tmp_alpha_times_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x17_plus_beta_times_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x17,beta,y19,BLAS2.orientation.normal),tmp_alpha_times_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x17_plus_beta_times_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x17),beta,y19,BLAS2.orientation.normal),tmp_alpha_times_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x17_plus_beta_times_D1D_y19_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x17,beta,new DoubleMatrix1D(y19),BLAS2.orientation.normal),tmp_alpha_times_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x19_plus_beta_times_D1D_y17_interfaced_normal() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x17),beta,new DoubleMatrix1D(y19),BLAS2.orientation.normal),tmp_alpha_times_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x19_plus_beta_times_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x19,beta,y17,BLAS2.orientation.transposed),tmp_alpha_times_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x19_plus_beta_times_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x19),beta,y17,BLAS2.orientation.transposed),tmp_alpha_times_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_x19_plus_beta_times_D1D_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,x19,beta,new DoubleMatrix1D(y17),BLAS2.orientation.transposed),tmp_alpha_times_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A19x17_times_D1D_x19_plus_beta_times_D1D_y17_interfaced_transposed() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix19by17,new DoubleMatrix1D(x19),beta,new DoubleMatrix1D(y17),BLAS2.orientation.transposed),tmp_alpha_times_A19x17T_times_x19_plus_beta_times_y17));
}

// test the inplaces
//** group 1:: A*x OR A^T*x

//5x5
@Test
public void testDGEMV_y5_eq_A5x5_times_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by5,x5);
  assertTrue(Arrays.equals(ycp,tmp_A5x5_times_x5));
}

@Test
public void testDGEMV_y5_eq_A5x5_times_D1D_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by5,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,tmp_A5x5_times_x5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x5_times_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix5by5,x5);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5_times_x5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x5_times_D1D_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix5by5,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5_times_x5));
}

@Test
public void testDGEMV_y5_eq_A5x5T_times_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlaceTransposed(ycp,aMatrix5by5,x5);
  assertTrue(Arrays.equals(ycp,tmp_A5x5T_times_x5));
}

@Test
public void testDGEMV_y5_eq_A5x5T_times_D1D_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlaceTransposed(ycp,aMatrix5by5,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,tmp_A5x5T_times_x5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x5T_times_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,aMatrix5by5,x5);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5T_times_x5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x5T_times_D1D_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,aMatrix5by5,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5T_times_x5));
}

@Test
public void testDGEMV_y5_eq_A5x5_times_x5_interfaced_normal() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by5,x5,BLAS2.orientation.normal);
  assertTrue(Arrays.equals(ycp,tmp_A5x5_times_x5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x5_times_x5_interfaced_normal() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(y5);
  BLAS2.dgemvInPlace(tmp,aMatrix5by5,x5,BLAS2.orientation.normal);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5_times_x5));
}

@Test
public void testDGEMV_y5_eq_A5x5_times_D1D_x5_interfaced_normal() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by5,new DoubleMatrix1D(x5),BLAS2.orientation.normal);
  assertTrue(Arrays.equals(ycp,tmp_A5x5_times_x5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x5_times_D1D_x5_interfaced_normal() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix5by5,new DoubleMatrix1D(x5),BLAS2.orientation.normal);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5_times_x5));
}

@Test
public void testDGEMV_y5_eq_A5x5_times_x5_interfaced_transposed() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by5,x5,BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(ycp,tmp_A5x5T_times_x5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x5_times_x5_interfaced_transposed() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(y5);
  BLAS2.dgemvInPlace(tmp,aMatrix5by5,x5,BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5T_times_x5));
}

@Test
public void testDGEMV_y5_eq_A5x5_times_D1D_x5_interfaced_transposed() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by5,new DoubleMatrix1D(x5),BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(ycp,tmp_A5x5T_times_x5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x5_times_D1D_x5_interfaced_transposed() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix5by5,new DoubleMatrix1D(x5),BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5T_times_x5));
}


//5x4
@Test
public void testDGEMV_y5_eq_A5x4_times_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by4,x4);
  assertTrue(Arrays.equals(ycp,tmp_A5x4_times_x4));
}

@Test
public void testDGEMV_y5_eq_A5x4_times_D1D_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by4,new DoubleMatrix1D(x4));
  assertTrue(Arrays.equals(ycp,tmp_A5x4_times_x4));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x4_times_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix5by4,x4);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4_times_x4));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x4_times_D1D_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix5by4,new DoubleMatrix1D(x4));
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4_times_x4));
}

@Test
public void testDGEMV_y4_eq_A5x4T_times_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlaceTransposed(ycp,aMatrix5by4,x5);
  assertTrue(Arrays.equals(ycp,tmp_A5x4T_times_x5));
}

@Test
public void testDGEMV_y4_eq_A5x4T_times_D1D_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlaceTransposed(ycp,aMatrix5by4,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,tmp_A5x4T_times_x5));
}

@Test
public void testDGEMV_D1D_y4_eq_A5x4T_times_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,aMatrix5by4,x5);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4T_times_x5));
}

@Test
public void testDGEMV_D1D_y4_eq_A5x4T_times_D1D_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,aMatrix5by4,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4T_times_x5));
}

@Test
public void testDGEMV_y5_eq_A5x4_times_x4_interfaced_normal() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by4,x4,BLAS2.orientation.normal);
  assertTrue(Arrays.equals(ycp,tmp_A5x4_times_x4));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x4_times_x4_interfaced_normal() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(y5);
  BLAS2.dgemvInPlace(tmp,aMatrix5by4,x4,BLAS2.orientation.normal);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4_times_x4));
}

@Test
public void testDGEMV_y5_eq_A5x4_times_D1D_x4_interfaced_normal() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by4,new DoubleMatrix1D(x4),BLAS2.orientation.normal);
  assertTrue(Arrays.equals(ycp,tmp_A5x4_times_x4));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x4_times_D1D_x4_interfaced_normal() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix5by4,new DoubleMatrix1D(x4),BLAS2.orientation.normal);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4_times_x4));
}

@Test
public void testDGEMV_y4_eq_A5x4_times_x5_interfaced_transposed() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by4,x5,BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(ycp,tmp_A5x4T_times_x5));
}

@Test
public void testDGEMV_D1D_y4_eq_A5x4_times_x5_interfaced_transposed() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(y4);
  BLAS2.dgemvInPlace(tmp,aMatrix5by4,x5,BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4T_times_x5));
}

@Test
public void testDGEMV_y4_eq_A5x4_times_D1D_x5_interfaced_transposed() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlace(ycp,aMatrix5by4,new DoubleMatrix1D(x5),BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(ycp,tmp_A5x4T_times_x5));
}

@Test
public void testDGEMV_D1D_y4_eq_A5x4_times_D1D_x5_interfaced_transposed() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix5by4,new DoubleMatrix1D(x5),BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4T_times_x5));
}

//4x5
@Test
public void testDGEMV_y4_eq_A4x5_times_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlace(ycp,aMatrix4by5,x5);
  assertTrue(Arrays.equals(ycp,tmp_A4x5_times_x5));
}

@Test
public void testDGEMV_y4_eq_A4x5_times_D1D_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlace(ycp,aMatrix4by5,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,tmp_A4x5_times_x5));
}

@Test
public void testDGEMV_D1D_y4_eq_A4x5_times_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix4by5,x5);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5_times_x5));
}

@Test
public void testDGEMV_D1D_y4_eq_A4x5_times_D1D_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix4by5,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5_times_x5));
}

@Test
public void testDGEMV_y5_eq_A4x5T_times_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlaceTransposed(ycp,aMatrix4by5,x4);
  assertTrue(Arrays.equals(ycp,tmp_A4x5T_times_x4));
}

@Test
public void testDGEMV_y5_eq_A4x5T_times_D1D_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlaceTransposed(ycp,aMatrix4by5,new DoubleMatrix1D(x4));
  assertTrue(Arrays.equals(ycp,tmp_A4x5T_times_x4));
}

@Test
public void testDGEMV_D1D_y5_eq_A4x5T_times_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,aMatrix4by5,x4);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5T_times_x4));
}

@Test
public void testDGEMV_D1D_y5_eq_A4x5T_times_D1D_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,aMatrix4by5,new DoubleMatrix1D(x4));
  assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5T_times_x4));
}

@Test
public void testDGEMV_y4_eq_A4x5_times_x5_interfaced_normal() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlace(ycp,aMatrix4by5,x5,BLAS2.orientation.normal);
  assertTrue(Arrays.equals(ycp,tmp_A4x5_times_x5));
}

@Test
public void testDGEMV_D1D_y4_eq_A4x5_times_x5_interfaced_normal() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(y4);
  BLAS2.dgemvInPlace(tmp,aMatrix4by5,x5,BLAS2.orientation.normal);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5_times_x5));
}

@Test
public void testDGEMV_y4_eq_A4x5_times_D1D_x5_interfaced_normal() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlace(ycp,aMatrix4by5,new DoubleMatrix1D(x5),BLAS2.orientation.normal);
  assertTrue(Arrays.equals(ycp,tmp_A4x5_times_x5));
}

@Test
public void testDGEMV_D1D_y4_eq_A4x5_times_D1D_x5_interfaced_normal() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix4by5,new DoubleMatrix1D(x5),BLAS2.orientation.normal);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5_times_x5));
}

@Test
public void testDGEMV_y5_eq_A4x5_times_x4_interfaced_transposed() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix4by5,x4,BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(ycp,tmp_A4x5T_times_x4));
}

@Test
public void testDGEMV_D1D_y5_eq_A4x5_times_x4_interfaced_transposed() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(y5);
  BLAS2.dgemvInPlace(tmp,aMatrix4by5,x4,BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5T_times_x4));
}

@Test
public void testDGEMV_y5_eq_A4x5_times_D1D_x4_interfaced_transposed() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,aMatrix4by5,new DoubleMatrix1D(x4),BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(ycp,tmp_A4x5T_times_x4));
}

@Test
public void testDGEMV_D1D_y5_eq_A4x5_times_D1D_x4_interfaced_transposed() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix4by5,new DoubleMatrix1D(x4),BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5T_times_x4));
}

//1x1
@Test
public void testDGEMV_y1_eq_A1x1_times_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  BLAS2.dgemvInPlace(ycp,aMatrix1by1,x1);
  assertTrue(Arrays.equals(ycp,tmp_A1x1_times_x1));
}

@Test
public void testDGEMV_y1_eq_A1x1_times_D1D_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  BLAS2.dgemvInPlace(ycp,aMatrix1by1,new DoubleMatrix1D(x1));
  assertTrue(Arrays.equals(ycp,tmp_A1x1_times_x1));
}

@Test
public void testDGEMV_D1D_y1_eq_A1x1_times_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix1by1,x1);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A1x1_times_x1));
}

@Test
public void testDGEMV_D1D_y1_eq_A1x1_times_D1D_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix1by1,new DoubleMatrix1D(x1));
  assertTrue(Arrays.equals(tmp.getData(),tmp_A1x1_times_x1));
}

@Test
public void testDGEMV_y1_eq_A1x1T_times_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  BLAS2.dgemvInPlaceTransposed(ycp,aMatrix1by1,x1);
  assertTrue(Arrays.equals(ycp,tmp_A1x1T_times_x1));
}

@Test
public void testDGEMV_y1_eq_A1x1T_times_D1D_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  BLAS2.dgemvInPlaceTransposed(ycp,aMatrix1by1,new DoubleMatrix1D(x1));
  assertTrue(Arrays.equals(ycp,tmp_A1x1T_times_x1));
}

@Test
public void testDGEMV_D1D_y1_eq_A1x1T_times_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,aMatrix1by1,x1);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A1x1T_times_x1));
}

@Test
public void testDGEMV_D1D_y1_eq_A1x1T_times_D1D_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,aMatrix1by1,new DoubleMatrix1D(x1));
  assertTrue(Arrays.equals(tmp.getData(),tmp_A1x1T_times_x1));
}

@Test
public void testDGEMV_y1_eq_A1x1_times_x1_interfaced_normal() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  BLAS2.dgemvInPlace(ycp,aMatrix1by1,x1,BLAS2.orientation.normal);
  assertTrue(Arrays.equals(ycp,tmp_A1x1_times_x1));
}

@Test
public void testDGEMV_D1D_y1_eq_A1x1_times_x1_interfaced_normal() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(y1);
  BLAS2.dgemvInPlace(tmp,aMatrix1by1,x1,BLAS2.orientation.normal);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A1x1_times_x1));
}

@Test
public void testDGEMV_y1_eq_A1x1_times_D1D_x1_interfaced_normal() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  BLAS2.dgemvInPlace(ycp,aMatrix1by1,new DoubleMatrix1D(x1),BLAS2.orientation.normal);
  assertTrue(Arrays.equals(ycp,tmp_A1x1_times_x1));
}

@Test
public void testDGEMV_D1D_y1_eq_A1x1_times_D1D_x1_interfaced_normal() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix1by1,new DoubleMatrix1D(x1),BLAS2.orientation.normal);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A1x1_times_x1));
}

@Test
public void testDGEMV_y1_eq_A1x1_times_x1_interfaced_transposed() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  BLAS2.dgemvInPlace(ycp,aMatrix1by1,x1,BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(ycp,tmp_A1x1T_times_x1));
}

@Test
public void testDGEMV_D1D_y1_eq_A1x1_times_x1_interfaced_transposed() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(y1);
  BLAS2.dgemvInPlace(tmp,aMatrix1by1,x1,BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A1x1T_times_x1));
}

@Test
public void testDGEMV_y1_eq_A1x1_times_D1D_x1_interfaced_transposed() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  BLAS2.dgemvInPlace(ycp,aMatrix1by1,new DoubleMatrix1D(x1),BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(ycp,tmp_A1x1T_times_x1));
}

@Test
public void testDGEMV_D1D_y1_eq_A1x1_times_D1D_x1_interfaced_transposed() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix1by1,new DoubleMatrix1D(x1),BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A1x1T_times_x1));
}

//19x17
@Test
public void testDGEMV_y19_eq_A19x17_times_x17() {
  double [] ycp = new double[y19.length];
  System.arraycopy(y19, 0, ycp, 0, y19.length);
  BLAS2.dgemvInPlace(ycp,aMatrix19by17,x17);
  assertTrue(Arrays.equals(ycp,tmp_A19x17_times_x17));
}

@Test
public void testDGEMV_y19_eq_A19x17_times_D1D_x1() {
  double [] ycp = new double[y19.length];
  System.arraycopy(y19, 0, ycp, 0, y19.length);
  BLAS2.dgemvInPlace(ycp,aMatrix19by17,new DoubleMatrix1D(x17));
  assertTrue(Arrays.equals(ycp,tmp_A19x17_times_x17));
}

@Test
public void testDGEMV_D1D_y19_eq_A19x17_times_x1() {
  double [] ycp = new double[y19.length];
  System.arraycopy(y19, 0, ycp, 0, y19.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix19by17,x17);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17_times_x17));
}

@Test
public void testDGEMV_D1D_y19_eq_A19x17_times_D1D_x1() {
  double [] ycp = new double[y19.length];
  System.arraycopy(y19, 0, ycp, 0, y19.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix19by17,new DoubleMatrix1D(x17));
  assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17_times_x17));
}

@Test
public void testDGEMV_y17_eq_A19x17T_times_x19() {
  double [] ycp = new double[y17.length];
  System.arraycopy(y17, 0, ycp, 0, y17.length);
  BLAS2.dgemvInPlaceTransposed(ycp,aMatrix19by17,x19);
  assertTrue(Arrays.equals(ycp,tmp_A19x17T_times_x19));
}

@Test
public void testDGEMV_y17eq_A19x17T_times_D1D_x19() {
  double [] ycp = new double[y17.length];
  System.arraycopy(y17, 0, ycp, 0, y17.length);
  BLAS2.dgemvInPlaceTransposed(ycp,aMatrix19by17,new DoubleMatrix1D(x19));
  assertTrue(Arrays.equals(ycp,tmp_A19x17T_times_x19));
}

@Test
public void testDGEMV_D1D_y17_eq_A19x17T_times_x19() {
  double [] ycp = new double[y17.length];
  System.arraycopy(y17, 0, ycp, 0, y17.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,aMatrix19by17,x19);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17T_times_x19));
}

@Test
public void testDGEMV_D1D_y17_eq_A19x17T_times_D1D_x19() {
  double [] ycp = new double[y17.length];
  System.arraycopy(y17, 0, ycp, 0, y17.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,aMatrix19by17,new DoubleMatrix1D(x19));
  assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17T_times_x19));
}

@Test
public void testDGEMV_y19_eq_A19x17_times_x17_interfaced_normal() {
  double [] ycp = new double[y19.length];
  System.arraycopy(y19, 0, ycp, 0, y19.length);
  BLAS2.dgemvInPlace(ycp,aMatrix19by17,x17,BLAS2.orientation.normal);
  assertTrue(Arrays.equals(ycp,tmp_A19x17_times_x17));
}

@Test
public void testDGEMV_D1D_y19_eq_A19x17_times_x17_interfaced_normal() {
  double [] ycp = new double[y19.length];
  System.arraycopy(y19, 0, ycp, 0, y19.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(y19);
  BLAS2.dgemvInPlace(tmp,aMatrix19by17,x17,BLAS2.orientation.normal);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17_times_x17));
}

@Test
public void testDGEMV_y19_eq_A19x17_times_D1D_x17_interfaced_normal() {
  double [] ycp = new double[y19.length];
  System.arraycopy(y19, 0, ycp, 0, y19.length);
  BLAS2.dgemvInPlace(ycp,aMatrix19by17,new DoubleMatrix1D(x17),BLAS2.orientation.normal);
  assertTrue(Arrays.equals(ycp,tmp_A19x17_times_x17));
}

@Test
public void testDGEMV_D1D_y19_eq_A19x17_times_D1D_x17_interfaced_normal() {
  double [] ycp = new double[y19.length];
  System.arraycopy(y19, 0, ycp, 0, y19.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix19by17,new DoubleMatrix1D(x17),BLAS2.orientation.normal);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17_times_x17));
}

@Test
public void testDGEMV_y17_eq_A19x17_times_x19_interfaced_transposed() {
  double [] ycp = new double[y17.length];
  System.arraycopy(y17, 0, ycp, 0, y17.length);
  BLAS2.dgemvInPlace(ycp,aMatrix19by17,x19,BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(ycp,tmp_A19x17T_times_x19));
}

@Test
public void testDGEMV_D1D_y17_eq_A19x17_times_x19_interfaced_transposed() {
  double [] ycp = new double[y17.length];
  System.arraycopy(y17, 0, ycp, 0, y17.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(y17);
  BLAS2.dgemvInPlace(tmp,aMatrix19by17,x19,BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17T_times_x19));
}

@Test
public void testDGEMV_y17_eq_A19x17_times_D1D_x19_interfaced_transposed() {
  double [] ycp = new double[y17.length];
  System.arraycopy(y17, 0, ycp, 0, y17.length);
  BLAS2.dgemvInPlace(ycp,aMatrix19by17,new DoubleMatrix1D(x19),BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(ycp,tmp_A19x17T_times_x19));
}

@Test
public void testDGEMV_D1D_y17_eq_A19x17_times_D1D_x19_interfaced_transposed() {
  double [] ycp = new double[y17.length];
  System.arraycopy(y17, 0, ycp, 0, y17.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,aMatrix19by17,new DoubleMatrix1D(x19),BLAS2.orientation.transposed);
  assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17T_times_x19));
}

//** group 2:: y=alpha*A*x OR y=alpha*A^T*x
//5x5
@Test
public void testDGEMV_y5_eq_alpha_times_A5x5_times_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by5,0,x5);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5_times_x5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A5x5_times_D1D_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by5,0,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5_times_x5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by5,0,x5);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5_times_x5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_D1D_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by5,0,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5_times_x5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A5x5T_times_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix5by5,0,x5);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5T_times_x5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A5x5T_times_D1D_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix5by5,0,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5T_times_x5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix5by5,0,x5);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5T_times_x5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_D1D_x5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix5by5,0,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5T_times_x5));
}

//5x4
@Test
public void testDGEMV_y5_eq_alpha_times_A5x4_times_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by4,0,x4);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x4_times_x4));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A5x4_times_D1D_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by4,0,new DoubleMatrix1D(x4));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x4_times_x4));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x4_times_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by4,0,x4);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x4_times_x4));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x4_times_D1D_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by4,0,new DoubleMatrix1D(x4));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x4_times_x4));
}

@Test
public void testDGEMV_y4_eq_alpha_times_A5x4T_times_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix5by4,0,x5);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x4T_times_x5));
}

@Test
public void testDGEMV_y4_eq_alpha_times_A5x4T_times_D1D_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix5by4,0,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x4T_times_x5));
}

@Test
public void testDGEMV_D1D_y4_eq_alpha_times_A5x4T_times_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix5by4,0,x5);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x4T_times_x5));
}

@Test
public void testDGEMV_D1D_y4_eq_alpha_times_A5x4T_times_D1D_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix5by4,0,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x4T_times_x5));
}

//4x5
@Test
public void testDGEMV_y4_eq_alpha_times_A4x5_times_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix4by5,0,x5);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A4x5_times_x5));
}

@Test
public void testDGEMV_y4_eq_alpha_times_A4x5_times_D1D_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix4by5,0,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A4x5_times_x5));
}

@Test
public void testDGEMV_D1D_y4_eq_alpha_times_A4x5_times_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix4by5,0,x5);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A4x5_times_x5));
}

@Test
public void testDGEMV_D1D_y4_eq_alpha_times_A4x5_times_D1D_x5() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix4by5,0,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A4x5_times_x5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A4x5T_times_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix4by5,0,x4);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A4x5T_times_x4));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A4x5T_times_D1D_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix4by5,0,new DoubleMatrix1D(x4));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A4x5T_times_x4));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A4x5T_times_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix4by5,0,x4);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A4x5T_times_x4));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A4x5T_times_D1D_x4() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix4by5,0,new DoubleMatrix1D(x4));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A4x5T_times_x4));
}

//1x1
@Test
public void testDGEMV_y1_eq_alpha_times_A1x1_times_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix1by1,0,x1);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A1x1_times_x1));
}

@Test
public void testDGEMV_y1_eq_alpha_times_A1x1_times_D1D_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix1by1,0,new DoubleMatrix1D(x1));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A1x1_times_x1));
}

@Test
public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix1by1,0,x1);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A1x1_times_x1));
}

@Test
public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_D1D_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix1by1,0,new DoubleMatrix1D(x1));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A1x1_times_x1));
}

@Test
public void testDGEMV_y1_eq_alpha_times_A1x1T_times_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix1by1,0,x1);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A1x1T_times_x1));
}

@Test
public void testDGEMV_y1_eq_alpha_times_A1x1T_times_D1D_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix1by1,0,new DoubleMatrix1D(x1));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A1x1T_times_x1));
}

@Test
public void testDGEMV_D1D_y1_eq_alpha_times_A1xT_times_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix1by1,0,x1);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A1x1T_times_x1));
}

@Test
public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_D1D_x1() {
  double [] ycp = new double[y1.length];
  System.arraycopy(y1, 0, ycp, 0, y1.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix1by1,0,new DoubleMatrix1D(x1));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A1x1T_times_x1));
}


//19x17
@Test
public void testDGEMV_y19_eq_alpha_times_A19x17_times_x17() {
  double [] ycp = new double[y19.length];
  System.arraycopy(y19, 0, ycp, 0, y19.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix19by17,0,x17);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A19x17_times_x17));
}

@Test
public void testDGEMV_y19_eq_alpha_times_A19x5_times_D1D_x17() {
  double [] ycp = new double[y19.length];
  System.arraycopy(y19, 0, ycp, 0, y19.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix19by17,0,new DoubleMatrix1D(x17));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A19x17_times_x17));
}

@Test
public void testDGEMV_D1D_y19_eq_alpha_times_A19x17_times_x17() {
  double [] ycp = new double[y19.length];
  System.arraycopy(y19, 0, ycp, 0, y19.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix19by17,0,x17);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A19x17_times_x17));
}

@Test
public void testDGEMV_D1D_y19_eq_alpha_times_A19x17_times_D1D_x17() {
  double [] ycp = new double[y19.length];
  System.arraycopy(y19, 0, ycp, 0, y19.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix19by17,0,new DoubleMatrix1D(x17));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A19x17_times_x17));
}

@Test
public void testDGEMV_y17_eq_alpha_times_A19x17T_times_x19() {
  double [] ycp = new double[y17.length];
  System.arraycopy(y17, 0, ycp, 0, y17.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix19by17,0,x19);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A19x17T_times_x19));
}

@Test
public void testDGEMV_y17_eq_alpha_times_A19x17T_times_D1D_x19() {
  double [] ycp = new double[y17.length];
  System.arraycopy(y17, 0, ycp, 0, y17.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix19by17,0,new DoubleMatrix1D(x19));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A19x17T_times_x19));
}

@Test
public void testDGEMV_D1D_y17_eq_alpha_times_A19x17T_times_x19() {
  double [] ycp = new double[y17.length];
  System.arraycopy(y17, 0, ycp, 0, y17.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix19by17,0,x19);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A19x17T_times_x19));
}

@Test
public void testDGEMV_D1D_y17_eq_alpha_times_A19x17T_times_D1D_x19() {
  double [] ycp = new double[y17.length];
  System.arraycopy(y17, 0, ycp, 0, y17.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix19by17,0,new DoubleMatrix1D(x19));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A19x17T_times_x19));
}



//** group 3:: A*x+y OR A^T*x+y
//5x5
@Test
public void testDGEMV_y5_eq_A5x5_times_x5_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix5by5,1,x5);
assertTrue(Arrays.equals(ycp,tmp_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_y5_eq_A5x5_times_D1D_x5_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix5by5,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(ycp,tmp_A5x5_times_x5_plus_y5));
}


@Test
public void testDGEMV_D1D_y5_eq_A5x5_times_x5_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix5by5,1,x5);
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_D1D_y5_A5x5_times_D1D_x5_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix5by5,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_y5_eq_A5x5T_times_x5_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix5by5,1,x5);
assertTrue(Arrays.equals(ycp,tmp_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_y5_eq_A5x5T_times_D1D_x5_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix5by5,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(ycp,tmp_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x5T_times_x5_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix5by5,1,x5);
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x5T_times_D1D_x5_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix5by5,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5T_times_x5_plus_y5));
}

//5x4
@Test
public void testDGEMV_y5_eq_A5x4_times_x4_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix5by4,1,x4);
assertTrue(Arrays.equals(ycp,tmp_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_y5_eq_A5x4_times_D1D_x4_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix5by4,1,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(ycp,tmp_A5x4_times_x4_plus_y5));
}


@Test
public void testDGEMV_D1D_y5_eq_A5x4_times_x5_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix5by4,1,x4);
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_D1D_y5_A5x4_times_D1D_x4_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix5by4,1,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_y4_eq_A5x4T_times_x5_plus_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix5by4,1,x5);
assertTrue(Arrays.equals(ycp,tmp_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_y4_eq_A5x4T_times_D1D_x5_plus_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix5by4,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(ycp,tmp_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_D1D_y4_eq_A5x4T_times_x5_plus_D1D_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix5by4,1,x5);
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_D1D_y4_eq_A5x4T_times_D1D_x5_plus_D1D_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix5by4,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4T_times_x5_plus_y4));
}

//4x5
@Test
public void testDGEMV_y4_eq_A4x5_times_x5_plus_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix4by5,1,x5);
assertTrue(Arrays.equals(ycp,tmp_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_y4_eq_A4x5_times_D1D_x5_plus_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix4by5,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(ycp,tmp_A4x5_times_x5_plus_y4));
}


@Test
public void testDGEMV_D1D_y4_eq_A4x5_times_x5_plus_D1D_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix4by5,1,x5);
assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_D1D_y4_A4x5_times_D1D_x5_plus_D1D_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix4by5,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_y5_eq_A4x5T_times_x4_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix4by5,1,x4);
assertTrue(Arrays.equals(ycp,tmp_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_y5_eq_A4x5T_times_D1D_x4_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix4by5,1,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(ycp,tmp_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_A4x5T_times_x4_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix4by5,1,x4);
assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_A4x5T_times_D1D_x4_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix4by5,1,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5T_times_x4_plus_y5));
}

//19x17
@Test
public void testDGEMV_y19_eq_A19x17_times_x17_plus_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix19by17,1,x17);
assertTrue(Arrays.equals(ycp,tmp_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_y19_eq_A19x17_times_D1D_x17_plus_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix19by17,1,new DoubleMatrix1D(x17));
assertTrue(Arrays.equals(ycp,tmp_A19x17_times_x17_plus_y19));
}


@Test
public void testDGEMV_D1D_y19_eq_A19x17_times_x17_plus_D1D_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix19by17,1,x17);
assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_D1D_y19_eq_A19x17_times_D1D_x17_plus_D1D_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix19by17,1,new DoubleMatrix1D(x17));
assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_y17_eq_A19x17T_times_x19_plus_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix19by17,1,x19);
assertTrue(Arrays.equals(ycp,tmp_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_y17_eq_A19x17T_times_D1D_x19_plus_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix19by17,1,new DoubleMatrix1D(x19));
assertTrue(Arrays.equals(ycp,tmp_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_D1D_y17_eq_A19x17T_times_x19_plus_D1D_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix19by17,1,x19);
assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_D1D_y17_eq_A19x17T_times_D1D_x19_plus_D1D_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix19by17,1,new DoubleMatrix1D(x19));
assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17T_times_x19_plus_y17));
}

//** group 4:: alpha*A*x+y OR alpha*A^T*x+y
//5x5
@Test
public void testDGEMV_y5_eq_alpha_times_A5x5_times_x5_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by5,1,x5);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A5x5_times_D1D_x5_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by5,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5_times_x5_plus_y5));
}


@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_x5_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by5,1,x5);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_D1D_y5_alpha_times_A5x5_times_D1D_x5_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by5,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5_times_x5_plus_y5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A5x5T_times_x5_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix5by5,1,x5);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A5x5T_times_D1D_x5_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix5by5,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_x5_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix5by5,1,x5);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5T_times_x5_plus_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_D1D_x5_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix5by5,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5T_times_x5_plus_y5));
}


//5x4
@Test
public void testDGEMV_y5_eq_alpha_times_A5x4_times_x4_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by4,1,x4);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A5x4_times_D1D_x4_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by4,1,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x4_times_x4_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by4,1,x4);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_D1D_y5_alpha_times_A5x4_times_D1D_x4_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by4,1,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x4_times_x4_plus_y5));
}

@Test
public void testDGEMV_y4_eq_alpha_times_A5x4T_times_x5_plus_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix5by4,1,x5);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_y4_eq_alpha_times_A5x4T_times_D1D_x5_plus_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix5by4,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_D1D_y4_eq_alpha_times_A5x4T_times_x5_plus_D1D_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix5by4,1,x5);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x4T_times_x5_plus_y4));
}

@Test
public void testDGEMV_D1D_y4_eq_alpha_times_A5x4T_times_D1D_x5_plus_D1D_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix5by4,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x4T_times_x5_plus_y4));
}

//4x5
@Test
public void testDGEMV_y4_eq_alpha_times_A4x5_times_x5_plus_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix4by5,1,x5);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_y4_eq_alpha_times_A4x5_times_D1D_x5_plus_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix4by5,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_D1D_y4_eq_alpha_times_A4x5_times_x5_plus_D1D_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix4by5,1,x5);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_D1D_y4_alpha_times_A4x5_times_D1D_x5_plus_D1D_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix4by5,1,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A4x5_times_x5_plus_y4));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A4x5T_times_x4_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix4by5,1,x4);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A4x5T_times_D1D_x4_plus_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix4by5,1,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A4x5T_times_x4_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix4by5,1,x4);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A4x5T_times_x4_plus_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A4x5T_times_D1D_x4_plus_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix4by5,1,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A4x5T_times_x4_plus_y5));
}

//1x1
@Test
public void testDGEMV_y1_eq_alpha_times_A1x1_times_x1_plus_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix1by1,1,x1);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_y1_eq_alpha_times_A1x1_times_D1D_x1_plus_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix1by1,1,new DoubleMatrix1D(x1));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_x1_plus_D1D_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix1by1,1,x1);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_D1D_y1_alpha_times_A1x1_times_D1D_x1_plus_D1D_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix1by1,1,new DoubleMatrix1D(x1));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A1x1_times_x1_plus_y1));
}

@Test
public void testDGEMV_y1_eq_alpha_times_A1x1T_times_x1_plus_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix1by1,1,x1);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_y1_eq_alpha_times_A1x1T_times_D1D_x1_plus_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix1by1,1,new DoubleMatrix1D(x1));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_x1_plus_D1D_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix1by1,1,x1);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A1x1T_times_x1_plus_y1));
}

@Test
public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_D1D_x1_plus_D1D_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix1by1,1,new DoubleMatrix1D(x1));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A1x1T_times_x1_plus_y1));
}

//19x17
@Test
public void testDGEMV_y19_eq_alpha_times_A19x17_times_x17_plus_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix19by17,1,x17);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_y19_eq_alpha_times_A19x17_times_D1D_x17_plus_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix19by17,1,new DoubleMatrix1D(x17));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_D1D_y19_eq_alpha_times_A19x17_times_x17_plus_D1D_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix19by17,1,x17);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_D1D_y19_alpha_times_A19x17_times_D1D_x17_plus_D1D_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix19by17,1,new DoubleMatrix1D(x17));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A19x17_times_x17_plus_y19));
}

@Test
public void testDGEMV_y17_eq_alpha_times_A19x17T_times_x19_plus_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix19by17,1,x19);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_y17_eq_alpha_times_A19x17T_times_D1D_x19_plus_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix19by17,1,new DoubleMatrix1D(x19));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_D1D_y17_eq_alpha_times_A19x17T_times_x19_plus_D1D_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix19by17,1,x19);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A19x17T_times_x19_plus_y17));
}

@Test
public void testDGEMV_D1D_y17_eq_alpha_times_A19x17T_times_D1D_x19_plus_D1D_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix19by17,1,new DoubleMatrix1D(x19));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A19x17T_times_x19_plus_y17));
}

//* group 5:: A*x + beta*y or A^T*x + beta*y */
//5x5
@Test
public void testDGEMV_y5_eq_A5x5_times_x5_plus_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix5by5,beta,x5);
assertTrue(Arrays.equals(ycp,tmp_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_y5_eq_A5x5_times_D1D_x5_plus_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix5by5,beta,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(ycp,tmp_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x5_times_x5_plus_D1D_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix5by5,beta,x5);
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_A5x5_times_D1D_x5_plus_D1D_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix5by5,beta,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_y5_eq_A5x5T_times_x5_plus_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix5by5,beta,x5);
assertTrue(Arrays.equals(ycp,tmp_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_y5_eq_A5x5T_times_D1D_x5_plus_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix5by5,beta,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(ycp,tmp_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x5T_times_x5_plus_D1D_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix5by5,beta,x5);
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x5T_times_D1D_x5_plus_D1D_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix5by5,beta,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x5T_times_x5_plus_beta_times_y5));
}

//5x4
@Test
public void testDGEMV_y5_eq_A5x4_times_x4_plus_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix5by4,beta,x4);
assertTrue(Arrays.equals(ycp,tmp_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_y5_eq_A5x4_times_D1D_x4_plus_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix5by4,beta,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(ycp,tmp_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_A5x4_times_x4_plus_D1D_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix5by4,beta,x4);
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_A5x4_times_D1D_x4_plus_D1D_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix5by4,beta,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_y4_eq_A5x4T_times_x5_plus_beta_times_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix5by4,beta,x5);
assertTrue(Arrays.equals(ycp,tmp_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_y4_eq_A5x4T_times_D1D_x5_plus_beta_times_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix5by4,beta,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(ycp,tmp_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_D1D_y4_eq_A5x4T_times_x5_plus_D1D_beta_times_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix5by4,beta,x5);
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_D1D_y4_eq_A5x4T_times_D1D_x5_plus_D1D_beta_times_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix5by4,beta,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(tmp.getData(),tmp_A5x4T_times_x5_plus_beta_times_y4));
}

//4x5
@Test
public void testDGEMV_y4_eq_A4x5_times_x5_plus_beta_times_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix4by5,beta,x5);
assertTrue(Arrays.equals(ycp,tmp_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_y4_eq_A4x5_times_D1D_x5_plus_beta_times_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix4by5,beta,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(ycp,tmp_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_D1D_y4_eq_A4x5_times_x5_plus_D1D_beta_times_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix4by5,beta,x5);
assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_D1D_y4_A4x5_times_D1D_x5_plus_D1D_beta_times_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix4by5,beta,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_y5_eq_A4x5T_times_x4_plus_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix4by5,beta,x4);
assertTrue(Arrays.equals(ycp,tmp_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_y5_eq_A4x5T_times_D1D_x4_plus_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix4by5,beta,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(ycp,tmp_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_A4x5T_times_x4_plus_D1D_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix4by5,beta,x4);
assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_A4x5T_times_D1D_x4_plus_D1D_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix4by5,beta,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(tmp.getData(),tmp_A4x5T_times_x4_plus_beta_times_y5));
}

//1x1
@Test
public void testDGEMV_y1_eq_A1x1_times_x1_plus_beta_times_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix1by1,beta,x1);
assertTrue(Arrays.equals(ycp,tmp_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_y1_eq_A1x1_times_D1D_x1_plus_beta_times_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix1by1,beta,new DoubleMatrix1D(x1));
assertTrue(Arrays.equals(ycp,tmp_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_D1D_y1_eq_A1x1_times_x1_plus_D1D_beta_times_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix1by1,beta,x1);
assertTrue(Arrays.equals(tmp.getData(),tmp_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_D1D_y1_A1x1_times_D1D_x1_plus_D1D_beta_times_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix1by1,beta,new DoubleMatrix1D(x1));
assertTrue(Arrays.equals(tmp.getData(),tmp_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_y1_eq_A1x1T_times_x1_plus_beta_times_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix1by1,beta,x1);
assertTrue(Arrays.equals(ycp,tmp_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_y1_eq_A1x1T_times_D1D_x1_plus_beta_times_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix1by1,beta,new DoubleMatrix1D(x1));
assertTrue(Arrays.equals(ycp,tmp_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_D1D_y1_eq_A1x1T_times_x1_plus_D1D_beta_times_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix1by1,beta,x1);
assertTrue(Arrays.equals(tmp.getData(),tmp_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_D1D_y1_eq_A1x1T_times_D1D_x1_plus_D1D_beta_times_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix1by1,beta,new DoubleMatrix1D(x1));
assertTrue(Arrays.equals(tmp.getData(),tmp_A1x1T_times_x1_plus_beta_times_y1));
}

//19x17
@Test
public void testDGEMV_y19_eq_A19x17_times_x17_plus_beta_times_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix19by17,beta,x17);
assertTrue(Arrays.equals(ycp,tmp_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_y19_eq_A19x17_times_D1D_x17_plus_beta_times_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
BLAS2.dgemvInPlace(ycp,1,aMatrix19by17,beta,new DoubleMatrix1D(x17));
assertTrue(Arrays.equals(ycp,tmp_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_D1D_y19_eq_A19x17_times_x17_plus_D1D_beta_times_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix19by17,beta,x17);
assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_D1D_y19_A19x17_times_D1D_x17_plus_D1D_beta_times_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,1,aMatrix19by17,beta,new DoubleMatrix1D(x17));
assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_y17_eq_A19x17T_times_x19_plus_beta_times_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix19by17,beta,x19);
assertTrue(Arrays.equals(ycp,tmp_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_y17_eq_A19x17T_times_D1D_x19_plus_beta_times_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
BLAS2.dgemvInPlaceTransposed(ycp,1,aMatrix19by17,beta,new DoubleMatrix1D(x19));
assertTrue(Arrays.equals(ycp,tmp_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_D1D_y17_eq_A19x17T_times_x19_plus_D1D_beta_times_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix19by17,beta,x19);
assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_D1D_y17_eq_A19x17T_times_D1D_x19_plus_D1D_beta_times_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,1,aMatrix19by17,beta,new DoubleMatrix1D(x19));
assertTrue(Arrays.equals(tmp.getData(),tmp_A19x17T_times_x19_plus_beta_times_y17));
}

//** group 6:: y=alpha*A*x+beta*y OR y=alpha*A^T*x+beta*y
//5x5
@Test
public void testDGEMV_y5_eq_alpha_times_A5x5_times_x5_plus_beta_times_y5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by5,beta,x5);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A5x5_times_D1D_x5_plus_beta_times_y5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by5,beta,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_x5_plus_beta_times_D1D_y5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by5,beta,x5);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_D1D_x5_plus_beta_times_D1D_y5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by5,beta,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A5x5T_times_x5_plus_beta_times_y5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix5by5,beta,x5);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A5x5T_times_D1D_x5_plus_beta_times_y5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix5by5,beta,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_x5_plus_beta_times_D1D_y5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix5by5,beta,x5);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_D1D_x5_plus_beta_times_D1D_y5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix5by5,beta,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
}

//5x4
@Test
public void testDGEMV_y5_eq_alpha_times_A5x4_times_x4_plus_beta_times_y5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by4,beta,x4);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A5x4_times_D1D_x4_plus_beta_times_y5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix5by4,beta,new DoubleMatrix1D(x4));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x4_times_x4_plus_beta_times_D1D_y5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by4,beta,x4);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A5x4_times_D1D_x4_plus_beta_times_D1D_y5() {
  double [] ycp = new double[y5.length];
  System.arraycopy(y5, 0, ycp, 0, y5.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlace(tmp,alpha,aMatrix5by4,beta,new DoubleMatrix1D(x4));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x4_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_y4_eq_alpha_times_A5x4T_times_x5_plus_beta_times_y4() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix5by4,beta,x5);
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_y4_eq_alpha_times_A5x4T_times_D1D_x5_plus_beta_times_y4() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix5by4,beta,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(ycp,tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_D1D_y4_eq_alpha_times_A5x4T_times_x5_plus_beta_times_D1D_y4() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix5by4,beta,x5);
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_D1D_y4_eq_alpha_times_A5x4T_times_D1D_x5_plus_beta_times_D1D_y4() {
  double [] ycp = new double[y4.length];
  System.arraycopy(y4, 0, ycp, 0, y4.length);
  DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
  BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix5by4,beta,new DoubleMatrix1D(x5));
  assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A5x4T_times_x5_plus_beta_times_y4));
}


//4x5
@Test
public void testDGEMV_y4_eq_alpha_times_A4x5_times_x5_plus_beta_times_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix4by5,beta,x5);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_y4_eq_alpha_times_A4x5_times_D1D_x5_plus_beta_times_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix4by5,beta,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_D1D_y4_eq_alpha_times_A4x5_times_x5_plus_beta_times_D1D_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix4by5,beta,x5);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_D1D_y4_eq_alpha_times_A4x5_times_D1D_x5_plus_beta_times_D1D_y4() {
double [] ycp = new double[y4.length];
System.arraycopy(y4, 0, ycp, 0, y4.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix4by5,beta,new DoubleMatrix1D(x5));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A4x5_times_x5_plus_beta_times_y4));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A4x5T_times_x4_plus_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix4by5,beta,x4);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_y5_eq_alpha_times_A4x5T_times_D1D_x4_plus_beta_times_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix4by5,beta,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A4x5T_times_x4_plus_beta_times_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix4by5,beta,x4);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5));
}

@Test
public void testDGEMV_D1D_y5_eq_alpha_times_A4x5T_times_D1D_x4_plus_beta_times_D1D_y5() {
double [] ycp = new double[y5.length];
System.arraycopy(y5, 0, ycp, 0, y5.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix4by5,beta,new DoubleMatrix1D(x4));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A4x5T_times_x4_plus_beta_times_y5));
}


//1x1
@Test
public void testDGEMV_y1_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix1by1,beta,x1);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_y1_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix1by1,beta,new DoubleMatrix1D(x1));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_x1_plus_beta_times_D1D_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix1by1,beta,x1);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_D1D_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix1by1,beta,new DoubleMatrix1D(x1));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_y1_eq_alpha_times_A1x1T_times_x1_plus_beta_times_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix1by1,beta,x1);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_y1_eq_alpha_times_A1x1T_times_D1D_x1_plus_beta_times_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix1by1,beta,new DoubleMatrix1D(x1));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_x1_plus_beta_times_D1D_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix1by1,beta,x1);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
}

@Test
public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_D1D_x1_plus_beta_times_D1D_y1() {
double [] ycp = new double[y1.length];
System.arraycopy(y1, 0, ycp, 0, y1.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix1by1,beta,new DoubleMatrix1D(x1));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
}


//19x17
@Test
public void testDGEMV_y19_eq_alpha_times_A19x17_times_x17_plus_beta_times_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix19by17,beta,x17);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_y19_eq_alpha_times_A19x17_times_D1D_x17_plus_beta_times_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
BLAS2.dgemvInPlace(ycp,alpha,aMatrix19by17,beta,new DoubleMatrix1D(x17));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_D1D_y19_eq_alpha_times_A19x17_times_x17_plus_beta_times_D1D_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix19by17,beta,x17);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_D1D_y19_eq_alpha_times_A19x17_times_D1D_x17_plus_beta_times_D1D_y19() {
double [] ycp = new double[y19.length];
System.arraycopy(y19, 0, ycp, 0, y19.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlace(tmp,alpha,aMatrix19by17,beta,new DoubleMatrix1D(x17));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A19x17_times_x17_plus_beta_times_y19));
}

@Test
public void testDGEMV_y17_eq_alpha_times_A19x17T_times_x19_plus_beta_times_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix19by17,beta,x19);
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_y17_eq_alpha_times_A19x17T_times_D1D_x19_plus_beta_times_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
BLAS2.dgemvInPlaceTransposed(ycp,alpha,aMatrix19by17,beta,new DoubleMatrix1D(x19));
assertTrue(Arrays.equals(ycp,tmp_alpha_times_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_D1D_y17_eq_alpha_times_A19x17T_times_x19_plus_beta_times_D1D_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix19by17,beta,x19);
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A19x17T_times_x19_plus_beta_times_y17));
}

@Test
public void testDGEMV_D1D_y17_eq_alpha_times_A19x17T_times_D1D_x19_plus_beta_times_D1D_y17() {
double [] ycp = new double[y17.length];
System.arraycopy(y17, 0, ycp, 0, y17.length);
DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
BLAS2.dgemvInPlaceTransposed(tmp,alpha,aMatrix19by17,beta,new DoubleMatrix1D(x19));
assertTrue(Arrays.equals(tmp.getData(),tmp_alpha_times_A19x17T_times_x19_plus_beta_times_y17));
}

}
