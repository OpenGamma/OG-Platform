/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import org.testng.annotations.Test;

/**
 * 
 */
public class InterpolatedTermStructureTest extends SingleStrikeSetup {

  @Test
  public void atmTest() {
    final CapletStripper stripper = new CapletStripperInterpolatedTermStructure();
    testATMStripping(stripper, 0.0, null, 1e-15, true);
  }

  @Test
  public void singleStrikeTest() {
    final CapletStripper stripper = new CapletStripperInterpolatedTermStructure();
    final int n = getNumberOfStrikes();
    for (int i = 0; i < n; i++) {
      testSingleStrikeStripping(stripper, i, 0.0, null, 1e-15, true);
    }
  }

  @Test
  public void globalFitTest() {
    final double[] knots = new double[] {0.25, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    final CapletStripper stripper = new CapletStripperInterpolatedTermStructure(knots);
    testStripping(stripper, getAllCaps(), getAllCapPrices(), 0, null, 1e-15, true);
  }

}
