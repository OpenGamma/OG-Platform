/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * NOT WORKING
 */
@Test(groups = TestGroup.UNIT)
public class CrankNicolsonFiniteDifference2DTest {

  private static final HestonPDETestCase HESTON_TESTER = new HestonPDETestCase();
  private static final SpreadOptionPDETestCase SPREAD_OPTION_TESTER = new SpreadOptionPDETestCase();
  private static final ConvectionDiffusionPDESolver2D SOLVER = new CrankNicolsonFiniteDifference2D(0.5);// set up as Crank-Nicolson

  @Test
  public void testSpreadOption() {

    int timeSteps = 10;
    int xSteps = 100;
    int ySteps = 100;

    SPREAD_OPTION_TESTER.testAgaintBSPrice(SOLVER, timeSteps, xSteps, ySteps);
  }

  @Test
  public void testHeston() {

    int timeSteps = 10;
    int xSteps = 80;
    int ySteps = 80;
    boolean print = false; // make sure this is false before commits

    HESTON_TESTER.testCallPrice(SOLVER, timeSteps, xSteps, ySteps, print);
  }

}
