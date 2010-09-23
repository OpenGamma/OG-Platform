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
import com.opengamma.math.statistics.descriptive.SampleFisherKurtosisCalculator;
import com.opengamma.math.statistics.descriptive.SampleSkewnessCalculator;
import com.opengamma.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class SkewKurtosisStatisticsTest {
  private static final Function1D<HistoricalVaRDataBundle, Double> MEAN = new PnLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator()));
  private static final Function1D<HistoricalVaRDataBundle, Double> STD = new PnLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(
      new SampleStandardDeviationCalculator()));
  private static final Function1D<HistoricalVaRDataBundle, Double> SKEW = new PnLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(new SampleSkewnessCalculator()));
  private static final Function1D<HistoricalVaRDataBundle, Double> KURTOSIS = new PnLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(
      new SampleFisherKurtosisCalculator()));
  private static final HistoricalVaRDataBundle DATA = new HistoricalVaRDataBundle(new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS,
      new long[] {1, 2, 3, 4}, new double[] {3, 4, 5, 6}));
  private static final SkewKurtosisStatistics<HistoricalVaRDataBundle> STATISTICS = new SkewKurtosisStatistics<HistoricalVaRDataBundle>(MEAN, STD, SKEW, KURTOSIS, DATA);

  @Test(expected = IllegalArgumentException.class)
  public void testNullStd() {
    new SkewKurtosisStatistics<HistoricalVaRDataBundle>(MEAN, null, SKEW, KURTOSIS, DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSkew() {
    new SkewKurtosisStatistics<HistoricalVaRDataBundle>(MEAN, STD, null, KURTOSIS, DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullKurtosis() {
    new SkewKurtosisStatistics<HistoricalVaRDataBundle>(MEAN, STD, SKEW, null, DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    new SkewKurtosisStatistics<HistoricalVaRDataBundle>(MEAN, STD, SKEW, KURTOSIS, null);
  }

  @Test
  public void testEqualsAndHashCode() {
    final Function1D<HistoricalVaRDataBundle, Double> skew = new Function1D<HistoricalVaRDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(HistoricalVaRDataBundle x) {
        return SKEW.evaluate(x) + 1;
      }

    };
    final Function1D<HistoricalVaRDataBundle, Double> kurtosis = new Function1D<HistoricalVaRDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(HistoricalVaRDataBundle x) {
        return KURTOSIS.evaluate(x) + 1;
      }

    };
    SkewKurtosisStatistics<HistoricalVaRDataBundle> statistics = new SkewKurtosisStatistics<HistoricalVaRDataBundle>(MEAN, STD, SKEW, KURTOSIS, DATA);
    assertEquals(statistics, STATISTICS);
    assertEquals(statistics.hashCode(), STATISTICS.hashCode());
    statistics = new SkewKurtosisStatistics<HistoricalVaRDataBundle>(MEAN, STD, skew, KURTOSIS, DATA);
    assertFalse(statistics.equals(STATISTICS));
    statistics = new SkewKurtosisStatistics<HistoricalVaRDataBundle>(MEAN, STD, SKEW, kurtosis, DATA);
    assertFalse(statistics.equals(STATISTICS));
  }
}
