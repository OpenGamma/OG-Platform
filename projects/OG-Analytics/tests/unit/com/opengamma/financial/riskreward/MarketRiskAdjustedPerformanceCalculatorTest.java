/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class MarketRiskAdjustedPerformanceCalculatorTest {

  @Test
  public void test() {
    final double assetReturn = 0.12;
    final double riskFreeReturn = 0.03;
    final double beta = 0.7;
    assertEquals(new MarketRiskAdjustedPerformanceCalculator().calculate(assetReturn, riskFreeReturn, beta), 0.1586, 1e-4);
  }
}
