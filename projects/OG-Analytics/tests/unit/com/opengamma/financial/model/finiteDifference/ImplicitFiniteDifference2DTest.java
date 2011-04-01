/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;


/**
 * NOT WORKING
 */
public class ImplicitFiniteDifference2DTest {

  private static final HestonPDETestCase HESTON_TESTER = new HestonPDETestCase();
  private static final SpreadOptionPDETestCase SPREAD_OPTION_TESTER = new SpreadOptionPDETestCase();
  private static final ConvectionDiffusionPDESolver2D SOLVER = new ImplicitFiniteDifference2D();

  // @Test
  public void testSpreadOption() {

    int timeSteps = 1000;
    int xSteps = 40;
    int ySteps = 40;

    SPREAD_OPTION_TESTER.testAgaintBSPrice(SOLVER, timeSteps, xSteps, ySteps);
  }

  // @Test
  public void testHeston() {

    int timeSteps = 100;
    int xSteps = 50;
    int ySteps = 50;

    HESTON_TESTER.testCallPrice(SOLVER, timeSteps, xSteps, ySteps);
  }

}
