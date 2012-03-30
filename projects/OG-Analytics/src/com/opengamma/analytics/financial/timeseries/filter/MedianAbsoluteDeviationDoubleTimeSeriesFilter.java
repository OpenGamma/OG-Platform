/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.filter;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.math.statistics.descriptive.MedianCalculator;
import com.opengamma.analytics.math.statistics.descriptive.robust.SampleMedianAbsoluteDeviationCalculator;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class MedianAbsoluteDeviationDoubleTimeSeriesFilter extends TimeSeriesFilter {
  private static final Logger s_logger = LoggerFactory.getLogger(StandardDeviationDoubleTimeSeriesFilter.class);
  private final DoubleTimeSeriesStatisticsCalculator _medianCalculator = new DoubleTimeSeriesStatisticsCalculator(new MedianCalculator());
  private final DoubleTimeSeriesStatisticsCalculator _madCalculator = new DoubleTimeSeriesStatisticsCalculator(new SampleMedianAbsoluteDeviationCalculator());
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = new ArrayLocalDateDoubleTimeSeries();
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
  public FilteredTimeSeries evaluate(final LocalDateDoubleTimeSeries ts) {
    Validate.notNull(ts, "ts");
    if (ts.isEmpty()) {
      s_logger.info("Time series was empty");
      return new FilteredTimeSeries(EMPTY_SERIES, EMPTY_SERIES);
    }
    final FastIntDoubleTimeSeries x = ts.toFastIntDoubleTimeSeries();
    final double median = _medianCalculator.evaluate(x);
    final double mad = _madCalculator.evaluate(x);
    final int n = ts.size();
    final int[] filteredDates = new int[n];
    final double[] filteredData = new double[n];
    final int[] rejectedDates = new int[n];
    final double[] rejectedData = new double[n];
    final ObjectIterator<Int2DoubleMap.Entry> iter = x.iteratorFast();
    Int2DoubleMap.Entry entry;
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
    return getFilteredSeries(ts, filteredDates, filteredData, i, rejectedDates, rejectedData, j);
  }
}
