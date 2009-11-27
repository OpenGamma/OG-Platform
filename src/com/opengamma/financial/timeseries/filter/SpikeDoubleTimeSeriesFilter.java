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
public class SpikeDoubleTimeSeriesFilter extends DoubleTimeSeriesFilter {
  private static final Logger s_Log = LoggerFactory.getLogger(SpikeDoubleTimeSeriesFilter.class);
  private double _maxPercentageMove;

  public SpikeDoubleTimeSeriesFilter(final double maxPercentageMove) {
    if (maxPercentageMove < 0) {
      s_Log.info("Maximum percentage move must be positive; using absolute value");
    }
    _maxPercentageMove = Math.abs(maxPercentageMove);
  }

  public void setMaxPercentageMove(final double maxPercentageMove) {
    if (maxPercentageMove < 0) {
      s_Log.info("Maximum percentage move must be positive; using absolute value");
    }
    _maxPercentageMove = Math.abs(maxPercentageMove);
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
    final ZonedDateTime firstDate = ts.getTime(0);
    final Double firstDatum = ts.getValue(0);
    if (Math.abs(firstDatum / ts.getValue(1) - 1) < _maxPercentageMove) {
      filteredDates.add(firstDate);
      filteredData.add(firstDatum);
    } else {
      rejectedDates.add(firstDate);
      rejectedData.add(firstDatum);
    }
    final Iterator<Entry<ZonedDateTime, Double>> iter = ts.iterator();
    Entry<ZonedDateTime, Double> first = iter.next();
    Entry<ZonedDateTime, Double> second;
    while (iter.hasNext()) {
      second = iter.next();
      if (Math.abs(second.getValue() / first.getValue() - 1) < _maxPercentageMove) {
        filteredDates.add(second.getKey());
        filteredData.add(second.getValue());
      } else {
        rejectedDates.add(second.getKey());
        rejectedData.add(second.getValue());
      }
      first = second;
    }
    return new FilteredDoubleTimeSeries(new ArrayDoubleTimeSeries(filteredDates, filteredData), new ArrayDoubleTimeSeries(rejectedDates, rejectedData));
  }
}
