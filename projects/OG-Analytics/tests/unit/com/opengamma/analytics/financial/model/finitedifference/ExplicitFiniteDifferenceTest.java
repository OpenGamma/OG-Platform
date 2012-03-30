/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.ExplicitFiniteDifference;

public class ExplicitFiniteDifferenceTest {

  private static final ConvectionDiffusionPDESolverTestCase TESTER = new ConvectionDiffusionPDESolverTestCase();
  @SuppressWarnings("deprecation")
  private static final ConvectionDiffusionPDESolver SOLVER = new ExplicitFiniteDifference();

  @Test
  public void testBlackScholesEquation() {
    int timeSteps = 2000; // need v large #time steps for explicit to be stable
    int priceSteps = 100;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 3.0;
    double volTol = 5e-3;
    double priceTol = 1e-2;
    double deltaTol = 5e-3; // the greeks are much better than C-N
    double gammaTol = 2e-2;
    boolean print = false; // set to false before pushing
    TESTER.testBlackScholesEquationUniformGrid(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
  }

  /**
   * This needs more price steps for the same accuracy, but can push to greater moneyness range
   */
  @Test
  public void testLogTransformedBlackScholesEquation() {
    int timeSteps = 2000;
    int priceSteps = 100;
    double lowerMoneyness = 0.3;
    double upperMoneyness = 4.0;
    double volTol = 5e-3;
    double priceTol = 2e-2;
    double deltaTol = 5e-2;
    double gammaTol = 2e-2;
    boolean print = false; // set to false before pushing
    TESTER.testLogTransformedBlackScholesEquation(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
  }

  @Test
  public void testCEV() {
    int timeSteps = 200;// TODO why is this working with so few steps?
    int priceSteps = 100;
    double lowerMoneyness = 0.3; // Not working well for ITM calls
    double upperMoneyness = 3.0;
    double volTol = 5e-3;
    boolean print = false; // set to false before pushing

    TESTER.testCEV(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, print);
  }

}
