/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BjerksundStenslandModelDualDeltaGammaSolverTest {

  @Test
      (enabled = false)
      public void deltaGammaTest() {
    System.out.println("BjerksundStenslandModelDualDeltaGammaSolverTest.deltaGammaTest");
    BjerksundStenslandModel bs = new BjerksundStenslandModel();
    //BjerksundStenslandModelDualDeltaGammaSolver temp = new BjerksundStenslandModelDualDeltaGammaSolver();
    final double[] s0Set = new double[] {60, 90, 100, 110, 160 };
    final double k = 100;
    final double r = 0.1;
    final double[] bSet = new double[] {-0.04, 0.0, 0.04, 0.09, 0.11 };
    final double sigma = 0.35;
    final double t = 0.5;

    final boolean isCall = false;

    for (double s0 : s0Set) {
      for (double b : bSet) {

        final double eps = s0 * 1e-5;

        //final double[] sense = temp.getCallDualDeltaGamma(k, s0, r - b, -b, t, sigma);
        final double[] sense = bs.getPutDeltaGamma(s0, k, r, b, t, sigma);
        double[] up = bs.getPriceAdjoint(s0 + eps, k, r, b, t, sigma, isCall);
        double[] down = bs.getPriceAdjoint(s0 - eps, k, r, b, t, sigma, isCall);

        double fd2 = (up[0] + down[0] - 2 * sense[0]) / eps / eps;
        System.out.println("s0=" + s0 + "\t" + "b=" + b);
        System.out.println(fd2);
        System.out.println(sense[2]);
        System.out.println("\n");
        assertEquals(fd2, sense[2], Math.abs(fd2) * 1e-4);
      }
    }
  }

}
