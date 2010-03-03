/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;

/**
 * 
 * @author emcleod
 */
public class HistoricalVolatilityHighLowCalculatorTest extends HistoricalVolatilityCalculatorTestCase {
  private static final HistoricalVolatilityCalculator<DoubleTimeSeries<Long>> CALCULATOR = new HistoricalVolatilityHighLowCalculator<DoubleTimeSeries<Long>>(
      RELATIVE_RETURN_CALCULATOR);

  @SuppressWarnings("unchecked")
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.financial.covariance.HistoricalVolatilityCalculatorTestCase
   * #getCalculator()
   */
  @Override
  protected HistoricalVolatilityCalculator<DoubleTimeSeries<Long>> getCalculator() {
    return CALCULATOR;
  }

}
