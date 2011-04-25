/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.testng.annotations.Test;

import com.opengamma.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.financial.model.finitedifference.CrankNicolsonFiniteDifference;

/**
 * 
 */
public class CrankNicolsonFiniteDifferenceTest {

  private static final ConvectionDiffusionPDESolverTestCase TESTER = new ConvectionDiffusionPDESolverTestCase();
  private static final ConvectionDiffusionPDESolver SOLVER = new CrankNicolsonFiniteDifference();

  @Test
  public void testBlackScholesEquation() {
    int timeSteps = 10;
    int priceSteps = 100;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 3.0;
    TESTER.testBlackScholesEquation(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness);
  }

  /**
   * This needs more price steps for the same accuracy, but can push to greater moneyness range
   */
  @Test
  public void testLogTransformedBlackScholesEquation() {
    int timeSteps = 10;
    int priceSteps = 200;
    double lowerMoneyness = 0.3;
    double upperMoneyness = 4.0;
    TESTER.testLogTransformedBlackScholesEquation(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness);
  }

  @Test
  public void testCEV() {
    int timeSteps = 25;
    int priceSteps = 100;
    double lowerMoneyness = 1.0; // Not working well for ITM calls
    double upperMoneyness = 3.0;
    TESTER.testCEV(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness);
  }

}
