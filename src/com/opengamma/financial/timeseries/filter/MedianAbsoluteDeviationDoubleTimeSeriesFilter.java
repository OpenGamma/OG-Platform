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

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.statistics.descriptive.MedianCalculator;
import com.opengamma.math.statistics.descriptive.SampleMedianAbsoluteDeviationCalculator;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class MedianAbsoluteDeviationDoubleTimeSeriesFilter extends DoubleTimeSeriesFilter {
  private static final Logger s_Log = LoggerFactory.getLogger(StandardDeviationDoubleTimeSeriesFilter.class);
  private final DoubleTimeSeriesStatisticsCalculator _medianCalculator = new DoubleTimeSeriesStatisticsCalculator(new MedianCalculator());
  private final DoubleTimeSeriesStatisticsCalculator _madCalculator = new DoubleTimeSeriesStatisticsCalculator(new SampleMedianAbsoluteDeviationCalculator());
  private double _standardDeviations;

  public MedianAbsoluteDeviationDoubleTimeSeriesFilter(final double standardDeviations) {
    if (standardDeviations < 0) {
      s_Log.info("Standard deviation was negative; using absolute value");
    }
    _standardDeviations = Math.abs(standardDeviations);
  }

  public void setStandardDeviations(final double standardDeviations) {
    if (standardDeviations < 0) {
      s_Log.info("Standard deviation was negative; using absolute value");
    }
    _standardDeviations = Math.abs(standardDeviations);
  }

  @Override
  public FilteredDoubleTimeSeries evaluate(final DoubleTimeSeries ts) {
    if (ts == null)
      throw new IllegalArgumentException("Time series was null");
    if (ts.isEmpty())
      throw new IllegalArgumentException("Time series was empty");
    final double median = _medianCalculator.evaluate(ts);
    final double mad = _madCalculator.evaluate(ts);
    final List<ZonedDateTime> filteredDates = new ArrayList<ZonedDateTime>();
    final List<Double> filteredData = new ArrayList<Double>();
    final List<ZonedDateTime> rejectedDates = new ArrayList<ZonedDateTime>();
    final List<Double> rejectedData = new ArrayList<Double>();
    final Iterator<Entry<ZonedDateTime, Double>> iter = ts.iterator();
    Entry<ZonedDateTime, Double> entry;
    while (iter.hasNext()) {
      entry = iter.next();
      if (Math.abs(entry.getValue() - median) > _standardDeviations * mad / .6745) {
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
