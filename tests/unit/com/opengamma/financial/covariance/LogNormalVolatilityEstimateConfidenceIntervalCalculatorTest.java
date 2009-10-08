/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.covariance.LogNormalVolatilityEstimateConfidenceIntervalCalculator;
import com.opengamma.math.statistics.ConfidenceInterval;

/**
 * 
 * @author emcleod
 */
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
