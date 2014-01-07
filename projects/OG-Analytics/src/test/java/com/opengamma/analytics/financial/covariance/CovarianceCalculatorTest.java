/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import org.testng.annotations.Test;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CovarianceCalculatorTest {
  private static final CovarianceCalculator CALCULATOR = new CovarianceCalculator() {

    @Override
    public Double evaluate(final DoubleTimeSeries<?>... x1) {
      return null;
    }

  };
  private static final DoubleTimeSeries<?> TS1 = ImmutableInstantDoubleTimeSeries.of(new long[] {2}, new double[] {1});
  private static final DoubleTimeSeries<?> TS2 = ImmutableInstantDoubleTimeSeries.of(new long[] {2, 3}, new double[] {1, 2});
  private static final DoubleTimeSeries<?> TS3 = ImmutableInstantDoubleTimeSeries.of(new long[] {4, 5}, new double[] {1, 2});
  private static final DoubleTimeSeries<?> TS4 = ImmutableInstantDoubleTimeSeries.of(new long[] {4, 5, 6}, new double[] {1, 2, 3});

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS1() {
    CALCULATOR.testTimeSeries(null, TS1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS2() {
    CALCULATOR.testTimeSeries(TS2, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyTS1() {
    CALCULATOR.testTimeSeries(ImmutableInstantDoubleTimeSeries.EMPTY_SERIES, TS1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyTS2() {
    CALCULATOR.testTimeSeries(TS2, ImmutableInstantDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSmallTS1() {
    CALCULATOR.testTimeSeries(TS1, TS2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSmallTS2() {
    CALCULATOR.testTimeSeries(TS2, TS1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDifferentLength() {
    CALCULATOR.testTimeSeries(TS2, TS4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDifferentDates() {
    CALCULATOR.testTimeSeries(TS2, TS3);
  }
}
