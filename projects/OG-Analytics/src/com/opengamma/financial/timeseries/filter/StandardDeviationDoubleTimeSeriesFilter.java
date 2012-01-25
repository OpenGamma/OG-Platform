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

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class StandardDeviationDoubleTimeSeriesFilter extends TimeSeriesFilter {
  private static final Logger s_logger = LoggerFactory.getLogger(StandardDeviationDoubleTimeSeriesFilter.class);
  private final DoubleTimeSeriesStatisticsCalculator _meanCalculator = new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator());
  private final DoubleTimeSeriesStatisticsCalculator _stdCalculator = new DoubleTimeSeriesStatisticsCalculator(new SampleStandardDeviationCalculator());
  private double _standardDeviations;
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = new ArrayLocalDateDoubleTimeSeries();

  public StandardDeviationDoubleTimeSeriesFilter(final double standardDeviations) {
    if (standardDeviations < 0) {
      s_logger.info("Standard deviation was negative; using absolute value");
    }
    _standardDeviations = Math.abs(standardDeviations);
  }

  public void setStandardDeviations(final double standardDeviations) {
    if (standardDeviations < 0) {
      s_logger.info("Standard deviation was negative; using absolute value");
    }
    _standardDeviations = Math.abs(standardDeviations);
  }

  @Override
  public FilteredTimeSeries evaluate(final LocalDateDoubleTimeSeries ts) {
    Validate.notNull(ts, "ts");
    if (ts.isEmpty()) {
      s_logger.info("Time series was empty");
      return new FilteredTimeSeries(EMPTY_SERIES, EMPTY_SERIES);
    }
    final FastIntDoubleTimeSeries x = (FastIntDoubleTimeSeries) ts.getFastSeries();
    final double mean = _meanCalculator.evaluate(x);
    final double std = _stdCalculator.evaluate(x);
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
      if (Math.abs(entry.getValue() - mean) > _standardDeviations * std) {
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
