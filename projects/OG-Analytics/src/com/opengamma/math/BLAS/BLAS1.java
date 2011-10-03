/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.BLAS;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * Provides the BLAS level 1 behaviour for the OG matrix library.
 * Massive amounts of overloading goes on, beware and only use if confident.
*  METHODS: DAXPY
 */
public class BLAS1 {

  /**
   * DAXPY performs the following vector operation
   *
   *  y := alpha*x + y
   *
   *  where alpha is scalar, x and y are vectors.
   *
   *  For speed, the method is overloaded such that simplified calls can be
   *  made when different parts of the DAXPY operation are not needed.
   *
   */

  /**
   * Ensures that the inputs to DAXPY routines are sane when DAXPY is function(Vector,Vector).
   * @param v1 double[] is the vector to be tested (y)
   * @param v2 double[] is the vector to be tested (x)
   */
  public static void daxpyInputSanityChecker(double[] v1, double[] v2) {
    final int xlen = v1.length;
    final int ylen = v2.length;
    assertNotNull(v1); // check not null
    assertNotNull(v2); // check not null
    assertEquals(xlen, ylen); // going to struggle singleton addition unless the vectors are the same length
  }

  /**
   * DAXPY simplified: returns:=x+y
   * @param y a double[] vector
   * @param x a double[] vector
   * @return tmp double[] vector
   */
  public double[] daxpy(double[] y, double[] x) {
    final int n = y.length;
    double[] tmp = new double[n];
    for (int i = 0; i < n; i++) {
      tmp[i] = y[i] + x[i];
    }
    final int extra = n - n % 16;
    final int ub = ((n / 16) * 16) - 1;
    // the induction (variable + loop unwind) common subexpression is actually spotted by the JIT and so
    // doesn't need to be spelled out which is a nice change
    for (int i = 0; i < ub; i += 16) {
      tmp[i] = x[i] + y[i];
      tmp[i + 1] = x[i + 1] + y[i + 1];
      tmp[i + 2] = x[i + 2] + y[i + 2];
      tmp[i + 3] = x[i + 3] + y[i + 3];
      tmp[i + 4] = x[i + 4] + y[i + 4];
      tmp[i + 5] = x[i + 5] + y[i + 5];
      tmp[i + 6] = x[i + 6] + y[i + 6];
      tmp[i + 7] = x[i + 7] + y[i + 7];
      tmp[i + 8] = x[i + 8] + y[i + 8];
      tmp[i + 9] = x[i + 9] + y[i + 9];
      tmp[i + 10] = x[i + 10] + y[i + 10];
      tmp[i + 11] = x[i + 11] + y[i + 11];
      tmp[i + 12] = x[i + 12] + y[i + 12];
      tmp[i + 13] = x[i + 13] + y[i + 13];
      tmp[i + 14] = x[i + 14] + y[i + 14];
      tmp[i + 15] = x[i + 15] + y[i + 15];
    }
    for (int i = extra; i < n; i++) {
      tmp[i] = x[i] + y[i];
    }
    return tmp;
  }

  /**
   * DAXPY simplified: returns:=x+y
   * @param y a DoubleMatrix1D vector
   * @param x a double[] vector
   * @return tmp double[] vector
   */
  public double[] daxpy(DoubleMatrix1D y, double[] x) {
    return daxpy(y.getData(), x);
  }

  /**
   * DAXPY simplified: returns:=x+y
   * @param y a double[] vector
   * @param x a DoubleMatrix1D vector
   * @return tmp double[] vector
   */
  public double[] daxpy(double[] y, DoubleMatrix1D x) {
    return daxpy(y, x.getData());
  }

  /**
   * DAXPY simplified: returns:=x+y
   * @param y a DoubleMatrix1D vector
   * @param x a DoubleMatrix1D vector
   * @return tmp double[] vector
   */
  public double[] daxpy(DoubleMatrix1D y, DoubleMatrix1D x) {
    return daxpy(y.getData(), x.getData());
  }



} // class end

