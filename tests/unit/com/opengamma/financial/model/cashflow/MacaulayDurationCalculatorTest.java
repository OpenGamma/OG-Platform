/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class MacaulayDurationCalculatorTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Long DATE = 0l;
  private static final DateTimeNumericEncoding ENCODING = DateTimeNumericEncoding.DATE_EPOCH_DAYS;
  private static final long[] DATES = new long[] {1l, 2l, 3l, 4l};
  private static final DoubleTimeSeries<Long> ZERO = new FastArrayLongDoubleTimeSeries(ENCODING, DATES, new double[] {0., 0., 0., 1.});
  private static final DoubleTimeSeries<Long> LOW_CF = new FastArrayLongDoubleTimeSeries(ENCODING, DATES, new double[] {0.03, 0.03, 0.03, 1.03});
  private static final DoubleTimeSeries<Long> CF = new FastArrayLongDoubleTimeSeries(ENCODING, DATES, new double[] {0.06, 0.06, 0.06, 1.06});
  private static final DoubleTimeSeries<Long> HIGH_CF = new FastArrayLongDoubleTimeSeries(ENCODING, DATES, new double[] {0.09, 0.09, 0.09, 1.09});
  private static final MacaulayDurationCalculator CALCULATOR = new MacaulayDurationCalculator();
  private static final PresentValueCalculator DISCRETE = new DiscreteCompoundingPresentValueCalculator();
  private static final PresentValueCalculator CONTINUOUS = new ContinuousCompoundingPresentValueCalculator();
  private static final double PRICE = 1.0362;
  private static final double EPS = 1e-6;

  @Test(expected = IllegalArgumentException.class)
  public void testCF() {
    CALCULATOR.calculate(null, PRICE, DATE, DISCRETE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    CALCULATOR.calculate(FastArrayLongDoubleTimeSeries.EMPTY_SERIES, PRICE, DATE, DISCRETE);
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
    for (int i = 0; i < 100; i++) {
      assertEquals(CALCULATOR.calculate(ZERO, RANDOM.nextDouble(), DATE, DISCRETE), 4, EPS);
      assertEquals(CALCULATOR.calculate(ZERO, RANDOM.nextDouble(), DATE, CONTINUOUS), 4, EPS);
    }
    final double discrete = CALCULATOR.calculate(CF, PRICE, DATE, DISCRETE);
    assertEquals(discrete, 3.68, 1e-2);
    assertTrue(CALCULATOR.calculate(LOW_CF, PRICE, DATE, DISCRETE) > discrete);
    assertTrue(CALCULATOR.calculate(HIGH_CF, PRICE, DATE, DISCRETE) < discrete);
    final double continuous = CALCULATOR.calculate(CF, PRICE, DATE, CONTINUOUS);
    assertEquals(continuous, 3.68, 1e-2);
    assertTrue(CALCULATOR.calculate(LOW_CF, PRICE, DATE, CONTINUOUS) > continuous);
    assertTrue(CALCULATOR.calculate(HIGH_CF, PRICE, DATE, CONTINUOUS) < continuous);
  }
}
