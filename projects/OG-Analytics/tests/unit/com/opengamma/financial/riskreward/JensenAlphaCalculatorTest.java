/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.function.Function;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class JensenAlphaCalculatorTest {
  private static final long[] T = new long[] {1};
  private static final DoubleTimeSeries<?> ASSET_RETURN = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, T, new double[] {0.12});
  private static final DoubleTimeSeries<?> RISK_FREE = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, T, new double[] {0.03});
  private static final DoubleTimeSeries<?> MARKET_RETURN = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, T, new double[] {0.11});
  private static final double BETA = 0.7;
  private static final DoubleTimeSeriesStatisticsCalculator EXCESS_RETURN = new DoubleTimeSeriesStatisticsCalculator(new Function<double[], Double>() {

    @Override
    public Double evaluate(final double[]... x) {
      return x[0][0];
    }

  });
  private static final JensenAlphaCalculator CALCULATOR = new JensenAlphaCalculator(EXCESS_RETURN, EXCESS_RETURN, EXCESS_RETURN);

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(ASSET_RETURN, RISK_FREE, BETA, MARKET_RETURN), 0.034, 1e-3);
  }
}
