/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.bond;

import static org.junit.Assert.assertEquals;

import javax.time.Instant;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.ArrayYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.YearOffsetDoubleTimeSeries;

/**
 *
 */
public class SimpleYieldToMaturityBondYieldCalculatorTest {
  private static final SimpleYieldToMaturityBondYieldCalculator CALCULATOR = new SimpleYieldToMaturityBondYieldCalculator();
  private static final ZonedDateTime DATE = ZonedDateTime.ofInstant(Instant.EPOCH, TimeZone.UTC);
  private static final YearOffsetDoubleTimeSeries CF1;
  private static final YearOffsetDoubleTimeSeries CF2;
  private static final double EPS = 1e-9;
  private static final double PAR = 100;
  private static final double COUPON = 0.05;

  static {
    final DoubleTimeSeries<Long> dts1 = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new long[] {DateUtil.MILLISECONDS_PER_YEAR}, new double[] {1});
    final DoubleTimeSeries<Long> dts2 = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new long[] {DateUtil.MILLISECONDS_PER_YEAR,
        DateUtil.MILLISECONDS_PER_YEAR * 5}, new double[] {COUPON, COUPON + 1});
    CF1 = new ArrayYearOffsetDoubleTimeSeries(DATE, dts1.toFastLongDoubleTimeSeries());
    CF2 = new ArrayYearOffsetDoubleTimeSeries(DATE, dts2.toFastLongDoubleTimeSeries());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCashFlows() {
    CALCULATOR.calculate(null, PAR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyCashFlows() {
    CALCULATOR.calculate(new ArrayYearOffsetDoubleTimeSeries(DATE, new Double[] {}, new double[] {}), PAR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePrice() {
    CALCULATOR.calculate(CF1, -PAR);
  }

  @Test
  public void test() {
    final double discount = 9;
    assertEquals(CALCULATOR.calculate(CF1, PAR - discount), (PAR + discount) / (PAR - discount), EPS);
    assertEquals(CALCULATOR.calculate(CF2, PAR), COUPON / PAR * 100, EPS);
  }

}
