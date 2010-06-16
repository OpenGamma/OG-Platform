/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;

/**
 * 
 */
public class HistoricalCovarianceCalculatorTest {
  private static final DoubleTimeSeries<?> TS1 = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 2, 3, 4, 5}, new double[] {1, 1, 1, 1, 1});
  private static final TimeSeriesReturnCalculator RETURNS = new TimeSeriesReturnCalculator(CalculationMode.STRICT) {

    @Override
    public DoubleTimeSeries<?> evaluate(final DoubleTimeSeries<?>... x) {
      return x[0];
    }
  };
  private static final CovarianceCalculator CALCULATOR = new HistoricalCovarianceCalculator(RETURNS);

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    new HistoricalCovarianceCalculator(null);
  }

  @Test
  public void test() {
    final double n = TS1.size();
    final double covariance = CALCULATOR.evaluate(TS1, TS1);
    assertEquals(covariance, n / (n - 1) - 1, 1e-9);
  }
}
