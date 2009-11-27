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
public class NegativeValueDoubleTimeSeriesFilter extends DoubleTimeSeriesFilter {
  private static final Logger s_Log = LoggerFactory.getLogger(NegativeValueDoubleTimeSeriesFilter.class);

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
    while (iter.hasNext()) {
      entry = iter.next();
      if (entry.getValue() < 0) {
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
