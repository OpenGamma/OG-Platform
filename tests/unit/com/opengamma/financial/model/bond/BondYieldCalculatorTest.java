/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.bond;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.opengamma.financial.model.cashflow.ContinuousCompoundingPresentValueCalculator;
import com.opengamma.financial.model.cashflow.DiscreteCompoundingPresentValueCalculator;
import com.opengamma.financial.model.cashflow.PresentValueCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class BondYieldCalculatorTest {
  private static final BondYieldCalculator CALCULATOR = new BondYieldCalculator();
  private static final double PRICE = 1.;
  private static final Long DATE = 0l;
  private static final DoubleTimeSeries<Long> TS;
  private static final PresentValueCalculator DISCRETE = new DiscreteCompoundingPresentValueCalculator();
  private static final PresentValueCalculator CONTINUOUS = new ContinuousCompoundingPresentValueCalculator();

  static {
    final List<Long> times = Arrays.asList(1l, 2l, 3l, 4l, 5l);
    final List<Double> cf = Arrays.asList(.1, .1, .1, .1, 1.1);
    TS = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, times, cf);
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
