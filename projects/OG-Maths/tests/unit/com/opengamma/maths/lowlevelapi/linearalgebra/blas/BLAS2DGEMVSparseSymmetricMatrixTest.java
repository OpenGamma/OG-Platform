/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.blas;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.SparseSymmetricMatrix;
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.BLAS2;

/**
 * Tests DGEMV on Sparse symmetric matrices
 * TODO: REMOVE THIS COMMENT AND IMPLEMENT THE DGEMV ABSTRACTION SO THE TESTS BELOW CAN BE RUN!
 */
public class BLAS2DGEMVSparseSymmetricMatrixTest {

    // try 5x5 and 19x19
    double[][] A5by5 = {
        {    1,    0,    3,    0,    0},
        {    0,    7,    8,    9,   10},
        {    3,    8,   13,   14,   15},
        {    0,    9,   14,   19,   20},
        {    0,   10,   15,   20,   25}
    };
    double[][] A19by19 =
      {
        {    1,    0,    3,    4,    5,    6,    0,    0,    0,   10,   11,    0,   13,   14,    0,    0,   17,   18,   19},
        {    0,   21,   22,    0,    0,    0,   26,    0,    0,    0,   30,   31,   32,    0,   34,    0,    0,    0,    0},
        {    3,   22,   41,   42,   43,    0,    0,    0,   47,   48,    0,    0,    0,    0,   53,   54,    0,    0,    0},
        {    4,    0,   42,   61,    0,   63,    0,    0,   66,    0,   68,    0,   70,   71,    0,    0,    0,    0,   76},
        {    5,    0,   43,    0,   81,    0,   83,   84,   85,    0,   87,    0,   89,    0,    0,    0,    0,   94,    0},
        {    6,    0,    0,   63,    0,  101,    0,  103,  104,    0,    0,    0,  108,    0,  110,  111,  112,    0,    0},
        {    0,   26,    0,    0,   83,    0,  121,    0,    0,    0,  125,  126,  127,  128,    0,  130,    0,  132,    0},
        {    0,    0,    0,    0,   84,  103,    0,  141,    0,  143,    0,  145,  146,    0,    0,    0,    0,    0,    0},
        {    0,    0,   47,   66,   85,  104,    0,    0,  161,    0,    0,    0,  165,    0,  167,    0,    0,    0,  171},
        {   10,    0,   48,    0,    0,    0,    0,  143,    0,  181,  182,  183,    0,    0,  186,  187,    0,  189,    0},
        {   11,   30,    0,   68,   87,    0,  125,    0,    0,  182,  201,  202,  203,    0,  205,    0,  207,  208,    0},
        {    0,   31,    0,    0,    0,    0,  126,  145,    0,  183,  202,  221,    0,    0,  224,  225,    0,  227,    0},
        {   13,   32,    0,   70,   89,  108,  127,  146,  165,    0,  203,    0,  241,  242,    0,    0,  245,    0,  247},
        {   14,    0,    0,   71,    0,    0,  128,    0,    0,    0,    0,    0,  242,  261,  262,    0,  264,    0,  266},
        {    0,   34,   53,    0,    0,  110,    0,    0,  167,  186,  205,  224,    0,  262,  281,    0,    0,    0,  285},
        {    0,    0,   54,    0,    0,  111,  130,    0,    0,  187,    0,  225,    0,    0,    0,  301,    0,  303,  304},
        {   17,    0,    0,    0,    0,  112,    0,    0,    0,    0,  207,    0,  245,  264,    0,    0,  321,  322,  323},
        {   18,    0,    0,    0,   94,    0,  132,    0,    0,  189,  208,  227,    0,    0,    0,  303,  322,  341,  342},
        {   19,    0,    0,   76,    0,    0,    0,    0,  171,    0,    0,    0,  247,  266,  285,  304,  323,  342,  361}
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
    SparseSymmetricMatrix aMatrix1by1 = new SparseSymmetricMatrix(A1by1);
    SparseSymmetricMatrix aMatrix5by5 = new SparseSymmetricMatrix(A5by5);
    SparseSymmetricMatrix aMatrix19by19 = new SparseSymmetricMatrix(A19by19);

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
    double[] tmp_A5x5_times_x5 = {10, 124, 189, 236, 270};
    double[] tmp_A5x5T_times_x5 = {10, 124, 189, 236, 270};
    double[] tmp_alpha_times_A5x5_times_x5 = {70, 868, 1323, 1652, 1890};
    double[] tmp_alpha_times_A5x5T_times_x5 = {70, 868, 1323, 1652, 1890};
    double[] tmp_A5x5_times_x5_plus_y5 = {20, 144, 219, 276, 320};
    double[] tmp_A5x5T_times_x5_plus_y5 = {20, 144, 219, 276, 320};
    double[] tmp_alpha_times_A5x5_times_x5_plus_y5 = {80, 888, 1353, 1692, 1940};
    double[] tmp_alpha_times_A5x5T_times_x5_plus_y5 = {80, 888, 1353, 1692, 1940};
    double[] tmp_A5x5_times_x5_plus_beta_times_y5 = {-20, 64, 99, 116, 120};
    double[] tmp_A5x5T_times_x5_plus_beta_times_y5 = {-20, 64, 99, 116, 120};
    double[] tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5 = {40, 808, 1233, 1532, 1740};
    double[] tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5 = {40, 808, 1233, 1532, 1740};


      // 19x19 answers
    double[] tmp_A19x19_times_x19 = {1647, 1918, 3115, 5442, 6363, 9358, 12100, 7234, 10802, 16490, 21085, 19854, 22604, 21466, 22491, 22354, 27237, 31272, 36442};
    double[] tmp_A19x19T_times_x19 = {1647, 1918, 3115, 5442, 6363, 9358, 12100, 7234, 10802, 16490, 21085, 19854, 22604, 21466, 22491, 22354, 27237, 31272, 36442};
    double[] tmp_alpha_times_A19x19_times_x19 = {11529, 13426, 21805, 38094, 44541, 65506, 84700, 50638, 75614, 115430, 147595, 138978, 158228, 150262, 157437, 156478, 190659, 218904, 255094};
    double[] tmp_alpha_times_A19x19T_times_x19 = {11529, 13426, 21805, 38094, 44541, 65506, 84700, 50638, 75614, 115430, 147595, 138978, 158228, 150262, 157437, 156478, 190659, 218904, 255094};
    double[] tmp_A19x19_times_x19_plus_y19 = {1657, 1938, 3145, 5482, 6413, 9418, 12170, 7314, 10892, 16590, 21195, 19974, 22734, 21606, 22641, 22514, 27407, 31452, 36632};
    double[] tmp_A19x19T_times_x19_plus_y19 = {1657, 1938, 3145, 5482, 6413, 9418, 12170, 7314, 10892, 16590, 21195, 19974, 22734, 21606, 22641, 22514, 27407, 31452, 36632};
    double[] tmp_alpha_times_A19x19_times_x19_plus_y19 = {11539, 13446, 21835, 38134, 44591, 65566, 84770, 50718, 75704, 115530, 147705, 139098, 158358, 150402, 157587, 156638, 190829, 219084, 255284};
    double[] tmp_alpha_times_A19x19T_times_x19_plus_y19 = {11539, 13446, 21835, 38134, 44591, 65566, 84770, 50718, 75704, 115530, 147705, 139098, 158358, 150402, 157587, 156638, 190829, 219084, 255284};
    double[] tmp_A19x19_times_x19_plus_beta_times_y19 = {1617, 1858, 3025, 5322, 6213, 9178, 11890, 6994, 10532, 16190, 20755, 19494, 22214, 21046, 22041, 21874, 26727, 30732, 35872};
    double[] tmp_A19x19T_times_x19_plus_beta_times_y19 = {1617, 1858, 3025, 5322, 6213, 9178, 11890, 6994, 10532, 16190, 20755, 19494, 22214, 21046, 22041, 21874, 26727, 30732, 35872};
    double[] tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19 = {11499, 13366, 21715, 37974, 44391, 65326, 84490, 50398, 75344, 115130, 147265, 138618, 157838, 149842, 156987, 155998, 190149, 218364, 254524};
    double[] tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19 = {11499, 13366, 21715, 37974, 44391, 65326, 84490, 50398, 75344, 115130, 147265, 138618, 157838, 149842, 156987, 155998, 190149, 218364, 254524};


    /**
     * Test input catchers
     */

    /* Normal 2 inputs */
//    @Test(expectedExceptions = AssertionError.class)
    public void testInputCatcherMatrix() {
      SparseSymmetricMatrix NullMat = null;
      BLAS2.dgemvInputSanityChecker(NullMat, x5);
    }

//    @Test(expectedExceptions = AssertionError.class)
    public void testInputCatcherVector() {
      double[] aVector = null;
      BLAS2.dgemvInputSanityChecker(aMatrix5by5, aVector);
    }

//    @Test(expectedExceptions = AssertionError.class)
    public void testInputCatcherSizeWillNotCommute() {
      BLAS2.dgemvInputSanityChecker(aMatrix5by5, x19);
    }

    /* Normal 3 inputs */
//    @Test(expectedExceptions = AssertionError.class)
    public void testInputCatcherMatrix3inputs() {
      SparseSymmetricMatrix NullMat = null;
      BLAS2.dgemvInputSanityChecker(NullMat, x5, y5);
    }

//    @Test(expectedExceptions = AssertionError.class)
    public void testInputCatcherVectorBadVectorGood() {
      double[] aVector = null;
      BLAS2.dgemvInputSanityChecker(aMatrix5by5, aVector, x5);
    }

//    @Test(expectedExceptions = AssertionError.class)
    public void testInputCatcherVectorGoodVectorBad() {
      double[] aVector = null;
      BLAS2.dgemvInputSanityChecker(aMatrix5by5, x5, aVector);
    }

//    @Test(expectedExceptions = AssertionError.class)
    public void testInputCatcherSizeWillNotCommuteWithIntermediteVector() {
      BLAS2.dgemvInputSanityChecker(aMatrix5by5, x19, y5);
    }

//    @Test(expectedExceptions = AssertionError.class)
    public void testInputCatcherSizeWillNotCommuteWithReturnVector() {
      BLAS2.dgemvInputSanityChecker(aMatrix5by5, x5, y19);
    }

    /* Transpose 2 inputs */
//    @Test(expectedExceptions = AssertionError.class)
    public void testTransposeInputCatcherMatrix() {
      SparseSymmetricMatrix NullMat = null;
      BLAS2.dgemvInputSanityCheckerTransposed(NullMat, x5);
    }

//    @Test(expectedExceptions = AssertionError.class)
    public void testTransposeInputCatcherVector() {
      double[] aVector = null;
      BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by5, aVector);
    }

//    @Test(expectedExceptions = AssertionError.class)
    public void testTransposeInputCatcherSizeWillNotCommute() {
      BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by5, x19);
    }

    /* Transpose 3 inputs */
//    @Test(expectedExceptions = AssertionError.class)
    public void testTransposeInputCatcherMatrix3inputs() {
      SparseSymmetricMatrix NullMat = null;
      BLAS2.dgemvInputSanityCheckerTransposed(NullMat, x5, y5);
    }

//    @Test(expectedExceptions = AssertionError.class)
    public void testTransposeInputCatcherVectorBadVectorGood() {
      double[] aVector = null;
      BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by5, aVector, x5);
    }

//    @Test(expectedExceptions = AssertionError.class)
    public void testTransposeInputCatcherVectorGoodVectorBad() {
      double[] aVector = null;
      BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by5, x5, aVector);
    }

//    @Test(expectedExceptions = AssertionError.class)
    public void testTransposeInputCatcherSizeWillNotCommuteWithIntermediteVector() {
      BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by5, x19, y5);
    }

//    @Test(expectedExceptions = AssertionError.class)
    public void testTransposeInputCatcherSizeWillNotCommuteWithReturnVector() {
      BLAS2.dgemvInputSanityCheckerTransposed(aMatrix5by5, x5, y19);
    }

    /**
     * DGEMV on FULL Matrices
     */
    // stateless manipulators

    //** group 1:: A*x OR A^T*x

    // test 5x5
    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5), tmp_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5)), tmp_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5T_times_x5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, x5), tmp_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5T_times_D1D_x5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, new DoubleMatrix1D(x5)), tmp_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, BLAS2.orientation.normal), tmp_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.normal), tmp_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5T_times_x5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, BLAS2.orientation.transposed), tmp_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.transposed), tmp_A5x5T_times_x5));
    }

    //test 1x1
    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1), tmp_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1)), tmp_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1T_times_x1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, x1), tmp_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1T_times_D1D_x1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, new DoubleMatrix1D(x1)), tmp_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, BLAS2.orientation.normal), tmp_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.normal), tmp_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, BLAS2.orientation.transposed), tmp_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.transposed), tmp_A1x1T_times_x1));
    }

    //test 19x19, should trip loop unwindings
    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19), tmp_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19)), tmp_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19T_times_x19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, x19), tmp_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19T_times_D1D_x19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, new DoubleMatrix1D(x19)), tmp_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, BLAS2.orientation.normal), tmp_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.normal), tmp_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, BLAS2.orientation.transposed), tmp_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.transposed), tmp_A19x19T_times_x19));
    }

    //** group 2:: alpha*A*x OR alpha*A^T*x
    //test 5x5
    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5), tmp_alpha_times_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5)), tmp_alpha_times_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, x5), tmp_alpha_times_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, new DoubleMatrix1D(x5)), tmp_alpha_times_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_A5x5_times_x5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_A5x5_times_x5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5));
    }


    //test 1x1
    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1), tmp_alpha_times_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1)), tmp_alpha_times_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, x1), tmp_alpha_times_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_alpha_times_eq_A1x1T_times_D1D_x1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, new DoubleMatrix1D(x1)), tmp_alpha_times_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1));
    }

    //test 19x19 should trip loop unwindings
    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19), tmp_alpha_times_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19)), tmp_alpha_times_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19T_times_x19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, x19), tmp_alpha_times_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_alpha_times_eq_A19x19T_times_D1D_x19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, new DoubleMatrix1D(x19)), tmp_alpha_times_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19));
    }

    //** group 3:: A*x+y OR A^T*x+y

    //test 5x5
    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5_plus_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, y5), tmp_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), y5), tmp_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5_plus_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, new DoubleMatrix1D(y5)), tmp_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5)), tmp_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5T_times_x5_plus_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, x5, y5), tmp_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_plus_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, new DoubleMatrix1D(x5), y5), tmp_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5T_times_x5_plus_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, x5, new DoubleMatrix1D(y5)), tmp_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_plus_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5)), tmp_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5_plus_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, y5, BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), y5, BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5_plus_D1D_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_D1D_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5_plus_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, y5, BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), y5, BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5_plus_D1D_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_D1D_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_y5));
    }

    //test 1x1
    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_plus_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, y1), tmp_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), y1), tmp_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_plus_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, new DoubleMatrix1D(y1)), tmp_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1)), tmp_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1T_times_x1_plus_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, x1, y1), tmp_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1T_times_D1D_x1_plus_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, new DoubleMatrix1D(x1), y1), tmp_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1T_times_x1_plus_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, x1, new DoubleMatrix1D(y1)), tmp_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1T_times_D1D_x1_plus_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1)), tmp_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_plus_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, y1, BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), y1, BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_plus_D1D_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_plus_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, y1, BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), y1, BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_plus_D1D_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_y1));
    }

    //test 19x19 should trip loop unwindings
    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_plus_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, y19), tmp_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), y19), tmp_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_plus_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, new DoubleMatrix1D(y19)), tmp_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19)), tmp_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19T_times_x19_plus_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, x19, y19), tmp_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19T_times_D1D_x19_plus_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, new DoubleMatrix1D(x19), y19), tmp_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19T_times_x19_plus_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, x19, new DoubleMatrix1D(y19)), tmp_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19T_times_D1D_x19_plus_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19)), tmp_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_plus_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, y19, BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), y19, BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_plus_y19_D1D_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_D1D_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_plus_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, y19, BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), y19, BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_plus_y19_D1D_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_D1D_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_y19));
    }

    //** group 4:: alpha*A*x+y OR alpha*A^T*x+y
    //5x5
    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, y5), tmp_alpha_times_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), y5), tmp_alpha_times_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5_plus_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, x5, y5), tmp_alpha_times_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5_plus_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, new DoubleMatrix1D(x5), y5), tmp_alpha_times_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5_plus_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, x5, new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5_plus_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, y5, BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), y5, BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5_D1D_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_D1D_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, y5, BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), y5, BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_y5_D1D_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_D1D_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_y5));
    }

    //1x1
    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, y1), tmp_alpha_times_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), y1), tmp_alpha_times_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1_plus_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, x1, y1), tmp_alpha_times_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1T_times_D1D_x1_plus_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, new DoubleMatrix1D(x1), y1), tmp_alpha_times_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1_plus_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, x1, new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1T_times_D1D_x1_plus_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, y1, BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), y1, BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_D1D_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, y1, BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), y1, BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_D1D_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_D1D_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_y1));
    }

    //19x19
    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, y19), tmp_alpha_times_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), y19), tmp_alpha_times_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19T_times_x19_plus_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, x19, y19), tmp_alpha_times_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19T_times_D1D_x19_plus_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, new DoubleMatrix1D(x19), y19), tmp_alpha_times_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19T_times_x19_plus_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, x19, new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19T_times_D1D_x19_plus_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, y19, BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), y19, BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_D1D_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_D1D_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, y19, BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x19_plus_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), y19, BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x19_plus_y19_D1D_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_D1D_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_y19));
    }

    //** group 5:: A*x+beta*y OR A^T*x+beta*y
    //5x5
    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, beta, y5), tmp_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), beta, y5), tmp_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, beta, new DoubleMatrix1D(y5)), tmp_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5)), tmp_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5T_times_x5_plus_beta_times_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, x5, beta, y5), tmp_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_plus_beta_times_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, new DoubleMatrix1D(x5), beta, y5), tmp_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5T_times_x5_plus_beta_times_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, x5, beta, new DoubleMatrix1D(y5)), tmp_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5T_times_D1D_x5_plus_beta_times_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5)), tmp_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, beta, y5, BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), beta, y5, BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_D1D_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, beta, new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_D1D_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, beta, y5, BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), beta, y5, BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_x5_plus_beta_times_y5_D1D_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, x5, beta, new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_A5x5_times_D1D_x5_plus_beta_times_D1D_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_A5x5T_times_x5_plus_beta_times_y5));
    }

    //1x1
    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, beta, y1), tmp_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), beta, y1), tmp_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, beta, new DoubleMatrix1D(y1)), tmp_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1)), tmp_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1T_times_x1_plus_beta_times_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, x1, beta, y1), tmp_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1T_times_D1D_x1_plus_beta_times_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, new DoubleMatrix1D(x1), beta, y1), tmp_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1T_times_x1_plus_beta_times_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, x1, beta, new DoubleMatrix1D(y1)), tmp_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1T_times_D1D_x1_plus_beta_times_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1)), tmp_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, beta, y1, BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), beta, y1, BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_y1_D1D_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, beta, new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, beta, y1, BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), beta, y1, BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, x1, beta, new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_A1x1T_times_x1_plus_beta_times_y1));
    }

    //19x19
    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_plus_beta_times_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, beta, y19), tmp_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_beta_times_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), beta, y19), tmp_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_plus_beta_times_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, beta, new DoubleMatrix1D(y19)), tmp_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_beta_times_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19)), tmp_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19T_times_x19_plus_beta_times_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, x19, beta, y19), tmp_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19T_times_D1D_x19_plus_beta_times_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, new DoubleMatrix1D(x19), beta, y19), tmp_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19T_times_x19_plus_beta_times_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, x19, beta, new DoubleMatrix1D(y19)), tmp_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19T_times_D1D_x19_plus_beta_times_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19)), tmp_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_plus_beta_times_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, beta, y19, BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_beta_times_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), beta, y19, BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_plus_beta_times_D1D_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, beta, new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_beta_times_D1D_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_plus_beta_times_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, beta, y19, BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_beta_times_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), beta, y19, BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_x19_plus_beta_times_D1D_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, x19, beta, new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_A19x19_times_D1D_x19_plus_beta_times_D1D_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_A19x19T_times_x19_plus_beta_times_y19));
    }

    //** group 6:: alpha*A*x+beta*y OR alpha*A^T*x+beta*y

    //5x5
    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_beta_times_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, beta, y5), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_beta_times_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, y5), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_x5_plus_beta_times_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, beta, new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5_times_D1D_x5_plus_beta_times_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5_plus_beta_times_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, x5, beta, y5), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5_plus_beta_times_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, y5), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5T_times_x5_plus_beta_times_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, x5, beta, new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A5x5T_times_D1D_x5_plus_beta_times_D1D_y5() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5)), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_A5x5_times_x5_plus_beta_times_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, beta, y5, BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_plus_beta_times_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, y5, BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_A5x5_times_x5_plus_beta_times_D1D_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, beta, new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_plus_beta_times_D1D_y5_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5), BLAS2.orientation.normal), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_A5x5_times_x5_plus_beta_times_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, beta, y5, BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_plus_beta_times_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, y5, BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_A5x5_times_x5_plus_beta_times_D1D_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, x5, beta, new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_A5x5_times_D1D_x5_plus_beta_times_D1D_y5_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix5by5, new DoubleMatrix1D(x5), beta, new DoubleMatrix1D(y5), BLAS2.orientation.transposed), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
    }

    //1x1
    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, beta, y1), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, y1), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, beta, new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1_plus_beta_times_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, x1, beta, y1), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1T_times_D1D_x1_plus_beta_times_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, y1), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1T_times_x1_plus_beta_times_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, x1, beta, new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1T_times_D1D_x1_plus_beta_times_D1D_y1() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1)), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, beta, y1, BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, y1, BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1_D1D_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, beta, new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1), BLAS2.orientation.normal), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, beta, y1, BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, y1, BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, x1, beta, new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_D1D_y1_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix1by1, new DoubleMatrix1D(x1), beta, new DoubleMatrix1D(y1), BLAS2.orientation.transposed), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
    }

    //19x19
    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_beta_times_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, beta, y19), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, y19), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_beta_times_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, beta, new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19T_times_x19_plus_beta_times_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, x19, beta, y19), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19T_times_D1D_x19_plus_beta_times_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, y19), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19T_times_x19_plus_beta_times_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, x19, beta, new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19T_times_D1D_x19_plus_beta_times_D1D_y19() {
      assertTrue(Arrays.equals(BLAS2.dgemvTransposed(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19)), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_beta_times_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, beta, y19, BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, y19, BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_beta_times_D1D_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, beta, new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_D1D_y19_interfaced_normal() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19), BLAS2.orientation.normal), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_beta_times_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, beta, y19, BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, y19, BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_x19_plus_beta_times_D1D_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, x19, beta, new DoubleMatrix1D(y19), BLAS2.orientation.transposed), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_ans_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_D1D_y19_interfaced_transposed() {
      assertTrue(Arrays.equals(BLAS2.dgemv(alpha, aMatrix19by19, new DoubleMatrix1D(x19), beta, new DoubleMatrix1D(y19), BLAS2.orientation.transposed),
          tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
    }

    // test the inplaces
    //** group 1:: A*x OR A^T*x

    //5x5
    //    @Test
    public void testDGEMV_y5_eq_A5x5_times_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, aMatrix5by5, x5);
      assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_y5_eq_A5x5_times_D1D_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, aMatrix5by5, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5_times_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, aMatrix5by5, x5);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5_times_D1D_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, aMatrix5by5, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_y5_eq_A5x5T_times_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlaceTransposed(ycp, aMatrix5by5, x5);
      assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_y5_eq_A5x5T_times_D1D_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlaceTransposed(ycp, aMatrix5by5, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5T_times_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, aMatrix5by5, x5);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5T_times_D1D_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, aMatrix5by5, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_y5_eq_A5x5_times_x5_interfaced_normal() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, aMatrix5by5, x5, BLAS2.orientation.normal);
      assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5_times_x5_interfaced_normal() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(y5);
      BLAS2.dgemvInPlace(tmp, aMatrix5by5, x5, BLAS2.orientation.normal);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_y5_eq_A5x5_times_D1D_x5_interfaced_normal() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.normal);
      assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5_times_D1D_x5_interfaced_normal() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.normal);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_y5_eq_A5x5_times_x5_interfaced_transposed() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, aMatrix5by5, x5, BLAS2.orientation.transposed);
      assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5_times_x5_interfaced_transposed() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(y5);
      BLAS2.dgemvInPlace(tmp, aMatrix5by5, x5, BLAS2.orientation.transposed);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_y5_eq_A5x5_times_D1D_x5_interfaced_transposed() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.transposed);
      assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5_times_D1D_x5_interfaced_transposed() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, aMatrix5by5, new DoubleMatrix1D(x5), BLAS2.orientation.transposed);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5));
    }

    //1x1
    //    @Test
    public void testDGEMV_y1_eq_A1x1_times_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, aMatrix1by1, x1);
      assertTrue(Arrays.equals(ycp, tmp_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_y1_eq_A1x1_times_D1D_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, aMatrix1by1, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(ycp, tmp_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_A1x1_times_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, aMatrix1by1, x1);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_A1x1_times_D1D_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, aMatrix1by1, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_y1_eq_A1x1T_times_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlaceTransposed(ycp, aMatrix1by1, x1);
      assertTrue(Arrays.equals(ycp, tmp_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_y1_eq_A1x1T_times_D1D_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlaceTransposed(ycp, aMatrix1by1, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(ycp, tmp_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_A1x1T_times_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, aMatrix1by1, x1);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_A1x1T_times_D1D_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, aMatrix1by1, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_y1_eq_A1x1_times_x1_interfaced_normal() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, aMatrix1by1, x1, BLAS2.orientation.normal);
      assertTrue(Arrays.equals(ycp, tmp_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_A1x1_times_x1_interfaced_normal() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(y1);
      BLAS2.dgemvInPlace(tmp, aMatrix1by1, x1, BLAS2.orientation.normal);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_y1_eq_A1x1_times_D1D_x1_interfaced_normal() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.normal);
      assertTrue(Arrays.equals(ycp, tmp_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_A1x1_times_D1D_x1_interfaced_normal() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.normal);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_y1_eq_A1x1_times_x1_interfaced_transposed() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, aMatrix1by1, x1, BLAS2.orientation.transposed);
      assertTrue(Arrays.equals(ycp, tmp_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_A1x1_times_x1_interfaced_transposed() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(y1);
      BLAS2.dgemvInPlace(tmp, aMatrix1by1, x1, BLAS2.orientation.transposed);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_y1_eq_A1x1_times_D1D_x1_interfaced_transposed() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.transposed);
      assertTrue(Arrays.equals(ycp, tmp_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_A1x1_times_D1D_x1_interfaced_transposed() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, aMatrix1by1, new DoubleMatrix1D(x1), BLAS2.orientation.transposed);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1T_times_x1));
    }

    //19x19
    //    @Test
    public void testDGEMV_y19_eq_A19x19_times_x19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, aMatrix19by19, x19);
      assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_y19_eq_A19x19_times_D1D_x1() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, aMatrix19by19, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19_times_x1() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, aMatrix19by19, x19);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19_times_D1D_x1() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, aMatrix19by19, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_y19_eq_A19x19T_times_x19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlaceTransposed(ycp, aMatrix19by19, x19);
      assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_y19eq_A19x19T_times_D1D_x19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlaceTransposed(ycp, aMatrix19by19, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19T_times_x19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, aMatrix19by19, x19);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19T_times_D1D_x19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, aMatrix19by19, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_y19_eq_A19x19_times_x19_interfaced_normal() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, aMatrix19by19, x19, BLAS2.orientation.normal);
      assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19_times_x19_interfaced_normal() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(y19);
      BLAS2.dgemvInPlace(tmp, aMatrix19by19, x19, BLAS2.orientation.normal);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_y19_eq_A19x19_times_D1D_x19_interfaced_normal() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.normal);
      assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19_times_D1D_x19_interfaced_normal() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.normal);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_y19_eq_A19x19_times_x19_interfaced_transposed() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, aMatrix19by19, x19, BLAS2.orientation.transposed);
      assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19_times_x19_interfaced_transposed() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(y19);
      BLAS2.dgemvInPlace(tmp, aMatrix19by19, x19, BLAS2.orientation.transposed);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_y19_eq_A19x19_times_D1D_x19_interfaced_transposed() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.transposed);
      assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19_times_D1D_x19_interfaced_transposed() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, aMatrix19by19, new DoubleMatrix1D(x19), BLAS2.orientation.transposed);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19));
    }

    //** group 2:: y=alpha*A*x OR y=alpha*A^T*x
    //5x5
    //    @Test
    public void testDGEMV_y5_eq_alpha_times_A5x5_times_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix5by5, 0, x5);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_y5_eq_alpha_times_A5x5_times_D1D_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix5by5, 0, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix5by5, 0, x5);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_D1D_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix5by5, 0, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5_times_x5));
    }

    //    @Test
    public void testDGEMV_y5_eq_alpha_times_A5x5T_times_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix5by5, 0, x5);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_y5_eq_alpha_times_A5x5T_times_D1D_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix5by5, 0, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix5by5, 0, x5);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5T_times_x5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_D1D_x5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix5by5, 0, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5T_times_x5));
    }

    //1x1
    //    @Test
    public void testDGEMV_y1_eq_alpha_times_A1x1_times_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix1by1, 0, x1);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_y1_eq_alpha_times_A1x1_times_D1D_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix1by1, 0, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix1by1, 0, x1);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_D1D_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix1by1, 0, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1_times_x1));
    }

    //    @Test
    public void testDGEMV_y1_eq_alpha_times_A1x1T_times_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix1by1, 0, x1);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_y1_eq_alpha_times_A1x1T_times_D1D_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix1by1, 0, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_alpha_times_A1xT_times_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix1by1, 0, x1);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1T_times_x1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_D1D_x1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix1by1, 0, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1T_times_x1));
    }

    //19x19
    //    @Test
    public void testDGEMV_y19_eq_alpha_times_A19x19_times_x19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix19by19, 0, x19);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_y19_eq_alpha_times_A19x5_times_D1D_x19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix19by19, 0, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_alpha_times_A19x19_times_x19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix19by19, 0, x19);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_alpha_times_A19x19_times_D1D_x19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix19by19, 0, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19_times_x19));
    }

    //    @Test
    public void testDGEMV_y19_eq_alpha_times_A19x19T_times_x19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix19by19, 0, x19);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_y19_eq_alpha_times_A19x19T_times_D1D_x19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix19by19, 0, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_alpha_times_A19x19T_times_x19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix19by19, 0, x19);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19T_times_x19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_alpha_times_A19x19T_times_D1D_x19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix19by19, 0, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19T_times_x19));
    }

    //** group 3:: A*x+y OR A^T*x+y
    //5x5
    //    @Test
    public void testDGEMV_y5_eq_A5x5_times_x5_plus_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, 1, aMatrix5by5, 1, x5);
      assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_y5_eq_A5x5_times_D1D_x5_plus_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, 1, aMatrix5by5, 1, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5_times_x5_plus_D1D_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, 1, aMatrix5by5, 1, x5);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_A5x5_times_D1D_x5_plus_D1D_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, 1, aMatrix5by5, 1, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_y5_eq_A5x5T_times_x5_plus_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix5by5, 1, x5);
      assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_y5_eq_A5x5T_times_D1D_x5_plus_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix5by5, 1, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5T_times_x5_plus_D1D_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix5by5, 1, x5);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5T_times_D1D_x5_plus_D1D_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix5by5, 1, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5_plus_y5));
    }

    //19x19
    //    @Test
    public void testDGEMV_y19_eq_A19x19_times_x19_plus_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, 1, aMatrix19by19, 1, x19);
      assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_y19_eq_A19x19_times_D1D_x19_plus_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, 1, aMatrix19by19, 1, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19_times_x19_plus_D1D_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, 1, aMatrix19by19, 1, x19);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19_times_D1D_x19_plus_D1D_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, 1, aMatrix19by19, 1, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_y19_eq_A19x19T_times_x19_plus_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix19by19, 1, x19);
      assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_y19_eq_A19x19T_times_D1D_x19_plus_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix19by19, 1, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19T_times_x19_plus_D1D_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix19by19, 1, x19);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19T_times_D1D_x19_plus_D1D_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix19by19, 1, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19_plus_y19));
    }

    //** group 4:: alpha*A*x+y OR alpha*A^T*x+y
    //5x5
    //    @Test
    public void testDGEMV_y5_eq_alpha_times_A5x5_times_x5_plus_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix5by5, 1, x5);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_y5_eq_alpha_times_A5x5_times_D1D_x5_plus_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix5by5, 1, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_x5_plus_D1D_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix5by5, 1, x5);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_alpha_times_A5x5_times_D1D_x5_plus_D1D_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix5by5, 1, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_y5_eq_alpha_times_A5x5T_times_x5_plus_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix5by5, 1, x5);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_y5_eq_alpha_times_A5x5T_times_D1D_x5_plus_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix5by5, 1, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_x5_plus_D1D_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix5by5, 1, x5);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5T_times_x5_plus_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_D1D_x5_plus_D1D_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix5by5, 1, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5T_times_x5_plus_y5));
    }

     //1x1
    //    @Test
    public void testDGEMV_y1_eq_alpha_times_A1x1_times_x1_plus_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix1by1, 1, x1);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_y1_eq_alpha_times_A1x1_times_D1D_x1_plus_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix1by1, 1, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_x1_plus_D1D_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix1by1, 1, x1);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_alpha_times_A1x1_times_D1D_x1_plus_D1D_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix1by1, 1, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_y1_eq_alpha_times_A1x1T_times_x1_plus_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix1by1, 1, x1);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_y1_eq_alpha_times_A1x1T_times_D1D_x1_plus_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix1by1, 1, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_x1_plus_D1D_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix1by1, 1, x1);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1T_times_x1_plus_y1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_D1D_x1_plus_D1D_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix1by1, 1, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1T_times_x1_plus_y1));
    }

    //19x19
    //    @Test
    public void testDGEMV_y19_eq_alpha_times_A19x19_times_x19_plus_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix19by19, 1, x19);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_y19_eq_alpha_times_A19x19_times_D1D_x19_plus_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix19by19, 1, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_alpha_times_A19x19_times_x19_plus_D1D_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix19by19, 1, x19);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_alpha_times_A19x19_times_D1D_x19_plus_D1D_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix19by19, 1, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_y19_eq_alpha_times_A19x19T_times_x19_plus_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix19by19, 1, x19);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_y19_eq_alpha_times_A19x19T_times_D1D_x19_plus_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix19by19, 1, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_alpha_times_A19x19T_times_x19_plus_D1D_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix19by19, 1, x19);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19T_times_x19_plus_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_alpha_times_A19x19T_times_D1D_x19_plus_D1D_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix19by19, 1, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19T_times_x19_plus_y19));
    }

    //* group 5:: A*x + beta*y or A^T*x + beta*y */
    //5x5
    //    @Test
    public void testDGEMV_y5_eq_A5x5_times_x5_plus_beta_times_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, 1, aMatrix5by5, beta, x5);
      assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_y5_eq_A5x5_times_D1D_x5_plus_beta_times_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, 1, aMatrix5by5, beta, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(ycp, tmp_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5_times_x5_plus_D1D_beta_times_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, 1, aMatrix5by5, beta, x5);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_A5x5_times_D1D_x5_plus_D1D_beta_times_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, 1, aMatrix5by5, beta, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_y5_eq_A5x5T_times_x5_plus_beta_times_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix5by5, beta, x5);
      assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_y5_eq_A5x5T_times_D1D_x5_plus_beta_times_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix5by5, beta, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(ycp, tmp_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5T_times_x5_plus_D1D_beta_times_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix5by5, beta, x5);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_A5x5T_times_D1D_x5_plus_D1D_beta_times_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix5by5, beta, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A5x5T_times_x5_plus_beta_times_y5));
    }

    //1x1
    //    @Test
    public void testDGEMV_y1_eq_A1x1_times_x1_plus_beta_times_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, 1, aMatrix1by1, beta, x1);
      assertTrue(Arrays.equals(ycp, tmp_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_y1_eq_A1x1_times_D1D_x1_plus_beta_times_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, 1, aMatrix1by1, beta, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(ycp, tmp_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_A1x1_times_x1_plus_D1D_beta_times_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, 1, aMatrix1by1, beta, x1);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_A1x1_times_D1D_x1_plus_D1D_beta_times_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, 1, aMatrix1by1, beta, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_y1_eq_A1x1T_times_x1_plus_beta_times_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix1by1, beta, x1);
      assertTrue(Arrays.equals(ycp, tmp_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_y1_eq_A1x1T_times_D1D_x1_plus_beta_times_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix1by1, beta, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(ycp, tmp_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_A1x1T_times_x1_plus_D1D_beta_times_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix1by1, beta, x1);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_A1x1T_times_D1D_x1_plus_D1D_beta_times_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix1by1, beta, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A1x1T_times_x1_plus_beta_times_y1));
    }

    //19x19
    //    @Test
    public void testDGEMV_y19_eq_A19x19_times_x19_plus_beta_times_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, 1, aMatrix19by19, beta, x19);
      assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_y19_eq_A19x19_times_D1D_x19_plus_beta_times_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, 1, aMatrix19by19, beta, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(ycp, tmp_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19_times_x19_plus_D1D_beta_times_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, 1, aMatrix19by19, beta, x19);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_A19x19_times_D1D_x19_plus_D1D_beta_times_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, 1, aMatrix19by19, beta, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_y19_eq_A19x19T_times_x19_plus_beta_times_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix19by19, beta, x19);
      assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_y19_eq_A19x19T_times_D1D_x19_plus_beta_times_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlaceTransposed(ycp, 1, aMatrix19by19, beta, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(ycp, tmp_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19T_times_x19_plus_D1D_beta_times_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix19by19, beta, x19);
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_A19x19T_times_D1D_x19_plus_D1D_beta_times_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, 1, aMatrix19by19, beta, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(tmp.getData(), tmp_A19x19T_times_x19_plus_beta_times_y19));
    }

    //** group 6:: y=alpha*A*x+beta*y OR y=alpha*A^T*x+beta*y
    //5x5
    //    @Test
    public void testDGEMV_y5_eq_alpha_times_A5x5_times_x5_plus_beta_times_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix5by5, beta, x5);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_y5_eq_alpha_times_A5x5_times_D1D_x5_plus_beta_times_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix5by5, beta, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_x5_plus_beta_times_D1D_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix5by5, beta, x5);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_alpha_times_A5x5_times_D1D_x5_plus_beta_times_D1D_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix5by5, beta, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_y5_eq_alpha_times_A5x5T_times_x5_plus_beta_times_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix5by5, beta, x5);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_y5_eq_alpha_times_A5x5T_times_D1D_x5_plus_beta_times_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix5by5, beta, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_x5_plus_beta_times_D1D_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix5by5, beta, x5);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
    }

    //    @Test
    public void testDGEMV_D1D_y5_eq_alpha_times_A5x5T_times_D1D_x5_plus_beta_times_D1D_y5() {
      double[] ycp = new double[y5.length];
      System.arraycopy(y5, 0, ycp, 0, y5.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix5by5, beta, new DoubleMatrix1D(x5));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A5x5T_times_x5_plus_beta_times_y5));
    }

    //1x1
    //    @Test
    public void testDGEMV_y1_eq_alpha_times_A1x1_times_x1_plus_beta_times_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix1by1, beta, x1);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_y1_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix1by1, beta, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_x1_plus_beta_times_D1D_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix1by1, beta, x1);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_alpha_times_A1x1_times_D1D_x1_plus_beta_times_D1D_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix1by1, beta, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_y1_eq_alpha_times_A1x1T_times_x1_plus_beta_times_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix1by1, beta, x1);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_y1_eq_alpha_times_A1x1T_times_D1D_x1_plus_beta_times_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix1by1, beta, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_x1_plus_beta_times_D1D_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix1by1, beta, x1);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
    }

    //    @Test
    public void testDGEMV_D1D_y1_eq_alpha_times_A1x1T_times_D1D_x1_plus_beta_times_D1D_y1() {
      double[] ycp = new double[y1.length];
      System.arraycopy(y1, 0, ycp, 0, y1.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix1by1, beta, new DoubleMatrix1D(x1));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A1x1T_times_x1_plus_beta_times_y1));
    }

    //19x19
    //    @Test
    public void testDGEMV_y19_eq_alpha_times_A19x19_times_x19_plus_beta_times_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix19by19, beta, x19);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_y19_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlace(ycp, alpha, aMatrix19by19, beta, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_alpha_times_A19x19_times_x19_plus_beta_times_D1D_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix19by19, beta, x19);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_alpha_times_A19x19_times_D1D_x19_plus_beta_times_D1D_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlace(tmp, alpha, aMatrix19by19, beta, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_y19_eq_alpha_times_A19x19T_times_x19_plus_beta_times_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix19by19, beta, x19);
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_y19_eq_alpha_times_A19x19T_times_D1D_x19_plus_beta_times_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      BLAS2.dgemvInPlaceTransposed(ycp, alpha, aMatrix19by19, beta, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(ycp, tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_alpha_times_A19x19T_times_x19_plus_beta_times_D1D_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix19by19, beta, x19);
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
    }

    //    @Test
    public void testDGEMV_D1D_y19_eq_alpha_times_A19x19T_times_D1D_x19_plus_beta_times_D1D_y19() {
      double[] ycp = new double[y19.length];
      System.arraycopy(y19, 0, ycp, 0, y19.length);
      DoubleMatrix1D tmp = new DoubleMatrix1D(ycp);
      BLAS2.dgemvInPlaceTransposed(tmp, alpha, aMatrix19by19, beta, new DoubleMatrix1D(x19));
      assertTrue(Arrays.equals(tmp.getData(), tmp_alpha_times_A19x19T_times_x19_plus_beta_times_y19));
    }

  }

