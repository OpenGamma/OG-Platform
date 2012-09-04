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

  private static final SurvivalCurve survivalCurve = new SurvivalCurve();

  // ---------------------------------------------------------------------------------------

  @Test
  public void testSurvivalCurve() {

    System.out.println("Running tests on survival curve construction ...");

    double hazardRate = 0.01;

    for (double t = 0.0; t <= 5.0; t += 0.25) {

      double survivalProbabilty = survivalCurve.getSurvivalProbability(hazardRate, t);

      System.out.println("t = " + t + ", S(0, t) = " + survivalProbabilty);
    }
  }

  // ---------------------------------------------------------------------------------------
}
