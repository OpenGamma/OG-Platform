/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * These tests are only valid for rho = 0
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class PeacemanRachfordTest {

  private static final HestonPDETestCase HESTON_TESTER = new HestonPDETestCase();
  private static final SpreadOptionPDETestCase SPREAD_OPTION_TESTER = new SpreadOptionPDETestCase();
  private static final ConvectionDiffusionPDESolver2D SOLVER = new PeacemanRachfordFiniteDifference2D();

  @Test(enabled = false)
  public void testSpreadOption() {

    final int timeSteps = 10;
    final int xSteps = 100;
    final int ySteps = 100;

    SPREAD_OPTION_TESTER.testAgaintBSPrice(SOLVER, timeSteps, xSteps, ySteps);
  }

  @Test(enabled = false)
  public void testHeston() {

    final int timeSteps = 50;
    final int xSteps = 150;
    final int ySteps = 40;
    final boolean print = false; // make sure this is false before commits

    HESTON_TESTER.testCallPrice(SOLVER, timeSteps, xSteps, ySteps, print);
  }

}
