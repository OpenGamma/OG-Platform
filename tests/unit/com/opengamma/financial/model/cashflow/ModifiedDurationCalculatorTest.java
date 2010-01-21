/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.time.DateUtil;

/**
 * @author emcleod
 * 
 */
public class ModifiedDurationCalculatorTest {
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final DoubleTimeSeries ZERO = new ArrayDoubleTimeSeries(Arrays.asList(DateUtil.getDateOffsetWithYearFraction(DATE, 1), DateUtil.getDateOffsetWithYearFraction(
      DATE, 2), DateUtil.getDateOffsetWithYearFraction(DATE, 3), DateUtil.getDateOffsetWithYearFraction(DATE, 4)), Arrays.asList(0., 0., 0., 1.));
  private static final DoubleTimeSeries LOW_CF = new ArrayDoubleTimeSeries(Arrays.asList(DateUtil.getDateOffsetWithYearFraction(DATE, 1), DateUtil.getDateOffsetWithYearFraction(
      DATE, 2), DateUtil.getDateOffsetWithYearFraction(DATE, 3), DateUtil.getDateOffsetWithYearFraction(DATE, 4)), Arrays.asList(0.03, 0.03, 0.03, 1.03));
  private static final DoubleTimeSeries CF = new ArrayDoubleTimeSeries(Arrays.asList(DateUtil.getDateOffsetWithYearFraction(DATE, 1), DateUtil.getDateOffsetWithYearFraction(DATE,
      2), DateUtil.getDateOffsetWithYearFraction(DATE, 3), DateUtil.getDateOffsetWithYearFraction(DATE, 4)), Arrays.asList(0.06, 0.06, 0.06, 1.06));
  private static final DoubleTimeSeries HIGH_CF = new ArrayDoubleTimeSeries(Arrays.asList(DateUtil.getDateOffsetWithYearFraction(DATE, 1), DateUtil.getDateOffsetWithYearFraction(
      DATE, 2), DateUtil.getDateOffsetWithYearFraction(DATE, 3), DateUtil.getDateOffsetWithYearFraction(DATE, 4)), Arrays.asList(0.09, 0.09, 0.09, 1.09));
  private static final ModifiedDurationCalculator CALCULATOR = new ModifiedDurationCalculator();
  private static final PresentValueCalculator DISCRETE = new DiscreteCompoundingPresentValueCalculator();
  private static final PresentValueCalculator CONTINUOUS = new ContinuousCompoundingPresentValueCalculator();
  private static final double PRICE = 0.987;
  private static final double EPS = 1e-4;

  @Test(expected = IllegalArgumentException.class)
  public void testCF() {
    CALCULATOR.calculate(null, PRICE, DATE, DISCRETE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    CALCULATOR.calculate(ArrayDoubleTimeSeries.EMPTY_SERIES, PRICE, DATE, DISCRETE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrice() {
    CALCULATOR.calculate(CF, -1, DATE, DISCRETE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDate() {
    CALCULATOR.calculate(CF, PRICE, null, DISCRETE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPV() {
    CALCULATOR.calculate(CF, PRICE, DATE, null);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.calculate(ZERO, 1, DATE, DISCRETE), 4, EPS);
    double d = CALCULATOR.calculate(CF, PRICE, DATE, DISCRETE);
    assertTrue(d < CALCULATOR.calculate(LOW_CF, PRICE, DATE, DISCRETE));
    assertTrue(d > CALCULATOR.calculate(HIGH_CF, PRICE, DATE, DISCRETE));
    d = CALCULATOR.calculate(CF, PRICE, DATE, CONTINUOUS);
    assertTrue(d < CALCULATOR.calculate(LOW_CF, PRICE, DATE, CONTINUOUS));
    assertTrue(d > CALCULATOR.calculate(HIGH_CF, PRICE, DATE, CONTINUOUS));
  }

}
