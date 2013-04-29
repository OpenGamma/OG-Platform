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
 * Filter that partitions the time-series points to be within a fixed min/max range.
 */
public class ExtremeValueDoubleTimeSeriesFilter extends TimeSeriesFilter {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExtremeValueDoubleTimeSeriesFilter.class);
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;

  private double _minValue;
  private double _maxValue;

  /**
   * Creates an instance.
   * 
   * @param minValue  the minimum value
   * @param maxValue  the maximum value
   */
  public ExtremeValueDoubleTimeSeriesFilter(final double minValue, final double maxValue) {
    if (minValue >= maxValue) {
      throw new IllegalArgumentException("Minumum value must be less than maximum value");
    }
    _minValue = minValue;
    _maxValue = maxValue;
  }

  //-------------------------------------------------------------------------
  public void setMinimumValue(final double minValue) {
    if (minValue >= _maxValue) {
      throw new IllegalArgumentException("Minimum value must be less than maximum value");
    }
    _minValue = minValue;
  }

  public void setMaximumValue(final double maxValue) {
    if (maxValue <= _minValue) {
      throw new IllegalArgumentException("Maximum value must be greater than mimumum value");
    }
    _maxValue = maxValue;
  }

  public void setRange(final double minValue, final double maxValue) {
    if (minValue >= maxValue) {
      throw new IllegalArgumentException("Minumum value must be less than maximum value");
    }
    _minValue = minValue;
    _maxValue = maxValue;
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
      if (value > _maxValue || value < _minValue) {
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
