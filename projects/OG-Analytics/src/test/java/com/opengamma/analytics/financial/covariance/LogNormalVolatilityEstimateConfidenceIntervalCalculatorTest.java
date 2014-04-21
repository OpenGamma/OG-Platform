/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.statistics.ConfidenceInterval;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class LogNormalVolatilityEstimateConfidenceIntervalCalculatorTest {
  private static final LogNormalVolatilityEstimateConfidenceIntervalCalculator CALCULATOR = new LogNormalVolatilityEstimateConfidenceIntervalCalculator();
  private static final double EPS = 1e-4;

  @Test
  public void test() {
    final ConfidenceInterval intervals = CALCULATOR.getConfidenceInterval(0.2743, 0.95, 20);
    assertEquals(intervals.getLowerInterval(), 0.2086, EPS);
    assertEquals(intervals.getUpperInterval(), 0.4006, EPS);
  }
}
