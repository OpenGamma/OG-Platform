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
import com.opengamma.maths.lowlevelapi.datatypes.primitive.DenseSymmetricMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.BLAS2;

/**
 *
 */
public class BLAS2DGEMVDenseSymmetricMatrixTest {

  // try 5x5 and 19x19
  double[][] A5by5 = { {1, 2, 3, 4, 5 }, {2, 7, 8, 9, 10 }, {3, 8, 13, 14, 15 }, {4, 9, 14, 19, 20 }, {5, 10, 15, 20, 25 } };
  double[][] A19by19 =
    {
      {    1,    2,    3,    4,    5,    6,    7,    8,    9,   10,   11,   12,   13,   14,   15,   16,   17,   18,   19},
      {    2,   21,   22,   23,   24,   25,   26,   27,   28,   29,   30,   31,   32,   33,   34,   35,   36,   37,   38},
      {    3,   22,   41,   42,   43,   44,   45,   46,   47,   48,   49,   50,   51,   52,   53,   54,   55,   56,   57},
      {    4,   23,   42,   61,   62,   63,   64,   65,   66,   67,   68,   69,   70,   71,   72,   73,   74,   75,   76},
      {    5,   24,   43,   62,   81,   82,   83,   84,   85,   86,   87,   88,   89,   90,   91,   92,   93,   94,   95},
      {    6,   25,   44,   63,   82,  101,  102,  103,  104,  105,  106,  107,  108,  109,  110,  111,  112,  113,  114},
      {    7,   26,   45,   64,   83,  102,  121,  122,  123,  124,  125,  126,  127,  128,  129,  130,  131,  132,  133},
      {    8,   27,   46,   65,   84,  103,  122,  141,  142,  143,  144,  145,  146,  147,  148,  149,  150,  151,  152},
      {    9,   28,   47,   66,   85,  104,  123,  142,  161,  162,  163,  164,  165,  166,  167,  168,  169,  170,  171},
      {   10,   29,   48,   67,   86,  105,  124,  143,  162,  181,  182,  183,  184,  185,  186,  187,  188,  189,  190},
      {   11,   30,   49,   68,   87,  106,  125,  144,  163,  182,  201,  202,  203,  204,  205,  206,  207,  208,  209},
      {   12,   31,   50,   69,   88,  107,  126,  145,  164,  183,  202,  221,  222,  223,  224,  225,  226,  227,  228},
      {   13,   32,   51,   70,   89,  108,  127,  146,  165,  184,  203,  222,  241,  242,  243,  244,  245,  246,  247},
      {   14,   33,   52,   71,   90,  109,  128,  147,  166,  185,  204,  223,  242,  261,  262,  263,  264,  265,  266},
      {   15,   34,   53,   72,   91,  110,  129,  148,  167,  186,  205,  224,  243,  262,  281,  282,  283,  284,  285},
      {   16,   35,   54,   73,   92,  111,  130,  149,  168,  187,  206,  225,  244,  263,  282,  301,  302,  303,  304},
      {   17,   36,   55,   74,   93,  112,  131,  150,  169,  188,  207,  226,  245,  264,  283,  302,  321,  322,  323},
      {   18,   37,   56,   75,   94,  113,  132,  151,  170,  189,  208,  227,  246,  265,  284,  303,  322,  341,  342},
      {   19,   38,   57,   76,   95,  114,  133,  152,  171,  190,  209,  228,  247,  266,  285,  304,  323,  342,  361}
  };

  double[] x5 = {1, 2, 3, 4, 5 };
  double[] y5 = {10, 20, 30, 40, 50 };

  double[] x19 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19};
  double[] y19 = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190 };

  double alpha = 7d;
  double beta = -3d;

  // single element data
  double[][] A1by1 = {{1 } };
  double[] x1 = {2 };
  double[] y1 = {10 };

  // casts to objects for tests
  DenseSymmetricMatrix aMatrix1by1 = new DenseSymmetricMatrix(A1by1);
  DenseSymmetricMatrix aMatrix5by5 = new DenseSymmetricMatrix(A5by5);
  DenseSymmetricMatrix aMatrix19by19 = new DenseSymmetricMatrix(A19by19);

  //1x1 answers
  double[] tmp_A1x1_times_x1 = {2 }; //A*x
  double[] tmp_A1x1T_times_x1 = {2 }; //A^T*x
  double[] tmp_alpha_times_A1x1_times_x1 = {14 }; //alpha*A*x
  double[] tmp_alpha_times_A1x1T_times_x1 = {14 }; //alpha*A^T*x
  double[] tmp_A1x1_times_x1_plus_y1 = {12 }; //A*x+y
  double[] tmp_A1x1T_times_x1_plus_y1 = {12 }; //A^T*x+y
  double[] tmp_alpha_times_A1x1_times_x1_plus_y1 = {24 }; //alpha*A*x+y
  double[] tmp_alpha_times_A1x1T_times_x1_plus_y1 = {24 }; //alpha*A^T*x+y
  double[] tmp_A1x1_times_x1_plus_beta_times_y1 = {-28 }; //A*x+beta*y
  double[] tmp_A1x1T_times_x1_plus_beta_times_y1 = {-28 }; //A^T*x+beta*y
  double[] tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1 = {-16 }; //alpha*A*x+beta*y
  double[] tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1 = {-16 }; //alpha*A^T*x+beta*y

  // 5x5 answers
  double[] tmp_A5x5_times_x5 = {55, 126, 189, 240, 275};
  double[] tmp_A5x5T_times_x5 = {55, 126, 189, 240, 275};
  double[] tmp_alpha_times_A5x5_times_x5 = {385, 882, 1323, 1680, 1925};
  double[] tmp_alpha_times_A5x5T_times_x5 = {385, 882, 1323, 1680, 1925};
  double[] tmp_A5x5_times_x5_plus_y5 = {65, 146, 219, 280, 325};
  double[] tmp_A5x5T_times_x5_plus_y5 = {65, 146, 219, 280, 325};
  double[] tmp_alpha_times_A5x5_times_x5_plus_y5 = {395, 902, 1353, 1720, 1975};
  double[] tmp_alpha_times_A5x5T_times_x5_plus_y5 = {395, 902, 1353, 1720, 1975};
  double[] tmp_A5x5_times_x5_plus_beta_times_y5 = {25, 66, 99, 120, 125};
  double[] tmp_A5x5T_times_x5_plus_beta_times_y5 = {25, 66, 99, 120, 125};
  double[] tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5 = {355, 822, 1233, 1560, 1775};
  double[] tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5 = {355, 822, 1233, 1560, 1775};


  // 19x19 answers
  double[] tmp_A19x19_times_x19 = {2470, 6062, 9618, 13120, 16550, 19890, 23122, 26228, 29190, 31990, 34610, 37032, 39238, 41210, 42930, 44380, 45542, 46398, 46930};
  double[] tmp_A19x19T_times_x19 = {2470, 6062, 9618, 13120, 16550, 19890, 23122, 26228, 29190, 31990, 34610, 37032, 39238, 41210, 42930, 44380, 45542, 46398, 46930};
  double[] tmp_alpha_times_A19x19_times_x19 = {17290, 42434, 67326, 91840, 115850, 139230, 161854, 183596, 204330, 223930, 242270, 259224, 274666, 288470, 300510, 310660, 318794, 324786, 328510};
  double[] tmp_alpha_times_A19x19T_times_x19 = {17290, 42434, 67326, 91840, 115850, 139230, 161854, 183596, 204330, 223930, 242270, 259224, 274666, 288470, 300510, 310660, 318794, 324786, 328510};
  double[] tmp_A19x19_times_x19_plus_y19 = {2480, 6082, 9648, 13160, 16600, 19950, 23192, 26308, 29280, 32090, 34720, 37152, 39368, 41350, 43080, 44540, 45712, 46578, 47120};
  double[] tmp_A19x19T_times_x19_plus_y19 = {2480, 6082, 9648, 13160, 16600, 19950, 23192, 26308, 29280, 32090, 34720, 37152, 39368, 41350, 43080, 44540, 45712, 46578, 47120};
  double[] tmp_alpha_times_A19x19_times_x19_plus_y19 = {17300, 42454, 67356, 91880, 115900, 139290, 161924, 183676, 204420, 224030, 242380, 259344, 274796, 288610, 300660, 310820, 318964, 324966, 328700};
  double[] tmp_alpha_times_A19x19T_times_x19_plus_y19 = {17300, 42454, 67356, 91880, 115900, 139290, 161924, 183676, 204420, 224030, 242380, 259344, 274796, 288610, 300660, 310820, 318964, 324966, 328700};
  double[] tmp_A19x19_times_x19_plus_beta_times_y19 = {2440, 6002, 9528, 13000, 16400, 19710, 22912, 25988, 28920, 31690, 34280, 36672, 38848, 40790, 42480, 43900, 45032, 45858, 46360};
  double[] tmp_A19x19T_times_x19_plus_beta_times_y19 = {2440, 6002, 9528, 13000, 16400, 19710, 22912, 25988, 28920, 31690, 34280, 36672, 38848, 40790, 42480, 43900, 45032, 45858, 46360};
  double[] tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19 = {17260, 42374, 67236, 91720, 115700, 139050, 161644, 183356, 204060, 223630, 241940, 258864, 274276, 288050, 300060, 310180, 318284, 324246, 327940};
  double[] tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19 = {17260, 42374, 67236, 91720, 115700, 139050, 161644, 183356, 204060, 223630, 241940, 258864, 274276, 288050, 300060, 310180, 318284, 324246, 327940};


  /**
   * Test input catchers
   */

  /* Normal 2 inputs */
  @Test(expectedExceptions = AssertionError.class)
  public void testInputCatcherMatrix() {
    DenseSymmetricMatrix NullMat = null;
    BLAS2.dgemvInputSanityChecker(NullMat, x5);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testInputCatcherVector() {
    double[] aVector = null;
    BLAS2.dgemvInputSanityChecker(aMatrix5by5, aVector);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testInputCatcherSizeWillNotCommute() {
    BLAS2.dgemvInputSanityChecker(aMatrix5by5, x19);
  }

  /* Normal 3 inputs */
  @Test(expectedExceptions = AssertionError.class)
  public void testInputCatcherMatrix3inputs() {
    DenseSymmetricMatrix NullMat = null;
    BLAS2.dgemvInputSanityChecker(NullMat, x5, y5);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testInputCatcherVectorBadVectorGood() {
    double[] aVector = null;
    BLAS2.dgemvInputSanityChecker(aMatrix5by5, aVector, x5);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testInputCatcherVectorGoodVectorBad() {
    double[] aVector = null;
    BLAS2.dgemvInputSanityChecker(aMatrix5by5, x5, aVector);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testInputCatcherSizeWillNotCommuteWithIntermediteVector() {
    BLAS2.dgemvInputSanityChecker(aMatrix5by5, x19, y5);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testInputCatcherSizeWillNotCommuteWithReturnVector() {
    BLAS2.dgemvInputSanityChecker(aMatrix5by5, x5, y19);
  }

  /* Transpose 2 inputs */
  @Test(expectedExceptions = AssertionError.class)
  public void testTransposeInputCatcherMatrix() {
    DenseSymmetricMatrix NullMat = null;
    BLAS2.dgemvInputSanityCheckerTransposed(NullMat, x5);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testTransposeInputCatcherVector() {
    double[] aVector = null;
    BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by5, aVector);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testTransposeInputCatcherSizeWillNotCommute() {
    BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by5, x19);
  }

  /* Transpose 3 inputs */
  @Test(expectedExceptions = AssertionError.class)
  public void testTransposeInputCatcherMatrix3inputs() {
    DenseSymmetricMatrix NullMat = null;
    BLAS2.dgemvInputSanityCheckerTransposed(NullMat, x5, y5);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testTransposeInputCatcherVectorBadVectorGood() {
    double[] aVector = null;
    BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by5, aVector, x5);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testTransposeInputCatcherVectorGoodVectorBad() {
    double[] aVector = null;
    BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by5, x5, aVector);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testTransposeInputCatcherSizeWillNotCommuteWithIntermediteVector() {
    BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by5, x19, y5);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testTransposeInputCatcherSizeWillNotCommuteWithReturnVector() {
    BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by5, x5, y19);
  }

  /**
   * DGEMV on FULL Matrices
   */
  // stateless manipulators

  //** group 1:: A*x OR A^T*x

  // test 5x5
  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5), tmp_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5)), tmp_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5T_times_x5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, x5), tmp_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5T_times_D1D_x5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, new DoubleMatrix1D(x5)), tmp_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, BLAS2.orientation.normal), tmp_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.normal), tmp_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5T_times_x5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, BLAS2.orientation.transposed), tmp_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.transposed), tmp_A5x5T_times_x5));
  }

  //test 1x1
  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1), tmp_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1)), tmp_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1T_times_x1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, x1), tmp_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1T_times_D1D_x1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, new DoubleMatrix1D(x1)), tmp_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, BLAS2.orientation.normal), tmp_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.normal), tmp_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, BLAS2.orientation.transposed), tmp_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.transposed), tmp_A1x1T_times_x1));
  }

  //test 19x19, should trip loop unwindings
  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19), tmp_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19)), tmp_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19T_times_x19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, x19), tmp_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19T_times_D1D_x19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, new DoubleMatrix1D(x19)), tmp_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, BLAS2.orientation.normal), tmp_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.normal), tmp_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, BLAS2.orientation.transposed), tmp_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.transposed), tmp_A19x19T_times_x19));
  }

  //** group 2:: alpha*A*x OR alpha*A^T*x
  //test 5x5
  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5), tmp_alpha_times_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5)), tmp_alpha_times_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, x5), tmp_alpha_times_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, new DoubleMatrix1D(x5)), tmp_alpha_times_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_A5x5_times_x5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_A5x5_times_x5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5));
  }


  //test 1x1
  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1), tmp_alpha_times_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1)), tmp_alpha_times_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, x1), tmp_alpha_times_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_ans_alpha_times_eq_A1x1T_times_D1D_x1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, new DoubleMatrix1D(x1)), tmp_alpha_times_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1));
  }

  //test 19x19 should trip loop unwindings
  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19), tmp_alpha_times_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19)), tmp_alpha_times_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19T_times_x19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, x19), tmp_alpha_times_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_ans_alpha_times_eq_A19x19T_times_D1D_x19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, new DoubleMatrix1D(x19)), tmp_alpha_times_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19));
  }

  //** group 3:: A*x+y OR A^T*x+y

  //test 5x5
  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5_plus_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, y5), tmp_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), y5), tmp_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5_plus_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, new DoubleMatrix1D(y5)), tmp_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5)), tmp_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5T_times_x5_plus_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, x5, y5), tmp_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_plus_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, new DoubleMatrix1D(x5), y5), tmp_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5T_times_x5_plus_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, x5, new DoubleMatrix1D(y5)), tmp_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_plus_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5)), tmp_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5_plus_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, y5, BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), y5, BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5_plus_D1D_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_D1D_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5_plus_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, y5, BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), y5, BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5_plus_D1D_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_D1D_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_y5));
  }

  //test 1x1
  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_plus_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, y1), tmp_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), y1), tmp_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_plus_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, new DoubleMatrix1D(y1)), tmp_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1)), tmp_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1T_times_x1_plus_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, x1, y1), tmp_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1T_times_D1D_x1_plus_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, new DoubleMatrix1D(x1), y1), tmp_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1T_times_x1_plus_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, x1, new DoubleMatrix1D(y1)), tmp_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1T_times_D1D_x1_plus_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1)), tmp_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_plus_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, y1, BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), y1, BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_plus_D1D_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_plus_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, y1, BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), y1, BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_plus_D1D_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_y1));
  }

  //test 19x19 should trip loop unwindings
  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_plus_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, y19), tmp_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), y19), tmp_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_plus_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, new DoubleMatrix1D(y19)), tmp_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19)), tmp_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19T_times_x19_plus_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, x19, y19), tmp_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19T_times_D1D_x19_plus_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, new DoubleMatrix1D(x19), y19), tmp_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19T_times_x19_plus_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, x19, new DoubleMatrix1D(y19)), tmp_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19T_times_D1D_x19_plus_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19)), tmp_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_plus_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, y19, BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), y19, BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_plus_y19_D1D_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_D1D_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_plus_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, y19, BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), y19, BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_plus_y19_D1D_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_D1D_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_y19));
  }

  //** group 4:: alpha*A*x+y OR alpha*A^T*x+y
  //5x5
  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, y5), tmp_alpha_times_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), y5), tmp_alpha_times_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5_plus_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, x5, y5), tmp_alpha_times_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5_plus_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, new DoubleMatrix1D(x5), y5), tmp_alpha_times_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5_plus_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, x5, new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5_plus_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, y5, BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), y5, BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5_D1D_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_D1D_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, y5, BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), y5, BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5_D1D_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_D1D_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_y5));
  }

  //1x1
  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, y1), tmp_alpha_times_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), y1), tmp_alpha_times_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1_plus_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, x1, y1), tmp_alpha_times_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1T_times_D1D_x1_plus_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, new DoubleMatrix1D(x1), y1), tmp_alpha_times_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1_plus_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, x1, new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1T_times_D1D_x1_plus_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, y1, BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), y1, BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_D1D_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, y1, BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), y1, BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_D1D_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_y1));
  }

  //19x19
  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, y19), tmp_alpha_times_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), y19), tmp_alpha_times_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19T_times_x19_plus_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, x19, y19), tmp_alpha_times_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19T_times_D1D_x19_plus_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, new DoubleMatrix1D(x19), y19), tmp_alpha_times_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19T_times_x19_plus_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, x19, new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19T_times_D1D_x19_plus_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, y19, BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), y19, BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_D1D_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_D1D_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, y19, BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x19_plus_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), y19, BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x19_plus_y19_D1D_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_D1D_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_y19));
  }

  //** group 5:: A*x+beta*y OR A^T*x+beta*y
  //5x5
  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, beta, y5), tmp_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), beta, y5), tmp_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, beta, new DoubleMatrix1D(y5)), tmp_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5)), tmp_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5T_times_x5_plus_beta_times_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, x5, beta, y5), tmp_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_plus_beta_times_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, new DoubleMatrix1D(x5), beta, y5), tmp_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5T_times_x5_plus_beta_times_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, x5, beta, new DoubleMatrix1D(y5)), tmp_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_plus_beta_times_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5)), tmp_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, beta, y5, BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), beta, y5, BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_D1D_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, beta, new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_D1D_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, beta, y5, BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), beta, y5, BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_y5_D1D_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, beta, new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_D1D_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_beta_times_y5));
  }

  //1x1
  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, beta, y1), tmp_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), beta, y1), tmp_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, beta, new DoubleMatrix1D(y1)), tmp_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1)), tmp_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1T_times_x1_plus_beta_times_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, x1, beta, y1), tmp_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1T_times_D1D_x1_plus_beta_times_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, new DoubleMatrix1D(x1), beta, y1), tmp_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1T_times_x1_plus_beta_times_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, x1, beta, new DoubleMatrix1D(y1)), tmp_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1T_times_D1D_x1_plus_beta_times_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1)), tmp_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, beta, y1, BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), beta, y1, BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_y1_D1D_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, beta, new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, beta, y1, BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), beta, y1, BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, beta, new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_beta_times_y1));
  }

  //19x19
  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_plus_beta_times_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, beta, y19), tmp_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_beta_times_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), beta, y19), tmp_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_plus_beta_times_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, beta, new DoubleMatrix1D(y19)), tmp_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_beta_times_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19)), tmp_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19T_times_x19_plus_beta_times_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, x19, beta, y19), tmp_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19T_times_D1D_x19_plus_beta_times_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, new DoubleMatrix1D(x19), beta, y19), tmp_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19T_times_x19_plus_beta_times_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, x19, beta, new DoubleMatrix1D(y19)), tmp_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19T_times_D1D_x19_plus_beta_times_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19)), tmp_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_plus_beta_times_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, beta, y19, BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_beta_times_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), beta, y19, BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_plus_beta_times_D1D_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, beta, new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_beta_times_D1D_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_plus_beta_times_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, beta, y19, BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_beta_times_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), beta, y19, BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_x19_plus_beta_times_D1D_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, beta, new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_beta_times_D1D_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_beta_times_y19));
  }

  //** group 6:: alpha*A*x+beta*y OR alpha*A^T*x+beta*y

  //5x5
  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_beta_times_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, beta, y5), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_beta_times_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, y5), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_beta_times_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, beta, new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_beta_times_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5_plus_beta_times_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, x5, beta, y5), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5_plus_beta_times_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, y5), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5_plus_beta_times_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, x5, beta, new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5_plus_beta_times_D1D_y5() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_A5x5_times_x5_plus_beta_times_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, beta, y5, BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_plus_beta_times_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, y5, BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_A5x5_times_x5_plus_beta_times_D1D_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, beta, new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_plus_beta_times_D1D_y5_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_A5x5_times_x5_plus_beta_times_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, beta, y5, BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_plus_beta_times_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, y5, BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_A5x5_times_x5_plus_beta_times_D1D_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, beta, new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_plus_beta_times_D1D_y5_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
  }

  //1x1
  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, beta, y1), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, y1), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, beta, new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1_plus_beta_times_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, x1, beta, y1), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1T_times_D1D_x1_plus_beta_times_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, y1), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1_plus_beta_times_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, x1, beta, new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1T_times_D1D_x1_plus_beta_times_D1D_y1() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, beta, y1, BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, y1, BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1_D1D_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, beta, new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, beta, y1, BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, y1, BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, beta, new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
  }

  //19x19
  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_beta_times_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, beta, y19), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, y19), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_beta_times_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, beta, new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19T_times_x19_plus_beta_times_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, x19, beta, y19), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19T_times_D1D_x19_plus_beta_times_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, y19), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19T_times_x19_plus_beta_times_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, x19, beta, new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19T_times_D1D_x19_plus_beta_times_D1D_y19() {
    assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_beta_times_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, beta, y19, BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, y19, BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_beta_times_D1D_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, beta, new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_D1D_y19_interfaced_normal() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_beta_times_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, beta, y19, BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, y19, BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_beta_times_D1D_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, beta, new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_D1D_y19_interfaced_transposed() {
    assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19), BLAS2.orientation.transposed),
        tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
  }

  // test the inplaces
  //** group 1:: A*x OR A^T*x

  //5x5
  @Test
  public void testDGEMV_y5_eq_A5x5_times_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, aMatrix5by5, x5);
    assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_y5_eq_A5x5_times_D1D_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, aMatrix5by5, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5_times_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, aMatrix5by5, x5);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5_times_D1D_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, aMatrix5by5, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_y5_eq_A5x5T_times_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlaceTransposed(ycp, aMatrix5by5, x5);
    assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_y5_eq_A5x5T_times_D1D_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlaceTransposed(ycp, aMatrix5by5, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5T_times_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, aMatrix5by5, x5);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5T_times_D1D_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, aMatrix5by5, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_y5_eq_A5x5_times_x5_interfaced_normal() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, aMatrix5by5, x5, BLAS2.orientation.normal);
    assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5_times_x5_interfaced_normal() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(y5);
    BLAS2.dgemvInPlace(tmp, aMatrix5by5, x5, BLAS2.orientation.normal);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_y5_eq_A5x5_times_D1D_x5_interfaced_normal() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.normal);
    assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5_times_D1D_x5_interfaced_normal() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.normal);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_y5_eq_A5x5_times_x5_interfaced_transposed() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, aMatrix5by5, x5, BLAS2.orientation.transposed);
    assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5_times_x5_interfaced_transposed() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(y5);
    BLAS2.dgemvInPlace(tmp, aMatrix5by5, x5, BLAS2.orientation.transposed);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_y5_eq_A5x5_times_D1D_x5_interfaced_transposed() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.transposed);
    assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5_times_D1D_x5_interfaced_transposed() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.transposed);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5));
  }

  //1x1
  @Test
  public void testDGEMV_y1_eq_A1x1_times_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, aMatrix1by1, x1);
    assertTrue(Arrays.equals(ycp, tmp_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_y1_eq_A1x1_times_D1D_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, aMatrix1by1, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(ycp, tmp_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_A1x1_times_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, aMatrix1by1, x1);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_A1x1_times_D1D_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, aMatrix1by1, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_y1_eq_A1x1T_times_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlaceTransposed(ycp, aMatrix1by1, x1);
    assertTrue(Arrays.equals(ycp, tmp_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_y1_eq_A1x1T_times_D1D_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlaceTransposed(ycp, aMatrix1by1, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(ycp, tmp_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_A1x1T_times_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, aMatrix1by1, x1);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_A1x1T_times_D1D_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, aMatrix1by1, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_y1_eq_A1x1_times_x1_interfaced_normal() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, aMatrix1by1, x1, BLAS2.orientation.normal);
    assertTrue(Arrays.equals(ycp, tmp_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_A1x1_times_x1_interfaced_normal() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(y1);
    BLAS2.dgemvInPlace(tmp, aMatrix1by1, x1, BLAS2.orientation.normal);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_y1_eq_A1x1_times_D1D_x1_interfaced_normal() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.normal);
    assertTrue(Arrays.equals(ycp, tmp_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_A1x1_times_D1D_x1_interfaced_normal() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.normal);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_y1_eq_A1x1_times_x1_interfaced_transposed() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, aMatrix1by1, x1, BLAS2.orientation.transposed);
    assertTrue(Arrays.equals(ycp, tmp_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_A1x1_times_x1_interfaced_transposed() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(y1);
    BLAS2.dgemvInPlace(tmp, aMatrix1by1, x1, BLAS2.orientation.transposed);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_y1_eq_A1x1_times_D1D_x1_interfaced_transposed() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.transposed);
    assertTrue(Arrays.equals(ycp, tmp_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_A1x1_times_D1D_x1_interfaced_transposed() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.transposed);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1T_times_x1));
  }

  //19x19
  @Test
  public void testDGEMV_y19_eq_A19x19_times_x19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, aMatrix19by19, x19);
    assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_y19_eq_A19x19_times_D1D_x1() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, aMatrix19by19, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19_times_x1() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, aMatrix19by19, x19);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19_times_D1D_x1() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, aMatrix19by19, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_y19_eq_A19x19T_times_x19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlaceTransposed(ycp, aMatrix19by19, x19);
    assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_y19eq_A19x19T_times_D1D_x19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlaceTransposed(ycp, aMatrix19by19, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19T_times_x19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, aMatrix19by19, x19);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19T_times_D1D_x19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, aMatrix19by19, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_y19_eq_A19x19_times_x19_interfaced_normal() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, aMatrix19by19, x19, BLAS2.orientation.normal);
    assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19_times_x19_interfaced_normal() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(y19);
    BLAS2.dgemvInPlace(tmp, aMatrix19by19, x19, BLAS2.orientation.normal);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_y19_eq_A19x19_times_D1D_x19_interfaced_normal() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.normal);
    assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19_times_D1D_x19_interfaced_normal() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.normal);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_y19_eq_A19x19_times_x19_interfaced_transposed() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, aMatrix19by19, x19, BLAS2.orientation.transposed);
    assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19_times_x19_interfaced_transposed() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(y19);
    BLAS2.dgemvInPlace(tmp, aMatrix19by19, x19, BLAS2.orientation.transposed);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_y19_eq_A19x19_times_D1D_x19_interfaced_transposed() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.transposed);
    assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19_times_D1D_x19_interfaced_transposed() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.transposed);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19));
  }

  //** group 2:: y=alpha*A*x OR y=alpha*A^T*x
  //5x5
  @Test
  public void testDGEMV_y5_eq_alpha_times_A5x5_times_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix5by5, 0, x5);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_y5_eq_alpha_times_A5x5_times_D1D_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix5by5, 0, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix5by5, 0, x5);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_D1D_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix5by5, 0, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5_times_x5));
  }

  @Test
  public void testDGEMV_y5_eq_alpha_times_A5x5T_times_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix5by5, 0, x5);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_y5_eq_alpha_times_A5x5T_times_D1D_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix5by5, 0, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix5by5, 0, x5);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5T_times_x5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_D1D_x5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix5by5, 0, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5T_times_x5));
  }

  //1x1
  @Test
  public void testDGEMV_y1_eq_alpha_times_A1x1_times_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix1by1, 0, x1);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_y1_eq_alpha_times_A1x1_times_D1D_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix1by1, 0, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix1by1, 0, x1);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_D1D_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix1by1, 0, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1_times_x1));
  }

  @Test
  public void testDGEMV_y1_eq_alpha_times_A1x1T_times_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix1by1, 0, x1);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_y1_eq_alpha_times_A1x1T_times_D1D_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix1by1, 0, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_alpha_times_A1xT_times_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix1by1, 0, x1);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1T_times_x1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_D1D_x1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix1by1, 0, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1T_times_x1));
  }

  //19x19
  @Test
  public void testDGEMV_y19_eq_alpha_times_A19x19_times_x19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix19by19, 0, x19);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_y19_eq_alpha_times_A19x5_times_D1D_x19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix19by19, 0, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_alpha_times_A19x19_times_x19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix19by19, 0, x19);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_alpha_times_A19x19_times_D1D_x19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix19by19, 0, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19_times_x19));
  }

  @Test
  public void testDGEMV_y19_eq_alpha_times_A19x19T_times_x19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix19by19, 0, x19);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_y19_eq_alpha_times_A19x19T_times_D1D_x19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix19by19, 0, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_alpha_times_A19x19T_times_x19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix19by19, 0, x19);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19T_times_x19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_alpha_times_A19x19T_times_D1D_x19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix19by19, 0, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19T_times_x19));
  }

  //** group 3:: A*x+y OR A^T*x+y
  //5x5
  @Test
  public void testDGEMV_y5_eq_A5x5_times_x5_plus_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, 1, aMatrix5by5, 1, x5);
    assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_y5_eq_A5x5_times_D1D_x5_plus_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, 1, aMatrix5by5, 1, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5_times_x5_plus_D1D_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, 1, aMatrix5by5, 1, x5);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_A5x5_times_D1D_x5_plus_D1D_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, 1, aMatrix5by5, 1, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_y5_eq_A5x5T_times_x5_plus_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix5by5, 1, x5);
    assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_y5_eq_A5x5T_times_D1D_x5_plus_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix5by5, 1, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5T_times_x5_plus_D1D_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix5by5, 1, x5);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5T_times_D1D_x5_plus_D1D_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix5by5, 1, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5_plus_y5));
  }

  //19x19
  @Test
  public void testDGEMV_y19_eq_A19x19_times_x19_plus_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, 1, aMatrix19by19, 1, x19);
    assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_y19_eq_A19x19_times_D1D_x19_plus_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, 1, aMatrix19by19, 1, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19_times_x19_plus_D1D_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, 1, aMatrix19by19, 1, x19);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19_times_D1D_x19_plus_D1D_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, 1, aMatrix19by19, 1, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_y19_eq_A19x19T_times_x19_plus_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix19by19, 1, x19);
    assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_y19_eq_A19x19T_times_D1D_x19_plus_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix19by19, 1, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19T_times_x19_plus_D1D_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix19by19, 1, x19);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19T_times_D1D_x19_plus_D1D_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix19by19, 1, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19_plus_y19));
  }

  //** group 4:: alpha*A*x+y OR alpha*A^T*x+y
  //5x5
  @Test
  public void testDGEMV_y5_eq_alpha_times_A5x5_times_x5_plus_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix5by5, 1, x5);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_y5_eq_alpha_times_A5x5_times_D1D_x5_plus_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix5by5, 1, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_x5_plus_D1D_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix5by5, 1, x5);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_alpha_times_A5x5_times_D1D_x5_plus_D1D_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix5by5, 1, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_y5_eq_alpha_times_A5x5T_times_x5_plus_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix5by5, 1, x5);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_y5_eq_alpha_times_A5x5T_times_D1D_x5_plus_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix5by5, 1, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_x5_plus_D1D_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix5by5, 1, x5);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5T_times_x5_plus_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_D1D_x5_plus_D1D_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix5by5, 1, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5T_times_x5_plus_y5));
  }

   //1x1
  @Test
  public void testDGEMV_y1_eq_alpha_times_A1x1_times_x1_plus_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix1by1, 1, x1);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_y1_eq_alpha_times_A1x1_times_D1D_x1_plus_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix1by1, 1, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_x1_plus_D1D_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix1by1, 1, x1);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_D1D_y1_alpha_times_A1x1_times_D1D_x1_plus_D1D_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix1by1, 1, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_y1_eq_alpha_times_A1x1T_times_x1_plus_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix1by1, 1, x1);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_y1_eq_alpha_times_A1x1T_times_D1D_x1_plus_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix1by1, 1, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_x1_plus_D1D_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix1by1, 1, x1);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1T_times_x1_plus_y1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_D1D_x1_plus_D1D_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix1by1, 1, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1T_times_x1_plus_y1));
  }

  //19x19
  @Test
  public void testDGEMV_y19_eq_alpha_times_A19x19_times_x19_plus_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix19by19, 1, x19);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_y19_eq_alpha_times_A19x19_times_D1D_x19_plus_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix19by19, 1, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_alpha_times_A19x19_times_x19_plus_D1D_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix19by19, 1, x19);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_alpha_times_A19x19_times_D1D_x19_plus_D1D_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix19by19, 1, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_y19_eq_alpha_times_A19x19T_times_x19_plus_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix19by19, 1, x19);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_y19_eq_alpha_times_A19x19T_times_D1D_x19_plus_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix19by19, 1, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_alpha_times_A19x19T_times_x19_plus_D1D_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix19by19, 1, x19);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19T_times_x19_plus_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_alpha_times_A19x19T_times_D1D_x19_plus_D1D_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix19by19, 1, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19T_times_x19_plus_y19));
  }

  //* group 5:: A*x + beta*y or A^T*x + beta*y */
  //5x5
  @Test
  public void testDGEMV_y5_eq_A5x5_times_x5_plus_beta_times_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, 1, aMatrix5by5, beta, x5);
    assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_y5_eq_A5x5_times_D1D_x5_plus_beta_times_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, 1, aMatrix5by5, beta, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5_times_x5_plus_D1D_beta_times_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, 1, aMatrix5by5, beta, x5);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_A5x5_times_D1D_x5_plus_D1D_beta_times_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, 1, aMatrix5by5, beta, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_y5_eq_A5x5T_times_x5_plus_beta_times_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix5by5, beta, x5);
    assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_y5_eq_A5x5T_times_D1D_x5_plus_beta_times_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix5by5, beta, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5T_times_x5_plus_D1D_beta_times_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix5by5, beta, x5);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_A5x5T_times_D1D_x5_plus_D1D_beta_times_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix5by5, beta, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5_plus_beta_times_y5));
  }

  //1x1
  @Test
  public void testDGEMV_y1_eq_A1x1_times_x1_plus_beta_times_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, 1, aMatrix1by1, beta, x1);
    assertTrue(Arrays.equals(ycp, tmp_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_y1_eq_A1x1_times_D1D_x1_plus_beta_times_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, 1, aMatrix1by1, beta, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(ycp, tmp_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_A1x1_times_x1_plus_D1D_beta_times_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, 1, aMatrix1by1, beta, x1);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_D1D_y1_A1x1_times_D1D_x1_plus_D1D_beta_times_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, 1, aMatrix1by1, beta, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_y1_eq_A1x1T_times_x1_plus_beta_times_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix1by1, beta, x1);
    assertTrue(Arrays.equals(ycp, tmp_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_y1_eq_A1x1T_times_D1D_x1_plus_beta_times_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix1by1, beta, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(ycp, tmp_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_A1x1T_times_x1_plus_D1D_beta_times_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix1by1, beta, x1);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_A1x1T_times_D1D_x1_plus_D1D_beta_times_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix1by1, beta, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1T_times_x1_plus_beta_times_y1));
  }

  //19x19
  @Test
  public void testDGEMV_y19_eq_A19x19_times_x19_plus_beta_times_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, 1, aMatrix19by19, beta, x19);
    assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_y19_eq_A19x19_times_D1D_x19_plus_beta_times_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, 1, aMatrix19by19, beta, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19_times_x19_plus_D1D_beta_times_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, 1, aMatrix19by19, beta, x19);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_A19x19_times_D1D_x19_plus_D1D_beta_times_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, 1, aMatrix19by19, beta, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_y19_eq_A19x19T_times_x19_plus_beta_times_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix19by19, beta, x19);
    assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_y19_eq_A19x19T_times_D1D_x19_plus_beta_times_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix19by19, beta, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19T_times_x19_plus_D1D_beta_times_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix19by19, beta, x19);
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_A19x19T_times_D1D_x19_plus_D1D_beta_times_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix19by19, beta, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19_plus_beta_times_y19));
  }

  //** group 6:: y=alpha*A*x+beta*y OR y=alpha*A^T*x+beta*y
  //5x5
  @Test
  public void testDGEMV_y5_eq_alpha_times_A5x5_times_x5_plus_beta_times_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix5by5, beta, x5);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_y5_eq_alpha_times_A5x5_times_D1D_x5_plus_beta_times_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix5by5, beta, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_x5_plus_beta_times_D1D_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix5by5, beta, x5);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_D1D_x5_plus_beta_times_D1D_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix5by5, beta, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_y5_eq_alpha_times_A5x5T_times_x5_plus_beta_times_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix5by5, beta, x5);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_y5_eq_alpha_times_A5x5T_times_D1D_x5_plus_beta_times_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix5by5, beta, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_x5_plus_beta_times_D1D_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix5by5, beta, x5);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
  }

  @Test
  public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_D1D_x5_plus_beta_times_D1D_y5() {
    double[] ycp = new double[y5.length];
    System.arraycopy(y5, 0, ycp, 0, y5.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix5by5, beta, new DoubleMatrix1D(x5));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
  }

  //1x1
  @Test
  public void testDGEMV_y1_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix1by1, beta, x1);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_y1_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix1by1, beta, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_x1_plus_beta_times_D1D_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix1by1, beta, x1);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_D1D_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix1by1, beta, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_y1_eq_alpha_times_A1x1T_times_x1_plus_beta_times_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix1by1, beta, x1);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_y1_eq_alpha_times_A1x1T_times_D1D_x1_plus_beta_times_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix1by1, beta, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_x1_plus_beta_times_D1D_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix1by1, beta, x1);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
  }

  @Test
  public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_D1D_x1_plus_beta_times_D1D_y1() {
    double[] ycp = new double[y1.length];
    System.arraycopy(y1, 0, ycp, 0, y1.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix1by1, beta, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
  }

  //19x19
  @Test
  public void testDGEMV_y19_eq_alpha_times_A19x19_times_x19_plus_beta_times_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix19by19, beta, x19);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_y19_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlace(ycp, alpha, aMatrix19by19, beta, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_alpha_times_A19x19_times_x19_plus_beta_times_D1D_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix19by19, beta, x19);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_D1D_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlace(tmp, alpha, aMatrix19by19, beta, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_y19_eq_alpha_times_A19x19T_times_x19_plus_beta_times_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix19by19, beta, x19);
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_y19_eq_alpha_times_A19x19T_times_D1D_x19_plus_beta_times_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix19by19, beta, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_alpha_times_A19x19T_times_x19_plus_beta_times_D1D_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix19by19, beta, x19);
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
  }

  @Test
  public void testDGEMV_D1D_y19_eq_alpha_times_A19x19T_times_D1D_x19_plus_beta_times_D1D_y19() {
    double[] ycp = new double[y19.length];
    System.arraycopy(y19, 0, ycp, 0, y19.length);
    DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
    BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix19by19, beta, new DoubleMatrix1D(x19));
    assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
  }

}
