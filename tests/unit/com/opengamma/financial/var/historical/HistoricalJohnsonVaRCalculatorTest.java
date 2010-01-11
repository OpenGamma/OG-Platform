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
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class HistoricalJohnsonVaRCalculatorTest {
  private static final DoubleTimeSeriesStatisticsCalculator MEAN = new DummyFunction(14.399);
  private static final DoubleTimeSeriesStatisticsCalculator STD = new DummyFunction(0.9036);
  private static final DoubleTimeSeriesStatisticsCalculator SKEW = new DummyFunction(-Math.sqrt(0.829));
  private static final DoubleTimeSeriesStatisticsCalculator KURTOSIS = new DummyFunction(1.863);
  private static final HistoricalJohnsonVaRCalculator CALCULATOR = new HistoricalJohnsonVaRCalculator(MEAN, STD, SKEW, KURTOSIS);
  private static final DoubleTimeSeries DUMMY_TS = new ArrayDoubleTimeSeries(new long[] { 1 }, new double[] { 2 }, new TimeZone[] { TimeZone.UTC });

  @Test(expected = IllegalArgumentException.class)
  public void testMean() {
    new HistoricalJohnsonVaRCalculator(null, STD, SKEW, KURTOSIS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStd() {
    new HistoricalJohnsonVaRCalculator(MEAN, null, SKEW, KURTOSIS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSkew() {
    new HistoricalJohnsonVaRCalculator(MEAN, STD, null, KURTOSIS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testKurtosis() {
    new HistoricalJohnsonVaRCalculator(MEAN, STD, SKEW, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    CALCULATOR.evaluate(null, 1, 1, 0.5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    CALCULATOR.evaluate(ArrayDoubleTimeSeries.EMPTY_SERIES, 1, 1, 0.5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePeriods() {
    CALCULATOR.evaluate(DUMMY_TS, -1, 1, 0.5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeHorizon() {
    CALCULATOR.evaluate(DUMMY_TS, 1, -1, 0.5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowQuantile() {
    CALCULATOR.evaluate(DUMMY_TS, 1, 1, -0.5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighQuantile() {
    CALCULATOR.evaluate(DUMMY_TS, 1, 1, 1.5);
  }

  @Test
  public void test() {
    final ProbabilityDistribution<Double> normal = new NormalDistribution(0, 1);
    final double eps = 1e-6;
    final double percent = 0.99;
    final double z = normal.getInverseCDF(percent);
    final double delta = 2.639891754;
    final double gamma = 2.366016653;
    final double lambda = 1.523184096;
    final double ksi = 16.07010570;
    final double var = -lambda * Math.sinh((-z - gamma) / delta) - ksi;
    assertEquals(CALCULATOR.evaluate(DUMMY_TS, 1, 1, percent), var, eps);
  }

  private static class DummyFunction extends DoubleTimeSeriesStatisticsCalculator {

    public DummyFunction(final double value) {
      super(new Function1D<Double[], Double>() {

        @Override
        public Double evaluate(final Double[] x) {
          return value;
        }

      });
    }
  }
}
