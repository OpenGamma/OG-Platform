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
import com.opengamma.maths.lowlevelapi.linearalgebra.blas.BLAS1;

/**
 * Tests the BLAS1 library
 */
public class BLAS1Test {

  // nulls
  double[] xnull = null;
  double[] ynull = null;

  // tests 1element short vector
  double[] x1 = {1 };
  double[] y1 = {10 };
  double[] x1_plus_y1 = {11 };
  double[] alpha_times_x1_plus_y1 = {17 };
  double[] alpha_times_x1 = {7 };

  // hits loop unwind max
  double[] x16 = range(1, 16);
  double[] y16 = range(10, 160, 10);
  double[] x16_plus_y16 = {11, 22, 33, 44, 55, 66, 77, 88, 99, 110, 121, 132, 143, 154, 165, 176 };
  double[] alpha_times_x16_plus_y16 = {17, 34, 51, 68, 85, 102, 119, 136, 153, 170, 187, 204, 221, 238, 255, 272 };
  double[] alpha_times_x16 = {7, 14, 21, 28, 35, 42, 49, 56, 63, 70, 77, 84, 91, 98, 105, 112 };

  // trips loop unwinds to hit clean up code
  double[] x37 = range(1, 37);
  double[] y37 = range(10, 370, 10);
  double[] x37_plus_y37 = {11, 22, 33, 44, 55, 66, 77, 88, 99, 110, 121, 132, 143, 154, 165, 176, 187, 198, 209, 220, 231, 242, 253, 264, 275, 286, 297, 308, 319, 330, 341, 352, 363, 374, 385, 396,
      407 };
  double[] alpha_times_x37_plus_y37 = {17, 34, 51, 68, 85, 102, 119, 136, 153, 170, 187, 204, 221, 238, 255, 272, 289, 306, 323, 340, 357, 374, 391, 408, 425, 442, 459, 476, 493, 510, 527, 544, 561,
      578, 595, 612, 629 };
  double[] alpha_times_x37 = {7, 14, 21, 28, 35, 42, 49, 56, 63, 70, 77, 84, 91, 98, 105, 112, 119, 126, 133, 140, 147, 154, 161, 168, 175, 182, 189, 196, 203, 210, 217, 224, 231, 238, 245, 252, 259 };
 
  // scalar
  double alpha = 7;
  
  // for idx search and sum
  double[] xSS = {13,1,5,19,4,5,19,7,8};

  BLAS1 blas1 = new BLAS1();

  ///////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////// DAXPY /////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////

  /** test the sanity checker */
  @Test(expectedExceptions = AssertionError.class)
  public void testDAXPYsanityCheckerArg1() {
    double[] tmp1 = null;
    double[] tmp2 = {1 };
    BLAS1.daxpy(tmp1, tmp2);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testDAXPYsanityCheckerArg2() {
    double[] tmp1 = {1 };
    double[] tmp2 = null;
    BLAS1.daxpy(tmp1, tmp2);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testDAXPYsanityCheckerLengths() {
    double[] tmp1 = {1, 2 };
    double[] tmp2 = {3, 4, 5 };
    BLAS1.daxpy(tmp1, tmp2);
  }

  /** Stateless y:=x+y */
  // Test DAXPY double[] double[] interface

  @Test
  public void testDAXPY_ans_eq_x1_plus_y1() {
    double[] tmp = BLAS1.daxpy(x1, y1);
    assertTrue(Arrays.equals(x1_plus_y1, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_x16_plus_y16() {
    double[] tmp = BLAS1.daxpy(x16, y16);
    assertTrue(Arrays.equals(x16_plus_y16, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_x37_plus_y37() {
    double[] tmp = BLAS1.daxpy(x37, y37);
    assertTrue(Arrays.equals(x37_plus_y37, tmp));
  }

  //Test DAXPY double[] DoubleMatrix1D interface
  @Test
  public void testDAXPY_ans_eq_x1_plus_D1D_y1() {
    double[] tmp = BLAS1.daxpy(x1, new DoubleMatrix1D(y1));
    assertTrue(Arrays.equals(x1_plus_y1, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_x16_plus_D1D_y16() {
    double[] tmp = BLAS1.daxpy(x16, new DoubleMatrix1D(y16));
    assertTrue(Arrays.equals(x16_plus_y16, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_x37_plus_D1D_y37() {
    double[] tmp = BLAS1.daxpy(x37, new DoubleMatrix1D(y37));
    assertTrue(Arrays.equals(x37_plus_y37, tmp));
  }

  //Test DAXPY DoubleMatrix1D double[] interface
  @Test
  public void testDAXPY_ans_eq_D1D_x1_plus_y1() {
    double[] tmp = BLAS1.daxpy(new DoubleMatrix1D(x1), y1);
    assertTrue(Arrays.equals(x1_plus_y1, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_D1D_x16_plus_y16() {
    double[] tmp = BLAS1.daxpy(new DoubleMatrix1D(x16), y16);
    assertTrue(Arrays.equals(x16_plus_y16, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_D1D_x37_plus_y37() {
    double[] tmp = BLAS1.daxpy(new DoubleMatrix1D(x37), y37);
    assertTrue(Arrays.equals(x37_plus_y37, tmp));
  }

  //Test DAXPY DoubleMatrix1D DoubleMatrix1D interface
  @Test
  public void testDAXPY_ans_eq_D1D_x1_plus_D1D_y1() {
    double[] tmp = BLAS1.daxpy(new DoubleMatrix1D(x1), new DoubleMatrix1D(y1));
    assertTrue(Arrays.equals(x1_plus_y1, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_D1D_x16_plus_D1D_y16() {
    double[] tmp = BLAS1.daxpy(new DoubleMatrix1D(x16), new DoubleMatrix1D(y16));
    assertTrue(Arrays.equals(x16_plus_y16, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_D1D_x37_plus_D1D_y37() {
    double[] tmp = BLAS1.daxpy(new DoubleMatrix1D(x37), new DoubleMatrix1D(y37));
    assertTrue(Arrays.equals(x37_plus_y37, tmp));
  }

  /** y:=alpha*x + y */
  //Test DAXPY double[] double[] interface
  @Test
  public void testDAXPY_ans_eq_alpha_x1_plus_y1() {
    double[] tmp = BLAS1.daxpy(alpha, x1, y1);
    assertTrue(Arrays.equals(alpha_times_x1_plus_y1, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_alpha_x16_plus_y16() {
    double[] tmp = BLAS1.daxpy(alpha, x16, y16);
    assertTrue(Arrays.equals(alpha_times_x16_plus_y16, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_alpha_x37_plus_y37() {
    double[] tmp = BLAS1.daxpy(alpha, x37, y37);
    assertTrue(Arrays.equals(alpha_times_x37_plus_y37, tmp));
  }

  //Test DAXPY double[] DoubleMatrix1D interface
  @Test
  public void testDAXPY_ans_eq_alpha_x1_plus_D1D_y1() {
    double[] tmp = BLAS1.daxpy(alpha, x1, new DoubleMatrix1D(y1));
    assertTrue(Arrays.equals(alpha_times_x1_plus_y1, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_alpha_x16_plus_D1D_y16() {
    double[] tmp = BLAS1.daxpy(alpha, x16, new DoubleMatrix1D(y16));
    assertTrue(Arrays.equals(alpha_times_x16_plus_y16, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_alpha_x37_plus_D1D_y37() {
    double[] tmp = BLAS1.daxpy(alpha, x37, new DoubleMatrix1D(y37));
    assertTrue(Arrays.equals(alpha_times_x37_plus_y37, tmp));
  }

  //Test DAXPY DoubleMatrix1D double[] interface
  @Test
  public void testDAXPY_ans_eq_alpha_D1D_x1_plus_y1() {
    double[] tmp = BLAS1.daxpy(alpha, new DoubleMatrix1D(x1), y1);
    assertTrue(Arrays.equals(alpha_times_x1_plus_y1, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_alpha_D1D_x16_plus_y16() {
    double[] tmp = BLAS1.daxpy(alpha, new DoubleMatrix1D(x16), y16);
    assertTrue(Arrays.equals(alpha_times_x16_plus_y16, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_alpha_D1D_x37_plus_y37() {
    double[] tmp = BLAS1.daxpy(alpha, new DoubleMatrix1D(x37), y37);
    assertTrue(Arrays.equals(alpha_times_x37_plus_y37, tmp));
  }

  //Test DAXPY DoubleMatrix1D DoubleMatrix1D interface
  @Test
  public void testDAXPY_ans_eq_alpha_D1D_x1_plus_D1D_y1() {
    double[] tmp = blas1.daxpy(alpha, new DoubleMatrix1D(x1), new DoubleMatrix1D(y1));
    assertTrue(Arrays.equals(alpha_times_x1_plus_y1, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_alpha_D1D_x16_plus_D1D_y16() {
    double[] tmp = blas1.daxpy(alpha, new DoubleMatrix1D(x16), new DoubleMatrix1D(y16));
    assertTrue(Arrays.equals(alpha_times_x16_plus_y16, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_alpha_D1D_x37_plus_D1D_y37() {
    double[] tmp = blas1.daxpy(alpha, new DoubleMatrix1D(x37), new DoubleMatrix1D(y37));
    assertTrue(Arrays.equals(alpha_times_x37_plus_y37, tmp));
  }

  //test fall through if(alpha==0)
  @Test
  public void testDAXPY_ans_eq_alphaZERO_x1_plus_y1() {
    double[] tmp = BLAS1.daxpy(0, x1, y1);
    assertTrue(Arrays.equals(y1, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_alphaZERO_x16_plus_y16() {
    double[] tmp = BLAS1.daxpy(0, x16, y16);
    assertTrue(Arrays.equals(y16, tmp));
  }

  @Test
  public void testDAXPY_ans_eq_alphaZERO_x37_plus_y37() {
    double[] tmp = BLAS1.daxpy(0, x37, y37);
    assertTrue(Arrays.equals(y37, tmp));
  }

  /** Test the in places */
  /** Statefull: y:=x+y */
  //Test DAXPY double[] double[] interface
  @Test
  public void testDAXPY_y1_eq_x1_plus_y1() {
    double[] tmp = new double[y1.length];
    System.arraycopy(y1, 0, tmp, 0, y1.length);
    BLAS1.daxpyInplace(x1, tmp);
    assertTrue(Arrays.equals(x1_plus_y1, tmp));
  }

  @Test
  public void testDAXPY_y16_eq_x16_plus_y16() {
    double[] tmp = new double[y16.length];
    System.arraycopy(y16, 0, tmp, 0, y16.length);
    BLAS1.daxpyInplace(x16, tmp);
    assertTrue(Arrays.equals(x16_plus_y16, tmp));
  }

  @Test
  public void testDAXPY_y37_eq_x37_plus_y37() {
    double[] tmp = new double[y37.length];
    System.arraycopy(y37, 0, tmp, 0, y37.length);
    BLAS1.daxpyInplace(x37, tmp);
    assertTrue(Arrays.equals(x37_plus_y37, tmp));
  }

  //Test DAXPY DoubleMatrix1D double[] interface
  @Test
  public void testDAXPY_D1D_y1_eq_x1_plus_D1D_y1() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(y1);
    BLAS1.daxpyInplace(x1, tmp);
    assertTrue(Arrays.equals(x1_plus_y1, tmp.getData()));
  }

  @Test
  public void testDAXPY_D1D_y16_eq_x16_plus_D1D_y16() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(y16);
    BLAS1.daxpyInplace(x16, tmp);
    assertTrue(Arrays.equals(x16_plus_y16, tmp.getData()));
  }

  @Test
  public void testDAXPY_D1D_y37_eq_x37_plus_D1D_y37() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(y37);
    BLAS1.daxpyInplace(x37, tmp);
    assertTrue(Arrays.equals(x37_plus_y37, tmp.getData()));
  }

  //Test DAXPY double[] DoubleMatrix1D interface
  @Test
  public void testDAXPY_y1_eq_D1D_x1_plus_y1() {
    double[] tmp = new double[y1.length];
    System.arraycopy(y1, 0, tmp, 0, y1.length);
    BLAS1.daxpyInplace(new DoubleMatrix1D(x1), tmp);
    assertTrue(Arrays.equals(x1_plus_y1, tmp));
  }

  @Test
  public void testDAXPY_y16_eq_D1D_x16_plus_y16() {
    double[] tmp = new double[y16.length];
    System.arraycopy(y16, 0, tmp, 0, y16.length);
    BLAS1.daxpyInplace(new DoubleMatrix1D(x16), tmp);
    assertTrue(Arrays.equals(x16_plus_y16, tmp));
  }

  @Test
  public void testDAXPY_y37_eq_D1D_x37_plus_y37() {
    double[] tmp = new double[y37.length];
    System.arraycopy(y37, 0, tmp, 0, y37.length);
    BLAS1.daxpyInplace(new DoubleMatrix1D(x37), tmp);
    assertTrue(Arrays.equals(x37_plus_y37, tmp));
  }

  //Test DAXPY DoubleMatrix1D DoubleMatrix1D interface
  @Test
  public void testDAXPY_D1D_y1_eq_D1D_x1_plus_D1D_y1() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(y1);
    DoubleMatrix1D tmp2 = new DoubleMatrix1D(x1);
    BLAS1.daxpyInplace(tmp2, tmp);
    assertTrue(Arrays.equals(x1_plus_y1, tmp.getData()));
  }

  @Test
  public void testDAXPY_D1D_y16_eq_D1D_x16_plus_D1D_y16() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(y16);
    DoubleMatrix1D tmp2 = new DoubleMatrix1D(x16);
    BLAS1.daxpyInplace(tmp2, tmp);
    assertTrue(Arrays.equals(x16_plus_y16, tmp.getData()));
  }

  @Test
  public void testDAXPY_D1D_y37_eq_D1D_x37_plus_D1D_y37() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(y37);
    DoubleMatrix1D tmp2 = new DoubleMatrix1D(x37);
    BLAS1.daxpyInplace(tmp2, tmp);
    assertTrue(Arrays.equals(x37_plus_y37, tmp.getData()));
  }

  /** Statefull y:=alpha*x+y */
  //Test DAXPY double[] double[] interface
  @Test
  public void testDAXPY_y1_eq_alphaZERO_times_x1_plus_y1() {
    double[] tmp = new double[y1.length];
    System.arraycopy(y1, 0, tmp, 0, y1.length);
    BLAS1.daxpyInplace(0, x1, tmp);
    assertTrue(Arrays.equals(y1, tmp));
  }

  @Test
  public void testDAXPY_y16_eq_alphaZERO_times_x16_plus_y16() {
    double[] tmp = new double[y16.length];
    System.arraycopy(y16, 0, tmp, 0, y16.length);
    BLAS1.daxpyInplace(0, x16, tmp);
    assertTrue(Arrays.equals(y16, tmp));
  }

  @Test
  public void testDAXPY_y37_eq_alphaZERO_times_x37_plus_y37() {
    double[] tmp = new double[y37.length];
    System.arraycopy(y37, 0, tmp, 0, y37.length);
    BLAS1.daxpyInplace(0, x37, tmp);
    assertTrue(Arrays.equals(y37, tmp));
  }

  //Test DAXPY DoubleMatrix1D double[] interface
  @Test
  public void testDAXPY_D1D_y1_eq_alpha_times_x1_plus_D1D_y1() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(y1);
    BLAS1.daxpyInplace(alpha, x1, tmp);
    assertTrue(Arrays.equals(alpha_times_x1_plus_y1, tmp.getData()));
  }

  @Test
  public void testDAXPY_D1D_y16_eq_alpha_times_x16_plus_D1D_y16() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(y16);
    BLAS1.daxpyInplace(alpha, x16, tmp);
    assertTrue(Arrays.equals(alpha_times_x16_plus_y16, tmp.getData()));
  }

  @Test
  public void testDAXPY_D1D_y37_eq_alpha_times_x37_plus_D1D_y37() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(y37);
    BLAS1.daxpyInplace(alpha, x37, tmp);
    assertTrue(Arrays.equals(alpha_times_x37_plus_y37, tmp.getData()));
  }

  //Test DAXPY double[] DoubleMatrix1D interface
  @Test
  public void testDAXPY_y1_eq_alpha_times_D1D_x1_plus_y1() {
    double[] tmp = new double[y1.length];
    System.arraycopy(y1, 0, tmp, 0, y1.length);
    BLAS1.daxpyInplace(alpha, new DoubleMatrix1D(x1), tmp);
    assertTrue(Arrays.equals(alpha_times_x1_plus_y1, tmp));
  }

  @Test
  public void testDAXPY_y16_eq_alpha_times_D1D_x16_plus_y16() {
    double[] tmp = new double[y16.length];
    System.arraycopy(y16, 0, tmp, 0, y16.length);
    BLAS1.daxpyInplace(alpha, new DoubleMatrix1D(x16), tmp);
    assertTrue(Arrays.equals(alpha_times_x16_plus_y16, tmp));
  }

  @Test
  public void testDAXPY_y37_eq_alpha_times_D1D_x37_plus_y37() {
    double[] tmp = new double[y37.length];
    System.arraycopy(y37, 0, tmp, 0, y37.length);
    BLAS1.daxpyInplace(alpha, new DoubleMatrix1D(x37), tmp);
    assertTrue(Arrays.equals(alpha_times_x37_plus_y37, tmp));
  }

  //Test DAXPY DoubleMatrix1D DoubleMatrix1D interface
  @Test
  public void testDAXPY_D1D_y1_eq_alpha_times_D1D_x1_plus_D1D_y1() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(y1);
    DoubleMatrix1D tmp2 = new DoubleMatrix1D(x1);
    blas1.daxpyInplace(alpha, tmp2, tmp);
    assertTrue(Arrays.equals(alpha_times_x1_plus_y1, tmp.getData()));
  }

  @Test
  public void testDAXPY_D1D_y16_eq_alpha_times_D1D_x16_plus_D1D_y16() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(y16);
    DoubleMatrix1D tmp2 = new DoubleMatrix1D(x16);
    blas1.daxpyInplace(alpha, tmp2, tmp);
    assertTrue(Arrays.equals(alpha_times_x16_plus_y16, tmp.getData()));
  }

  @Test
  public void testDAXPY_D1D_y37_eq_alpha_times_D1D_x37_plus_D1D_y37() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(y37);
    DoubleMatrix1D tmp2 = new DoubleMatrix1D(x37);
    blas1.daxpyInplace(alpha, tmp2, tmp);
    assertTrue(Arrays.equals(alpha_times_x37_plus_y37, tmp.getData()));
  }

  //test fall through if(alpha==0)
  @Test
  public void testDAXPY_y1_eq_alpha_times_x1_plus_y1() {
    double[] tmp = new double[y1.length];
    System.arraycopy(y1, 0, tmp, 0, y1.length);
    BLAS1.daxpyInplace(alpha, x1, tmp);
    assertTrue(Arrays.equals(alpha_times_x1_plus_y1, tmp));
  }

  @Test
  public void testDAXPY_y16_eq_alpha_times_x16_plus_y16() {
    double[] tmp = new double[y16.length];
    System.arraycopy(y16, 0, tmp, 0, y16.length);
    BLAS1.daxpyInplace(alpha, x16, tmp);
    assertTrue(Arrays.equals(alpha_times_x16_plus_y16, tmp));
  }

  @Test
  public void testDAXPY_y37_eq_alpha_times_x37_plus_y37() {
    double[] tmp = new double[y37.length];
    System.arraycopy(y37, 0, tmp, 0, y37.length);
    BLAS1.daxpyInplace(alpha, x37, tmp);
    assertTrue(Arrays.equals(alpha_times_x37_plus_y37, tmp));
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////// DSCAL /////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////

  // test stateless

  //Test DSCAL null checker
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDSCALstateless_nullcheck() {
    BLAS1.dscal(alpha, xnull);
  }

  //Test DSCAL double[] interface
  @Test
  public void testDSCAL_ans_eq_alpha_times_x1() {
    double[] tmp = BLAS1.dscal(alpha, x1);
    assertTrue(Arrays.equals(alpha_times_x1, tmp));
  }

  @Test
  public void testDSCAL_ans_eq_alpha_times_x16() {
    double[] tmp = BLAS1.dscal(alpha, x16);
    assertTrue(Arrays.equals(alpha_times_x16, tmp));
  }

  @Test
  public void testDSCAL_ans_eq_alpha_times_x37() {
    double[] tmp = BLAS1.dscal(alpha, x37);
    assertTrue(Arrays.equals(alpha_times_x37, tmp));
  }

  //Test DSCAL DoubleMatrix1D interface
  @Test
  public void testDSCAL_ans_eq_alpha_times_D1D_x1() {
    double[] tmp = BLAS1.dscal(alpha, new DoubleMatrix1D(x1));
    assertTrue(Arrays.equals(alpha_times_x1, tmp));
  }

  @Test
  public void testDSCAL_ans_eq_alpha_times_D1D_x16() {
    double[] tmp = BLAS1.dscal(alpha, new DoubleMatrix1D(x16));
    assertTrue(Arrays.equals(alpha_times_x16, tmp));
  }

  @Test
  public void testDSCAL_ans_eq_alpha_times_D1D_x37() {
    double[] tmp = BLAS1.dscal(alpha, new DoubleMatrix1D(x37));
    assertTrue(Arrays.equals(alpha_times_x37, tmp));
  }

  // test stateful
  //Test DSCAL null checker
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDSCALstatefull_nullcheck() {
    BLAS1.dscalInplace(alpha, xnull);
  }

  // test DSCAL double[] interface
  @Test
  public void testDSCAL_x1_eq_alpha_times_x1() {
    double[] tmp = new double[x1.length];
    System.arraycopy(x1, 0, tmp, 0, x1.length);
    BLAS1.dscalInplace(alpha, tmp);
    assertTrue(Arrays.equals(alpha_times_x1, tmp));
  }

  @Test
  public void testDSCAL_x16_eq_alpha_times_x16() {
    double[] tmp = new double[x16.length];
    System.arraycopy(x16, 0, tmp, 0, x16.length);
    BLAS1.dscalInplace(alpha, tmp);
    assertTrue(Arrays.equals(alpha_times_x16, tmp));
  }

  @Test
  public void testDSCAL_x37_eq_alpha_times_x37() {
    double[] tmp = new double[x37.length];
    System.arraycopy(x37, 0, tmp, 0, x37.length);
    BLAS1.dscalInplace(alpha, tmp);
    assertTrue(Arrays.equals(alpha_times_x37, tmp));
  }

  //test DSCAL DoubleMatrix1D interface
  @Test
  public void testDSCAL_D1D_x1_eq_alpha_times_D1D_x1() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(x1);
    BLAS1.dscalInplace(alpha, tmp);
    assertTrue(Arrays.equals(alpha_times_x1, tmp.getData()));
  }

  @Test
  public void testDSCAL_D1D_x16_eq_alpha_times_D1D_x16() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(x16);
    BLAS1.dscalInplace(alpha, tmp);
    assertTrue(Arrays.equals(alpha_times_x16, tmp.getData()));
  }

  @Test
  public void testDSCAL_D1D_x37_eq_alpha_times_D1D_x37() {
    DoubleMatrix1D tmp = new DoubleMatrix1D(x37);
    BLAS1.dscalInplace(alpha, tmp);
    assertTrue(Arrays.equals(alpha_times_x37, tmp.getData()));
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////// DSWAP /////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDSWAP_nullX() {
    BLAS1.dswapInplace(xnull, y1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDSWAP_nullY() {
    BLAS1.dswapInplace(x1, ynull);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDSWAP_badLengths() {
    BLAS1.dswapInplace(x1, y16);
  }

  @Test
  public void testDSWAP_x37_swap_y37() {
    double[] tmpX = new double[x37.length];
    double[] tmpY = new double[y37.length];
    System.arraycopy(x37, 0, tmpX, 0, x37.length);
    System.arraycopy(y37, 0, tmpY, 0, y37.length);
    BLAS1.dswapInplace(tmpX, tmpY);
    assertTrue(Arrays.equals(tmpX, y37));
    assertTrue(Arrays.equals(tmpY, x37));
  }

  @Test
  public void testDSWAP_D1D_x37_swap_y37() {
    DoubleMatrix1D tmpX = new DoubleMatrix1D(x37);
    double[] tmpY = new double[y37.length];
    System.arraycopy(y37, 0, tmpY, 0, y37.length);
    BLAS1.dswapInplace(tmpX, tmpY);
    assertTrue(Arrays.equals(tmpX.getData(), y37));
    assertTrue(Arrays.equals(tmpY, x37));
  }

  @Test
  public void testDSWAP_x37_swap_D1D_y37() {
    DoubleMatrix1D tmpY = new DoubleMatrix1D(y37);
    double[] tmpX = new double[x37.length];
    System.arraycopy(x37, 0, tmpX, 0, x37.length);
    BLAS1.dswapInplace(tmpX, tmpY);
    assertTrue(Arrays.equals(tmpY.getData(), x37));
    assertTrue(Arrays.equals(tmpX, y37));
  }

  @Test
  public void testDSWAP_D1D_x37_swap_D1D_y37() {
    DoubleMatrix1D tmpX = new DoubleMatrix1D(x37);
    DoubleMatrix1D tmpY = new DoubleMatrix1D(y37);
    BLAS1.dswapInplace(tmpX, tmpY);
    assertTrue(Arrays.equals(tmpX.getData(), y37));
    assertTrue(Arrays.equals(tmpY.getData(), x37));
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////// DCOPY /////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDCOPY_nullX() {
    BLAS1.dcopyInplace(xnull, y1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDCOPY_nullY() {
    BLAS1.dcopyInplace(xnull, y1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDCOPY_badLengths() {
    BLAS1.dcopyInplace(x1, y16);
  }

  @Test
  public void testDCOPY_x37_copy_y37() {
    double[] tmpX = new double[x37.length];
    double[] tmpY = new double[y37.length];
    System.arraycopy(x37, 0, tmpX, 0, x37.length);
    System.arraycopy(y37, 0, tmpY, 0, y37.length);
    BLAS1.dcopyInplace(tmpX, tmpY);
    assertTrue(Arrays.equals(tmpX, y37));
    assertTrue(Arrays.equals(tmpY, y37));
  }

  @Test
  public void testDCOPY_D1D_x37_copy_y37() {
    DoubleMatrix1D tmpX = new DoubleMatrix1D(x37);
    double[] tmpY = new double[y37.length];
    System.arraycopy(y37, 0, tmpY, 0, y37.length);
    BLAS1.dcopyInplace(tmpX, tmpY);
    assertTrue(Arrays.equals(tmpX.getData(), y37));
    assertTrue(Arrays.equals(tmpY, y37));
  }

  @Test
  public void testDCOPY_x37_copy_D1D_y37() {
    DoubleMatrix1D tmpY = new DoubleMatrix1D(y37);
    double[] tmpX = new double[x37.length];
    System.arraycopy(x37, 0, tmpX, 0, x37.length);
    BLAS1.dcopyInplace(tmpX, tmpY);
    assertTrue(Arrays.equals(tmpX, y37));
    assertTrue(Arrays.equals(tmpY.getData(), y37));
  }

  @Test
  public void testDCOPY_D1D_x37_copy_D1D_y37() {
    DoubleMatrix1D tmpY = new DoubleMatrix1D(y37);
    DoubleMatrix1D tmpX = new DoubleMatrix1D(x37);
    BLAS1.dcopyInplace(tmpX, tmpY);
    assertTrue(Arrays.equals(tmpX.getData(), y37));
    assertTrue(Arrays.equals(tmpY.getData(), y37));
  }

  
  ///////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////// DDOT /////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDDOT_nullX() {
    BLAS1.ddot(xnull, y1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDDOT_nullY() {
    BLAS1.ddot(xnull, y1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDDOT_badLengths() {
    BLAS1.ddot(x1, y16);
  }

  @Test
  public void testDDOT_x37_dot_y37() {
    assertTrue(BLAS1.ddot(x37, y37)==175750);
  }

  @Test
  public void testDDOT_D1D_x37_dot_y37() {
    assertTrue(BLAS1.ddot(new DoubleMatrix1D(x37).getData(), y37)==175750);
  }

  @Test
  public void testDDOT_x37_dot_D1D_y37() {
    assertTrue(BLAS1.ddot(x37, new DoubleMatrix1D(y37).getData())==175750);
  }

  @Test
  public void testDDOT_D1D_x37_dot_D1D__y37() {
    assertTrue(BLAS1.ddot(new DoubleMatrix1D(x37).getData(), new DoubleMatrix1D(y37).getData())==175750);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////// DASUM /////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDASUM_nullX() {
    BLAS1.dasum(xnull);
  }
  
  @Test
  public void testDASUM() {
    assertTrue(BLAS1.dasum(x37)==703);
  }

  @Test
  public void testDASUM_D1D() {
    assertTrue(BLAS1.dasum(new DoubleMatrix1D(x37).getData())==703);
  }  
  
  
  ///////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////// DNRM2 /////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDNRM2_nullX(){
    BLAS1.dnrm2(xnull);
  }

  @Test
  public void testDNRM2_x16(){
    assertTrue(Math.abs(Math.pow(BLAS1.dnrm2(x16),2)-1496)<1e-16);
  }

  @Test
  public void testDNRM2_D1D_x16(){
    assertTrue(Math.abs(Math.pow(BLAS1.dnrm2(new DoubleMatrix1D(x16).getData()),2)-1496)<1e-16);
  }  
  
  ///////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////// IDMAX /////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testIDMAX_nullX() {
    BLAS1.idmax(xnull);
  }
  
  @Test
  public void testIDMAX() {
    assertTrue(BLAS1.idmax(xSS)==3);
  }

  @Test
  public void testIDMAX_D1D() {
    assertTrue(BLAS1.idmax(new DoubleMatrix1D(xSS).getData())==3);
  }  
  
  ///////////////////////////////////////////////////////////////////////////////////////////////
  /* HELPERS *//* HELPERS *//* HELPERS *//* HELPERS *//* HELPERS *//* HELPERS *//* HELPERS */
  ///////////////////////////////////////////////////////////////////////////////////////////////

  /** helper functions to generate number ranges */
  private double[] range(int low, int high) {
    assert (high >= low);
    final int lim = (high - low) > 0 ? (high - low) + 1 : 1;
    double[] tmp = new double[lim];
    for (int i = 0; i < lim; i++) {
      tmp[i] = low + i;
    }
    return tmp;
  }

  private double[] range(int low, int high, int step) {
    assert (high >= low);
    assert (step > 0);
    final int t = (high - low) > 0 ? (high - low) + step : 1;
    final int lim = t / step;
    double[] tmp = new double[lim];
    for (int i = 0; i < lim; i++) {
      tmp[i] = low + i * step;
    }
    return tmp;
  }

}
