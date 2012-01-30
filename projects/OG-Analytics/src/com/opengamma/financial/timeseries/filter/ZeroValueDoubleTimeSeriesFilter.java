/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.colt.Arrays;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class ZeroValueDoubleTimeSeriesFilter extends TimeSeriesFilter {
  private static final Logger s_logger = LoggerFactory.getLogger(ZeroValueDoubleTimeSeriesFilter.class);
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = new ArrayLocalDateDoubleTimeSeries();
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
    final ObjectIterator<Int2DoubleMap.Entry> iter = x.iteratorFast();
    Int2DoubleMap.Entry entry;
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
    return new FilteredTimeSeries(new ArrayLocalDateDoubleTimeSeries(new FastArrayIntDoubleTimeSeries(encoding, Arrays.trimToCapacity(filteredDates, i), Arrays.trimToCapacity(filteredData, i))),
        new ArrayLocalDateDoubleTimeSeries(new FastArrayIntDoubleTimeSeries(encoding, Arrays.trimToCapacity(rejectedDates, j), Arrays.trimToCapacity(rejectedData, j))));
  }
}
