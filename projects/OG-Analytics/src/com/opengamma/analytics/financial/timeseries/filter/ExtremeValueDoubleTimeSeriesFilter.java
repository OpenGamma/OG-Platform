/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.filter;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

import java.util.Iterator;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class ExtremeValueDoubleTimeSeriesFilter extends TimeSeriesFilter {
  private static final Logger s_logger = LoggerFactory.getLogger(ExtremeValueDoubleTimeSeriesFilter.class);
  private double _minValue;
  private double _maxValue;
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = new ArrayLocalDateDoubleTimeSeries();

  public ExtremeValueDoubleTimeSeriesFilter(final double minValue, final double maxValue) {
    if (minValue >= maxValue) {
      throw new IllegalArgumentException("Minumum value must be less than maximum value");
    }
    _minValue = minValue;
    _maxValue = maxValue;
  }

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

  @Override
  public FilteredTimeSeries evaluate(final LocalDateDoubleTimeSeries ts) {
    Validate.notNull(ts, "ts");
    if (ts.isEmpty()) {
      s_logger.info("Time series was empty");
      return new FilteredTimeSeries(EMPTY_SERIES, EMPTY_SERIES);
    }
    final FastIntDoubleTimeSeries x = (FastIntDoubleTimeSeries) ts.getFastSeries();
    final int n = x.size();
    final int[] filteredDates = new int[n];
    final double[] filteredData = new double[n];
    final int[] rejectedDates = new int[n];
    final double[] rejectedData = new double[n];
    final Iterator<Int2DoubleMap.Entry> iter = x.iteratorFast();
    Int2DoubleMap.Entry entry;
    Double value;
    int i = 0, j = 0;
    while (iter.hasNext()) {
      entry = iter.next();
      value = entry.getValue();
      if (value > _maxValue || value < _minValue) {
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
