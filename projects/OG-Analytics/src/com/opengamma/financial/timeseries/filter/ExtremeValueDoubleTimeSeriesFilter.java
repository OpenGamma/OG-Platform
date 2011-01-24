/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * 
 */
public class ExtremeValueDoubleTimeSeriesFilter extends TimeSeriesFilter {
  private static final Logger s_logger = LoggerFactory.getLogger(ExtremeValueDoubleTimeSeriesFilter.class);
  private double _minValue;
  private double _maxValue;

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
  public FilteredTimeSeries evaluate(final DoubleTimeSeries<?> ts) {
    Validate.notNull(ts, "ts");
    if (ts.isEmpty()) {
      s_logger.info("Time series was empty");
      return new FilteredTimeSeries(FastArrayLongDoubleTimeSeries.EMPTY_SERIES, FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
    }
    final FastLongDoubleTimeSeries x = ts.toFastLongDoubleTimeSeries();
    final int n = x.size();
    final long[] filteredDates = new long[n];
    final double[] filteredData = new double[n];
    final long[] rejectedDates = new long[n];
    final double[] rejectedData = new double[n];
    final Iterator<Entry<Long, Double>> iter = x.iterator();
    Entry<Long, Double> entry;
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
    return getFilteredSeries(x, filteredDates, filteredData, i, rejectedDates, rejectedData, j);
  }

}
