/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class SharpeRatioCalculatorTest {

  @Test
  public void test() {
    final double assetReturn = 0.12;
    final double benchmarkReturn = 0.03;
    final double standardDeviation = 0.15;
    assertEquals(new SharpeRatioCalculator().calculate(assetReturn, benchmarkReturn, standardDeviation), 0.6, 0);
  }
}
