/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.historical;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.TimeZone;

import org.junit.Test;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.SampleFisherKurtosisCalculator;
import com.opengamma.math.statistics.descriptive.SampleSkewnessCalculator;
import com.opengamma.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.math.statistics.descriptive.SampleVarianceCalculator;
import com.opengamma.math.statistics.distribution.GammaDistribution;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class HistoricalCornishFisherVaRCalculatorTest {
  private static final double MU = 0.05;
  private static final double SIGMA = 0.1;
  private static final ProbabilityDistribution<Double> NORMAL = new NormalProbabilityDistribution(MU, SIGMA);
  private static final ProbabilityDistribution<Double> GAMMA = new GammaDistribution(1, 1);
  private static final DoubleTimeSeries NORMAL_RETURNS;
  private static final DoubleTimeSeries RETURNS;
  private static final DoubleTimeSeriesStatisticsCalculator MEAN = new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator());
  private static final DoubleTimeSeriesStatisticsCalculator STDDEV = new DoubleTimeSeriesStatisticsCalculator(new SampleStandardDeviationCalculator());
  private static final DoubleTimeSeriesStatisticsCalculator VARIANCE = new DoubleTimeSeriesStatisticsCalculator(new SampleVarianceCalculator());
  private static final DoubleTimeSeriesStatisticsCalculator SKEW = new DoubleTimeSeriesStatisticsCalculator(new SampleSkewnessCalculator());
  private static final DoubleTimeSeriesStatisticsCalculator KURTOSIS = new DoubleTimeSeriesStatisticsCalculator(new SampleFisherKurtosisCalculator());
  private static final HistoricalVaRCalculator NORMAL_VAR = new HistoricalNormalVaRCalculator(STDDEV);
  private static final HistoricalVaRCalculator CF_VAR = new HistoricalCornishFisherVaRCalculator(MEAN, VARIANCE, SKEW, KURTOSIS);

  static {
    final int n = 500000;
    final long[] times = new long[n];
    final double[] normal = new double[n];
    final double[] data = new double[n];
    final TimeZone[] zones = new TimeZone[n];
    for (int i = 0; i < n; i++) {
      times[i] = i + 1;
      normal[i] = NORMAL.nextRandom();
      data[i] = GAMMA.nextRandom();
    }
    NORMAL_RETURNS = new ArrayDoubleTimeSeries(times, normal, zones);
    RETURNS = new ArrayDoubleTimeSeries(times, data, zones);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadConstructor() {
    new HistoricalNormalVaRCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    CF_VAR.evaluate(null, 200, 10, 0.99);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTS() {
    CF_VAR.evaluate(ArrayDoubleTimeSeries.EMPTY_SERIES, 200, 10, 0.99);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePeriod() {
    CF_VAR.evaluate(NORMAL_RETURNS, -200, 10, 0.99);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeHorizon() {
    CF_VAR.evaluate(NORMAL_RETURNS, 200, -10, 0.99);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeQuantile() {
    CF_VAR.evaluate(NORMAL_RETURNS, 200, 10, -0.99);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighQuantile() {
    CF_VAR.evaluate(NORMAL_RETURNS, 200, 100, 1.3);
  }

  @Test
  public void test() {
    final double horizon = 1;
    final double daysPerYear = 250;
    final double quantile = NORMAL.getCDF(MU + 3 * SIGMA);
    assertEquals(NORMAL_VAR.evaluate(NORMAL_RETURNS, daysPerYear, horizon, quantile), CF_VAR.evaluate(NORMAL_RETURNS, daysPerYear, horizon, quantile), 1e-3);
    assertEquals(35. * Math.sqrt(horizon / daysPerYear) / 6, CF_VAR.evaluate(RETURNS, daysPerYear, horizon, quantile), 1e-3);
  }
}
