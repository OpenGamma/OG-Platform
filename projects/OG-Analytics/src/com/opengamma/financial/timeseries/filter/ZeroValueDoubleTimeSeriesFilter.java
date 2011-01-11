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

import cern.colt.Arrays;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * 
 */
public class ZeroValueDoubleTimeSeriesFilter extends TimeSeriesFilter {
  private static final Logger s_logger = LoggerFactory.getLogger(ZeroValueDoubleTimeSeriesFilter.class);
  private double _zero;

  public ZeroValueDoubleTimeSeriesFilter() {
    _zero = 1e-15;
  }

  public ZeroValueDoubleTimeSeriesFilter(final double zero) {
    ArgumentChecker.notNegative(zero, "zero");
    _zero = zero;
  }

  public void setZero(final double zero) {
    ArgumentChecker.notNegative(zero, "zero");
    _zero = zero;
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
    int i = 0, j = 0;
    while (iter.hasNext()) {
      entry = iter.next();
      if (Math.abs(entry.getValue()) < _zero) {
        rejectedDates[j] = entry.getKey();
        rejectedData[j++] = entry.getValue();
      } else {
        filteredDates[i] = entry.getKey();
        filteredData[i++] = entry.getValue();
      }
    }
    final DateTimeNumericEncoding encoding = x.getEncoding();
    return new FilteredTimeSeries(new FastArrayLongDoubleTimeSeries(encoding, Arrays.trimToCapacity(filteredDates, i), Arrays.trimToCapacity(filteredData, i)),
        new FastArrayLongDoubleTimeSeries(encoding, Arrays.trimToCapacity(rejectedDates, j), Arrays.trimToCapacity(rejectedData, j)));
  }
}
