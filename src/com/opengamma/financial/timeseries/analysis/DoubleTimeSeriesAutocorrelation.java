/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperations;

/**
 * 
 * @author emcleod
 * 
 */
public class DoubleTimeSeriesAutocorrelation {

  public static double getAutocorrelation(final DoubleTimeSeries ts, final int lag) {
    // if(lag == 0) return 1;
    final DoubleTimeSeries lagged = DoubleTimeSeriesOperations.lag(ts, lag);
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    final List<Double> data = new ArrayList<Double>();
    for (final Map.Entry<ZonedDateTime, Double> entry : lagged) {
      dates.add(entry.getKey());
      data.add(ts.getDataPoint(entry.getKey()));
    }
    return DoubleTimeSeriesComparisonStatistics.getCorrelation(lagged, new ArrayDoubleTimeSeries(dates, data));
  }

  public static List<Double> getAutocorrelationSeries(final DoubleTimeSeries ts, final int max) {
    // TODO check for max < size
    final List<Double> result = new ArrayList<Double>();
    for (int i = 0; i <= max; i++) {
      result.add(getAutocorrelation(ts, i));
    }
    return result;
  }

  /**
   * Moran
   */
  public static double getStandardError(final DoubleTimeSeries ts, final int max) {
    final int n = ts.size();
    return (n - max) / (n * (n + 2));
  }
}
