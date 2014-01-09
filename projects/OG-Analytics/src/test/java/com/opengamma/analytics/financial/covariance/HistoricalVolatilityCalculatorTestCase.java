/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.timeseries.returns.ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator;
import com.opengamma.analytics.financial.timeseries.returns.ContinuouslyCompoundedTimeSeriesReturnCalculator;
import com.opengamma.analytics.financial.timeseries.returns.RelativeTimeSeriesReturnCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public abstract class HistoricalVolatilityCalculatorTestCase {
  private static final long[] T_RAW = new long[] {1l, 2l, 3l, 4l, 5l, 6l, 7l, 8l, 9l, 10l, 11l, 12l, 13l, 14l, 15l, 16l, 17l, 18l, 19l, 20l, 21l};
  private static final LocalDate[] T = new LocalDate[T_RAW.length];
  static {
    for (int i=0; i<T_RAW.length; i++) {
      T[i] = LocalDate.ofEpochDay(T_RAW[i]);
    }
  }
  private static final double[] CLOSE = new double[] {132.5, 133.5, 135., 133., 133., 137., 135., 135., 142.5, 143., 144.5, 145., 146., 149., 148., 147., 147., 147., 145., 145., 150.};
  private static final double[] HIGH = new double[] {132.5, 134., 136., 137., 136., 137., 136.5, 136., 143.5, 145., 147., 147.5, 147., 150., 149., 149.5, 147.5, 149., 147.5, 145., 150.};
  private static final double[] LOW = new double[] {131., 131., 134., 133., 133., 133., 135., 135., 137., 142., 142., 145., 143., 148., 146.5, 147., 146., 146.5, 144.5, 144., 143.5};
  protected static final LocalDateDoubleTimeSeries CLOSE_TS = ImmutableLocalDateDoubleTimeSeries.of(T, CLOSE);
  protected static final LocalDateDoubleTimeSeries HIGH_TS = ImmutableLocalDateDoubleTimeSeries.of(T, HIGH);
  protected static final LocalDateDoubleTimeSeries LOW_TS = ImmutableLocalDateDoubleTimeSeries.of(T, LOW);
  protected static final TimeSeriesReturnCalculator RETURN_CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  protected static final RelativeTimeSeriesReturnCalculator RELATIVE_RETURN_CALCULATOR = new ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  protected static final double EPS = 1e-4;
  private static HistoricalVolatilityCalculator CALCULATOR = new HistoricalVolatilityCalculator() {

    @Override
    public Double evaluate(final LocalDateDoubleTimeSeries... x) {
      return 0.;
    }

  };
  private static HistoricalVolatilityCalculator LENIENT_CALCULATOR = new HistoricalVolatilityCalculator(CalculationMode.LENIENT) {

    @Override
    public Double evaluate(final LocalDateDoubleTimeSeries... x) {
      return 0.;
    }

  };
  private static HistoricalVolatilityCalculator FOOLISH_CALCULATOR = new HistoricalVolatilityCalculator(CalculationMode.LENIENT, 1.1) {

    @Override
    public Double evaluate(final LocalDateDoubleTimeSeries... x) {
      return 0.;
    }

  };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS() {
    getCalculator().evaluate((LocalDateDoubleTimeSeries) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInInput() {
    getCalculator().testTimeSeries(null, 2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyArray() {
    getCalculator().evaluate(new LocalDateDoubleTimeSeries[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testShortTS() {
    getCalculator().testTimeSeries(new LocalDateDoubleTimeSeries[] {ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(1)}, new double[] {3})}, 2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInputs1() {
    final LocalDateDoubleTimeSeries[] tsArray = (new LocalDateDoubleTimeSeries[] {
        ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(2), LocalDate.ofEpochDay(3)}, new double[] {3, 4, 5}),
        ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(2)}, new double[] {3, 4})});
    getCalculator().testTimeSeries(tsArray, 2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInputs2() {
    final LocalDateDoubleTimeSeries[] tsArray = new LocalDateDoubleTimeSeries[] {
        ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(2), LocalDate.ofEpochDay(3)}, new double[] {3, 4, 5}),
        ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(2), LocalDate.ofEpochDay(3)}, new double[] {4, 5, 6}), 
        ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(2), LocalDate.ofEpochDay(3), LocalDate.ofEpochDay(4)}, new double[] {4, 5, 6})};
    getCalculator().testTimeSeries(tsArray, 2);

  }

  @Test
  public void testTimeSeries() {
    assertHighLowTimeSeries(LOW_TS, HIGH_TS);
    assertHighLowCloseTimeSeries(LOW_TS, HIGH_TS, CLOSE_TS);
    assertHighLowCloseTimeSeries(LOW_TS, CLOSE_TS, HIGH_TS);
    assertHighLowCloseTimeSeries(HIGH_TS, CLOSE_TS, LOW_TS);
    assertHighLowCloseTimeSeries(CLOSE_TS, HIGH_TS, LOW_TS);
    assertHighLowCloseTimeSeries(CLOSE_TS, LOW_TS, HIGH_TS);
  }

  private void assertHighLowTimeSeries(final LocalDateDoubleTimeSeries x, final LocalDateDoubleTimeSeries y) {
    try {
      CALCULATOR.testHighLow(x, y);
      Assert.fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    try {
      LENIENT_CALCULATOR.testHighLow(x, y);
      Assert.fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    FOOLISH_CALCULATOR.testHighLow(x, y);
  }

  private void assertHighLowCloseTimeSeries(final LocalDateDoubleTimeSeries x, final LocalDateDoubleTimeSeries y, final LocalDateDoubleTimeSeries z) {
    try {
      CALCULATOR.testHighLowClose(x, y, z);
      Assert.fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    try {
      LENIENT_CALCULATOR.testHighLowClose(x, y, z);
      Assert.fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    FOOLISH_CALCULATOR.testHighLowClose(x, y, z);
  }

  protected abstract HistoricalVolatilityCalculator getCalculator();
}
