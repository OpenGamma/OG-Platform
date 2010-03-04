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
 * @author emcleod
 */
public class HistoricalVolatilityCloseCalculatorTest extends HistoricalVolatilityCalculatorTestCase {
  private static final HistoricalVolatilityCalculator CALCULATOR = new HistoricalVolatilityCloseCalculator(RETURN_CALCULATOR);

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(new DoubleTimeSeries[] { CLOSE_TS }), 0.0173, EPS);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.financial.covariance.HistoricalVolatilityCalculatorTestCase
   * #getCalculator()
   */
  @Override
  protected HistoricalVolatilityCalculator getCalculator() {
    return CALCULATOR;
  }
}
