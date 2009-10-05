/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.covariance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;

/**
 * 
 * @author emcleod
 */
public class HistoricalVolatilityHighLowCalculatorTest extends HistoricalVolatilityCalculatorTest {
  private static final HistoricalVolatilityCalculator CALCULATOR = new HistoricalVolatilityHighLowCalculator(RELATIVE_RETURN_CALCULATOR);

  @Test
  public void test() {
    try {
      CALCULATOR.evaluate(new DoubleTimeSeries[] { HIGH_TS });
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    assertEquals(CALCULATOR.evaluate(new DoubleTimeSeries[] { HIGH_TS, LOW_TS }), 0.0126, EPS);
  }
}
