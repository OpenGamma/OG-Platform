/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.time.calendar.TimeZone;

import org.junit.Test;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * @author emcleod
 * 
 */
public class NormalStatisticsTest {
  private static final Function1D<HistoricalVaRDataBundle, Double> MEAN = new PNLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator()));
  private static final Function1D<HistoricalVaRDataBundle, Double> STD = new PNLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(
      new SampleStandardDeviationCalculator()));
  private static final double X = 3;
  private static final DoubleTimeSeries PNL = new ArrayDoubleTimeSeries(new long[] { 1, 2 }, new double[] { X, X }, new TimeZone[] { TimeZone.UTC, TimeZone.UTC });
  private static final HistoricalVaRDataBundle DATA = new HistoricalVaRDataBundle(PNL);

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalculator() {
    new NormalStatistics<HistoricalVaRDataBundle>(null, null, DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    new NormalStatistics<HistoricalVaRDataBundle>(null, STD, null);
  }

  @Test
  public void testNullMeanCalculator() {
    final NormalStatistics<HistoricalVaRDataBundle> normal = new NormalStatistics<HistoricalVaRDataBundle>(null, STD, DATA);
    assertNull(normal.getMean());
  }

  @Test
  public void test() {
    final double eps = 1e-9;
    final NormalStatistics<HistoricalVaRDataBundle> normal = new NormalStatistics<HistoricalVaRDataBundle>(MEAN, STD, DATA);
    assertEquals(normal.getMean(), X, eps);
    assertEquals(normal.getStandardDeviation(), 0, eps);
  }

}
