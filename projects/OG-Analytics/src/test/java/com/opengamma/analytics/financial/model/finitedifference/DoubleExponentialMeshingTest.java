/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DoubleExponentialMeshingTest {

  private static double LOWER = 1.2;
  private static double UPPER = 4.1;
  private static double CENTRE = 3.0;
  private static int N = 100;
  private static double LAMBDA_L = 5.0;
  private static double LAMBDA_U = 0.0;

  MeshingFunction MESHER = new DoubleExponentialMeshing(LOWER, UPPER, CENTRE, N, LAMBDA_L, LAMBDA_U);

  @Test
  public void testEndPoints() {
    assertEquals(LOWER, MESHER.evaluate(0));
    assertEquals(UPPER, MESHER.evaluate(N-1));
    double frac = (CENTRE-LOWER) / (UPPER - LOWER);
    assertEquals(CENTRE, MESHER.evaluate((int) (frac * N)-1));
  }

  @Test
  public void testIncreassing() {
    double vOld = LOWER;
    for (int i = 1; i < N; i++) {
      double vNew = MESHER.evaluate(i);
      assertTrue(vNew > vOld);
      vOld = vNew;
    }
  }

  @Test(enabled=false)
  public void testIncreassingprint() {

    for (int i = 0; i < N; i++) {
      double vNew = MESHER.evaluate(i);
      System.out.println(i+"\t"+vNew);
    }
  }


}
