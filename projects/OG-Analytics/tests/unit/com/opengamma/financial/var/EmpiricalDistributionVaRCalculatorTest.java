/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

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
  private static final EmpiricalDistributionVaRCalculator CALCULATOR = new EmpiricalDistributionVaRCalculator(HORIZON, PERIODS, QUANTILE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeHorizon() {
    new EmpiricalDistributionVaRCalculator(-HORIZON, PERIODS, QUANTILE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePeriods() {
    new EmpiricalDistributionVaRCalculator(HORIZON, -PERIODS, QUANTILE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighQuantile() {
    new EmpiricalDistributionVaRCalculator(HORIZON, PERIODS, QUANTILE + 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowQuantile() {
    new EmpiricalDistributionVaRCalculator(HORIZON, PERIODS, QUANTILE - 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS1() {
    CALCULATOR.evaluate((DoubleTimeSeries<?>[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyArray() {
    CALCULATOR.evaluate(new DoubleTimeSeries<?>[0]);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS2() {
    CALCULATOR.evaluate(new DoubleTimeSeries<?>[] {null});
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

  @Test
  public void testEqualsAndHashCode() {
    assertEquals(CALCULATOR.getHorizon(), HORIZON, 0);
    assertEquals(CALCULATOR.getPeriods(), PERIODS, 0);
    assertEquals(CALCULATOR.getQuantile(), QUANTILE, 0);
    EmpiricalDistributionVaRCalculator other = new EmpiricalDistributionVaRCalculator(HORIZON, PERIODS, QUANTILE);
    assertEquals(other, CALCULATOR);
    assertEquals(other.hashCode(), CALCULATOR.hashCode());
    other = new EmpiricalDistributionVaRCalculator(HORIZON + 1, PERIODS, QUANTILE);
    assertFalse(other.equals(CALCULATOR));
    other = new EmpiricalDistributionVaRCalculator(HORIZON, PERIODS + 1, QUANTILE);
    assertFalse(other.equals(CALCULATOR));
    other = new EmpiricalDistributionVaRCalculator(HORIZON, PERIODS, QUANTILE * 0.5);
    assertFalse(other.equals(CALCULATOR));
  }
}
