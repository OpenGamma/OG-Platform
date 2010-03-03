/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class SpikeDoubleTimeSeriesFilter<T extends DoubleTimeSeries<?>> extends TimeSeriesFilter<T> {
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
  public FilteredTimeSeries<DoubleTimeSeries<Long>> evaluate(final T ts) {
    if (ts == null)
      throw new IllegalArgumentException("Time series was null");
    if (ts.isEmpty()) {
      s_Log.info("Time series was empty");
      return new FilteredTimeSeries<DoubleTimeSeries<Long>>(FastArrayLongDoubleTimeSeries.EMPTY_SERIES, null);
    }
    final FastLongDoubleTimeSeries x = ts.toFastLongDoubleTimeSeries();
    final int n = x.size();
    final long[] filteredDates = new long[n];
    final double[] filteredData = new double[n];
    final long[] rejectedDates = new long[n];
    final double[] rejectedData = new double[n];
    final long firstDate = x.getTime(0);
    final double firstDatum = x.getValueAt(0);
    int i = 0, j = 0;
    if (Math.abs(firstDatum / x.getValueAt(1) - 1) < _maxPercentageMove) {
      filteredDates[i] = firstDate;
      filteredData[i++] = firstDatum;
    } else {
      rejectedDates[j] = firstDate;
      rejectedData[j++] = firstDatum;
    }
    final Iterator<Entry<Long, Double>> iter = x.iterator();
    Entry<Long, Double> first = iter.next();
    Entry<Long, Double> second;
    while (iter.hasNext()) {
      second = iter.next();
      if (Math.abs(second.getValue() / first.getValue() - 1) < _maxPercentageMove) {
        filteredDates[i] = second.getKey();
        filteredData[i++] = second.getValue();
      } else {
        rejectedDates[j] = second.getKey();
        rejectedData[j++] = second.getValue();
      }
      first = second;
    }
    return getFilteredSeries(x, filteredDates, filteredData, i, rejectedDates, rejectedData, j);
  }
}
