/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Filter that partitions the time-series points based on removing near zero values.
 */
public class ZeroValueDoubleTimeSeriesFilter extends TimeSeriesFilter {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ZeroValueDoubleTimeSeriesFilter.class);
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;

  private double _zero;

  /**
   * Creates an instance.
   */
  public ZeroValueDoubleTimeSeriesFilter() {
    _zero = 1e-15;
  }

  /**
   * Creates an instance.
   * 
   * @param zero  the zero tolerance, not negative
   */
  public ZeroValueDoubleTimeSeriesFilter(final double zero) {
    ArgumentChecker.notNegative(zero, "zero");
    _zero = zero;
  }

  //-------------------------------------------------------------------------
  public void setZero(final double zero) {
    ArgumentChecker.notNegative(zero, "zero");
    _zero = zero;
  }

  //-------------------------------------------------------------------------
  @Override
  public FilteredTimeSeries evaluate(final LocalDateDoubleTimeSeries ts) {
    ArgumentChecker.notNull(ts, "ts");
    if (ts.isEmpty()) {
      s_logger.info("Time series was empty");
      return new FilteredTimeSeries(EMPTY_SERIES, EMPTY_SERIES);
    }
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
      if (Math.abs(value) < _zero) {
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
