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
 * Filter that partitions the time-series points based on maximum percentage moves.
 */
public class SpikeDoubleTimeSeriesFilter extends TimeSeriesFilter {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SpikeDoubleTimeSeriesFilter.class);
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;

  private double _maxPercentageMove;

  /**
   * Creates an instance.
   * 
   * @param maxPercentageMove  the maximum percentage move, zero or greater
   */
  public SpikeDoubleTimeSeriesFilter(final double maxPercentageMove) {
    if (maxPercentageMove < 0) {
      s_logger.info("Maximum percentage move must be positive; using absolute value");
    }
    _maxPercentageMove = Math.abs(maxPercentageMove);
  }

  //-------------------------------------------------------------------------
  public void setMaxPercentageMove(final double maxPercentageMove) {
    if (maxPercentageMove < 0) {
      s_logger.info("Maximum percentage move must be positive; using absolute value");
    }
    _maxPercentageMove = Math.abs(maxPercentageMove);
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
    
    LocalDateDoubleEntryIterator it = ts.iterator();
    int firstDate = it.nextTimeFast();
    double firstValue = it.currentValue();
    int secondDate = 0;
    double secondValue = 0;
    int i = 0, j = 0;
    // handle most pairs
    while (it.hasNext()) {
      secondDate = it.nextTimeFast();
      secondValue = it.currentValue();
      if (Math.abs(firstValue / secondValue - 1) < _maxPercentageMove) {
        filteredDates[i] = firstDate;
        filteredData[i++] = firstValue;
      } else {
        rejectedDates[j] = firstDate;
        rejectedData[j++] = firstValue;
      }
      firstDate = secondDate;
      firstValue = secondValue;
    }
    // handle last pair
    if (Math.abs(secondValue / firstValue - 1) < _maxPercentageMove) {
      filteredDates[i] = secondDate;
      filteredData[i++] = secondValue;
    } else {
      rejectedDates[j] = secondDate;
      rejectedData[j++] = secondValue;
    }
    return getFilteredSeries(filteredDates, filteredData, i, rejectedDates, rejectedData, j);
  }

}
