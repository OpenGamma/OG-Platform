/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.financial.timeseries.returns.ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.ContinuouslyCompoundedTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.RelativeTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public abstract class HistoricalVolatilityCalculatorTestCase {
  private static final long[] T = new long[] {1l, 2l, 3l, 4l, 5l, 6l, 7l, 8l, 9l, 10l, 11l, 12l, 13l, 14l, 15l, 16l, 17l, 18l, 19l, 20l, 21l};
  private static final double[] CLOSE = new double[] {132.5, 133.5, 135., 133., 133., 137., 135., 135., 142.5, 143., 144.5, 145., 146., 149., 148., 147., 147., 147., 145., 145., 150.};
  private static final double[] HIGH = new double[] {132.5, 134., 136., 137., 136., 137., 136.5, 136., 143.5, 145., 147., 147.5, 147., 150., 149., 149.5, 147.5, 149., 147.5, 145., 150.};
  private static final double[] LOW = new double[] {131., 131., 134., 133., 133., 133., 135., 135., 137., 142., 142., 145., 143., 148., 146.5, 147., 146., 146.5, 144.5, 144., 143.5};
  private static final DateTimeNumericEncoding ENCODING = DateTimeNumericEncoding.DATE_EPOCH_DAYS;
  protected static final DoubleTimeSeries<Long> CLOSE_TS = new FastArrayLongDoubleTimeSeries(ENCODING, T, CLOSE);
  protected static final DoubleTimeSeries<Long> HIGH_TS = new FastArrayLongDoubleTimeSeries(ENCODING, T, HIGH);
  protected static final DoubleTimeSeries<Long> LOW_TS = new FastArrayLongDoubleTimeSeries(ENCODING, T, LOW);
  protected static final TimeSeriesReturnCalculator RETURN_CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  protected static final RelativeTimeSeriesReturnCalculator RELATIVE_RETURN_CALCULATOR = new ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  protected static final double EPS = 1e-4;
  private static HistoricalVolatilityCalculator CALCULATOR = new HistoricalVolatilityCalculator() {

    @Override
    public Double evaluate(final DoubleTimeSeries<?>... x) {
      return 0.;
    }

  };
  private static HistoricalVolatilityCalculator LENIENT_CALCULATOR = new HistoricalVolatilityCalculator(CalculationMode.LENIENT) {

    @Override
    public Double evaluate(final DoubleTimeSeries<?>... x) {
      return 0.;
    }

  };
  private static HistoricalVolatilityCalculator FOOLISH_CALCULATOR = new HistoricalVolatilityCalculator(CalculationMode.LENIENT, 1.1) {

    @Override
    public Double evaluate(final DoubleTimeSeries<?>... x) {
      return 0.;
    }

  };

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    getCalculator().evaluate((DoubleTimeSeries<Long>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullInInput() {
    getCalculator().testTimeSeries(null, 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyArray() {
    getCalculator().evaluate(new DoubleTimeSeries[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShortTS() {
    getCalculator().testTimeSeries(new DoubleTimeSeries[] {new FastArrayLongDoubleTimeSeries(ENCODING, new long[] {1l}, new double[] {3})}, 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInputs1() {
    final DoubleTimeSeries<?>[] tsArray = (new DoubleTimeSeries[] {new FastArrayLongDoubleTimeSeries(ENCODING, new long[] {1l, 2l, 3l}, new double[] {3, 4, 5}),
        new FastArrayLongDoubleTimeSeries(ENCODING, new long[] {1l, 2l}, new double[] {3, 4})});
    getCalculator().testTimeSeries(tsArray, 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInputs2() {
    final DoubleTimeSeries<?>[] tsArray = new DoubleTimeSeries[] {new FastArrayLongDoubleTimeSeries(ENCODING, new long[] {1l, 2l, 3l}, new double[] {3, 4, 5}),
        new FastArrayLongDoubleTimeSeries(ENCODING, new long[] {1l, 2l, 3l}, new double[] {4, 5, 6}), new FastArrayLongDoubleTimeSeries(ENCODING, new long[] {2l, 3l, 4l}, new double[] {4, 5, 6})};
    getCalculator().testTimeSeries(tsArray, 2);

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

  private void testHighLowTimeSeries(final DoubleTimeSeries<Long> x, final DoubleTimeSeries<Long> y) {
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

  private void testHighLowCloseTimeSeries(final DoubleTimeSeries<Long> x, final DoubleTimeSeries<Long> y, final DoubleTimeSeries<Long> z) {
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

  protected abstract HistoricalVolatilityCalculator getCalculator();
}
