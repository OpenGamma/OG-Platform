/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.bond;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.cashflow.ContinuousCompoundingPresentValueCalculator;
import com.opengamma.financial.model.cashflow.DiscreteCompoundingPresentValueCalculator;
import com.opengamma.financial.model.cashflow.PresentValueCalculator;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.time.DateUtil;

/**
 * @author emcleod
 * 
 */
public class BondYieldCalculatorTest {
  private static final BondYieldCalculator CALCULATOR = new BondYieldCalculator();
  private static final double PRICE = 1.;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final DoubleTimeSeries TS;
  private static final PresentValueCalculator DISCRETE = new DiscreteCompoundingPresentValueCalculator();
  private static final PresentValueCalculator CONTINUOUS = new ContinuousCompoundingPresentValueCalculator();

  static {
    final List<ZonedDateTime> times = Arrays.asList(DateUtil.getDateOffsetWithYearFraction(DATE, 1.), DateUtil.getDateOffsetWithYearFraction(DATE, 2.), DateUtil
        .getDateOffsetWithYearFraction(DATE, 3.), DateUtil.getDateOffsetWithYearFraction(DATE, 4.), DateUtil.getDateOffsetWithYearFraction(DATE, 5.));
    final List<Double> cf = Arrays.asList(.1, .1, .1, .1, 1.1);
    TS = new ArrayDoubleTimeSeries(times, cf);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCF() {
    CALCULATOR.calculate(null, PRICE, DATE, DISCRETE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrice() {
    CALCULATOR.calculate(TS, 0., DATE, DISCRETE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDate() {
    CALCULATOR.calculate(TS, PRICE, null, DISCRETE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPV() {
    CALCULATOR.calculate(TS, PRICE, DATE, null);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.calculate(TS, 1.2559, DATE, DISCRETE), 0.0422, 1e-4);
    assertEquals(CALCULATOR.calculate(TS, 1.2559, DATE, CONTINUOUS), 0.0413, 1e-4);
  }
}
