/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.timeseries.returns.RelativeTimeSeriesReturnCalculator;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.TimeSeriesException;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class RelativeTimeSeriesReturnCalculatorTest {
  private static final LocalDateDoubleTimeSeries TS = new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.of(2000, 1, 2), LocalDate.of(2000, 1, 3)}, new double[] {3, 4});
  private static final RelativeTimeSeriesReturnCalculator STRICT = new RelativeTimeSeriesReturnCalculator(CalculationMode.STRICT) {

    @Override
    public LocalDateDoubleTimeSeries evaluate(final LocalDateDoubleTimeSeries... x) {
      testInputData(x);
      return null;
    }
  };
  private static final RelativeTimeSeriesReturnCalculator LENIENT = new RelativeTimeSeriesReturnCalculator(CalculationMode.LENIENT) {

    @Override
    public LocalDateDoubleTimeSeries evaluate(final LocalDateDoubleTimeSeries... x) {
      testInputData(x);
      return null;
    }
  };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    STRICT.evaluate((LocalDateDoubleTimeSeries[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyArray() {
    STRICT.evaluate(new LocalDateDoubleTimeSeries[0]);
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
    STRICT.evaluate(TS, new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.of(2000, 1, 1)}, new double[] {1}));
  }

  @Test
  public void testDifferentLengthsLenient() {
    LENIENT.evaluate(TS, new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.of(2000, 1, 1)}, new double[] {1}));
  }

  @Test(expectedExceptions = TimeSeriesException.class)
  public void testDifferentDatesStrict() {
    STRICT.evaluate(TS, new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.of(2000, 1, 1)}, new double[] {1}));
  }

  @Test
  public void testDifferentDatessLenient() {
    LENIENT.evaluate(TS, new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 3)}, new double[] {1, 2}));
  }
}
