/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;

/**
 * Test ThetaMethodFiniteDifference when theta = 0.5 (i.e. A Crank-Nicolson scheme)
 */
@Test(groups = TestGroup.UNIT)
public class ThetaMethodFiniteDifferenceTest {

  private static final ConvectionDiffusionPDESolverTestCase TESTER = new ConvectionDiffusionPDESolverTestCase();
  private static final ThetaMethodFiniteDifference SOLVER = new ThetaMethodFiniteDifference(0.5, false);

  @Test
  public void testBlackScholesEquation1() {
    int timeSteps = 10; //10 // with this few steps get massive oscillations in implied vol & gamma around ATM
    int priceSteps = 100;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 3.0;
    double volTol = 5e-3;
    double priceTol = 5e-2;
    double deltaTol = 5e-2;
    double gammaTol = 1.0; // Crank-Nicolson gives awful greeks around ATM - this is why it shouldn't be used
    boolean print = false; // set to false before pushing

    TESTER.testBlackScholesEquationUniformGrid(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
  }

  @Test
  public void testBlackScholesEquation2() {
    int warmups = 1;
    int benchmarkCycles = 0;
    final Logger logger = LoggerFactory.getLogger(ThetaMethodFiniteDifferenceTest.class);

    int timeSteps = 20;
    int priceSteps = 100;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 3.0;
    double volTol = 5e-3;
    double priceTol = 1e-2; // 5 times better than with 10 time steps
    double deltaTol = 5e-3; // 10 times better than with 10 time steps
    double gammaTol = 2e-2; // 50 times better than with 10 time steps
    boolean print = false; // set to false before pushing

    for (int i = 0; i < warmups; i++) {
      TESTER.testBlackScholesEquationUniformGrid(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
    }
    if (benchmarkCycles > 0) {
      final OperationTimer timer = new OperationTimer(logger, "processing {} cycles on testBlackScholesEquation2", benchmarkCycles);
      for (int i = 0; i < benchmarkCycles; i++) {
        TESTER.testBlackScholesEquationUniformGrid(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
      }
      timer.finished();
    }
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
    double volTol = 1e-2;
    boolean print = false; // set to false before pushing

    TESTER.testCEV(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, print);
  }

  @Test
  public void testAmericanPrice() {
    int timeSteps = 10;
    int priceSteps = 100;
    double lowerMoneyness = 0.4;
    double upperMoneyness = 3.0;
    boolean print = false; // set to false before pushing
    TESTER.testAmericanPrice(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, print);
  }

}
