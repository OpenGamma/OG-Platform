/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.cashflow.PresentValueCalculator;
import com.opengamma.financial.model.interestrate.InterestRateModel;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.math.interpolation.StepInterpolator1D;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.time.DateUtil;

/**
 * @author emcleod
 * 
 */
public class PresentValueCalculatorTest {
  private static final PresentValueCalculator CALCULATOR = new PresentValueCalculator();
  private static final DoubleTimeSeries TS;
  private static final InterestRateModel<Double> RATES;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);

  static {
    final List<ZonedDateTime> times = Arrays.asList(DateUtil.getDateOffsetWithYearFraction(DATE, 1.), DateUtil.getDateOffsetWithYearFraction(DATE, 2.), DateUtil
        .getDateOffsetWithYearFraction(DATE, 3.), DateUtil.getDateOffsetWithYearFraction(DATE, 4.), DateUtil.getDateOffsetWithYearFraction(DATE, 5.));
    final List<Double> cf = Arrays.asList(.1, .1, .1, .1, 1.1);
    final Map<Double, Double> rates = new HashMap<Double, Double>();
    rates.put(1., 0.04);
    rates.put(2., 0.0425);
    rates.put(3., 0.045);
    rates.put(4., 0.0425);
    rates.put(5., 0.042);
    RATES = new InterpolatedDiscountCurve(rates, new StepInterpolator1D());
    TS = new ArrayDoubleTimeSeries(times, cf);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCF() {
    CALCULATOR.calculate(null, RATES, DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyCF() {
    CALCULATOR.calculate(ArrayDoubleTimeSeries.EMPTY_SERIES, RATES, DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testModel() {
    CALCULATOR.calculate(TS, null, DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDate() {
    CALCULATOR.calculate(TS, RATES, null);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.calculate(TS, RATES, DATE), 1.2559, 1e-4);
  }
}
