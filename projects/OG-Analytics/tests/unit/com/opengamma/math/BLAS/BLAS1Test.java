/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.BLAS;

import java.util.Arrays;

import org.testng.annotations.Test;


/**
 * Tests the BLAS1 library
 */
public class BLAS1Test {
  // tests 1element short vector
  double[] x1 = {1};
  double[] y1 = {10};
  double[] x1_times_y1={10};

  // hits loop unwind max
  double[] x16 = range(1,16);
  double[] y16 = range(10,160,10);
  double[] x16_times_y16 = {10,40,90,160,250,360,490,640,810,1000,1210,1440,1690,1960,2250,2560};

  // trips loop unwinds to hit clean up code
  double[] x37 = {1};
  double[] y37 = {10};
  double[] x37_times_y37 = {10,40,90,160,250,360,490,640,810,1000,1210,1440,1690,1960,2250,2560,2890,3240,3610,4000,4410,4840,5290,5760,6250,6760,7290,7840,8410,9000,9610,10240,10890,11560,12250,12960,13690};

  BLAS1 blas1 = new BLAS1();

@Test
public void testDAXPY_ans_eq_x1_times_y1() {
  double[] tmp = blas1.daxpy(y1, x1);
  Arrays.equals(x1_times_y1, tmp);
}

@Test
public void testDAXPY_ans_eq_x16_times_y16() {
  double[] tmp = blas1.daxpy(y16, x16);
  Arrays.equals(x16_times_y16, tmp);
}

@Test
public void testDAXPY_ans_eq_x37_times_y37() {
  double[] tmp = blas1.daxpy(y37, x37);
  Arrays.equals(x37_times_y37, tmp);
}


/* helper functions to generate number ranges */
  private double[] range(int low, int high) {
    assert(high>=low);
    final int lim = (high - low) > 0 ? (high - low) : 1;
    double [] tmp = new double[lim];
    for ( int i = 0; i < lim; i++) {
      tmp[i] = low + i;
    }
    return tmp;
  }

  private double[] range(int low, int high, int step) {
    assert(high>=low);
    assert(step>0);
    final int t = (high - low) > 0 ? (high - low) : 1;
    final int lim = t / step;
    double [] tmp = new double[lim];
    for ( int i = 0; i < lim; i++) {
      tmp[i] = low + i*step;
    }
    return tmp;
  }


}
