/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.equity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.timeseries.returns.ContinuouslyCompoundedTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.math.statistics.descriptive.GeometricMeanCalculator;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.SampleCovarianceCalculator;
import com.opengamma.math.statistics.descriptive.SampleVarianceCalculator;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class CAPMCalculatorsTest {
  private static final double HIGH_BETA = 2;
  private static final double LOW_BETA = -0.02;
  private static final double RISK_FREE = 0.04;
  private static final DoubleTimeSeries<?> MARKET_TS;
  private static final DoubleTimeSeries<?> RISK_FREE_TS;
  private static final DoubleTimeSeries<?> HIGH_BETA_TS;
  private static final DoubleTimeSeries<?> LOW_BETA_TS;
  private static final double EPS = 1e-12;
  private static final DoubleTimeSeriesStatisticsCalculator COVARIANCE = new DoubleTimeSeriesStatisticsCalculator(new SampleCovarianceCalculator());
  private static final DoubleTimeSeriesStatisticsCalculator VARIANCE = new DoubleTimeSeriesStatisticsCalculator(new SampleVarianceCalculator());
  private static final TimeSeriesReturnCalculator RETURNS = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  private static final CAPMBetaCalculator BETA = new CAPMBetaCalculator(RETURNS, COVARIANCE, VARIANCE);
  private static final DoubleTimeSeriesStatisticsCalculator GEOMETRIC_MEAN = new DoubleTimeSeriesStatisticsCalculator(new GeometricMeanCalculator());
  private static final DoubleTimeSeriesStatisticsCalculator ARITHMETIC_MEAN = new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator());
  private static final CAPMExpectedReturnCalculator RETURN = new CAPMExpectedReturnCalculator(RETURNS, GEOMETRIC_MEAN, ARITHMETIC_MEAN);

  static {
    final int n = 1000;
    final long[] t = new long[n];
    final double[] r0 = new double[n];
    final double[] r1 = new double[n];
    final double[] r2 = new double[n];
    final double[] r3 = new double[n];
    for (int i = 0; i < n; i++) {
      t[i] = i;
      r0[i] = Math.random() - 0.5;
      r1[i] = RISK_FREE;
      r2[i] = r0[i] * HIGH_BETA;
      r3[i] = r0[i] * LOW_BETA;
    }
    final double[] x0 = new double[n];
    final double[] x1 = r1;
    final double[] x2 = new double[n];
    final double[] x3 = new double[n];
    x0[0] = (1 + Math.random()) * Math.exp(r0[0]);
    x2[0] = (1 + Math.random()) * Math.exp(r2[0]);
    x3[0] = (1 + Math.random()) * Math.exp(r3[0]);
    for (int i = 1; i < n; i++) {
      x0[i] = x0[i - 1] * Math.exp(r0[i]);
      x2[i] = x2[i - 1] * Math.exp(r2[i]);
      x3[i] = x3[i - 1] * Math.exp(r3[i]);
    }
    MARKET_TS = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, t, x0);
    RISK_FREE_TS = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, t, x1);
    HIGH_BETA_TS = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, t, x2);
    LOW_BETA_TS = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, t, x3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullReturnCalculator() {
    new CAPMBetaCalculator(null, COVARIANCE, VARIANCE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCovarianceCalculator() {
    new CAPMBetaCalculator(RETURNS, null, VARIANCE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVarianceCalculator() {
    new CAPMBetaCalculator(RETURNS, COVARIANCE, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMarketReturnCalculator() {
    new CAPMExpectedReturnCalculator(null, GEOMETRIC_MEAN, ARITHMETIC_MEAN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullExpectedMarketReturnCalculator() {
    new CAPMExpectedReturnCalculator(RETURNS, null, ARITHMETIC_MEAN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullExpectedRiskFreeRateCalculator() {
    new CAPMExpectedReturnCalculator(RETURNS, GEOMETRIC_MEAN, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTSArray() {
    BETA.evaluate((DoubleTimeSeries<?>[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLength() {
    BETA.evaluate(MARKET_TS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShortTS() {
    BETA.evaluate(new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new long[] {1}, new double[] {1}), new FastArrayLongDoubleTimeSeries(
        DateTimeNumericEncoding.DATE_EPOCH_DAYS, new long[] {1}, new double[] {1}));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMismatchingDates() {
    final Long[] t = (Long[]) MARKET_TS.timesArray();
    t[1] = t[1] * 100;
    final DoubleTimeSeries<?> ts = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, t, MARKET_TS.valuesArray());
    BETA.evaluate(MARKET_TS, ts);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMarketPriceTS() {
    RETURN.evaluate(null, RISK_FREE_TS, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullRiskFreeTS() {
    RETURN.evaluate(MARKET_TS, null, 0);
  }

  @Test
  public void testBetas() {
    assertEquals(BETA.evaluate(MARKET_TS, MARKET_TS), 1, EPS);
    assertEquals(BETA.evaluate(RISK_FREE_TS, MARKET_TS), 0, EPS);
    assertEquals(BETA.evaluate(HIGH_BETA_TS, MARKET_TS), HIGH_BETA, EPS);
    assertEquals(BETA.evaluate(LOW_BETA_TS, MARKET_TS), LOW_BETA, EPS);
  }

  @Test
  public void testExpectedReturns() {
    assertEquals(RETURN.evaluate(MARKET_TS, RISK_FREE_TS, 1), 0, EPS);
    assertEquals(RETURN.evaluate(MARKET_TS, RISK_FREE_TS, HIGH_BETA), (1 - HIGH_BETA) * RISK_FREE, EPS);
    assertEquals(RETURN.evaluate(MARKET_TS, RISK_FREE_TS, LOW_BETA), (1 - LOW_BETA) * RISK_FREE, EPS);
  }
}
