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
public class SortinoRatioCalculatorTest {

  @Test
  public void test() {
    final double assetReturn = 0.15;
    final double benchmarkReturn = 0.12;
    final double standardDeviation = 0.30;
    assertEquals(new SortinoRatioCalculator().calculate(assetReturn, benchmarkReturn, standardDeviation), new SharpeRatioCalculator().calculate(assetReturn, benchmarkReturn, standardDeviation), 0);
  }
}
