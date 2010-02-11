/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.etl;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.TimeZone;

import org.junit.Test;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.var.historical.HistoricalNormalVaRCalculator;
import com.opengamma.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * @author emcleod
 * 
 */
public class NormalLinearExpectedTailLossCalculatorTest {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0., 0.3);
  private static final DoubleTimeSeries RETURNS;
  private static final NormalLinearExpectedTailLossCalculator CALCULATOR = new NormalLinearExpectedTailLossCalculator(new DoubleTimeSeriesStatisticsCalculator(
      new SampleStandardDeviationCalculator()));

  static {
    final int n = 500000;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final TimeZone[] zones = new TimeZone[n];
    for (int i = 0; i < n; i++) {
      times[i] = i + 1;
      data[i] = NORMAL.nextRandom();
    }
    RETURNS = new ArrayDoubleTimeSeries(times, data, zones);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadConstructor() {
    new HistoricalNormalVaRCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    CALCULATOR.evaluate(null, 200, 10, 0.95);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTS() {
    CALCULATOR.evaluate(ArrayDoubleTimeSeries.EMPTY_SERIES, 200, 10, 0.95);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePeriod() {
    CALCULATOR.evaluate(RETURNS, -200, 10, 0.95);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeHorizon() {
    CALCULATOR.evaluate(RETURNS, 200, -10, 0.95);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeQuantile() {
    CALCULATOR.evaluate(RETURNS, 200, 10, -0.95);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighQuantile() {
    CALCULATOR.evaluate(RETURNS, 200, 100, 1.05);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(RETURNS, 250, 10, 0.99), 0.1601, 1e-4);
  }
}
