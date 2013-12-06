/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RichardsonExtrapolationFiniteDifferenceTest {

  private static final ConvectionDiffusionPDESolverTestCase TESTER = new ConvectionDiffusionPDESolverTestCase();
  private static final ConvectionDiffusionPDESolver BASE_SOLVER = new ThetaMethodFiniteDifference(1.0, false);
  private static final ConvectionDiffusionPDESolver SOLVER = new RichardsonExtrapolationFiniteDifference(BASE_SOLVER);

  @Test
  public void testBlackScholesEquation() {
    int timeSteps = 10;
    int priceSteps = 100;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 3.0;
    double volTol = 1e-2; // These tolerances are not as good as running Crank-Nicolson with 20 time steps
    double priceTol = 5e-2;
    double deltaTol = 1e-2;
    double gammaTol = 5e-2;
    boolean print = false; // set to false before pushing

    TESTER.testTimeExtrapolation(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
  }

}
