/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.returns;

import org.testng.annotations.Test;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;

/**
 * 
 */
public class RelativeTimeSeriesReturnCalculatorTest {
  private static final DoubleTimeSeries<?> TS = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 2}, new double[] {3, 4});
  private static final RelativeTimeSeriesReturnCalculator STRICT = new RelativeTimeSeriesReturnCalculator(CalculationMode.STRICT) {

    @Override
    public DoubleTimeSeries<?> evaluate(final DoubleTimeSeries<?>... x) {
      testInputData(x);
      return null;
    }
  };
  private static final RelativeTimeSeriesReturnCalculator LENIENT = new RelativeTimeSeriesReturnCalculator(CalculationMode.LENIENT) {

    @Override
    public DoubleTimeSeries<?> evaluate(final DoubleTimeSeries<?>... x) {
      testInputData(x);
      return null;
    }
  };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    STRICT.evaluate((DoubleTimeSeries<?>[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyArray() {
    STRICT.evaluate(new DoubleTimeSeries<?>[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirstElement() {
    STRICT.evaluate(null, TS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecondElement() {
    STRICT.evaluate(TS, null);
  }

  @Test(expectedExceptions = TimeSeriesException.class)
  public void testDifferentLengthsStrict() {
    STRICT.evaluate(TS, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1}, new double[] {1}));
  }

  @Test
  public void testDifferentLengthsLenient() {
    LENIENT.evaluate(TS, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1}, new double[] {1}));
  }

  @Test(expectedExceptions = TimeSeriesException.class)
  public void testDifferentDatesStrict() {
    STRICT.evaluate(TS, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 3}, new double[] {1, 2}));
  }

  @Test
  public void testDifferentDatessLenient() {
    LENIENT.evaluate(TS, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 3}, new double[] {1, 2}));
  }
}
