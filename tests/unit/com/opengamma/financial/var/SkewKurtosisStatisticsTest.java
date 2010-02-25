/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import javax.time.calendar.TimeZone;

import org.junit.Test;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.var.historical.HistoricalVaRDataBundle;
import com.opengamma.financial.var.historical.PnLStatisticsCalculator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.SampleFisherKurtosisCalculator;
import com.opengamma.math.statistics.descriptive.SampleSkewnessCalculator;
import com.opengamma.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;

/**
 * @author emcleod
 * 
 */
public class SkewKurtosisStatisticsTest {
  private static final Function1D<HistoricalVaRDataBundle, Double> MEAN = new PnLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator()));
  private static final Function1D<HistoricalVaRDataBundle, Double> STD = new PnLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(
      new SampleStandardDeviationCalculator()));
  private static final Function1D<HistoricalVaRDataBundle, Double> SKEW = new PnLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(new SampleSkewnessCalculator()));
  private static final Function1D<HistoricalVaRDataBundle, Double> KURTOSIS = new PnLStatisticsCalculator(new DoubleTimeSeriesStatisticsCalculator(
      new SampleFisherKurtosisCalculator()));
  private static final HistoricalVaRDataBundle DATA = new HistoricalVaRDataBundle(new ArrayDoubleTimeSeries(new long[] { 1, 2, 3, 4 }, new double[] { 3, 3, 3, 3 }, new TimeZone[] {
      TimeZone.UTC, TimeZone.UTC, TimeZone.UTC, TimeZone.UTC }));

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
  public void testNulData() {
    new SkewKurtosisStatistics<HistoricalVaRDataBundle>(MEAN, STD, SKEW, KURTOSIS, null);
  }

}
