/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.BLAS;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.math.matrix.FullMatrix;

/**
 * Tests the BLAS2 library
 */
public class BLAS2Test {

  double [][] A ={{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14,15},{16,17,18,19,20},{21,22,23,24,25}};
  double [][] oddA ={{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,16},{17,18,19,20}};
  double [][] singleA = {{1}};
  double [] x = {1,2,3,4,5};
  double [] oddx = {1,2,3,4};
  double [] singlex = {2};
  double [] y = {10,20,30,40,50};
  double [] oddy = {10,20,30,40,50};
  double [] singley = {4};
  double alpha = 7.0;
  double beta = -3.0;
  FullMatrix aMatrix = new FullMatrix(A);
  FullMatrix oddaMatrix = new FullMatrix(oddA);
  FullMatrix singleaMatrix = new FullMatrix(singleA);

  // Answers to Full matrix DGEMV's
  double [] A_times_x = {55,130,205,280,355};
  double [] alpha_times_A_times_x = {385,910,1435,1960,2485};
  double [] A_times_x_plus_y = {65,150,235,320,405};
  double [] alpha_times_A_times_x_plus_y = {395, 930, 1465, 2000, 2535};
  double [] A_times_x_plus_beta_times_y = {25,70,115,160,205};
  double [] alpha_times_A_times_x_plus_beta_times_y = {355, 850, 1345, 1840, 2335};
  double [] alpha_times_oddA_times_oddx_plus_beta_times_oddy = {180, 430, 680, 930, 1180};
  double [] alpha_times_singleA_times_singlex_plus_beta_times_singley = {2};

  // stateless manipulators
@Test
public void testDGEMV_ans_eq_A_times_x() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,x),A_times_x));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_x() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,x),alpha_times_A_times_x));
}

@Test
public void testDGEMV_ans_eq_A_times_x_plus_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,x,y),A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_x_plus_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,x,y),alpha_times_A_times_x_plus_y));
}

@Test
public void testDGEMV_ans_eq_A_times_x_plus_beta_times_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(aMatrix,x,beta,y),A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_alpha_times_A_times_x_plus_beta_times_y() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,aMatrix,x,beta,y),alpha_times_A_times_x_plus_beta_times_y));
}

@Test
public void testDGEMV_ans_eq_alpha_times_oddA_times_oddx_plus_beta_times_oddy() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,oddaMatrix,oddx,beta,oddy),alpha_times_oddA_times_oddx_plus_beta_times_oddy));
}

public void testDGEMV_ans_eq_alpha_times_singleA_times_singlex_plus_beta_times_singley() {
  assertTrue(Arrays.equals(BLAS2.dgemv(alpha,singleaMatrix,singlex,beta,singley),alpha_times_singleA_times_singlex_plus_beta_times_singley));
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
public void testDGEMV_y_eq_alpha_times_A_times_x() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix,x,0);
  assertTrue(Arrays.equals(ycp,alpha_times_A_times_x));
}

@Test
public void testDGEMV_y_eq_alpha_times_A_times_x_plus_beta_times_y() {
  double [] ycp = new double[y.length];
  System.arraycopy(y, 0, ycp, 0, y.length);
  BLAS2.dgemvInPlace(ycp,alpha,aMatrix,x,beta);
  assertTrue(Arrays.equals(ycp,alpha_times_A_times_x_plus_beta_times_y));
}

}
