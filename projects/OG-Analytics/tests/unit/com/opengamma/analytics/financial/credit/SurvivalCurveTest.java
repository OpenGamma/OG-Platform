/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import org.testng.annotations.Test;

/**
 * Class to test the implementation of the survival curve object
 */
public class SurvivalCurveTest {

  // ---------------------------------------------------------------------------------------

  private static final double parSpread = 60.0;
  private static final double curveRecoveryRate = 0.40;

  // Construct a survival curve based on a flat hazard rate term structure (for testing purposes only)
  private static final SurvivalCurve survivalCurve = new SurvivalCurve(parSpread, curveRecoveryRate);

  // ---------------------------------------------------------------------------------------

  @Test
  public void testSurvivalCurve() {

    System.out.println("Running tests on survival curve construction ...");

    double hazardRate = survivalCurve.getFlatHazardRate();

    for (double t = 0.0; t <= 5.0; t += 0.25) {

      double survivalProbabilty = survivalCurve.getSurvivalProbability(hazardRate, t);

      System.out.println("t = " + t + ", S(0, t) = " + survivalProbabilty);
    }
  }

  // ---------------------------------------------------------------------------------------
}
