/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.math.statistics.descriptive.MedianCalculator;
import com.opengamma.analytics.math.statistics.descriptive.robust.SampleMedianAbsoluteDeviationCalculator;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Filter that partitions the time-series points based on a standard deviation.
 */
public class MedianAbsoluteDeviationDoubleTimeSeriesFilter extends TimeSeriesFilter {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(StandardDeviationDoubleTimeSeriesFilter.class);
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;

  private final DoubleTimeSeriesStatisticsCalculator _medianCalculator = new DoubleTimeSeriesStatisticsCalculator(new MedianCalculator());
  private final DoubleTimeSeriesStatisticsCalculator _madCalculator = new DoubleTimeSeriesStatisticsCalculator(new SampleMedianAbsoluteDeviationCalculator());
  private double _standardDeviations;

  /**
   * Creates an instance.
   * 
   * @param standardDeviations  the standard deviations, zero or greater
   */
  public MedianAbsoluteDeviationDoubleTimeSeriesFilter(final double standardDeviations) {
    if (standardDeviations < 0) {
      s_logger.info("Standard deviation was negative; using absolute value");
    }
    _standardDeviations = Math.abs(standardDeviations);
  }

  //-------------------------------------------------------------------------
  public void setStandardDeviations(final double standardDeviations) {
    if (standardDeviations < 0) {
      s_logger.info("Standard deviation was negative; using absolute value");
    }
    _standardDeviations = Math.abs(standardDeviations);
  }

  //-------------------------------------------------------------------------
  @Override
  public FilteredTimeSeries evaluate(final LocalDateDoubleTimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    if (ts.isEmpty()) {
      s_logger.info("Time series was empty");
      return new FilteredTimeSeries(EMPTY_SERIES, EMPTY_SERIES);
    }
    final double median = _medianCalculator.evaluate(ts);
    final double mad = _madCalculator.evaluate(ts);
    final int n = ts.size();
    final int[] filteredDates = new int[n];
    final double[] filteredData = new double[n];
    final int[] rejectedDates = new int[n];
    final double[] rejectedData = new double[n];
    final LocalDateDoubleEntryIterator it = ts.iterator();
    int i = 0, j = 0;
    while (it.hasNext()) {
      int date = it.nextTimeFast();
      double value = it.currentValue();
      if (Math.abs(value - median) > _standardDeviations * mad / .6745) {
        rejectedDates[j] = date;
        rejectedData[j++] = value;
      } else {
        filteredDates[i] = date;
        filteredData[i++] = value;
      }
    }
    return getFilteredSeries(filteredDates, filteredData, i, rejectedDates, rejectedData, j);
  }

}
