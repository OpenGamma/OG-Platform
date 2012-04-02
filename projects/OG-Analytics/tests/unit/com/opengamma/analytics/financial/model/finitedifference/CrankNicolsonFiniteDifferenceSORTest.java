package com.opengamma.analytics.financial.model.finitedifference;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.CrankNicolsonFiniteDifferenceSOR;

/**
 * This test the old Crank-Nicolson (strictly a theta method) code 
 */
public class CrankNicolsonFiniteDifferenceSORTest {

  private static final ConvectionDiffusionPDESolverTestCase TESTER = new ConvectionDiffusionPDESolverTestCase();
  private static final ConvectionDiffusionPDESolver SOLVER = new CrankNicolsonFiniteDifferenceSOR(0.5);

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
    boolean print = false; // set to false before pushing

    TESTER.testBlackScholesEquationUniformGrid(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness, volTol, priceTol, deltaTol, gammaTol, print);
  }

  // @Test(enabled = false)
  // public void testSpaceExtrapolation() {
  // int timeSteps = 10;
  // int priceSteps = 100;
  // double lowerMoneyness = 0.4;
  // double upperMoneyness = 3.0;
  // TESTER.testSpaceExtrapolation(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness);
  // }
  //
  // @Test(enabled = false)
  // public void testTimeExtrapolation() {
  // int timeSteps = 10;
  // int priceSteps = 100;
  // double lowerMoneyness = 0.0;
  // double upperMoneyness = 3.0;
  // TESTER.testTimeExtrapolation(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness);
  // }
  //
  // /**
  // * This needs more price steps for the same accuracy, but can push to greater moneyness range
  // */
  // @Test(enabled = false)
  // public void testLogTransformedBlackScholesEquation() {
  // int timeSteps = 10;
  // int priceSteps = 200;
  // double lowerMoneyness = 0.3;
  // double upperMoneyness = 4.0;
  // TESTER.testLogTransformedBlackScholesEquation(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness);
  // }
  //
  // @Test(enabled = false)
  // public void testCEV() {
  // int timeSteps = 25;
  // int priceSteps = 100;
  // double lowerMoneyness = 1.0; // Not working well for ITM calls
  // double upperMoneyness = 3.0;
  // TESTER.testCEV(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness);
  // }
  //
  // @Test(enabled = false)
  // public void testAmericanPrice() {
  // int timeSteps = 10;
  // int priceSteps = 100;
  // double lowerMoneyness = 0.4;
  // double upperMoneyness = 3.0;
  // TESTER.testAmericanPrice(SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness);
  // }
  //
  // @Test(enabled = false)
  // public void testBlackScholesEquationNonuniformGrid() {
  // int timeSteps = 10; // one less step with exp meshing
  // int priceSteps = 100;
  // double lowerMoneyness = 0.4;
  // double upperMoneyness = 3.0;
  // TESTER.testBlackScholesEquationNonuniformGrid((CrankNicolsonFiniteDifferenceSOR) SOLVER, timeSteps, priceSteps, lowerMoneyness, upperMoneyness);
  // }

}
