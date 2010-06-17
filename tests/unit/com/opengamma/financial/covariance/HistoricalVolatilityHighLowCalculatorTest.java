/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class HistoricalVolatilityHighLowCalculatorTest extends HistoricalVolatilityCalculatorTestCase {
  private static final HistoricalVolatilityCalculator CALCULATOR = new HistoricalVolatilityHighLowCalculator(RELATIVE_RETURN_CALCULATOR);

  @Test(expected = IllegalArgumentException.class)
  public void testHighTS() {
    CALCULATOR.evaluate(new DoubleTimeSeries[] {HIGH_TS});
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(new DoubleTimeSeries[] {HIGH_TS, LOW_TS}), 0.0126, EPS);
  }

  @Override
  protected HistoricalVolatilityCalculator getCalculator() {
    return CALCULATOR;
  }

}
