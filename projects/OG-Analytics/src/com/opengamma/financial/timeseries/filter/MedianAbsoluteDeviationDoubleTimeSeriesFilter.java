/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
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
 */
public class MedianAbsoluteDeviationDoubleTimeSeriesFilter extends TimeSeriesFilter {
  private static final Logger s_logger = LoggerFactory.getLogger(StandardDeviationDoubleTimeSeriesFilter.class);
  private final DoubleTimeSeriesStatisticsCalculator _medianCalculator = new DoubleTimeSeriesStatisticsCalculator(new MedianCalculator());
  private final DoubleTimeSeriesStatisticsCalculator _madCalculator = new DoubleTimeSeriesStatisticsCalculator(new SampleMedianAbsoluteDeviationCalculator());
  private double _standardDeviations;

  public MedianAbsoluteDeviationDoubleTimeSeriesFilter(final double standardDeviations) {
    if (standardDeviations < 0) {
      s_logger.info("Standard deviation was negative; using absolute value");
    }
    _standardDeviations = Math.abs(standardDeviations);
  }

  public void setStandardDeviations(final double standardDeviations) {
    if (standardDeviations < 0) {
      s_logger.info("Standard deviation was negative; using absolute value");
    }
    _standardDeviations = Math.abs(standardDeviations);
  }

  @Override
  public FilteredTimeSeries evaluate(final DoubleTimeSeries<?> ts) {
    Validate.notNull(ts, "ts");
    if (ts.isEmpty()) {
      s_logger.info("Time series was empty");
      return new FilteredTimeSeries(FastArrayLongDoubleTimeSeries.EMPTY_SERIES, FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
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
