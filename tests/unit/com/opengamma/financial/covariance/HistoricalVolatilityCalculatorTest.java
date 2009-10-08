/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.financial.covariance.HistoricalVolatilityCalculator;
import com.opengamma.financial.timeseries.returns.ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.ContinuouslyCompoundedTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.RelativeTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.util.CalculationMode;

/**
 * 
 * @author emcleod
 */
public class HistoricalVolatilityCalculatorTest {
  private static final long[] T = new long[] { 1l, 2l, 3l, 4l, 5l, 6l, 7l, 8l, 9l, 10l, 11l, 12l, 13l, 14l, 15l, 16l, 17l, 18l, 19l, 20l, 21l };
  private static final double[] CLOSE = new double[] { 132.5, 133.5, 135., 133., 133., 137., 135., 135., 142.5, 143., 144.5, 145., 146., 149., 148., 147., 147., 147., 145., 145.,
      150. };
  private static final double[] HIGH = new double[] { 132.5, 134., 136., 137., 136., 137., 136.5, 136., 143.5, 145., 147., 147.5, 147., 150., 149., 149.5, 147.5, 149., 147.5,
      145., 150. };
  private static final double[] LOW = new double[] { 131., 131., 134., 133., 133., 133., 135., 135., 137., 142., 142., 145., 143., 148., 146.5, 147., 146., 146.5, 144.5, 144.,
      143.5 };
  protected static final DoubleTimeSeries CLOSE_TS = new ArrayDoubleTimeSeries(T, CLOSE);
  protected static final DoubleTimeSeries HIGH_TS = new ArrayDoubleTimeSeries(T, HIGH);
  protected static final DoubleTimeSeries LOW_TS = new ArrayDoubleTimeSeries(T, LOW);
  protected static final TimeSeriesReturnCalculator RETURN_CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  protected static final RelativeTimeSeriesReturnCalculator RELATIVE_RETURN_CALCULATOR = new ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  protected static final double EPS = 1e-4;
  private static final HistoricalVolatilityCalculator CALCULATOR = new HistoricalVolatilityCalculator() {

    @Override
    public Double evaluate(final DoubleTimeSeries... x) {
      return 0.;
    }

  };
  private static final HistoricalVolatilityCalculator LENIENT_CALCULATOR = new HistoricalVolatilityCalculator(CalculationMode.LENIENT) {

    @Override
    public Double evaluate(final DoubleTimeSeries... x) {
      return 0.;
    }

  };
  private static final HistoricalVolatilityCalculator FOOLISH_CALCULATOR = new HistoricalVolatilityCalculator(CalculationMode.LENIENT, 1.1) {

    @Override
    public Double evaluate(final DoubleTimeSeries... x) {
      return 0.;
    }

  };

  @Test
  public void testInputs() {
    try {
      CALCULATOR.testInput(null);
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    try {
      CALCULATOR.testInput(new DoubleTimeSeries[0]);
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    try {
      CALCULATOR.testTimeSeries(new DoubleTimeSeries[] { new ArrayDoubleTimeSeries(new long[] { 1l }, new double[] { 3 }) }, 2);
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    try {
      CALCULATOR.testDatesCoincide(new DoubleTimeSeries[] { new ArrayDoubleTimeSeries(new long[] { 1l, 2l, 3l }, new double[] { 3, 4, 5 }),
          new ArrayDoubleTimeSeries(new long[] { 1l, 2l }, new double[] { 3, 4 }) });
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    try {
      CALCULATOR.testDatesCoincide(new DoubleTimeSeries[] { new ArrayDoubleTimeSeries(new long[] { 1l, 2l, 3l }, new double[] { 3, 4, 5 }),
          new ArrayDoubleTimeSeries(new long[] { 1l, 2l, 3l }, new double[] { 4, 5, 6 }), new ArrayDoubleTimeSeries(new long[] { 2l, 3l, 4l }, new double[] { 4, 5, 6 }) });
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
  }

  @Test
  public void testTimeSeries() {
    testHighLowTimeSeries(LOW_TS, HIGH_TS);
    testHighLowCloseTimeSeries(LOW_TS, HIGH_TS, CLOSE_TS);
    testHighLowCloseTimeSeries(LOW_TS, CLOSE_TS, HIGH_TS);
    testHighLowCloseTimeSeries(HIGH_TS, CLOSE_TS, LOW_TS);
    testHighLowCloseTimeSeries(CLOSE_TS, HIGH_TS, LOW_TS);
    testHighLowCloseTimeSeries(CLOSE_TS, LOW_TS, HIGH_TS);
  }

  private void testHighLowTimeSeries(final DoubleTimeSeries x, final DoubleTimeSeries y) {
    try {
      CALCULATOR.testHighLow(x, y);
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    try {
      LENIENT_CALCULATOR.testHighLow(x, y);
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    FOOLISH_CALCULATOR.testHighLow(x, y);
  }

  private void testHighLowCloseTimeSeries(final DoubleTimeSeries x, final DoubleTimeSeries y, final DoubleTimeSeries z) {
    try {
      CALCULATOR.testHighLowClose(x, y, z);
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    try {
      LENIENT_CALCULATOR.testHighLowClose(x, y, z);
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    FOOLISH_CALCULATOR.testHighLowClose(x, y, z);
  }
}
