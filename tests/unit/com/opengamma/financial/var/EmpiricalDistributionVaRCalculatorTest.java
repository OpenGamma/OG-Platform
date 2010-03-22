/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * @author emcleod
 *
 */
public class EmpiricalDistributionVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = 0.9;
  private static final Function1D<DoubleTimeSeries<?>, Double> CALCULATOR = new EmpiricalDistributionVaRCalculator(HORIZON, PERIODS, QUANTILE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    CALCULATOR.evaluate((DoubleTimeSeries<?>) null);
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
    assertEquals(CALCULATOR.evaluate(ts), -0.082, 1e-7);
  }
}
