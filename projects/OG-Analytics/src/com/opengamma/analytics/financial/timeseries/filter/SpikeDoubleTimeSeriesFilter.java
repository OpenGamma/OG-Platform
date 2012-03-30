/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.filter;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class SpikeDoubleTimeSeriesFilter extends TimeSeriesFilter {
  private static final Logger s_logger = LoggerFactory.getLogger(SpikeDoubleTimeSeriesFilter.class);
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = new ArrayLocalDateDoubleTimeSeries();
  private double _maxPercentageMove;

  public SpikeDoubleTimeSeriesFilter(final double maxPercentageMove) {
    if (maxPercentageMove < 0) {
      s_logger.info("Maximum percentage move must be positive; using absolute value");
    }
    _maxPercentageMove = Math.abs(maxPercentageMove);
  }

  public void setMaxPercentageMove(final double maxPercentageMove) {
    if (maxPercentageMove < 0) {
      s_logger.info("Maximum percentage move must be positive; using absolute value");
    }
    _maxPercentageMove = Math.abs(maxPercentageMove);
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
    final int firstDate = x.getTimeAt(0);
    final double firstDatum = x.getValueAt(0);
    int i = 0, j = 0;
    if (Math.abs(firstDatum / x.getValueAt(1) - 1) < _maxPercentageMove) {
      filteredDates[i] = firstDate;
      filteredData[i++] = firstDatum;
    } else {
      rejectedDates[j] = firstDate;
      rejectedData[j++] = firstDatum;
    }
    final ObjectIterator<Int2DoubleMap.Entry> iter = x.iteratorFast();
    Int2DoubleMap.Entry first = iter.next();
    Int2DoubleMap.Entry second;
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
    return getFilteredSeries(ts, filteredDates, filteredData, i, rejectedDates, rejectedData, j);
  }
}
