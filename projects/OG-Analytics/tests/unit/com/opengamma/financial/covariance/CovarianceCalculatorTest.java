/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import org.junit.Test;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;

public class CovarianceCalculatorTest {
  private static final CovarianceCalculator CALCULATOR = new CovarianceCalculator() {

    @Override
    public Double evaluate(final DoubleTimeSeries<?>... x1) {
      return null;
    }

  };
  private static final DoubleTimeSeries<?> TS1 = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_SECONDS, new int[] {2}, new double[] {1});
  private static final DoubleTimeSeries<?> TS2 = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_SECONDS, new int[] {2, 3}, new double[] {1, 2});
  private static final DoubleTimeSeries<?> TS3 = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_SECONDS, new int[] {4, 5}, new double[] {1, 2});
  private static final DoubleTimeSeries<?> TS4 = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_SECONDS, new int[] {4, 5, 6}, new double[] {1, 2, 3});

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS1() {
    CALCULATOR.testTimeSeries(null, TS1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS2() {
    CALCULATOR.testTimeSeries(TS2, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTS1() {
    CALCULATOR.testTimeSeries(FastArrayIntDoubleTimeSeries.EMPTY_SERIES, TS1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTS2() {
    CALCULATOR.testTimeSeries(TS2, FastArrayIntDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSmallTS1() {
    CALCULATOR.testTimeSeries(TS1, TS2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSmallTS2() {
    CALCULATOR.testTimeSeries(TS2, TS1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDifferentLength() {
    CALCULATOR.testTimeSeries(TS2, TS4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDifferentDates() {
    CALCULATOR.testTimeSeries(TS2, TS3);
  }
}
