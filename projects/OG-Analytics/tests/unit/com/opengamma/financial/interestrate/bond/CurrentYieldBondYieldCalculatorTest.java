/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.interestrate.bond.CurrentYieldBondYieldCalculator;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.ArrayYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.YearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 *
 */
public class CurrentYieldBondYieldCalculatorTest {
  private static final CurrentYieldBondYieldCalculator CALCULATOR = new CurrentYieldBondYieldCalculator();
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 4, 1);
  private static final YearOffsetDoubleTimeSeries CF1;
  private static final YearOffsetDoubleTimeSeries CF2;
  private static final double EPS = 1e-9;
  private static final double PAR = 100;
  private static final double COUPON = 0.05;

  static {
    final DoubleTimeSeries<ZonedDateTime> dts1 = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtil.getUTCDate(2011, 4, 1)}, new double[] {1});
    final DoubleTimeSeries<ZonedDateTime> dts2 = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtil.getUTCDate(2011, 4, 1), DateUtil.getUTCDate(2012, 4, 1)},
        new double[] {COUPON, COUPON + 1});
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
    assertEquals(CALCULATOR.calculate(CF1, PAR), 1, EPS);
    assertEquals(CALCULATOR.calculate(CF2, PAR), 0.05, EPS);
  }
}
