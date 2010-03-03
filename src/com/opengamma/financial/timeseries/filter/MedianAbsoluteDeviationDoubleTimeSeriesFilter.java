/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.statistics.descriptive.MedianCalculator;
import com.opengamma.math.statistics.descriptive.robust.SampleMedianAbsoluteDeviationCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class MedianAbsoluteDeviationDoubleTimeSeriesFilter<T extends DoubleTimeSeries<?>> extends TimeSeriesFilter<T> {
  private static final Logger s_Log = LoggerFactory.getLogger(StandardDeviationDoubleTimeSeriesFilter.class);
  private final DoubleTimeSeriesStatisticsCalculator<FastLongDoubleTimeSeries> _medianCalculator = new DoubleTimeSeriesStatisticsCalculator<FastLongDoubleTimeSeries>(
      new MedianCalculator());
  private final DoubleTimeSeriesStatisticsCalculator<FastLongDoubleTimeSeries> _madCalculator = new DoubleTimeSeriesStatisticsCalculator<FastLongDoubleTimeSeries>(
      new SampleMedianAbsoluteDeviationCalculator());
  private double _standardDeviations;

  public MedianAbsoluteDeviationDoubleTimeSeriesFilter(final double standardDeviations) {
    if (standardDeviations < 0) {
      s_Log.info("Standard deviation was negative; using absolute value");
    }
    _standardDeviations = Math.abs(standardDeviations);
  }

  public void setStandardDeviations(final double standardDeviations) {
    if (standardDeviations < 0) {
      s_Log.info("Standard deviation was negative; using absolute value");
    }
    _standardDeviations = Math.abs(standardDeviations);
  }

  @Override
  public FilteredTimeSeries<DoubleTimeSeries<Long>> evaluate(final T ts) {
    if (ts == null)
      throw new IllegalArgumentException("Time series was null");
    if (ts.isEmpty()) {
      s_Log.info("Time series was empty");
      return new FilteredTimeSeries<DoubleTimeSeries<Long>>(FastArrayLongDoubleTimeSeries.EMPTY_SERIES, null);
    }
    final FastLongDoubleTimeSeries x = ts.toFastLongDoubleTimeSeries();
    final double median = _medianCalculator.evaluate(x);
    final double mad = _madCalculator.evaluate(x);
    final int n = ts.size();
    final long[] filteredDates = new long[n];
    final double[] filteredData = new double[n];
    final long[] rejectedDates = new long[n];
    final double[] rejectedData = new double[n];
    final Iterator<Entry<Long, Double>> iter = x.iterator();
    Entry<Long, Double> entry;
    int i = 0, j = 0;
    while (iter.hasNext()) {
      entry = iter.next();
      if (Math.abs(entry.getValue() - median) > _standardDeviations * mad / .6745) {
        rejectedDates[j] = entry.getKey();
        rejectedData[j++] = entry.getValue();
      } else {
        filteredDates[i] = entry.getKey();
        filteredData[i++] = entry.getValue();
      }
    }
    return getFilteredSeries(x, filteredDates, filteredData, i, rejectedDates, rejectedData, j);
  }
}
