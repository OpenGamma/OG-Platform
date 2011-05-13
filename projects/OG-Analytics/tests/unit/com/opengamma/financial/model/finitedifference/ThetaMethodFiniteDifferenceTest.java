/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.testng.annotations.Test;

/**
 * Test ThetaMethodFiniteDifference when theta = 0.5 (i.e. A Crank-Nicolson scheme)
 */
public class ThetaMethodFiniteDifferenceTest {

  private static final ConvectionDiffusionPDESolverTestCase TESTER = new ConvectionDiffusionPDESolverTestCase();
  private static final ThetaMethodFiniteDifference SOLVER = new ThetaMethodFiniteDifference(1.0);

  @Test
  public void testBlackScholesEquation() {
    int timeSteps = 10;
    int priceSteps = 100;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 3.0;
    double volTol = 5e-3;
    double priceTol = 5e-2;
    double deltaTol = 5e-2;
    double gammaTol = 1.0; // Crank-Nicolson gives awful greeks around ATM - this is why it shouldn't be used
    boolean print = true; // set to false before pushing

    TESTER.testBlackScholesEquationUniformGrid(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
  }

  @Test
  public void testBlackScholesEquationNonuniformGrid() {
    int timeSteps = 10;
    int priceSteps = 100;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 3.0;
    double volTol = 2e-2; // Crank-Nicolson does not play well with non-uniform grids
    double priceTol = 5e-2;
    double deltaTol = 5e-1;
    double gammaTol = 2e1;// Doesn't even get the sign of gamma right ATM
    boolean print = false; // set to false before pushing

    TESTER.testBlackScholesEquationNonuniformGrid(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
  }

  @Test
  public void testLogBlackScholesEquation() {
    int timeSteps = 10;
    int priceSteps = 100;
    double lowerMoneyness = 0.3;
    double upperMoneyness = 4.0;
    double volTol = 5e-3;
    double priceTol = 5e-2;
    double deltaTol = 5e-2;
    double gammaTol = 5.0; // Crank-Nicolson gives awful greeks around ATM - this is why it shouldn't be used
    boolean print = false; // set to false before pushing

    TESTER.testLogTransformedBlackScholesEquation(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
  }

  @Test
  public void testCEV() {
    int timeSteps = 10;
    int priceSteps = 100;
    double lowerMoneyness = 0.3;
    double upperMoneyness = 3.0;
    double volTol = 5e-3;
    boolean print = false; // set to false before pushing

    TESTER.testCEV(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, print);
  }

  @Test
  public void testAmericanPrice() {
    int timeSteps = 10;
    int priceSteps = 100;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 3.0;
    double priceTol = 1e-3;
    boolean print = false; // set to false before pushing
    TESTER.testAmericanPrice(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, priceTol, print);
  }

}
