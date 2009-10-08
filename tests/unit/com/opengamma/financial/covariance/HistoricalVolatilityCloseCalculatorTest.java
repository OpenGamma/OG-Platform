/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.covariance.HistoricalVolatilityCalculator;
import com.opengamma.financial.covariance.HistoricalVolatilityCloseCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class HistoricalVolatilityCloseCalculatorTest extends HistoricalVolatilityCalculatorTest {
  private static final HistoricalVolatilityCalculator CALCULATOR = new HistoricalVolatilityCloseCalculator(RETURN_CALCULATOR);

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(new DoubleTimeSeries[] { CLOSE_TS }), 0.0173, EPS);
  }
}
