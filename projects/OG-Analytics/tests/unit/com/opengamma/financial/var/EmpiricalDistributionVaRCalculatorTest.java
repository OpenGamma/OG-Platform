/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 *
 */
public class EmpiricalDistributionVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = 0.9;
  private static final EmpiricalDistributionVaRCalculator CALCULATOR = new EmpiricalDistributionVaRCalculator();
  private static final EmpiricalDistributionVaRParameters PARAMETERS = new EmpiricalDistributionVaRParameters(HORIZON, PERIODS, QUANTILE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS1() {
    CALCULATOR.evaluate(PARAMETERS, (DoubleTimeSeries<?>[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullParameters() {
    CALCULATOR.evaluate(null, new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new long[] {1, 2}, new double[] {0.06, 0.07}));
  }
  
  @Test
  public void test() {
    final int n = 10;
    final Long[] t = new Long[n];
    final Double[] pnl = new Double[n];
    for (int i = 0; i < 10; i++) {
      t[i] = Long.valueOf(i);
      pnl[i] = i / 10. - 0.5;
    }
    final DoubleTimeSeries<?> ts = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, t, pnl);
    assertEquals(CALCULATOR.evaluate(PARAMETERS, ts), -0.082, 1e-7);
  }
}
