/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.historical;

import javax.time.calendar.TimeZone;

import org.junit.Test;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.function.Function1D;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class HistoricalJohnsonVaRCalculatorTest {
  private static final DoubleTimeSeriesStatisticsCalculator MEAN = new DoubleTimeSeriesStatisticsCalculator(new Function1D<Double[], Double>() {
    @Override
    public Double evaluate(final Double[] x) {
      return 0.02;
    }
  });
  private static final DoubleTimeSeriesStatisticsCalculator STDDEV = new DoubleTimeSeriesStatisticsCalculator(new Function1D<Double[], Double>() {
    @Override
    public Double evaluate(final Double[] x) {
      return 0.25;
    }
  });
  private static final DoubleTimeSeriesStatisticsCalculator SKEW = new DoubleTimeSeriesStatisticsCalculator(new Function1D<Double[], Double>() {
    @Override
    public Double evaluate(final Double[] x) {
      return -0.2;
    }
  });
  private static final DoubleTimeSeriesStatisticsCalculator KURTOSIS = new DoubleTimeSeriesStatisticsCalculator(new Function1D<Double[], Double>() {
    @Override
    public Double evaluate(final Double[] x) {
      return 7.;
    }
  });
  private static final HistoricalVaRCalculator CALCULATOR = new HistoricalJohnsonVaRCalculator(MEAN, STDDEV, SKEW, KURTOSIS);

  @Test
  public void test() {
    CALCULATOR.evaluate(new ArrayDoubleTimeSeries(new long[] { 1 }, new double[] { 2 }, new TimeZone[] { TimeZone.UTC }), 250, 10, 0.99);
  }
}
