package com.opengamma.financial.timeseries.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.time.InstantProvider;

import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperations;

public class DoubleTimeSeriesAutocorrelation {

  public static double getAutocorrelation(DoubleTimeSeries ts, int lag) {
    // if(lag == 0) return 1;
    DoubleTimeSeries lagged = DoubleTimeSeriesOperations.lag(ts, lag);
    List<InstantProvider> dates = new ArrayList<InstantProvider>();
    List<Double> data = new ArrayList<Double>();
    for (Map.Entry<InstantProvider, Double> entry : lagged) {
      dates.add(entry.getKey());
      data.add(ts.getDataPoint(entry.getKey()));
    }
    return DoubleTimeSeriesComparisonStatistics.getCorrelation(lagged, new ArrayDoubleTimeSeries(dates, data));
  }

  public static List<Double> getAutocorrelationSeries(DoubleTimeSeries ts, int max) {
    // TODO check for max < size
    List<Double> result = new ArrayList<Double>();
    for (int i = 0; i <= max; i++) {
      result.add(getAutocorrelation(ts, i));
    }
    return result;
  }

  /**
   * Moran
   */
  public static double getStandardError(DoubleTimeSeries ts, int max) {
    int n = ts.size();
    return (n - max) / (n * (n + 2));
  }
}
