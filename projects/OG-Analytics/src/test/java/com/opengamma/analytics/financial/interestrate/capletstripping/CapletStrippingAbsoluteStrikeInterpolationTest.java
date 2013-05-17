/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.List;

import org.testng.annotations.Test;

/**
 * 
 */
public class CapletStrippingAbsoluteStrikeInterpolationTest extends CapletStrippingAbsoluteStrikeTest {

  @Override
  public CapletStrippingAbsoluteStrike getStripper(List<CapFloor> caps) {
    return new CapletStrippingAbsoluteStrikeInterpolation(caps, getYieldCurves());
  }
  
  @Test
  public void test() {
    final boolean print = false;
    testVolStripping(print);
  }
  
  @Test
  public void timingTest() {
    timingTest(1, 0);
  }

}
