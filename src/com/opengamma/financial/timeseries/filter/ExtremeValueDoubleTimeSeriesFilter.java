/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class ExtremeValueDoubleTimeSeriesFilter extends DoubleTimeSeriesFilter {
  private static final Logger s_Log = LoggerFactory.getLogger(ExtremeValueDoubleTimeSeriesFilter.class);
  private double _minValue;
  private double _maxValue;

  public ExtremeValueDoubleTimeSeriesFilter(final double minValue, final double maxValue) {
    if (minValue >= maxValue)
      throw new IllegalArgumentException("Minumum value must be less than maximum value");
    _minValue = minValue;
    _maxValue = maxValue;
  }

  public void setMinimumValue(final double minValue) {
    if (minValue >= _maxValue)
      throw new IllegalArgumentException("Minimum value must be less than maximum value");
    _minValue = minValue;
  }

  public void setMaximumValue(final double maxValue) {
    if (maxValue <= _minValue)
      throw new IllegalArgumentException("Maximum value must be greater than mimumum value");
    _maxValue = maxValue;
  }

  public void setRange(final double minValue, final double maxValue) {
    if (minValue >= maxValue)
      throw new IllegalArgumentException("Minumum value must be less than maximum value");
    _minValue = minValue;
    _maxValue = maxValue;
  }

  @Override
  public FilteredDoubleTimeSeries evaluate(final DoubleTimeSeries ts) {
    if (ts == null)
      throw new IllegalArgumentException("Time series was null");
    if (ts.isEmpty()) {
      s_Log.info("Time series was empty");
      return new FilteredDoubleTimeSeries(ArrayDoubleTimeSeries.EMPTY_SERIES, null);
    }
    final List<ZonedDateTime> filteredDates = new ArrayList<ZonedDateTime>();
    final List<Double> filteredData = new ArrayList<Double>();
    final List<ZonedDateTime> rejectedDates = new ArrayList<ZonedDateTime>();
    final List<Double> rejectedData = new ArrayList<Double>();
    final Iterator<Entry<ZonedDateTime, Double>> iter = ts.iterator();
    Entry<ZonedDateTime, Double> entry;
    Double value;
    while (iter.hasNext()) {
      entry = iter.next();
      value = entry.getValue();
      if (value > _maxValue || value < _minValue) {
        rejectedDates.add(entry.getKey());
        rejectedData.add(entry.getValue());
      } else {
        filteredDates.add(entry.getKey());
        filteredData.add(entry.getValue());
      }
    }
    return new FilteredDoubleTimeSeries(new ArrayDoubleTimeSeries(filteredDates, filteredData), new ArrayDoubleTimeSeries(rejectedDates, rejectedData));
  }

}
