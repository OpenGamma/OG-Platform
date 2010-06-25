/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.var.historical.HistoricalVaRDataBundle;
import com.opengamma.financial.var.historical.PnLStatisticsCalculator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class NormalStatisticsTest {
  private static final Function1D<HistoricalVaRDataBundle, Double> MEAN = new PnLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator()));
  private static final Function1D<HistoricalVaRDataBundle, Double> STD = new PnLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(
      new SampleStandardDeviationCalculator()));
  private static final double X = 3;
  private static final DoubleTimeSeries<Long> PNL = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new long[] {1, 2}, new double[] {X, X});
  private static final HistoricalVaRDataBundle DATA = new HistoricalVaRDataBundle(PNL);
  private static final NormalStatistics<HistoricalVaRDataBundle> NORMAL = new NormalStatistics<HistoricalVaRDataBundle>(MEAN, STD, DATA);

  @Test(expected = IllegalArgumentException.class)
  public void testNullMeanCalculator() {
    new NormalStatistics<HistoricalVaRDataBundle>(null, STD, DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullStdCalculator() {
    new NormalStatistics<HistoricalVaRDataBundle>(MEAN, null, DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    new NormalStatistics<HistoricalVaRDataBundle>(null, STD, null);
  }

  @Test
  public void test() {
    final double eps = 1e-9;
    assertEquals(NORMAL.getMean(), X, eps);
    assertEquals(NORMAL.getStandardDeviation(), 0, eps);
  }

  @Test
  public void testHashCodeAndEquals() {
    NormalStatistics<HistoricalVaRDataBundle> normal = new NormalStatistics<HistoricalVaRDataBundle>(MEAN, STD, DATA);
    assertEquals(normal, NORMAL);
    assertEquals(normal.hashCode(), NORMAL.hashCode());
    DoubleTimeSeries<Long> pnl = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new long[] {1, 2}, new double[] {3, 5});
    HistoricalVaRDataBundle data = new HistoricalVaRDataBundle(pnl);
    normal = new NormalStatistics<HistoricalVaRDataBundle>(MEAN, MEAN, data);
    assertFalse(normal.equals(NORMAL));
    pnl = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new long[] {1, 2}, new double[] {5, 5});
    data = new HistoricalVaRDataBundle(pnl);
    normal = new NormalStatistics<HistoricalVaRDataBundle>(MEAN, STD, data);
    assertFalse(normal.equals(NORMAL));
  }
}
