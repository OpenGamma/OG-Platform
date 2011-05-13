/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.testng.annotations.Test;

/**
 * 
 */
public class RichardsonExtrapolationFiniteDifferenceTest {

  private static final ConvectionDiffusionPDESolverTestCase TESTER = new ConvectionDiffusionPDESolverTestCase();
  private static final ThetaMethodFiniteDifference SOLVER = new ThetaMethodFiniteDifference(1.0);

  @Test
  public void testBlackScholesEquation() {
    int timeSteps = 10;
    int priceSteps = 100;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 3.0;
    double volTol = 5e-2;
    double priceTol = 5e-2;
    double deltaTol = 5e-2;
    double gammaTol = 1.0;
    boolean print = false; // set to false before pushing

    TESTER.testTimeExtrapolation(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
  }

}
